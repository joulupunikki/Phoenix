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
package ai;

import galaxyreader.Planet;
import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Game;
import game.Hex;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.C;
import util.Util;

/**
 * Represents a task force of units with a specific mission. Here we are
 * interested in moving ground forces from one continent to another (which may
 * not be on the same planet.)
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class TaskForce implements Serializable {

    private static long tf_id_count = 0;
    private static Game game;
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(TaskForce.class);
    private static int exception_counter = 0;

    static void init(Game game_ref) {
        tf_id_count = 0;
        game = game_ref;
    }

    /**
     * @return the exception_counter
     */
    static int getExceptionCounter() {
        return exception_counter;
    }

    /**
     *
     */
    static void zeroExceptionCounter() {
        exception_counter = 0;
    }

    int faction;
    long tf_id;
    LinkedList<List<Unit>> ground_stacks;
    List<Unit> ground_forces;
    List<Unit> transports;
    List<Unit> escorts;
    Hex target_hex;
    int target_p_idx;
    Unit t;
    Unit c;
    private STATE state;
    private final int TF_SIZE = 1;
    private final int MAX_AI_PLAN_STEPS = 256;

    private enum STATE {

        MOVING_TO_PICKUP,
        AT_PICKUP,
        MOVING_TO_DESTINATION,
        UNLOAD;
    }

    private enum SUB_STATE {

        FINISHED,
        MOVES_LEFT,
        NO_MOVES_LEFT;
    }
    public TaskForce(Game game, int target_p_idx, Hex target) {
        tf_id = tf_id_count++;
        ground_stacks = new LinkedList<>();
        ground_forces = new LinkedList<>();
        transports = new ArrayList<>(C.STACK_SIZE);
        escorts = new ArrayList<>(C.STACK_SIZE);
        this.game = game;
        this.target_p_idx = target_p_idx;
        this.target_hex = target;
        logger.debug("TaskForce " + tf_id + " created: target " + game.getPlanet(target_p_idx).name + " " + target_hex.getX() + "," + target_hex.getY());
    }

    public void addTransport(Unit u) {
        u.task_force = 1;
        transports.add(u);
    }

    public void addEscort(Unit u) {
        u.task_force = 1;
        escorts.add(u);
    }

    public void add(List<Unit> stack) {
        for (Unit u : stack) {
            u.task_force = 1;
        }
        Unit u = stack.get(0);
        ground_stacks.add(game.getHexFromPXY(u.p_idx, u.x, u.y).getStack());
        ground_forces.addAll(stack);
    }

    private void printFirstCargoInWaiting() {
        if (ground_stacks.isEmpty() || ground_stacks.get(0).isEmpty()) {
            logger.debug("  no cargo left");
            return;
        }
        Unit u = ground_stacks.get(0).get(0);
        logger.debug("  ground_stacks.get(0).get(0) " + u + "  at " + game.getPlanet(u.p_idx).name + " " + u.x + "," + u.y);
    }

    public void initPlan() {
        t = transports.get(0);
        faction = t.owner;
        printFirstCargoInWaiting();
        c = ground_stacks.get(0).get(0);
        if (!t.in_space && t.p_idx == c.p_idx && t.x == c.x && t.y == c.y) {
            state = STATE.AT_PICKUP;
        } else {
            state = STATE.MOVING_TO_PICKUP;
        }
    }

    /**
     * Execute the plan of this task force. Moves until move points exhausted, a
     * low level exception is thrown or this task force has finished. This
     * method must handle AIExceptions.
     * @return true iff passed without non-fatal Exceptions
     */
    public boolean executePlan() {
        boolean pass = true;
        String loc = "space";
        if (!transports.get(0).in_space) {
            loc = transports.get(0).x + "," + transports.get(0).y;
        }
        logger.debug("----> " + tf_id + " execute plan: start at " + game.getPlanet(t.p_idx).name + " " + loc);
        printFirstCargoInWaiting();
        if (Util.movesLeft(transports)) {
            Util.selectAll(transports);
            int steps = 1;
            try {
                while (executePlan1()) {
                    steps++;
                    if (steps > MAX_AI_PLAN_STEPS) {
                        throw new AIFatalException(" MAX_AI_PLAN_STEPS (" + MAX_AI_PLAN_STEPS + ") exceeded");
                    }
                }
            } catch (AIException ex) { // at this stage we are content with logging
                exception_counter++;
                if (ex instanceof AIFatalException) { // fatals will not be re-queued
                    logger.debug("  XXX AIFatalException: " + ex.getMessage());
                } else {
                    pass = false;
                    logger.debug("  XXX AIException: " + ex.getMessage());
                }
            }
            Util.unSelectAll(transports);
            printFirstCargoInWaiting();
            if (!t.in_space) {
                loc = t.x + "," + t.y;
            }
            logger.debug("<---- execute plan: steps " + steps + " end at " + game.getPlanet(t.p_idx).name + " " + loc);
        }
        return pass;
    }

    private SUB_STATE moveToPickup() throws AIException {
//        Hex hex = game.getHexFromPXY(t.p_idx, t.x, t.y);
        if (!t.in_space) {
            game.setCurrentPlanetNr(t.p_idx);
            game.setSelectedPoint(new Point(t.x, t.y), -1);
            game.setSelectedFaction(-1);
            setSelected();
            if (!game.launchStack()) { // FIXME this may fail due to a traffic jam
                throw new AIException("launch failed at " + game.getPlanet(t.p_idx).name + " stack " + game.getSelectedStack());
            }
//            hex.getStack().removeAll(transports);
//            game.getPlanet(t.p_idx).space_stacks[t.owner].addAll(transports);
//            game.subMovePointsSpace(transports);
            if(Util.movesLeft(transports)) {
                return SUB_STATE.MOVES_LEFT;
            } else {
                return SUB_STATE.NO_MOVES_LEFT;
            }
        } else {
            game.setCurrentPlanetNr(t.p_idx);
            game.setSelectedPoint(new Point(t.x, t.y), faction);
            game.setSelectedFaction(faction);
            setSelected();
            if (t.p_idx == c.p_idx) {
                // FIXME pts fire
                if (!game.landStack(new Point(c.x, c.y))) { // FIXME this may fail due to a traffic jam

                    Hex cargo_hex = game.getHexFromPXY(c.p_idx, c.x, c.y);
                    Hex side_step = null;
                    Util.FindHexesAround finder = new Util.FindHexesAround(cargo_hex, faction, Util.FindHexesAround.Hextype.LAND, game.getPlanet(c.p_idx).tile_set_type, C.STACK_SIZE - TF_SIZE, 1);
                    side_step = finder.next();
                    if (side_step != null) {
                        Unit move = null;
                        int rank = Integer.MAX_VALUE;
                        for (Unit u : cargo_hex.getStack()) {
                            u.selected = false;
                            if (u.type_data.rank < rank && (u.move_type == C.MoveType.FOOT
                                    || u.move_type == C.MoveType.HOVER
                                    || u.move_type == C.MoveType.TREAD
                                    || u.move_type == C.MoveType.WHEEL)) {
                                if (move != null) {
                                    move.selected = false;
                                }
                                move = u;
                                move.selected = true;
                                rank = u.type_data.rank;
                            }
                        }
                        if (move != null) {
                            game.setSelectedPoint(new Point(side_step.getX(), side_step.getY()), -1);
                            game.setSelectedFaction(-1);
                            LinkedList<Hex> path = new LinkedList<>();
                            path.add(cargo_hex);
                            path.add(side_step);
                            game.setPath(path);
                            game.moveStack();
                            game.setSelectedPoint(new Point(t.x, t.y), faction);
                            game.setSelectedFaction(faction);
                            logger.debug("YYY moved to side");
                        }
                    }
                    if (!game.landStack(new Point(c.x, c.y))) {
                        throw new AIException("landing failed at " + game.getPlanet(t.p_idx).name + " stack " + game.getSelectedStack());
                    }

                }
//                game.getPlanet(t.p_idx).space_stacks[t.owner].removeAll(transports);
//                hex.getStack().addAll(transports);
//                game.subMovePointsSpace(transports);
                return SUB_STATE.FINISHED;
            } else {
                Planet planet = game.getPlanet(c.p_idx);
                if (!game.moveSpaceStack(new Point(planet.x, planet.y))) { // FIXME this may fail due to a traffic jam
                    throw new AIException("move from " + game.getPlanet(t.p_idx).name + " to " + planet.name + " failed");
                }
                // need to set these or will try to draw empty stack
                game.setCurrentPlanetNr(t.p_idx);
                game.setSelectedPoint(new Point(t.x, t.y), faction);
                game.setSelectedFaction(faction);
//                game.getPlanet(t.p_idx).space_stacks[t.owner].removeAll(transports);
//                game.getPlanet(c.p_idx).space_stacks[t.owner].addAll(transports);
//                for (Unit unit : transports) {
//                    unit.move_points = 0;
//                }
                return SUB_STATE.NO_MOVES_LEFT;
            }
        }
    }

    private void setSelected() {
        for (Unit u : game.getSelectedStack()) {
            u.selected = false;
        }
        for (Unit u : transports) {
            u.selected = true;
        }
    }

    private SUB_STATE moveToDestination() throws AIException {
//        Hex hex = game.getHexFromPXY(t.p_idx, t.x, t.y);
        if (!t.in_space) {
            game.setCurrentPlanetNr(t.p_idx);
            game.setSelectedPoint(new Point(t.x, t.y), -1);
            game.setSelectedFaction(-1);
            setSelected();
            if (!game.launchStack()) { // FIXME this may fail due to a traffic jam
                throw new AIException("launch failed at " + game.getPlanet(t.p_idx).name + " stack " + game.getSelectedStack());
            }
//            hex.getStack().removeAll(transports);
//            game.getPlanet(t.p_idx).space_stacks[t.owner].addAll(transports);
//            game.subMovePointsSpace(transports);
            if (Util.movesLeft(transports)) {
                return SUB_STATE.MOVES_LEFT;
            } else {
                return SUB_STATE.NO_MOVES_LEFT;
            }
        } else {
            game.setCurrentPlanetNr(t.p_idx);
            game.setSelectedPoint(new Point(t.x, t.y), faction);
            game.setSelectedFaction(faction);
            setSelected();
            if (t.p_idx == target_p_idx) {
                // FIXME if can't find land, abort ?
                Util.FindHexesAround finder = new Util.FindHexesAround(target_hex, faction, Util.FindHexesAround.Hextype.LAND, game.getPlanet(target_p_idx).tile_set_type);
                Hex land = finder.next();
                if (land == null) {

                }
                // FIXME pts fire
                if (!game.landStack(new Point(land.getX(), land.getY()))) { // FIXME this may fail due to a traffic jam
                    throw new AIException("landing failed at " + game.getPlanet(t.p_idx).name + " stack " + game.getSelectedStack());
                }
//                game.getPlanet(t.p_idx).space_stacks[t.owner].removeAll(transports);
//                land.getStack().addAll(transports);
//                game.subMovePointsSpace(transports);
                return SUB_STATE.FINISHED;
            } else {
                Planet planet = game.getPlanet(target_p_idx);
                if (!game.moveSpaceStack(new Point(planet.x, planet.y))) { // FIXME this may fail due to a traffic jam
                    throw new AIException("move from " + game.getPlanet(t.p_idx).name + " to " + planet.name + " failed");
                }
                // need to set these or will try to draw empty stack
                game.setCurrentPlanetNr(t.p_idx);
                game.setSelectedPoint(new Point(t.x, t.y), faction);
                game.setSelectedFaction(faction);
//                game.getPlanet(t.p_idx).space_stacks[t.owner].removeAll(transports);
//                game.getPlanet(target_p_idx).space_stacks[t.owner].addAll(transports);
//                for (Unit unit : transports) {
//                    unit.move_points = 0;
//                }
                return SUB_STATE.NO_MOVES_LEFT;
            }
        }
    }

    private boolean pickUp() throws AIException {
        if (t.in_space) {
            throw new AIFatalException("pickup attempt in space");
        }
        Hex hex = game.getHexFromPXY(t.p_idx, t.x, t.y);
        int cap = 0;
        for (Unit transport : transports) {
            cap += transport.type_data.cargo - transport.cargo_list.size();
            logger.debug("  cap: " + cap);
        }
        List<Unit> tmp = new LinkedList<>();
        for (Unit u : hex.getStack()) {
            if (cap == 0) {
                break;
            }
            if (u.type_data.non_combat == 0 && (u.move_type == C.MoveType.FOOT
                    || u.move_type == C.MoveType.HOVER
                    || u.move_type == C.MoveType.TREAD
                    || u.move_type == C.MoveType.WHEEL)) {
                tmp.add(u);
                cap--;
            }
        }
        logger.debug("  ground_stacks.get(0): " + ground_stacks.get(0));
        hex.minusStack(tmp);
        logger.debug("  ground_stacks.get(0): " + ground_stacks.get(0));
        String s_cargo = " ";
        for (Unit t : transports) {
            for (Iterator<Unit> iterator = tmp.iterator(); iterator.hasNext();) {
                if (t.cargo_list.size() < t.type_data.cargo) {
                    Unit u = iterator.next();
                    s_cargo += u.type_data.abbrev + " ";
                    t.embark(u);
                    iterator.remove();
                } else {
                    break;
                }
            }
        }
        logger.debug("  picked up: " + s_cargo + " at " + game.getPlanet(t.p_idx).name + " " + t.x + "," + t.y);
        return Util.movesLeft(transports);
    }

    private SUB_STATE unload() throws AIException {
        if (t.in_space) {
            throw new AIFatalException("unload attempt in space");
        }
        List<Unit> stack = game.getHexFromPXY(t.p_idx, t.x, t.y).getStack();
        Unit[] cargo_array = new Unit[4];
        String s_cargo = " ";
        for (Unit t : transports) {
            cargo_array = t.cargo_list.toArray(cargo_array);
            for (Unit c : cargo_array) {
                if (c == null) {
                    break;
                }
                s_cargo += c.type_data.abbrev + " ";
                t.disembark(c);
                stack.add(c);
                c.task_force = 0;
            }
        }
        logger.debug("  dropped : " + s_cargo + " at " + game.getPlanet(t.p_idx).name + " " + t.x + "," + t.y);
        if (AI.haveGroundTroop(ground_stacks.get(0))) {
            c = ground_stacks.get(0).get(0);
            logger.debug("  more cargo at " + game.getPlanet(c.p_idx).name + " " + c.x + "," + c.y);
            if (Util.movesLeft(transports)) {
                return SUB_STATE.MOVES_LEFT;
            } else {
                return SUB_STATE.NO_MOVES_LEFT;
            }
        }
        return SUB_STATE.FINISHED;
    }


    private void finished() {
        logger.debug("TaskForce " + tf_id + " finished: target " + game.getPlanet(target_p_idx).name + " " + target_hex.getX() + "," + target_hex.getY());
        for (Unit u : ground_forces) {
            u.task_force = 0;
        }
        for (Unit u : transports) {
            u.task_force = 0;
        }
        for (Unit u : escorts) {
            u.task_force = 0;
        }
        ground_stacks.clear();
        ground_forces.clear();
        transports.clear();
        escorts.clear();
        Structure struct = target_hex.getStructure();
        if (struct != null) {
            struct.task_force &= ~(0x1 << faction);
        }
        target_hex = null;
        target_p_idx = -1;

    }

    private boolean executePlan1() throws AIException {
        logger.debug("  state " + state);
        switch (state) {
            case MOVING_TO_PICKUP:
                switch (moveToPickup()) {
                    case MOVES_LEFT:
                        return true;
                    case NO_MOVES_LEFT:
                        return false;
                    case FINISHED:
                        state = STATE.AT_PICKUP;
                        return true;
                    default:
                        throw new AssertionError();
                        
                }
            case AT_PICKUP:
                boolean result = pickUp();
                state = STATE.MOVING_TO_DESTINATION;
                return result;
            case MOVING_TO_DESTINATION:
                switch (moveToDestination()) {
                    case MOVES_LEFT:
                        return true;
                    case NO_MOVES_LEFT:
                        return false;
                    case FINISHED:
                        state = STATE.UNLOAD;
                        return true;
                    default:
                        throw new AssertionError();
                }
            case UNLOAD:
                switch (unload()) {
                    case MOVES_LEFT:
                        state = STATE.MOVING_TO_PICKUP;
                        return true;
                    case NO_MOVES_LEFT:
                        state = STATE.MOVING_TO_PICKUP;
                        return false;
                    case FINISHED:
                        finished();
                        return false;
                    default:
                        throw new AssertionError();
                }
            default:
                throw new AssertionError();
        }

    }

}
