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

import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Hex;
import game.PlanetGrid;
import game.Square;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.LinkedList;
import java.util.List;
import util.C;
import util.Util;

/**
 * Planet map landing spaceships.
 *
 * @author joulupunikki
 */
public class LAND1 extends STP {

    private static LAND1 instance = new LAND1();

    private Point landing_point;

    public LAND1() {
    }

    public Point popLandingPoint() {
        Point p = landing_point;
        landing_point = null;
        return p;
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
            if (unit.isSelected()) {
                selected.add(unit);
            }
        }
        PlanetGrid planet_grid = game.getPlanetGrid(game.getCurrentPlanetNr());
        Hex target_hex = planet_grid.getHex(p.x, p.y);
        List<Unit> target_stack = target_hex.getStack();
//        double[][][] terr_cost = game.getTerrCost();
        int tile_set = game.getPlanet(game.getCurrentPlanetNr()).tile_set_type;
//        double move_cost = 1;
        boolean[] terr_types = target_hex.getTerrain();
//        move_cost = 1;
//        for (int k = 0; k < terr_types.length; k++) {
//            if (terr_types[k] == true) {
//                move_cost *= terr_cost[k][tile_set][l];
////                        System.out.println("Move_cost: " + move_cost);
//            }
//
//        }
        if (terr_types[0] && tile_set != 4) {
            gui.showInfoWindow("Cannot land on water.");
            return;
        }
        Structure struct = target_hex.getStructure();
        if (struct != null && (struct.type == C.RUINS || struct.type == C.ALIEN_RUINS)) {
            gui.showInfoWindow("Cannot land on ruins.");
            return;
        }
        if (!target_stack.isEmpty() && target_stack.get(0).owner == faction.x && target_stack.get(0).prev_owner != faction.y) { // fix #70
            gui.showInfoWindow("Cannot merge loaned stacks.");
            return;
        }
        if (!target_stack.isEmpty() && target_stack.get(0).owner != faction.x && !game.isNonCombat(target_stack)) {
            gui.showInfoWindow("Cannot land on another's units.");
            return;
        }
        if (Util.stackSize(selected) + Util.stackSize(target_stack) > 20) {
            gui.showInfoWindow("Too many units in target area.");
            return;
        }
                
        // start PTS defence fire if any
        if (SU.byzIICombatOK(stack, false)) {
            game.startBombardOrPTS(target_hex, true, -1);
            List<Hex> pts_queue = game.getBattle().getPTSQueue();
            if (!pts_queue.isEmpty()) {
                // save state so we can land in state.CWPTS2 after PTS
                saveMainGameState();
                landing_point = p;
                Hex pts_hex = pts_queue.remove(0);
                // since we are landing, do not try to bomb landing hex
                game.startBombardOrPTS(pts_hex, false, -1);
                pts_hex.spot(game.getTurn());
                game.resolveGroundBattleInit(C.PTS_COMBAT, pts_hex.getStack().get(0).owner);
                gui.setMouseCursor(C.S_CURSOR_SCEPTOR);
                SU.showCombatWindowPTS();
                return;
            }
        }
        if (game.landStack(p)) {
            gui.setMenus(C.S_PLANET_MAP);
            gui.setMouseCursor(C.S_CURSOR_SCEPTOR);
            gui.setCurrentState(PW2.get());
        }
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
