/*
 * Copyright (C) 2015 joulupunikki joulupunikki@gmail.communist.invalid.
 *
 *  Disclaimer of Warranties and Limitation of Liability.
 *
 *     The creators and distributors offer this software as-is and
 *     as-available, and make no representations or warranties of any
 *     kind concerning this software, whether express, implied, statutory,
 *     or other. This includes, without limitation, warranties of title,
 *     merchantability, fitness for a particular purpose, non-infringement,
 *     absence of latent or other defects, accuracy, or the presence or
 *     absence of errors, whether or not known or discoverable.
 *
 *     To the extent possible, in no event will the creators or distributors
 *     be liable on any legal theory (including, without limitation,
 *     negligence) or otherwise for any direct, special, indirect,
 *     incidental, consequential, punitive, exemplary, or other losses,
 *     costs, expenses, or damages arising out of the use of this software,
 *     even if the creators or distributors have been advised of the
 *     possibility of such losses, costs, expenses, or damages.
 *
 *     The disclaimer of warranties and limitation of liability provided
 *     above shall be interpreted in a manner that, to the extent possible,
 *     most closely approximates an absolute disclaimer and waiver of
 *     all liability.
 *
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
import util.C;
import util.Util;

/**
 * Space window stack selected destination selected
 *
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
                    List<Unit> stack = planet.space_stacks[game.getSelectedFaction().y];
                    for (Unit unit : stack) {
                        if (unit.isSelected() && unit.move_points > 0
                                && (unit.move_type == C.MoveType.JUMP || unit.move_type == C.MoveType.LANDER)) {
                            jump_capable = true;
                        } else if (unit.isSelected()) {
                            jump_capable = false;
                            break;
                        }
                    }
                    if (!jump_capable) {
                        gui.showInfoWindow("Cannot make the jump...you have selected units without jump capability.");
                        return;
                    }
                    if (game.moveSpaceStack(p)) {
                        game.setJumpPath(null);
                        game.setSelectedPoint(p, game.getSelectedFaction().y);
                        Planet p2 = galaxy_grid[x1][y1].planet;
                        Point smo = Util.resolveSpaceMapOrigin(new Point(p2.x, p2.y), ws);
                        game.setSpaceMapOrigin(smo);
                        gui.setCurrentState(SW2.get());
                        gui.getSpaceWindow().repaint();
                    } else {
                        //info window too many units in target area
                        gui.showInfoWindow("Too many units in the destination area.");
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
