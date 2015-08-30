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

import dat.EfsIni;
import java.io.Serializable;
import util.C;

/**
 * Holds game data relating to regency, ministerial assignments etc.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class Regency implements Serializable {
    private static final long serialVersionUID = 1L;

    private final static int CANDIDATE_IDX = 0;
    private static final int VOTES_IDX = 1;

    // -1 if unassigned, faction ID otherwise
    private int regent = -1;
    private int garrison = -1;
    private int eye = -1;
    private int fleet = -1;
    // -1 if no ongoing throne claim
    private int years_since_throne_claim = -1;

    // vote tally for all the houses + league + church
    // vote_tally[faction][CANDIDATE_IDX]: -2 iff not voted yet; -1 iff abstained; else candidate faction ID
    // vote_tally[faction][VOTES_IDX]: number of votes
    private int[][] vote_tally = new int[C.THE_CHURCH + 1][2]; // {{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}};

    public Regency() {
        resetVoteTally();
    }

    /**
     * @return the garrison
     */
    public int getGarrison() {
        return garrison;
    }

    /**
     * @param garrison the garrison to set
     */
    public void setGarrison(int garrison) {
        this.garrison = garrison;
    }

    /**
     * @return the eye
     */
    public int getEye() {
        return eye;
    }

    /**
     * @param eye the eye to set
     */
    public void setEye(int eye) {
        this.eye = eye;
    }

    /**
     * @return the fleet
     */
    public int getFleet() {
        return fleet;
    }

    /**
     * @param fleet the fleet to set
     */
    public void setFleet(int fleet) {
        this.fleet = fleet;
    }

    /**
     * @return the regent
     */
    public int getRegent() {
        return regent;
    }

    /**
     * @param regent the regent to set
     */
    public void setRegent(int regent) {
        this.regent = regent;
    }

    /**
     * Advance age of throne claim by one iff throne was just claimed or throne
     * claim is in force.
     *
     * @param just_claimed
     */
    public void advanceThroneClaim(boolean just_claimed) {
        if (years_since_throne_claim > -1 || just_claimed) {
            ++years_since_throne_claim;
        }
    }

    /**
     * Cancel ongoing throne claim.
     */
    public void dropThroneClaim() {
        years_since_throne_claim = -1;
    }

    /**
     * Need to vote during regent election year or first year or last year (term
     * length) after year of throne claim.
     *
     * @param faction current faction
     * @param res gui.Resource, contains regency_term_length
     * @param year current year
     * @return
     */
    public boolean needToVote(int faction, EfsIni ini, int year) {
        int term_length = ini.regency_term_length;
        if (faction <= C.THE_CHURCH) {
            if (vote_tally[faction][CANDIDATE_IDX] == -2
                    && ((years_since_throne_claim == 1 || years_since_throne_claim == term_length)
                    || (year != C.STARTING_YEAR && (year - C.STARTING_YEAR) % term_length == 0))) {
                return true;
            }
        }
        return false;
    }

    public void setVotes(int faction, int candidate, int votes) {
        vote_tally[faction][CANDIDATE_IDX] = candidate;
        vote_tally[faction][VOTES_IDX] = votes;
    }

    public void resetVoteTally() {
        for (int[] vote_tally1 : vote_tally) {
            vote_tally1[CANDIDATE_IDX] = -2;
            vote_tally1[VOTES_IDX] = -2;
        }
    }

    public void resolveElections() {
    }

}
