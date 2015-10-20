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
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import util.C;
import util.Util;

/**
 * Planet Window selected stack is moving along path
 *
 * @author joulupunikki
 */
public class PW4 extends PW {

    private static PW4 instance = new PW4();

    public PW4() {
    }

    public static State get() {
        return instance;
    }

    @Override
    public void clickOnPlanetMap(MouseEvent e) {

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
                game.captureCity(city, faction.x, faction.y);
                tryToMove();
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
                game.captureCity(city, faction.x, faction.y);
                tryToMove();
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

    private boolean byzIICombatOK(List<Unit> stack) {
//        return true; //DEBUG
        if (SU.byzIICombatOK(stack, true)) {
            return true;
        }

        stop();
        return false;
    }

    private void tryToMove() {
        if (!game.moveStack()) {
            gui.setStop_stack(true);
            gui.showInfoWindow("Too many units in the destination area.");
        }
    }

    private void combat() {
        gui.setStop_stack(true);
        game.resolveGroundBattleInit(C.GROUND_COMBAT, -1);
        gui.setCurrentState(PW2.get());
        gui.setMenus(C.S_PLANET_MAP);
        gui.getStack_move_timer().stop();
        gui.setStack_moving(false);
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
    public void pressEndTurnButton() {
    }

    @Override
    public void pressSpaceButton() {
    }

    @Override
    public void clickOnPlanetWindow(MouseEvent e) {
    }

}
