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
import galaxyreader.Planet;
import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Contract;
import game.Faction;
import game.GalaxyGrid;
import game.Game;
import game.Hex;
import game.Message;
import game.PlanetGrid;
import gui.ResolveContract;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.C;
import util.Comp;
import util.PathFind;
import util.Util;

/**
 * Abstract base class of all faction AI base classes.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public abstract class AI implements Serializable {

    private static final Logger logger = LogManager.getLogger(AI.class);
    private static final long serialVersionUID = 1L;
    private static final int TASK_FORCE_INIT_COUNT = 32;

    static boolean haveGroundTroop(List<Unit> stack) {
        for (Unit u : stack) {
            if (isGroundTroop(u)) {
                return true;
            }
        }
        return false;
    }

    static boolean isGroundTroop(Unit u) {
        if (u.type_data.non_combat == 0 && (u.move_type == C.MoveType.FOOT || u.move_type == C.MoveType.HOVER || u.move_type == C.MoveType.TREAD || u.move_type == C.MoveType.WHEEL) && (u.type_data.close_str > 0 || u.type_data.direct_str > 0 || u.type_data.indirect_str > 0 || u.type_data.psy_str > 0)) {
            return true;
        }
        return false;
    }
    /**
     * Return true iff Unit u is in task force number tf_id, tf_id of zero
     * indicates no task force.
     *
     * @param u
     * @param tf_id
     * @return
     */
    static boolean isInTaskForce(Unit u, int tf_id) {
        return u.task_force == tf_id;
    }
    /**
     * Return first instance of Unit which is in task force number tf_id, null
     * if none found in stack.
     *
     * @param stack
     * @param tf_id
     * @return
     */
    static Unit haveInTaskForce(List<Unit> stack, int tf_id) {
        for (Unit u : stack) {
            if (isInTaskForce(u, tf_id)) {
                return u;
            }
        }
        return null;
    }

    static int year_since_start;
    int faction;
    Game game;
    List<UnitType> buildable_units;
    List<Planet> planets;
    List<List<Continent>> continents;
    List<List<Continent>> oceans;
    List<Unit> all_units;
    List<Unit> units;
    List<Unit> units_land_battle;
    List<List<Unit>> stacks_land_battle_free;
    List<TaskForce> task_forces;
    List<TaskForceScout> task_force_scouts;
    Set<Unit> units_space_battle;
    Set<Unit> units_space_trasport;
    List<Unit> units_recon;
    List<Unit> units_engineer;

    List<Unit> enemy_units;
    List<Unit> enemy_units_on_planet;
    List<Unit> enemy_units_at_sea;
    List<Unit> enemy_units_in_space;
    List<Structure> all_structures;
    List<Structure> structures;
    List<Structure> enemy_structures;
    Set<Structure> free_enemy_structures;
    GalaxyGrid galaxy_grid;
    KnownGalaxy known_galaxy;
    private int task_force_id; // long -> int because taskforce id stored in Units

    public AI(Game game, int faction) {
        logger.debug("Hey");
        Util.dP("##### AI init begin");
        this.game = game;
        this.faction = faction;
        buildable_units = new ArrayList<>();
        all_units = game.getUnits();
        planets = game.getPlanets();
        all_structures = game.getStructures();
        galaxy_grid = game.getGalaxyMap();
        units = new LinkedList<>();
        units_land_battle = new LinkedList<>();
        stacks_land_battle_free = new LinkedList<>();
        task_forces = new ArrayList<>(TASK_FORCE_INIT_COUNT);
        task_force_scouts = new ArrayList<>(TASK_FORCE_INIT_COUNT);
        units_space_battle = new LinkedHashSet<>();
        units_space_trasport = new LinkedHashSet<>();
        units_recon = new LinkedList<>();
        units_engineer = new LinkedList<>();
        structures = new LinkedList<>();
        enemy_units = new LinkedList<>();
        enemy_units_at_sea = new LinkedList<>();
        enemy_units_in_space = new LinkedList<>();
        enemy_units_on_planet = new LinkedList<>();
        enemy_structures = new LinkedList<>();
        free_enemy_structures = new LinkedHashSet<>();
        continents = new ArrayList<>(planets.size());
        logger.debug(" continents " + continents.size());
        for (int i = 0; i < planets.size(); i++) {
            int size = planets.get(i).planet_grid.getContinentMaps().size();
            if (size > 1) { // handle ocean "continent"
                size--;
            }
            ArrayList<Continent> tmp = new ArrayList<>(size);
            for (int j = 0; j < size; j++) {
                tmp.add(new Continent());
            }
            continents.add(tmp);
            //logger.debug(" continents " + planets.get(i).name + " " + i + "," + tmp.size());
        }
//        TaskForceSuper.init(game);
        Util.dP("##### AI init end");
    }
    

    /**
     * Top level AI method.
     */
    public void doTurn() {
        year_since_start = game.getYear() - C.STARTING_YEAR;
    }


    protected void logSuper(int faction, String msg) {
        logger.debug("******** " + Util.getFactionName(faction) + " " + msg + " ********\n");
    }

    /**
     * To help alleviate galactic traffic jams, task forces that fail to fully
     * execute orders will be re-queued with a maximum number of requeues equal
     * to the number of task forces.
     */
    protected void moveTaskForces() {
        List<TaskForce> finished = new LinkedList<>();
        LinkedList<TaskForce> pending = new LinkedList<>();
        pending.addAll(task_forces);
        final int tf_count = pending.size();
        int run_count = 0;
        while (!pending.isEmpty()){
            TaskForce tf = pending.pollFirst();
            if (!tf.executePlan() && tf_count > run_count) {
                pending.addLast(tf);
            } else if (tf.target_hex == null) {
                finished.add(tf);
            }
            run_count++;
        }
        task_forces.removeAll(finished);
        String s_task = "";
        for (TaskForce task_force : task_forces) {
            s_task += task_force.tf_id + " ";
        }
        logger.debug(year_since_start + "   task forces left: " + s_task);
    }

    /**
     * To help alleviate galactic traffic jams, task forces that fail to fully
     * execute orders will be re-queued with a maximum number of requeues equal
     * to the number of task forces.
     */
    protected void moveTaskForceScouts() {
        List<TaskForceScout> finished = new LinkedList<>();
        LinkedList<TaskForceScout> pending = new LinkedList<>();
        pending.addAll(task_force_scouts);
        final int tf_count = pending.size();
        int run_count = 0;
        while (!pending.isEmpty()) {
            TaskForceScout tf = pending.pollFirst();
            if (!tf.executePlan() && tf_count > run_count) {
                pending.addLast(tf);
            } else if (tf.scout == null) {
                finished.add(tf);
            }
            run_count++;
        }
        task_force_scouts.removeAll(finished);
        String s_task = "";
        for (TaskForceScout tf : task_force_scouts) {
            s_task += tf.tf_id + " ";
        }
        logger.debug(year_since_start + "   task forces scouts left: " + s_task);
    }

    public int nextTfID() {
        return ++task_force_id;
    }

    /**
     *
     */
    protected void considerPeaceOffers() {
        Faction f_ref = game.getFaction(faction);
        for (Message message : f_ref.getMessages()) {
            Contract contract = message.getContract();
            if (contract == null) {
                continue;
            }
            boolean sue_for_peace = false;
            boolean pay_enough_compensation = false;
            boolean decline = false;
            for (Contract.Term term : contract.getTerms()) {
                switch (term.getType()) {
                    case STATE:
                        sue_for_peace = true;
                        break;
                    case MONEY:
                        if (term.getRecipient() == faction && term.getAmount() >= game.getDiplomacy().getCompensationMatrix(term.getDonor(), term.getRecipient())) {
                            pay_enough_compensation = true;
                        }
                        break;
                    default:
                        decline = true;
                        break;
                }
                if (decline) {
                    break;
                }
            }
            if (!(!decline && sue_for_peace && pay_enough_compensation && ResolveContract.tryToAccept(null, contract, game))) {
                ResolveContract.reject(contract, game);
            } else {
                // reset compensation counter
                game.getDiplomacy().zeroCompensationMatrix(contract.getSender(), contract.getReceiver());
            }
        }
        f_ref.getMessages();
    }

    protected void updatePersistent(int faction_id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private enum Task_Loop {

        AAA,
        ABB,
        BBA,
        ABA,
        ABC;
    }

    /**
     * Create task forces of units for specific missions. At this stage we are
     * mainly interested with transporting ground forces between continents and
     * planets to attack enemy assets.
     */
    protected void createTaskForces() {
        /*
         priority ordering: T(ransport)C(argo)H(ex) on planets: AAA, ABB, BBA, ABA, ABC
         */
        long c1 = 0;
        long c2 = 0;
        long c3 = 0;
        long c4 = 0;
        logger.debug(year_since_start + " createTaskForces() troops, targets, transports: " + stacks_land_battle_free.size() + "," + free_enemy_structures.size() + "," + units_space_trasport.size());
        Task_Loop state = Task_Loop.AAA;
        next_triplet:
        while (!stacks_land_battle_free.isEmpty() && !free_enemy_structures.isEmpty() && !units_space_trasport.isEmpty()) { 
            ++c1;
            for (Iterator<List<Unit>> c_it = stacks_land_battle_free.iterator(); c_it.hasNext();) {
                ++c2;
                List<Unit> stack = c_it.next();
                for (Iterator<Structure> h_it = free_enemy_structures.iterator(); h_it.hasNext();) {
                    ++c3;
                    Structure target = h_it.next();
                    for (Iterator<Unit> t_it = units_space_trasport.iterator(); t_it.hasNext();) {
                        ++c4;
                        Unit transport = t_it.next();
                        switch (state) {
                            case AAA:
                                if (stack.get(0).p_idx == target.p_idx && target.p_idx == transport.p_idx) {
                                    updateCollections(c_it, h_it, t_it);
                                    createTaskForce(target, stack, transport);
                                    continue next_triplet;
                                }
                                break;
                            case ABB:
                                if (stack.get(0).p_idx == target.p_idx) {
                                    updateCollections(c_it, h_it, t_it);
                                    createTaskForce(target, stack, transport);
                                    continue next_triplet;
                                }
                                break;
                            case BBA:
                                if (stack.get(0).p_idx == transport.p_idx) {
                                    updateCollections(c_it, h_it, t_it);
                                    createTaskForce(target, stack, transport);
                                    continue next_triplet;
                                }
                                break;
                            case ABA:
                                if (target.p_idx == transport.p_idx) {
                                    updateCollections(c_it, h_it, t_it);
                                    createTaskForce(target, stack, transport);
                                    continue next_triplet;
                                }
                                break;
                            case ABC:
                                updateCollections(c_it, h_it, t_it);
                                createTaskForce(target, stack, transport);
                                continue next_triplet;
                            default:
                                throw new AssertionError();
                        }

                    }
                }
            }
            switch (state) {
                case AAA:
                    state = Task_Loop.ABB;
                    break;
                case ABB:
                    state = Task_Loop.BBA;
                    break;
                case BBA:
                    state = Task_Loop.ABA;
                    break;
                case ABA:
                    state = Task_Loop.ABC;
                    break;
                case ABC:
                    break;
                default:
                    throw new AssertionError();
            }
        }
        logger.debug(year_since_start + " createTaskForces() loops: " + c1 + "," + c2 + "," + c3 + "," + c4);
    }

    private void createTaskForce(Structure target, List<Unit> stack, Unit transport) {
        TaskForce tf = new TaskForce(game, target.p_idx, game.getHexFromPXY(target.p_idx, target.x, target.y), nextTfID());
        tf.add(stack);
        tf.addTransport(transport);
        task_forces.add(tf);
        tf.initPlan();
        target.task_force |= (0x1 << faction);
    }

    private void updateCollections(Iterator<List<Unit>> c_it, Iterator<Structure> h_it, Iterator<Unit> t_it) {
        c_it.remove();
        h_it.remove();
        t_it.remove();
    }

    /**
     * Set build queues.
     */
    protected void buildUnits() {

    }

    /**
     * Form task forces of scouts to move to orbits of unmapped planets nearest
     * to known planets.
     */
    protected void createTaskForceScouts() {
        if (!enemy_structures.isEmpty()) {
            logger.debug(year_since_start + " No scouts created.");
            return;
        }
        for (Integer integer : known_galaxy.getEdge()) {
            logger.debug(year_since_start + "   scout considering planet " + planets.get(integer).name);
            int dist = Integer.MAX_VALUE;
            Unit chosen_scout = null;
            String s_dist = "";
            for (Unit unit : units_space_battle) {
                int current_dist = galaxy_grid.getJumpDistance(integer, unit.p_idx);
                if (current_dist < dist) {
                    dist = current_dist;
                    chosen_scout = unit;
                    s_dist += dist + " ";
                }
            }
            logger.debug(year_since_start + "  " + s_dist);
            if (chosen_scout != null) {
                TaskForceScout tf = new TaskForceScout(game, integer, nextTfID());
                tf.add(chosen_scout);
                units_space_battle.remove(chosen_scout);
                tf.initPlan();
                task_force_scouts.add(tf);
            }
        }
        logger.debug(year_since_start + " Scouts : " + task_force_scouts.size());
    }

    /**
     * Find assets (units, structures) of a faction, and their enemies. TODO
     * neutrals ?
     *
     * @param faction
     */
    protected void findAssets(int faction) {
        logger.debug(year_since_start + " Ai.findAssets()");
        long time = System.nanoTime();
        clear();
        // find own units and enemy units
        for (Unit unit : all_units) {
            if (unit.owner == faction) {
                units.add(unit);
                addToContinents(unit);
                unit.setSelected(false);
            } else if (unit.spotted[faction]) {
                enemy_units.add(unit);
                addToContinents(unit);
            }
        }
        // sort
        units.sort(Comp.unit_xy);
        units.sort(Comp.unit_cidx);
        units.sort(Comp.unit_pidx);
        enemy_units.sort(Comp.unit_xy);
        enemy_units.sort(Comp.unit_cidx);
        enemy_units.sort(Comp.unit_pidx);
        // group own units
        for (Unit unit : units) {
            if (unit.task_force > 0) {
            } else if (unit.type == C.NESTER_UNIT_TYPE) {
                units_engineer.add(unit);
            } else if (AI.isGroundTroop(unit)) {
                units_land_battle.add(unit);
                unit.setSelected(true);
            } else if (unit.move_type == C.MoveType.AIR) {
                units_recon.add(unit);
            } else if (unit.move_type == C.MoveType.JUMP || unit.move_type == C.MoveType.LANDER) {
                if (unit.type_data.cargo > 0) {
                    units_space_trasport.add(unit);
                } else {
                    units_space_battle.add(unit);
                }
            }
        }
        // group enemy units
        for (Unit unit : enemy_units) {
            if (unit.in_space) {
                enemy_units_in_space.add(unit);
            } else {
                enemy_units_on_planet.add(unit);
            }
        }
        for (Structure s : all_structures) {
            if (s.type == C.MONASTERY || s.type == C.ALIEN_RUINS || s.type == C.RUINS) {
                continue;
            }
            if (s.owner == faction) {
                structures.add(s);
                addToContinents(s);
            } else {
//                Hex hex = game.getHexFromPXY(s.p_idx, s.x, s.y);
//                if (hex.isSpotted(faction)) {
//                    enemy_structures.add(s);
//                }
                if (planets.get(s.p_idx).spotted[faction]) {
                    enemy_structures.add(s);
                    addToContinents(s);
                }
            }
        }
        structures.sort(Comp.city_xy);
        structures.sort(Comp.city_cidx);
        structures.sort(Comp.city_pidx);
        enemy_structures.sort(Comp.city_xy);
        enemy_structures.sort(Comp.city_cidx);
        enemy_structures.sort(Comp.city_pidx);

        for (Structure s : enemy_structures) {
            // if on continent with no friendly force in place or en-route
            if (((s.task_force >> faction) & 0x1) == 0 && continents.get(s.p_idx).get(game.getHexFromPXY(s.p_idx, s.x, s.y).getLandNr()).getAssetCount(faction).ground_combat == 0) {
                free_enemy_structures.add(s);
                logger.debug(year_since_start + "   free enemy city : " + game.getPlanet(s.p_idx).name + " " + s.x + "," + s.y);
            }
        }
        known_galaxy = new KnownGalaxy(game, faction);
        //logger.debug(year_since_start + " Ai.findAssets() end " + (System.nanoTime() - time) / 1_000_000d + "ms");
    }

    private void addToContinents(Structure s) {
        int land_nr = game.getHexFromPXY(s.p_idx, s.x, s.y).getLandNr();
        if (land_nr > -1) {
            continents.get(s.p_idx).get(land_nr).add(s);
        }
    }

    private void addToContinents(Unit unit) {
        if (unit.in_space) {
            return;
        }
        int land_nr = game.getHexFromPXY(unit.p_idx, unit.x, unit.y).getLandNr();
        if (land_nr > -1) {
            continents.get(unit.p_idx).get(land_nr).add(unit);
        }
    }

    protected void clear() { // fix #126
        units.clear();
        units_engineer.clear();
        units_land_battle.clear();
        units_recon.clear();
        units_space_battle.clear();
        units_space_trasport.clear();
        stacks_land_battle_free.clear();
        enemy_units.clear();
        enemy_units_at_sea.clear();
        enemy_units_in_space.clear();
        enemy_units_on_planet.clear();
        structures.clear();
        enemy_structures.clear();
        free_enemy_structures.clear();
        for (List<Continent> c : continents) {
            for (Continent c1 : c) {
                c1.clear();
            }
        }
    }

    /**
     * Top level ground conquer method, groups units per continent.
     *
     * @param faction
     */
    protected void conquerContinents(int faction) throws AIException {
        LinkedHashSet<List<Unit>> current_continent = new LinkedHashSet<>(C.PLANET_MAP_WIDTH * C.PLANET_MAP_COLUMNS);
        List<Unit> current_units = new LinkedList<>();      
        Hex[][] map = null;
        Point coord = new Point(-1, -1);
        int p_idx = -1;
        int c_idx = Integer.MIN_VALUE;
        for (Unit unit : units_land_battle) {
            if (unit.in_space) {
                throw new AIFatalException();
            }
            if (p_idx != unit.p_idx) {
                if (p_idx != -1) {
                    current_continent.add(current_units);
                    conquerContinent(p_idx, c_idx, current_continent, map);
                }
                p_idx = unit.p_idx;
                map = game.getPlanetGrid(p_idx).getMapArray();
                c_idx = map[unit.x][unit.y].getLandNr();
                current_continent.clear();
                current_units = new LinkedList<>();
            } else if (c_idx != map[unit.x][unit.y].getLandNr()) {
                current_continent.add(current_units);
                conquerContinent(p_idx, c_idx, current_continent, map);
                c_idx = map[unit.x][unit.y].getLandNr();
                current_continent.clear();
                current_units = new LinkedList<>();                
            } else if (coord.x != -1 && (coord.x != unit.x || coord.y != unit.y)) {
                current_continent.add(current_units);
                current_units = new LinkedList<>();               
            }
            coord.x = unit.x;
            coord.y = unit.y;
            current_units.add(unit);
        }
        if (p_idx != -1) {
            current_continent.add(current_units);
            conquerContinent(p_idx, c_idx, current_continent, map);
        }
    }

    /**
     * Mid level ground conquer method, handles conquests on one continent.
     *
     * @param p_idx
     * @param c_idx
     * @param stacks
     * @param map
     */
    protected void conquerContinent(int p_idx, int c_idx, LinkedHashSet<List<Unit>> stacks, Hex[][] map) throws AIException {
        logger.debug(year_since_start + " Ai.conquerContinent() " + planets.get(p_idx).name + "," + c_idx);
        for (List<Unit> stack : stacks) {
            String tmp = "";
            for (Unit u : stack) {
                tmp += u.type_data.abbrev + " ";
                if (u.in_space) {
                    throw new AIFatalException("Ground unit " + u.p_idx + "," + u.x + "," + u.y + "," + u.getUnit_no() + " with in_space flag.");
                }
            }
            logger.debug(year_since_start + "   stacks (" + stacks.size() + ") on continent: " + stack.hashCode() + " " + stack.size() + " " + stack.get(0).x + "," + stack.get(0).y + " " + tmp + stack);
        }
        game.setSelectedFaction(-1, -1);
        long time = System.nanoTime();
        PlanetGrid pg = game.getPlanetGrid(p_idx);
        LinkedHashSet<Hex> targets = getTargets(p_idx, c_idx, map);
        {
            String tmp = "";
            for (Hex target : targets) {

                tmp += ((target.getStructure() != null) ? target.getStructure().type : "null") + ":" + target.getX() + "," + target.getY() + " ";

            }
            logger.debug(year_since_start + "   targets (" + targets.size() + ") on continent: " + tmp);
        }
        while (!targets.isEmpty() && !stacks.isEmpty()) {
            Hex h = null;
            List<Unit> s = null;
            int dist = Integer.MAX_VALUE;
            String s_dists = "";
            for (Hex target : targets) {
                for (List<Unit> stack : stacks) {
                    int tmp = pg.getIntraContHexDist(target, map[stack.get(0).x][stack.get(0).y]);
                    if (tmp == Byte.MIN_VALUE) { // for pathological continents
                        tmp = Integer.MAX_VALUE;
                    }
                    if (tmp < dist) {
                        h = target;
                        s = stack;
//                        if (s.equals(stack)) {
//                            System.exit(0);
//                        }
                        dist = tmp;
                        s_dists += "s:" + stack.get(0).p_idx + ":" + stack.get(0).x + "," + stack.get(0).y + " t:" + target.getX() + "," + target.getY() + " ";
                    }
                }
            }
            logger.debug(year_since_start + "      dist " + dist + " " + s_dists);
            if (!targets.remove(h)) {
                throw new AIException(h.toString());
            }
            if (!stacks.remove(s)) {
                throw new AIException("" + s.hashCode());
            }
            selectLandUnits(s, true);
            game.setCurrentPlanetNr(p_idx);
            game.setSelectedPoint(new Point(s.get(0).x, s.get(0).y), -1);
            LinkedList<Hex> path = PathFind.findPath(game, pg, h, map[s.get(0).x][s.get(0).y]);
            if (path == null) {
                logger.debug(year_since_start + " No path to target, canceling movement ...");
                selectLandUnits(game.getSelectedStack(), false);
                continue;
            }
            game.setPath(path);
            String s_moves = "";
            while (game.getPath().size() > 1 && Util.moveCapable(game) && moveToNextHex(path, s)) {
                game.setSelectedPoint(new Point(s.get(0).x, s.get(0).y), -1);
                s_moves += game.getPath().size() + " ";
            }
            logger.debug(year_since_start + "      " + s.get(0).x + "," + s.get(0).y + " path " + s_moves);
            // TODO if path.size() == 1 try to capture any routed enemies
            selectLandUnits(game.getSelectedStack(), false);

        }
        // collect idle ground forces
        String s_idle_stacks = "";
        for (List<Unit> stack : stacks) {
            s_idle_stacks += stack.size() + ":" + stack.get(0).x + "," + stack.get(0).y + " ";
            stacks_land_battle_free.add(stack);
        }
        if (s_idle_stacks.length() > 0) {
            logger.debug(year_since_start + "   idle stacks (" + stacks.size() + ") : " + s_idle_stacks);
        }
        //logger.debug(year_since_start + " Ai.conquerContinent() end " + (System.nanoTime() - time) / 1_000_000d + "ms");
    }

    private void selectLandUnits(List<Unit> s, boolean selected) {
        for (Unit u : s) {
            if (u.type_data.non_combat == 0 && (u.move_type == C.MoveType.FOOT
                    || u.move_type == C.MoveType.WHEEL
                    || u.move_type == C.MoveType.TREAD
                    || u.move_type == C.MoveType.HOVER)) {
                u.setSelected(selected);
            }
        }
    }

    /**
     * What happens when a stack moves between two planetary hexes, when stack
     * is owned by the Symbiots.
     * <p>
     * We have four possibilities for structures: 1. no city; 2. own city; 3.
     * neutral city; 4. enemy city
     * <p>
     * And four possibilities for units: 1. no units; 2. own units; 3. neutral
     * units; 4. enemy units
     * <p>
     * For neutral and enemy units we have two possibilities: 1. only non ground
     * combatants in stack; 2. ground combatants in stack
     * <p>
     * Additionally, we have a special case for neutral cities: 0. city is a
     * League agora and there are cargo pods in moving stack.
     * <p>
     * Additionally, we have special cases for own units/cities: in 1.2 and 2
     * cannot merge loaned unit stacks
     * <p>
     * Combining the above, and removing impossible combinations we get the
     * following cases (stacks with only non-combat units are handled
     * separately):
     * <pre>
     * 1: no city in target hex
     *   1.1: no enemy units in stack
     *   *** try to move
     *   1.4: enemy units in stack
     *     1.4.1: only non ground combatants in stack
     *     *** capture
     *     1.4.2: ground combatants in stack
     *     *** battle
     * 2: own city in hex
     * *** try to move
     * 4: at war with city owner
     *   4.1: no units in stack
     *   *** capture
     *   4.4: enemy units in stack
     *     4.4.1: only non ground combatants in stack
     *     *** capture
     *     4.4.2: ground combatants in stack
     *     *** battle
     * </pre>
     *
     * @param path
     * @param moving_stack
     * @return true iff moving stack should continue moving along path, i.e. no
     * events occurred which mandate stopping
     */
    protected boolean moveToNextHex(LinkedList<Hex> path, List<Unit> moving_stack) {
        Hex target_hex = path.get(1);
        Structure city = target_hex.getStructure();
        List<Unit> stack = target_hex.getStack();
        Unit moving_unit = moving_stack.get(0);
        Point faction = new Point(moving_unit.owner, moving_unit.prev_owner);
        boolean stack_moving = true;
        // handle non-combat stacks and agora sales
        if (!Util.anyCombat(moving_stack)) {
////            logger.debug(year_since_start + " PW4 non-combat stack");
//            boolean agora_sale = false;
            if (city != null && city.owner != faction.x) {
//                if (city.type == C.AGORA && city.owner == C.LEAGUE
//                        && game.getDiplomacy().getDiplomaticState(city.owner, faction.x) != C.DS_WAR
//                        && Util.anyCargoPods(moving_stack)) {
////                    logger.debug(year_since_start + "  + agora sale");
//                    agora_sale = true;
//                } else {
//                    stop();
                    return false;
            }
//            }
            if (!stack.isEmpty() && stack.get(0).owner != faction.x) {
                return false;
            }
        }
        //1: no city in hex
        if (city == null) {
            //1.1: no enemy units in stack
            if (stack.isEmpty() || stack.get(0).owner == faction.x) {
//                logger.debug(year_since_start + " PW4 1.1");
                stack_moving = tryToMove();
                //1.4: enemy units in stack
            } else {
//                logger.debug(year_since_start + " PW4 1.4");
                //1.4.1: only non ground combatants in stack
                if (game.isCapture()) {
//                    logger.debug(year_since_start + " PW4 1.4.1");
                    game.capture(faction);
//                    stack_moving = false;
                    //1.4.2: ground combatants in stack
                } else {
//                    logger.debug(year_since_start + " PW4 1.4.2");
                    combat();
//                    stack_moving = false;
                }
            }
            //2: own city in hex
        } else if (city.owner == faction.x) {
            stack_moving = tryToMove();
            //4: at war with city owner
        } else {
            //4.1: no units in stack
            if (stack.isEmpty()) {
//                logger.debug(year_since_start + " PW4 4.1");
                game.captureCity(city, faction.x, faction.y);
                stack_moving = tryToMove();
//                stack_moving = false;
                //4.4: enemy units in stack
            } else {
                //4.4.1: only non ground combatants in stack
                if (game.isCapture()) {
//                    logger.debug(year_since_start + " PW4 4.4.1");
                    game.capture(faction);
                    game.captureCity(city, faction.x, faction.y);
//                    stack_moving = false;
                    //4.4.2: ground combatants in stack
                } else {
//                    logger.debug(year_since_start + " PW4 4.4.2");
                    combat();
//                    stack_moving = false;
                }
            }
        }
        return stack_moving;
    }

    protected boolean tryToMove() {
        return game.moveStack();
    }

    private void combat() {
//        gui.setStop_stack(true);
        game.resolveGroundBattleInit(C.GROUND_COMBAT, -1);
        game.resolveGroundBattleFight();
        game.resolveGroundBattleFinalize();
//        gui.setCurrentState(PW2.get());
//        gui.setMenus(C.S_PLANET_MAP);
//        gui.getStack_move_timer().stop();
//        gui.setStack_moving(false);
//        SU.showCombatWindow();
    }

//    private void stop() {
//        gui.setStop_stack(true);
//        game.setPath(null);
//        gui.setCurrentState(PW2.get());
//        gui.setMenus(C.S_PLANET_MAP);
//        gui.getStack_move_timer().stop();
//        gui.setStack_moving(false);
//    }
    /**
     * Find all hexes with enemy assets (units, structures) on planet p_idx,
     * continent c_idx.
     *
     * @param p_idx
     * @param c_idx
     * @param map
     * @return hexes with enemy assets.
     */
    protected LinkedHashSet<Hex> getTargets(int p_idx, int c_idx, Hex[][] map) {
        LinkedHashSet<Hex> targets = new LinkedHashSet<>(C.PLANET_MAP_WIDTH * C.PLANET_MAP_COLUMNS);
        String s_targets = "";
        for (Structure s : enemy_structures) {
            if (s.p_idx == p_idx && map[s.x][s.y].getLandNr() == c_idx) {
                targets.add(map[s.x][s.y]);
                s_targets += Structure.getName(s.type) + " " + s.x + "," + s.y + " ";
            }
        }
        if (s_targets.length() > 0) {
            logger.debug(year_since_start + "      cities: " + s_targets);
        }
        for (Unit u : enemy_units_on_planet) {
            if (u.p_idx == p_idx) {
                Hex tmp = map[u.x][u.y];
                if (tmp.getLandNr() == c_idx) {
                    if (tmp.getTerrain(C.OCEAN)) {
                        System.out.println(" OCEAN TARGET: c_idx:" + c_idx + " x,y:" + tmp.getX() + "," + tmp.getY());
                    }
                    //assert !tmp.getTerrain(C.OCEAN) || tmp.getTerrain(C.DELTA);
                    targets.add(tmp);
                }
            }
        }
        return targets;
    }

    /**
     * Top level reconnaissance method.
     */
    protected void reconAll() {
        LinkedList<Unit> current_units = new LinkedList<>();
        int p_idx = -1;
        for (Unit unit : units_recon) {
            if (p_idx != unit.p_idx) {
                if (p_idx != -1) {
                    logger.debug(year_since_start + "  call Ai.reconPlanet()");
                    reconPlanet(current_units, p_idx);
                    current_units.clear();
                }
                p_idx = unit.p_idx;
            }
            current_units.add(unit);
        }
        reconPlanet(current_units, p_idx);
    }

    /**
     * Recon planet
     *
     * @param units
     * @param p_idx
     */
    protected void reconPlanet(LinkedList<Unit> units, int p_idx) {
        PlanetGrid pg = game.getPlanetGrid(p_idx);
        LinkedList<Hex> unexplored = new LinkedList();
        /*
         Is there need for the Symbiots to explicitly recon ? All cities are
         visible from
         orbit, and all cities are allways targets for the Symbiots. If any
         non-symbiot units are spotted while going for the cities they will be
         included as targets.

         Air units triangular patrol from/around cities:
         1 turn::go move/3 in some direction, then
         turn left (or right) twice, move/3, then turn twice to same direction as
         previously, move/3 and you are back in the starting city.
         1 turn example:: go north for move/3, go southwest for move/3, go southeast for
         move/3 and you are back in the city
         2 turn:: same as above but use 2*move/3
         */
    }

    /**
     * WIP Yes/no response to a peace offer based on firebirds offered/demanded
     * compared to amount of damage caused.
     *
     * @param firebirds
     * @return
     */
    protected boolean considerPeaceOffer(int firebirds) {
        return false;
    }

    boolean isMapped(int p_idx) {
        return known_galaxy.isMapped(p_idx);
    }

}
