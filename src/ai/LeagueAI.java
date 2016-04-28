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
package ai;

import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Game;
import game.Hex;
import java.util.LinkedList;
import java.util.List;
import util.C;
import util.Util;

/**
 * League AI base class.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class LeagueAI extends AI {
    private static final long serialVersionUID = 1L;

    /**
     * Agora restock interval in years.
     */
    protected static final int RESTOCK_INTERVAL = 2;

    /**
     * Agora restock values.
     */
    protected static final int[] AGORA_RESTOCK = {
        500,
        200,
        200,
        100,
        100,
        50,
        50,
        50,
        50,
        25,
        25,
        10,
        2
    };

    @Override
    protected void findAssets(int faction) {
        for (Structure s : all_structures) {
            if (s.type == C.MONASTERY || s.type == C.ALIEN_RUINS || s.type == C.RUINS) {
                continue;
            }
            if (s.owner == faction) {
                structures.add(s);
            } else if (planets.get(s.p_idx).spotted[faction]) {
                enemy_structures.add(s);
            }
        }
    }

    /**
     * Restock all League agoras every RESTOCK_INTERVAL years. Excess pods are
     * silently deleted and replaced with pods of missing resources if
     * necessary.
     */
    protected void restockAgoras() {
        System.out.println("Check agora restock ...");
        if ((game.getYear() - C.STARTING_YEAR + 1) % RESTOCK_INTERVAL != 0) {
            System.out.println("... no agora restock");
            return;
        }
        System.out.println("... restocking");
        for (Structure s : structures) {
            if (s.type != C.AGORA) {
                continue;
            }
            System.out.println(" Agora at " + s.p_idx + "," + s.x + "," + s.y);
            Hex hex = game.getHexFromPXY(s.p_idx, s.x, s.y);
            List<Unit> stock = hex.getStack();
            boolean[] have = new boolean[C.NR_RESOURCES];
            List<Unit> overflow = new LinkedList<>();

            for (Unit u : stock) {
                if (u.type == C.CARGO_UNIT_TYPE) {
                    if (have[u.res_relic]) {
                        overflow.add(u);
                        continue;
                    }
                    have[u.res_relic] = true;
                    int adjust = AGORA_RESTOCK[u.res_relic] - u.amount;                   
                    game.getResources().adjustPodResources(u, adjust);
                }
            }
            for (Unit u : overflow) {
                game.deleteUnitNotInCombat(u);
            }
            for (int i = 0; i < have.length; i++) {
                if (!have[i]) {
                    game.createUnitInHex(s.p_idx, s.x, s.y, C.LEAGUE, C.LEAGUE, C.CARGO_UNIT_TYPE, 0, i, AGORA_RESTOCK[i]);
                }
            }
        }
    }

    public enum UTypes {

        PSYCH,
        CLOSE,
        DIRECT,
        INDIRECT,
        AIR,
        NESTER,
        CLOSE_SP,
        DIRECT_SP,
        RANGED_SP,
        CARGO_SP,
    }
    public LeagueAI(Game game) {
        super(game, C.LEAGUE);
        Util.dP("##### LeagueAI init begin");
        Util.dP("##### LeagueAI init end");
    }

    @Override
    public void doTurn() {
        findAssets(C.LEAGUE);
        considerPeaceOffers();
        restockAgoras();
        //logSuper(C.NEUTRAL, "Start");
        // list stacks
        //findAssets(C.NEUTRAL);
        // list known enemy cities

        // attack enemy cities
        // attack enemy units
    }

}
