/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import galaxyreader.JumpGate;
import galaxyreader.Planet;
import galaxyreader.Unit;
import game.Square;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import javax.swing.JOptionPane;
import util.C;
import util.Util;

/**
 * Space window stack selected destination selected
 * @author joulupunikki
 */
public class SW3 extends SW {

    private static SW3 instance = new SW3();

    public SW3() {
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
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            clickOnSpaceMapButton1(p);
        }
    }

    public void clickOnSpaceWindow(MouseEvent e) {
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
        SU.wheelOnSpaceMap(e);
    }

    public void clickOnSpaceMapButton1(Point p) {
        int x1 = p.x;
        int y1 = p.y;
        Point sel = game.getSelectedPoint();
        JumpGate jump_path = game.getJumpPath();
        Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
        Planet planet = galaxy_grid[sel.x][sel.y].parent_planet;
        int x2 = planet.x;
        int y2 = planet.y;

        if (galaxy_grid[x1][y1].planet != null) {
            if (planet.equals(galaxy_grid[x1][y1].planet)) {
                game.setJumpPath(null);
                gui.setCurrentState(SW2.get());
                gui.getSpaceWindow().repaint();
            } else {
                if ((jump_path.getX1() == x1 && jump_path.getY1() == y1
                        && jump_path.getX2() == x2 && jump_path.getY2() == y2)
                        || (jump_path.getX1() == x2 && jump_path.getY1() == y2
                        && jump_path.getX2() == x1 && jump_path.getY2() == y1)) {
                    boolean jump_capable = false;
                    List<Unit> stack = planet.space_stacks[game.getSelectedFaction()];
                    for (Unit unit : stack) {
                        if (unit.selected && unit.move_points > 0 &&
                                (unit.move_type == C.MoveType.JUMP || unit.move_type == C.MoveType.LANDER)) {
                            jump_capable = true;
                        } else if (unit.selected) {
                            jump_capable = false;
                            break;
                        }
                    }
                    if (!jump_capable) {
                        JOptionPane.showMessageDialog(gui, "Cannot make the jump...you have selected units without jump capability.", null, JOptionPane.PLAIN_MESSAGE);
                        return;
                    }
                    if (game.moveSpaceStack(p)) {
                        game.setJumpPath(null);
                        game.setSelectedPoint(p, game.getSelectedFaction());
                        Planet p2 = galaxy_grid[x1][y1].planet;
            Point smo = Util.resolveSpaceMapOrigin(new Point(p2.x, p2.y), ws);
            game.setSpaceMapOrigin(smo);
                        gui.setCurrentState(SW2.get());
                        gui.getSpaceWindow().repaint();
                    } else {
                        //info window too many units in target area
                        JOptionPane.showMessageDialog(gui, "Too many units in the destination area.", null, JOptionPane.PLAIN_MESSAGE);
                    }

                } else {


                    List<JumpGate> jump_routes = galaxy_grid[x1][y1].planet.jump_routes;
                    for (JumpGate jg : jump_routes) {
                        System.out.println(jg.getX1() + " " + jg.getY1() + " " + jg.getX2() + " " + jg.getY2());
                        if ((jg.getX1() == x1 && jg.getY1() == y1 && jg.getX2() == x2 && jg.getY2() == y2)
                                || (jg.getX1() == x2 && jg.getY1() == y2 && jg.getX2() == x1 && jg.getY2() == y1)) {
                            game.setJumpPath(jg);
                            gui.setCurrentState(SW3.get());
                            gui.getSpaceWindow().repaint();
                            break;
                        }
                    }
                }
            }
        }
    }
    

    
}
