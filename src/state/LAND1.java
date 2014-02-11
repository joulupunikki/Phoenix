/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import javax.swing.JOptionPane;
import util.C;
import util.Util;

/**
 * Planet map landing spaceships.
 *
 * @author joulupunikki
 */
public class LAND1 extends State {

    private static LAND1 instance = new LAND1();

    public LAND1() {
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
        int faction = game.getSelectedFaction();
        List<Unit> stack = null;

        Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
        stack = galaxy_grid[q.x][q.y].parent_planet.space_stacks[faction];
        List<Unit> selected = new LinkedList<>();
        for (Unit unit : stack) {
            if (unit.selected) {
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
            JOptionPane.showMessageDialog(gui, "Cannot land on water.", null, JOptionPane.PLAIN_MESSAGE);
            return;
        }
        Structure struct = target_hex.getStructure();
        if (struct != null && (struct.type == C.RUINS || struct.type == C.ALIEN_RUINS)) {
            JOptionPane.showMessageDialog(gui, "Cannot land on ruins.", null, JOptionPane.PLAIN_MESSAGE);
            return;
        }

        if (!target_stack.isEmpty() && target_stack.get(0).owner != faction) {
            JOptionPane.showMessageDialog(gui, "Cannot land on another's units.", null, JOptionPane.PLAIN_MESSAGE);
            return;
        }

        if (Util.stackSize(selected) + Util.stackSize(target_stack) > 20) {
            JOptionPane.showMessageDialog(gui, "Too many units in target area.", null, JOptionPane.PLAIN_MESSAGE);
            return;
        }
        if (game.landStack(p)) {
            gui.setMenus(true);
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

        System.out.println("map_X, map_y: " + map_point_x + ", " + map_point_y);
    }

    public void wheelRotated(MouseWheelEvent e) {
        if (e.getWheelRotation() > 0) {
            pressSpaceButton();
        }

    }

    public void pressSpaceButton() {
        SU.setWindow(C.S_STAR_MAP);
        gui.setCurrentState(SW2.get());
    }
}
