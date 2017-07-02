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
import game.Square;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.math3.util.FastMath;
import util.C;

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

    @Override
    public void pressExitButton() {
        boolean none_selected = true;
        for (Unit unit : game.getSelectedStack()) {
            if (unit.selected) {
                none_selected = false;
                break;
            }
        }
        if (none_selected) {
            State tmp = null;
            if (main_game_state instanceof PW) {
                tmp = PW1B.get();
            } else {
                tmp = SW1B.get();
            }
            gui.setCurrentState(tmp);
        } else {
            gui.setCurrentState(main_game_state);
        }
        SU.restoreMainWindow();
        main_game_state = null;
        gui.getUnitInfoWindow().stopAnimation();
        gui.setInfo_unit(null);

    }

    /**
     * May only disband non-cargo/noble/scepter house units on planets.
     */
    @Override
    public void pressDisbandButton() {
        Unit u = gui.getInfo_unit();
        if (u == null || u.prev_owner != game.getTurn() || game.getSelectedFaction().x != -1) {
            return;
        }
        switch (u.type) {
            //case C.CARGO_UNIT_TYPE:
            case C.NOBLE_UNIT_TYPE:
            case C.SCEPTER_UNIT_TYPE:
                return;
            default:
                break;
        }
        int[] res = u.type_data.reqd_res;
        for (int i = 0; i < res.length; i++) {
            int req = res[i];
            if (req > 0) {
                req = (int) FastMath.ceil(req * C.DISBAND_REFUND);
                game.getResources().addOneResourceTypeToHex(u.p_idx, u.x, u.y, u.owner, u.prev_owner, i, req);
            }
        }
        game.deleteUnitNotInCombat(u);
        gui.setInfo_unit(null);
        if (game.getSelectedStack().isEmpty()) {
            gui.setCurrentState(PW1.get());
            game.setSelectedPointFaction(null, -1, null, null);
            SU.restoreMainWindow();
            main_game_state = null;
        }
    }

    @Override
    public void pressUnloadButton() {
        List<Unit> stack = game.getSelectedStack();
        Point faction = game.getSelectedFaction();
        if (stack.get(0).owner != game.getTurn()) {
            return;
        }
        Point p = game.getSelectedPoint();
        boolean[] terrain = null;
        int tile_set = -1;
        Hex hex = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y);
        if (faction.x == -1) {
            terrain = hex.getTerrain();
            tile_set = game.getPlanet(game.getCurrentPlanetNr()).tile_set_type;
        }
        List<Unit> tmp = new LinkedList<>();
        for (Unit u : stack) {
            if (u.cargo_list.size() > 0) {
                for (Unit c : u.cargo_list) {
                    if ((faction.x != -1 && !(c.move_type == C.MoveType.JUMP
                            || c.move_type == C.MoveType.LANDER
                            || c.move_type == C.MoveType.SPACE))
                            || (faction.x == -1 && terrain[C.OCEAN] == true && tile_set != 4)) { // fix #69
                        continue;
                    }
                    tmp.add(c);
                }
            }
        }
        if (tmp.isEmpty()) {
            return;
        }
        Unit c;
        for (Unit u : tmp) {
            c = u.carrier;
            c.disembark(u);
        }
        game.getHexProc().spotProc(hex, tmp);
        stack.addAll(tmp);
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

        Point faction = game.getSelectedFaction();
        Point p = game.getSelectedPoint();
//        int[][] unit_icons = Gui.getUnitIcons();
//        BufferedImage bi = new BufferedImage(ws.unit_icon_size, ws.unit_icon_size, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
//        WritableRaster wr = bi.getRaster();
//        int[] pixel_data = new int[1];
        List<Unit> stack = null;
        if (faction.x == -1) {
            stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y).getStack();
        } else {
            Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
            stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[faction.y];
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

                if (dx <= q.x && q.x <= dx + ws.ui_unit_block_w
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

        Point faction = game.getSelectedFaction();
        Point p = game.getSelectedPoint();
//        int[][] unit_icons = Gui.getUnitIcons();
//        BufferedImage bi = new BufferedImage(ws.unit_icon_size, ws.unit_icon_size, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
//        WritableRaster wr = bi.getRaster();
//        int[] pixel_data = new int[1];
        List<Unit> stack = null;
        if (faction.x == -1) {
            stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y).getStack();
        } else {
            Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
            stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[faction.y];
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

                if (dx <= q.x && q.x <= dx + ws.ui_unit_block_w
                        && dy <= q.y && q.y <= dy + ws.unit_icon_size) {
                    gui.setDragUnit(u, q);
                    setState();
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

    protected void setState() {
        gui.setCurrentState(UIW2.get());
    }

}
