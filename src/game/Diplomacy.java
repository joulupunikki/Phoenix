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
package game;

import java.io.File;
import java.io.Serializable;
import util.C;
import util.Util;

/**
 * Diplomacy related data structures and algorithms.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class Diplomacy implements Serializable {

    private static final long serialVersionUID = 1L;

    // holds the diplomatic state between each faction:
    // currently WAR/PEACE
    private int[][] war_matrix;
    private Game game;

    private Diplomacy() {
    }

    public Diplomacy(Game game) {
        war_matrix = new int[C.NR_FACTIONS][C.NR_FACTIONS];
        this.game = game;
    }

    public void initDiplomacy() {
        for (int i = 0; i < C.NR_FACTIONS; i++) {
            if (i < C.NR_HOUSES) {
                setDiplomaticState(i, C.NEUTRAL, C.DS_WAR);
            }
            setDiplomaticState(i, C.SYMBIOT, C.DS_WAR);

        }
    }

    /**
     * @return the war_matrix
     */
    public int[][] getWarMatrix() {
        return war_matrix;
    }

    /**
     * @param war_matrix the war_matrix to set
     */
    public void setWarMatrix(int[][] war_matrix) {
        this.war_matrix = war_matrix;
    }

    private int getSetDiplomaticState(int faction_a, int faction_b, int state, boolean set) {
        if (faction_a > faction_b) {
            int tmp = faction_a;
            faction_a = faction_b;
            faction_b = tmp;
        }
        if (set) {
            switch (state) {
                case C.DS_PEACE:
                case C.DS_WAR:
                    break;
                default:
                    throw new AssertionError("Invalid diplomatic state " + state);
            }
            war_matrix[faction_a][faction_b] = state;
        }
        return war_matrix[faction_a][faction_b];
    }

    public void setDiplomaticState(int faction_a, int faction_b, int state) {
        getSetDiplomaticState(faction_a, faction_b, state, true);
    }
    
    public int getDiplomaticState(int faction_a, int faction_b) {
        return getSetDiplomaticState(faction_a, faction_b, -1, false);
    }

    void record(File file) {
        Util.printString(file, "diplomacy");
    }
}
