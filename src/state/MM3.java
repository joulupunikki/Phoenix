/*
 * Copyright (C) 2015 joulupunikki joulupunikki@gmail.communist.invalid.
 *
 *  Disclaimer of Warranties and Limitation of Liability.
 *
 *     The creators and distributors offer this software as-is and
 *     as-available, and make no representations or warranties of any
 *     kind concerning this software, whether express, implied, statutory,
 *     or other. This includes, without limitation, warranties of title,
 *     merchantability, fitness for a particular purpose, non-infringement,
 *     absence of latent or other defects, accuracy, or the presence or
 *     absence of errors, whether or not known or discoverable.
 *
 *     To the extent possible, in no event will the creators or distributors
 *     be liable on any legal theory (including, without limitation,
 *     negligence) or otherwise for any direct, special, indirect,
 *     incidental, consequential, punitive, exemplary, or other losses,
 *     costs, expenses, or damages arising out of the use of this software,
 *     even if the creators or distributors have been advised of the
 *     possibility of such losses, costs, expenses, or damages.
 *
 *     The disclaimer of warranties and limitation of liability provided
 *     above shall be interpreted in a manner that, to the extent possible,
 *     most closely approximates an absolute disclaimer and waiver of
 *     all liability.
 *
 */
package state;

import java.awt.event.ItemEvent;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

/**
 * Main Menu 3, select game parameters. Like factions, PBEM.
 *
 * @author joulupunikki
 */
public class MM3 extends State {

    private static MM3 instance = new MM3();

    public MM3() {
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
                gui.getPBEMGui().getPasswd(game.getTurn(), gui);
            }

            SU.selectNextUnmovedUnit();
        } else {
            JOptionPane.showMessageDialog(gui, "Need to select at least one human player.", null, JOptionPane.PLAIN_MESSAGE);
        }
    }
}
