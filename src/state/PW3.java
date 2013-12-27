/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import game.Hex;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.LinkedList;
import util.Util;

/**
 * Planet window stack selected destination selected.
 *
 * @author joulupunikki
 */
public class PW3 extends PW {

    private static PW3 instance = new PW3();

    public PW3() {
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

        if (e.getButton() == MouseEvent.BUTTON1) {
            clickOnPlanetMapButton1(p);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            SU.clickOnPlanetMapButton3(p);
        }

    }

    public void clickOnPlanetWindow(MouseEvent e) {
        Point p = e.getPoint();
        if (SU.isOnStackDisplay(p)) {
            SU.clickOnStackDisplay(e);
        }
    }

//        public void clickOnStackDisplay(MouseEvent e) {
//        if (e.getButton() == MouseEvent.BUTTON1) {
//            SU.clickOnStackDisplayButton1(e);
//        } else if (e.getButton() == MouseEvent.BUTTON3) {
//            // display stack window
////            gui.showStackWindow();
//            SU.showUnitInfoWindow();
//        }
//    }
    public void wheelRotated(MouseWheelEvent e) {
        SU.wheelOnPlanetMap(e);
    }

    public void clickOnPlanetMapButton1(Point p) {
        LinkedList<Hex> path = game.getPath();
        if (p.x == path.getLast().getX() && p.y == path.getLast().getY()) {

            if (Util.moveCapable(game)) {
                
                
                
                // start stack movement loop
                    
                
                gui.setCurrentState(PW4.get());
                gui.setMenus(false);
                gui.startStackMove();
            }

            //
        } else {
            SU.findPath(p);
            if (game.getPath() == null) {
                gui.setCurrentState(PW2.get());
            }
        }
    }


    
    @Override
    public void pressLaunchButton() {
        SU.pressLaunchButtonSU();
    }
    

    
}
