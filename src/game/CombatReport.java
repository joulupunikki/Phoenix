/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import galaxyreader.Unit;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to record combat data necessary to show combat replays.
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class CombatReport implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public List<Unit> attacker;
    public List<Unit> defender;
    public int[] atk_post_health;
    public boolean[] atk_rout;
    public int[] def_post_health;
    public boolean[] def_rout;

    public CombatReport(int nr_atk, int nr_def) {
        attacker = new LinkedList<>();
        defender = new LinkedList<>();
        atk_post_health = new int[nr_atk];
        atk_rout = new boolean[nr_atk];
        def_post_health = new int[nr_def];
        def_rout = new boolean[nr_def];
    }

}
