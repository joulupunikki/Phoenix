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
import util.C;

/**
 * Combat Window PTS fire 2
 *
 * @author joulupunikki
 */
public class CWB2 extends State {

    private static CWB2 instance = new CWB2();

    public CWB2() {
    }

    public static State get() {
        return instance;
    }

    @Override
    public void pressFightButton() {
        CWPTS2.pressFightButtonProcess();
    }

    @Override
    public void pressBombardAgainButton() {
        // need to get target hex here since resolveBattleFinalize nulls it
        Hex target = game.getBattle().getRangedSpaceTarget();
        game.resolveGroundBattleFinalize();
        gui.getCombatWindow().setFightText("Do Combat");
        gui.getCombatWindow().toggleButtons(false, true, false);       
        game.startBombardOrPTS(target, true);
        game.resolveGroundBattleInit(C.BOMBARD_COMBAT, game.getBattle().getRangedSpaceTarget().getStack().get(0).owner);
        gui.setCurrentState(CWB1.get());
        game.subMovePointsSpace(game.getCombatStack("a"));
        SU.showCombatWindowBombard();
    }

}
