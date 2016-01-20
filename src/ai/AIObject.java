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
import java.io.Serializable;
import util.C;

/**
 * Acts as a bridge between Game and AI. Persistently stores certain static AI
 * variables.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class AIObject implements Serializable {

    private static final long serialVersionUID = 1L;
    private AI[] ai;

    public AIObject() {
        ai = new AI[C.NR_FACTIONS];
    }
    
    public void adAI(Game game, int faction) {
        switch (faction) {
            case C.SYMBIOT:
                ai[faction] = new SymbiotAI(game);
                break;
            case C.NEUTRAL: // rebels
                break;
            default:
                throw new AssertionError();
        }
    }

    public boolean isAIcontrolled(int faction) {
        return ai[faction] != null;
    }

    public void doTurn(int faction) {
        ai[faction].doTurn();
    }

}
