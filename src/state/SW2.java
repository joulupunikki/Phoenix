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
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import static state.State.gui;
import util.C;
import util.Util;
import util.UtilG;

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

    @Override
    public void clickOnSpaceMap(MouseEvent e) {
        Point p = SU.getSpaceMapClickPoint(e);
        if (e.getButton() == MouseEvent.BUTTON3) {
            SU.clickOnSpaceMapButton3(p);
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            clickOnSpaceMapButton1(p);
        }
    }

    @Override
    public void clickOnSpaceWindow(MouseEvent e) {
        Point p = e.getPoint();
        if (SU.isOnStackDisplay(p)) {
            SU.clickOnStackDisplay(e);
        }
    }

    @Override
    public void wheelRotated(MouseWheelEvent e) {
        SU.wheelOnSpaceMap(e);
    }

    public void clickOnSpaceMapButton1(Point p) {
        int x1 = p.x;
        int y1 = p.y;
        Point sel = game.getSelectedPoint();
        Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
        Planet planet = galaxy_grid[sel.x][sel.y].parent_planet;

        List<Unit> stack = planet.space_stacks[game.getSelectedFaction().y];
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
            int attacked_slot = galaxy_grid[x1][y1].stack_owner;
            int attacking_faction = game.getSelectedFaction().x;
            if (planet2 == null || !planet.equals(planet2)
                    || !game.checkMoveLeftSpace(stack)) {
                return;
            }

            if (attacked_slot >= C.LEAGUE) {

                List<Integer> stack_list = new LinkedList<>();
                int factions = 3;
                for (int i = 0; i < factions; i++) {
                    if (!planet2.space_stacks[attacked_slot + i].isEmpty()) {

                        if (planet2.space_stacks[attacked_slot + i].get(0).owner != attacking_faction) {
                            stack_list.add(new Integer(attacked_slot + i));
                        }
                    }

                }

                int size = stack_list.size();

                if (size == 0) {
                    return;
                } else if (size == 1) {
                    attacked_slot = stack_list.get(0).intValue();
                } else {

                    String[] options = new String[size];
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

                    JOptionPane pane = new UtilG.PhoenixJOptionPane("Who do you want to attack?",
                            JOptionPane.PLAIN_MESSAGE, j_options,
                            null, options, options[0]);
                    JDialog dialog = pane.createDialog(gui, null);
                    dialog.setVisible(true);
                    String n = (String) pane.getValue();
                    int selected_faction = -1;
                    if (n == null || n.equals(options[0])) {
                        selected_faction = faction_nrs[0];
                    } else if (n.equals(options[1])) {
                        selected_faction = faction_nrs[1];
                    } else if (size == 3 && n.equals(options[2])) {
                        selected_faction = faction_nrs[2];
                    } else {
                        selected_faction = faction_nrs[0];
                    }

                    attacked_slot = selected_faction;

                }
            }
            // why is this here ? all the checks are performed allready ...
            if (attacked_slot == attacking_faction || planet.space_stacks[attacked_slot].isEmpty()) {
                return;
            }

            System.out.println("attacked_slot = " + attacked_slot);

            if (!SU.byzIICombatOK(stack, true)) {
                return;
            }

            if (game.getDiplomacy().getDiplomaticState(game.getTurn(), planet.space_stacks[attacked_slot].get(0).owner) != C.DS_WAR
                    && !gui.showAttackConfirmWindow(game.getTurn(), planet.space_stacks[attacked_slot])) {
                return;
            }
            game.getDiplomacy().setDiplomaticState(game.getTurn(), planet.space_stacks[attacked_slot].get(0).owner, C.DS_WAR);
            game.resolveGroundBattleInit(C.SPACE_COMBAT, attacked_slot);
            SU.showCombatWindow();

        }

    }
}
