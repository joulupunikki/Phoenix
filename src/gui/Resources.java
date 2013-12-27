/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import util.C;
import util.UtilG;

/**
 *
 * @author joulupunikki
 */
public class Resources {
    private int[] color_scaler;

    public Resources(Gui gui) {
        
        color_scaler = UtilG.scaleColorsToDark(C.GRAY_SCALING_FACTOR, gui.getPallette());
    }
    public int[] getColorScaler() {
        return color_scaler;
    }
    
}
