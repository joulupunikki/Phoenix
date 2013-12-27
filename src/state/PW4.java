/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import game.Hex;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
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
            if (game.isCapture()) {
                gui.setStop_stack(true);
                game.capture();
                game.setPath(null);
                gui.setCurrentState(PW2.get());
                gui.setMenus(true);
                gui.getStack_move_timer().stop();
                gui.setStack_moving(false);
                return;
            } else if (game.isCombat()) {
                gui.setStop_stack(true);
//                game.setPath(null);
                game.resolveGroundBattleInit(C.GROUND_COMBAT, -1);
                gui.setCurrentState(PW2.get());
                gui.setMenus(true);
                gui.getStack_move_timer().stop();
                gui.setStack_moving(false);
                SU.showCombatWindow();

                return; 
            } else if (game.isEnemy()) {
                gui.setStop_stack(true);
            } else if (!game.moveStack()) {
                gui.setStop_stack(true);
                JOptionPane.showMessageDialog(gui, "Too many units in the destination area.", null, JOptionPane.PLAIN_MESSAGE);
//                gui.showTooManyUnits();
            }
            LinkedList<Hex> path = game.getPath();
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
        gui.getPlanetWindow().repaint();
    }

    // hides methods from PW
    public void pressNextStackButton() {
    }

    public void pressEndTurnButton() {
    }
}
