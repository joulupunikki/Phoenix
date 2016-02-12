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
import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Game;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.C;
import util.RingCounter;
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

    protected enum UNames {
        GRAPPLER,
        BLOW_SHIP,
        SPORE_SHIP,
        POD_SHIP,
        SPITTER,
        MINDER,
        SHUFFLER,
        ARCER,
        BUTCHER,
        REAVER,
        NESTER,
    }
    /**
     * This is the symbiot build order as given by Matt Caspermeyer for
     * EFSHYP.EXE on kborek.cz, with the replaced ground units inserted after
     * the space replacements, so the ratios of EFS.EXE ground units stay the
     * same.
     */
    private static final UNames[] SYMBIOT_BUILD_ORDER = {UNames.SPITTER,
        UNames.SHUFFLER,
        UNames.ARCER,
        UNames.BUTCHER,
        UNames.SPITTER,
        UNames.SHUFFLER,
        UNames.BUTCHER,
        UNames.GRAPPLER,
        UNames.SPITTER,
        UNames.REAVER,
        UNames.MINDER,
        UNames.POD_SHIP,
        UNames.SHUFFLER,
        UNames.ARCER,
        UNames.SPORE_SHIP,
        UNames.SPITTER,
        UNames.BUTCHER,
        UNames.REAVER,
        UNames.SHUFFLER,
        UNames.BLOW_SHIP,
        UNames.SPITTER,
        UNames.SPORE_SHIP,
        UNames.BUTCHER,
        UNames.GRAPPLER,
        UNames.REAVER,
        UNames.NESTER
    };
    /**
     * How many percent of each unit would be in an ideal force composition.
     */
    protected double[] ideal_force_fractions;
    protected int[] real_force_counts;
    protected int[] buildable_indexes = new int[game.getUnitTypes().length];
    protected RingCounter build_idx;

    public SymbiotAI(Game game) {
        super(game, C.SYMBIOT);
        Util.dP("##### SymbiotAI init begin");
        build_idx = new RingCounter(SYMBIOT_BUILD_ORDER.length - 1, 0);
        classifyUnits();
        Util.dP("##### SymbiotAI init end");
    }

    @Override
    public void doTurn() {
        TaskForce.zeroExceptionCounter();
        try {
            logSuper(C.SYMBIOT, "Start");
            // list stacks, cities, own, enemies
            findAssets(C.SYMBIOT);
            // symbiots are no retreat, no surrender
            for (Unit unit : units) {
                unit.loyalty = SYMBIOT_LOYALTY;
            }
            // if enemy cities on same continent as own units, conquer
            conquerContinents(C.SYMBIOT);
            // if no enemy cities known, send warships to closest unmapped planets
            createTaskForceScouts();
            moveTaskForceScouts();
            /* find ground units with no targets available, find available transports
             assign ground units to transports, set task force destination, start moving task force
             */
            createTaskForces();
            moveTaskForces();
            // queue more units to build if necessary
            getRealForceCounts();
            buildUnits();
            logSuper(C.SYMBIOT, "End year " + game.getYear() + " (+" + (game.getYear() - C.STARTING_YEAR) + ")");
        } catch (AIException ex) {
            logger.debug("", ex);
        }
        int ex_count = TaskForce.getExceptionCounter();
        if (ex_count > 0) {
            logger.debug(" ******** Exceptions: " + ex_count + " ********");
        }
    }

    private void getRealForceCounts() {

        for (int i = 0; i < real_force_counts.length; i++) {
            real_force_counts[i] = 0;
        }
        for (Unit unit : units) {
            if (buildable_indexes[unit.type] > -1) {
                real_force_counts[buildable_indexes[unit.type]]++;
            }
        }
//        for (int i = 0; i < real_force_counts.length; i++) {
//            real_force_counts[i] /= units.size();
//        }
    }

    @Override
    protected void buildUnits() {
        int in_queue = 0;
        String msg = "";
        for (Structure structure : structures) {
            if (structures.size() * MAX_UNITS_PER_CITY < units.size()) {
                break;
            }
            if (structure.type == C.HIVE) {
                //in_queue++;
                if (!structure.build_queue.isEmpty()) {
                    continue;
                }
                int shortage = 0;
                double fraction = Double.MAX_VALUE;
                for (int i = 0; i < real_force_counts.length; i++) {
                    double tmp_fract = real_force_counts[i] / ((units.size() + in_queue) * ideal_force_fractions[i]);
                    if (tmp_fract < fraction) {
                        fraction = tmp_fract;
                        shortage = i;
                    }
                }
                real_force_counts[shortage]++;
                UnitType tmp = buildable_units.get(shortage);
                int[] tmp2 = {tmp.index, tmp.t_lvl};
                structure.addToQueue(tmp2, game.getUnitTypes(), game);
                msg += tmp.name + " " + structure.p_idx + " " + "(" + structure.x + "," + structure.y + ") ";
            }
        }
        if (msg.length() > 0) {
            String fractions = "";
            for (int i = 0; i < real_force_counts.length; i++) {
                fractions += real_force_counts[i] / (units.size() * ideal_force_fractions[i]) + " ";

            }
            logger.debug("Force fractions: " + fractions);
            logger.debug("Build: " + msg);
        }
    }
    private static final int SYMBIOT_LOYALTY = 100;
    private static final int MAX_UNITS_PER_CITY = 40;

    private void classifyUnits() {
        // find buildable units
        for (int i = 0; i < buildable_indexes.length; i++) {
            buildable_indexes[i] = -1;
        }
        int idx = 0;
        for (UnitType[] types : game.getUnitTypes()) {
            for (UnitType type : types) {
                if (type != null && type.bldgs == C.HIVE) {
                    System.out.println(type.abbrev);
                    buildable_units.add(type);
                    buildable_indexes[type.index] = idx++;
                }
            }
        }
        real_force_counts = new int[buildable_units.size()];
        ideal_force_fractions = new double[buildable_units.size()];
        for (UNames uNames : SYMBIOT_BUILD_ORDER) {
            ideal_force_fractions[uNames.ordinal()]++;
        }
        for (int i = 0; i < ideal_force_fractions.length; i++) {
            ideal_force_fractions[i] /= ideal_force_fractions.length;
        }
    }

}
