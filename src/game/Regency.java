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
import java.util.LinkedList;
import java.util.List;
import state.ByzII;
import util.C;
import util.Util;

/**
 * Holds game data relating to regency, ministerial assignments etc.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class Regency implements Serializable {
    private static final long serialVersionUID = 1L;

    private final static int CANDIDATE_IDX = 0;
    private static final int VOTES_IDX = 1;
    // index to int[] ministers, these are equal to value - 10 of the
    // faction id's in util.C;
    public static final int FLEET = 0;
    public static final int GARRISON = 1;
    public static final int EYE = 2;
    

    // -1 if unassigned, faction ID otherwise
    private int regent = -1;
    private int[] ministers = {-1, -1, -1};
//    private int garrison = -1;
//    private int eye = -1;
//    private int fleet = -1;
    // -1 if no ongoing throne claim
    private int years_since_throne_claim = -1;
    private boolean may_set_offices = false;

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
        return ministers[GARRISON];
    }

    /**
     * @param garrison the garrison to set
     */
    public void setGarrison(int garrison) {
        assertOfficer(garrison);
        ministers[GARRISON] = garrison;
    }

    /**
     * @return the eye
     */
    public int getEye() {
        return ministers[EYE];
    }

    /**
     * @param eye the eye to set
     */
    public void setEye(int eye) {
        assertOfficer(eye);
        ministers[EYE] = eye;
    }

    /**
     * @return the fleet
     */
    public int getFleet() {
        return ministers[FLEET];
    }

    /**
     * @param fleet the fleet to set
     */
    public void setFleet(int fleet) {
        assertOfficer(fleet);
        ministers[FLEET] = fleet;
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
        assertOfficer(regent);
        this.regent = regent;
    }

    /**
     * Advance age of throne claim by one iff throne was just claimed or throne
     * claim is in force. Call this when throne is claimed and at the start of
     * new year.
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
     * Need to vote during regent election year, or first year or last year
     * (term length) after year of throne claim.
     *
     * @param faction current faction
     * @param ini
     * @param year current year
     * @return
     */
    public boolean needToVote(int faction, EfsIni ini, int year) {
        return needToVote(faction, ini, year, false);
    }

    /**
     *
     * @param ini
     * @param faction
     * @param year
     * @param advance_notice
     * @return
     */
    public boolean needToVote(int faction, EfsIni ini, int year, boolean advance_notice) {
        int years_since_throne_claim = this.years_since_throne_claim;
        if (advance_notice) {
            years_since_throne_claim++;
        }
        int term_length = ini.regency_term_length;
        if (faction <= C.THE_CHURCH) {
            if (vote_tally[faction][CANDIDATE_IDX] == -2) {
                if (years_since_throne_claim < 1 && year != C.STARTING_YEAR && (year - C.STARTING_YEAR) % term_length == 0) {
                    return true; // regent elections
                } else if (years_since_throne_claim == 1 || years_since_throne_claim == term_length + 1) {
                    return true; // thone claim
                }
            }
        }
        return false;
    }

    public void setVotes(int faction, int candidate, int votes) {
        vote_tally[faction][CANDIDATE_IDX] = candidate;
        vote_tally[faction][VOTES_IDX] = votes;
    }

    private void resetVoteTally() {
        for (int[] vote_tally1 : vote_tally) {
            vote_tally1[CANDIDATE_IDX] = -2;
            vote_tally1[VOTES_IDX] = -2;
        }
    }

    public void resolveElections(Game game) {
        may_set_offices = false;
        if (haveVotes()) {
            String message = "Election results:";
            // count votes
            int[] vote_count = new int[C.NR_HOUSES];
            for (int[] vote_tally1 : vote_tally) {
                if (vote_tally1[CANDIDATE_IDX] > -1) {
                    vote_count[vote_tally1[CANDIDATE_IDX]] += vote_tally1[VOTES_IDX];
                }
            }
            for (int i = 0; i < C.NR_HOUSES; i++) {
                message += " " + Util.getFactionName(i) + " " + vote_count[i] + ";";
            }
            int max_votes = 0;
            int candidate = -1;
            for (int i = 0; i < vote_count.length; i++) {
                if (vote_count[i] > max_votes) { // new frontrunner
                    max_votes = vote_count[i];
                    candidate = i;
                } else if (vote_count[i] == max_votes) { // a draw
                    candidate = -1;
                }
            }
            if (years_since_throne_claim < 2) { // regent elections

                if (candidate > -1) { // a new regent
                    setRegent(candidate);
                    may_set_offices = true;
                    message += " Lord of " + Util.getFactionName(candidate) + " is the new Regent.";

                } else {
                    message += " No one had a majority of votes, so the Regent remains the same.";
                }
            } else { // emperor vote
                if (candidate == -1 && vote_count[regent] == max_votes) {
                    message += " The vote was a tie with the throne claimant. The claim is dropped and the claimant remains a Regent.";
                    dropThroneClaim();
                } else if (candidate == -1 && vote_count[regent] < max_votes) {
                    message += " The vote was a tie without the throne claimant. The claim is dropped and the regency is vacated.";
                    dropThroneClaim();
                    regent = -1;
                } else if (candidate == regent) {
                    if (years_since_throne_claim == 2) { // 1st vote
                        message += " The claimant " + Util.getFactionName(candidate)
                                + " has gathered the support necessary to become emperor."
                                + " Other houses have " + game.getEfs_ini().regency_term_length
                                + " years before the rest of humanity recognizes this claim.";
                    } else { // final vote
                        message += " In the final vote, the claimant " + Util.getFactionName(candidate)
                                + " has gathered the support necessary to become emperor. The "
                                + Util.factionNameDisplay(candidate) + " has been crowned the Emperor !";
                    }
                } else {
                    setRegent(candidate);
                    may_set_offices = true;
                    message += "The claimant loses the election. Lord of " + Util.getFactionName(candidate) + " is the new Regent.";
                }  
            }
            for (Faction faction : game.getFactions()) {
                faction.addMessage(new Message(message, C.Msg.ELECTION_RESULTS, game.getYear(), null));
            }
            resetVoteTally();
        }
        
    }
    
    private boolean haveVotes() {
        for (int[] vote_tally1 : vote_tally) {
            if (vote_tally1[0] > -2) {
                return true;
            }
        }
        return false;
    }

    private void assertOfficer(int faction) {
        if (faction < -1 || faction > C.HOUSE5) {
            throw new AssertionError("Invalid officer value " + faction);
        }
    }

    /**
     * @return the may_set_offices
     */
    public boolean isMay_set_offices() {
        return may_set_offices;
    }

    /**
     * @param may_set_offices the may_set_offices to set
     */
    public void setMay_set_offices(boolean may_set_offices) {
        this.may_set_offices = may_set_offices;
    }

    public boolean needToAssignOffices(Game game) {
        if (may_set_offices && regent == game.getTurn()) {
            for (int minister : ministers) {
                if (minister == -1) {
                    return true;
                }
            }
        }
        return false;
    }

    public int cycleMinistry(int value, int ministry, Game game) {
        List<Integer> eligibles = new LinkedList<>();
        // these should ensure that no house may have two offices,
        // unless there are only two houses left
        loop:
        for (int i = 0; i < C.NR_HOUSES; i++) {
            if (!game.getFaction(i).isEliminated()) {
                for (int j = 0; j < ministers.length; j++) {
                    if (ministry != j && ministers[j] == i) {
                        System.out.println("Continue");
                        continue loop;
                        
                    }
                }
                System.out.println("Add i " + i);
                eligibles.add(i);
            }
        }
        if (eligibles.isEmpty()) {
            for (int i = 0; i < ministers.length; i++) {
                if (ministers[i] != -1) {
                    eligibles.add(ministers[i]);
                }

            }
        }
        while (!eligibles.contains(++value)) {
            System.out.println("Value " + value);
            if (value > C.HOUSE5) {
                value = -1;
            }
        }
        return value;
    }

    public void purgeEliminatedFromOffices(Game game) {
        if (regent != -1) {
            if (game.getFaction(regent).isEliminated()) {
                regent = -1;
            }
        }
        for (int i = 0; i < ministers.length; i++) {
            if (ministers[i] != -1 && game.getFaction(ministers[i]).isEliminated()) {
                ministers[i] = -1;
                int t = -1;
                switch (i) {
                    case GARRISON:
                        t = C.STIGMATA;
                        break;
                    case EYE:
                        t = C.THE_SPY;
                        break;
                    case FLEET:
                        t = C.FLEET;
                        break;
                    default:
                        throw new AssertionError();
                }
                ByzII.setAssets(t, t);
            } 
        }
    }

    public int getYearsSinceThroneClaim() {
        return years_since_throne_claim;
    }
}
