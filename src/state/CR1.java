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

import galaxyreader.Unit;
import game.CombatReport;
import java.util.List;

/**
 * Combat Replay 1
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class CR1 extends State {

    private static CR1 instance = new CR1();

    public CR1() {
    }

    public static State get() {
        return instance;
    }

    public void pressFightButton() {
        updateStacks();
        // not needed, Battle already holds the references
//        game.getBattle().setCombatStacks(report.attacker, report.defender);
        if (gui.getMessages().findNextReplay(-2) == -1) {
            gui.getCombatWindow().toggleButtons(true, false, false);
        } else {
            gui.getCombatWindow().toggleButtons(true, true, false);
        }
        gui.getCombatWindow().setFightText("Next Replay");
        gui.getCombatWindow().repaint();
        gui.setCurrentState(CR2.get());
    }

    /**
     * Swaps combat report unit stats between pre and post combat states.
     */
    public static void updateStacks() {
        CombatReport report = gui.getMessages().getCombatReport();
        List<Unit> stack = report.attacker;
        int[] health = report.atk_post_health;
        boolean[] rout = report.atk_rout;
        updateStats(stack, health, rout);
        stack = report.defender;
        health = report.def_post_health;
        rout = report.def_rout;
        updateStats(stack, health, rout);
    }

    public static void updateStats(List<Unit> stack, int[] health, boolean[] rout) {

        int idx = 0;
        for (Unit unit : stack) {
            int tmp = unit.health;
            unit.health = health[idx];
            health[idx] = tmp;
            boolean tmp2 = unit.routed;
            unit.routed = rout[idx];
            rout[idx] = tmp2;
            idx++;
        }
    }

}
