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

/**
 * Holds game data relating to regency, ministerial assignments etc.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class Regency implements Serializable {
    private static final long serialVersionUID = 1L;

    // -1 if unassigned, faction ID otherwise
    private int regent = -1;
    private int garrison = -1;
    private int eye = -1;
    private int fleet = -1;

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

}
