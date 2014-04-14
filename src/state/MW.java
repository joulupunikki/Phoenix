/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

/**
 * Main Window MW, superclass of PW and SW, super superclass for Planet Windows
 * (PW1-4) and Space Windows (SW1-3)
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class MW extends State {

    public void pressNextStackButton() {
        SU.pressNextStackButtonSU();
    }

    public void pressSkipStackButton() {
        SU.pressSkipStackButtonSU();
    }

    public void pressEndTurnButton() {
        if (game.getEfs_ini().pbem.pbem) {
            game.setSelectedPoint(null, -1);
            game.setSelectedFaction(-1);
            game.getEfs_ini().pbem.end_turn = true;
            gui.saveGame();
            return;
        }
        game.endTurn();
        game.setPath(null);
        game.setJumpPath(null);
        SU.selectNextUnmovedUnit();

    }
}
