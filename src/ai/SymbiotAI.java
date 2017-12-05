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
import gui.Gui;
import java.util.LinkedList;
import java.util.List;
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

    /**
     * Names and indexes of buildable symbiot units in UNIT.DAT
     */
    protected enum UNames {
        GRAPPLER(0),
        BLOW_SHIP(1),
        SPORE_SHIP(2),
        POD_SHIP(3),
        SPITTER(38),
        MINDER(39),
        SHUFFLER(40),
        ARCER(41),
        BUTCHER(42),
        REAVER(43),
        NESTER(44),;

        private final int idx;

        private UNames(int idx) {
            this.idx = idx;
        }

        public int idx() {
            return idx;
        }
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

    protected int native_symbiot_count;

    public SymbiotAI(Game game) {
        super(game, C.SYMBIOT);
        Util.dP("##### SymbiotAI init begin");
        build_idx = new RingCounter(SYMBIOT_BUILD_ORDER.length - 1, 0);
        classifyUnits();
        Util.dP("##### SymbiotAI init end");
    }

    @Override
    public void doTurn() {
        super.doTurn();
        TaskForce.zeroExceptionCounter();
        try {
            logSuper(C.SYMBIOT, "Start");
            // update AI data after enemy/neutral activity
            updatePersistent(C.SYMBIOT);
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
            countNativeSymbiots();
            getRealForceCounts();
            buildUnits();
            logSuper(C.SYMBIOT, "End year " + game.getYear() + " (+" + (game.getYear() - C.STARTING_YEAR) + ")");
        } catch (AIException ex) {
            logger.debug("", ex);
            if (ex instanceof AIFatalException) {
                if (Gui.getMainArgs().hasOption(C.OPT_DEBUG_STOP)) {
                logger.debug("AI Debug stop", ex);
                    game.setDebugStop();
                }
            }
        } catch (Throwable t) {
            if (Gui.getMainArgs().hasOption(C.OPT_DEBUG_STOP)) {
            logger.debug("AI Debug stop", t);
                game.setDebugStop();
            }
        }
        int ex_count = TaskForce.getExceptionCounter();
        if (ex_count > 0) {
            logger.debug(" ******** Exceptions: " + ex_count + " ********");
        }
    }

    private void countNativeSymbiots() {
        native_symbiot_count = 0;
        for (Unit unit : units) {
            if (unit.owner == C.SYMBIOT && unit.type_data.t_lvl == 0 && unit.type_data.bldgs == C.HIVE) {
                ++native_symbiot_count;
            }
        }
        logger.debug("Native symbiot count: " + native_symbiot_count);
    }

    @Override
    protected void updatePersistent(int faction_id) {
        // check for lost units in task_forces
        List<TaskForce> tf_finished = new LinkedList<>();
        for (TaskForce task_force : task_forces) {
            List<Unit> dead = new LinkedList<>();
            for (Unit u : task_force.ground_forces) {
                if (u.task_force == Integer.MIN_VALUE) {
                    dead.add(u);
                }
            }
            task_force.ground_forces.removeAll(dead);
            dead.clear();
            for (Unit u : task_force.transports) {
                if (u.task_force == Integer.MIN_VALUE) {
                    dead.add(u);
                }
            }
            task_force.transports.removeAll(dead);
            if (task_force.ground_forces.isEmpty() || task_force.transports.isEmpty()) {
                tf_finished.add(task_force);
            }
        }
        task_forces.removeAll(tf_finished);
        for (TaskForce taskForce : tf_finished) {
            taskForce.finished();
        }
        // check for lost units in scouts
        List<TaskForceScout> tfs_finished = new LinkedList<>();
        for (TaskForceScout task_force_scout : task_force_scouts) {
            if (task_force_scout.scout.task_force == Integer.MIN_VALUE) {
                tfs_finished.add(task_force_scout);
            }
        }
        task_force_scouts.removeAll(tfs_finished);
        for (TaskForceScout taskForceScout : tfs_finished) {
            taskForceScout.finished();
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
        String msg = "";
        for (int i = 0; i < real_force_counts.length; i++) {
            msg += " " + buildable_units.get(i).abbrev + ":" + real_force_counts[i];

        }
        logger.debug("Real force counts: " + msg);
    }

    @Override
    protected void buildUnits() {
        int in_queue = 0;
        String msg = "";
        for (Structure structure : structures) {
            if (structures.size() * MAX_UNITS_PER_CITY < native_symbiot_count) {
                logger.debug("Too many units, stopping build");
                break;
            }
            if (structure.type == C.HIVE) {
                //in_queue++;
                if (!structure.build_queue.isEmpty()) {
                    continue;
                }
                System.out.println("###");
                int shortage = 0;
                double fraction = Double.MAX_VALUE;
                for (int i = 0; i < real_force_counts.length; i++) {
                    double tmp_fract = real_force_counts[i] / ((native_symbiot_count + in_queue) * ideal_force_fractions[i]);
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
                fractions += buildable_units.get(i).abbrev + " " + real_force_counts[i] / (native_symbiot_count * ideal_force_fractions[i]) + " ";

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
//        UnitType[][] types = game.getUnitTypes();
//        for (int i = 0; i < types.length; i++) {
//            UnitType type = types[i][0];
//            if (type != null && type.bldgs == C.HIVE) {
//                System.out.println(type.abbrev);
//                buildable_units.add(type);
//                buildable_indexes[type.index] = idx++;
//            }
//        }
        for (UnitType[] types : game.getUnitTypes()) {
            for (UnitType type : types) {
                if (type != null && type.t_lvl == 0 && type.bldgs == C.HIVE) {
                    System.out.println(type.abbrev);
                    buildable_units.add(type);
                    buildable_indexes[type.index] = idx++;
                }
            }
        }
        real_force_counts = new int[buildable_units.size()];
        ideal_force_fractions = new double[buildable_units.size()];
        for (UNames uNames : SYMBIOT_BUILD_ORDER) {
            ideal_force_fractions[uNames.ordinal()] += 1;
        }
        for (int i = 0; i < ideal_force_fractions.length; i++) {
            ideal_force_fractions[i] /= ideal_force_fractions.length;
        }
//        String msg = "";
//        for (int i = 0; i < ideal_force_fractions.length; i++) {
//            msg += UNames.
//            double ideal_force_fraction = ideal_force_fractions[i];
//
//        }
    }

}
