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
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import util.C;
import util.Util;

/**
 * Space window stack selected no destination selected.
 *
 * @author joulupunikki
 */
public class SW2 extends SW {

    private static SW2 instance = new SW2();

    public SW2() {
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
        Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
        Planet planet = galaxy_grid[sel.x][sel.y].parent_planet;

        List<Unit> stack = planet.space_stacks[game.getSelectedFaction()];
        if (stack.get(0).owner != game.getTurn()) {
            return;
        }
        int x2 = planet.x;
        int y2 = planet.y;

        if (galaxy_grid[x1][y1].planet != null) {
            if (planet.equals(galaxy_grid[x1][y1].planet)) {
                SU.spaceToPlanet(planet);
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
        } else {
            Planet planet2 = galaxy_grid[x1][y1].parent_planet;
            int attacked_faction = galaxy_grid[x1][y1].stack_owner;
            int faction = game.getSelectedFaction();
            if (planet2 == null || !planet.equals(planet2)
                    || !game.checkMoveLeftSpace(stack)) {
                return;
            }

            if (attacked_faction >= C.LEAGUE) {

                List<Integer> stack_list = new LinkedList<>();
                int factions = 3;
                for (int i = 0; i < factions; i++) {
                    if (!galaxy_grid[x1][y1].parent_planet.space_stacks[attacked_faction + i].isEmpty()) {

                        if (attacked_faction + i != faction) {
                            stack_list.add(new Integer(attacked_faction + i));
                        }
                    }

                }

                int size = stack_list.size();

                if (size == 0) {
                    return;
                } else if (size == 1) {
                    attacked_faction = stack_list.get(0).intValue();
                } else {

                    Object[] options = new Object[size];
                    int[] faction_nrs = new int[size];
                    for (int i = 0; i < size; i++) {
                        int tmp = stack_list.get(i).intValue();
                        options[i] = Util.getFactionName(tmp);
                        faction_nrs[i] = tmp;
                    }

                    int j_options = -1;

                    if (size == 2) {
                        j_options = JOptionPane.YES_NO_OPTION;

                    } else {
                        j_options = JOptionPane.YES_NO_CANCEL_OPTION;
                    }

                    int n = JOptionPane.showOptionDialog(gui,
                            "Who do you want to attack?",
                            "",
                            j_options,
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            options,
                            options[0]);

                    int selected_faction = -1;

                    switch (n) {
                        case JOptionPane.YES_OPTION:
                            selected_faction = faction_nrs[0];
                            break;
                        case JOptionPane.NO_OPTION:
                            selected_faction = faction_nrs[1];
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            selected_faction = faction_nrs[2];
                            break;
                        default:
                            selected_faction = faction_nrs[0];
                            break;
                    }

                    attacked_faction = selected_faction;

                }
            }

            if (attacked_faction == faction || planet.space_stacks[attacked_faction].isEmpty()) {
                return;
            }

            System.out.println("attacked_faction = " + attacked_faction);

            game.resolveGroundBattleInit(C.SPACE_COMBAT, attacked_faction);
            SU.showCombatWindow();

        }

    }
}
