/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

/**
 * MsgsW (Messages Window)
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class MsgsW extends State {

    private static MsgsW instance = new MsgsW();

    public MsgsW() {
    }

    public static State get() {
        return instance;
    }

    public void pressExitButton() {
        SU.restoreMainWindow();
        gui.setCurrentState(main_game_state);
        main_game_state = null;
    }

}
