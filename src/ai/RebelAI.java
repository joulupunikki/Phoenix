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

import game.Game;
import util.C;
import util.Util;

/**
 * Symbiot AI base class. In EFS1.4 symbiots have no tech, their all_units have
 * no cost other than build time, and the only structure capable of building
 symbiot all_units is the hive. Also, the symbiot prime directives are 1.
 * Violent conquest of all the planets. 2. Never surrender. Also, they are a supporting
 faction. With these premises, writing a simple but reasonably working Symbiot
 AI should be among the easiest faction AI writing tasks for Phoenix.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class RebelAI extends AI {
    private static final long serialVersionUID = 1L;

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
    public RebelAI(Game game) {
        super(game);
        Util.dP("##### RebelAI init begin");
        Util.dP("##### RebelAI init end");
    }

    @Override
    public void doTurn() {
        logSuper(C.NEUTRAL);
        // list stacks
        findAssets(C.NEUTRAL);
        // list known enemy cities

        // attack enemy cities
        // attack enemy units
    }

}
