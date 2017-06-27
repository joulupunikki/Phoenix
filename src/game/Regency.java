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
import galaxyreader.Unit;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import state.ByzII;
import state.ByzII2;
import util.C;
import util.Util;

/**
 * Holds game data relating to regency, ministerial assignments etc.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class Regency implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int CANDIDATE_IDX = 0;
    private static final int VOTES_IDX = 1;
    // index to int[] ministers, these are equal to value - 10 of the
    // faction id's in util.C;
    public static final int FLEET = 0;
    public static final int GARRISON = 1;
    public static final int EYE = 2;

    /**
     * @return the votes
     */
    public int[][] getVotes() {
        return votes;
    }

    /**
     * @return the promised_ministries
     */
    public int[] getPromisedMinistries() {
        return promised_ministries;
    }

    void initRegency(EfsIni efs_ini) {
        election_level = C.ELECTION_LEVEL.REGENT;
        years_till_elections = efs_ini.regency_term_length;
    }

    public enum VoteCheck {
        ADVANCE,
        NORMAL,
        FINAL,
    }

    // -1 if unassigned, faction ID otherwise
    private int regent = -1;
    private int[] ministers = {-1, -1, -1};
    private int[] promised_ministries;
//    private int garrison = -1;
//    private int eye = -1;
//    private int fleet = -1;
    // how many years till the next scheduled elections
    private int years_till_elections = -1;
    private boolean may_set_offices = false;

    // vote tally for all the houses + league + church
    // votes[faction][CANDIDATE_IDX]: -2 iff not voted yet; -1 iff abstained; else candidate faction ID
    // votes[faction][VOTES_IDX]: number of votes
    private int[][] votes = new int[C.THE_CHURCH + 1][2]; // {{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}};
    // 0 regent elections; 1 first emperor vote; 2 final emperor vote; 3 emperor crowned, game over
    private int election_level = -1;

    private boolean[] allowed_to_vote;

    public Regency() {
        resetVotingRights();
        resetVotes();
        resetPromisedMinistries();
    }

    private void resetVotingRights() {
        allowed_to_vote = new boolean[]{true, true, true, true, true};
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
     * Call this at the start of new year.
     *
     */
    public void advanceElectionTimer() {
        --years_till_elections;
    }

    public void makeThroneClaim() {
        election_level = C.ELECTION_LEVEL.FIRST_EMPEROR;
        years_till_elections = 1;
    }

    /**
     * Cancel ongoing throne claim.
     * @param ini the value of ini
     */
    public void dropThroneClaim(EfsIni ini) {
        election_level = C.ELECTION_LEVEL.REGENT;
        years_till_elections = ini.regency_term_length;
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
        return needToVote(faction, ini, year, VoteCheck.NORMAL);
    }

    /**
     * If check equals VoteCheck.ADVANCE then we check for elections next year
     * If check equals VoteCheck.FINAL then we assign pledged votes
     *
     * @param ini
     * @param faction
     * @param year
     * @param check
     * @return
     */
    public boolean needToVote(int faction, EfsIni ini, int year, VoteCheck check) {
        int years_till_elections = this.years_till_elections;
        if (check == VoteCheck.ADVANCE) {
            years_till_elections--;
        }
        if (faction <= C.THE_CHURCH) {
            if (years_till_elections == 0) {

                if (votes[faction][CANDIDATE_IDX] == -2) { // has not voted yet
                    return true; 
                }
                if (check == VoteCheck.FINAL && votes[faction][VOTES_IDX] == -1) { // pledged votes must be cast
                    votes[faction][VOTES_IDX] = ByzII2.scepterCount();
                }
            }
        }
        return false;
    }

    /**
     * Houses are allowed to vote only if fulfill the requirements.
     *
     * @param house
     * @return
     */
    public boolean allowedToVote(int house) {
        //FIXME church and league voting rights ?
        return house < C.NR_HOUSES && allowed_to_vote[house];
    }
    
    public void setVotes(int faction, int candidate, int nr_votes) {
        votes[faction][CANDIDATE_IDX] = candidate;
        votes[faction][VOTES_IDX] = nr_votes;
    }

    private void resetVotes() {
        for (int[] voter : votes) {
            voter[CANDIDATE_IDX] = -2;
            voter[VOTES_IDX] = -2;
        }
    }

    /**
     * Resolves elections.
     *
     * @param game
     */
    public void resolveElections(Game game) {
        may_set_offices = false;
        if (years_till_elections < 0) {
            switch (years_till_elections) {
                case -1:
                    break;
                default:
                    throw new AssertionError();
            }
        } else {
            return;
        }
        String message = "Election results:\n";
        // First: count votes
        int[] vote_count = new int[C.NR_HOUSES];
        for (int[] voter : votes) {
            if (voter[CANDIDATE_IDX] > -1) {
                vote_count[voter[CANDIDATE_IDX]] += voter[VOTES_IDX];
            }
        }
        for (int i = 0; i < C.NR_HOUSES; i++) {
            message += " " + Util.getFactionName(i) + " " + vote_count[i] + "\n";
        }
        int max_votes = 0;
        int vote_winner = -1;
        for (int i = 0; i < vote_count.length; i++) {
            if (vote_count[i] > max_votes) { // new frontrunner
                max_votes = vote_count[i];
                vote_winner = i;
            } else if (vote_count[i] == max_votes) { // a draw
                vote_winner = -1;
            }
        }
        // Second: decide results based on votes and election level
        switch (election_level) {
            case C.ELECTION_LEVEL.REGENT:
                years_till_elections = game.getEfs_ini().regency_term_length;
                if (vote_winner > -1) { // a new regent
                    setRegent(vote_winner);
                    may_set_offices = true;
                    message += " Lord of " + Util.getFactionName(vote_winner) + " is the new Regent.";

                } else {
                    message += " No one had a majority of votes, so the Regent remains the same.";
                }
                break;
            case C.ELECTION_LEVEL.FIRST_EMPEROR:
            case C.ELECTION_LEVEL.FINAL_EMPEROR:
                if (vote_winner == -1 && vote_count[regent] == max_votes) {
                    message += " The vote was a tie with the throne claimant. The claim is dropped and the claimant remains a Regent.";
                    dropThroneClaim(game.getEfs_ini());
                } else if (vote_winner == -1 && vote_count[regent] < max_votes) {
                    message += " The vote was a tie without the throne claimant. The claim is dropped and the regency is vacated.";
                    dropThroneClaim(game.getEfs_ini());
                    regent = -1;
                } else if (vote_winner == regent) {
                    switch (election_level) {
                        case C.ELECTION_LEVEL.FIRST_EMPEROR:// 1st vote
                            message += " The claimant " + Util.getFactionName(vote_winner)
                                    + " has gathered the support necessary to become emperor."
                                    + " Other houses have " + game.getEfs_ini().regency_term_length
                                    + " years before the rest of humanity recognizes this claim.";
                            years_till_elections = game.getEfs_ini().regency_term_length;
                            break;
                        case C.ELECTION_LEVEL.FINAL_EMPEROR: // final vote
                            message += " In the final vote, the claimant " + Util.getFactionName(vote_winner)
                                    + " has gathered the support necessary to become emperor. \n\n"
                                    + "All hail The Imperial Majesty, Lord of Lords of "
                                    + Util.factionNameDisplay(vote_winner) + ", Emperor of the Fading Suns !";
                            election_level = C.ELECTION_LEVEL.EMPEROR_CROWNED;
                            break;
                        default:
                            throw new AssertionError();
                    }
                } else {
                    setRegent(vote_winner);
                    may_set_offices = true;
                    message += "The claimant loses the election. Lord of " + Util.getFactionName(vote_winner) + " is the new Regent.";
                    dropThroneClaim(game.getEfs_ini());
                }
                break;
            default:
                throw new AssertionError();
        }
        for (Faction faction : game.getFactions()) {
            faction.addMessage(new Message(message, C.Msg.ELECTION_RESULTS, game.getYear(), null));
        }
        if (may_set_offices) { // enforce ministry promises if any
            int[] tmp = game.getDiplomacy().getMinistryPromises(regent);
            System.arraycopy(tmp, 0, promised_ministries, 0, tmp.length);
        }
        game.getDiplomacy().zeroMinistryPromises();
        resetVotes();
        resetVotingRights();

    }
    
    private boolean haveVotes() {
        for (int[] faction : votes) {
            if (faction[CANDIDATE_IDX] > -2 && faction[VOTES_IDX] > -1) {
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

    public int getYearsTillElections() {
        return years_till_elections;
    }

    /**
     * @return the crowned_emperor
     */
    public int getElectionLevel() {
        return election_level;
    }

    void record(PrintWriter pw) {
        pw.println("regency " + regent + "," + ministers[0] + "," + ministers[1] + "," + ministers[2] + "," + election_level);

    }

    /**
     * @param crowned_emperor the crowned_emperor to set
     */
    public void setCrownedEmperor(int crowned_emperor) {
        this.regent = crowned_emperor;
        this.election_level = C.ELECTION_LEVEL.EMPEROR_CROWNED;
    }

    boolean promisedVotes(int a) {
        boolean ret_val = false;
        if (votes[a][CANDIDATE_IDX] > -2) {
            ret_val = true;
        }
        return ret_val;
    }

    public void enforcePromisedMinistries() {
        for (int i = 0; i < promised_ministries.length; i++) {
            switch (promised_ministries[i]) {
                case C.FLEET:
                    setFleet(i);
                    break;
                case C.THE_SPY:
                    setEye(i);
                    break;
                case C.STIGMATA:
                    setGarrison(i);
                    break;
                default:
                    break;
            }
            if (promised_ministries[i] > -1) {
                ByzII.setAssets(promised_ministries[i], i);
            }

        }
    }

    public boolean isPromisedMinistry(int ministry) {
        for (int promised_ministry : promised_ministries) {
            if (promised_ministry == ministry) {
                return true;
            }
        }
        return false;
    }

    public final void resetPromisedMinistries() {
        promised_ministries = new int[]{-1, -1, -1, -1, -1};
    }

    /**
     *
     * @param game
     * @return
     */
    public boolean checkRightToVote(Game game) {
        int faction = game.getTurn();
        if (faction >= C.NR_HOUSES) {
            return true;
        }
        List<Unit> units = game.getUnits();
        for (Unit unit : units) {
            if (unit.type == C.NOBLE_UNIT_TYPE && unit.prev_owner == faction && unit.p_idx == C.BYZ_II_P_IDX) {
                return true;
            }
        }
        allowed_to_vote[faction] = false;
        return false;
    }

    /**
     * Temporary code to ensure correctness of game object when loading saves
     * after modifications which change save game structure have been made.
     *
     * @param game
     */
    void adjustForNewSaveVersion(Game game) {
        this.election_level = 0;
        this.years_till_elections = 1;
        resetVotingRights();
    }
}
