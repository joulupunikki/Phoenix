/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import galaxyreader.Structure;
import java.io.Serializable;
import java.util.List;
import util.C;

/**
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class Research implements Serializable {

    public boolean[] techs;
    public int[] points;
    public int researched;
    public int points_left;
    private Game game;

    public Research(Game game) {
        this.game = game;
        int len = game.getResources().getTech().length;
        techs = new boolean[len];
        points = new int[len];
        researched = 0;
        for (int i = 0; i < techs.length; i++) {
            techs[i] = false;
            points[i] = 0;
        }
        techs[0] = true;
    }

    public void setResearch(int tech) {
        researched = tech;
    }

    /**
     * Calculate research points at beginning of turn.
     */
    public void initResearchPts() {
        points_left = 0;
        List<Structure> cities = game.getStructures();
        for (Structure structure : cities) {
            if (structure.owner == game.getTurn() && structure.type == C.LAB) {
                points_left += game.getEfs_ini().lab_points;
            }
        }

        for (int i = 0; i < techs.length; i++) {
            if (techs[i]) {
                points_left -= game.getResources().getTech()[i].stats[C.TECH_COST] / C.TECH_MAINT;
            }

        }

    }

    /**
     * Researches selected tech.
     */
    public void doResearch() {
        if (researched == 0) {
            return;
        }
        points[researched] += points_left;
        int cost = game.getResources().getTech()[researched].stats[C.TECH_COST];
        points_left = points[researched] - cost;
        if (points_left >= 0) {
            techs[researched] = true;
            researched = 0;
        } else {
            points_left = 0;
        }
    }

}
