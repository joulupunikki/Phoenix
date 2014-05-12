/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import galaxyreader.Unit;
import java.awt.CardLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import javax.swing.JPanel;
import util.C;

/**
 * Planet window stack selected no destination selected.
 *
 * @author joulupunikki
 */
public class PW2 extends PW {

    private static PW2 instance = new PW2();

    public PW2() {
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

//    public void clickOnPlanetWindow(MouseEvent e) {
//        Point p = e.getPoint();
//        if (SU.isOnStackDisplay(p)) {
//            SU.clickOnStackDisplay(e);
//        }
//    }

//    public void clickOnStackDisplay(MouseEvent e) {
//        if (e.getButton() == MouseEvent.BUTTON1) {
//            SU.clickOnStackDisplayButton1(e);
//        } else if (e.getButton() == MouseEvent.BUTTON3) {
//            // display stack window
////            gui.showStackWindow();
//            SU.showUnitInfoWindow();
//
//        }
//    }
    public void wheelRotated(MouseWheelEvent e) {
        SU.wheelOnPlanetMap(e);
    }

    public void clickOnPlanetMapButton1(Point p) {
        Point q = game.getSelectedPoint();
        List<Unit> stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(q.x, q.y).getStack();
        if (stack.get(0).owner != game.getTurn()) {
            return;
        }
        if (SU.disembarkNavalCargo(p) || SU.embarkNavalCargo(p)) {
            return;
        }
        SU.findPath(p);
        if (game.getPath() != null) {
            gui.setCurrentState(PW3.get());
        }
    }

    @Override
    public void pressLaunchButton() {
        SU.pressLaunchButtonSU();
    }

    @Override
    public void pressBuildButton() {
        SU.pressBuildButtonSU();
    }

}
