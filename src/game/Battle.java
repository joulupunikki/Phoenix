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

import galaxyreader.Planet;
import galaxyreader.Structure;
import galaxyreader.Unit;
import gui.CombatStrategyPanel;
import java.awt.Point;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import org.apache.commons.math3.util.FastMath;
import util.C;
import util.StackIterator;
import util.Util;

/**
 * Handles combat between two stacks.
 *
 * @author joulupunikki
 */
public class Battle implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // combat orders modifiers TODO make these moddable
    private static final double ASSAULT_STR = 1.2;
    private static final double ASSAULT_ARMOR = 0.8;
    private static final double FEINT_STR = 0.8;
    private static final double FEINT_ARMOR = 1.2;
    private static final double ASSAULT_MOD = ASSAULT_STR / ASSAULT_ARMOR;
    private static final double FEINT_MOD = FEINT_STR / FEINT_ARMOR;
    // xp modifiers TODO make these moddable
    private static final double ELITE_MOD = 1.2;
    private static final double GREEN_MOD = 0.8;
    private List<Unit> combat_stack_a;
    private List<Unit> combat_stack_b;
    private List<Unit> defender_defence = new LinkedList<>();
    private List<Unit> defender_offence = new LinkedList<>();
    private List<Unit> defender_normal = new LinkedList<>();
    private List<Unit> defender_persons = new LinkedList<>();
    private List<Unit> attacker_offence = new LinkedList<>();
    private List<Unit> attacker_defence = new LinkedList<>();
    private List<Unit> attacker_normal = new LinkedList<>();
    private List<Unit> attacker_assassins = new LinkedList<>();
    private LinkedList<Hex> path;
    private String combat_type;
    private int attacked_faction;
    private Random random;
    private int[][] damage;
    private int[][] target;
//    private GalaxyGrid galaxy_grid;
//    private UnitType[][] unit_types;
    private double[][][] terr_cost;
    private int current_planet;
    private List<Planet> planets;
    private Game game;
    // target hex of landing or bombardment
    private Hex ranged_space_target;
    // pts defence fire queue against landing or bombardment
    private List<Hex> pts_queue;
    private int city_damage;
    private CombatStrategyPanel.Strategy strategy;

    public Battle() {
        
    }

    public void battleInit(Random random, int[][] damage, int[][] target,
            double[][][] terr_cost, Game game, List<Planet> planets) {
        this.random = random;
        this.damage = damage;
        this.target = target;
//        this.galaxy_grid = galaxy_grid;
        this.terr_cost = terr_cost;
        this.game = game;
        this.planets = planets;
        this.pts_queue = new LinkedList<>();
        System.out.println(pts_queue.size());
    }

    public void perBattleInit(LinkedList<Hex> path, int current_planet) {
        this.path = path;
        this.current_planet = current_planet;
        this.city_damage = 0;
    }

    public List<Unit> getCombatStack(String stack) {
        List<Unit> rv = null;
        switch (stack) {
            case "a":
                rv = combat_stack_a;
                break;
            case "b":
                rv = combat_stack_b;
                break;
            default:
                throw new AssertionError();
        }
        return rv;
    }

    public void chooseOffence(List<Unit> attacker, List<Unit> attacker_offence, int i) {
        for (Unit unit : attacker) {
            switch (i) {
                case C.WATER:
                    if (unit.type_data.water_str > 0 && unit.health > 0 && !unit.routed) {
                        attacker_offence.add(unit);
                    }
                    break;
                case C.INDIRECT:
                    if (unit.type_data.indirect_str > 0 && unit.health > 0 && !unit.routed) {
                        attacker_offence.add(unit);
                    }
                    break;
                case C.AIR:
                    if (unit.type_data.air_str > 0 && unit.health > 0 && !unit.routed) {
                        attacker_offence.add(unit);
                    }
                    break;

                case C.DIRECT:
                    if (unit.type_data.direct_str > 0 && unit.health > 0 && !unit.routed) {
                        attacker_offence.add(unit);
                    }
                    break;
                case C.CLOSE:
                    if (unit.type_data.close_str > 0 && unit.health > 0 && !unit.routed) {
                        attacker_offence.add(unit);
                    }
                    break;
                case C.PSYCH:
                    if (unit.type_data.psy_str > 0 && unit.health > 0 && !unit.routed) {
                        attacker_offence.add(unit);
                    }
                    break;
                case C.RANGED_SPACE:
                    if (unit.type_data.ranged_sp_str > 0 && unit.health > 0 && !unit.routed) {
                        attacker_offence.add(unit);
                    }
                    break;

                case C.DIRECT_SPACE:
                    if (unit.type_data.direct_sp_str > 0 && unit.health > 0 && !unit.routed) {
                        attacker_offence.add(unit);
                    }
                    break;
                case C.CLOSE_SPACE:
                    if (unit.type_data.close_sp_str > 0 && unit.health > 0 && !unit.routed) {
                        attacker_offence.add(unit);
                    }
                    break;
                default:
                    throw new AssertionError();
            }
        }

    }

    public void chooseDefence(List<Unit> defender, List<Unit> defender_defence, int i) {
        for (Unit unit : defender) {
            if (target[unit.move_type.ordinal()][i] == 1 && unit.health > 0 && !unit.routed) {
                // what does this do ?
//                if (unit.type_data.move_type == C.MoveType.SPACE
//                        || unit.type_data.move_type == C.MoveType.JUMP
//                        || unit.type_data.move_type == C.MoveType.LANDER
//                        || i != C.RANGED_SPACE) {
                defender_defence.add(unit);
//                }
            }
        }
    }

    public int getAtkAcc(int atk_type, Unit atk) {
        int to_hit = 0;
        //System.out.println("attack_type " + atk_type);
        switch (atk_type) {
            case C.WATER:
                to_hit += atk.type_data.water_acc;
                break;
            case C.INDIRECT:
                to_hit += atk.type_data.indirect_acc;
                break;
            case C.AIR:
                to_hit += atk.type_data.air_acc;
                break;
            case C.DIRECT:
                to_hit += atk.type_data.direct_acc;
                break;
            case C.CLOSE:
                to_hit += atk.type_data.close_acc;
                break;
            case C.PSYCH:
                to_hit += atk.type_data.psy_acc;
                break;
            case C.RANGED_SPACE:
                to_hit += atk.type_data.ranged_sp_acc;
                break;
            case C.DIRECT_SPACE:
                to_hit += atk.type_data.direct_sp_acc;
                break;
            case C.CLOSE_SPACE:
                to_hit += atk.type_data.close_sp_acc;
                break;
            default:
                throw new AssertionError();
        }
        return to_hit;
    }

    public int getDam(Unit atk, Unit def, int atk_type) {
        int dmg = 0;
        // the strength/armor ratio, start with armor effect
        double ratio = 1.0 / def.type_data.armor;
        System.out.println("A/D ratio " + atk.type_data.abbrev + " -> " + def.type_data.abbrev + " after armor: " + ratio);
        // adjust for combat orders
        switch (strategy) {
            case ASSAULT:
                ratio *= ASSAULT_MOD;
                break;
            case FEINT:
                ratio *= FEINT_MOD;
                break;
            case NORMAL:
                break;
            default:
                throw new AssertionError();
        }
        System.out.println("A/D ratio after orders (" + strategy.toString() + "): " + ratio);
        // adjust for attacker xp
        if (game.getEfs_ini().experience_combat_effect) {
            switch (Unit.XP.values()[atk.experience]) {
                case ELITE:
                    ratio *= ELITE_MOD;
                    break;
                case GREEN:
                    ratio *= GREEN_MOD;
                    break;
                case EXPERT:
                    break;
                default:
                    throw new AssertionError();
            }
            System.out.println("A/D ratio after atk xp (" + atk.experience + "): " + ratio);
            // adjust for defender xp
            switch (Unit.XP.values()[def.experience]) {
                case ELITE:
                    ratio /= ELITE_MOD;
                    break;
                case GREEN:
                    ratio /= GREEN_MOD;
                    break;
                case EXPERT:
                    break;
                default:
                    throw new AssertionError();
            }
            System.out.println("A/D ratio after def xp (" + def.experience + "): " + ratio);
        }
        // adjust for attack strength
        switch (atk_type) {
            case C.WATER:
                ratio *= atk.type_data.water_str;
                break;
            case C.INDIRECT:
                ratio *= atk.type_data.indirect_str;
                break;
            case C.AIR:
                ratio *= atk.type_data.air_str;
                break;
            case C.DIRECT:
                ratio *= atk.type_data.direct_str;
                break;
            case C.CLOSE:
                ratio *= atk.type_data.close_str;
                break;
            case C.PSYCH: // swap psydef for armor
                ratio *= atk.type_data.psy_str * 1.0 / def.type_data.psy_def * def.type_data.armor;
                break;
            case C.RANGED_SPACE:
                ratio *= atk.type_data.ranged_sp_str;
                break;
            case C.DIRECT_SPACE:
                ratio *= atk.type_data.direct_sp_str;
                break;
            case C.CLOSE_SPACE:
                ratio *= atk.type_data.close_sp_str;
                break;
            default:
                throw new AssertionError();
        }
        System.out.println("A/D ratio FINAL: " + ratio);
        int dmg_index = -1;
        // find damage index ... were you drunk when you did this ?
        if (ratio < 3) {
            if (ratio < .25) {
                dmg_index = 0;
            } else if (ratio < .33) {
                dmg_index = 1;
            } else if (ratio < .5) {
                dmg_index = 2;
            } else if (ratio < 1) {
                dmg_index = 3;
            } else if (ratio < 2) {
                dmg_index = 4;
            } else {
                dmg_index = 5;
            }
        } else {
            if (ratio < 4) {
                dmg_index = 6;
            } else if (ratio < 5) {
                dmg_index = 7;
            } else if (ratio < 6) {
                dmg_index = 8;
            } else if (ratio < 7) {
                dmg_index = 9;
            } else if (ratio < 8) {
                dmg_index = 10;
            } else {
                dmg_index = 11;
            }
        }

        dmg = damage[random.nextInt(10)][dmg_index];

        return dmg;

    }

    /**
     * Handles one round of attacks by an attacker stack against a defender
     * stack. Handles assassinations and agility bonus for unspotted units.
     *
     * @param attacker the attacking stack
     * @param defender the defending stack
     * @param damage not used.
     * @param atk_type type of attack.
     */
    public void doAttack(List<Unit> attacker, List<Unit> defender, int[] damage, int atk_type) {

        if (attacker.isEmpty() || defender.isEmpty()) {
            return;
        }

        int count = attacker.size();
        attacker_assassins.clear();
        attacker_normal.clear();
        defender_normal.clear();
        defender_persons.clear();
        for (Unit unit : attacker) {
            if (unit.type == C.SPY_UNIT_TYPE) {
                attacker_assassins.add(unit);
            } else {
                attacker_normal.add(unit);
            }
        }
        for (Unit unit : defender) {
            if (unit.type_data.rank >= C.PERSONNEL_UNIT_MIN_RANK) {
                defender_persons.add(unit);
            } else {
                defender_normal.add(unit);
            }
        }
//        ListIterator<Unit> atk_it = attacker.listIterator();
//        ListIterator<Unit> def_it = defender.listIterator();
        ListIterator<Unit> atk_norm = attacker_normal.listIterator();
        ListIterator<Unit> def_norm = defender_normal.listIterator();
        ListIterator<Unit> atk_assa = attacker_assassins.listIterator();
        ListIterator<Unit> def_pers = defender_persons.listIterator();
        ListIterator<Unit> def_all = defender.listIterator();

        Unit atk = null;
        Unit def = null;
//        if (atk_assa.hasNext()) {
//            atk = atk_assa.next();
//        } else {
//            atk = atk_norm.next();
//        }
//        if (def_pers.hasNext()) {
//            def = def_pers.next();
//        } else {
//            def = def_norm.next();
//        }
        for (int i = 0; i < count; i++) {
            if (atk_assa.hasNext()) {
                atk = atk_assa.next();
                if (def_pers.hasNext()) {
                    def = def_pers.next();
                } else if (def_norm.hasNext()) {
                    def = def_norm.next();
                } else if (!def_all.hasNext()) {
                    def_all = defender.listIterator();
                    def = def_all.next();
                }
            } else if (atk_norm.hasNext()) {
                atk = atk_norm.next();
                if (def_norm.hasNext()) {
                    def = def_norm.next();
                } else if (def_pers.hasNext()) {
                    def = def_pers.next();
                } else if (!def_all.hasNext()) {
                    def_all = defender.listIterator();
                    def = def_all.next();
                }
            } else {
                return;
            }
            int to_hit = getAtkAcc(atk_type, atk) - def.type_data.ag;
            if (!def.spotted[atk.owner]) {
                to_hit -= C.UNSPOTTED_AG_BONUS;
            }
            if (random.nextInt(20) + to_hit > 9) { // nextInt(20) returns 0<= x < 20
                def.health_tmp -= getDam(atk, def, atk_type);
            } else if (combat_type.equals(C.BOMBARD_COMBAT) && atk.in_space && random.nextBoolean()) { // orbital bombardment city damage
                city_damage += FastMath.min(10, (atk.type_data.ranged_sp_str + 9) / 10);
            }
        }

    }

    public void dropDead(List<Unit> stack) {
        ListIterator<Unit> iter = stack.listIterator();
        Unit unit = null;
        while (iter.hasNext()) {
            unit = iter.next();
            if (unit.health_tmp <= 0) {
                unit.health = 0;
                iter.remove();
            }
        }
    }

    public void checkRout(List<Unit> stack) {
        for (Unit unit : stack) {
            if (unit.type_data.move_pts > 0 && unit.health_tmp < unit.health) {
                if (random.nextInt(100) + 1 > unit.loyalty) {
                    unit.routed = true;
                    if (combat_type.equals(C.GROUND_COMBAT)) {
                        unit.move_points = 0;
                    }
                }
            }
        }
    }

    public boolean checkRoutedKilled(List<Unit> stack) {
        boolean rv = true;
        for (Unit unit : stack) {
            if (unit.health > 0 && !unit.routed) {
                rv = false;
                break;
            }
        }
        return rv;
    }

    public void removeDead(List<Unit> stack) {
        List<Unit> dead_list = new LinkedList<>();
        for (Unit unit : stack) {
            // check for dead fighters aboard carriers
            for (Unit unit1 : unit.cargo_list) {
                if (unit1.health <= 0) {
                    dead_list.add(unit1);
                }
            }
            if (unit.health <= 0) {
                dead_list.add(unit);
            } else if (unit.health > unit.health_tmp) {
                unit.health = unit.health_tmp;
            }
        }
        for (Unit unit : dead_list) {             // Put dead units on a temporary list and then delete them,
            recordFinancialLoss(unit);
            game.deleteUnitInCombat(unit);    // so we don't remove units from the stack or cargo list we're iterating over
        }
    }

    private void recordFinancialLoss(Unit unit) {

        int perpetrator = combat_stack_a.get(0).owner;
        if (unit.owner == perpetrator) {
            perpetrator = combat_stack_b.get(0).owner;
        }
        Util.recordFinancialLoss(game, unit, perpetrator);
    }


    public boolean isSkip(Hex[] neighbours, int r_pos, Unit unit_b, int tile_set) {
        boolean skip = false;
        Hex hex = neighbours[r_pos];
        if (hex == null) {
            skip = true;
        } else {
            boolean[] terr_types = hex.getTerrain();
            double move_cost = 1;
            for (int k = 0; k < terr_types.length; k++) {
                if (terr_types[k] == true) {
                    move_cost *= terr_cost[k][tile_set][unit_b.type_data.move_type.ordinal()];

                }

            }
            //System.out.println("move_cost = " + move_cost);

            int stack_size = hex.getStack().size();
            if (move_cost == 0) {
                skip = true;
                //System.out.println("move_cost");
            } else if (stack_size >= 20) {
                skip = true;
                //System.out.println("stack_size");
            } else if (stack_size > 0) {
                if (unit_b.prev_owner != hex.getStack().get(0).prev_owner) {
                    skip = true;
                    //System.out.println("owner");
                }
            } else if (hex.getStructure() != null && unit_b.prev_owner != hex.getStructure().prev_owner) { // fix #63
                skip = true;
            }
        }

        return skip;
    }

    public Unit moveRouted(ListIterator<Unit> it, Hex[] neighbours,
            int r_pos, List<Unit> stack_b, Unit unit) {

        Hex hex = neighbours[r_pos];
        hex.getStack().add(unit);
        stack_b.remove(unit);
        // unspot and respot
        List<Unit> stack = new LinkedList<>();
        stack.add(unit);
        game.unSpot(stack);
        game.getHexProc().spotProc(hex, stack);
        game.setUnitCoords(false, current_planet, hex.getX(), hex.getY(), unit);
        if (it.hasNext()) {
            return it.next();
        } else {
            return null;
        }
    }

    /**
     * Retreats routed units. Tries to retreat units to hexes surrounding
     * defender hex. Starts at hex opposite the attacker hex and works toward
     * attacker hex skipping out of map hexes, full hexes and enemy occupied
     * hexes.
     *
     * @param stack_a attacking stack.
     * @param stack_b defending stack.
     */
    public void retreatRouted(List<Unit> stack_a, List<Unit> stack_b) {
        List<Unit> routed = new LinkedList<>();
        for (Unit unit : stack_b) {
            if (unit.routed && unit.health > 0) {
                routed.add(unit);
            }
        }
        if (routed.isEmpty()) {
            return;
        }
        Unit unit_a = stack_a.get(0);
        PlanetGrid pg = game.getPlanetGrid(unit_a.p_idx);
        Hex hex_a = pg.getHex(unit_a.x, unit_a.y);
        Unit unit_b = stack_b.get(0);
        Hex hex_b = pg.getHex(unit_b.x, unit_b.y);
        Hex[] neighbours = hex_b.getNeighbours();
        int tile_set = planets.get(unit_a.p_idx).tile_set_type;
        int a_pos = -1;
        for (int i = 0; i < neighbours.length; i++) {
            if (neighbours[i].equals(hex_a)) {
                a_pos = i;
                break;
            }

        }
        int r_pos = a_pos + 3;
        if (r_pos > 5) {
            r_pos -= 6;
        }
        int r_pos_stored = r_pos;
        boolean skip = false;
        int state = 0;
        ListIterator<Unit> it = routed.listIterator();
        Unit unit = it.next();
        while (unit != null) {
            switch (state) {
                case 0:
                    skip = isSkip(neighbours, r_pos, unit, tile_set);
                    if (skip) {
                        r_pos -= 1;
                        if (r_pos < 0) {
                            r_pos += 6;
                        }
                        state = 1;
                    } else {
                        unit = moveRouted(it, neighbours, r_pos, stack_b, unit);
                    }
                    break;
                case 1:
                    skip = isSkip(neighbours, r_pos, unit, tile_set);
                    if (skip) {
                        r_pos += 2;
                        if (r_pos > 5) {
                            r_pos -= 6;
                        }
                        state = 2;
                    } else {
                        unit = moveRouted(it, neighbours, r_pos, stack_b, unit);
                        state = 0;
                        r_pos = r_pos_stored;
                    }
                    break;
                case 2:
                    skip = isSkip(neighbours, r_pos, unit, tile_set);
                    if (skip) {
                        r_pos -= 3;
                        if (r_pos < 0) {
                            r_pos += 6;
                        }
                        state = 3;
                    } else {
                        unit = moveRouted(it, neighbours, r_pos, stack_b, unit);
                        state = 0;
                        r_pos = r_pos_stored;
                    }
                    break;
                case 3:
                    skip = isSkip(neighbours, r_pos, unit, tile_set);
                    if (skip) {
                        r_pos += 4;
                        if (r_pos > 5) {
                            r_pos -= 6;
                        }
                        state = 4;
                    } else {
                        unit = moveRouted(it, neighbours, r_pos, stack_b, unit);
                        state = 0;
                        r_pos = r_pos_stored;
                    }
                    break;
                case 4:
                    skip = isSkip(neighbours, r_pos, unit, tile_set);
                    if (skip) {
                        state = 5;
                    } else {
                        unit = moveRouted(it, neighbours, r_pos, stack_b, unit);
                        state = 0;
                        r_pos = r_pos_stored;
                    }
                    break;
                case 5:

                    unit.health = 0;
                    unit.health_tmp = 0;
                    if (it.hasNext()) {
                        unit = it.next();
                    } else {
                        unit = null;
                    }
                    state = 0;
                    r_pos = r_pos_stored;
                    break;
                default:
                    throw new AssertionError();
            }

        }

    }

    public void cancelRout(List<Unit> attacker, List<Unit> defender) {
        StackIterator iterator = new StackIterator(attacker);
        Unit e = iterator.next();
        while (e != null) {
            e.routed = false;
            //System.out.println("e.routed = " + e.routed);
            e = iterator.next();
        }

        iterator = new StackIterator(defender);
        e = iterator.next();
        while (e != null) {
            e.routed = false;
            e = iterator.next();
        }
    }

    public void assignDamage(List<Unit> attacker, List<Unit> defender) {
        for (Unit unit : defender) {
            unit.health = unit.health_tmp;
        }
        for (Unit unit : attacker) {
            unit.health = unit.health_tmp;
        }
    }

//    public void resolveBattleInit()
    public void selectGroundFighters() {

        List<Unit> stack_a = game.getSelectedStack();
        List<Unit> stack_b = path.get(1).getStack();

        List<Unit> attacker = new LinkedList<>();
        for (Unit unit : stack_a) {
            if (unit.isSelected() && unit.type_data.non_combat == 0
                    && unit.type_data.move_type != C.MoveType.JUMP
                    && unit.type_data.move_type != C.MoveType.SPACE) {
                attacker.add(unit);
                unit.health_tmp = unit.health;
            }
        }
        combat_stack_a = attacker;
        Util.sortRank(combat_stack_a);
        game.subMovePoints(attacker);

        List<Unit> defender = new LinkedList<>();
        for (Unit unit : stack_b) {
            if (unit.type_data.non_combat == 0
                    && unit.type_data.move_type != C.MoveType.JUMP
                    && unit.type_data.move_type != C.MoveType.SPACE) {
                defender.add(unit);
                unit.health_tmp = unit.health;
            }
        }
        combat_stack_b = defender;
        Util.sortRank(combat_stack_b);
    }

    public void selectSpaceFightersB(Unit unit, List<Unit> combat, boolean attacker) {

        combat.add(unit);
        unit.health_tmp = unit.health;

        if (unit.type == 16) {
            for (Unit unit1 : unit.cargo_list) {
                switch (unit1.type) {
                    case 17:
                    case 18:
                        combat.add(unit1);
                        unit1.health_tmp = unit1.health;
                        if (attacker) {
                            unit1.move_points++;
                        }
                        break;
                    default:
                }
            }
        }

    }

    /**
     * Select fighters for a space battle. Selected combat units are selected
     * for attackers, all units are selected for defenders. Additionally using
 * {@link #selectSpaceFightersB(galaxyreader.Unit, java.util.List, boolean) }
     * fighters and bombers aboard carriers are selected.
     */
    public void selectSpaceFighters() {

        List<Unit> stack_a = game.getSelectedStack();
        List<Unit> stack_b = null;
        Point p = game.getSelectedPoint();
        Square[][] grid = game.getGalaxyMap().getGalaxyGrid();
        Planet planet = grid[p.x][p.y].parent_planet;
        stack_b = planet.space_stacks[attacked_faction];

        List<Unit> attacker = new LinkedList<>();
        for (Unit unit : stack_a) {
            // if you change this change checkMoveLeftSpace also
            if (unit.isSelected() && unit.type_data.non_combat == 0) {
                selectSpaceFightersB(unit, attacker, true);
            }
        }
        combat_stack_a = attacker;
        Util.sortRank(combat_stack_a);
        game.subMovePointsSpace(attacker);

        List<Unit> defender = new LinkedList<>();
        for (Unit unit : stack_b) {
            selectSpaceFightersB(unit, defender, false);
        }

        combat_stack_b = defender;
        Util.sortRank(combat_stack_b);
    }

    /**
     * Select fighters for a bombard/PTS battle. If bombard include units
     * vulnerable to ranged space.
     *
     * @param bombard true iff bombarding, false iff landing
     */
    public void selectPTSFighters(boolean bombard) {

        List<Unit> stack_a = game.getSelectedStack();
        List<Unit> stack_b = ranged_space_target.getStack();

        List<Unit> attacker = new LinkedList<>();
        for (Unit unit : stack_a) {
            if (unit.isSelected()) {
                attacker.add(unit);
                unit.health_tmp = unit.health;
            }
        }
        combat_stack_a = attacker;
        Util.sortRank(combat_stack_a);

        List<Unit> defender = new LinkedList<>();
        for (Unit unit : stack_b) {

            if (unit.type_data.ranged_sp_str > 0 || bombard) {
                defender.add(unit);
                unit.health_tmp = unit.health;
            }
        }
        combat_stack_b = defender;
        Util.sortRank(combat_stack_b);
    }

    public void resolveGroundBattleInit(String combat_type, int defender_owner) {
        this.combat_type = combat_type;
        this.attacked_faction = defender_owner;

        switch (combat_type) {
            case C.GROUND_COMBAT:
                selectGroundFighters();
                break;
            case C.SPACE_COMBAT:
                selectSpaceFighters();
                break;
            case C.BOMBARD_COMBAT:
                selectPTSFighters(true);
                break;
            case C.PTS_COMBAT:
                selectPTSFighters(false);
                break;
            default:
                throw new AssertionError();
        }

    }

    public void setCombatStacks(List<Unit> stack_a, List<Unit> stack_b) {
        combat_stack_a = stack_a;
        combat_stack_b = stack_b;
    }

    /**
     * Record combat pre-conditions for combat report.
     *
     * @param report combat report object to write data to
     */
    public void combatReportPre(CombatReport report) {
        for (Unit unit : combat_stack_a) {
            Unit copy = new Unit(unit.p_idx, unit.x, unit.y, unit.owner, unit.prev_owner, unit.type, unit.t_lvl, 0, 0, game);
            copy.health = unit.health;
            copy.in_space = unit.in_space;
            report.attacker.add(copy);
        }
        for (Unit unit : combat_stack_b) {
            Unit copy = new Unit(unit.p_idx, unit.x, unit.y, unit.owner, unit.prev_owner, unit.type, unit.t_lvl, 0, 0, game);
            copy.health = unit.health;
            copy.in_space = unit.in_space;
            report.defender.add(copy);
        }
    }

    /**
     * Record combat post-conditions for combat report.
     *
     * @param report combat report object to write data to
     */
    public void combatReportPost(CombatReport report) {
        int idx = 0;
        for (Unit unit : combat_stack_a) {
            report.atk_post_health[idx] = unit.health;
            report.atk_rout[idx] = unit.routed;
            idx++;
        }
        idx = 0;
        for (Unit unit : combat_stack_b) {
            report.def_post_health[idx] = unit.health;
            report.def_rout[idx] = unit.routed;
            idx++;
        }

    }

    public void spotAllUnits() {
        int fact_a = combat_stack_a.get(0).owner;
        int fact_b = combat_stack_b.get(0).owner;
        for (Unit unit : combat_stack_a) {
            unit.spotted[fact_b] = true;
        }
        for (Unit unit : combat_stack_b) {
            unit.spotted[fact_a] = true;
        }
    }

    public void resolveGroundBattleFight() {
        CombatReport report = new CombatReport(combat_stack_a.size(), combat_stack_b.size());
        //record combat report combat preconditions combatReportPre()
        combatReportPre(report);
        doCombat(combat_stack_a, combat_stack_b);
        spotAllUnits();
        assignDamage(combat_stack_a, combat_stack_b);
        //record combat report combat postconditions, send message combatReportPost()
        combatReportPost(report);
        game.getFaction(combat_stack_b.get(0).owner).addMessage(new Message("",
                C.Msg.COMBAT_REPORT, game.getYear(), report));
    }

    public void resolveGroundBattleFinalize() {
        List<Unit> stack_a = game.getSelectedStack();
        List<Unit> stack_b = null;
        switch (combat_type) {
            case C.GROUND_COMBAT:
                stack_b = path.get(1).getStack();
                retreatRouted(stack_a, stack_b);
                break;
            case C.SPACE_COMBAT:
                Point p = game.getSelectedPoint();
                Square[][] grid = game.getGalaxyMap().getGalaxyGrid();
                Planet planet = grid[p.x][p.y].parent_planet;
                stack_b = planet.space_stacks[attacked_faction];
                cancelRout(stack_a, stack_b);
                break;
            case C.BOMBARD_COMBAT:
                Structure s = ranged_space_target.getStructure();
                if (s != null && s.health < 1) {
                    game.destroyCity(s.p_idx, s.x, s.y);
                }
            // no break !!!
            case C.PTS_COMBAT:
                stack_b = ranged_space_target.getStack();
                cancelRout(stack_a, stack_b);
                break;
            default:
                throw new AssertionError();
        }
        removeDead(stack_a);
        removeDead(stack_b);

        if (combat_type.equals(C.GROUND_COMBAT)) {
            if (game.isCapture()) {
                Point faction = new Point(stack_a.get(0).owner, stack_a.get(0).prev_owner);
                game.capture(faction);
                Structure city = path.get(1).getStructure();
                if (city != null) {
                    game.captureCity(city, stack_a.get(0).owner, stack_a.get(0).prev_owner);
                }
            }
        }
//        removeDead(game.getUnits());
//        removeDead(game.getUnmovedUnits());
        if (game.getEfs_ini().experience_combat_effect) {
            for (Unit unit : combat_stack_a) {
                if (random.nextInt(64) < 16) {
                    unit.promote();
                }
            }
            for (Unit unit : combat_stack_b) {
                if (random.nextInt(64) < 16) {
                    unit.promote();
                }
            }
        }
        combat_stack_a = null;
        combat_stack_b = null;
        combat_type = null;
        ranged_space_target = null;
        attacked_faction = -2;
    }

    /**
     * Handles a battle between an attacking and a defending stack. During each
     * iteration through combat phases, for each phase, 1. selects offensive and
     * defensive units for both sides, 2. shuffles offensive units, 3. conducts
     * attacks in the phase while dropping dead units, 4. checks for rout.
     *
     * @param attacker attacking stack.
     * @param defender defending stack.
     */
    public void doCombat(List<Unit> attacker, List<Unit> defender) {

        city_damage = 0;
        int[] defender_damage = new int[C.STACK_SIZE];

        int[] attacker_damage = new int[C.STACK_SIZE];

        int combat_loop_iter = -1;
        int[] combat_phases = null;
        boolean defender_shielded = false;

        switch (combat_type) {
            case C.GROUND_COMBAT:
                combat_loop_iter = C.COMBAT_LOOP_ITER;
                combat_phases = C.GROUND_COMBAT_PHASES;
                break;
            case C.SPACE_COMBAT:
                combat_loop_iter = 1;
                combat_phases = C.SPACE_COMBAT_PHASES;
                break;
            case C.BOMBARD_COMBAT:
            case C.PTS_COMBAT:
                Unit u = defender.get(0);
                Hex target_hex = game.getHexFromPXY(u.p_idx, u.x, u.y);
                if (game.isShielded(target_hex, u.p_idx)) {
                    defender_shielded = true;
                }
                combat_loop_iter = 1;
                combat_phases = C.PTS_COMBAT_PHASES;
                break;
            default:
                throw new AssertionError();
        }
        fight:
        for (int k = 0; k < combat_loop_iter; k++) {
            for (int i = 0; i < combat_phases.length; i++) {
                int phase = combat_phases[i];
                attacker_offence.clear();
                attacker_defence.clear();
                chooseOffence(attacker, attacker_offence, phase);
                chooseDefence(attacker, attacker_defence, phase);
//                System.out.println("attacker_offence = " + attacker_offence);
//                System.out.println("attacker_defence = " + attacker_defence);
                defender_defence.clear();
                defender_offence.clear();
                if (!defender_shielded) {
                    chooseDefence(defender, defender_defence, phase);
                }
                chooseOffence(defender, defender_offence, phase);
//                System.out.println("defender_defence = " + defender_defence);
//                System.out.println("defender_offence = " + defender_offence);
                Collections.shuffle(attacker_offence, random);
                Collections.shuffle(defender_offence, random);
                for (int j = 0; j < C.NR_ATTACKS[phase]; j++) {
                    doAttack(attacker_offence, defender_defence, defender_damage, phase);
                    doAttack(defender_offence, attacker_defence, attacker_damage, phase);
                    dropDead(attacker_offence);
                    dropDead(defender_defence);
                    dropDead(attacker_defence);
                    dropDead(defender_offence);
                }
                checkRout(attacker);
                checkRout(defender);
                if (checkRoutedKilled(attacker) || checkRoutedKilled(defender)) {
                    break fight;
                }

            }
        }
        if (combat_type.equals(C.BOMBARD_COMBAT)) {
            Structure s = ranged_space_target.getStructure();
            if (s != null) {
                s.health -= city_damage;
            }
        }
    }

    public String getCombatType() {
        return combat_type;
    }

    public Hex getRangedSpaceTarget() {
        return ranged_space_target;
    }

    public List<Hex> getPTSQueue() {
        return pts_queue;
    }

    /**
     * Called when bombarding or landing. Records target hex, if queue_pts then
     * checks for hostile PTS around target hex and queues them for return fire.
     *
     * @param h
     * @param queue_pts
     */
    public void startBombardOrPTS(Hex h, boolean queue_pts, int target_faction) {
        ranged_space_target = h;
        if (!queue_pts) {
            return;
        }
        pts_queue.clear(); // fix #64
        Set<Hex> pts_area = Util.getHexesWithinRadiusOf(h, game.getEfs_ini().pts_fire_range, null);
        pts_area.remove(h);
        for (Hex next : pts_area) {
            List<Unit> stack = next.getStack();
            if (!stack.isEmpty()) {
                int fac_a = stack.get(0).owner;
                int fac_b = game.getTurn();
                if ((target_faction == -1 || target_faction == fac_a) && fac_a != fac_b && game.getDiplomacy().getDiplomaticState(fac_a, fac_b) == C.DS_WAR) {
                    for (Unit unit : stack) {
                        if (unit.type_data.ranged_sp_str > 0) {
                            pts_queue.add(next);
                            System.out.println("PTS QUEUE add (" + next.getX() + "," + next.getY() + ")");
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * @return the strategy
     */
    public CombatStrategyPanel.Strategy getStrategy() {
        return strategy;
    }

    /**
     * @param strategy the strategy to set
     */
    public void setStrategy(CombatStrategyPanel.Strategy strategy) {
        this.strategy = strategy;
    }

}
