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
package state;

import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Hex;
import game.Research;
import gui.CombatStrategyPanel;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;
import org.apache.commons.math3.random.RandomGenerator;
import util.C;
import util.Util;

/**
 * Planet Window selected stack is moving along path
 *
 * @author joulupunikki
 */
public class PW4 extends PW {

    private static PW4 instance = new PW4();
    private final int LOOT_SIZE = 4;
    private final int BASE_LOOT_SIZE = 1;
    private final int MAX_LOOT_UNITS = 3;

    private final int GUARD_UNIT_COUNT = 0;
    private final int GUARD_UNIT_TYPE = 1;
    private final int GUARD_UNIT_T_LVL = 2;

    private int[][][] ruin_guard_list = {
        {{1, 45, 0}, // noble
        {4, 82, 0}, // power leg
        {2, 85, 0}, //Flak
        {2, 86, 0}, //AT
        {2, 87, 0}}, //Arty
        {{3, 55, 0}, //Rebel Partisans
        {3, 74, 0}, //Jet Fighter
        {1, 85, 0}, //Flak
        {1, 86, 0}, //AT
        {1, 87, 0}}, //Arty
        {{1, 51, 0}, //Officer
        {5, 83, 0}, //Infantry Legion
        {1, 85, 0}, //Flak
        {1, 86, 0}, //AT
        {1, 87, 0}}, //Arty
        {{3, 59, 0}, //Medium Tank
        {1, 65, 0}, //Tank Destroyer
        {1, 67, 0}, //Mobile Flak Battery
        {3, 84, 0}, //Militia Legion
        {1, 87, 0}}, //Arty
        {{1, 13, 0}, //Space Cruiser
        {1, 15, 0}, //Space Frigate
        {2, 19, 0}, //Assault Lander
        {3, 84, 0}, //Militia Legion
        {2, 86, 0}, //AT
        {2, 87, 0}}, //Arty
        {{3, 38, 0}, //Symbiot Spitter
        {1, 39, 0}, //Symbiot Minder
        {2, 42, 0}, //Symbiot Butcher
        {3, 43, 0}} //Symbiot Reaver
    };

    private enum LOOT_CATEGORIES {
        UNIT,
        TECH,
        RESOURCE
    }

    private List<LOOT_CATEGORIES> loot_list;

    public PW4() {
        loot_list = new LinkedList<>();
        for (LOOT_CATEGORIES value : LOOT_CATEGORIES.values()) {
            loot_list.add(value);
        }
    }

    public static State get() {
        return instance;
    }

    @Override
    public void clickOnMainMap(MouseEvent e) {

        gui.stopStackMove();

    }

    @Override
    public void stackMoveEvent() {
        gui.setStack_move_counter(gui.getStack_move_counter() + 1);
        if (gui.getStack_move_counter() >= 20) {
            gui.setStack_move_counter(0);
            LinkedList<Hex> path = game.getPath();
            /*
             try to move to next hex along path, this may result in failed move,
             successful move, combat, trading etc. iff the stack should continue
             moving along path then stack_moving will be true
             */
            boolean stack_moving = moveToNextHex(path);
            /*
             if stack has movement points and path left then continue moving
             */
            if (stack_moving) {
                continueMoving(path);
            }
        }
        // only minimal repaint needs to be done when animating stack movement
        if (0 < gui.getStack_move_counter() && gui.getStack_move_counter() < 20) {
            gui.getPlanetMap().repaint();
        } else {
            gui.getPlanetWindow().repaint();
        }
    }

    private void continueMoving(LinkedList<Hex> path) {
        // no path left
        if (path != null && path.getFirst().equals(path.getLast())) {
            gui.setStop_stack(true);
            game.setPath(null);
            // no movement points left
        } else if (!Util.moveCapable(game)) {
            gui.setStop_stack(true);
        }
        // stop animation, set state, activate menus
        if (gui.isStop_stack()) {
            gui.getStack_move_timer().stop();
            gui.setStack_moving(false);
            if (game.getPath() == null) {
                gui.setCurrentState(PW2.get());
            } else {
                gui.setCurrentState(PW3.get());
            }
            gui.setMenus(C.S_PLANET_MAP);
        }
    }
    /**
     * What happens when a stack moves between two planetary hexes.
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
     * TODO Additionally, we have the special case for empty cities: x. city
     * type is (alien) ruins and guardians have not been generated ... in this
     * case the move is stopped and guardians will be generated in the city hex,
     * the owner of the ruins should also be set to the owner of the guardian
     * units
     * <p>
     * Additionally, we have the special case for cities which are entered by
     * units (ie. a move into hex with the city has just succeeded): y. city
     * type is (alien) ruins or city type is monastery ... in this case the city
     * special treasure should be generated (possible stack overflow should be
     * handled) and the city should be deleted
     * <p>
     * Combining the above, and removing impossible combinations we get the
     * following cases (stacks with only non-combat units are handled
     * separately):
     * <pre>
     * 1: no city in target hex
     *   1.1: no units in stack
     *   *** try to move
     *   1.2: own units in stack
     *   *** check merging of loaned
     *   *** try to move
     *   1.3: neutral units in stack
     *     *** check Byz II, ask confirmation, start a war
     *     1.3.1: only non ground combatants in stack
     *     *** capture
     *     1.3.2: ground combatants in stack
     *     *** battle
     *   1.4: enemy units in stack
     *     1.4.1: only non ground combatants in stack
     *     *** capture
     *     1.4.2: ground combatants in stack
     *     *** battle
     * 2: own city in hex
     * *** check merging of loaned
     * *** try to move
     * 3: neutral city in hex //WIP
     *   3.0: league agora and cargo in moving stack
     *   *** sell, return
     *   *** check Byz II, ask confirmation, start a war
     *   3.1: no units in stack
     *   *** capture
     *   3.3: neutral units in stack
     *     3.3.1: only non ground combatants in stack
     *     *** capture
     *     3.3.2: ground combatants in stack
     *     *** battle
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
     * @return true iff moving stack should continue moving along path, i.e. no
     * events occurred which mandate stopping
     */
    private boolean moveToNextHex(LinkedList<Hex> path) {
        gui.setStack_move_counter(0);
        //LinkedList<Hex> path = game.getPath();
        Hex target_hex = path.get(1);
        Structure city = target_hex.getStructure();
        List<Unit> stack = target_hex.getStack();
        List<Unit> moving_stack = Util.getSelectedUnits(game.getSelectedStack());
        Unit moving_unit = moving_stack.get(0);
        Point faction = new Point(moving_unit.owner, moving_unit.prev_owner);
        boolean stack_moving = true;
        // handle non-combat stacks and agora sales
        if (!Util.anyCombat(moving_stack)) {
//            logger.debug("PW4 non-combat stack");
            boolean agora_sale = false;
            if (city != null && city.owner != faction.x) {
                if (city.type == C.AGORA && city.owner == C.LEAGUE
                        && game.getDiplomacy().getDiplomaticState(city.owner, faction.x) != C.DS_WAR
                        && Util.anyCargoPods(moving_stack)) {
//                    logger.debug(" + agora sale");
                    agora_sale = true;
                } else {
                    stop();
                    return false;
                }
            }
            if (!agora_sale && !stack.isEmpty() && stack.get(0).owner != faction.x) {
                stop();
                return false;
            }
        }
        //1: no city in hex
        if (city == null) {
            //1.1: no units in stack 
            if (stack.isEmpty()) {
//                logger.debug("PW4 1.1");
                tryToMove();
                //1.2: own units in stack
            } else if (stack.get(0).owner == faction.x) {
//                logger.debug("PW4 1.2");
                if (stack.get(0).prev_owner != faction.y) {
                    gui.showInfoWindow("Cannot merge loaned unit stacks.");
                    stop();
                    return false;
                }
                tryToMove();
                //1.3:neutral units in stack
            } else if (game.getDiplomacy().getDiplomaticState(faction.x, stack.get(0).owner) != C.DS_WAR) {
//                logger.debug("PW4 1.3");
                if (!byzIICombatOK(moving_stack)) {
                    return false;
                }
                if (!gui.showAttackConfirmWindow(faction.x, stack)) {
                    stop();
                    return false;
                }
                game.getDiplomacy().setDiplomaticState(faction.x, stack.get(0).owner, C.DS_WAR);
                //1.3.1: only non ground combatants in stack
                if (game.isCapture()) {
//                    logger.debug("PW4 1.3.1");
                    game.capture(faction);
                    stop();
                    stack_moving = false;
                    //1.3.2: ground combatants in stack
                } else {
//                    logger.debug("PW4 1.3.2");
                    combat();
                    stack_moving = false;
                }
                //1.4: enemy units in stack
            } else {
//                logger.debug("PW4 1.4");
                if (!byzIICombatOK(moving_stack)) {
                    return false;
                }
                //1.4.1: only non ground combatants in stack
                if (game.isCapture()) {
//                    logger.debug("PW4 1.4.1");
                    game.capture(faction);
                    stop();
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
//            logger.debug("PW4 2");
            if (city.prev_owner != faction.y) {
                gui.showInfoWindow("Cannot merge loaned unit stacks.");
                stop();
                return false;
            }
            tryToMove();
            //y: check if city is lootable
            handleLootableCity(city, moving_unit, faction, false);
            //3: neutral city in hex
        } else if (game.getDiplomacy().getDiplomaticState(city.owner, faction.x) != C.DS_WAR) {
            //3.0: league agora and cargo in moving stack
            if (city.type == C.AGORA && city.owner == C.LEAGUE
                    && Util.anyCargoPods(moving_stack)) {
//                logger.debug("PW4 3.0");
                stop();
                saveMainGameState();
                gui.setCurrentState(AW2.get());
                gui.getAgoraWindow().enterAgora(target_hex);
                SU.setWindow(C.S_AGORA_WINDOW);
                return false;
            }
            if (!byzIICombatOK(moving_stack)) {
                return false;
            }
            if (!gui.showAttackConfirmWindow(faction.x, stack)) {
                stop();
                return false;
            }
            game.getDiplomacy().setDiplomaticState(faction.x, stack.get(0).owner, C.DS_WAR);
            //3.1: no units in stack
            if (stack.isEmpty()) {
//                logger.debug("PW4 3.1");
                if (handleLootableCity(city, moving_unit, faction, true)) {
                    return false;
                }
                game.captureCity(city, faction.x, faction.y);
                tryToMove();
                // y: check if city is lootable
                handleLootableCity(city, moving_unit, faction, false);
                stop();
                stack_moving = false;
                //3.2 not possible
                //3.3: neutral units in stack
            } else {
                //3.3.1: only non ground combatants in stack
                if (game.isCapture()) {
//                    logger.debug("PW4 3.3.1");
                    game.capture(faction);
                    game.captureCity(city, faction.x, faction.y);
                    stop();
                    stack_moving = false;
                    //3.3.2: ground combatants in stack
                } else {
//                    logger.debug("PW4 3.3.2");
                    combat();
                    stack_moving = false;
                }
            }
            //3.4 not possible
            //4: at war with city owner
        } else {
//            logger.debug("PW4 4");
            if (!byzIICombatOK(moving_stack)) {
                return false;
            }
            //4.1: no units in stack
            if (stack.isEmpty()) {
//                logger.debug("PW4 4.1");
                if (handleLootableCity(city, moving_unit, faction, true)) {
                    return false;
                }
                game.captureCity(city, faction.x, faction.y);
                tryToMove();
                //y: check if city is lootable
                handleLootableCity(city, moving_unit, faction, false);
                stop();
                stack_moving = false;
                    //4.2, 4.3 not possible
                //4.4: enemy units in stack
            } else {
                //4.4.1: only non ground combatants in stack
                if (game.isCapture()) {
//                    logger.debug("PW4 4.4.1");
                    game.capture(faction);
                    game.captureCity(city, faction.x, faction.y);
                    stop();
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

    private boolean handleLootableCity(Structure city, Unit moving_unit, Point faction, boolean is_guard) throws AssertionError {
        if (is_guard) {
            return handleRuinGuards(city, moving_unit, faction, is_guard);
        }
        return handleLoot(city, moving_unit, faction, is_guard);
    }

    private boolean handleRuinGuards(Structure city, Unit moving_unit, Point faction, boolean is_guard) {
        switch (city.type) {
            case C.RUINS:
            case C.ALIEN_RUINS:
                break;
            default:
                return false;
        }
        if (!city.getFlag(Structure.FLAG.RUIN_GUARD)) {
            city.setFlags(Structure.FLAG.RUIN_GUARD, true);
        } else {
            return false;
        }
        long city_seed = createCitySeed(is_guard, city);
        // fix #104 each lootable city will have a random, but per game fixed loot
        Random loot_random = new RandomSplit(game.getInitialSeed() ^ city_seed);
        switch (city.type) {
            case C.ALIEN_RUINS:
            // TODO alien ruin guardians
            case C.RUINS:
                int stack_nr = loot_random.nextInt(ruin_guard_list.length);
                for (int i = 0; i < ruin_guard_list[stack_nr].length; i++) {
                    for (int j = 0; j < ruin_guard_list[stack_nr][i][GUARD_UNIT_COUNT]; j++) {
                        game.createUnitInHex(city.p_idx, city.x, city.y,
                                C.NEUTRAL, C.NEUTRAL,
                                ruin_guard_list[stack_nr][i][GUARD_UNIT_TYPE],
                                ruin_guard_list[stack_nr][i][GUARD_UNIT_T_LVL], 0, 0);
                    }
                }
                break;
            default:
                throw new AssertionError();
        }
        return true;
    }
    /**
     *
     * @param city the value of city
     * @param moving_unit the value of moving_unit
     * @param faction the value of faction
     * @param is_guard the value of is_guard
     * @return the boolean
     * @throws AssertionError
     */
    private boolean handleLoot(Structure city, Unit moving_unit, Point faction, boolean is_guard) throws AssertionError {
        switch (city.type) {
            case C.RUINS:
            case C.ALIEN_RUINS:
            case C.MONASTERY:
                break;
            default:
                return false;
        }

        long city_seed = createCitySeed(is_guard, city);
        // fix #104 each lootable city will have a random, but per game fixed loot
        Random loot_random = new RandomSplit(game.getInitialSeed() ^ city_seed);
        int type;
        int amount;
        switch (city.type) {
            case C.RUINS:
                // TODO ruin loot
                LinkedList<LOOT_CATEGORIES> loot_list = new LinkedList<>();
                loot_list.addAll(this.loot_list);
                Collections.shuffle(loot_list, loot_random);
                int loot_left = BASE_LOOT_SIZE;
                boolean relic = false;
                if (loot_random.nextInt(LOOT_SIZE) == 0) {
                    loot_left++;
                }
                // TODO if relics left => relic = true
                for (int i = 0; i < loot_left; i++) {
                    if (relic && loot_random.nextBoolean()) {
                        relic = false;
                        // TODO generate relic
                        continue;
                    }
                    LOOT_CATEGORIES loot_category = loot_list.pop();
                    switch (loot_category) {
                        case UNIT:
                            List<Unit> tmp = new LinkedList<>();
                            type = loot_random.nextInt(C.UNIT_TYPES);
                            amount = loot_random.nextInt(MAX_LOOT_UNITS) + 1;
                            for (int j = 0; j < amount; j++) {
                                Hex hex = game.findRoom(city, game.getUnitTypes()[type][0].move_type);
                                game.createUnitInHex(moving_unit.p_idx,
                                        hex.getX(), hex.getY(),
                                        faction.x, faction.y, type, 0, 0, 0);
                            }
                            String name = game.getUnitTypes()[type][0].name;
                            String singular = " has pledged fealty to your noble house!";
                            String plural = " have pledged fealty to your noble house!";
                            String end = singular;
                            String start = "A";
                            if (amount > 1) {
                                switch (amount) {
                                    case 2:
                                        start = "Two";
                                        break;
                                    case 3:
                                        start = "Three";
                                        break;
                                    default:
                                        throw new AssertionError();
                                }
                                switch (game.getUnitTypes()[type][0].plural) {
                                    case 0:
                                        break;
                                    case 1:
                                        name += "s";
                                        break;
                                    case 2:
                                        name += "es";
                                        break;
                                    case 3:
                                        name = name.substring(0, name.length() - 1) + "ies";
                                        break;
                                    default:
                                        throw new AssertionError();
                                }
                                end = plural;
                            }
                            gui.showInfoWindow(start + " lost " + name + end);
                            break;
                        case TECH:
                            gainResource(moving_unit, loot_random);
                            break;
                        case RESOURCE:
                            type = loot_random.nextInt(C.RES_TYPES);
                            amount = 100;
                            if (type > C.RES_EXOTICA) {
                                amount = 50;
                            }
                            if (type > C.RES_WETWARE) {
                                amount = 5;
                            }
                            Hex hex = game.findRoom(city, game.getUnitTypes()[type][0].move_type);
                            game.createUnitInHex(moving_unit.p_idx,
                                    hex.getX(), hex.getY(),
                                    faction.x, faction.y, C.CARGO_UNIT_TYPE, 0, type, amount);
                            gui.showInfoWindow("Your archeologist has found " + amount
                                    + " points of " + Util.getResName(type) + " buried beneath the ruins.");
                            break;
                        default:
                            throw new AssertionError();
                    }
                }
                break;
            case C.ALIEN_RUINS:
                type = loot_random.nextInt(3) + C.RES_MONOPOLS;
                amount = 10;
                if (type > C.RES_MONOPOLS) {
                    amount = 20;
                }
                if (type > C.RES_GEMS) {
                    amount = 10;
                }
                Hex hex = game.findRoom(city, game.getUnitTypes()[type][0].move_type);
                game.createUnitInHex(moving_unit.p_idx,
                        hex.getX(), hex.getY(),
                        faction.x, faction.y, C.CARGO_UNIT_TYPE, 0, type, amount);
                gui.showInfoWindow("Your archeologist has found " + amount
                        + " points of " + Util.getResName(type) + " buried beneath the ruins.");
                break;
            case C.MONASTERY:
                gainResource(moving_unit, loot_random);
                break;
            default:
                throw new AssertionError();
        }
        game.destroyCity(city.p_idx, city.x, city.y);
        return true;
    }

    /**
     * Uses city coordinates to create a unique long, a flipped bit at high 32
     * bits separates for guards and loot for the same city
     *
     * @param is_guard
     * @param city
     * @return
     */
    private long createCitySeed(boolean is_guard, Structure city) {
        /*
        use city coordinates to create a unique long, a flipped bit at high
        32 bits separates for guards and loot for the same city
         */
        long city_seed = 0;
        if (is_guard) {
            city_seed = 1L << 32;
        }
        city_seed = (city_seed ^ city.p_idx) << 13;
        city_seed = (city_seed ^ city.x) << 11;
        city_seed ^= city.y;
        System.out.println("City seed = " + Long.toBinaryString(city_seed));
        return city_seed;
    }

    private void gainResource(Unit moving_unit, Random loot_random) {
        Research res = game.getFaction(moving_unit.owner).getResearch();
        List<Integer> researchables = res.getResearchableTechs();
        int received_tech = 0;
        if (researchables.isEmpty()) {
            gui.showInfoWindow("You found no new knowledge in the ruins ...");
            return;
        } else if (researchables.size() > 1) {
            received_tech = researchables.get(loot_random.nextInt(researchables.size()));
        }
        res.receiveTech(received_tech);
        gui.showInfoWindow("You have found the ancient knowledge of " + game.getGameResources().getTech()[received_tech].name + " !");
        return;
    }

    private boolean byzIICombatOK(List<Unit> stack) {
//        return true; //DEBUG
        if (SU.byzIICombatOK(stack, true)) {
            return true;
        }

        stop();
        return false;
    }

    /**
     *
     * @return the boolean
     */
    private boolean tryToMove() {
        boolean rv = true;
        if (!game.moveStack()) {
            gui.setStop_stack(true);
            gui.showInfoWindow("Too many units in the destination area.");
            rv = false;
        }
        return rv;
    }

    private void combat() {
        gui.setMenus(C.S_PLANET_MAP);
        gui.setStop_stack(true);
        gui.getStack_move_timer().stop();
        gui.setStack_moving(false);
        CombatStrategyPanel.Strategy[] strategy = {CombatStrategyPanel.Strategy.NORMAL};
        if (game.getEfs_ini().combat_strategy_selection) {
            gui.showCombatStrategySelectorDialog(strategy);
            if (strategy[0] == CombatStrategyPanel.Strategy.CANCEL) {
                gui.setCurrentState(PW2.get());
                game.setPath(null);
                return;
            }
        }
        gui.setMenus(null);
        game.resolveGroundBattleInit(C.GROUND_COMBAT, -1, strategy[0]);
        gui.setCurrentState(PW2.get());
        gui.setMenus(C.S_PLANET_MAP);
        SU.showCombatWindow();
    }

    private void stop() {
        gui.setStop_stack(true);
        game.setPath(null);
        gui.setCurrentState(PW2.get());
        gui.setMenus(C.S_PLANET_MAP);
        gui.getStack_move_timer().stop();
        gui.setStack_moving(false);
    }

    // hide methods to disable functionality
    @Override
    public void pressNextStackButton() {
    }

    @Override
    public void pressSkipStackButton() {
    }

    @Override
    public void pressSentryButton() {
    }

    @Override
    public void pressEndTurnButton() {
    }

    @Override
    public void pressSpaceButton() {
    }

    @Override
    public void clickOnPlanetWindow(MouseEvent e) {
    }

    @Override
    public void clickOnGlobeMap(MouseEvent e) {
    }

    private class RandomSplit extends Random implements RandomGenerator {

        private static final long serialVersionUID = 1L;
        private SplittableRandom rng;

        private RandomSplit() {
        }

        private RandomSplit(long seed) {
            rng = new SplittableRandom(seed);
        }

        @Override
        public int nextInt(int bound) {
            return rng.nextInt(bound);
        }

        @Override
        public void setSeed(int i) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setSeed(int[] ints) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setSeed(long l) {
            super.setSeed(l);
        }

        @Override
        public void nextBytes(byte[] bytes) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int nextInt() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public long nextLong() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean nextBoolean() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public float nextFloat() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public double nextDouble() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public double nextGaussian() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
