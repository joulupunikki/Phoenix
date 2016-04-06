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
        LOYALTY_H_X,
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

        // faction relations, lower left
        STATE_X, STATE_Y, STATE_W, STATE_S,
        // loyalty effect panel
        LOYALTY_P_X, LOYALTY_P_Y, LOYALTY_P_W, LOYALTY_P_H, LOYALTY_P_TAX_Y, LOYALTY_P_PAY_Y,
    }

    /**
     * Diplomacy Selector gui coordinates
     */
    public enum CD {

        WIN_X,
        WIN_Y,
        WIN_W,
        WIN_H,
        WIN_H_X,
        WIN_H_Y,
        ROW1_X,
        ROW1_Y,
        ROW2_X,
        ROW2_Y,
        ROW_W,
        ROW_H_Y,
        EXIT_X,
        EXIT_Y,
        EXIT_W,
        EXIT_H,


    }

    /**
     * Diplomacy Window gui coordinates, Resolve Contract Window gui
     * coordinates.
     */
    public enum CDW {
        GIVE_H_Y,
        GIVE_X,
        GIVE_Y,
        GIVE_W,
        GIVE_H,
        ROW_H,
        TAKE_Y,
        // Resolve Contract Window coordinates
        R_ACCEPT_X,
        R_ACCEPT_Y,
        R_ACCEPT_W,
        R_ACCEPT_H,
        R_BUTTON_W, R_WIN_Y, R_WIN_H, R_RESPONCE_Y, R_RESPONCE_X, DMG_X, DMG_Y,
    }

    /**
     * GalaxyWindow and GlobeWindow coordinates.
     */
    public enum CGW {

        MAP_X,
        MAP_Y,
        MAP_W,
        MAP_H,
        BUT_X,
        BUT_Y,
        BUT_W,
        BUT_H,
        SQUARE, MAP_MARGIN,
        // Grand Planetary map
        PMAP_X,
        PMAP_Y,
        PMAP_W,
        PMAP_H,
        PBUT_X,
        PBUT_Y,
        PBUT_W,
        PBUT_H, PNAME_X, PNAME_Y,

    }

    public enum OW {

        WIN_X,
        WIN_Y,
        WIN_W,
        WIN_H,
        COL1_X,
        COL1_Y,
        EXIT_X,
        EXIT_Y,
        EXIT_W,
        EXIT_H, BOX_W, BOX_H,
    }

    /**
     * Combat Strategy Selector coordinates
     */
    public enum SS {
        WIN_X, WIN_Y, WIN_W, WIN_H, BUTTON_X, BUTTON_Y, BUTTON_W, BUTTON_H, ROW_H, HEADER_X, HEADER_Y

    }

}
