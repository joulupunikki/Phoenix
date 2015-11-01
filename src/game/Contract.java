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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import util.Util;

/**
 * A proposed agreement between two factions. Formulated in the Diplomacy Window
 * by one party and then sent as a Message attachment to the other party. If the
 * receiving party declines the contract then nothing happens. If the receiving
 * party accepts the contract then the terms of the contract take immediate
 * effect.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class Contract implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Term> terms;
    private boolean resolved;
    private int sender;

    public Contract() {
        terms = new LinkedList<>();
        resolved = false;
    }

    public void addTerm(Term term) {
        terms.add(term);
    }

    public void clear() {
        terms.clear();
    }

    /**
     * @return the terms
     */
    public List<Term> getTerms() {
        return terms;
    }

    /**
     * @return the resolved
     */
    public boolean isResolved() {
        return resolved;
    }

//    public boolean accept(Game game, boolean check) {
//        resolved = true;
//        if (check) {
//            return false;
//        } else {
//            acceptDo(game);
//            return true;
//        }
//    }

    public boolean acceptCheck(Game game) throws AssertionError {
        for (Term term : terms) {
            int donor = term.getDonor();
            int recipient = term.getRecipient();
            switch (term.type) {
                case STATE:
                    break;
                case MONEY:
                    if (donor == game.getTurn()
                            && game.getFaction(donor).balanceBudget(false) - term.amount < 0) {
                        return false;
                    }
                    break;
                case VOTES:
                    if (donor == game.getTurn()
                            && game.getRegency().promisedVotes(donor)) {
                        return false;
                    }
                    break;
                case MINISTRY:
                    if (donor == game.getTurn()
                            && game.getDiplomacy().isPromisedMinistry(term.amount)) {
                        return false;
                    }
                    break;

                default:
                    throw new AssertionError();
            }
        }
        return true;
    }

    public void acceptDo(Game game) throws AssertionError {
        resolved = true;
        for (Term term : terms) {
            int donor = term.getDonor();
            int recipient = term.getRecipient();
            switch (term.type) {
                case STATE:
                    game.getDiplomacy().setDiplomaticState(donor, recipient, ((DiplomaticState) term).state);
                    break;
                case MONEY:
                    System.out.println("DBG money " + Util.getFactionName(donor) + " " + -term.amount);
                    game.getFaction(donor).addFirebirds(-term.amount);
                    game.getFaction(recipient).addFirebirds(term.amount);
                    break;
                case VOTES:
                    System.out.println("SetVotes " + donor + "," + recipient);
                    game.getRegency().setVotes(donor, recipient, -1);
                    int[] tmp = game.getRegency().getVotes()[donor];
                    System.out.println(" " + tmp[0] + "," + tmp[1]);
                    break;
                case MINISTRY:
                    game.getDiplomacy().setMinistryPromise(donor, recipient, term.getAmount());
                    break;
                default:
                    throw new AssertionError();
            }
        }
    }

    public void reject() {
        resolved = true;
    }

    /**
     * @param resolved the resolved to set
     */
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    /**
     * @return the sender
     */
    public int getSender() {
        return sender;
    }

    /**
     * @param sender the sender to set
     */
    public void setSender(int sender) {
        this.sender = sender;
    }

//    @Override
//    public String toString() {
//        String s = "";
//        for (Term term : terms) {
//            term.type
//        }
//    }

    public enum Type {

        STATE,
        MONEY,
        VOTES,
        MINISTRY,
    }

    /**
     * Superclass of Contract Terms.
     */
    public static class Term implements Serializable {

        private static final long serialVersionUID = 1L;

        protected Type type;
        private int donor;
        private int recipient;
        protected int amount;

        private Term() {
        }

        public void setFactions(int donor, int recipient) {
            this.donor = donor;
            this.recipient = recipient;
        }

        /**
         * @return the type
         */
        public Type getType() {
            return type;
        }

        /**
         * @return the donor
         */
        public int getDonor() {
            return donor;
        }

        /**
         * @return the recipient
         */
        public int getRecipient() {
            return recipient;
        }

        /**
         * @return the amount
         */
        public int getAmount() {
            return amount;
        }


    }
    
    public static class DiplomaticState extends Term {
        private static final long serialVersionUID = 1L;

        int state;

        public DiplomaticState(int state) {
            this.type = Type.STATE;
            this.state = state;
        }

    }
    
    public static class Money extends Term {
        private static final long serialVersionUID = 1L;

        public Money(int amount) {
            this.type = Type.MONEY;
            this.amount = amount;
        }

    }

    public static class Votes extends Term {

        private static final long serialVersionUID = 1L;

        public Votes() {
            this.type = Type.VOTES;
        }

    }

    public static class Ministry extends Term {

        private static final long serialVersionUID = 1L;

        public Ministry(int ministry) {
            this.type = Type.MINISTRY;
            this.amount = ministry;
        }

    }

//    public static class Resource extends Term {
//        private static final long serialVersionUID = 1L;
//
//        int resource_type;
//
//        public Resource(int amount, int resource_type) {
//            this.type = Type.RESOURCE;
//            this.amount = amount;
//            this.resource_type = resource_type;
//        }
//
//    }

}
