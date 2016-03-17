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

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import util.C;

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
//    // elections: votes promised
//    private int[] vote_mat;
    // elections: ministries promised
    private int[][] ministry_mat;
    // damages caused in firebirds due to hostile actions
    private int[][] compensation_matrix;

    private int[] agora_prices;

    private List<Contract> sent_contracts;
    private Game game;

    private Diplomacy() {
    }

    public Diplomacy(Game game) {
        war_matrix = new int[C.NR_FACTIONS][C.NR_FACTIONS];
//        vote_mat = new int[C.THE_CHURCH + 1];
        ministry_mat = new int[C.NR_HOUSES][C.NR_HOUSES];
        compensation_matrix = new int[C.NR_FACTIONS][C.NR_FACTIONS];
        agora_prices = new int[C.NR_RESOURCES];
        sent_contracts = new LinkedList<>();
        this.game = game;
    }

    public void initDiplomacy() {
        for (int i = 0; i < C.NR_FACTIONS; i++) {
            if (i < C.NR_HOUSES) {
                setDiplomaticState(i, C.NEUTRAL, C.DS_WAR);
            }
            setDiplomaticState(i, C.SYMBIOT, C.DS_WAR);

        }
//        zeroVotePromises();
        zeroMinistryPromises();
        compensation_matrix = new int[C.NR_FACTIONS][C.NR_FACTIONS];
        setAgoraPrices();
    }

    private void setAgoraPrices() {
        for (int i = 0; i < agora_prices.length; i++) {
            agora_prices[i] = game.getResTypes()[i].price;;
        }
    }

//    private void zeroVotePromises() {
//        for (int i = 0; i < vote_mat.length; i++) {
//            vote_mat[i] = -1;
//        }
//    }

    public void zeroMinistryPromises() {
        for (int[] mm : ministry_mat) {
            for (int i = 0; i < mm.length; i++) {
                mm[i] = -1;
            }
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

    private int getSetMinistryPromise(int donor, int recipient, int state, boolean set) {
        if (set) {
            switch (state) {
                case C.FLEET:
                case C.THE_SPY:
                case C.STIGMATA:
                    break;
                default:
                    throw new AssertionError("Invalid ministry " + state);
            }
            ministry_mat[donor][recipient] = state;
        }
        return ministry_mat[donor][recipient];
    }

//    private int getSetVotePromise(int faction_a, int faction_b, boolean set) {
//        if (set) {
//            vote_mat[faction_a] = faction_b;
//        }
//        return vote_mat[faction_a];
//    }

    public void setDiplomaticState(int faction_a, int faction_b, int state) {
        getSetDiplomaticState(faction_a, faction_b, state, true);
    }
    
    public int getDiplomaticState(int faction_a, int faction_b) {
        return getSetDiplomaticState(faction_a, faction_b, -1, false);
    }

    public void setMinistryPromise(int donor, int recipient, int state) {
        getSetMinistryPromise(donor, recipient, state, true);
    }

    public int getMinistryPromise(int donor, int recipient) {
        return getSetMinistryPromise(donor, recipient, -1, false);
    }

//    public void setVotePromise(int faction_a, int faction_b) {
//        getSetVotePromise(faction_a, faction_b, true);
//    }
//
//    public int getVotePromise(int faction_a) {
//        return getSetVotePromise(faction_a, -1, false);
//    }

    void record(PrintWriter pw) {
        pw.println( "diplomacy");
    }

    /**
     * @return the sent_contracts
     */
    public List<Contract> getSentContracts() {
        return sent_contracts;
    }

    public void addSentContract(Contract c) {
        sent_contracts.add(c);
    }

    public int[] getMinistryPromises(int faction) {
        return ministry_mat[faction];
    }

    boolean isPromisedMinistry(int amount) {
        for (int n : ministry_mat[game.getTurn()]) {
            if (n == amount) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param villain
     * @param victim
     * @return the compensation_matrix
     */
    public int getCompensationMatrix(int villain, int victim) {
        return compensation_matrix[villain][victim];
    }

    /**
     * @param villain
     * @param victim
     * @param amount
     */
    public void addToCompensationMatrix(int villain, int victim, int amount) {
        compensation_matrix[villain][victim] += amount;
    }

    /**
     * @param villain
     * @param victim
     */
    public void zeroCompensationMatrix(int villain, int victim) {
        compensation_matrix[villain][victim] = 0;
    }

    /**
     * @return the agora_prices
     */
    public int[] getAgora_prices() {
        return agora_prices;
    }
}
