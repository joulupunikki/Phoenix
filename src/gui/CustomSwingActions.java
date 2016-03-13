/*
 * Copyright (C) 2016 joulupunikki joulupunikki@gmail.communist.invalid.
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
package gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import state.SU;

/**
 * Holds keyboard shortcut custom Swing Actions.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class CustomSwingActions {

//    static final String S_SPACE = "pressSpace";
//    static final AbstractAction space = new AbstractAction() {
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            SU.pressNextStackButtonSU();
//        }
//    };

    static final String S_WAIT = "wait";
    static final AbstractAction WAIT = new AbstractAction() {
        private static final long serialVersionUID = 1L;
        @Override
        public void actionPerformed(ActionEvent e) {
            SU.pressNextStackButtonSU();
        }
    };
    static final String S_SENTRY = "sentry";
    static final AbstractAction SENTRY = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("SENTRY");
            SU.pressSentryButtonSU();
        }
    };

    static void setUpKeyBindings(JComponent jcomp) {
        //        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), CustomSwingActions.S_SPACE);
        //        this.getActionMap().put(CustomSwingActions.S_SPACE, CustomSwingActions.space);
        jcomp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), CustomSwingActions.S_WAIT);
        jcomp.getActionMap().put(CustomSwingActions.S_WAIT, CustomSwingActions.WAIT);
        jcomp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), CustomSwingActions.S_SENTRY);
        jcomp.getActionMap().put(CustomSwingActions.S_SENTRY, CustomSwingActions.SENTRY);
    }
}
