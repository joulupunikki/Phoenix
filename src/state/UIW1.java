/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import galaxyreader.Unit;
import game.Square;
import gui.Gui;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import util.C;
import util.Util;

/**
 * Unit Info Window no unit dragged
 *
 * @author joulupunikki
 */
public class UIW1 extends State {

    private static UIW1 instance = new UIW1();

    public UIW1() {
    }

    public static State get() {
        return instance;
    }

    public void pressExitButton() {
        SU.restoreMainWindow();
        gui.setCurrentState(main_game_state);
        main_game_state = null;
        gui.setInfo_unit(null);

    }

    public void clickOnWindow(MouseEvent e) {

        Point q = e.getPoint();

        if (e.getButton() == MouseEvent.BUTTON1) {
            clickButton1(q);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            clickButton3(q);
        }
    }

    public void clickButton3(Point q) {

        int faction = game.getSelectedFaction();
        Point p = game.getSelectedPoint();
//        int[][] unit_icons = Gui.getUnitIcons();
//        BufferedImage bi = new BufferedImage(ws.unit_icon_size, ws.unit_icon_size, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
//        WritableRaster wr = bi.getRaster();
//        int[] pixel_data = new int[1];
        List<Unit> stack = null;
        if (faction == -1) {
            stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y).getStack();
        } else {
            Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
            stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[faction];
        }
        if (game.getTurn() != stack.get(0).owner) {

            List<Unit> tmp = new LinkedList<>();
            for (Unit unit : stack) {
                if (unit.spotted[game.getTurn()]) {
                    tmp.add(unit);
                }
            }
            stack = tmp;
        }
        boolean is_cargo_listing = false;
        Iterator<Unit> iterator = stack.listIterator();
        Iterator<Unit> cargo_it = null;
        Unit u = iterator.next();
//        Graphics2D g2d = (Graphics2D) g;

        for (int i = 0; i < C.STACK_WINDOW_UNITS_Y; i++) {
            for (int j = 0; j < C.STACK_WINDOW_UNITS_X; j++) {
//                g.setColor(Color.BLACK);
//                g.fillRect((int) (ws.unit_panel_x_offset + j * 3.5 * ws.unit_icon_size),
//                        (int) (ws.unit_panel_y_offset + i * 1.15 * ws.unit_icon_size),
//                        3 * ws.unit_icon_size,
//                        ws.unit_icon_size);

//                int color = Util.getOwnerColor(u.owner);
//                if (u.selected) {
//                    color += 3;
//                }
////                System.out.println("color = " + color);
//                Util.fillRaster(wr, color);
//                Util.drawUnitIconEdges(wr, ws);
//                Util.writeUnit(pixel_data, u.type, unit_icons, wr, ws);
                int dx = (int) (ws.unit_panel_x_offset + j * 3.5 * ws.unit_icon_size);
                int dy = (int) (ws.unit_panel_y_offset + i * 1.15 * ws.unit_icon_size);

                if (dx <= q.x && q.x <= dx + ws.unit_icon_size
                        && dy <= q.y && q.y <= dy + ws.unit_icon_size) {
                    gui.setInfo_unit(u);
                    return;
                }

//                g2d.drawImage(bi, null, dx, dy);
//
//                Util.drawUnitDetails(g, game, u, dx, dy);
//                if (iterator.hasNext()) {
//                    u = iterator.next();
//                } else {
//                    return;
//                }
                if (is_cargo_listing) {
                    u = cargo_it.next();
                    if (!cargo_it.hasNext()) {
                        cargo_it = null;
                        is_cargo_listing = false;
                    }
                } else if (u.cargo_list.isEmpty()) {
                    if (iterator.hasNext()) {
                        u = iterator.next();
                    } else {
                        return;
                    }
                } else {
                    cargo_it = u.cargo_list.listIterator();
                    u = cargo_it.next();
                    if (cargo_it.hasNext()) {
                        is_cargo_listing = true;
                    }
                }

            }

        }
    }

    public void clickButton1(Point q) {

        int faction = game.getSelectedFaction();
        Point p = game.getSelectedPoint();
//        int[][] unit_icons = Gui.getUnitIcons();
//        BufferedImage bi = new BufferedImage(ws.unit_icon_size, ws.unit_icon_size, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
//        WritableRaster wr = bi.getRaster();
//        int[] pixel_data = new int[1];
        List<Unit> stack = null;
        if (faction == -1) {
            stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y).getStack();
        } else {
            Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
            stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[faction];
        }

        if (stack.get(0).owner != game.getTurn()) {
            return;
        }

        boolean is_cargo_listing = false;
        Iterator<Unit> iterator = stack.listIterator();
        Iterator<Unit> cargo_it = null;
        Unit u = iterator.next();
//        Graphics2D g2d = (Graphics2D) g;

        for (int i = 0; i < C.STACK_WINDOW_UNITS_Y; i++) {
            for (int j = 0; j < C.STACK_WINDOW_UNITS_X; j++) {
//                g.setColor(Color.BLACK);
//                g.fillRect((int) (ws.unit_panel_x_offset + j * 3.5 * ws.unit_icon_size),
//                        (int) (ws.unit_panel_y_offset + i * 1.15 * ws.unit_icon_size),
//                        3 * ws.unit_icon_size,
//                        ws.unit_icon_size);

//                int color = Util.getOwnerColor(u.owner);
//                if (u.selected) {
//                    color += 3;
//                }
////                System.out.println("color = " + color);
//                Util.fillRaster(wr, color);
//                Util.drawUnitIconEdges(wr, ws);
//                Util.writeUnit(pixel_data, u.type, unit_icons, wr, ws);
                int dx = (int) (ws.unit_panel_x_offset + j * 3.5 * ws.unit_icon_size);
                int dy = (int) (ws.unit_panel_y_offset + i * 1.15 * ws.unit_icon_size);

                if (dx <= q.x && q.x <= dx + ws.unit_icon_size
                        && dy <= q.y && q.y <= dy + ws.unit_icon_size) {
                    gui.setDragUnit(u, q);
                    gui.setCurrentState(UIW2.get());
                    gui.getMainWindows().repaint();
                    return;
                }

//                g2d.drawImage(bi, null, dx, dy);
//
//                Util.drawUnitDetails(g, game, u, dx, dy);
//                if (iterator.hasNext()) {
//                    u = iterator.next();
//                } else {
//                    return;
//                }
                if (is_cargo_listing) {
                    u = cargo_it.next();
                    if (!cargo_it.hasNext()) {
                        cargo_it = null;
                        is_cargo_listing = false;
                    }
                } else if (u.cargo_list.isEmpty()) {
                    if (iterator.hasNext()) {
                        u = iterator.next();
                    } else {
                        return;
                    }
                } else {
                    cargo_it = u.cargo_list.listIterator();
                    u = cargo_it.next();
                    if (cargo_it.hasNext()) {
                        is_cargo_listing = true;
                    }
                }

            }

        }
    }

}
