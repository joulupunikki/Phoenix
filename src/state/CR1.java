/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
        gui.getMessages().repaint();
        gui.setCurrentState(CR2.get());
    }

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
