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
import game.GalaxyGrid;
import game.Game;
import game.Hex;
import game.PlanetGrid;
import java.awt.Point;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
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
    Game game;
    List<Planet> planets;   
    List<Unit> all_units;
    List<Unit> units;
    List<Unit> units_land_battle;
    List<Unit> units_recon;

    List<Unit> enemy_units;
    List<Structure> all_structures;    
    List<Structure> structures;
    List<Structure> enemy_structures;
    GalaxyGrid galaxy_grid;

    public AI(Game game) {
        logger.debug("Hey");
        Util.dP("##### AI init begin");
        this.game = game;
        all_units = game.getUnits();
        planets = game.getPlanets();
        all_structures = game.getStructures();
        galaxy_grid = game.getGalaxyMap();
        units = new LinkedList<>();
        units_land_battle = new LinkedList<>();
        units_recon = new LinkedList<>();
        structures = new LinkedList<>();
        enemy_units = new LinkedList<>();
        enemy_structures = new LinkedList<>();

        Util.dP("##### AI init end");
    }

    public abstract void doTurn();

    protected void logSuper(int faction) {
        logger.debug(Util.getFactionName(faction));
    }

    protected void findAssets(int faction) {
        logger.debug("Ai.findAssets()");
        long time = System.nanoTime();
        units.clear();
        enemy_units.clear();
        for (Unit unit : all_units) {
            if (unit.owner == faction) {
                units.add(unit);
                if (unit.type_data.non_combat == 0 && (unit.move_type == C.MoveType.FOOT
                        || unit.move_type == C.MoveType.WHEEL
                        || unit.move_type == C.MoveType.TREAD
                        || unit.move_type == C.MoveType.HOVER)) {
                    units_land_battle.add(unit);
                    unit.selected = true;
                } else if (unit.move_type == C.MoveType.AIR) {
                    units_recon.add(unit);
                }
            } else if (unit.spotted[faction]) {
                enemy_units.add(unit);
            }
        }
        units.sort(Comp.unit_xy);
        units.sort(Comp.unit_cidx);
        units.sort(Comp.unit_pidx);
        enemy_units.sort(Comp.unit_xy);
        enemy_units.sort(Comp.unit_cidx);
        enemy_units.sort(Comp.unit_pidx);
        structures.clear();
        enemy_structures.clear();
        for (Structure s : all_structures) {
            if (s.type == C.MONASTERY || s.type == C.ALIEN_RUINS || s.type == C.RUINS) {
                continue;
            }
            if (s.owner == faction) {
                structures.add(s);
            } else {
                Hex hex = game.getHexFromPXY(s.p_idx, s.x, s.y);
                if (hex.isSpotted(faction)) {
                    enemy_structures.add(s);
                }
            }
        }
        structures.sort(Comp.city_xy);
        structures.sort(Comp.city_cidx);
        structures.sort(Comp.city_pidx);
        enemy_structures.sort(Comp.city_xy);
        enemy_structures.sort(Comp.city_cidx);
        enemy_structures.sort(Comp.city_pidx);
        
        logger.debug("Ai.findAssets() end " + (System.nanoTime() - time) / 1_000_000d + "ms");
    }

    /**
     * Top level conquer method, groups units per continent.
     *
     * @param faction
     */
    protected void conquerAllUnits(int faction) {
        LinkedHashSet<LinkedList<Unit>> current_continent = new LinkedHashSet<>(C.PLANET_MAP_WIDTH * C.PLANET_MAP_COLUMNS);
        LinkedList<Unit> current_units = new LinkedList<>();
        current_continent.add(current_units);
        Hex[][] map = null;
        int p_idx = -1;
        int c_idx = Integer.MIN_VALUE;
        for (Unit unit : units_land_battle) {
            if (p_idx != unit.p_idx) {
                if (p_idx != -1) {
                    logger.debug(" call Ai.conquerContinent()");
                    conquerContinent(p_idx, c_idx, current_continent, map);
                }
                p_idx = unit.p_idx;
                map = game.getPlanetGrid(p_idx).getMapArray();
                c_idx = map[unit.x][unit.y].getLandNr();
                current_continent.clear();
                current_units = new LinkedList<>();
                current_continent.add(current_units);
            } else if (c_idx != map[unit.x][unit.y].getLandNr()) {
                conquerContinent(p_idx, c_idx, current_continent, map);
                c_idx = map[unit.x][unit.y].getLandNr();
                current_continent.clear();
                current_units = new LinkedList<>();
                current_continent.add(current_units);
            }
            current_units.add(unit);
        }
        if (p_idx != -1) {
            conquerContinent(p_idx, c_idx, current_continent, map);
        }
    }

    /**
     * Mid level conquer method, handles conquests on one continent.
     * @param p_idx
     * @param c_idx
     * @param stacks
     * @param map
     */
    protected void conquerContinent(int p_idx, int c_idx, LinkedHashSet<LinkedList<Unit>> stacks, Hex[][] map) {
        logger.debug("Ai.conquerContinent() " + p_idx + "," + c_idx);
        game.setSelectedFaction(-1, -1);
        long time = System.nanoTime();
        PlanetGrid pg = game.getPlanetGrid(p_idx);
        LinkedHashSet<Hex> targets = getTargets(p_idx, c_idx, map);
        while (!targets.isEmpty() && !stacks.isEmpty()) {
            Hex h = null;
            LinkedList<Unit> s = null;
            int dist = Integer.MAX_VALUE;
            for (Hex target : targets) {
                for (LinkedList<Unit> stack : stacks) {
                    int tmp = pg.getIntraContHexDist(target, map[stack.getFirst().x][stack.getFirst().y]);                  
                    if (tmp == Byte.MIN_VALUE) { // for pathological continents
                        tmp = Integer.MAX_VALUE;
                    }
                    if (tmp < dist) {
                        h = target;
                        s = stack;
                        dist = tmp;
                    }
                    logger.debug("     dist " + dist + " stack " + stack.getFirst().x + "," + stack.getFirst().y + " target " + target.getX() + "," + target.getY());
                }
            }
            targets.remove(h);
            stacks.remove(s);

            LinkedList<Hex> path = PathFind.findPath(game, pg, h, map[s.getFirst().x][s.getFirst().y]);
            game.setPath(path);
            game.setCurrentPlanetNr(p_idx);
            game.setSelectedPoint(new Point(s.getFirst().x, s.getFirst().y), -1);
            while (game.getPath().size() > 1 && Util.moveCapable(game) && moveToNextHex(path, s)) {
                game.setSelectedPoint(new Point(s.getFirst().x, s.getFirst().y), -1);
                logger.debug("     path " + game.getPath().size());
            }

        }
        logger.debug("Ai.conquerContinent() end " + (System.nanoTime() - time) / 1_000_000d + "ms");
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
//        if (!Util.anyCombat(moving_stack)) {
////            logger.debug("PW4 non-combat stack");
//            boolean agora_sale = false;
//            if (city != null && city.owner != faction.x) {
//                if (city.type == C.AGORA && city.owner == C.LEAGUE
//                        && game.getDiplomacy().getDiplomaticState(city.owner, faction.x) != C.DS_WAR
//                        && Util.anyCargoPods(moving_stack)) {
////                    logger.debug(" + agora sale");
//                    agora_sale = true;
//                } else {
//                    stop();
//                    return false;
//                }
//            }
//            if (!agora_sale && !stack.isEmpty() && stack.get(0).owner != faction.x) {
//                stop();
//                return false;
//            }
//        }
        //1: no city in hex
        if (city == null) {
            //1.1: no enemy units in stack
            if (stack.isEmpty() || stack.get(0).owner == faction.x) {
//                logger.debug("PW4 1.1");
                tryToMove();
                //1.4: enemy units in stack
            } else {
//                logger.debug("PW4 1.4");
                //1.4.1: only non ground combatants in stack
                if (game.isCapture()) {
//                    logger.debug("PW4 1.4.1");
                    game.capture(faction);
                    stack_moving = false;
                    //1.4.2: ground combatants in stack
                } else {
//                    logger.debug("PW4 1.4.2");
                    combat();
                    stack_moving = false;
                }
            }
            //2: own city in hex
        } else if (city.owner == faction.x) {
            tryToMove();
            //4: at war with city owner
        } else {
            //4.1: no units in stack
            if (stack.isEmpty()) {
//                logger.debug("PW4 4.1");
                game.captureCity(city, faction.x, faction.y);
                tryToMove();
                stack_moving = false;
                //4.4: enemy units in stack
            } else {
                //4.4.1: only non ground combatants in stack
                if (game.isCapture()) {
//                    logger.debug("PW4 4.4.1");
                    game.capture(faction);
                    game.captureCity(city, faction.x, faction.y);
                    stack_moving = false;
                    //4.4.2: ground combatants in stack
                } else {
//                    logger.debug("PW4 4.4.2");
                    combat();
                    stack_moving = false;
                }
            }
        }
        return stack_moving;
    }

    protected void tryToMove() {
        game.moveStack();
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
        for (Structure s : enemy_structures) {
            if (s.p_idx == p_idx && map[s.x][s.y].getLandNr() == c_idx) {
                targets.add(map[s.x][s.y]);
                logger.debug("     city " + " " + Structure.getName(s.type) + " " + s.x + "," + s.y);
            }
        }
        for (Unit u : enemy_units) {
            if (u.p_idx == p_idx && map[u.x][u.y].getLandNr() == c_idx) {
                targets.add(map[u.x][u.y]);
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
                    logger.debug(" call Ai.reconPlanet()");
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
     */
    protected void reconPlanet(LinkedList<Unit> units, int p_idx) {
        PlanetGrid pg = game.getPlanetGrid(p_idx);
        LinkedList<Hex> unexplored = new LinkedList();

    }

//        LinkedList<LinkedList<LinkedList<LinkedList<Unit>>>> known_planets = new LinkedList<>();
//        LinkedList<LinkedList<LinkedList<Unit>>> current_planet = new LinkedList<>();
//        known_planets.add(current_planet);
//        LinkedList<LinkedList<Unit>> current_continent = new LinkedList<>();
//        current_planet.add(current_continent);
//        LinkedList<Unit> current_units = new LinkedList<>();
//        current_continent.add(current_units);
//        PlanetGrid planet_grid = null;
//        Unit prev = null;
//        int prev_hex_idx = -1;
//        for (Unit unit : units) {
//            if (current_units.isEmpty()) {
//                planet_grid = game.getPlanetGrid(unit.p_idx);
//                current_units.add(unit);
//                prev = unit;
//                prev_hex_idx = planet_grid.getHex(unit.x, unit.y).getHexIdx();
//            } else {
//                if (prev.p_idx != unit.p_idx) {
//                    planet_grid = game.getPlanetGrid(unit.p_idx);
//                    current_planet = new LinkedList<>();
//                    known_planets.add(current_planet);
//                    current_continent = new LinkedList<>();
//                    current_planet.add(current_continent);
//                    current_units = new LinkedList<>();
//                    current_continent.add(current_units);
//                } else if (planet_grid.getIntraContHexDist(prev_hex_idx, planet_grid.getHex(unit.x, unit.y).getHexIdx()) != -1) {
//                    current_continent = new LinkedList<>();
//                    current_planet.add(current_continent);
//                    current_units = new LinkedList<>();
//                    current_continent.add(current_units);
//                } else if (prev.x != unit.x || prev.y != unit.y) {
//                    current_units = new LinkedList<>();
//                    current_continent.add(current_units);
//                }
//                current_units.add(unit);
//            }
//        }
}
