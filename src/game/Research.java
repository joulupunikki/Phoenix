/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package game;

/**
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class Research {
    public boolean[] techs;
    public int[] points;
    public int researched;
    public int points_left;

    public Research(Game game) {
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
    
    
}
