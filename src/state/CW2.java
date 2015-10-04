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

import util.C;
import util.Util;

/**
 * Combat Window (between two planetary stacks) 2
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
        gui.getCombatWindow().setFightText("Do Combat");
        if (Util.getSelectedUnits(game.getSelectedStack()).isEmpty()) {
            game.setSelectedPointFaction(null, -1, null, null);
            if (gui.getCurrentState() instanceof PW) {
                gui.setCurrentState(PW1.get());
            } else if (gui.getCurrentState() instanceof SW) {
                gui.setCurrentState(SW1.get());
            }

        }
    }
}
