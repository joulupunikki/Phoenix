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

import galaxyreader.Planet;
import java.io.Serializable;

/**
 * A galaxy map square.
 *
 * @author joulupunikki
 */
public class Square implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public Planet planet;
    public int stack_owner;
//    public List<Unit>[] stacks;
    /**
     * The planet of whose orbit this square represents. Total of 8 such squares
     * around each planet.
     */
    public Planet parent_planet;

    public Square() {
        planet = null;
        stack_owner = -1;
        parent_planet = null;
//        stacks = null;
    }

//    public void placeUnit(Unit e) {        
//        
//    }
    public void setStackData(int stack_owner, Planet parent_planet) {
        this.stack_owner = stack_owner;
        this.parent_planet = parent_planet;
    }

}
