/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Hex;
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

//    public void stackMoveEvent2() {
//        gui.setStack_move_counter(gui.getStack_move_counter() + 1);
//        if (gui.getStack_move_counter() >= 20) {
//            gui.setStack_move_counter(0);
//            if (game.isCapture()) {
//                gui.setStop_stack(true);
//                game.capture();
//                game.setPath(null);
//                gui.setCurrentState(PW2.get());
//                gui.setMenus(true);
//                gui.getStack_move_timer().stop();
//                gui.setStack_moving(false);
//                return;
//            } else if (game.isCombat()) {
//                gui.setStop_stack(true);
////                game.setPath(null);
//                game.resolveGroundBattleInit(C.GROUND_COMBAT, -1);
//                gui.setCurrentState(PW2.get());
//                gui.setMenus(true);
//                gui.getStack_move_timer().stop();
//                gui.setStack_moving(false);
//                SU.showCombatWindow();
//
//                return;
//            } else if (game.isEnemy()) {
//                gui.setStop_stack(true);
//            } else if (!game.moveStack()) {
//                gui.setStop_stack(true);
//                JOptionPane.showMessageDialog(gui, "Too many units in the destination area.", null, JOptionPane.PLAIN_MESSAGE);
////                gui.showTooManyUnits();
//            }
//            LinkedList<Hex> path = game.getPath();
//            if (path != null && path.getFirst().equals(path.getLast())) {
//                gui.setStop_stack(true);
//                game.setPath(null);
//            } else if (!Util.moveCapable(game)) {
//                gui.setStop_stack(true);
//            }
//            if (gui.isStop_stack()) {
//                gui.getStack_move_timer().stop();
//                gui.setStack_moving(false);
//                if (game.getPath() == null) {
//                    gui.setCurrentState(PW2.get());
//                } else {
//                    gui.setCurrentState(PW3.get());
//                }
//                gui.setMenus(true);
//            }
//        }
//        if (0 < gui.getStack_move_counter() && gui.getStack_move_counter() < 20) {
//            gui.getPlanetMap().repaint();
//        } else {
//            gui.getPlanetWindow().repaint();
//        }
//    }

    public void stackMoveEvent() {
        gui.setStack_move_counter(gui.getStack_move_counter() + 1);
        if (gui.getStack_move_counter() >= 20) {
            gui.setStack_move_counter(0);
            LinkedList<Hex> path = game.getPath();
            Hex target_hex = path.get(1);
            Structure city = target_hex.getStructure();
            List<Unit> stack = target_hex.getStack();
            int f_idx = game.getTurn();
            boolean stack_moving = true;
            //1: no city in hex
            if (city == null) {
                //1.1, 1.2: no units in stack or own units in stack
                if (stack.isEmpty() || stack.get(0).owner == f_idx) {
                    tryToMove();
//                    //1.3:neutral units in stack
//                } else if (game.isNeutral(stack.get(0).owner)) {
//                    //TODO
                    //1.4: enemy units in stack
                } else {
                    //1.4.1: only non ground combatants in stack
                    if (game.isCapture()) {
                        game.capture();
                        stop();
                        stack_moving = false;
                        //1.4.2: ground combatants in stack
                    } else {
                        combat();
                        stack_moving = false;
                    }
                }
                //2: own city in hex
            } else if (city.owner == f_idx) {
                //2.1, 2.2: no units in stack or own units in stack
                if (stack.isEmpty() || stack.get(0).owner == f_idx) {
                    tryToMove();
                }
                //2.3, 2.4 not possible
//            //3: neutral city in hex
//            } else if (game.isNeutral(target_hex.getStructure().owner)) {
//                // TODO
                //4: at war with city owner
            } else {
                //4.1: no units in stack
                if (stack.isEmpty()) {
                    game.captureCity(city, f_idx);
                    tryToMove();
                    stop();
                    stack_moving = false;
                    //4.2, 4.3 not possible
                    //4.4: enemy units in stack
                } else {
                    //4.4.1: only non ground combatants in stack
                    if (game.isCapture()) {
                        game.capture();
                        game.captureCity(city, f_idx);
                        stop();
                        stack_moving = false;
                        //4.4.2: ground combatants in stack
                    } else {
                        combat();
                        System.out.println("Path: " + game.getPath());
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
