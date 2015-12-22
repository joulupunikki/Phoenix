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

import dat.UnitType;
import galaxyreader.Unit;
import game.Game;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.C;
import util.Util;

/**
 * Symbiot AI base class. In EFS1.4 symbiots have no tech, their all_units have
 * no cost other than build time, and the only structure capable of building
 * symbiot all_units is the hive. Also, the symbiot prime directives are 1.
 * Violent conquest of all the planets. 2. Never surrender. Also, they are a
 * supporting faction. With these premises, writing a simple but reasonably
 * working Symbiot AI should be among the easiest faction AI writing tasks for
 * Phoenix.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class SymbiotAI extends AI {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(SymbiotAI.class);
    public enum UTypes {

        PSYCH,
        CLOSE,
        DIRECT,
        INDIRECT,
        AIR,
        NESTER,
        CLOSE_SP,
        DIRECT_SP,
        RANGED_SP,
        CARGO_SP,
    }

    public SymbiotAI(Game game) {
        super(game, C.SYMBIOT);
        Util.dP("##### SymbiotAI init begin");
        classifyUnits();
        Util.dP("##### SymbiotAI init end");
    }

    @Override
    public void doTurn() {
        TaskForce.zeroExceptionCounter();
        try {
            logSuper(C.SYMBIOT, "Start");
            // list stacks, cities, own enemies
            findAssets(C.SYMBIOT);
            // symbiots are no retreat, no surrender
            for (Unit unit : units) {
                unit.loyalty = SYMBIOT_LOYALTY;
            }
            // if enemy cities on same continent as own units, conquer
            conquerGalaxy(C.SYMBIOT);
            /* find ground units with no targets available, find available transports
             assign ground units to transports, set task force destination, start moving task force
             */
            createTaskForces();
            moveTaskForces();
            // attack enemy cities
            // attack enemy units
            logSuper(C.SYMBIOT, "End");
        } catch (AIException ex) {
            logger.debug("", ex);
        }
        int ex_count = TaskForce.getExceptionCounter();
        if (ex_count > 0) {
            logger.debug(" ******** Exceptions: " + ex_count + " ********");
        }
    }
    private static final int SYMBIOT_LOYALTY = 100;

    private void classifyUnits() {
        for (UnitType[] type : game.getUnitTypes()) {
            for (UnitType type1 : type) {
                if (type1 != null && type1.bldgs == C.HIVE) {
                    System.out.println(type1.abbrev);
                }
            }
        }
    }

}
