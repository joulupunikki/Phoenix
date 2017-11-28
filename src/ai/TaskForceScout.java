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
import galaxyreader.Unit;
import game.Game;
import java.awt.Point;
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a task force of units with a specific mission. Here we are
 * interested in scouting unmapped planets on the edge of the area of the mapped
 * planets.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class TaskForceScout extends TaskForceSuper implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(TaskForceScout.class);
    private static int exception_counter = 0;

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

    boolean finished;
    int faction;
    int tf_id;

    Unit scout;
    int target_p_idx;
    private STATE state;
    private final int TF_SIZE = 1;
    private final int MAX_AI_PLAN_STEPS = 2;

    @Override
    protected boolean removeUnit(Unit u) throws AIFatalException {
        if (!scout.equals(u)) {
            throw new AIFatalException("Indicated unit not found in task force");
        }
        finished();
        return true;
    }

    private enum STATE {
        ON_PLANET,
        IN_SPACE;
    }

    public TaskForceScout(Game game, int target_p_idx, int tf_id) {
        this.game = game;
        this.gal_grid = game.getGalaxyMap();
        this.tf_id = tf_id;
        this.target_p_idx = target_p_idx;
        logger.debug("TaskForceScout " + tf_id + " created: target " + game.getPlanet(target_p_idx).name);
    }

    public void add(Unit scout) {
        this.scout = scout;
        scout.task_force = tf_id;
    }

    public void initPlan() {
        faction = scout.owner;
        if (scout.in_space) {
            state = STATE.IN_SPACE;
        } else {
            state = STATE.ON_PLANET;
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
        if (!scout.in_space) {
            loc = scout.x + "," + scout.y;
        }
        logger.debug("----> " + tf_id + " execute plan: start at " + game.getPlanet(scout.p_idx).name + " " + loc);
        if (scout.move_points > 0) {
            int steps = 1;
            try {
                while (executePlan1()) {
                    if (steps++ > MAX_AI_PLAN_STEPS) {
                        throw new AIFatalException(" MAX_AI_PLAN_STEPS (" + MAX_AI_PLAN_STEPS + ") exceeded");
                    }
                }
            } catch (AIException ex) { // at this stage we are content with logging
                exception_counter++;
                if (ex instanceof AIFatalException) {
                    // fatals will not be re-queued
                    logger.debug("  XXX AIFatalException: " + ex.getMessage());
                } else {
                    pass = false;
                    logger.debug("  XXX AIException: " + ex.getMessage());
                }
            }
            loc = "space";
            if (!scout.in_space) {
                loc = scout.x + "," + scout.y;
            }
            logger.debug("<---- execute plan: steps " + steps + " end at " + game.getPlanet(scout.p_idx).name + " " + loc);
            if (finished) {
                finished();
            }
        }
        return pass;
    }

    private void setSelected() {
        for (Unit u : game.getSelectedStack()) {
            u.setSelected(false);
        }
        scout.setSelected(true);
    }

    void finished() {
        logger.debug("TaskForceScout " + tf_id + " finished: target " + game.getPlanet(target_p_idx).name);
        scout.task_force = 0;
        scout = null;
        target_p_idx = -1;
    }

    private boolean executePlan1() throws AIException {
        logger.debug("  state " + state);
        switch (state) {
            case ON_PLANET:
                game.setCurrentPlanetNr(scout.p_idx);
                game.setSelectedPoint(new Point(scout.x, scout.y), -1);
                game.setSelectedFaction(-1);
                setSelected();
                if (!game.launchStack()) { // FIXME this may fail due to a traffic jam
                    throw new AIException("launch failed at " + game.getPlanet(scout.p_idx).name + " stack " + game.getSelectedStack());
                }
                state = STATE.IN_SPACE;
                return scout.move_points > 0;
            case IN_SPACE:
                game.setCurrentPlanetNr(scout.p_idx);
                game.setSelectedPoint(new Point(scout.x, scout.y), faction);
                game.setSelectedFaction(faction);
                setSelected();
                //System.out.println("XXXXXX STACK:" + game.getSelectedStack());
                // FIXED find next planet on path to target
                Planet planet = game.getPlanet(gal_grid.nextInRoutingTable(scout.p_idx, target_p_idx));
                if (!game.moveSpaceStack(new Point(planet.x, planet.y))) { // FIXME this may fail due to a traffic jam
                    throw new AIException("move from " + game.getPlanet(scout.p_idx).name + " to " + planet.name + " failed");
                }
                // need to set these or will try to draw empty stack
                game.setCurrentPlanetNr(scout.p_idx);
                game.setSelectedPoint(new Point(scout.x, scout.y), faction);
                game.setSelectedFaction(faction);
                if (scout.p_idx == target_p_idx) {
                    finished = true;
                    return false;
                }
                return scout.move_points > 0;
            default:
                throw new AssertionError();
        }

    }

}
