/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import util.C;

/**
 *
 * @author joulupunikki
 */
public class CW2 extends State {

    private static CW2 instance = new CW2();

    public CW2() {
    }

    public static State get() {
        return instance;
    }

    public void pressFightButton() {
        // need to get combat_type here since resolveBattleFinalize nulls it 
        String combat_type = game.getCombatType();
        game.resolveGroundBattleFinalize();
        
        if (combat_type.equals(C.GROUND_COMBAT)) {
        game.setPath(null);
        }
        SU.restoreMainWindow();
        gui.setCurrentState(main_game_state);
        main_game_state = null;
        if (game.getSelectedStack().isEmpty()) {
            game.setSelectedPointFaction(null, -1, null, null);
            if (gui.getCurrentState() instanceof PW) {
                gui.setCurrentState(PW1.get());
            } else if (gui.getCurrentState() instanceof SW) {
                gui.setCurrentState(SW1.get());
            }
            
        }
    }
}
