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

import galaxyreader.Unit;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;
import util.C;
import util.Util;

/**
 * Byzantium II Window Casting Votes
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class ByzII2 extends State {

    private static ByzII2 instance = new ByzII2();

    private ByzII2() {
    }

    public static State get() {
        return instance;
    }

    @Override
    public void pressExitButton() {
        gui.setCurrentState(ByzII.get());
        gui.showInfoWindow("Voting canceled.");
        gui.getByzantium_ii_window().enableAbstainButton(false);
        gui.getByzantium_ii_window().enableVoteButton(true);
    }

    @Override
    public void pressAbstainButton() {
        if (gui.showConfirmWindow("Are you sure you wish to abstain ?")) {
            gui.setCurrentState(ByzII.get());
            game.getRegency().setVotes(game.getTurn(), -1, scepterCount());
            gui.getByzantium_ii_window().enableAbstainButton(false);
        }
    }

    @Override
    public void clickOnByzantiumIIWindow(MouseEvent e) {
        Point p = e.getPoint();
        int pos = ByzII.getPosition(p);
        if (C.HOUSE1 <= pos && pos <= C.HOUSE5) {
            System.out.println("Vote for " + Util.getFactionName(pos));
            if (gui.showConfirmWindow("Are you sure you want to cast your votes for House " + Util.getFactionName(pos))) {
                game.getRegency().setVotes(game.getTurn(), pos, scepterCount());
                gui.setCurrentState(ByzII.get());
                gui.getByzantium_ii_window().enableAbstainButton(false);
            }
        }
    }

    public static int scepterCount() {
        int count = 0;
        List<Unit> units = game.getUnits();
        for (Unit unit : units) {
            if (unit.type == C.SCEPTER_UNIT_TYPE && unit.owner == game.getTurn()) {
                count++;
            }
        }
        return count;
    }
}
