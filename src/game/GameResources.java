/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package game;

import dat.UnitSpot;
import java.io.Serializable;

/**
 * Loaded data used by the game object.
 * @author joulupunikki
 */
public class GameResources implements Serializable{
        private double[][][] unit_spot;

    public GameResources() {

        unit_spot = UnitSpot.readUnitSpot();

    }

    public double[][][] getUnitSpot() {
        return unit_spot;
    }
}

