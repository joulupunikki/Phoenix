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

import game.Regency;
import util.C;

/**
 * Main Window MW, superclass of PW and SW, super superclass for Planet Windows
 * (PW1-4) and Space Windows (SW1-3)
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class MW extends State {

    @Override
    public void pressNextStackButton() {
        SU.pressNextStackButtonSU();
    }

    @Override
    public void pressSkipStackButton() {
        SU.pressSkipStackButtonSU();
    }

    @Override
    public void pressSentryButton() {
        SU.pressSentryButtonSU();
    }

    /**
     * Invoked when End Turn button is pressed. Note: be careful when adding
     * checks than have side-effects.
     */
    @Override
    public void pressEndTurnButton() {
        if (game.getTurn() <= C.HOUSE5) {
            // only the last one may have side effects !!!
            if (game.getRegency().needToVote(game.getTurn(), game.getEfs_ini(), game.getYear())) {
                gui.showInfoWindow("My Lord, we must cast our votes before the day is done!");
                return;
            }
            if (game.getRegency().needToAssignOffices(game)) {
                gui.showInfoWindow("My Lord, as newly elected Regents we must assign "
                        + "all the offices before the day is done!");
                return;
            }
            if (game.getFaction(game.getTurn()).haveUnresolvedContracts()) {
                gui.showInfoWindow("My Lord, all contracts must be resolved "
                        + "before the day is done!");
                return;
            }
            // ***** NOTE: since this has side-effects it must be last ! *****
            if (game.getFaction(game.getTurn()).balanceBudget(true) < 0) {
                gui.showInfoWindow("My Lord, we must find a way to pay our debts "
                        + "before the day is done!");
                return;
            }
        }

//        int[] tmp = game.getRegency().getVotes()[game.getTurn()];
//        System.out.println(" " + tmp[0] + "," + tmp[1]);
        // pledged votes are cast here
        game.getRegency().needToVote(game.getTurn(), game.getEfs_ini(), game.getYear(), Regency.VoteCheck.FINAL);
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
