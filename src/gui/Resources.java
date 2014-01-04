/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import dat.UnitSpot;
import util.C;
import util.UtilG;

/**
 *
 * @author joulupunikki
 */
public class Resources {

    private int[] color_scaler;
    private double[][][] unit_spot;

    public Resources(Gui gui) {

        color_scaler = UtilG.scaleColorsToDark(C.GRAY_SCALING_FACTOR, gui.getPallette());
        unit_spot = UnitSpot.readUnitSpot();

    }

    public int[] getColorScaler() {
        return color_scaler;
    }

    public double[][][] getUnitSpot() {
        return unit_spot;
    }
}
