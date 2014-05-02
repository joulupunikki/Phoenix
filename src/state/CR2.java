/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package state;

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

    public void pressFightButton() {
        CR1.updateStacks();
        SU.setWindow(C.S_MESSAGES);
        gui.setCurrentState(MsgsW.get());
    }
}
