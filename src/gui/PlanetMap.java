/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import dat.UnitType;
import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Game;
import game.Hex;
import game.PlanetGrid;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JPanel;
import util.C;
import util.Util;
import util.WindowSize;

/**
 *
 * @author joulupunikki
 */
public class PlanetMap extends JPanel {

    // pointer to GUI
    private Gui gui;
    private Game game;
    private int[][] hex_tiles;
    private int[][] unit_icons;
    private int[][] structures;
    private WindowSize ws;
    private IndexColorModel color_index;
    private byte[][] pallette;
    int[] color_scaler;

    Hex[][] hex_grid;
    Hex current_hex;
    int current_faction;

    BufferedImage bi;

    public PlanetMap(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        color_index = gui.getICM();
        game = gui.getGame();
        pallette = gui.getPallette();
        color_scaler = gui.getResources().getColorScaler();
        bi = new BufferedImage(ws.planet_map_width, ws.planet_map_height, BufferedImage.TYPE_BYTE_INDEXED, color_index);
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderPlanetMap(g);
        
    }

    public void renderPlanetMap(Graphics g) {

//        byte[][] pallette = gui.getPallette();
//        BufferedImage bi = new BufferedImage(ws.planet_map_width, ws.planet_map_height, BufferedImage.TYPE_BYTE_INDEXED, color_index);
//        int tile_set_type = game.getGalaxy().game.getCurrentPlanetNr();
//        hex_tiles = Util.loadHexTiles("bin/efstile0.bin", 134);
//        structures = Util.loadHexTiles("bin/struct0.bin", 32);
        int tile_set = game.getPlanet(game.getCurrentPlanetNr()).tile_set_type;
        hex_tiles = Gui.getHexTiles(tile_set);
        structures = Gui.getStructureTiles(tile_set);

        unit_icons = Gui.getUnitIcons();

        hex_grid = game.getPlanetGrid(game.getCurrentPlanetNr()).getMapArray();
        current_faction = game.getTurn();
//        structures = Gui.getStructures();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, ws.planet_map_width, ws.planet_map_height);

        drawHexTiles(g, hex_tiles, bi);

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);

        drawHexGrid(g);

        drawStructureNames(g);

        drawUnits(g);

        if (game.getPath() != null) {
            drawPath(g);
        }

        drawSelectedStack(g);
        //drawFlags(g);

        //drawHexTypes(g);
//        drawMoveCosts(g);
    }

    public void drawMoveCosts(Graphics g) {

        Point origin = game.getMapOrigin();

        int current_planet = game.getCurrentPlanetNr();

        PlanetGrid planet_grid = game.getPlanetGrid(current_planet);

        // get move_type of selected unit
        Point sel = game.getSelectedPoint();
        if (sel == null) {
            return;
        }

        int unit_type = 0;
        int t_lvl = 0;
        List<Unit> stack = planet_grid.getHex(sel.x, sel.y).getStack();
        for (Unit e : stack) {
            if (e.selected) {
                unit_type = e.type;
                t_lvl = e.t_lvl;
            }
        }
        UnitType[][] unit_types = game.getUnitTypes();
        C.MoveType move_type = unit_types[unit_type][t_lvl].move_type;

        int origin_x = origin.x;
        int origin_y = origin.y;

        int x_offset = C.STRUCT_BIN_WIDTH / 2;
        int y_offset = C.STRUCT_BIN_HEIGHT / 2;

        int[] pixel_data = new int[1];
        int x = 0;
        int y = 0;
        int counter = 0;
        int dx = 0;
        int dy = 0;
        //need to roll-over i to 0 when i = 43, see end of loop
        for (int i = origin_x; counter < 13; i++) {
            y = 0;

            for (int j = origin_y; j < origin_y + 10; j++) {
                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
                    int dip = 0;

                    if (i % 2 == 0) {
                        dip = +20;
                    }

                    int move_cost = planet_grid.getHex(i, j).getMoveCost(move_type.ordinal());

                    dx = x + x_offset;
                    dy = y + dip + y_offset;

                    if (ws.is_double) {
                        dx *= 2;
                        dy *= 2;
                    }

                    g.setColor(Color.red);
                    g.setFont(new Font("Arial", Font.BOLD, 20));
                    g.drawString("" + move_cost, dx, dy);

                }
                y += 40;

            }

            x += 38;

            // need to roll-over i to 0 when i = 43
            if (i == C.PLANET_MAP_WIDTH - 1) {
                i = -1;

            }
            counter++;

        }
    }

    public void drawHexTypes(Graphics g) {

        Point origin = game.getMapOrigin();

        int current_planet = game.getCurrentPlanetNr();

        PlanetGrid planet_grid = game.getPlanetGrid(current_planet);

        int origin_x = origin.x;
        int origin_y = origin.y;

        int x_offset = (C.STRUCT_BIN_WIDTH - C.EFSUNIT_BIN_WIDTH) / 2;
        int y_offset = (C.STRUCT_BIN_HEIGHT - C.EFSUNIT_BIN_HEIGHT) / 2;

        int[] pixel_data = new int[1];
        int x = 0;
        int y = 0;
        int counter = 0;
        int dx = 0;
        int dy = 0;
        //need to roll-over i to 0 when i = 43, see end of loop
        for (int i = origin_x; counter < 13; i++) {
            y = 0;

            for (int j = origin_y; j < origin_y + 10; j++) {
                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
                    int dip = 0;

                    if (i % 2 == 0) {
                        dip = +20;
                    }

                    boolean[] terrain_type = planet_grid.getHex(i, j).getTerrain();

                    int n = 0;
                    for (int k = 0; k < terrain_type.length; k++) {
                        if (terrain_type[k] == true) {
                            dx = x + x_offset;
                            dy = 5 + y + dip + y_offset + 9 * n++;

                            if (ws.is_double) {
                                dx *= 2;
                                dy *= 2;
                            }
                            g.setColor(Color.RED);
                            g.setFont(new Font("Arial", Font.PLAIN, 15));
                            g.drawString(Util.terrainTypeAbbrev(k), dx, dy);
                        }
                    }

                    dx = x + x_offset;
                    dy = y + dip + y_offset;

                    if (ws.is_double) {
                        dx *= 2;
                        dy *= 2;
                    }

//                        for (int k = 0; k < name.length(); k++) {
//                            String t = name.substring(k, k + 1);
//                            g.setFont(ws.font_structure_name_bg);
//                            g.setColor(Color.BLACK);
//                            g.drawString(t, dx + k * (ws.font_structure_name_size - ws.font_structure_name_gap), dy);
//                                                        
//                            g.setColor(Util.getColor(pallette, Util.getOwnerColor(city.owner)));
//                            g.setFont(ws.font_structure_name_fg);
//                            g.drawString(t, dx + k * (ws.font_structure_name_size - ws.font_structure_name_gap), dy);
//                        }
                }
                y += 40;

            }

            x += 38;

            // need to roll-over i to 0 when i = 43
            if (i == C.PLANET_MAP_WIDTH - 1) {
                i = -1;

            }
            counter++;

        }
    }

    public void drawStructureNames(Graphics g) {

        Point origin = game.getMapOrigin();

        int current_planet = game.getCurrentPlanetNr();

        int origin_x = origin.x;
        int origin_y = origin.y;

        int x_offset = (C.STRUCT_BIN_WIDTH - C.EFSUNIT_BIN_WIDTH) / 2;
        int y_offset = (C.STRUCT_BIN_HEIGHT - C.EFSUNIT_BIN_HEIGHT) / 2;

        int[] pixel_data = new int[1];
        int x = 0;
        int y = 0;
        int counter = 0;
        int dx = 0;
        int dy = 0;
        //need to roll-over i to 0 when i = 43, see end of loop
        for (int i = origin_x; counter < 13; i++) {
            y = 0;

            for (int j = origin_y; j < origin_y + 10; j++) {
                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
                    int dip = 0;

                    if (i % 2 == 0) {
                        dip = +20;
                    }
                    Structure city = game.getPlanetGrid(current_planet).getHex(i, j).getStructure();
                    if (city != null) {
                        String name = game.getStrBuild(city.type).name;

                        dx = x + x_offset;
                        dy = y + dip + y_offset + C.STRUCT_BIN_HEIGHT;

                        if (ws.is_double) {
                            dx *= 2;
                            dy *= 2;
                        }

//                        for (int k = 0; k < name.length(); k++) {
//                            String t = name.substring(k, k + 1);
//                            g.setFont(ws.font_structure_name_bg);
//                            g.setColor(Color.BLACK);
//                            g.drawString(t, dx + k * (ws.font_structure_name_size - ws.font_structure_name_gap), dy);
//                                                        
//                            g.setColor(Util.getColor(pallette, Util.getOwnerColor(city.owner)));
//                            g.setFont(ws.font_structure_name_fg);
//                            g.drawString(t, dx + k * (ws.font_structure_name_size - ws.font_structure_name_gap), dy);
//                        }
                        g.setFont(ws.font_structure_name_fg);
                        g.setColor(Color.BLACK);
                        g.drawString(name, dx + ws.font_structure_name_gap, dy);
                        g.drawString(name, dx - ws.font_structure_name_gap, dy);
                        g.drawString(name, dx, dy - ws.font_structure_name_gap);
                        g.drawString(name, dx, dy + ws.font_structure_name_gap);
                        g.setColor(Util.getColor(pallette, Util.getOwnerColor(city.owner)));
                        g.setFont(ws.font_structure_name_fg);
                        g.drawString(name, dx, dy);
                    }

                    Structure resource = game.getPlanetGrid(current_planet).getHex(i, j).getResource();
                    if (resource != null) {
                        String name = game.getStrBuild(resource.type).name;

                        dx = x + x_offset;
                        dy = y + dip + y_offset + C.STRUCT_BIN_HEIGHT;

                        if (ws.is_double) {
                            dx *= 2;
                            dy *= 2;
                        }

                        g.setFont(ws.font_structure_name_fg);
                        g.setColor(Color.BLACK);
                        g.drawString(name, dx + ws.font_structure_name_gap, dy);
                        g.drawString(name, dx - ws.font_structure_name_gap, dy);
                        g.drawString(name, dx, dy - ws.font_structure_name_gap);
                        g.drawString(name, dx, dy + ws.font_structure_name_gap);
                        g.setColor(Util.getColor(pallette, Util.getOwnerColor(resource.owner)));
                        g.setFont(ws.font_structure_name_fg);
                        g.drawString(name, dx, dy);
                    }

                }
                y += 40;

            }

            x += 38;

            // need to roll-over i to 0 when i = 43
            if (i == C.PLANET_MAP_WIDTH - 1) {
                i = -1;

            }
            counter++;

        }

    }

    public void drawPath(Graphics g) {

        Point origin = game.getMapOrigin();

        LinkedList<Hex> path = game.getPath();

        int current_planet = game.getCurrentPlanetNr();

        PlanetGrid planet_grid = game.getPlanetGrid(current_planet);

        UnitType[][] unit_types = game.getUnitTypes();
        // get move_type of selected unit
        Point sel = game.getSelectedPoint();
        if (sel == null) {
            return;
        }

        List<Unit> stack = planet_grid.getHex(sel.x, sel.y).getStack();
        List<Unit> selected = new LinkedList<>();
        for (Unit unit : stack) {
            if (unit.selected) {
                selected.add(unit);
            }
        }

        boolean no_move_left = false;
        boolean end_turn = false;
        int turn = 1;

        int origin_x = origin.x;
        int origin_y = origin.y;

        int x_offset = C.STRUCT_BIN_WIDTH / 2;
        int y_offset = C.STRUCT_BIN_HEIGHT / 2;

        int x = 0;
        int y = 0;
        int counter = 0;
        int dx = 0;
        int dy = 0;
        //need to roll-over i to 0 when i = 43, see end of loop
        for (int i = origin_x; counter < 13; i++) {
            y = 0;

            for (int j = origin_y; j < origin_y + 10; j++) {
                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
                    int dip = 0;

                    if (i % 2 == 0) {
                        dip = +20;
                    }

                    dx = x + x_offset;
                    dy = y + dip + y_offset;

                    if (ws.is_double) {
                        dx *= 2;
                        dy *= 2;
                    }

                    int[] curr_mv_points = new int[Util.stackSize(selected)];

                    int index = 0;
                    for (Unit e : selected) {
                        curr_mv_points[index++] = e.move_points;
                    }

                    turn = 1;
                    for (ListIterator<Hex> it = path.listIterator(1); it.hasNext();) {
                        Hex h = it.next();

                        index = 0;
                        for (Unit e : selected) {
                            int move_cost = h.getMoveCost(e.move_type.ordinal());
                            int max_move = unit_types[e.type][e.t_lvl].move_pts;
                            if (move_cost > max_move) {
                                move_cost = max_move;
                            }
                            if (curr_mv_points[index] >= move_cost) {
                                curr_mv_points[index] -= move_cost;
                                index++;
                            } else { // no movement points left
                                no_move_left = true;
                                break;
                            }
                        }

                        if (no_move_left) {
                            turn++;
                            index = 0;
                            for (Unit e : selected) {
                                curr_mv_points[index] = unit_types[e.type][e.t_lvl].move_pts;
                                int move_cost = h.getMoveCost(e.move_type.ordinal());
                                int max_move = unit_types[e.type][e.t_lvl].move_pts;
                                if (move_cost > max_move) {
                                    move_cost = max_move;
                                }
                                curr_mv_points[index] -= move_cost;
                                index++;
                            }
//                                g.setColor(Color.RED);
//                                g.fillOval(dx, dy, ws.path_circle, ws.path_circle);
//                                g.setColor(Color.BLACK);
//                                g.drawOval(dx, dy, ws.path_circle, ws.path_circle);
//                                break;
                            no_move_left = false;
                        }

                        if (it.hasNext()) {
                            Hex h2 = it.next();
                            index = 0;
                            for (Unit e : selected) {
                                int move_cost = h2.getMoveCost(e.move_type.ordinal());
                                int max_move = unit_types[e.type][e.t_lvl].move_pts;
                                if (move_cost > max_move) {
                                    move_cost = max_move;
                                }
                                if (curr_mv_points[index] < move_cost) {
                                    end_turn = true;
                                }
                                index++;
                            }
                            it.previous();
                        }

                        if (h.getX() == i && h.getY() == j) {
                            if (turn > 1) {
                                g.setColor(Color.RED);
                            } else {
                                g.setColor(Color.GREEN);
                            }

                            if (end_turn) {
                                dx -= ws.font_path_numbers_size / 2;
                                dy += ws.font_path_numbers_size / 2;
                                String s = "" + turn;
                                g.setFont(ws.font_path_numbers);
                                g.setColor(Color.BLACK);
                                g.drawString(s, dx + ws.font_structure_name_gap, dy);
                                g.drawString(s, dx - ws.font_structure_name_gap, dy);
                                g.drawString(s, dx, dy - ws.font_structure_name_gap);
                                g.drawString(s, dx, dy + ws.font_structure_name_gap);
                                if (turn > 1) {
                                    g.setColor(Color.RED);
                                } else {
                                    g.setColor(Color.GREEN);
                                }

                                g.drawString(s, dx, dy);

                            } else {
//                                g.setColor(Color.GREEN);
                                dx -= ws.path_circle / 2;
                                dy -= ws.path_circle / 2;
                                g.fillOval(dx, dy, ws.path_circle, ws.path_circle);
                                g.setColor(Color.BLACK);
                                g.drawOval(dx, dy, ws.path_circle, ws.path_circle);
                            }
                        }
                        end_turn = false;
                    }

                }
                y += 40;

            }

            x += 38;

            // need to roll-over i to 0 when i = 43
            if (i == C.PLANET_MAP_WIDTH - 1) {
                i = -1;

            }
            counter++;

        }

    }

//    public void drawUnits(Graphics g) {
//
//        BufferedImage bi = new BufferedImage(ws.unit_icon_size, ws.unit_icon_size, BufferedImage.TYPE_BYTE_INDEXED, color_index);
//        WritableRaster wr = bi.getRaster();
//
//        Point origin = game.getMapOrigin();
//
//        Point selected_stack = game.getSelectedPoint();
//
//        int current_planet = game.getCurrentPlanetNr();
//
//        int origin_x = origin.x;
//        int origin_y = origin.y;
//
//        int x_offset = (C.STRUCT_BIN_WIDTH - C.EFSUNIT_BIN_WIDTH) / 2;
//        int y_offset = (C.STRUCT_BIN_HEIGHT - C.EFSUNIT_BIN_HEIGHT) / 2;
//
//        int[] pixel_data = new int[1];
//        int x = 0;
//        int y = 0;
//        int counter = 0;
//        int dx = 0;
//        int dy = 0;
//        //need to roll-over i to 0 when i = 43, see end of loop
//        for (int i = origin_x; counter < 13; i++) {
//            y = 0;
//
//            for (int j = origin_y; j < origin_y + 10; j++) {
//                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
//                    int dip = 0;
//
//                    if (i % 2 == 0) {
//                        dip = +20;
//                    }
//                    if (selected_stack == null || selected_stack.x != i || selected_stack.y != j || gui.getAnimationBlink()) {
//
//                        dx = x + x_offset;
//                        dy = y + dip + y_offset;
//
//                        if (ws.is_double) {
//                            dx *= 2;
//                            dy *= 2;
//                        }
//
//
//
//                        List<Unit> stack = game.getPlanetGrid(current_planet).getHex(i, j).getStack();
//                        if (stack != null && Util.stackSize(stack) > 0) {
//                            Unit e = stack.get(0);
//
//
//                            if (game.getPlanetGrid(current_planet).getHex(i, j).getStructure() == null
//                                    || (selected_stack != null && selected_stack.x == i && selected_stack.y == j)) {
//
//
//
//                                Util.fillRaster(wr, Util.getOwnerColor(e.owner));
//                                Util.drawUnitIconEdges(wr, ws);
//                                Util.writeUnit(pixel_data, e.type, unit_icons, wr, ws);
//
//                                Graphics2D g2d = (Graphics2D) g;
//
//                                g2d.drawImage(bi, null, dx, dy);
//                                Util.writeUnitCount(g2d, ws, Util.stackSize(stack), dx, dy);
//                            } else {
//                                g.setColor(Util.getColor(pallette, Util.getOwnerColor(e.owner)));
//                                Util.drawBlip(g, dx, dy, ws.blip_side);
//                            }
//                        }
//                    }
//
//                }
//                y += 40;
//
//            }
//
//            x += 38;
//
//            // need to roll-over i to 0 when i = 43
//            if (i == C.PLANET_MAP_WIDTH - 1) {
//                i = -1;
//
//            }
//            counter++;
//
//        }
//
//    }
    public void drawSelectedStack(Graphics g) {

        BufferedImage bi = new BufferedImage(ws.unit_icon_size, ws.unit_icon_size, BufferedImage.TYPE_BYTE_INDEXED, color_index);
        WritableRaster wr = bi.getRaster();

        Point origin = game.getMapOrigin();

        Point selected_stack = game.getSelectedPoint();

        int current_planet = game.getCurrentPlanetNr();

        int origin_x = origin.x;
        int origin_y = origin.y;

        int x_offset = (C.STRUCT_BIN_WIDTH - C.EFSUNIT_BIN_WIDTH) / 2;
        int y_offset = (C.STRUCT_BIN_HEIGHT - C.EFSUNIT_BIN_HEIGHT) / 2;

        int[] pixel_data = new int[1];
        int x = 0;
        int y = 0;
        int counter = 0;
        int dx = 0;
        int dy = 0;
        //need to roll-over i to 0 when i = 43, see end of loop
        for (int i = origin_x; counter < 13; i++) {
            y = 0;

            for (int j = origin_y; j < origin_y + 10; j++) {
                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
                    int dip = 0;

                    if (i % 2 == 0) {
                        dip = +20;
                    }
                    if (selected_stack != null && selected_stack.x == i && selected_stack.y == j) { // && gui.getAnimationBlink()) {

                        if (!gui.getStackMove() && gui.getAnimationBlink()) {
                            return;
                        }

                        dx = x + x_offset;
                        dy = y + dip + y_offset;

                        if (gui.getStackMove()) {
                            int move_counter = gui.getStackMoveCounter();
                            LinkedList<Hex> path = game.getPath();
                            Hex hex_1 = path.get(0);
                            Hex hex_2 = path.get(1);

                            Hex[] neighbours = hex_1.getNeighbours();
                            int neighbour = 0;
                            for (int k = 0; k < neighbours.length; k++) {
                                Hex hex = neighbours[k];
                                if (hex_2.equals(hex)) {
                                    neighbour = k;
                                }

                            }

                            double smxo = 1.9;
                            double smyo = 1.0;
                            switch (neighbour) {
                                case 0:
//                                    dx -= smxo * move_counter;
                                    dy -= 2 * smyo * move_counter;
                                    break;
                                case 1:
                                    dx += smxo * move_counter;
                                    dy -= smyo * move_counter;
                                    break;
                                case 2:
                                    dx += smxo * move_counter;
                                    dy += smyo * move_counter;
                                    break;
                                case 3:
//                                    dx -= smxo * move_counter;
                                    dy += 2 * smyo * move_counter;
                                    break;
                                case 4:
                                    dx -= smxo * move_counter;
                                    dy += smyo * move_counter;
                                    break;
                                case 5:
                                    dx -= smxo * move_counter;
                                    dy -= smyo * move_counter;
                                    break;
                                default:
                                    throw new AssertionError();
                            }

                        }
                        if (ws.is_double) {
                            dx *= 2;
                            dy *= 2;
                        }

                        List<Unit> stack = game.getPlanetGrid(current_planet).getHex(i, j).getStack();
                        if (stack != null && Util.stackSize(stack) > 0) {
                            Unit e = null;
                            for (Unit unit : stack) {
                                if (unit.selected) {
                                    e = unit;
                                    break;
                                }
                            }
                            if (e == null) {
                                e = stack.get(0);
                            }
                            Util.fillRaster(wr, Util.getOwnerColor(e.owner));
                            Util.drawUnitIconEdges(wr, ws);
                            Util.writeUnit(pixel_data, e.type, unit_icons, wr, ws);

                            Graphics2D g2d = (Graphics2D) g;

                            g2d.drawImage(bi, null, dx, dy);
                            Util.writeUnitCount(g2d, ws, Util.stackSize(stack), dx, dy);

                        }
                    }

                }
                y += 40;

            }

            x += 38;

            // need to roll-over i to 0 when i = 43
            if (i == C.PLANET_MAP_WIDTH - 1) {
                i = -1;

            }
            counter++;

        }

    }

    public void drawUnits(Graphics g) {

        BufferedImage bi = new BufferedImage(ws.unit_icon_size, ws.unit_icon_size, BufferedImage.TYPE_BYTE_INDEXED, color_index);
        WritableRaster wr = bi.getRaster();

        Point origin = game.getMapOrigin();

        Point selected_stack = game.getSelectedPoint();

        int current_planet = game.getCurrentPlanetNr();

        int origin_x = origin.x;
        int origin_y = origin.y;

        int x_offset = (C.STRUCT_BIN_WIDTH - C.EFSUNIT_BIN_WIDTH) / 2;
        int y_offset = (C.STRUCT_BIN_HEIGHT - C.EFSUNIT_BIN_HEIGHT) / 2;

        int[] pixel_data = new int[1];
        int x = 0;
        int y = 0;
        int counter = 0;
        int dx = 0;
        int dy = 0;
        //need to roll-over i to 0 when i = 43, see end of loop
        for (int i = origin_x; counter < 13; i++) {
            y = 0;

            for (int j = origin_y; j < origin_y + 10; j++) {
                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
                    int dip = 0;

                    if (i % 2 == 0) {
                        dip = +20;
                    }
                    if (selected_stack == null || selected_stack.x != i || selected_stack.y != j) {

                        dx = x + x_offset;
                        dy = y + dip + y_offset;

                        if (ws.is_double) {
                            dx *= 2;
                            dy *= 2;
                        }

                        List<Unit> stack = game.getPlanetGrid(current_planet).getHex(i, j).getStack();
                        if (stack != null && Util.stackSize(stack) > 0) {
                            Unit e = stack.get(0);

                            if (game.getPlanetGrid(current_planet).getHex(i, j).getStructure() == null) {

                                Util.fillRaster(wr, Util.getOwnerColor(e.owner));
                                Util.drawUnitIconEdges(wr, ws);
                                Util.writeUnit(pixel_data, e.type, unit_icons, wr, ws);

                                Graphics2D g2d = (Graphics2D) g;

                                g2d.drawImage(bi, null, dx, dy);
                                Util.writeUnitCount(g2d, ws, Util.stackSize(stack), dx, dy);
                            } else {
                                g.setColor(Util.getColor(pallette, Util.getOwnerColor(e.owner)));
                                Util.drawBlip(g, dx, dy, ws.blip_side);
                            }
                        }
                    }

                }
                y += 40;

            }

            x += 38;

            // need to roll-over i to 0 when i = 43
            if (i == C.PLANET_MAP_WIDTH - 1) {
                i = -1;

            }
            counter++;

        }

    }

    /**
     * Draw a hexagonal image into square target raster
     *
     * @param g
     * @param hex_tiles
     * @param bi
     */
    public void drawHexTiles(Graphics g, int[][] hex_tiles, BufferedImage bi) {

//        System.out.println("hex_tiles length: " + hex_tiles.length);
        WritableRaster wr = bi.getRaster();

        Point origin = game.getMapOrigin();

        boolean stack_moving = gui.isStack_moving();
        int stack_move_counter = gui.getStackMoveCounter();

        if (stack_moving && 0 < stack_move_counter && 20 > stack_move_counter) {
            return;
        }
        int a_x = wr.getWidth();
        int a_y = wr.getHeight();
        int d_x = 10;
        int d_y = 20;
        if (ws.is_double) {
            d_x *= 2;
            d_y *= 2;
        }
        int[] data = {0};
        for (int i = 0; i < a_x; i++) {
            for (int j = 0; j < d_y; j++) {
                wr.setPixel(i, j, data);

            }

        }
        for (int i = 0; i < a_x; i++) {
            for (int j = a_y - d_y; j < a_y; j++) {
                wr.setPixel(i, j, data);

            }

        }
        for (int i = 0; i < d_x; i++) {
            for (int j = d_y; j < a_y - d_y; j++) {
                wr.setPixel(i, j, data);

            }

        }
        for (int i = a_x - d_x; i < a_x; i++) {
            for (int j = d_y; j < a_y - d_y; j++) {
                wr.setPixel(i, j, data);

            }

        }

        int origin_x = origin.x;
        int origin_y = origin.y;

        int[] pixel_data = new int[1];
        int x = 0;
        int y = 0;
        int counter = 0;
        //need to roll-over i to 0 when i = 43, see end of loop
        for (int i = origin_x; counter < 13; i++) {
            y = 0;

            for (int j = origin_y; j < origin_y + 10; j++) {
                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
                    int dip = 0;

                    if (i % 2 == 0) {
                        dip = +20;
                    }
                    // for setting spotted squares
                    current_hex = hex_grid[i][j];

                    // hex tile numbers in efstile*.bin
                    int[] tile_no;

                    tile_no = getTileNo(i, j);

                    //skip top j when i % 2 == 0
//                if (j != origin_y || (i % 2 != 0)) {
                    if (!stack_moving || 0 >= stack_move_counter || 20 <= stack_move_counter) {
                        writeHex2(x, y, dip, pixel_data, hex_tiles, tile_no, wr);
                    }
//                }
                    //                int t_idx = 0;
//                    writeUnit(g, x, y, dip, pixel_data, unit_icons, tile_no, wr);
                }
                y += 40;

            }

            x += 38;

            // need to roll-over i to 0 when i = 43
            if (i == C.PLANET_MAP_WIDTH - 1) {
                i = -1;

            }
            counter++;

        }

    }

    public static void writeUnit(int[] pixel_data, int unit_no,
            int[][] unit_pics, WritableRaster wr, WindowSize ws) {

//        int x_offset = (C.STRUCT_BIN_WIDTH - C.EFSUNIT_BIN_WIDTH) / 2;
//        int y_offset = (C.STRUCT_BIN_HEIGHT - C.EFSUNIT_BIN_HEIGHT) / 2;
        for (int i = 0; i < C.EFSUNIT_BIN_HEIGHT; i++) {
            for (int j = 0; j < C.EFSUNIT_BIN_WIDTH; j++) {

                writeUnitPixel(j, i, i * C.EFSUNIT_BIN_WIDTH + j,
                        pixel_data, unit_pics, unit_no, wr, ws);

            }
        }
    }

    public void writeHex2(int x, int y, int dip, int[] pixel_data,
            int[][] hex_tiles, int[] tile_no, WritableRaster wr) {

        int dx = 0;
        int dy = 0;

        for (int i = 0; i < C.STRUCT_BIN_HEIGHT; i++) {
            for (int j = 0; j < C.STRUCT_BIN_WIDTH; j++) {

                dx = x + j;
                dy = y + dip + i;
                if (dx < ws.planet_map_width && dy < ws.planet_map_height) {

                    writePixel(dx, dy, i * C.STRUCT_BIN_WIDTH + j,
                            pixel_data, hex_tiles, tile_no, wr);

                }

            }
        }
    }

    public void writeHex(int x, int y, int x_offset, int y_offset,
            int dip, int[] pixel_data, int[][] hex_tiles,
            int[] tile_no, WritableRaster wr) {
        /*
         * write hexagonal source image to square target
         * 3 nested loops
         */
        int t_idx = 0;
        // ... top

        int y_pos = 0;
        for (int l = 10; l > 0; l--) {

            for (int k = 0; k < 2; k++) {
                if (l != 10 || k != 0) {
                    t_idx += l;

                    for (int m = 0; m < (C.STRUCT_BIN_WIDTH - (2 * l)); ++m) {

                        writePixel(x + x_offset + l + m, y + dip + y_offset + y_pos,
                                t_idx, pixel_data, hex_tiles, tile_no, wr);

                        t_idx++;

                    }
                    ++y_pos;

                    t_idx += l;
                }
            }
        }
        // ... center
        for (int k = 0; k < 2; k++) {

            for (int l = 0; l < C.STRUCT_BIN_WIDTH; ++l) {

                writePixel(x + x_offset + l, y + dip + y_offset + y_pos,
                        t_idx, pixel_data, hex_tiles, tile_no, wr);
                t_idx++;
            }
            ++y_pos;
        }
        // ... bottom
        for (int l = 1; l <= 10; l++) {

            for (int k = 0; k < 2; k++) {
                if (l != 10 || k != 0) {
                    t_idx += l;
                    for (int m = 0; m < (C.STRUCT_BIN_WIDTH - (2 * l)); ++m) {

                        writePixel(x + x_offset + l + m, y + dip + y_offset + y_pos,
                                t_idx, pixel_data, hex_tiles, tile_no, wr);
                        t_idx++;
                    }
                    ++y_pos;

                    t_idx += l;
                }
            }
        }
    }

    public static void writeUnitPixel(int x, int y, int t_idx, int[] pixel_data, int[][] unit_pics, int unit_nr, WritableRaster wr, WindowSize ws) {

        pixel_data[0] = unit_pics[unit_nr][t_idx];
        if (pixel_data[0] != 0) {

            // if double window size scale image
            if (ws.is_double) {
                wr.setPixel(2 * x, 2 * y, pixel_data);
                wr.setPixel(2 * x + 1, 2 * y, pixel_data);
                wr.setPixel(2 * x, 2 * y + 1, pixel_data);
                wr.setPixel(2 * x + 1, 2 * y + 1, pixel_data);
            } else {
                wr.setPixel(x, y, pixel_data);
            }
        }

    }

    public void writePixel(int x, int y, int t_idx, int[] pixel_data, int[][] hex_tiles, int[] tile_no, WritableRaster wr) {
        for (int i = 0; i < tile_no.length; i++) {

            if (tile_no[i] != -1) {
                if (i == 3 || i == 11) {
                    pixel_data[0] = structures[tile_no[i]][t_idx];
                } else {
                    pixel_data[0] = hex_tiles[tile_no[i]][t_idx];
                }

                if (pixel_data[0] != 0) {
                    if (!current_hex.isSpotted(current_faction)) {
                        pixel_data[0] = color_scaler[pixel_data[0]];

                    }
                    // if double window size scale image
                    if (ws.is_double) {
                        wr.setPixel(2 * x, 2 * y, pixel_data);
                        wr.setPixel(2 * x + 1, 2 * y, pixel_data);
                        wr.setPixel(2 * x, 2 * y + 1, pixel_data);
                        wr.setPixel(2 * x + 1, 2 * y + 1, pixel_data);
                    } else {
                        wr.setPixel(x, y, pixel_data);
                    }
                }
            }
        }

//        if (tile_no[tile_no.length - 1] != -1) {
//            pixel_data[0] = hex_tiles[tile_no[]][t_idx];
//            if (pixel_data[0] != 0) {
//
//                // if double window size scale image
//                if (ws.is_double) {
//                    wr.setPixel(2 * x, 2 * y, pixel_data);
//                    wr.setPixel(2 * x + 1, 2 * y, pixel_data);
//                    wr.setPixel(2 * x, 2 * y + 1, pixel_data);
//                    wr.setPixel(2 * x + 1, 2 * y + 1, pixel_data);
//                } else {
//                    wr.setPixel(x, y, pixel_data);
//                }
//            }
//        }
    }

    public int[] getTileNo(int u, int v) {
        /*
         * room for:
         * basic hex type
         * forest
         * mountain, hill, river
         * resource
         * road 1
         * 
         * 
         * 
         * 
         * road 6
         * road 7 ?
         * structure
         * 
         */
        int[] tile_no = {
            -1, -1, -1, -1,
            -1, -1, -1, -1,
            -1, -1, -1, -1};

        int current_planet = game.getCurrentPlanetNr();

        Structure city = game.getPlanetGrid(current_planet).getHex(u, v).getStructure();

        if (city != null) {
            tile_no[11] = city.type;
        }

        Structure resource = game.getPlanetGrid(current_planet).getHex(u, v).getResource();
        if (resource != null) {
            tile_no[3] = resource.type;
        }

        PlanetGrid planet_map = game.getPlanetGrid(current_planet);
        Hex h = planet_map.getHex(u, v);
        if (h.getTerrain(C.ROAD)) {
            tile_no[4] = 66;

            Hex neighbour = h.getN(C.NORTH);
            if (neighbour != null && neighbour.getTerrain(C.ROAD)) {
                tile_no[5] = 70;
                tile_no[4] = -1;
            }
            neighbour = h.getN(C.NORTHEAST);
            if (neighbour != null && neighbour.getTerrain(C.ROAD)) {
                tile_no[6] = 72;
                tile_no[4] = -1;
            }
            neighbour = h.getN(C.SOUTHEAST);
            if (neighbour != null && neighbour.getTerrain(C.ROAD)) {
                tile_no[7] = 69;
                tile_no[4] = -1;
            }
            neighbour = h.getN(C.SOUTH);
            if (neighbour != null && neighbour.getTerrain(C.ROAD)) {
                tile_no[8] = 68;
                tile_no[4] = -1;
            }
            neighbour = h.getN(C.SOUTHWEST);
            if (neighbour != null && neighbour.getTerrain(C.ROAD)) {
                tile_no[9] = 71;
                tile_no[4] = -1;
            }
            neighbour = h.getN(C.NORTHWEST);
            if (neighbour != null && neighbour.getTerrain(C.ROAD)) {
                tile_no[10] = 67;
                tile_no[4] = -1;
            }
        }
//        // change to instance variable
//        int[][] hex_buffer = gui.getGalaxy().getBuffer();
//
//        Point[][] buf_tab = Util.createBufferConversionTable();
//
//        int flags = hex_buffer[buf_tab[u][v].x][buf_tab[u][v].y];
        int[][] planet_map_flags = game.getGalaxy().getPlanetMap(current_planet);

//        PlanetGrid planet_map = game.getPlanetGrid();
//        
//        
//        Hex h = planet_map.getHex(u, v);
//        System.out.println("u, v: " + u + ", " + v);
//
//        if (h.getN(C.NORTH) != null) {
//            tile_no[4] = 70;
//        }
//
//        if (h.getN(C.NORTHEAST) != null) {
//            tile_no[5] = 72;
//        }
//
//        if (h.getN(C.SOUTHEAST) != null) {
//            tile_no[6] = 69;
//        }
//        if (h.getN(C.SOUTH) != null) {
//            tile_no[7] = 68;
//        }
//        if (h.getN(C.SOUTHWEST) != null) {
//            tile_no[8] = 71;
//        }
//        if (h.getN(C.NORTHWEST) != null) {
//            tile_no[9] = 67;
//        }
////
//}
        int flags = planet_map_flags[u][v];

        if (Util.isWater(flags)) {

            tile_no[0] = 49;
//            System.out.println("water found");
            return tile_no;

        } else if (Util.isRoad(flags)) {
//            System.out.println("road found");
            // road is set elsewhere
//            tile_no[4] = 66;
            flags >>>= 9;
        }

        if (Util.isDelta(flags)) {
            tile_no[1] = 94;
            flags >>>= 11;
        }

        if (Util.isForestHill(flags)) {
            tile_no[2] = processFlags(flags & 0b0011_0111);
            flags >>>= 9;
        } else if (Util.isForestMtn(flags)) {
            tile_no[2] = processFlags(flags & 0b0011_0110);
            flags >>>= 9;
        } else if (Util.isForestRiver(flags)) {
//            System.out.println("isForestRiver");
//            System.out.println("raw flags: " + Util.createFlagString(flags));
            tile_no[2] = processFlags(flags & 0b0000_1111_1111);
//            System.out.println("tile_no[2]: " + tile_no[2]);
            flags >>>= 9;
        }

        if (Util.isOnlyGrass(flags)) {
            tile_no[0] = 73;
            return tile_no;
        }

        if (Util.isOnlyAridGrass(flags)) {
            tile_no[0] = 80;
            return tile_no;
        }

        if (Util.isOnlyDesert(flags)) {
            tile_no[0] = 87;
            return tile_no;
        }

        if (Util.isOnlyIce(flags)) {
            tile_no[0] = 124;
            return tile_no;
        }

        if (Util.isOnlyTundra(flags)) {
            tile_no[0] = 117;
            return tile_no;
        }

        if (Util.isGrass(flags)) {
            tile_no[0] = 73;
            flags = flags ^ 0b0010_0000_0000;
        } else if (Util.isAridGrass(flags)) {
            tile_no[0] = 80;
            flags = flags ^ 0b0100_0000_0000;
        } else if (Util.isDesert(flags)) {
            tile_no[0] = 87;
            flags = flags ^ 0b0110_0000_0000;
        } else if (Util.isIce(flags)) {
            tile_no[0] = 124;
            flags = flags ^ 0b1000_0000_0000;
        } else if (Util.isTundra(flags)) {
            tile_no[0] = 117;
            flags = flags ^ 0b1010_0000_0000;
        }
        String s_flags = Util.createFlagString(flags);
//                System.out.println("flags u, v: " + u + ", " + v + ": " + s_flags);
        switch (s_flags) {
            case "0124":
                tile_no[2] = 131;
                break;
            case "0125":
                tile_no[2] = 132;
                break;
            case "012":
                tile_no[2] = 130;
                break;
            case "034568":
                tile_no[1] = 64;
                break;
            case "03456":
                tile_no[1] = 64;
                break;
            case "0345":
                tile_no[1] = 60;
                break;
            case "03468":
                tile_no[1] = 62;
                break;
            case "0346":
                tile_no[1] = 62;
                break;
            case "0348":
                tile_no[1] = 58;
                break;
            case "034":
                tile_no[1] = 58;
                break;
            case "0356":
                tile_no[1] = 63;
                break;
            case "0357":
                tile_no[1] = 59;
                break;
            case "035":
                tile_no[1] = 59;
                break;
            case "03678":
                tile_no[1] = 61;
                break;
            case "0367":
                tile_no[1] = 61;
                break;
            case "0368":
                tile_no[1] = 61;
                break;
            case "036":
                tile_no[1] = 61;
                break;
            case "0378":
                tile_no[1] = 57;
                break;
            case "037":
                tile_no[1] = 57;
                break;
            case "038":
                tile_no[1] = 57;
                break;
            case "03":
                tile_no[1] = 57;
                break;
            case "124":
                tile_no[2] = 30;
                break;
            case "125":
                tile_no[2] = 31;
                break;
            case "12":
                tile_no[2] = 29;
                break;
            case "34567":
                tile_no[1] = 15;
                break;
            case "34568":
                tile_no[1] = 23;
                break;
            case "3456":
                tile_no[1] = 7;
                break;
            case "34578":
                tile_no[1] = 27;
                break;
            case "3457":
                tile_no[1] = 11;
                break;
            case "3458":
                tile_no[1] = 19;
                break;
            case "345":
                tile_no[1] = 3;
                break;
            case "3467":
                tile_no[1] = 13;
                break;
            case "3468":
                tile_no[1] = 21;
                break;
            case "346":
                tile_no[1] = 5;
                break;
            case "3478":
                tile_no[1] = 25;
                break;
            case "347":
                tile_no[1] = 9;
                break;
            case "348":
                tile_no[1] = 17;
                break;
            case "34":
                tile_no[1] = 1;
                break;
            case "3567":
                tile_no[1] = 14;
                break;
            case "3568":
                tile_no[1] = 22;
                break;
            case "356":
                tile_no[1] = 6;
                break;
            case "3578":
                tile_no[1] = 26;
                break;
            case "357":
                tile_no[1] = 10;
                break;
            case "358":
                tile_no[1] = 18;
                break;
            case "35":
                tile_no[1] = 2;
                break;
            case "3678":
                tile_no[1] = 28;
                break;
            case "367":
                tile_no[1] = 12;
                break;
            case "368":
                tile_no[1] = 20;
                break;
            case "36":
                tile_no[1] = 4;
                break;
            case "378":
                tile_no[1] = 24;
                break;
            case "37":
                tile_no[1] = 8;
                break;
            case "38":
                tile_no[1] = 16;
                break;
            case "3":
                tile_no[1] = 0;
                break;
            default:
                break;
        }

        //efstile*.bin has extra first element
        if (tile_no[1] != -1) {
            tile_no[1]++;
        }
        if (tile_no[2] != -1) {
            tile_no[2]++;
        }
        return tile_no;
    }

    public int processFlags(int flags) {

        int tile_no = 0;

        String s_flags = Util.createFlagString(flags);

//        System.out.println("s_flags: " + s_flags);
        switch (s_flags) {
            case "0124":
                tile_no = 131;
                break;
            case "0125":
                tile_no = 132;
                break;
            case "012":
                tile_no = 130;
                break;
            case "034568":
                tile_no = 64;
                break;
            case "03456":
                tile_no = 64;
                break;
            case "0345":
                tile_no = 60;
                break;
            case "03468":
                tile_no = 62;
                break;
            case "0346":
                tile_no = 62;
                break;
            case "0348":
                tile_no = 58;
                break;
            case "034":
                tile_no = 58;
                break;
            case "0356":
                tile_no = 63;
                break;
            case "0357":
                tile_no = 59;
                break;
            case "035":
                tile_no = 59;
                break;
            case "03678":
                tile_no = 61;
                break;
            case "0367":
                tile_no = 61;
                break;
            case "0368":
                tile_no = 61;
                break;
            case "036":
                tile_no = 61;
                break;
            case "0378":
                tile_no = 57;
                break;
            case "037":
                tile_no = 57;
                break;
            case "038":
                tile_no = 57;
                break;
            case "03":
                tile_no = 57;
                break;
            case "124":
                tile_no = 30;
                break;
            case "125":
                tile_no = 31;
                break;
            case "12":
                tile_no = 29;
                break;
            case "34567":
                tile_no = 15;
                break;
            case "34568":
                tile_no = 23;
                break;
            case "3456":
                tile_no = 7;
                break;
            case "34578":
                tile_no = 27;
                break;
            case "3457":
                tile_no = 11;
                break;
            case "3458":
                tile_no = 19;
                break;
            case "345":
                tile_no = 3;
                break;
            case "3467":
                tile_no = 13;
                break;
            case "3468":
                tile_no = 21;
                break;
            case "346":
                tile_no = 5;
                break;
            case "3478":
                tile_no = 25;
                break;
            case "347":
                tile_no = 9;
                break;
            case "348":
                tile_no = 17;
                break;
            case "34":
                tile_no = 1;
                break;
            case "3567":
                tile_no = 14;
                break;
            case "3568":
                tile_no = 22;
                break;
            case "356":
                tile_no = 6;
                break;
            case "3578":
                tile_no = 26;
                break;
            case "357":
                tile_no = 10;
                break;
            case "358":
                tile_no = 18;
                break;
            case "35":
                tile_no = 2;
                break;
            case "3678":
                tile_no = 28;
                break;
            case "367":
                tile_no = 12;
                break;
            case "368":
                tile_no = 20;
                break;
            case "36":
                tile_no = 4;
                break;
            case "378":
                tile_no = 24;
                break;
            case "37":
                tile_no = 8;
                break;
            case "38":
                tile_no = 16;
                break;
            case "3":
                tile_no = 0;
                break;
            default:
                break;
        }

        //efstile*.bin has extra first element
        //tile_no++;
        return tile_no;

    }

//    public void drawFlags(Graphics g) {
//        
//      int n = 0;
//                for (int k = 0; k < powers.length; k++) {
//                    if ((hex_buffer[buf_tab[i][j].x][buf_tab[i][j].y] & powers[k]) == powers[k]) {
//                        System.out.println("powers k:" + powers[k]);
//                        System.out.println("i, j:" + i + ", " + j);
//                        char c = 'a';
//                        int tmp = Math.getExponent(powers[k]);
//                        int x_draw = x + x_offset;
//                        int y_draw = y + dip + y_offset;
//
//                        
//                        
//                        if (n < 3) {
//                            y_draw += n * 10;
//                        } else if (n < 6) {
//                            y_draw += (n - 3) * 10;
//                            x_draw += 10;
//                        } else {
//                            y_draw += (n - 6) * 10;
//                            x_draw += 20;
//                        }
//
//                        if (i % 2 != 0) {
//                            y_draw += y / 2;
//                        }
//
//                        if (tmp < 10) {
//                            g.drawString(String.valueOf(tmp), x_draw, y_draw);
//                        } else {
//                            g.drawString(Character.toString((char) (c + tmp - 10)), x_draw, y_draw);
//                        }
//
//                        n++;
//                    }
//
//                }  
//        
//    }
    public void drawHexGrid(Graphics g) {

//        int x_offset = ws.planet_map_x_offset;
//        int y_offset = ws.planet_map_y_offset;
        g.setColor(Color.lightGray);
//        g.setColor(new Color(200, 200, 200, 128));
//        System.out.println("before int x");
        int x = 0; //(int) Math.round((1662 / 44) * (2 / 3));
//        System.out.println("int x: " + x);
        int y = 0; //(int) Math.round((1260 / 32) * (2 / 3));
//        System.out.println("int y: " + y);

        for (int i = 0; i < 10; i++) {
            x = 0;
            for (int j = 0; j < 13; j++) {
//                int[] x_p = {x + 39, x + 11, x + 0, x + 11, x + 39};
//                int[] y_p = {y + 40, y + 40, y + 20, y + 0, y + 0};

                int[] x_p = {x + 11, x + 0, x + 11, x + 38};
                int[] y_p = {y + 40, y + 20, y + 0, y + 0};
                int dip = 0;
                if (i == 9) {
                    y_p[0] = y + 39;
                }

                if (j % 2 == game.getMapOrigin().x % 2) {
                    dip = +20;
                }
                for (int k = 0; k < x_p.length; k++) {
                    x_p[k] += x;
                    y_p[k] += y + dip;

                }

                // if window is double size scale hexagon
                if (ws.is_double) {
                    for (int k = 0; k < x_p.length; k++) {
                        x_p[k] = x_p[k] * 2;
                        y_p[k] = y_p[k] * 2;
                    }
                }

//                g.drawPolygon(x_p, y_p, 6);
                //skip top j when i % 2 == 0
//                if ((i != 0 || (j % 2 != game.getMapOrigin().x % 2))) {
                if (j % 2 != game.getMapOrigin().x % 2 || i != 9) {
                    g.drawPolyline(x_p, y_p, x_p.length);
                }
//                }
                x += 19;
            }

            y += 20;
        }

    }
}
