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

import dat.EfsIni;
import galaxyreader.Structure;
import galaxyreader.Unit;
import java.io.File;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.math3.util.FastMath;
import util.C;
import util.Util;

/**
 * Will handle faction data, particularly money
 *
 * @author RSW
 */
public class Faction implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int CITY_GDP = 1000;
    private Game game;
    private EfsIni efs_ini;
    private int turn;

    private boolean eliminated;
    private int number; // faction ID
    private int firebirds;
    private int tax_rate;
    private int tithe_rate;
    private int pay_rate;
    private int debt;
    List<Message> messages = new LinkedList<>();
//    private boolean[] techs;
//    private int[] tech_costs;

    private Research research;

    public Faction(Game game, int number) {

        this.game = game;
        this.efs_ini = game.getEfs_ini();

        eliminated = false;
        this.number = number;
        firebirds = efs_ini.starting_credits;
        tax_rate = efs_ini.default_tax_rate;
        tithe_rate = efs_ini.default_tithe_rate;
        pay_rate = 100;

//        initTechs();
        initResearch();

    }

    public void addMessage(Message m) {
        messages.add(m);
    }

    public void initResearch() {
        research = new Research(game);
    }

    public Research getResearch() {
        return research;
    }

//    public void setResearch(int tech) {
//        research.researched = tech;
//    }
//
//    /**
//     * Calculate research points at beginning of turn.
//     */
//    public void initResearchPts() {
//        research.points_left = 0;
//        List<Structure> cities = game.getStructures();
//        for (Structure structure : cities) {
//            if (structure.owner == game.getTurn() && structure.type == C.LAB) {
//                research.points_left += game.getEfs_ini().lab_points;
//            }
//        }
//
//        for (int i = 0; i < research.techs.length; i++) {
//            if (research.techs[i]) {
//                research.points_left -= game.getResources().getTech()[i].stats[C.TECH_COST] / C.TECH_MAINT;
//            }
//
//        }
//
//    }
//
//    public void doResearch() {
//        if (research.researched == 0) {
//            return;
//        }
//        research.points[research.researched] += research.points_left;
//        int cost = game.getResources().getTech()[research.researched].stats[C.TECH_COST];
//        research.points_left = research.points[research.researched] - cost;
//        if (research.points_left >= 0) {
//            research.techs[research.researched] = true;
//            research.researched = 0;
//        } else {
//            research.points_left = 0;
//        }
//    }
//    public void initTechs() {
//        int len = game.getResources().getTech().length;
//        techs = new boolean[len];
//        tech_costs = new int[len];
//        for (int i = 0; i < techs.length; i++) {
//            techs[i] = false;
//            tech_costs[i] = 0;
//
//        }
//        techs[0] = true;
//    }
//    public boolean[] getTechs() {
//        return techs;
//    }
//
//    public int[] getTechCosts() {
//        return tech_costs;
//    }
    public List<Message> getMessages() {
        return messages;
    }

    public void deleteOldMessages() {
        messages.clear();
    }

//    public void deleteOldMessages(int year) {
//        ListIterator<Message> iter = messages.listIterator();
//        Message m = null;
//        while (iter.hasNext()) {
//            m = iter.next();
//            if (year > m.getYear()) {
//                iter.remove();
//            }
//        }
//
//    }
    public static Faction[] createFactions(Game game) {
        Faction[] factions = new Faction[C.NR_FACTIONS];
        for (int i = 0; i < factions.length; i++) {
            factions[i] = new Faction(game, i);

        }
        return factions;
    }

    /**
     * Game state printout method, prints out faction information.
     *
     */
    public void record(File file) {
        Util.printString(file, " " + Util.getFactionName(number) + "," + number);
        research.record(file);
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated() {
        eliminated = true;
    }

    public static void eliminateNoblelessFactions(Game game) {
        List<Unit> units = game.getUnits();
        List<Structure> cities = game.getStructures();
        // find out newly eliminated factions and set them to eliminated and
        // set their original assets to neutral/rebel faction
        boolean[] prev_no_nobles = new boolean[C.NR_HOUSES];
        for (int i = 0; i < prev_no_nobles.length; i++) {
            prev_no_nobles[i] = game.getFaction(i).isEliminated();          
        }
        boolean[] no_nobles = {true, true, true, true, true};
        for (Unit unit : units) {
            if (unit.type == C.NOBLE_UNIT_TYPE && C.HOUSE1 <= unit.prev_owner && unit.prev_owner <= C.HOUSE5) {
                no_nobles[unit.prev_owner] = false;
            }
        }
        for (int i = 0; i < no_nobles.length; i++) {
            if (no_nobles[i] ^ prev_no_nobles[i]) {
                game.getFaction(i).setEliminated();
                for (Unit unit : units) {
                    if (unit.prev_owner == i) {
                        unit.prev_owner = C.NEUTRAL;
                        unit.owner = C.NEUTRAL;
                    }
                }
                for (Structure city : cities) {
                    if (city.prev_owner == i) {
                        city.prev_owner = C.NEUTRAL;
                        city.owner = C.NEUTRAL;
                    }
                }
            }
    
        }

    }

    /**
     * @return the firebirds
     */
    public int getFirebirds() {
        return firebirds;
    }

    /**
     * @param firebirds the firebirds to set
     */
    public void setFirebirds(int firebirds) {
        this.firebirds = firebirds;
    }

    public void addFirebirds(int firebirds) {
        this.firebirds += firebirds;
    }

    /**
     * Calculate faction Gross Domestic Product, that is the total firebirds
     * produced by cities on planets where you own the palace.
     */
    public int calculateGDP() {
        int pop = 0;
        List<Structure> all_cities = game.getStructures();
        boolean[] rules = new boolean[game.getPlanets().size()];
        for (Structure s : all_cities) {
            if (s.type == C.PALACE && s.owner == game.getTurn()) {
                rules[s.p_idx] = true;
            }
        }
        for (Structure s : all_cities) {
            if (s.prev_owner == number && rules[s.p_idx]) {
                pop += s.health;
            }
        }
        return (int) FastMath.ceil(pop * CITY_GDP / 100);
    }
    

    /**
     * Calculate base unit pay.
     *
     * @return
     */
    public int calculateUnitPay() {
        int pay = 0;
        List<Unit> all_units = game.getUnits();
        for (Unit u : all_units) {
            if (u.prev_owner == number) {
                pay += u.type_data.crd_trn;
            }
        }
        return pay;
    }

    /**
     * Try to balance the budget, to be called when end turn button is pressed.
     * Calculate
     * <code>total = firebirds + taxes + tithe skim - unit pay - debt</code>, if
     * total is less than 0 return false, else set
     * <code>firebirds = total</code> and return true
     *
     * @return true if budget is balanced
     */
    public boolean balanceBudget() {
        int gdp = calculateGDP();
        int total = gdp * tax_rate / 100
                + gdp * tithe_rate / 1000
                - calculateUnitPay() * pay_rate / 100
                - debt
                + firebirds;
        if (total < 0) {
            return false;
        }
        firebirds = total;
        return true;
    }
    
    /**
     * @return the tax_rate
     */
    public int getTaxRate() {
        return tax_rate;
    }

    /**
     * @param tax_rate the tax_rate to set
     */
    public void setTaxRate(int tax_rate) {
        this.tax_rate = tax_rate;
    }

    /**
     * @return the tithe_rate
     */
    public int getTitheRate() {
        return tithe_rate;
    }

    /**
     * @param tithe_rate the tithe_rate to set
     */
    public void setTitheRate(int tithe_rate) {
        this.tithe_rate = tithe_rate;
    }

    /**
     * @return the pay_rate
     */
    public int getPayRate() {
        return pay_rate;
    }

    /**
     * @param pay_rate the pay_rate to set
     */
    public void setPayRate(int pay_rate) {
        this.pay_rate = pay_rate;
    }
}
