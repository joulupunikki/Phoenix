/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import dat.Tech;
import dat.UnitSpot;
import java.io.Serializable;

/**
 * Loaded data used by the game object.
 *
 * @author joulupunikki
 */
public class GameResources implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private double[][][] unit_spot;
    private Tech[] techs;

    public GameResources() {

        unit_spot = UnitSpot.readUnitSpot();
        techs = Tech.readTech();
//        Tech.print(techs);
//        System.exit(0);
    }

    public double[][][] getUnitSpot() {
        return unit_spot;
    }

    public Tech[] getTech() {
        return techs;
    }
}
