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

import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Hex;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import util.C;
import util.Util;

/**
 * Agora Window Buy
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class AW extends State {

    private static AW instance = new AW();

    private AW() {
    }

    public static State get() {
        return instance;
    }

    @Override
    public void pressBuySellButton() {
        int[] amounts = gui.getAgoraWindow().getAmounts();
        int[] sells = gui.getAgoraWindow().getSells();
        List<Unit> agora_stack = game.getSelectedStack();
        Hex h = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(agora_stack.get(0).x, agora_stack.get(0).y);
        Set<Hex> hexes = Util.getHexesWithinRadiusOf(h, 1);
        hexes.remove(h);
        h = null;
        List<Integer> manifest = new LinkedList<>();
        for (int i = 0; i < amounts.length; i++) {
            if (amounts[i] > 0) {
                manifest.add(i);
            }
        }
        for (Hex hex : hexes) {
            Structure struct = hex.getStructure();
            List<Unit> stack = hex.getStack();
            if ((struct == null || struct.owner == game.getTurn())
                    && (stack.isEmpty() || (stack.get(0).owner == game.getTurn())
                    && stack.size() + manifest.size() <= C.STACK_SIZE)) {
                h = hex;
                break;
            }
        }
        if (h == null) {
            gui.showInfoWindow("We cannot sell resources to you because there is nowhere to put it. ("
                    + "At least one hex next to the Agora must have enough room for as many new pods "
                    + "as the number of different resources you bought.)");
            pressCancelButton();
            return;
        }
        for (int i = 0; i < amounts.length; i++) {
            if(amounts[i] > 0) {
                for (Unit u : agora_stack) {
                    if (u.type == C.CARGO_UNIT_TYPE && u.res_relic == i) {
                        //u.amount -= amounts[i];
                        game.getResources().adjustPodResources(u, -amounts[i]);
                        game.getFaction(game.getTurn()).addFirebirds(amounts[i] * sells[i] * (-1));
                        game.getResources().addOneResourceTypeToHex(game.getCurrentPlanetNr(), h.getX(), h.getY(), game.getTurn(), game.getTurn(), i, amounts[i]);
                    }
                        
                }
            }  
        }
        for (Iterator<Unit> iterator = agora_stack.iterator(); iterator.hasNext();) {
            Unit next = iterator.next();

            if (next.type == C.CARGO_UNIT_TYPE && next.amount < 1) {
                iterator.remove();
                game.deleteUnitNotInCombat(next);
            }
        }
        pressCancelButton();
    }

    @Override
    public void pressCancelButton() {
        SU.restoreMainWindow();
        gui.setCurrentState(main_game_state);
        main_game_state = null;
    }

}
