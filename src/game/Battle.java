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
import java.awt.Point;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
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
    private List<Unit> combat_stack_a;
    private List<Unit> combat_stack_b;
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
    }

    public void perBattleInit(LinkedList<Hex> path, int current_planet) {
        this.path = path;
        this.current_planet = current_planet;
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
        double ratio = 1.0 / def.type_data.armor;
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
            case C.PSYCH:
                ratio = atk.type_data.psy_str * 1.0 / def.type_data.psy_def;
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

        int dmg_index = -1;

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
     * stack.
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

        int count = attacker.size(); // > defender.size() ? attacker.size() : defender.size();
//        for (int i = 0; i < count; i++) {
//            damage[i] = 0;
//        }
        ListIterator<Unit> atk_it = attacker.listIterator();
        ListIterator<Unit> def_it = defender.listIterator();
        Unit atk = atk_it.next();
        Unit def = def_it.next();
        int def_indx = 0;
        for (int i = 0; i < count; i++) {

            int to_hit = getAtkAcc(atk_type, atk) - def.type_data.ag;
            if (random.nextInt(20) + to_hit > 9) {
//                damage[def_indx] += getDam(atk, def, atk_type);
                def.health_tmp -= getDam(atk, def, atk_type);
            }
//            if (!atk_it.hasNext()) {
//                atk_it = attacker.listIterator();
//            }
            if (!atk_it.hasNext()) {
                return;
            }
            atk = atk_it.next();
            if (!def_it.hasNext()) {
                def_it = defender.listIterator();
                def_indx = -1;
            }
            def = def_it.next();
            def_indx++;
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
        for (Unit unit : dead_list) {         // Put dead units on a temporary list and then delete them,
            game.deleteUnitInCombat(unit);    // so we don't remove units from the stack or cargo list we're iterating over
        }
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
                if (unit_b.owner != hex.getStack().get(0).owner) {
                    skip = true;
                    //System.out.println("owner");
                }
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
            if (unit.selected && unit.type_data.non_combat == 0
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
            if (unit.selected && unit.type_data.non_combat == 0) {
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
        spotAllUnits();
        doCombat(combat_stack_a, combat_stack_b);
        assignDamage(combat_stack_a, combat_stack_b);
        //record combat report combat postconditions, send message combatReportPost()
        combatReportPost(report);
        game.getFaction(combat_stack_b.get(0).owner).addMessage(new Message("",
                C.Msg.COMBAT_REPORT, game.getYear(), report));
    }

    public void resolveGroundBattleFinalize() {
        List<Unit> stack_a = game.getSelectedStack();
        List<Unit> stack_b = null;
        if (combat_type.equals(C.GROUND_COMBAT)) {
            stack_b = path.get(1).getStack();
            retreatRouted(stack_a, stack_b);
        } else if (combat_type.equals(C.SPACE_COMBAT)) {
            Point p = game.getSelectedPoint();
            Square[][] grid = game.getGalaxyMap().getGalaxyGrid();
            Planet planet = grid[p.x][p.y].parent_planet;
            stack_b = planet.space_stacks[attacked_faction];
            cancelRout(stack_a, stack_b);
        }
        removeDead(stack_a);
        removeDead(stack_b);

        if (combat_type.equals(C.GROUND_COMBAT)) {
            if (game.isCapture()) {
                game.capture();
                Structure city = path.get(1).getStructure();
                if (city != null) {
                    game.captureCity(city, stack_a.get(0).owner);
                }
            }
        }
//        removeDead(game.getUnits());
//        removeDead(game.getUnmovedUnits());
        combat_stack_a = null;
        combat_stack_b = null;
        combat_type = null;
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

        int[] defender_damage = new int[C.STACK_SIZE];
        List<Unit> defender_defence = new LinkedList<>();
        List<Unit> defender_offence = new LinkedList<>();

        int[] attacker_damage = new int[C.STACK_SIZE];
        List<Unit> attacker_offence = new LinkedList<>();
        List<Unit> attacker_defence = new LinkedList<>();

        int combat_loop_iter = -1;
        int[] combat_phases = null;

        switch (combat_type) {
            case C.GROUND_COMBAT:
                combat_loop_iter = C.COMBAT_LOOP_ITER;
                combat_phases = C.GROUND_COMBAT_PHASES;
                break;
            case C.SPACE_COMBAT:
                combat_loop_iter = 1;
                combat_phases = C.SPACE_COMBAT_PHASES;
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
                chooseDefence(defender, defender_defence, phase);
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

    }

    public String getCombatType() {
        return combat_type;
    }

}
