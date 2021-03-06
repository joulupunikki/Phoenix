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
package state;

import util.C;

/**
 * Main Menu 1, the first screen presented to players.
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class MM1 extends State {

    private static MM1 instance = new MM1();

    public MM1() {
    }

    public static State get() {
        return instance;
    }

    /**
     * Start new game
     */
    @Override
    public void pressStartNew() {
        SU.setWindow(C.S_MAIN_MENU);
        gui.setCurrentState(MM3.get());
    }

    /**
     * Load saved game
     */
    @Override
    public void pressLoadGame() {
        gui.loadGame();
    }

    /**
     * Quit game
     */
    @Override
    public void pressQuit() {
        System.exit(0);
    }

}
