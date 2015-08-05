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

import java.awt.Point;
import java.awt.event.MouseEvent;
import static state.State.ws;
import util.C;
import util.Util;

/**
 * Planet Window PW, superclass of all Planet Windows
 *
 * @author joulupunikki
 */
public class PW extends MW {

//    public void pressNextStackButton() {
//        SU.pressNextStackButtonSU();
//    }
//
//    public void pressSkipStackButton() {
//        SU.pressSkipStackButtonSU();
//    }
//
//    public void pressEndTurnButton() {
//        if (game.getEfs_ini().pbem) {
//            game.setSelectedPoint(null, -1);
//            game.setSelectedFaction(-1);
//            gui.saveGame();
//            return;
//        }
//        game.endTurn();
//        game.setPath(null);
//
//        SU.selectNextUnmovedUnit();
//
//    }
    public void pressSpaceButton() {
        SU.pressSpaceButtonSU();
    }

    public void clickOnGlobeMap(MouseEvent e) {

        Point p = e.getPoint();
        int x = p.x / (ws.globe_map_width / C.PLANET_MAP_WIDTH);
        int y = p.y / (ws.globe_map_height / (C.PLANET_MAP_COLUMNS - 1));
        x = x - 6;
        y = y - 4;

        Point q = Util.forcePlanetMapCoordinates(new Point(x, y));

        game.setMapOrigin(q);
    }

    public void clickOnPlanetWindow(MouseEvent e) {
        Point p = e.getPoint();
        if (SU.isOnStackDisplay(p)) {
            SU.clickOnStackDisplay(e);
        } else {
            int res = SU.isOnResourceIcon(p);
            if (res > -1) {
                SU.clickOnResourceIcon(res);
            }
        }

    }

//    public void pressNextStackButton() {
//        System.out.println("this = " + this);
//        Point p = game.getSelectedPoint();
//        int faction = game.getSelectedFaction();
//        int current_planet = game.getCurrentPlanetNr();
//
//        List<Unit> unmoved_units = game.getUnmovedUnits();
//        if (unmoved_units.isEmpty()) {
//            JOptionPane.showMessageDialog(gui, "You have moved all of your units.", null, JOptionPane.PLAIN_MESSAGE);
//            return;
//        }
//        if (p != null) {
//            List<Unit> stack = null;
//            if (faction == -1) {
//                stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y).getStack();
//            } else {
//                Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
//                stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[faction];
//            }
//            List<Unit> selected = new LinkedList<>();
//            for (Unit unit : stack) {
//                if (unit.selected) {
//                    selected.add(unit);
//                }
//            }
//
//            boolean is_cargo_listing = false;
//            Iterator<Unit> iterator = selected.listIterator();
//            Iterator<Unit> cargo_it = null;
//
//            Unit e = iterator.next();
//            for (int i = 0; i < C.STACK_SIZE; i++) {
//
//                unmoved_units.remove(e);
//                if (is_cargo_listing) {
//                    e = cargo_it.next();
//                    if (!cargo_it.hasNext()) {
//                        cargo_it = null;
//                        is_cargo_listing = false;
//                    }
//                } else if (e.cargo_list.isEmpty()) {
//                    if (iterator.hasNext()) {
//                        e = iterator.next();
//                    } else {
//                        break;
//                    }
//                } else {
//                    cargo_it = e.cargo_list.listIterator();
//                    e = cargo_it.next();
//                    if (cargo_it.hasNext()) {
//                        is_cargo_listing = true;
//                    }
//                }
//
//            }
//
//
//        }
//
//
//        Unit unit = unmoved_units.get(0);
//        int x = unit.x;
//        System.out.println("x = " + x);
//        int y = unit.y;
//        System.out.println("y = " + y);
//        Point point = new Point(x, y);
//        faction = -1;
//        if (unit.in_space) {
//            System.out.println("unit.in_space = " + unit.in_space);
////            point = game.resolveSpaceStack(new Point(x, y), unit.prev_owner);
//            faction = unit.owner;
//            System.out.println("faction = " + faction);
//        }
//        game.setCurrentPlanetNr(unit.p_idx);
//        System.out.println("unit.p_idx = " + unit.p_idx);
//        game.setSelectedPointFaction(point, faction, null, null);
//        game.setSelectedPoint(point, faction);
//        game.setSelectedFaction(faction);
//        String name = game.getPlanet(unit.p_idx).name;
//        System.out.println("name = " + name);
//
//        p = game.getSelectedPoint();
//        System.out.println("p = " + p);
//        List<Unit> stack = null;
//        if (faction == -1) {
//            stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y).getStack();
//        } else {
//            Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
//            stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[faction];
//            System.out.println("stack = " + stack);
//        }
//
//        boolean is_cargo_listing = false;
//        Iterator<Unit> iterator = stack.listIterator();
//        Iterator<Unit> cargo_it = null;
//
//        Unit e = iterator.next();
//        for (int i = 0; i < C.STACK_SIZE; i++) {
//            System.out.println("i = " + i);
//            e.selected = true;
//            if (is_cargo_listing) {
//                e = cargo_it.next();
//                if (!cargo_it.hasNext()) {
//                    cargo_it = null;
//                    is_cargo_listing = false;
//                }
//            } else if (e.cargo_list.isEmpty()) {
//                if (iterator.hasNext()) {
//                    e = iterator.next();
//                } else {
//                    break;
//                }
//            } else {
//                cargo_it = e.cargo_list.listIterator();
//                e = cargo_it.next();
//                if (cargo_it.hasNext()) {
//                    is_cargo_listing = true;
//                }
//            }
//
//        }
//
//        
//        if (unit.in_space) {
//            Point smo = Util.resolveSpaceMapOrigin(new Point(unit.x, unit.y), ws);
//            game.setSpaceMapOrigin(smo);
//            System.out.println(" Star map");
//            SU.setWindow(C.S_STAR_MAP);
//            gui.setCurrentState(SW2.get());
//        } else {
//            game.setMapOrigin(Util.resolvePlanetMapOrigin(new Point(unit.x - C.PLANET_MAP_ORIGIN_X_OFFSET,
//                    unit.y - C.PLANET_MAP_ORIGIN_Y_OFFSET)));
//            SU.setWindow(C.S_PLANET_MAP);
//            gui.setCurrentState(PW2.get());
//        }
//    }
}
