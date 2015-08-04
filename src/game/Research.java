/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import dat.Tech;
import dat.UnitType;
import galaxyreader.Structure;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import util.C;
import util.Util;

/**
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
    public int points_left;
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
        points_left = 0;
        List<Structure> cities = game.getStructures();
        for (Structure structure : cities) {
            if (structure.owner == game.getTurn() && structure.type == C.LAB) {
                points_left += game.getEfs_ini().lab_points;
            }
        }

        for (int i = 0; i < techs.length; i++) {
            if (techs[i]) {
                points_left -= game.getGameResources().getTech()[i].stats[C.TECH_COST] / C.TECH_MAINT;
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
        points[researched] += points_left;
        int cost = game.getGameResources().getTech()[researched].stats[C.TECH_COST];
        points_left = points[researched] - cost;
        if (points_left >= 0) {
            techs[researched] = true;
            researched = 0;
            setCanBuild(game.getUnitTypes());
        } else {
            points_left = 0;
        }
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

    /**
     * Game state printout method, CSV records of research status of
     * technologies are printed.
     */
    public void record(File file) {
        Tech[] tech_list = game.getGameResources().getTech();
        Util.printString(file, " #TECHS");
        for (int i = 0; i < techs.length; i++) {
            String s = "  " + tech_list[i].name;
            if (techs[i]) {
                s += ",1";
            } else {
                s += ",0";
            }
            s += "," + points[i];
            Util.printString(file, s);
        }
    }
}
