/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

/**
 *
 * @author joulupunikki
 */
public class CW1 extends State {

    private static CW1 instance = new CW1();

    public CW1() {
    }

    public static State get() {
        return instance;
    }

    public void pressFightButton() {
        game.resolveGroundBattleFight();
        gui.setCurrentState(CW2.get());
    }

}
