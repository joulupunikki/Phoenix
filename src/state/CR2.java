/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import javax.swing.JTable;
import util.C;

/**
 * Combat Replay 2
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class CR2 extends State {

    private static CR2 instance = new CR2();

    public CR2() {
    }

    public static State get() {
        return instance;
    }

    public void pressExitButton() {
        CR1.updateStacks();
        gui.getCombatWindow().toggleButtons(false, true);
        gui.getMessages().getMessageTable().clearSelection();
        gui.getCombatWindow().setFightText("Do Combat");
        SU.setWindow(C.S_MESSAGES);
        gui.setCurrentState(MsgsW.get());

    }

    public void pressFightButton() {
        CR1.updateStacks();
        JTable messages = gui.getMessages().getMessageTable();
        int row = gui.getMessages().findNextReplay(-2);
        gui.getMessages().setReplay(row);
        gui.getCombatWindow().toggleButtons(false, true);
        gui.getCombatWindow().setFightText("Do Combat");
        gui.getCombatWindow().repaint();
        gui.setCurrentState(CR1.get());
    }

}
