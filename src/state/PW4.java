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
import javax.swing.JOptionPane;
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

    public void clickOnPlanetMap(MouseEvent e) {

        gui.stopStackMove();

    }

    public void stackMoveEvent() {
        gui.setStack_move_counter(gui.getStack_move_counter() + 1);
        if (gui.getStack_move_counter() >= 20) {
            gui.setStack_move_counter(0);
            LinkedList<Hex> path = game.getPath();
            Hex target_hex = path.get(1);
            Structure city = target_hex.getStructure();
            List<Unit> stack = target_hex.getStack();
            Unit moving_unit = game.getSelectedStack().get(0);
            Point faction = new Point(moving_unit.owner, moving_unit.prev_owner);
            boolean stack_moving = true;
            //1: no city in hex
            if (city == null) {
                //1.1, 1.2: no units in stack or own units in stack
                System.out.println("DEBUG1 " + faction.x);

                if (stack.isEmpty() || stack.get(0).owner == faction.x) {
                    System.out.println("DEBUG2");
                    tryToMove();
//                    //1.3:neutral units in stack
//                } else if (game.isNeutral(stack.get(0).owner)) {
//                    //TODO
                    //1.4: enemy units in stack
                } else {
                    //1.4.1: only non ground combatants in stack
                    if (game.isCapture()) {
                        game.capture(faction);
                        stop();
                        stack_moving = false;
                        //1.4.2: ground combatants in stack
                    } else {
                        combat();
                        stack_moving = false;
                    }
                }
                //2: own city in hex
            } else if (city.owner == faction.x) {
                //2.1, 2.2: no units in stack or own units in stack
                if (stack.isEmpty() || stack.get(0).owner == faction.x) {
                    tryToMove();
                }
                //2.3, 2.4 not possible
            //3: neutral city in hex TODO only trade with league is considered
            } else if (game.getDiplomacy().getDiplomaticState(target_hex.getStructure().owner, faction.x) != C.DS_WAR
                    && target_hex.getStructure().type == C.AGORA && target_hex.getStructure().owner == C.LEAGUE
                    && Util.anyCargoPods(path.get(0).getStack())) {
                stop();
                stack_moving = false;
                saveMainGameState();
                gui.setCurrentState(AW2.get());
                gui.getAgoraWindow().enterAgora(target_hex);
                SU.setWindow(C.S_AGORA_WINDOW);
                //4: at war with city owner
            } else {
                //4.1: no units in stack
                if (stack.isEmpty()) {
                    game.captureCity(city, faction.x, faction.y);
                    tryToMove();
                    stop();
                    stack_moving = false;
                    //4.2, 4.3 not possible
                    //4.4: enemy units in stack
                } else {
                    //4.4.1: only non ground combatants in stack
                    if (game.isCapture()) {
                        game.capture(faction);
                        game.captureCity(city, faction.x, faction.y);
                        stop();
                        stack_moving = false;
                        //4.4.2: ground combatants in stack
                    } else {
                        combat();
                        //System.out.println("Path: " + game.getPath());
//                        game.setPath(path);
//                        if (target_hex.getStack().isEmpty() || game.isCapture()) {
//                            game.capture();
//                            game.captureCity(city, f_idx);
//                        }
//                        game.setPath(null);
                        stack_moving = false;
                    }
                }
            }

            if (stack_moving) {
                if (path != null && path.getFirst().equals(path.getLast())) {
                    gui.setStop_stack(true);
                    game.setPath(null);
                } else if (!Util.moveCapable(game)) {
                    gui.setStop_stack(true);
                }
                if (gui.isStop_stack()) {
                    gui.getStack_move_timer().stop();
                    gui.setStack_moving(false);
                    if (game.getPath() == null) {
                        gui.setCurrentState(PW2.get());
                    } else {
                        gui.setCurrentState(PW3.get());
                    }
                    gui.setMenus(true);
                }
            }
        }
        if (0 < gui.getStack_move_counter() && gui.getStack_move_counter() < 20) {
            gui.getPlanetMap().repaint();
        } else {
            gui.getPlanetWindow().repaint();
        }
    }

    private void tryToMove() {
        if (!game.moveStack()) {
            gui.setStop_stack(true);
            JOptionPane.showMessageDialog(gui, "Too many units in the destination area.", null, JOptionPane.PLAIN_MESSAGE);
        }
    }

    private void combat() {
        gui.setStop_stack(true);
//                game.setPath(null);
        game.resolveGroundBattleInit(C.GROUND_COMBAT, -1);
        gui.setCurrentState(PW2.get());
        gui.setMenus(true);
        gui.getStack_move_timer().stop();
        gui.setStack_moving(false);
        SU.showCombatWindow();
    }

//    private void capture() {
//        game.capture();
//        stop();
//    }
    private void stop() {
        gui.setStop_stack(true);
        game.setPath(null);
        gui.setCurrentState(PW2.get());
        gui.setMenus(true);
        gui.getStack_move_timer().stop();
        gui.setStack_moving(false);
    }

    // hides methods from PW
    public void pressNextStackButton() {
    }

    public void pressSkipStackButton() {
    }

    public void pressEndTurnButton() {
    }

    public void pressSpaceButton() {
    }

    public void clickOnPlanetWindow(MouseEvent e) {
    }

}
