/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Space window no stack selected.
 *
 * @author joulupunikki
 */
public class SW1 extends SW {

    private static SW1 instance = new SW1();

    public SW1() {
    }

    public static State get() {
        return instance;
    }

    public void clickOnSpaceMap(MouseEvent e) {
        //button3
        //on stack
        //on empty square
        //on planet
        //button 1
        //on stack
        //on planet
        
        Point p = SU.getSpaceMapClickPoint(e);

        if (e.getButton() == MouseEvent.BUTTON3) {
            SU.clickOnSpaceMapButton3(p);
        }
        
    }

    public void wheelRotated(MouseWheelEvent e) {
        SU.wheelOnSpaceMap(e);
    }
    

    
}
