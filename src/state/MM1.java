/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import java.awt.event.ItemEvent;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

/**
 *
 * @author joulupunikki
 */
public class MM1 extends State {

    private static MM1 instance = new MM1();

    public MM1() {
    }

    public static State get() {
        return instance;
    }

    public void selectFactionControl(ItemEvent e, JCheckBox[] hc) {

        Object source = e.getItemSelectable();

        int source_nr = -1;
        for (int i = 0; i < hc.length; i++) {
            if (source == hc[i]) {
                source_nr = i;

                break;
            }

        }

        boolean player_human;
        if (e.getStateChange() == ItemEvent.DESELECTED) {
            player_human = false;
        } else {
            player_human = true;
        }
        game.setFactionPlayer(source_nr, player_human);
    }

//    public void selectFactionControl(ItemEvent e, int source) {
//        boolean player_human;
//        if (e.getStateChange() == ItemEvent.DESELECTED) {
//            player_human = false;
//        } else {
//            player_human = true;
//        }
//        game.setFactionPlayer(source, player_human);
//    }
    public void pressPlayButton() {
        if (game.humanPlayerPresent()) {
            game.beginGame();
            // if pbem ask for new passwd
            if (game.getEfs_ini().pbem.pbem) {
                game.getEfs_ini().pbem.getPasswd(game.getTurn(), gui);
            }
            
            SU.selectNextUnmovedUnit();
        } else {
            JOptionPane.showMessageDialog(gui, "Need to select at least one human player.", null, JOptionPane.PLAIN_MESSAGE);
        }
    }
}
