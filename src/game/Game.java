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

import ai.AIObject;
import ai.StaticThreads;
import com.github.joulupunikki.math.random.XorShift1024Star;
import dat.Damage;
import dat.EfsIni;
import dat.ResType;
import dat.StrBuild;
import dat.Target;
import dat.TerrCost;
import dat.UnitType;
import galaxyreader.Galaxy;
import galaxyreader.JumpGate;
import galaxyreader.Planet;
import galaxyreader.Structure;
import galaxyreader.Unit;
import gui.CombatStrategyPanel;
import gui.Gui;
import gui.Resource;
import java.awt.Point;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import org.apache.commons.math3.random.RandomAdaptor;
import util.C;
import util.StackIterator;
import util.Util;
import util.Util.HexIter;

/**
 * Root object of all game state and is directly serialized to and from save
 * files during the File->Save and File->Load actions and initialized from EFS
 * and Phoenix data files during Phoenix boot up and File->Restart actions.
 *
 * @author joulupunikki
 */
public class Game implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    //***** these variables are used for interfacing with the GUI and the Game object
    //***** they can be considered part of the GUI state
    //coordinates of currently selected hex/square/stack  
    private Point selected_point;
    //faction of selected space stack, x == owner, y == prev_owner
    private Point selected_faction = new Point(-1, -1);
    // the upper left corner of the planet map
    private Point planet_map_origin;    // Not used? RSW
    private Point space_map_origin;    // Not used? RSW
    // the travel path of the selected stack
    private LinkedList<Hex> path;
    // the travel path of the selected space stack
    private JumpGate jump_path;
//    private PlanetGrid[] planet_grids;
    private int current_planet;
    //***** these variables are purely Game object state
    //game year
    private int year;
    //turn ie the current faction id
    private int turn;
    private boolean[] human_ctrl;
    private Galaxy galaxy;
    private GalaxyGrid galaxy_grid;
    private UnitType[][] unit_types;
    private ResType[] res_types;
    private StrBuild[] str_build;
    private double[][][] terr_cost;
//    private int max_spot_range;
    private GameResources game_resources;
    private Resource dat_resources;
//    private List<Unit> current_stack;
    private List<Planet> planets;
    private List<JumpGate> jump_gates;
    private List<Unit> units;
    private List<Structure> structures;

    private Faction[] factions = new Faction[C.NR_FACTIONS];    // RSW
    private Regency regency = new Regency();
    private List<Unit> unmoved_units;
    private List<Unit> cargo_pods;
    private List<Structure> faction_cities;

//    private List<Unit> combat_stack_a;
//    private List<Unit> combat_stack_b;
//    private String combat_type;
//    private int attacked_faction;
//    
    private int[][] damage;
    private int[][] target;

    private Random random;
    private long initial_seed;
    private EfsIni efs_ini;
    private Resources resources;
    private Economy economy;
    private Battle battle;
    private HexProc hex_proc;
    private Diplomacy diplomacy;

    private AIObject ai;

    public Game(String galaxy_file, int current_planet) {

        random = RandomAdaptor.createAdaptor(new XorShift1024Star(1L));

        // Read fixed data files
        unit_types = UnitType.readUnitDat();
        res_types = ResType.readResDat();
        str_build = StrBuild.readStrBuildDat();
        terr_cost = TerrCost.readTerrCost();
        damage = Damage.readDamageDat();
        target = Target.readTargetDat();
        galaxy = Galaxy.loadGalaxy(galaxy_file);
        planet_map_origin = new Point(0, 0);
        space_map_origin = new Point(0, 0);

        planets = new ArrayList<>(galaxy.getPlanets().size());
        planets.addAll(galaxy.getPlanets());
        jump_gates = galaxy.getJumpGates();
        units = galaxy.getUnits();
        structures = galaxy.getStructures();
        galaxy_grid = new GalaxyGrid(galaxy);
        human_ctrl = new boolean[14];
        human_ctrl[0] = true;
        unmoved_units = new LinkedList<>();
        faction_cities = new LinkedList<>();
        this.current_planet = current_planet;
        year = C.STARTING_YEAR;
        turn = -1;
        hex_proc = new HexProc(this);

        game_resources = new GameResources();
//        Structure.setCanBuild(unit_types);
//        factions = Faction.createFactions();

        placeUnits();
        checkStackSizes();
        placeStructures();
        resetMovePoints();
        resetUnmovedUnits(true);

        setMoveType();
        setUnitTypeData();
        setJumpRoutes();
        initVisibilitySpot(true);
        setMaxSpotRange();

        battle = new Battle();

        initAI(true);
    }

    public void initAI(boolean dynamic) {
        if (!Gui.getMainArgs().hasOption(C.OPT_ENABLE_AI)) {
            return;
        }
        for (Planet planet : planets) { // fast serial
            planet.planet_grid.serialSetAIDataStructures(planet);
        }
        StaticThreads.dispatchStaticAIWorker(planets); // slow parallel
        if (!dynamic) {
            return;
        }
//        if (!Gui.getMainArgs().hasOption(C.OPT_ENABLE_AI)) {
//            return;
//        }
        ai = new AIObject();
        ai.adAI(this, C.LEAGUE);
        ai.adAI(this, C.THE_CHURCH);
        if (!Gui.getMainArgs().hasOption(C.OPT_AI_TEST)) {
            return;
        }
        ai.adAI(this, C.SYMBIOT);

    }

    private void checkStackSizes() {
        for (Planet planet : planets) {
            HexIter hi = new HexIter(this, planet.index);
            for (Hex h = hi.next(); h != null; h = hi.next()) {
                int size = h.getStack().size();
                if (size > C.STACK_SIZE) {
                    System.out.println("Stack size " + size + " on " + planet.name + " at (" + h.getX() + "," + h.getY() + ") faction " + Util.getFactionName(h.getStack().get(0).owner));
                }
            }
        }
    }

    public void init(Resource gui_resource) {

        battle.battleInit(random, damage, target, terr_cost, this, planets);

        efs_ini = EfsIni.readEfsIni(gui_resource.getEFSIni(), gui_resource.getPhoenixIni());
        Target.setLanderVulnerability(target, efs_ini);
        resources = new Resources(this);
        economy = new Economy(this, resources);

        for (int i = 0; i < C.NR_FACTIONS; i++) {
            factions[i] = new Faction(this, i);
        }
        diplomacy = new Diplomacy(this);
        diplomacy.initDiplomacy();
    }

    public void initVisibilitySpot(boolean do_reset) {
        if (do_reset) {
            for (Planet planet : planets) {
                Hex[][] planet_grid = planet.planet_grid.getMapArray();
                for (int i = 0; i < planet_grid.length; i++) {
                    for (int j = 0; j < planet_grid[i].length; j++) {
                        planet_grid[i][j].initVisibility();
                    }
                }
            }
        }
//        for (Structure structure : structures) {
//            Hex hex = getPlanetGrid(structure.p_idx).getHex(structure.x, structure.y);
//            hex_proc.hexProc(hex, 5, structure.owner, C.INIT_SPOT);
//        }
        for (Planet planet : planets) {
            Hex[][] planet_grid = planet.planet_grid.getMapArray();
            for (int i = 0; i < planet_grid.length; i++) {
                for (int j = 0; j < planet_grid[i].length; j++) {
                    Hex hex = planet_grid[i][j];
                    hex_proc.initSpotForHex(hex, planet);

                }

            }
            List<Unit>[] stacks = planet.space_stacks;
            for (int i = 0; i < stacks.length; i++) {
                if (!stacks[i].isEmpty()) {
                    int owner = stacks[i].get(0).owner; // loaned ministry
                    planet.spotted[owner] = true;
                    for (int j = 0; j < stacks.length; j++) {
                        List<Unit> stack = stacks[j];
                        for (Unit unit : stack) {
                            unit.spotted[owner] = true;
                        }

                    }
                }
            }
        }

    }

//    public void setMoveCosts() {
//
//
//        for (Planet e : planets) {
//
//            PathFind.setMoveCosts(e.planet_grid, terr_cost, e.tile_set_type);
//        }
//    }
    public List<JumpGate> getJumpGates() {
        return jump_gates;
    }

    public Resources getResources() {
        return resources;
    }

    public Economy getEconomy() {
        return economy;
    }

    public void setResearch(int tech) {
        factions[turn].getResearch().setResearch(tech);
    }

    /**
     * Called at the beginning of a turn only. Initializes research points and
     * researches selected tech.
     */
    public void doResearch() {
        factions[turn].getResearch().initResearchPts();
        factions[turn].getResearch().doResearch();
    }

    public void deleteUnit2(Unit u) {
        units.remove(u);
        unmoved_units.remove(u);
    }

    public List<Unit> getCombatStack(String stack) {
        return battle.getCombatStack(stack);
//        List<Unit> rv = null;
//        switch (stack) {
//            case "a":
//                rv = combat_stack_a;
//                break;
//            case "b":
//                rv = combat_stack_b;
//                break;
//            default:
//                throw new AssertionError();
//        }
//        return rv;
    }

    public Battle getBattle() {
        return battle;
    }

    public void resolveGroundBattleInit(String combat_type, int defender_owner) {
        resolveGroundBattleInit(combat_type, defender_owner, CombatStrategyPanel.Strategy.NORMAL);
    }

    public void resolveGroundBattleInit(String combat_type, int defender_owner, CombatStrategyPanel.Strategy strategy) {
        battle.setStrategy(strategy);
        battle.perBattleInit(path, current_planet);
        battle.resolveGroundBattleInit(combat_type, defender_owner);
    }

    public void resolveGroundBattleFight() {
        battle.resolveGroundBattleFight();
    }

    public void resolveGroundBattleFinalize() {
        battle.resolveGroundBattleFinalize();
    }

    public String getCombatType() {
        return battle.getCombatType();
    }

    public boolean isNonCombat(List<Unit> stack) {
        return stack.stream().noneMatch((stack1) -> (stack1.type_data.non_combat == 0));
    }

    public boolean isEnemy() {
        boolean rv = false;

        if (path.size() == 2) {
            List<Unit> stack = path.get(1).getStack();
            if (!stack.isEmpty() && stack.get(0).owner != turn) {
                rv = true;
            }
        }

        return rv;
    }

    public boolean isCombat() {
        boolean rv = false;

        if (path.size() == 2) {
            List<Unit> stack = path.get(1).getStack();
            for (Unit unit : stack) {
                if (unit.owner != turn && unit.type_data.non_combat == 0 && !unit.routed) {
                    rv = true;
                    break;
                }

            }
        }

        return rv;
    }

    public boolean isCapture() {

        boolean captee = false;
        boolean captor = false;

        if (path.size() == 2) {
            List<Unit> stack = path.get(1).getStack();
            if (!stack.isEmpty()) {
                captee = true;
            }
            for (Unit unit : stack) {
                if (unit.owner == turn || (unit.type_data.non_combat == 0 && !unit.routed
                        && unit.type_data.move_type != C.MoveType.SPACE
                        && unit.type_data.move_type != C.MoveType.JUMP)) {
                    captee = false;
                    break;
                }

            }

            List<Unit> stack2 = path.get(0).getStack();
            for (Unit unit : stack2) {
                if (unit.type_data.non_combat == 0 && (unit.type_data.move_type != C.MoveType.AIR
                        && unit.type_data.move_type != C.MoveType.SPACE
                        && unit.type_data.move_type != C.MoveType.JUMP)) {
                    captor = true;
                    break;
                }
            }
        }

        return captor && captee;
    }

    public boolean checkMoveLeftSpace(List<Unit> stack) {
        boolean rv = true;
        for (Unit unit : stack) {
            // if you change this change selectSpaceFighters also
            if (unit.isSelected() && unit.type_data.non_combat == 0
                    && unit.move_points < 1) {
                rv = false;
                break;
            }
        }
        return rv;
    }

    public void capture(Point captor_faction) {
        capture(captor_faction, null);
    }

    public void capture(Point captor_faction, List<Unit> stack) {
        if (stack == null) {
            stack = path.get(1).getStack();
        }
        StackIterator iter = new StackIterator(stack);

        Unit u = iter.next();
        while (u != null) {
            Util.recordFinancialLoss(this, u, captor_faction.x);
            changeOwnerOfUnit(captor_faction, u);
            u.spotted[turn] = true;
            u = iter.next();
        }
    }

    public int getTurn() {
        return turn;
    }

    public Random getRandom() {
        return random;
    }

    public EfsIni getEfs_ini() {
        return efs_ini;
    }

    public List<Planet> getPlanets() {
        return planets;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public List<Structure> getStructures() {
        return structures;
    }

    public List<Unit> getSelectedStack() {
        List<Unit> stack = null;
        Point p = getSelectedPoint();
        if (p == null) {
            return null;
        }
        //int faction = getSelectedFaction().x;
        if (selected_faction.x == -1) {
            stack = getPlanetGrid(getCurrentPlanetNr()).getHex(p.x, p.y).getStack();
        } else {
            Square[][] galaxy_grid = getGalaxyMap().getGalaxyGrid();
            stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[selected_faction.y];
            //System.out.println("stack = " + stack);
        }
        return stack;
    }

//    public void setNextUnmovedUnit() {
//        
//    }
    public void setFactionPlayer(int faction, boolean human_player) {
        human_ctrl[faction] = human_player;
    }

    public boolean humanPlayerPresent() {
        boolean rv = false;

        for (int i = 0; i < human_ctrl.length; i++) {
            if (human_ctrl[i]) {
                rv = true;
                break;
            }

        }

        return rv;
    }

    public boolean[] getHumanControl() {
        return human_ctrl;
    }

    public void doAITurn() {
        if (ai.isAIcontrolled(turn)) {
            ai.doTurn(turn);
        }
    }

    public int getYear() {
        return year;
    }

    public void beginGame() {
        advanceTurn();
        while (!human_ctrl[turn]) {
            advanceTurn();
        }

    }

    public void endTurn() {
        endTurnHousekeeping();
        advanceTurn();
        while (!human_ctrl[turn] || factions[turn].isEliminated() || (year - C.STARTING_YEAR < 1000 && Gui.getMainArgs().hasOption(C.OPT_AI_TEST) && !ai.isMapped(C.SYMBIOT, 17) && !ai.isMapped(C.SYMBIOT, 18) && !ai.isMapped(C.SYMBIOT, 19) && !ai.isMapped(C.SYMBIOT, 20))) {
            endTurnHousekeeping();
            advanceTurn();
        }
    }

    private void endTurnHousekeeping() {
        setFactionCities(); // FIX #52
        for (Structure faction_city : faction_cities) {
            if (faction_city.on_hold_no_res) {
                faction_city.tryToStartBuild(faction_city.build_queue.getFirst(), unit_types, this);
            }
        }
        factions[turn].deleteOldMessages();
        diplomacy.sendContracts();
    }

    public void advanceTurn() {
        if (turn >= 13) {
            advanceYear();
        } else {
            turn++;
        }
        if (turn < C.NR_HOUSES) {
            factions[turn].adjustLoyalty();
        }
        economy.updateEconomy(turn);    //RSW

//        factions[turn].deleteOldMessages(year);
        setFactionCities();
        doResearch();
        buildUnits();

        resetUnmovedUnits(true);
        resetMovePoints();
        setMaxSpotRange();
        cargo_pods = Util.getCargoPods(units, this);
        if (regency.needToVote(turn, efs_ini, year + 1, Regency.VoteCheck.ADVANCE)) { // election notice
            factions[turn].addMessage(new Message("Regent elections will happen next turn.", C.Msg.ELECTION_NOTICE, year, null));
        }
        if (Gui.getMainArgs().hasOption(C.OPT_ENABLE_AI) && !human_ctrl[turn] && ai.isAIcontrolled(turn)) {
            while (!StaticThreads.isStaticDone()) {
                try {
                    System.out.println("Waiting for static AI.");
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            }
            ai.doTurn(turn);
        }
    }

    private void advanceYear() {
        turn = 0;
        year++;
        //Util.dP("     ***** year " + year + " *****");
        Faction.eliminateNoblelessFactions(this);
        int last_house_standing = Faction.checkVictoryByElimination(factions);
        if (last_house_standing > -1) {
            regency.setCrownedEmperor(last_house_standing);
        }
        regency.purgeEliminatedFromOffices(this);
        regency.advanceThroneClaim(false);
        regency.resolveElections(this);
        diplomacy.printState();// DEBUG
    }

    public List<Unit> getCargoPods() {
        return cargo_pods;
    }

    /**
     * Will try to build units. In building city or if it has full stack then
     * will try surrounding hexes, if there are no enemy units or cities or full
     * stack or impassable terrain will build unit there. If it fails to build
     * it will send city full message to owning faction.
     */
    public void buildUnits() {
        for (Structure city : faction_cities) {
            if (city.build_queue.isEmpty()) {
                continue;
            }
            Unit unit = null;
            if (!city.on_hold_no_res) {
                if (city.turns_left == 1) {
                    //System.out.println("Here");
                    int[] u_t = city.build_queue.getFirst();
                    C.MoveType move = unit_types[u_t[0]][u_t[1]].move_type;
                    Hex hex = findRoom(city, move);
                    if (hex != null) {
                        city.buildUnits(unit_types, this, hex);

                    } else {
                        factions[turn].addMessage(new Message(null, C.Msg.CITY_FULL, year, city));
                    }
                } else {
                    city.buildUnits(unit_types, this, null);
                }
            } else {
                city.tryToStartBuild(city.build_queue.getFirst(), unit_types, this);
            }
        }
    }

    /**
     * Tries to find room for one unit in a city or surrounding hexes.
     *
     * @param city
     * @param move move type of unit
     * @return Hex where there is room or null if no room found;
     */
    public Hex findRoom(Structure city, C.MoveType move) {
        boolean found = false;
        Hex hex = planets.get(city.p_idx).planet_grid.getHex(city.x, city.y);
        if (Util.stackSize(hex.getStack()) < C.STACK_SIZE) {
            found = true;
        }

        int tile_set = planets.get(city.p_idx).tile_set_type;
        Hex[] neighbors = hex.getNeighbours();
        test:
        for (int i = 0; i < neighbors.length && !found; i++) {
            hex = neighbors[i];
            if (hex == null) {
                continue;
            }
            Structure city_h = hex.getStructure();
            if (city_h != null) {
                if (city_h.prev_owner != turn) {
                    continue;
                }
            }
            List<Unit> stack = hex.getStack();
            if (!stack.isEmpty()) {
                if (stack.get(0).prev_owner != turn) {
                    continue;
                }
            }
            if (Util.stackSize(stack) < C.STACK_SIZE) {
                if (city_h == null) {
                    boolean[] terrain = hex.getTerrain();
                    for (int j = 0; j < terrain.length; j++) {
                        if (terrain[j] && terr_cost[j][tile_set][move.ordinal()] == 0) {
                            continue test;
                        }
                    }
                }
                found = true;
            }

        }
        if (!found) {
            hex = null;
        }
        return hex;
    }

    public HexProc getHexProc() {
        return hex_proc;
    }

    public Faction getFaction(int faction) {
        return factions[faction];
    }

    public Faction[] getFactions() {
        return factions;
    }

    public void setFactionCities() {
        faction_cities.clear();
        for (Structure s : structures) {
            if (s.owner == turn) {
                faction_cities.add(s);
            }
        }
    }

    public List<Unit> getUnmovedUnits() {
        return unmoved_units;
    }

    /**
     * Resets the list of unmoved units. If start_of_turn then cancels rout.
     *
     * @param start_of_turn the value of start_of_turn
     */
    public void resetUnmovedUnits(boolean start_of_turn) {
        unmoved_units.clear();
        for (Unit u : units) {
            /*
            FIXME units with zero move may have more than 0 mp when this is called during the first turn
            so we need to check type_data also, this should not be so
             */
            if (u.owner == turn && !u.is_sentry && u.move_points > 0 && u.type_data.move_pts > 0) {

                unmoved_units.add(u);
                if (start_of_turn) {
                    u.routed = false;
                }
            }
        }

        Collections.sort(unmoved_units, new Comparator<Unit>() {
            public int compare(Unit o1, Unit o2) {
                int x = o1.x - o2.x;
                if (x != 0) {
                    return x;
                } else {
                    return o1.y - o2.y;
                }

            }
        });

        Collections.sort(unmoved_units, new Comparator<Unit>() {
            public int compare(Unit o1, Unit o2) {
                return o1.p_idx - o2.p_idx;
            }
        });

    }

    public void setJumpRoutes() {
        for (JumpGate jg : jump_gates) {
            Square[][] galaxy_map = galaxy_grid.getGalaxyGrid();
            Planet p1 = galaxy_map[jg.getX1()][jg.getY1()].planet;
            Planet p2 = galaxy_map[jg.getX2()][jg.getY2()].planet;
            if (p1 != null && p2 != null) {
                p1.jump_routes.add(jg);
                p2.jump_routes.add(jg);
                jg.planet_1_index = p1.index;
                jg.planet_2_index = p2.index;
            }

        }
        for (Planet p : planets) {
            p.setNeighbours(planets);
        }
        galaxy_grid.defineJumpRouteTables(planets);
    }

    public void printMoveCost() {
        for (int i = 0; i < terr_cost.length; i++) {
            for (int j = 0; j < terr_cost[i].length; j++) {
                for (int k = 0; k < terr_cost[i][j].length; k++) {
                    System.out.print(" " + terr_cost[i][j][k]);

                }
                System.out.println("");
            }
            System.out.println("");
        }
    }

    public void resetMovePoints() {
        for (Unit e : units) {
            if (e.owner == turn) {
                e.move_points = unit_types[e.type][e.t_lvl].move_pts;
            }
        }
    }

    public void setUnitTypeData() {
        for (Unit e : units) {
            e.type_data = unit_types[e.type][e.t_lvl];
        }

    }

    public void setMoveType() {
        for (Unit e : units) {
            e.move_type = unit_types[e.type][e.t_lvl].move_type;
        }
    }

    public void placeUnits() {

        int y;

        int out_of_map_units = 0;
        Planet planet;
        ListIterator<Unit> iterator = units.listIterator();
//        for (Unit e : units) {
        while (iterator.hasNext()) {
            Unit e = iterator.next();
            planet = planets.get(e.p_idx);
            if (e.in_space) {
                planet.placeUnit(e);
                y = e.y;
            } else { // column numbering is special
                if (e.x % 2 == 0) {
                    y = (e.y - 1) / 2;
                } else {
                    y = e.y / 2;
                }
                if (e.x < 0 || e.x >= C.PLANET_MAP_WIDTH || y < 0 || y >= C.PLANET_MAP_COLUMNS) {

                    System.out.println("Out of map unit number " + out_of_map_units++);
                    iterator.remove();
                    System.out.println("Owner " + e.owner);
                    System.out.println("Planet " + planets.get(e.p_idx).name);
                } else {
//                    System.out.println("(x,y): " + e.x + "," + y);
                    planet.planet_grid.getHex(e.x, y).placeUnit(e);
                    if (e.x % 2 == 0 && y == C.PLANET_MAP_COLUMNS - 1) {
                        System.out.print("Low edge unit: ");
                        System.out.print(" Owner " + e.owner);
                        System.out.print(" Type " + e.type);
                        System.out.print(" Planet " + planets.get(e.p_idx).name);
                        System.out.println(" Hex(x,y): " + e.x + "," + y);
                    }

                }
            }
            e.y = y;
            if (e.in_space) {
                //debug code
//                if (planet.x != e.x || planet.y != e.y) {
//                    System.out.println("Off square space unit");
//                    System.out.print("planet = " + planet);
//                    System.out.println("e.owner = " + e.owner);
//                    System.out.print("planet.x = " + planet.x + "planet.y = " + planet.y);
//                    System.out.println("e.x = " + e.x + "e.y = " + e.y);
//                }

//                Point p = convertSpaceUnit(new Point(e.x, e.y), e.owner);
                e.x = planet.x;
                e.y = planet.y;
            }
        }

    }

    public void placeStructures() {

        int y;

        int out_of_map_units = 0;

        ListIterator<Structure> iterator = structures.listIterator();
//        for (Unit e : units) {
        while (iterator.hasNext()) {
            Structure e = iterator.next();

            if (e.type < 32) {
                if (e.x % 2 == 0) {
                    y = (e.y - 1) / 2;
                } else {
                    y = e.y / 2;
                }
                if (e.x < 0 || e.x >= C.PLANET_MAP_WIDTH || y < 0 || y >= C.PLANET_MAP_COLUMNS) {

                    iterator.remove();
                    out_of_map_units++;
//                    if (e.owner < 5) {
                    System.out.print("Del struct " + out_of_map_units);
                    System.out.print(" Type " + e.type);
                    System.out.print(" Owner " + e.owner);
                    System.out.print(" Planet " + planets.get(e.p_idx).name);
                    System.out.println(" Hex(x,y): " + e.x + "," + y);
//                    }
                } else {
//                    System.out.println("(x,y): " + e.x + "," + y);
                    if (e.type < 26) {
                        planets.get(e.p_idx).planet_grid.getHex(e.x, y).placeStructure(e);
                        switch (e.type) {
                            case C.SHIELD:
                                planets.get(e.p_idx).setShield(e);
                                break;
                            case C.RUINS:
                            case C.ALIEN_RUINS:
                                e.setFlags(Structure.FLAG.RUIN_GUARD, false);
                                break;
                            default:
                                break;
                        }
                    } else {
                        planets.get(e.p_idx).planet_grid.getHex(e.x, y).placeResource(e);
                        iterator.remove(); // FIX #53
                    }
                    if (e.x % 2 == 0 && y == C.PLANET_MAP_COLUMNS - 1) {
                        System.out.print("Low edge structure: ");
                        System.out.print(" Owner " + e.owner);
                        System.out.print(" Type " + e.type);
                        System.out.print(" Planet " + planets.get(e.p_idx).name);
                        System.out.println(" Hex(x,y): " + e.x + "," + y);
                    }
                    e.y = y;
                }
            }
        }
        if (out_of_map_units > 0) {
            System.out.println("Out of map structures: " + out_of_map_units);
        }

    }

    /**
     * Deducts move points from units of a moving planetary stack. If a unit's
     * move is reduced to zero it will be removed from unmoved units.
     *
     * @param selected
     */
    public void subMovePoints(List<Unit> selected) {
        Hex destination = getPath().get(1);
//        int current_planet = getCurrentPlanetNr();
//        PlanetGrid planet_grid = getPlanetGrid(current_planet);
//        Point sel = getSelectedPoint();
//        List<Unit> stack = planet_grid.getHex(sel.x, sel.y).getStack();
//        System.out.println("stack = " + stack);
//        List<Unit> selected = new LinkedList<>();
//
//        for (Unit unit : stack) {
//            if (unit.selected) {
//                selected.add(unit);
//                System.out.println("unit = " + unit);
//            }
//        }
        List<Unit> t_list = new LinkedList<>();
        for (Unit e : selected) {
            int move_cost = destination.getMoveCost(e.move_type.ordinal());
            int max_move = e.type_data.move_pts;
            if (move_cost > max_move) {
                move_cost = max_move;
            }
            e.move_points -= move_cost;
            if (e.move_points == 0) {
                t_list.add(e);
            }
        }
        unmoved_units.removeAll(t_list);
    }
    /**
     * Deducts move points from units of a moving non-jumping space stack. If a
     * unit's move is reduced to zero it will be removed from unmoved units.
     *
     * @param selected
     */
    public void subMovePointsSpace(List<Unit> selected) {
        List<Unit> t_list = new LinkedList<>();
        for (Unit unit : selected) {
            unit.move_points--;
            if (unit.move_points == 0) {
                t_list.add(unit);
            }
        }
        unmoved_units.removeAll(t_list);
    }

    public void unSpot(List<Unit> stack) {
        for (Unit unit : stack) {
            for (int i = 0; i < unit.spotted.length; i++) {
                if (i != unit.owner && i != unit.prev_owner) {
                    unit.spotted[i] = false;
                }
            }
        }
    }

    public void spotSpace(Planet planet, List<Unit> stack, int faction) {
        int spotting_a = 0;
        for (Unit unit : stack) {
            if (unit.type_data.spot > spotting_a) {
                spotting_a = unit.type_data.spot;
            }
        }
        List<Unit>[] stacks = planet.space_stacks;
        for (int i = 0; i < stacks.length; i++) {
            List<Unit> stack_b = stacks[i];
            if (!stack_b.isEmpty()) {
                int spotting_b = 0;
                for (Unit unit : stack_b) {
                    if (unit.type_data.spot > spotting_b) {
                        spotting_b = unit.type_data.spot;
                    }
                }
                spotSpaceStack(stack_b, spotting_a, faction);
                spotSpaceStack(stack, spotting_b, stack_b.get(0).owner);
            }
        }
        planet.spotted[faction] = true;
    }

    public void spotSpaceStack(List<Unit> stack, int spotting, int faction) {
        for (Unit unit : stack) {
            if (spotting >= unit.type_data.camo) {
                unit.spotted[faction] = true;
            }
        }
    }

    public boolean moveStack() {
        boolean rv = false;
        Hex hex = path.getFirst();
        Hex hex2 = path.get(1);
        List<Unit> stack = hex.getStack();
        List<Unit> stack2 = hex2.getStack();
        List<Unit> selected = new LinkedList<>();
        for (Unit unit : stack) {
            if (unit.isSelected()) {
                selected.add(unit);
            }
        }
        Util.unSelectAll(stack2);

        boolean own_units = true;
        if (!stack2.isEmpty() && selected.get(0).owner != stack2.get(0).owner) {
            own_units = false;
        }
        if (Util.stackSize(selected) + Util.stackSize(stack2) <= 20 && own_units) {
            subMovePoints(selected);
            hex2.addStack(selected);
            hex.minusStack(selected);
            setUnitCoords(false, current_planet, hex2.getX(), hex2.getY(), selected);

            path.removeFirst();
            setSelectedPoint(new Point(hex2.getX(), hex2.getY()), -1);
            unSpot(selected);
            hex_proc.spotProc(hex2, selected);
            rv = true;
        }
        return rv;
    }

    /**
     * System to system space travel, that is Jump in EFS terms. Move points set
     * to zero and all units removed from unmoved units.
     *
     * @param p
     * @return
     */
    public boolean moveSpaceStack(Point p) {
        boolean rv = false;
        Square[][] galaxy_map = getGalaxyMap().getGalaxyGrid();
        int x1 = selected_point.x;
        int y1 = selected_point.y;
        Planet source = galaxy_map[x1][y1].parent_planet;
        Planet destination = galaxy_map[p.x][p.y].planet;
        List<Unit> stack = source.space_stacks[selected_faction.y];
        List<Unit> stack2 = destination.space_stacks[selected_faction.y];
        List<Unit> selected = new LinkedList<>();
        for (Unit unit : stack) {
            if (unit.isSelected()) {
                selected.add(unit);
            }
        }
        Util.unSelectAll(stack2);

        if (Util.stackSize(selected) + Util.stackSize(stack2) <= 20) {
            for (Unit unit : selected) {
                unit.move_points = 0;
            }
            destination.addStack(selected, selected_faction.y);
            source.minusStack(selected, selected_faction.y);
            unmoved_units.removeAll(selected);
            unSpot(selected);
            spotSpace(destination, selected, selected_faction.x);
            setUnitCoords(true, destination.index, p.x, p.y, selected);
            rv = true;
        }

        return rv;
    }

    /**
     * Launch units to space from Planet hex. Note that we can't use
     * Game.selected_faction here since since a planetary stack is selected.
     *
     * @return
     */
    public boolean launchStack() {
        boolean rv = false;
        Point q = getSelectedPoint();
        PlanetGrid planet_grid = getPlanetGrid(getCurrentPlanetNr());
        Hex target_hex = planet_grid.getHex(q.x, q.y);
        List<Unit> stack = target_hex.getStack();
        List<Unit> selected = new LinkedList<>();
        for (Unit unit : stack) {
            if (unit.isSelected()) {
                selected.add(unit);
            }
        }

        Point selected_faction = new Point(selected.get(0).owner, selected.get(0).prev_owner);
//        System.out.println("faction = " + faction);
        Planet planet = planets.get(getCurrentPlanetNr());
        List<Unit> target_stack = planet.space_stacks[selected_faction.y];
        Util.unSelectAll(target_stack);

        if (Util.stackSize(selected) + Util.stackSize(target_stack) <= 20) {
            subMovePointsSpace(selected);
            planet.addStack(selected, selected_faction.y);
            target_hex.minusStack(selected);
            unSpot(Util.xS(selected));
            spotSpace(planet, selected, selected_faction.x);
            setUnitCoords(true, planet.index, planet.x, planet.y, selected);
            // which code-monkey did this ?
            setSelectedPointFaction(new Point(planet.x, planet.y), selected_faction.y, null, null);
            setSelectedPoint(new Point(planet.x, planet.y), selected_faction.y);
            setSelectedFaction(selected_faction.x, selected_faction.y);
//            System.out.println("selected_point = " + selected_point);
//            System.out.println("faction = " + faction);
            rv = true;
        }
        return rv;
    }

    /**
     * Land a space stack, that is merge a space stack with a land stack.
     *
     * @param p
     * @return
     */
    public boolean landStack(Point p) {
        boolean rv = false;
        Square[][] galaxy_map = getGalaxyMap().getGalaxyGrid();
        int x1 = selected_point.x;
        int y1 = selected_point.y;
        Planet planet = galaxy_map[x1][y1].parent_planet;
        List<Unit> stack = planet.space_stacks[selected_faction.y];

        Hex target_hex = planet.planet_grid.getHex(p.x, p.y);
        List<Unit> stack2 = target_hex.getStack();
        List<Unit> selected = new LinkedList<>();
        for (Unit unit : stack) {
            if (unit.isSelected()) {
                selected.add(unit);
            }
        }
        Util.unSelectAll(stack2);

        if (Util.stackSize(selected) + Util.stackSize(stack2) <= 20) {
            subMovePointsSpace(selected);
            if (!stack2.isEmpty() && stack2.get(0).owner != selected.get(0).owner) {
                capture(selected_faction, stack2);
            }
            target_hex.addStack(selected);
            planet.minusStack(selected, selected_faction.y);
            setUnitCoords(false, planet.index, p.x, p.y, selected);
            setSelectedPointFaction(p, -1, null, null);
            unSpot(Util.xS(selected));
            hex_proc.spotProc(target_hex, selected);
            Structure city = target_hex.getStructure();
            if (city != null && city.owner != selected.get(0).owner) {
                captureCity(city, selected.get(0).owner, selected.get(0).prev_owner);
            }
            rv = true;
        }
        return rv;
    }

    public void setUnitCoords(boolean in_space, int p_idx, int x, int y, Unit e) {

        relocateUnit(in_space, p_idx, x, y, e);

        for (Unit u : e.cargo_list) {
            relocateUnit(in_space, p_idx, x, y, u);
        }

    }

    public void setUnitCoords(boolean in_space, int p_idx, int x, int y, List<Unit> selected) {
        boolean is_cargo_listing = false;
        Iterator<Unit> iterator = selected.listIterator();
        Iterator<Unit> cargo_it = null;

        Unit e = iterator.next();
        for (int i = 0; i < C.STACK_SIZE; i++) {

            relocateUnit(in_space, p_idx, x, y, e);

            if (is_cargo_listing) {
                e = cargo_it.next();
                if (!cargo_it.hasNext()) {
                    cargo_it = null;
                    is_cargo_listing = false;
                }
            } else if (e.cargo_list.isEmpty()) {
                if (iterator.hasNext()) {
                    e = iterator.next();
                } else {
                    return;
                }
            } else {
                cargo_it = e.cargo_list.listIterator();
                e = cargo_it.next();
                if (cargo_it.hasNext()) {
                    is_cargo_listing = true;
                }
            }

        }
    }

    /**
     * Note for space stacks the selected point is complicated, it is one of the
     * squares around the parent planet. So when setting the selected point
     * using the planets coordinates one has to use setSelectedPoint(p, faction)
     * and setSelectedFaction(faction).
     *
     * @param p the value of p
     * @param faction the value of faction
     * @param path the value of path
     */
    public void setSelectedPointFaction(Point p, int faction, LinkedList<Hex> path, JumpGate jump_path) {
        setSelectedPoint(p, -1);
        setSelectedFaction(faction);
        setPath(path);
        setJumpPath(jump_path);
    }

    public void setJumpPath(JumpGate jg) {
        jump_path = jg;
    }

    public JumpGate getJumpPath() {
        return jump_path;
    }

    /**
     * For a space stack, converts Point coordinates from planet coordinates to
     * faction square coordinates around planet.
     *
     * @param p
     * @param faction
     * @return
     */
    public Point resolveSpaceStack(Point p, int faction) {
        Point q = new Point(p.x, p.y);
        if (faction > -1) {

            switch (faction) {

                case C.HOUSE1:
                    q.x += -1;
                    q.y += -1;
                    break;
                case C.HOUSE2:
                    q.x += 0;
                    q.y += -1;
                    break;
                case C.HOUSE3:
                    q.x += 1;
                    q.y += -1;
                    break;
                case C.HOUSE4:
                    q.x += -1;
                    q.y += 0;
                    break;
                case C.HOUSE5:
                    q.x += 1;
                    q.y += 0;
                    break;
                case C.LEAGUE:
                case C.THE_CHURCH:
                case C.SYMBIOT:
                    q.x += -1;
                    q.y += 1;
                    break;
                case C.VAU:
                case C.IMPERIAL:
                case C.FLEET:
                    q.x += 0;
                    q.y += 1;
                    break;
                case C.STIGMATA:
                case C.THE_SPY:
                case C.NEUTRAL:
                    q.x += 1;
                    q.y += 1;
                    break;
                default:
                    throw new AssertionError();
            }

        }
        return q;
    }

    public Point convertSpaceUnit(Point p, int faction) {
        if (faction > -1) {

            switch (faction) {

                case C.HOUSE1:
                    p.x += +1;
                    p.y += +1;
                    break;
                case C.HOUSE2:
                    p.x += 0;
                    p.y += +1;
                    break;
                case C.HOUSE3:
                    p.x += -1;
                    p.y += +1;
                    break;
                case C.HOUSE4:
                    p.x += +1;
                    p.y += 0;
                    break;
                case C.HOUSE5:
                    p.x += -1;
                    p.y += 0;
                    break;
                case C.LEAGUE:
                case C.THE_CHURCH:
                case C.SYMBIOT:
                    p.x += +1;
                    p.y += -1;
                    break;
                case C.VAU:
                case C.IMPERIAL:
                case C.FLEET:
                    p.x += 0;
                    p.y += -1;
                    break;
                case C.STIGMATA:
                case C.THE_SPY:
                case C.NEUTRAL:
                    p.x += -1;
                    p.y += -1;
                    break;
                default:
                    throw new AssertionError();
            }

        }
        return p;
    }

    /**
     * Set the selected point. For space the faction of the selected units must
     * be given also to determine the proper square next to the planet.
     *
     * @param p the selected point
     * @param faction for space, the faction of the selected units.
     */
    public void setSelectedPoint(Point p, int faction) {
        if (faction > -1) {

            switch (faction) {

                case C.HOUSE1:
                    p.x += -1;
                    p.y += -1;
                    break;
                case C.HOUSE2:
                    p.x += 0;
                    p.y += -1;
                    break;
                case C.HOUSE3:
                    p.x += 1;
                    p.y += -1;
                    break;
                case C.HOUSE4:
                    p.x += -1;
                    p.y += 0;
                    break;
                case C.HOUSE5:
                    p.x += 1;
                    p.y += 0;
                    break;
                case C.LEAGUE:
                case C.THE_CHURCH:
                case C.SYMBIOT:
                    p.x += -1;
                    p.y += 1;
                    break;
                case C.VAU:
                case C.IMPERIAL:
                case C.FLEET:
                    p.x += 0;
                    p.y += 1;
                    break;
                case C.STIGMATA:
                case C.THE_SPY:
                case C.NEUTRAL:
                    p.x += 1;
                    p.y += 1;
                    break;
                default:
                    throw new AssertionError();
            }

        }
        selected_point = p;
    }

    public Point getSelectedPoint() {
        return selected_point;
    }

    public StrBuild getStrBuild(int index) {
        return str_build[index];
    }

    public StrBuild[] getStrBuild() {
        return str_build;
    }

    public void setMapOrigin(Point p) {
        planet_map_origin = p;

    }

    public Galaxy getGalaxy() {
        return galaxy;
    }

    public GalaxyGrid getGalaxyMap() {
        return galaxy_grid;
    }

    public Point getMapOrigin() {
        return planet_map_origin;
    }

    public Point getSpaceMapOrigin() {
        return space_map_origin;

    }

    public void setSpaceMapOrigin(Point p) {
        space_map_origin = p;
    }

    public int[][] getCurrentMap() {

        return galaxy.getPlanetMap(current_planet);
    }

    public void setCurrentPlanetNr(int current_planet) {
        this.current_planet = current_planet;
    }

    public int getCurrentPlanetNr() {
        return current_planet;
    }

    public Planet getPlanet(int index) {
        return planets.get(index);
    }

    public PlanetGrid getPlanetGrid(int index) {
        return planets.get(index).planet_grid;
    }

    public static void main(String[] args) {
        PlanetGrid map = new PlanetGrid();

        if (map.test()) {
            System.out.println("Map ok.");
        } else {
            System.out.println("Map not ok.");
        }

    }

    /**
     * @return the selected_faction
     */
    public Point getSelectedFaction() {
        return selected_faction;
    }

    /**
     * @param selected_faction the selected_faction to set
     */
    public void setSelectedFaction(int selected_faction) {
        setSelectedFaction(selected_faction, selected_faction);
    }

    /**
     * @param selected_faction the selected_faction to set
     * @param prev_owner the owner of the stack
     */
    public void setSelectedFaction(int selected_faction, int prev_owner) {
        this.selected_faction.x = selected_faction;
        this.selected_faction.y = prev_owner;
    }

    /**
     * @return the path
     */
    public LinkedList<Hex> getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(LinkedList<Hex> path) {
        this.path = path;
    }

    /**
     * @return the terr_cost
     */
    public double[][][] getTerrCost() {
        return terr_cost;
    }

    /**
     * @param terr_cost the terr_cost to set
     */
    public void setTerrCost(double[][][] terr_cost) {
        this.terr_cost = terr_cost;
    }

    /**
     * @return the resource types
     */
    public ResType[] getResTypes() {
        return res_types;
    }

    /**
     * @return the unit_types
     */
    public UnitType[][] getUnitTypes() {
        return unit_types;
    }

    /**
     * @param unit_types the unit_types to set
     */
    public void setUnitTypes(UnitType[][] unit_types) {
        this.unit_types = unit_types;
    }

    public void getAllResources() {
        HexIter iter = Util.getHexIter(this, getCurrentPlanetNr());
        Hex hex = iter.next();
        while (hex != null) {
            if (!hex.getTerrain(C.OCEAN) && hex.getStack().isEmpty() && hex.getStructure() == null) {
                for (int i = 0; i < C.RES_TYPES; i++) {
                    createUnitInHex(getCurrentPlanetNr(), hex.getX(), hex.getY(), getTurn(), getTurn(), C.CARGO_UNIT_TYPE, 0, i, 999);
                }
                break;
            }
            hex = iter.next();
        }
    }

    public void setMaxSpotRange() {
        int spotting = 0;
        for (Unit unit : units) {
            if (spotting < unit.type_data.spot) {
                spotting = unit.type_data.spot;
            }
        }
        hex_proc.setMaxSpotRange(Unit.spotRange(spotting));

    }

    public GameResources getGameResources() {
        return game_resources;
    }

//    public List<Structure> getStructures() {
//        return structures;
//    }
//    public int getMaxSpotRange() {
//        return max_spot_range;
//    }
    /**
     * Capture (or change ownership of) city. City ownership change must be done
     * thru this method to update production and consumption data.
     *
     * @param city
     * @param new_owner
     */
    public void captureCity(Structure city, int new_owner, int new_prev_owner) {
        //subtract prod_cons for old owner
        economy.updateProdConsForCity(city, false);
        getFaction(city.owner).addMessage(new Message("City lost to " + Util.getFactionName(new_owner) + "!",
                C.Msg.CITY_LOST, getYear(), city));
        city.owner = new_owner;
        city.prev_owner = new_prev_owner;
        //add prod_cons for new owner
        economy.updateProdConsForCity(city, true);

    }

    /**
     * Creates a city. New cities must be created thru this method to update
     * production and consumption data.
     *
     * @param owner
     * @param p_idx
     * @param x
     * @param y
     * @return
     */
    public Structure createCity(int owner, int prev_owner, int p_idx, int x, int y, int type, int health) {
        Structure city = new Structure(owner, prev_owner, type, p_idx, x, y, health);
        city.loyalty = Faction.calculateCityLoyalty(factions[turn].getTaxRate(), efs_ini, this);
        Hex hex = getHexFromPXY(p_idx, x, y);
        hex.placeStructure(city);
        structures.add(city);
        // update production/consumption data
        economy.updateProdConsForCity(city, true);
        if (city.type == C.SHIELD) {
            planets.get(p_idx).setShield(city);
        }
        return city;
    }

    /**
     * Destroys a city. Cities must be removed thru this method to update
     * production and consumption data.
     *
     * @param p_idx
     * @param x
     * @param y
     */
    public void destroyCity(int p_idx, int x, int y) {
        Hex hex = getHexFromPXY(p_idx, x, y);
        Structure city = hex.getStructure();
        hex.placeStructure(null);
        if (city.type == C.SHIELD) {
            planets.get(p_idx).setShield(null);
        }
        structures.remove(city);
        // update production/consumption data
        economy.updateProdConsForCity(city, false);
    }

    /**
     * Adjust city health to new health. City health must be changed thru this
     * method to update production and consumption data.
     *
     * @param city
     * @param new_health
     */
    public void adjustCityHealth(Structure city, int new_health) {
        //subtract prod_cons for old health
        economy.updateProdConsForCity(city, false);
        city.health = new_health;
        //add prod_cons for new health
        economy.updateProdConsForCity(city, true);
    }

    /**
     * Adjust city loyalty to new loyalty. City loyalty must be changed thru
     * this method to update production and consumption data.
     *
     * @param city
     * @param new_loyalty
     */
    public void adjustCityLoyalty(Structure city, int new_loyalty) {
        //subtract subtract prod_cons for old loyalty
        economy.updateProdConsForCity(city, false);
        city.loyalty = new_loyalty;
        //add prod_cons for new loyalty
        economy.updateProdConsForCity(city, true);
    }

    /**
     * Make and place fresh unit from scratch, not from Galaxy file. Places unit
     * on a planet at specified coordinates. Units cannot be created in space
     * during game. If hex is already full, does nothing and returns null.
     *
     * @param p_idx, x, y Planet index and hex coordinates of location
     * @param owner Owning faction
     * @param type Type number (position in UNIT.DAT, 0-91)
     * @param t_lvl Subtype (subordinate position in UNIT.DAT)
     * @param res_relic Resource or relic type (cargo pods and relics only, set
     * to 0 for other units)
     * @param amount Quantity of resources (cargo pods only, set to 0 for other
     * units)
     * @return New unit.
     *
     */
    public Unit createUnitInHex(int p_idx, int x, int y, int owner, int prev_owner, int type, int t_lvl, int res_relic, int amount) {    //RSW

        Hex hex = getHexFromPXY(p_idx, x, y);
        List<Unit> stack = hex.getStack();

        if (Util.stackSize(stack) < 20) {    // If stack not full, create new unit
            Unit unit = new Unit(p_idx, x, y, owner, prev_owner, type, t_lvl, res_relic, amount, this);

            units.add(unit);    // Add new unit to the general unit list
            hex.placeUnit(unit);    // Add new unit to the stack
            unmoved_units.add(unit);    // Add new unit to the unmoved units list

//            System.out.println("spotted[] before: " + Arrays.toString(unit.spotted));    //DEBUG
            hex_proc.spotProc(hex, stack);    // Set spotted[] flags for new unit

//            System.out.println("spotted[] after: " + Arrays.toString(unit.spotted));    //DEBUG
            if (unit.type == C.CARGO_UNIT_TYPE) {
                resources.addToPodLists(unit);    // Locations of cargo pods are tracked by class Resources
            }
            if (unit.type_data.eat) {
                resources.addToProdCons(C.CONS, owner, p_idx, C.RES_FOOD, 1);
            }

//            System.out.println("*** createUnitInHex: created " + unit.type_data.name + " at " + p_idx + ", "
//                    + x + ", " + y + ", " + unit.in_space);    // TESTING
            return unit;

        } else {
            return null;
        }
    }

    /**
     * Removes from the game a unit killed in combat (in space or hex). Performs
     * all necessary clean-up.
     *
     * IMPORTANT: Do not call this method while iterating over the general unit
     * list, or any stack or cargo list the unit is on!
     *
     * @param unit Unit to be deleted.
     */
    public void deleteUnitInCombat(Unit unit) {

        // Note: When disembarking units from a list, put them on a temporary list first,
        // to avoid the problem of removing an object from a list while iterating through that list
        List<Unit> temp_list = new LinkedList<>();

        // Disembark any space units on carriers in space
        if (unit.in_space && unit.type == C.SPACE_CARRIER_UNIT_TYPE) {
            for (Unit cargo : unit.cargo_list) {
                if (cargo.type == C.FIGHTER_UNIT_TYPE || cargo.type == C.TORP_BMBR_UNIT_TYPE) {
                    temp_list.add(cargo);
                }
            }
        }
        for (Unit cargo : temp_list) {
            unit.disembark(cargo);
            getUnitStack(unit).add(cargo);
        }

        // Now delete the unit along with any remaining cargo
        deleteUnitAndCargo(unit);
    }

    /**
     * Removes from the game a unit NOT killed in combat. Performs all necessary
     * clean-up.
     *
     * IMPORTANT: Do not call this method while iterating over the general unit
     * list, or any stack or cargo list the unit is on!
     *
     * @param unit Unit to be deleted.
     */
    public void deleteUnitNotInCombat(Unit unit) {

        // Note: When disembarking units from a list, put them on a temporary list first,
        // to avoid the problem of removing an object from a list while iterating through that list
        List<Unit> temp_list = new LinkedList<>();

        // Disembark any cargo which can be disembarked
        for (Unit cargo : unit.cargo_list) {
            if (unit.in_space) {
                if (cargo.move_type == C.MoveType.SPACE
                        || cargo.move_type == C.MoveType.JUMP
                        || cargo.move_type == C.MoveType.LANDER) {
                    temp_list.add(cargo);
                }
            } else {
                boolean[] terrain = getUnitHex(unit).getTerrain();
                int tile_set = getPlanet(unit.p_idx).tile_set_type;
                if (terrain[C.OCEAN] == false || tile_set == C.BARREN_TILE_SET) {
                    temp_list.add(cargo);
                }
            }
        }
        for (Unit cargo : temp_list) {
            unit.disembark(cargo);
            getUnitStack(unit).add(cargo);
        }

        // Now delete the unit along with any remaining cargo
        deleteUnitAndCargo(unit);
    }

    /**
     * Removes a unit from the game along with all its cargo. Not to be called
     * by any method other than deleteUnitInCombat and deleteUnitNotInCombat.
     *
     * @param unit Unit to be deleted.
     */
    private void deleteUnitAndCargo(Unit unit) {

        // Note: When deleting units from a list, put them on a temporary list first,
        // to avoid the problem of removing an object from a list while iterating through that list
        List<Unit> temp_list = new LinkedList<>();

        // First delete all cargo
        for (Unit cargo : unit.cargo_list) {
            temp_list.add(cargo);
        }
        for (Unit cargo : temp_list) {
            deleteEmptyUnit(cargo);
        }

        // Now delete the unit itself
        deleteEmptyUnit(unit);
    }

    /**
     * Removes a unit from the game, when it definitely has no cargo. Not to be
     * called by any method other than deleteUnitAndCargo.
     *
     * @param unit Unit to be deleted.
     */
    private void deleteEmptyUnit(Unit unit) {

        List<Unit> stack = getUnitStack(unit);    // Get unit's stack

        stack.remove(unit);         // Remove unit from its stack
        unmoved_units.remove(unit); // Remove unit from the unmoved unit list
        units.remove(unit);         // Remove unit from the general unit list

        if (unit.carrier != null) {
            unit.carrier.disembark(unit);    // Remove this unit from its carrier's cargo list
        }

        if (unit.type == C.CARGO_UNIT_TYPE) {
            resources.removeFromPodLists(unit);    // Locations of cargo pods are tracked by class Resources
        }

        // Remove planetbound eaters from list of eaters
        if (unit.type_data.eat && !unit.in_space) {
            resources.addToProdCons(C.CONS, unit.owner, unit.p_idx, C.RES_FOOD, -1);
        }
//        System.out.println("*** deleteEmptyUnit: deleted " + unit.type_data.name + " at " + unit.p_idx + ", "
//                + unit.x + ", " + unit.y + ", " + unit.in_space);    // TESTING
        // If there are any units spotted only by this one, we should clear their spotted flags. ???
        if (unit.type == C.NOBLE_UNIT_TYPE && unit.owner < C.NR_HOUSES) {
            for (int i = 0; i < C.NR_HOUSES; i++) {
                factions[i].addMessage(new Message("A noble from House " + Util.getFactionName(unit.owner)
                        + " has been killed!", C.Msg.NOBLE_KILLED, year, Util.getFactionName(unit.owner)));
            }
        }
    }

    /**
     * Relocates a unit to the specified location. All relocation of units must
     * be done through this method, to ensure the updating of secondary data
     * structures that are dependent on the location of units.
     *
     * @param in_space True if the unit is to be in space
     * @param p_idx, x, y Planet index and hex coordinates of new location
     * @param unit Unit to be relocated
     */
    public void relocateUnit(boolean in_space, int p_idx, int x, int y, Unit unit) {    //RSW

        if (unit.type == C.CARGO_UNIT_TYPE) {
            resources.removeFromPodLists(unit);    // Locations of cargo pods are tracked by class Resources
        }
        // units in_space do not eat
        if (unit.type_data.eat && in_space != unit.in_space) {
            if (in_space) {
                resources.addToProdCons(C.CONS, unit.owner, p_idx, C.RES_FOOD, -1);
            } else {
                resources.addToProdCons(C.CONS, unit.owner, p_idx, C.RES_FOOD, 1);
            }
        }
        unit.in_space = in_space;
        unit.p_idx = p_idx;
        unit.x = x;
        unit.y = y;

        if (unit.type == C.CARGO_UNIT_TYPE) {
            resources.addToPodLists(unit);    // Locations of cargo pods are tracked by class Resources
        }

//        System.out.println("*** relocateUnit: " + unit.type_data.name + " relocated to " + p_idx + ", "
//                + x + ", " + y + ", " + in_space);    // TESTING
    }

    /**
     * Changes the ownership of a unit from one faction to another. All changes
     * of ownership must be done through this method, to ensure the updating of
     * secondary data structures that are dependent on the location of units.
     *
     * @param new_owner
     * @param unit
     */
    public void changeOwnerOfUnit(Point new_owner, Unit unit) {    //RSW

        if (unit.type == C.CARGO_UNIT_TYPE) {
            resources.removeFromPodLists(unit);    // Ownership of cargo pods is tracked by class Resources
        }

        // update food consumption data
        if (unit.type_data.eat && !unit.in_space) {
            resources.addToProdCons(C.CONS, unit.owner, unit.p_idx, C.RES_FOOD, -1);
            resources.addToProdCons(C.CONS, new_owner.x, unit.p_idx, C.RES_FOOD, 1);

        }

        unit.owner = new_owner.x;
        unit.prev_owner = new_owner.y;
        unit.spotted[new_owner.x] = true;
        if (unit.type == C.CARGO_UNIT_TYPE) {
            resources.addToPodLists(unit);    // Ownership of cargo pods is tracked by class Resources
        }

//        System.out.println("*** changeOwnerOfUnit: " + unit.type_data.name
//                + " now owned by faction " + new_owner); // TESTING
    }

    /**
     * Returns the hex that a unit's in. IMPORTANT. Be sure unit is in a hex
     * before calling. If necessary, check unit.in_space
     *
     */
    public Hex getUnitHex(Unit unit) {

        Planet planet = planets.get(unit.p_idx);
        Hex hex = planet.planet_grid.getHex(unit.x, unit.y);

        return hex;
    }

    /**
     * Returns the stack that a unit's in (whether in space or in a hex)
     *
     */
    public List<Unit> getUnitStack(Unit unit) {

        List<Unit> stack;

        if (unit.in_space) {
            Planet planet = planets.get(unit.p_idx);
            stack = planet.space_stacks[unit.prev_owner];
        } else {
            Hex hex = getUnitHex(unit);
            stack = hex.getStack();
        }
        return stack;
    }

    /**
     * Returns the hex from a planet index and coordinates
     *
     */
    public Hex getHexFromPXY(int p_idx, int x, int y) {

        Planet planet = planets.get(p_idx);
        Hex hex = planet.planet_grid.getHex(x, y);

        return hex;
    }

    /**
     * Returns whether unit is in a city
     *
     */
    public boolean unitInCity(Unit unit) {

        if (unit.in_space) {
            return false;
        } else {
            Hex hex = getUnitHex(unit);
            if (hex.getStructure() == null) {
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * Game state printout method, prints identifying statistics of Game,
     * Factions, Techs, Planets, Fleets, Cities, Stacks, Units.
     *
     * @param file_name
     */
    public void record(PrintWriter pw) {
        pw.println( "Year," + year + ",Turn," + turn);
        regency.record(pw);
        diplomacy.record(pw);
        pw.println( " #FACTIONS");
        for (Faction faction : factions) {
            faction.record(pw);
        }
        pw.println( " #PLANETS");
        for (Planet planet : planets) {
            planet.record(pw);
        }
    }

    public Regency getRegency() {
        return regency;
    }

    /**
     * Called when bombarding or landing, delegates to Battle instance.
     *
     * @param h
     * @param queue_pts
     */
    public void startBombardOrPTS(Hex h, boolean queue_pts, int target_faction) {
        battle.startBombardOrPTS(h, queue_pts, target_faction);

    }

    /**
     * @return the diplomacy
     */
    public Diplomacy getDiplomacy() {
        return diplomacy;
    }
    
    /**
     * Return true iff Hex hex on Planet p_idx is under shield effect.
     *
     * @param hex
     * @param p_idx
     * @return
     */
    public boolean isShielded(Hex hex, int p_idx) {
        Structure shield = planets.get(p_idx).getShield();
        if (shield != null && getEfs_ini().shield_radius > -1) {
            Set<Hex> shielded_hexes = Util.getHexesWithinRadiusOf(getHexFromPXY(shield.p_idx, shield.x, shield.y), getEfs_ini().shield_radius, null);
            if (shielded_hexes.contains(hex)) {
                return true;
            }
        }
        return false;
    }

    public void omniscience() {
        for (Planet planet : planets) {
            planet.omniscience(getTurn());
        }
    }

    /**
     * @return the initial_seed
     */
    public long getInitialSeed() {
        return initial_seed;
    }

    /**
     * @param initial_seed the initial_seed to set
     */
    public void setInitialSeed(long initial_seed) {
        this.initial_seed = initial_seed;
    }
}
