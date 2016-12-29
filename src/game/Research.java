/*
 * Copyright (C) 2015 joulupunikki joulupunikki@gmail.communist.invalid.
 *
 *  Disclaimer of Warranties and Limitation of Liability.
 *
 *     The creators and distributors offer this software as-is and
 *     as-available, and make no representations or warranties of any
 *     kind concerning this software, whether express, implied, statutory,
 *     or other. This includes, without limitation, warranties of title,
 *     merchantability, fitness for a particular purpose, non-infringement,
 *     absence of latent or other defects, accuracy, or the presence or
 *     absence of errors, whether or not known or discoverable.
 *
 *     To the extent possible, in no event will the creators or distributors
 *     be liable on any legal theory (including, without limitation,
 *     negligence) or otherwise for any direct, special, indirect,
 *     incidental, consequential, punitive, exemplary, or other losses,
 *     costs, expenses, or damages arising out of the use of this software,
 *     even if the creators or distributors have been advised of the
 *     possibility of such losses, costs, expenses, or damages.
 *
 *     The disclaimer of warranties and limitation of liability provided
 *     above shall be interpreted in a manner that, to the extent possible,
 *     most closely approximates an absolute disclaimer and waiver of
 *     all liability.
 *
 */
package game;

import dat.Tech;
import dat.UnitType;
import galaxyreader.Structure;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import util.C;

/**
 * Technology and research related data structures and logic.
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class Research implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    //owned techs
    public boolean[] techs;
    //points researched in each tech
    public int[] points;
    //currently researched tech
    public int researched;
    //available research points
    public int rp_available;
    //for each city type, list of units which can be built there
    public ArrayList<ArrayList<int[]>> can_build;
    private Game game;

    public Research(Game game) {
        this.game = game;
        int len = game.getGameResources().getTech().length;
        techs = new boolean[len];
        points = new int[len];
        researched = 0;
        for (int i = 0; i < techs.length; i++) {
            techs[i] = false;
            points[i] = 0;
        }
        techs[0] = true;
        setCanBuild(game.getUnitTypes());
    }

    public void setResearch(int tech) {
        researched = tech;
    }

    /**
     * Calculate research points at beginning of turn.
     */
    public void initResearchPts() {
        rp_available = 0;
        List<Structure> cities = game.getStructures();
        for (Structure structure : cities) {
            if (structure.owner == game.getTurn() && structure.type == C.LAB) {
                rp_available += game.getEfs_ini().lab_points;
            }
        }

        for (int i = 0; i < techs.length; i++) {
            if (techs[i]) {
                rp_available -= game.getGameResources().getTech()[i].stats[C.TECH_COST] / C.TECH_MAINT;
            }

        }

    }

    /**
     * Researches selected tech.
     */
    public void doResearch() {
        if (researched == 0) {
            return;
        }
        points[researched] += rp_available;
        int cost = game.getGameResources().getTech()[researched].stats[C.TECH_COST];
        rp_available = points[researched] - cost;
        if (rp_available >= 0) {
            techs[researched] = true;
            researched = 0;
            setCanBuild(game.getUnitTypes());
        } else {
            rp_available = 0;
        }
    }

    /**
     * Handles receiving tech from sources other than own research.
     *
     * @param tech
     */
    public void receiveTech(int tech) {
        if (researched == tech) {
            researched = 0;
        }
        techs[tech] = true;
        setCanBuild(game.getUnitTypes()); // fix #103
    }

    /**
     * Update list of units which can be built in cities.
     *
     * @param unit_types
     */
    public void setCanBuild(UnitType[][] unit_types) {
        can_build = new ArrayList<>();
        can_build.ensureCapacity(C.CITY_TYPES);
        for (int i = 0; i < C.CITY_TYPES; i++) {
            can_build.add(new ArrayList<int[]>());
        }

        for (int i = 0; i < unit_types.length; i++) {
            for (int j = 0; j < unit_types[i].length; j++) {
                if (unit_types[i][j] == null) {
                    continue;
                }
                int building = unit_types[i][j].bldgs;
                if (building > -1) {
                    if (!checkNeededTechs(unit_types, i, j)) {
                        continue;
                    }
                    int[] unit_type = new int[2];
                    unit_type[0] = i;
                    unit_type[1] = j;
                    if (building < 99) {
                        can_build.get(building).add(unit_type);
                    } else if (building == 99) {
                        for (int k = 0; k < can_build.size(); k++) {
                            can_build.get(k).add(unit_type);
                        }
                    }
                }

            }

        }
    }

    public boolean checkNeededTechs(UnitType[][] unit_types, int type, int subtype) {
        boolean ret_val = true;
        int[] reqd_techs = unit_types[type][subtype].reqd_tech;
        for (int i : reqd_techs) {
            if (!techs[i]) {
                ret_val = false;
                break;
            }
        }
        return ret_val;
    }

    public List<Integer> getResearchableTechs() {
        Tech[] tech = game.getGameResources().getTech();
        List<Integer> researchables = new LinkedList<>();
        for (int i = 1; i < tech.length; i++) {
            if (techs[i]) {
                continue;
            }
            if (tech[i].stats[C.TECH_COST] == 0) {
                continue;
            }
            if (tech[i].stats[C.TECH0] >= 900) {
                researchables.add(tech[i].idx);
                continue;
            }
            else if (tech[i].stats[C.TECH0] >= 800) {
                continue;
            }
            if (techs[tech[i].stats[C.TECH0]]
                    && techs[tech[i].stats[C.TECH1]]
                    && techs[tech[i].stats[C.TECH2]]) {
                researchables.add(tech[i].idx);
            }
        }
        return researchables;
    }

    /**
     * Game state printout method, CSV records of research status of
     * technologies are printed.
     */
    public void record(PrintWriter pw) {
        Tech[] tech_list = game.getGameResources().getTech();
        pw.println( " #TECHS");
        for (int i = 0; i < techs.length; i++) {
            String s = "  " + tech_list[i].name;
            if (techs[i]) {
                s += ",1";
            } else {
                s += ",0";
            }
            s += "," + points[i];
            pw.println( s);
        }
    }
}
