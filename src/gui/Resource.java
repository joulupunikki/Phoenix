/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import dat.EfsIni;
import dat.TerColor;
import dat.UnitSpot;
import java.awt.Color;
import java.util.Properties;
import util.C;
import util.UtilG;

/**
 *
 * @author joulupunikki
 */
public class Resource {

    private int[] color_scaler;
    private double[][][] unit_spot;
    private Color[] colors;
    private int[][] ter_color;
    private Properties efs_ini;
    
    public Resource(Gui gui) {

        color_scaler = UtilG.scaleColorsToDark(C.GRAY_SCALING_FACTOR, gui.getPallette());
        unit_spot = UnitSpot.readUnitSpot();
        setColors(gui.getPallette());
        ter_color = TerColor.readTerColor();
        efs_ini = EfsIni.readEFSINI();
    }

    public void setColors(byte[][] pallette) {
        colors = new Color[pallette[0].length];
        for (int i = 0; i < pallette[0].length; i++) {

            int red = (int) pallette[2][i] & 0xff;
            int green = (int) pallette[1][i] & 0xff;
            int blue = (int) pallette[0][i] & 0xff;

            colors[i] = new Color(red, green, blue);
        }
    }

    public Color getColor(int color) {
        return colors[color];
    }
    
    public int[] getColorScaler() {
        return color_scaler;
    }

    public double[][][] getUnitSpot() {
        return unit_spot;
    }
    
    public int[][] getTerColor() {
        return ter_color;
    }
    
    public Properties getEFSIni() {
        return efs_ini;
    }
}
