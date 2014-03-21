/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import java.awt.CardLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Planet window no stack selected.
 *
 * @author joulupunikki
 */
public class PW1 extends PW {

    private static PW1 instance = new PW1();

    public PW1() {
    }

    public static State get() {
        return instance;
    }

    public void clickOnPlanetMap(MouseEvent e) {

        // if button 3
        //on stack
        //on city
        //on empty hex
        // if button 1
        //on hex
        Point p = SU.getPlanetMapClickPoint(e);

        if (e.getButton() == MouseEvent.BUTTON3) {
            SU.clickOnPlanetMapButton3(p);
        }

    }

    public void clickOnPlanetWindow(MouseEvent e) {
        Point p = e.getPoint();
        int res = SU.isOnResourceIcon(p);
        if (res > -1) {
            SU.clickOnResourceIcon(res);
        }
    }

    public void wheelRotated(MouseWheelEvent e) {
        SU.wheelOnPlanetMap(e);

    }

}
