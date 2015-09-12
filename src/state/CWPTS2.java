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

import game.Hex;
import java.util.List;
import util.C;
import util.Util;

/**
 * Combat Window Bombard 2
 *
 * @author joulupunikki
 */
public class CWPTS2 extends State {

    private static CWPTS2 instance = new CWPTS2();

    public CWPTS2() {
    }

    public static State get() {
        return instance;
    }

    static void pressFightButtonProcess() {
        game.resolveGroundBattleFinalize();
        gui.getCombatWindow().setFightText("Do Combat");
        gui.getCombatWindow().toggleButtons(false, true, false);
        List<Hex> pts_queue = game.getBattle().getPTSQueue();
        if (!Util.getSelectedUnits(game.getSelectedStack()).isEmpty() && !pts_queue.isEmpty()) {
            Hex pts_hex = pts_queue.remove(0);
            pts_hex.spot(game.getTurn());
            game.startBombardOrPTS(pts_hex, false);
            game.resolveGroundBattleInit(C.PTS_COMBAT, pts_hex.getStack().get(0).owner);
            SU.showCombatWindowPTS();
            return;
        }
        pts_queue.clear();
        if (main_game_state != null && main_game_state.equals(LAND1.get())) {
            main_game_state = null;

            if (!Util.getSelectedUnits(game.getSelectedStack()).isEmpty() && game.landStack(((LAND1) LAND1.get()).popLandingPoint())) {

                gui.setMenus(true);
                gui.setMouseCursor(C.S_CURSOR_SCEPTOR);
                gui.setCurrentState(PW2.get());
                SU.setWindow(C.S_PLANET_MAP);
                return;
            }
        }
        if (Util.getSelectedUnits(game.getSelectedStack()).isEmpty()) {
            game.setSelectedPointFaction(null, -1, null, null);
                gui.setCurrentState(SW1.get());
        } else {
            gui.setCurrentState(SW2.get());
        }
        SU.setWindow(C.S_STAR_MAP);
    }

    @Override
    public void pressFightButton() {
        pressFightButtonProcess();
    }

}
