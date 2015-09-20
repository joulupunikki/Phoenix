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
package util;

/**
 * Holds enums which are keys to HashMaps in Window Size holding gui constants.
 * In the keys, last letter X,Y coordinates, W,H witdh, height next to last
 * letter H header, V value
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class G {

    /**
     * House window coordinate keys.
     */
    public enum CH {

        // budget area, lower right

        BUDGET_H_X,
        BUDGET_H_Y,
        BUDGET_H,
        //
        TAX_H_X,
        TAX_H_Y,
        TITHE_SKIM_H_X,
        UNIT_PAY_H_X,
        TAX_V_X,
        TAX_V_W,
        SLIDER_X,
        SLIDER_Y,
        SLIDER_W,
        //
        DEBT_H_X,
        DEBT_H_Y,
        DEBT_H_H,
        DEBT_V_Y,
        // portrait area, upper left
        LEADER_H_X,
        LEADER_H_Y,
        LEADER_H_W,
        LEADER_V_X,
        LEADER_V_Y,

    }
}
