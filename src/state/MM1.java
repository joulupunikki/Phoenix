/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package state;

import util.C;

/**
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class MM1 extends State {

    private static MM1 instance = new MM1();

    public MM1() {
    }

    public static State get() {
        return instance;
    }

    public void pressStartNew() {
        SU.setWindow(C.S_MAIN_MENU);
        gui.setCurrentState(MM3.get());
    }

    public void pressLoadGame() {
        gui.loadGame();
    }

    public void pressQuit() {
        System.exit(0);
    }

}
