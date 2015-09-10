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

import galaxyreader.Unit;
import game.Hex;
import game.PlanetGrid;
import game.Square;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import util.C;

/**
 * Planet map bombardment.
 *
 * @author joulupunikki
 */
public class Bomb extends State {

    private static Bomb instance = new Bomb();

    public Bomb() {
    }

    public static State get() {
        return instance;
    }

    public void clickOnPlanetMap(MouseEvent e) {
        Point p = SU.getPlanetMapClickPoint(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            clickOnPlanetMapButton1(p);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            clickOnPlanetMapButton3(p);
        }
    }

    public void clickOnPlanetMapButton1(Point p) {
        Point q = game.getSelectedPoint();
        Point faction = game.getSelectedFaction();
        List<Unit> stack = null;

        Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
        stack = galaxy_grid[q.x][q.y].parent_planet.space_stacks[faction.y];
        List<Unit> selected = new LinkedList<>();
        for (Unit unit : stack) {
            if (unit.selected) {
                selected.add(unit);
            }
        }
        PlanetGrid planet_grid = game.getPlanetGrid(game.getCurrentPlanetNr());
        Hex target_hex = planet_grid.getHex(p.x, p.y);
        List<Unit> target_stack = target_hex.getStack();
        int tile_set = game.getPlanet(game.getCurrentPlanetNr()).tile_set_type;
        boolean[] terr_types = target_hex.getTerrain();
        if (target_stack.isEmpty()) {
            
            String msg = "";
            if (tile_set == 4) {
                msg = "You wasted some wasteland !";
            } else if (terr_types[C.OCEAN]) {
                msg = "You vaporized a lot of water !";
            } else if (terr_types[C.TREE]) {
                msg = "You leveled a forest !";
            } else if (terr_types[C.HILL] || terr_types[C.MOUNTAIN]) {
                msg = "You turned highlands into lowlands !";
            } else if (terr_types[C.GRASS] || terr_types[C.ARID_GRASS]) {
                msg = "You roasted some grass !";
            } else {
                msg = "You created some mighty fireworks !";
            }
            game.subMovePointsSpace(selected);
            JOptionPane.showMessageDialog(gui, msg, null, JOptionPane.PLAIN_MESSAGE);
            for (Unit selected1 : selected) {
                if (selected1.move_points == 0) {
                    pressSpaceButton();
                }
                break;
            }
            return;
        }

//        Structure struct = target_hex.getStructure();
//        if (struct != null && (struct.type == C.RUINS || struct.type == C.ALIEN_RUINS)) {
//            JOptionPane.showMessageDialog(gui, "Cannot land on ruins.", null, JOptionPane.PLAIN_MESSAGE);
//            return;
//        }

        if (!target_stack.isEmpty() && target_stack.get(0).owner == faction.x) {
            return;
        }
        // bombard and schedule PTS
        game.subMovePointsSpace(selected);
        target_hex.spot(faction.x);
        game.startBombardOrPTS(target_hex);
        game.resolveGroundBattleInit(C.BOMBARD_COMBAT, target_stack.get(0).owner);
        gui.setMouseCursor(C.S_CURSOR_SCEPTOR);
        gui.setCurrentState(CWB1.get());
        SU.showCombatWindowBombard();
//        if (game.landStack(p)) {
//            gui.setMenus(true);
//            gui.setMouseCursor(C.S_CURSOR_SCEPTOR);
//            gui.setCurrentState(PW2.get());
//        }
    }

    public static void clickOnPlanetMapButton3(Point p) {
        int map_point_x = p.x;
        int map_point_y = p.y;

//        List<Unit> stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(map_point_x, map_point_y).getStack();
//        if (!stack.isEmpty()) {
//            game.setSelectedPointFaction(new Point(map_point_x, map_point_y), -1, null, null);
//            stack.get(0).selected = true;
//            gui.setCurrentState(PW2.get());
//        }
        //if destination selected gui.setCurrentState(PW3.get());
        int map_origin_x = map_point_x - C.PLANET_MAP_ORIGIN_X_OFFSET;
        int map_origin_y = map_point_y - C.PLANET_MAP_ORIGIN_Y_OFFSET;

        // roll-over x at x = 44
        if (map_origin_x < 0) {
            map_origin_x = C.PLANET_MAP_WIDTH - 1 + map_origin_x;
        } else if (map_origin_x > 43) {
            map_origin_x = map_origin_x - C.PLANET_MAP_WIDTH;
        }

        // limit y to between 0 and (32 - 10)
        if (map_origin_y < 0) {
            map_origin_y = 0;
        } else if (map_origin_y > 32 - 10) {
            map_origin_y = 32 - 10;
        }

        game.setMapOrigin(new Point(map_origin_x, map_origin_y));

        //draw new map location
        gui.getPlanetWindow().repaint();

        //System.out.println("map_X, map_y: " + map_point_x + ", " + map_point_y);
    }

    public void wheelRotated(MouseWheelEvent e) {
        if (e.getWheelRotation() > 0) {
            pressSpaceButton();
        }

    }

    public void pressSpaceButton() {
        SU.setWindow(C.S_STAR_MAP);
        gui.setMouseCursor(C.S_CURSOR_SCEPTOR);
        gui.setCurrentState(SW2.get());
    }
}
