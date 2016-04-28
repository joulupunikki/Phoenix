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
import game.Hex;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.util.FastMath;
import util.C;
import util.Util;

/**
 * Agora Window Sell. Maximum of 999 of each resource (one full pod) will be
 * stored in an Agora, excess resources will disappear into the bottomless
 * pockets of the gnomes of <strike>Zurich</strike> the League. Full price is
 * however paid to seller so no value will be lost. If you sell thousands of a
 * resource and then decide that you need it back fast, it is going to take a
 * few turns to buy it all back.
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class AW2 extends State {

    private static AW2 instance = new AW2();

    private AW2() {
    }

    public static State get() {
        return instance;
    }

    @Override
    public void pressBuySellButton() {
        int[] amounts = gui.getAgoraWindow().getAmounts();
        int[] buys = gui.getAgoraWindow().getBuys();
        Hex h = gui.getAgoraWindow().getAgoraHex();
        List<Unit> agora_stack = h.getStack();
        List<Unit> sell_stack = Util.getSelectedUnits(game.getSelectedStack());

        for (int i = 0; i < amounts.length; i++) {
            if (amounts[i] > 0) {
                for (Unit u : sell_stack) {
                    if (u.type == C.CARGO_UNIT_TYPE && u.res_relic == i && u.amount > 0) {
                        //u.amount -= amounts[i];
                        int amount = FastMath.min(amounts[i], u.amount); // fixes #80
                        amounts[i] -= amount;
                        game.getResources().adjustPodResources(u, -amount);
                        game.getFaction(sell_stack.get(0).owner).addFirebirds(amount * buys[i]);
                        boolean res_exists = false;
                        Unit agora_unit = null;
                        for (Unit agora_stack1 : agora_stack) {
                            if (agora_stack1.type == C.CARGO_UNIT_TYPE && agora_stack1.res_relic == i) {
                                res_exists = true;
                                agora_unit = agora_stack1;
                                break;
                            }
                        }
                        if (res_exists) {
                            game.getResources().adjustPodResources(agora_unit, FastMath.min(amount, 999 - agora_unit.amount));
                        } else {
                            game.createUnitInHex(game.getCurrentPlanetNr(), h.getX(), h.getY(), C.LEAGUE, C.LEAGUE, C.CARGO_UNIT_TYPE, 0, i, amount);
                        }
                    }
                }
                for (Iterator<Unit> iterator = sell_stack.iterator(); iterator.hasNext();) {
                    Unit next = iterator.next();

                    if (next.type == C.CARGO_UNIT_TYPE && next.amount < 1) {
                        iterator.remove();
                        game.deleteUnitNotInCombat(next);
                    }
                }
            }
        }
        pressCancelButton();
        
    }

    @Override
    public void pressCancelButton() {
        if (game.getSelectedStack().isEmpty()) {
            game.setSelectedPointFaction(null, -1, null, null);
            gui.setCurrentState(PW1.get());
        } else {
            gui.setCurrentState(main_game_state);
        }
        SU.restoreMainWindow();
        main_game_state = null;
    }

}
