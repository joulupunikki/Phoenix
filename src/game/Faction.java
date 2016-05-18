/*
 * Copyright (C) 2015 joulupunikki joulupunikki@gmail.communist.invalid.
 * Copyright (C) 2014 Richard Wein
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
import java.awt.Point;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.math3.util.FastMath;
import util.C;
import util.Comp;
import util.Util;

/**
 * Contains faction data; money, tax, pay, technologies, messages; also does
 * some processing such as handling rebellions.
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
    private int old_tax_rate;
    private int old_tithe_rate;
    private int old_pay_rate;
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
        pay_rate = 75;
        adjustLoyalty();
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

    public List<Message> getMessages() {
        return messages;
    }

    public void deleteOldMessages() {
        messages.clear();
    }

    public boolean haveUnresolvedContracts() {
        for (Message message : messages) {
            Contract c = message.getContract();
            if (c != null) {
                if (!c.isResolved()) {
                    return true;
                }
            }
        }
        return false;
    }
    
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
    public void record(PrintWriter pw) {
        pw.println( " " + Util.getFactionName(number) + "," + number);
        pw.println( "  " + eliminated + "," + firebirds + "," + tax_rate + "," + tithe_rate + "," + pay_rate + "," + debt);
        research.record(pw);
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
     * total is not less than 0 and set_budget == true then set
     * <code>firebirds = total</code> and always return total.
     *
     * @param set_budget
     * @return total
     */
    public int balanceBudget(boolean set_budget) {
        int gdp = calculateGDP();
        int total = gdp * tax_rate / 100
                + gdp * tithe_rate / 1000
                - calculateUnitPay() * pay_rate / 100
                - debt
                + firebirds;
        if (total >= 0 && set_budget) {
            firebirds = total;
        }
        
        return total;
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
    
    /**
     * Return -1 iff at least 2 houses left; house id iff only 1 house left; -2
     * iff no houses left.
     *
     * @param factions
     * @return
     */
    public static int checkVictoryByElimination(Faction[] factions) {
        int ret = -2;
        int left = C.NR_HOUSES;
        for (int i = 0; i < C.NR_HOUSES; i++) {
            if (factions[i].isEliminated()) {
                left--;
            } else {
                ret = i;
            }
        }
        switch (left) {
            case 0:
            case 1:
                return ret;
            default:
                return -1;
        }
    }

    /**
     * @return the old_tax_rate
     */
    public int getOldTaxRate() {
        return old_tax_rate;
    }

    /**
     * @return the old_tithe_rate
     */
    public int getOldTitheRate() {
        return old_tithe_rate;
    }

    /**
     * @return the old_pay_rate
     */
    public int getOldPayRate() {
        return old_pay_rate;
    }

    public final void adjustLoyalty() {
        if (old_tax_rate != tax_rate) {           
            old_tax_rate = tax_rate;
        }
        adjustCityLoyalty();
        old_tithe_rate = tithe_rate;
        if (old_pay_rate != pay_rate) {            
            old_pay_rate = pay_rate;
        }
        adjustUnitLoyalty();
    }

    /**
     * Adjust city loyalty, create rebels, place rebels.
     */
    public void adjustCityLoyalty() {
        List<Structure> cities = game.getStructures();
        List<Structure> fully_rebel = new LinkedList<>();
        for (Structure city : cities) {
            if (city.owner == game.getTurn()) {
                game.adjustCityLoyalty(city, calculateCityLoyalty(tax_rate, efs_ini, game));
                if (city.loyalty < C.LOYALTY_REBEL_LIMIT) {
                    int rebel_pop = 0;
                    for (int i = city.health; i > 0; i -= 10) {
                        float prob = (1 - city.loyalty / C.LOYALTY_REBEL_LIMIT) * C.LOYALTY_REBEL_HIGH_P;
                        Util.dP(prob);
                        if (game.getRandom().nextFloat() < prob) {
                            rebel_pop += 10;
                        }
                    }
                    if (rebel_pop == 0) {
                        continue;
                    }
//                    if (rebel_pop > city.health) {
//                        rebel_pop = city.health;
//                    }
                    game.adjustCityHealth(city, city.health - rebel_pop);
                    LinkedList<Unit> rebels = new LinkedList<>();
                    Util.dP(rebel_pop);
                    for (int i = rebel_pop; i > 0; i -= 10) {
                        rebels.add(new Unit(city.p_idx, city.x, city.y, C.NEUTRAL, C.NEUTRAL, C.PARTISAN_UNIT_TYPE, 0, -1, -1, game));
                    }
                    placeRebels(rebels, Util.FindHexesAround.Hextype.LAND);
                    if (city.health <= 0) {
                        fully_rebel.add(city);
                    }
                }
            }
        }
        for (Structure city : fully_rebel) { // this here or else ConcurrentModificationException
            game.destroyCity(city.p_idx, city.x, city.y);
        }
    }

    public static int calculateCityLoyalty(int tax_rate, EfsIni efs_ini, Game game) {
        int excom_penalty = 0;
        //System.out.println(" state = " + game.getDiplomacy().getDiplomaticState(game.getTurn(), C.THE_CHURCH));
        if (game.getDiplomacy().getDiplomaticState(game.getTurn(), C.THE_CHURCH) == C.DS_WAR) {
            excom_penalty = game.getEfs_ini().excom_peasant_loyalty_hit;
            //System.out.println("WTF");
        }
        //System.out.println("excom_peasant_loyalty_hit = " + game.getEfs_ini().excom_peasant_loyalty_hit);
        //System.out.println("Excom penalty = " + excom_penalty);
        return FastMath.max(0, FastMath.min(100, 100 - (tax_rate - efs_ini.default_tax_rate) * C.TAX_LOYALTY_HIT - excom_penalty));
    }

    /**
     * Adjust unit loyalty, create rebels, place rebels. Note: AFAIK in all mods
     * Naval move units can only move on oceans, and non-Naval units can move on
     * land, so rebels are placed with this feature in mind.
     */
    public void adjustUnitLoyalty() {
        List<Unit> units = game.getUnits();
        LinkedList<Unit> rebels = new LinkedList<>();
        for (Unit unit : units) {
            if (unit.owner == game.getTurn() && canRebel(unit)) {
                unit.loyalty = pay_rate * C.PAY_LOYALTY_HIT;
                if (!unit.in_space && unit.loyalty < C.LOYALTY_REBEL_LIMIT) {
                    if (game.getRandom().nextFloat() < (1 - unit.loyalty / C.LOYALTY_REBEL_LIMIT) * C.LOYALTY_REBEL_HIGH_P) {
                        rebels.add(unit);
                    }
                }
            }
        }
        if (rebels.isEmpty()) {
            return;
        }
        rebels.sort(Comp.unit_xy);
        rebels.sort(Comp.unit_pidx);
        LinkedList<Unit> naval = new LinkedList<>();
        LinkedList<Unit> non_naval = new LinkedList<>();
        for (Unit rebel : rebels) {
            if (rebel.move_type == C.MoveType.NAVAL) {
                naval.add(rebel);
            } else {
                non_naval.add(rebel);
            }

        }
        placeRebels(naval, Util.FindHexesAround.Hextype.SEA);
        placeRebels(non_naval, Util.FindHexesAround.Hextype.LAND);

    }

    /**
     * Place rebels, fill hexes starting from closest available hex. Return true
     * if all rebels placed, false if we run out of planet hexes before all
     * rebels are placed.
     *
     * @param rebels list of units to place
     * @return true iff all rebels placed
     */
    private boolean placeRebels(LinkedList<Unit> rebels, Util.FindHexesAround.Hextype type) {
        Hex center = null;
        Hex target = null;
        Hex prev_target = null;
        Util.FindHexesAround hex_finder = null;
        Point p = new Point(C.NEUTRAL, C.NEUTRAL);
        int p_idx = -1;
        int x = -1;
        int y = -1;
        while (!rebels.isEmpty()) {
            Unit unit = rebels.pop();
            if (unit.y != y || unit.x != x || unit.p_idx != p_idx) {
                y = unit.y;
                x = unit.x;
                p_idx = unit.p_idx;
                addMessage(new Message("Rebellion on " + game.getPlanet(p_idx).name + " " + x + "," + y + "!", C.Msg.REBELLION, game.getYear(), game.getPlanet(p_idx)));
            }
            Hex hex_tmp = game.getHexFromPXY(unit.p_idx, unit.x, unit.y);
            if (!hex_tmp.equals(center)) {
                center = hex_tmp;
                hex_finder = new Util.FindHexesAround(center, C.NEUTRAL, type, game.getPlanet(p_idx).tile_set_type);
                prev_target = target;
                target = hex_finder.next();
            }
            while (target != null && Util.stackSize(target.getStack()) >= C.STACK_SIZE) {
                target = hex_finder.next();
            }
            prev_target = spot(prev_target, target);
            if (target == null) {
                return false;
            }
            if (unit.carrier == null) {            
                if (unit.type_data.cargo > 0 && unit.cargo_list.size() > 0) {
                    List<Unit> tmp = new LinkedList<>();
                    tmp.addAll(unit.cargo_list);
                    for (Unit u : tmp) {
                        unit.disembark(u);
                        center.addUnit(u);
                    }
                }
            } else {
                Unit u = unit.carrier;
                u.disembark(unit);
            }
            center.getStack().remove(unit);
            game.changeOwnerOfUnit(p, unit);
            game.relocateUnit(false, unit.p_idx, target.getX(), target.getY(), unit);
            target.addUnit(unit);
            game.getUnits().add(unit);
            //Util.dP(" hex " + target.getX() + "," + target.getY());
        }
        spot(target, prev_target);
        return true;
    }

    private boolean canRebel(Unit u) {
        return !(u.type == C.NOBLE_UNIT_TYPE || u.type == C.CARGO_UNIT_TYPE || u.type == C.SCEPTER_UNIT_TYPE);
    }

    private Hex spot(Hex prev_target, Hex target) {
        if (prev_target != null && !prev_target.equals(target)) {
            List<Unit> tmp = prev_target.getStack();
            //Util.dP("spot " + prev_target.getX() + "," + prev_target.getY() + " " + tmp.size() + " " + Util.getFactionName(tmp.get(0).owner));
            game.getHexProc().spotProc(prev_target, tmp);
            prev_target = target;    
        }
        return prev_target;
    }
    
}
