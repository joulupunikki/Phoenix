/*
 * Copyright (C) 2015 joulupunikki joulupunikki@gmail.communist.invalid.
 * Copyright (C) 2016 Charles Keil ck@charleskeil.com
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
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import util.C;
import util.FN;
import util.Util;
import util.WindowSize;

/**
 * Implements planetary hex map display.
 *
 * @author joulupunikki
 */
public class PlanetMap extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
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
    Set<Hex> shielded_hexes;
    BufferedImage bi;
    WritableRaster horiz_edge;
    WritableRaster vert_edge;

    static final Color PATH_GREEN = new Color(96, 208, 64);
    static final Color PATH_RED = new Color(160, 60, 20);

    public PlanetMap(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        color_index = gui.getICM();
        game = gui.getGame();
        pallette = gui.getPallette();
        color_scaler = gui.getResources().getColorScaler();
        bi = new BufferedImage(ws.planet_map_width, ws.planet_map_height, BufferedImage.TYPE_BYTE_INDEXED, color_index);
        int d_x = 10;
        int d_y = 20;
        if (ws.is_double) {
            d_x *= 2;
            d_y *= 2;
        }
        BufferedImage tmp = Util.loadImage(FN.S_BG0_PCX, ws.is_double, pallette, 640, 480);
        WritableRaster wr = tmp.getRaster();
        horiz_edge = wr.createWritableChild(0, 0, ws.planet_map_width, d_y, 0, 0, null);
        vert_edge = wr.createWritableChild(0, 0, d_x, ws.planet_map_height, 0, 0, null);
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

        drawShield(g);

        drawStructureNames(g);

        drawUnits(g);

        if (game.getPath() != null) {
            drawPath(g);
        }

        drawSelectedStack(g);
        //drawFlags(g);

        drawHexTypes(g);
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
            if (e.isSelected()) {
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
//                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
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

//                }
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
//                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
                int dip = 0;

                if (i % 2 == 0) {
                    dip = +20;
                }

                boolean[] terrain_type = planet_grid.getHex(i, j).getTerrain();

//                    int n = 0;
//                    for (int k = 0; k < terrain_type.length; k++) {
//                        if (terrain_type[k] == true) {
//                            dx = x + x_offset;
//                            dy = 5 + y + dip + y_offset + 9 * n++;
//
//                            if (ws.is_double) {
//                                dx *= 2;
//                                dy *= 2;
//                            }
//                            g.setColor(Color.RED);
//                            g.setFont(new Font("Arial", Font.PLAIN, 15));
//                            g.drawString(Util.terrainTypeAbbrev(k), dx, dy);
//                            //g.drawString("" + i + "," + j, dx, dy);
//                            //g.drawString("" + planet_grid.getHex(i, j).getLandNr(), dx, dy);
//                        }
//                    }
                dx = x + x_offset;
                dy = y + dip + y_offset;

                if (ws.is_double) {
                    dx *= 2;
                    dy *= 2;
                }

                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.PLAIN, 10));
                String s = "";
                EnumMap gui_opt = gui.getGuiOpt();
                if (((JCheckBox) gui_opt.get(OptionsPanel.GUI_OPT.LAND_NR)).isSelected()) {
                    s += planet_grid.getHex(i, j).getLandNr() + " ";
                }
                if (((JCheckBox) gui_opt.get(OptionsPanel.GUI_OPT.HEX_XY)).isSelected()) {
                    s += i + "," + j;
                }
                g.drawString(s, dx, dy);
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
//                }
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
//                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
                int dip = 0;

                if (i % 2 == 0) {
                    dip = +20;
                }
                Structure city = game.getPlanetGrid(current_planet).getHex(i, j).getStructure();
                Hex hex = game.getPlanetGrid(current_planet).getHex(i, j);
                if (city != null && hex.isSpotted(game.getTurn())) {
                    String name = game.getStrBuild(city.type).name;

                    dx = x + x_offset;
                    dy = y + dip + y_offset + C.STRUCT_BIN_HEIGHT;

                    if (ws.is_double) {
                        dx *= 2;
                        dy *= 2;
                    }

                    drawOutlinedText(g, name, dx, dy, city);
                }

                Structure resource = game.getPlanetGrid(current_planet).getHex(i, j).getResource();
                if (resource != null && hex.isSpotted(game.getTurn())) {
                    String name = game.getStrBuild(resource.type).name;

                    dx = x + x_offset;
                    dy = y + dip + y_offset + C.STRUCT_BIN_HEIGHT;

                    if (ws.is_double) {
                        dx *= 2;
                        dy *= 2;
                    }

                    drawOutlinedText(g, name, dx, dy, resource);
                }

//                }
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

    private void drawOutlinedText(Graphics g, String name, int dx, int dy, Structure city) {
        g.setFont(ws.font_structure_name_fg);
        if (city.owner == city.prev_owner) {
            g.setColor(Color.BLACK);
        } else {
            g.setColor(Color.WHITE);
        }
        g.drawString(name, dx + ws.font_structure_name_gap, dy);
        g.drawString(name, dx - ws.font_structure_name_gap, dy);
        g.drawString(name, dx, dy - ws.font_structure_name_gap);
        g.drawString(name, dx, dy + ws.font_structure_name_gap);
        g.setColor(Util.getColor(pallette, Util.getOwnerColor(city.owner)));
        g.setFont(ws.font_structure_name_fg);
        g.drawString(name, dx, dy);
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
            if (unit.isSelected()) {
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
//                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
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
                            g.setColor(PATH_RED);
                        } else {
                            g.setColor(PATH_GREEN);
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
                                g.setColor(PATH_RED);
                            } else {
                                g.setColor(PATH_GREEN);
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

//                }
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
//                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
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
                        List<Unit> spotted = new LinkedList<>();
                        for (Unit unit : stack) {
                            if (unit.spotted[game.getTurn()]) {
                                spotted.add(unit);
                            }
                        }
                        e = spotted.get(0);
                        Util.fillRaster(wr, Util.getOwnerColor(e.owner));
                        Util.drawUnitIconEdges(wr, ws);
                        Util.writeUnit(pixel_data, e.type, unit_icons, wr, ws);

                        Graphics2D g2d = (Graphics2D) g;

                        g2d.drawImage(bi, null, dx, dy);
                        Util.writeUnitCount(g2d, ws, Util.stackSize(spotted), dx, dy, e.owner != e.prev_owner);

                    }
                }

//                }
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
//                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
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
                        Unit e = null;  //stack.get(0);
                        int nr_spotted = 0;
                        boolean spotted = false;
                        for (Unit unit : stack) {
                            if (unit.spotted[game.getTurn()]) {
                                spotted = true;
                                e = unit;
                                nr_spotted++;
                                nr_spotted += unit.cargo_list.size();
                            }
                        }
                        if (spotted) {

                            if (game.getPlanetGrid(current_planet).getHex(i, j).getStructure() == null) {

                                Util.fillRaster(wr, Util.getOwnerColor(e.owner));
                                Util.drawUnitIconEdges(wr, ws);
                                Util.writeUnit(pixel_data, e.type, unit_icons, wr, ws);

                                Graphics2D g2d = (Graphics2D) g;

                                g2d.drawImage(bi, null, dx, dy);
                                Util.writeUnitCount(g2d, ws, nr_spotted, dx, dy, e.owner != e.prev_owner);
                            } else {
                                g.setColor(Util.getColor(pallette, Util.getOwnerColor(e.owner)));
                                Util.drawBlip(g, dx, dy, ws.blip_side);
                            }
                        }
                    }
                }

//                }
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

        wr.setRect(0, 0, horiz_edge);
        wr.setRect(0, 0, vert_edge);
        wr.setRect(wr.getWidth() - vert_edge.getWidth(), 0, vert_edge);
        wr.setRect(0, wr.getHeight() - horiz_edge.getHeight(), horiz_edge);

//        int a_x = wr.getWidth();
//        int a_y = wr.getHeight();
//        int d_x = 10;
//        int d_y = 20;
//        if (ws.is_double) {
//            d_x *= 2;
//            d_y *= 2;
//        }
//        int[] data = {0};
//        for (int i = 0; i < a_x; i++) {
//            for (int j = 0; j < d_y; j++) {
//                wr.setPixel(i, j, data);
//
//            }
//
//        }
//        for (int i = 0; i < a_x; i++) {
//            for (int j = a_y - d_y; j < a_y; j++) {
//                wr.setPixel(i, j, data);
//
//            }
//
//        }
//        for (int i = 0; i < d_x; i++) {
//            for (int j = d_y; j < a_y - d_y; j++) {
//                wr.setPixel(i, j, data);
//
//            }
//
//        }
//        for (int i = a_x - d_x; i < a_x; i++) {
//            for (int j = d_y; j < a_y - d_y; j++) {
//                wr.setPixel(i, j, data);
//
//            }
//
//        }
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
//                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
                int dip = 0;

                if (i % 2 == 0) {
                    dip = +20;
                }
                // for setting spotted squares
                current_hex = hex_grid[i][j];

                // hex tile numbers in efstile*.bin
                int[] tile_no;

                tile_no = getTileNo(i, j, game);

                //skip top j when i % 2 == 0
//                if (j != origin_y || (i % 2 != 0)) {
                if (!stack_moving || 0 >= stack_move_counter || 20 <= stack_move_counter) {
                    writeHex2(x, y, dip, pixel_data, hex_tiles, tile_no, wr);
                }
//                }
                //                int t_idx = 0;
//                    writeUnit(g, x, y, dip, pixel_data, unit_icons, tile_no, wr);
//                }
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
        final int origTidx = t_idx;

        for (int i = 0; i < tile_no.length; i++) {
            if (tile_no[i] == -1) { continue; }
            
            Orientation orientation = Orientation.DEFAULT;
            if (tile_no[i] >= (Orientation.FLIPFLOP.ordinal() * 100000)) {
                orientation = Orientation.FLIPFLOP;
            } else if (tile_no[i] >= (Orientation.FLOP.ordinal() * 100000)) {
                orientation = Orientation.FLOP;
            } else if (tile_no[i] >= (Orientation.FLIP.ordinal() * 100000)) {
                orientation = Orientation.FLIP;
            }
            int t_y = (t_idx)/C.STRUCT_BIN_WIDTH;
            int t_x = (t_idx)-(t_y*C.STRUCT_BIN_WIDTH);                        
            if (orientation != Orientation.DEFAULT) {
                if ((orientation == Orientation.FLIP) || (orientation == Orientation.FLIPFLOP)) {
                    t_y = C.STRUCT_BIN_HEIGHT - 1 - t_y;
                }
                if ((orientation == Orientation.FLOP) || (orientation == Orientation.FLIPFLOP)) {
                    t_x = C.STRUCT_BIN_WIDTH - 1 - t_x;
                }
                t_idx = t_y * C.STRUCT_BIN_WIDTH + t_x;
            }
            if (i >= Layer.EDGEN.ordinal() && i <= Layer.EDGENW.ordinal()) {
                if (!TILE_EDGES[i - Layer.EDGEN.ordinal()].contains(t_x, t_y)) {
                    continue;
                };
            }

            if (i != Layer.RESOURCE.ordinal() && i != Layer.STRUCTURE.ordinal()) {
                pixel_data[0] = hex_tiles[tile_no[i] - (orientation.ordinal() * 100000)][t_idx];  
            } else {
                pixel_data[0] = structures[tile_no[i] - (orientation.ordinal() * 100000)][t_idx];
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

            t_idx = origTidx;
        }
    }

    /**
     *
     * @param u the value of u
     * @param v the value of v
     * @param game the value of game
     */
   public static int[] getTileNo(int u, int v, Game game) {
    /*
     * room for:
 [0] * 6 coastal edge tiles (one for each direction)
 [7] * 1 basic hex type (Ocean, Grass, Arid, Desert, Ice, Tundra
 [8] * 1 forest
 [9] * mountain or hill
[10] * 6 base terrain transition edges (one for each direction)
[15] * river
[16] * resource
[17] * 6 road tiles (one for each direction)
[23] * structure
     */
        final int EDGEN     = Layer.EDGEN.ordinal();
        final int EDGENW    = Layer.EDGENW.ordinal();
        final int BASE      = Layer.BASE.ordinal();
        final int FOREST    = Layer.FOREST.ordinal();
        final int MOUNTAIN  = Layer.MOUNTAIN.ordinal();
        final int FADEN     = Layer.FADEN.ordinal();
        final int FADENW    = Layer.FADENW.ordinal();
        final int RIVER = Layer.RIVER.ordinal();
        final int RESOURCE  = Layer.RESOURCE.ordinal();
        final int ROAD      = Layer.ROAD.ordinal();
        final int ROADN     = Layer.ROADN.ordinal();
        final int STRUCTURE = Layer.STRUCTURE.ordinal();
        
        int[] tile_no = { 
            -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1,
            -1
        };
        
        final int DEFAULT = Orientation.DEFAULT.ordinal() * 100000;
        final int FLIP = Orientation.FLIP.ordinal() * 100000;
        final int FLOP = Orientation.FLOP.ordinal() * 100000;
        final int FLIPFLOP = Orientation.FLIPFLOP.ordinal() * 100000;
        
        int current_planet = game.getCurrentPlanetNr();
        int[][] planet_map_flags = game.getGalaxy().getPlanetMap(current_planet);
    
        Hex h = game.getPlanetGrid(current_planet).getHex(u, v);
        Structure city = h.getStructure();
        Structure resource = h.getResource();
        Hex[] neighbour = h.getNeighbours();
        boolean[] terrain = h.getTerrain();
    
        boolean isRiver = terrain[C.RIVER] || terrain[C.DELTA];
        boolean isWater = terrain[C.OCEAN] || terrain[C.DELTA];
        boolean isOcean = terrain[C.OCEAN];
        boolean isDelta = terrain[C.DELTA];
        boolean isForest = terrain[C.TREE];
        int riverNeighbourFlags    = 0;
        int landNeighbourFlags     = 0;
        int forestNeighbourFlags   = 0;

        for (int dir = C.NORTH; dir <= C.NORTHWEST; ++dir) {
            if (neighbour[dir] == null) { continue; }          
            if (isRiver){
                if (neighbour[dir].getTerrain(C.RIVER) || neighbour[dir].getTerrain(C.DELTA))
                    riverNeighbourFlags |= (1<<(C.NORTHWEST - dir));
            }
            if (isWater 
            && !neighbour[dir].getTerrain(C.OCEAN) 
            && !neighbour[dir].getTerrain(C.DELTA)) {
                landNeighbourFlags |= (1<<(C.NORTHWEST - dir));
            }
            if (isForest && neighbour[dir].getTerrain(C.TREE)) {
                forestNeighbourFlags |= (1<<(C.NORTHWEST - dir));
            }
        }
        
        if (city != null) {
            if (h.isSpotted(game.getTurn())) {
                tile_no[STRUCTURE] = city.type;
            } else {
                tile_no[STRUCTURE] = 21;
            }
        }
        if (h.getTerrain(C.ROAD)) {
            int[] roadTiles = {70,72,69,68,71,67}; 
            tile_no[ROAD] = 66;
            assert (C.NORTHWEST == (C.NORTH + 5));
            for (int dir = C.NORTH;dir <= C.NORTHWEST;++dir) {
                assert(dir >= 0 && dir < roadTiles.length);
                if ((neighbour[dir] != null) && neighbour[dir].getTerrain(C.ROAD)) {
                    tile_no[ROADN + dir] = roadTiles[dir];
                    tile_no[ROAD] = -1;
                }
            }
        }
        
        if (resource != null && h.isSpotted(game.getTurn())) {
            tile_no[RESOURCE] = resource.type;
        }

        if (isRiver) {
            switch (riverNeighbourFlags) { // duplicates removed
                case  01: tile_no[RIVER] = 58 + DEFAULT ; break; /* 000001 */
                case  02: tile_no[RIVER] = 58 + FLIP    ; break; /* 000010 */
                case 020: tile_no[RIVER] = 58 + FLOP    ; break; /* 010000 */
                case 010: tile_no[RIVER] = 58 + FLIPFLOP; break; /* 001000 */
                
                case 040: tile_no[RIVER] = 59 + DEFAULT ; break; /* 100000 */
                case 004: tile_no[RIVER] = 59 + FLIP    ; break; /* 000100 */
                
                case 011: tile_no[RIVER] = 60 + DEFAULT ; break; /* 001001 */
                case 022: tile_no[RIVER] = 60 + FLIP    ; break; /* 010010 */
                
                case 044: tile_no[RIVER] = 61 + DEFAULT ; break; /* 100100 */
                
                case 050: tile_no[RIVER] = 62 + DEFAULT ; break; /* 101000 */
                case 024: tile_no[RIVER] = 62 + FLIP    ; break; /* 010100 */
                case 042: tile_no[RIVER] = 62 + FLOP    ; break; /* 100010 */
                case 005: tile_no[RIVER] = 62 + FLIPFLOP; break; /* 000101 */
                
                case 052: tile_no[RIVER] = 63 + DEFAULT ; break; /* 101010 */
                case 025: tile_no[RIVER] = 63 + FLIP    ; break; /* 010101 */
                
                case   0: tile_no[RIVER] = 64 + DEFAULT ; break; /* 000000 */
                
                case 012: tile_no[RIVER] = 65 + DEFAULT ; break; /* 001010 */
                case 021: tile_no[RIVER] = 65 + FLIP    ; break; /* 010001 */
                default:  tile_no[RIVER] = 116; break;
            }
        }
      
        if (isDelta) {
            int waterNeighborFlags = ~landNeighbourFlags & 077;
            int deltaTile = (waterNeighborFlags<<6) | riverNeighbourFlags;
            switch (deltaTile) { // duplicates commented out
                case 00110: tile_no[RIVER] =  94 + DEFAULT ; break; /* 000001 0010000 */
                case 00220: tile_no[RIVER] =  94 + FLIP    ; break; /* 000010 0100000 */
                case 02002: tile_no[RIVER] =  94 + FLOP    ; break; /* 010000 0000100 */
                case 01001: tile_no[RIVER] =  94 + FLIPFLOP; break; /* 001000 0000010 */

                case 04004: tile_no[RIVER] =  95 + DEFAULT ; break; /* 100000 0001000 */
                case 00440: tile_no[RIVER] =  95 + FLIP    ; break; /* 000100 1000000 */
//              case 04004: tile_no[RIVER] =  95 + FLOP    ; break; /* 100000 0001000 */
//              case 00440: tile_no[RIVER] =  95 + FLIPFLOP; break; /* 000100 1000000 */

                case 04012: tile_no[RIVER] =  96 + DEFAULT ; break; /* 100000 0010100 */
                case 00421: tile_no[RIVER] =  96 + FLIP    ; break; /* 000100 0100010 */
//              case 04012: tile_no[RIVER] =  96 + FLOP    ; break; /* 100000 0010100 */
//              case 00421: tile_no[RIVER] =  96 + FLIPFLOP; break; /* 000100 0100010 */

                case 01042: tile_no[RIVER] =  97 + DEFAULT ; break; /* 001000 1000100 */
                case 02005: tile_no[RIVER] =  97 + FLIP    ; break; /* 010000 0001010 */
                case 00250: tile_no[RIVER] =  97 + FLOP    ; break; /* 000010 1010000 */
                case 00124: tile_no[RIVER] =  97 + FLIPFLOP; break; /* 000001 0101000 */

                case 00210: tile_no[RIVER] =  98 + DEFAULT ; break; /* 000010 0010000 */
                case 00120: tile_no[RIVER] =  98 + FLIP    ; break; /* 000001 0100000 */
                case 01002: tile_no[RIVER] =  98 + FLOP    ; break; /* 001000 0000100 */
                case 02001: tile_no[RIVER] =  98 + FLIPFLOP; break; /* 010000 0000010 */

                case 04010: tile_no[RIVER] =  99 + DEFAULT ; break; /* 100000 0010000 */
                case 00420: tile_no[RIVER] =  99 + FLIP    ; break; /* 000100 0100000 */
                case 04002: tile_no[RIVER] =  99 + FLOP    ; break; /* 100000 0000100 */
                case 00401: tile_no[RIVER] =  99 + FLIPFLOP; break; /* 000100 0000010 */

                case 00104: tile_no[RIVER] = 100 + DEFAULT ; break; /* 000001 0001000 */
                case 00240: tile_no[RIVER] = 100 + FLIP    ; break; /* 000010 1000000 */
                case 02004: tile_no[RIVER] = 100 + FLOP    ; break; /* 010000 0001000 */
                case 01040: tile_no[RIVER] = 100 + FLIPFLOP; break; /* 001000 1000000 */

                case 01324: tile_no[RIVER] = 101 + DEFAULT ; break; /* 001011 0101000 */
                case 02350: tile_no[RIVER] = 101 + FLIP    ; break; /* 010011 1010000 */
                case 03205: tile_no[RIVER] = 101 + FLOP    ; break; /* 011010 0001010 */
                case 03142: tile_no[RIVER] = 101 + FLIPFLOP; break; /* 011001 1000100 */

                case 04124: tile_no[RIVER] = 102 + DEFAULT ; break; /* 100001 0101000 */
                case 00650: tile_no[RIVER] = 102 + FLIP    ; break; /* 000110 1010000 */
                case 06005: tile_no[RIVER] = 102 + FLOP    ; break; /* 110000 0001010 */
                case 01442: tile_no[RIVER] = 102 + FLIPFLOP; break; /* 001100 1000100 */

                case 05124: tile_no[RIVER] = 103 + DEFAULT ; break; /* 101001 0101000 */
                case 02650: tile_no[RIVER] = 103 + FLIP    ; break; /* 010110 1010000 */
                case 06205: tile_no[RIVER] = 103 + FLOP    ; break; /* 110010 0001010 */
                case 01542: tile_no[RIVER] = 103 + FLIPFLOP; break; /* 001101 1000100 */

                case 00324: tile_no[RIVER] = 104 + DEFAULT ; break; /* 000011 0101000 */
                case 00350: tile_no[RIVER] = 104 + FLIP    ; break; /* 000011 1010000 */
                case 03005: tile_no[RIVER] = 104 + FLOP    ; break; /* 011000 0001010 */
                case 03042: tile_no[RIVER] = 104 + FLIPFLOP; break; /* 011000 1000100 */

                case 04324: tile_no[RIVER] = 105 + DEFAULT ; break; /* 100011 0101000 */
                case 00750: tile_no[RIVER] = 105 + FLIP    ; break; /* 000111 1010000 */
                case 07005: tile_no[RIVER] = 105 + FLOP    ; break; /* 111000 0001010 */
                case 03442: tile_no[RIVER] = 105 + FLIPFLOP; break; /* 011100 1000100 */

                case 05324: tile_no[RIVER] = 106 + DEFAULT ; break; /* 101011 0101000 */
                case 02750: tile_no[RIVER] = 106 + FLIP    ; break; /* 010111 1010000 */
                case 07205: tile_no[RIVER] = 106 + FLOP    ; break; /* 111010 0001010 */
                case 03542: tile_no[RIVER] = 106 + FLIPFLOP; break; /* 011101 1000100 */

                case 06112: tile_no[RIVER] = 107 + DEFAULT ; break; /* 110001 0010100 */
                case 01621: tile_no[RIVER] = 107 + FLIP    ; break; /* 001110 0100010 */
//              case 06112: tile_no[RIVER] = 107 + FLOP    ; break; /* 110001 0010100 */
//              case 01621: tile_no[RIVER] = 107 + FLIPFLOP; break; /* 001110 0100010 */

                case 06512: tile_no[RIVER] = 108 + DEFAULT ; break; /* 110101 0010100 */
                case 05621: tile_no[RIVER] = 108 + FLIP    ; break; /* 101110 0100010 */
//              case 06512: tile_no[RIVER] = 108 + FLOP    ; break; /* 110101 0010100 */
//              case 05621: tile_no[RIVER] = 108 + FLIPFLOP; break; /* 101110 0100010 */

                case 04112: tile_no[RIVER] = 109 + DEFAULT ; break; /* 100001 0010100 */
                case 00621: tile_no[RIVER] = 109 + FLIP    ; break; /* 000110 0100010 */
                case 06012: tile_no[RIVER] = 109 + FLOP    ; break; /* 110000 0010100 */
                case 01421: tile_no[RIVER] = 109 + FLIPFLOP; break; /* 001100 0100010 */

                case 04512: tile_no[RIVER] = 110 + DEFAULT ; break; /* 100101 0010100 */
                case 04621: tile_no[RIVER] = 110 + FLIP    ; break; /* 100110 0100010 */
                case 06412: tile_no[RIVER] = 110 + FLOP    ; break; /* 110100 0010100 */
                case 05421: tile_no[RIVER] = 110 + FLIPFLOP; break; /* 101100 0100010 */

                case 04110: tile_no[RIVER] = 111 + DEFAULT ; break; /* 100001 0010000 */
                case 00620: tile_no[RIVER] = 111 + FLIP    ; break; /* 000110 0100000 */
                case 06002: tile_no[RIVER] = 111 + FLOP    ; break; /* 110000 0000100 */
                case 01401: tile_no[RIVER] = 111 + FLIPFLOP; break; /* 001100 0000010 */

                case 00310: tile_no[RIVER] = 112 + DEFAULT ; break; /* 000011 0010000 */
                case 00320: tile_no[RIVER] = 112 + FLIP    ; break; /* 000011 0100000 */
                case 03002: tile_no[RIVER] = 112 + FLOP    ; break; /* 011000 0000100 */
                case 03001: tile_no[RIVER] = 112 + FLIPFLOP; break; /* 011000 0000010 */

                case 04310: tile_no[RIVER] = 113 + DEFAULT ; break; /* 100011 0010000 */
                case 00720: tile_no[RIVER] = 113 + FLIP    ; break; /* 000111 0100000 */
                case 07002: tile_no[RIVER] = 113 + FLOP    ; break; /* 111000 0000100 */
                case 03401: tile_no[RIVER] = 113 + FLIPFLOP; break; /* 011100 0000010 */

                case 04104: tile_no[RIVER] = 114 + DEFAULT ; break; /* 100001 0001000 */
                case 00640: tile_no[RIVER] = 114 + FLIP    ; break; /* 000110 1000000 */
                case 06004: tile_no[RIVER] = 114 + FLOP    ; break; /* 110000 0001000 */
                case 01440: tile_no[RIVER] = 114 + FLIPFLOP; break; /* 001100 1000000 */

                case 06104: tile_no[RIVER] = 115 + DEFAULT ; break; /* 110001 0001000 */
                case 01640: tile_no[RIVER] = 115 + FLIP    ; break; /* 001110 1000000 */
//              case 06104: tile_no[RIVER] = 115 + FLOP    ; break; /* 110001 0001000 */
//              case 01640: tile_no[RIVER] = 115 + FLIPFLOP; break; /* 001110 1000000 */
                
                default:    tile_no[RIVER] = 116; break;
            }
        }
        if (isForest) {
            int forestTile = forestNeighbourFlags;
            switch (forestTile) {
                case 013: /* 001011 */
                case 023: /* 010011 */
                case 003: /* 000011 */ tile_no[FOREST] =  1;  break;
                case 045: /* 100101 */
                case 051: /* 101001 */
                case 041: /* 100001 */ tile_no[FOREST] =  2;  break;
                case 064: /* 110100 */
                case 062: /* 110010 */
                case 060: /* 110000 */ tile_no[FOREST] =  3;  break;
                case 031: /* 011001 */
                case 032: /* 011010 */
                case 030: /* 011000 */ tile_no[FOREST] =  4;  break;
                case 035: /* 011101 */
                case 054: /* 101100 */
                case 015: /* 001101 */
                case 014: /* 001100 */ tile_no[FOREST] =  5;  break;
                case 026: /* 010110 */
                case 046: /* 100110 */
                case 006: /* 000110 */ tile_no[FOREST] =  6;  break;
                case 053: /* 101011 */
                case 043: /* 100011 */ tile_no[FOREST] =  7;  break;
                case 065: /* 110101 */
                case 061: /* 110001 */ tile_no[FOREST] =  8;  break;
                case 072: /* 111010 */
                case 070: /* 111000 */ tile_no[FOREST] =  9;  break;
                case 034: /* 011100 */ tile_no[FOREST] = 10;  break;
                case 056: /* 101110 */
                case 016: /* 001110 */ tile_no[FOREST] = 11;  break;
                case 027: /* 010111 */
                case 007: /* 000111 */ tile_no[FOREST] = 12;  break;
                case 063: /* 110011 */ tile_no[FOREST] = 13;  break;
                case 071: /* 111001 */ tile_no[FOREST] = 14;  break;
                case 074: /* 111100 */ tile_no[FOREST] = 15;  break;
                case 036: /* 011110 */ tile_no[FOREST] = 16;  break;
                case 017: /* 001111 */ tile_no[FOREST] = 17;  break;
                case 047: /* 100111 */ tile_no[FOREST] = 18;  break;
                case 073: /* 111011 */ tile_no[FOREST] = 19;  break;
                case 075: /* 111101 */ tile_no[FOREST] = 20;  break;
                case 076: /* 111110 */ tile_no[FOREST] = 21;  break;
                case 037: /* 011111 */ tile_no[FOREST] = 22;  break;
                case 057: /* 101111 */ tile_no[FOREST] = 23;  break;
                case 067: /* 110111 */ tile_no[FOREST] = 24;  break;
                case 033: /* 011011 */ tile_no[FOREST] = 25;  break;
                case 055: /* 101101 */ tile_no[FOREST] = 26;  break;
                case 066: /* 110110 */ tile_no[FOREST] = 27;  break;
                case 077: /* 111111 */ tile_no[FOREST] = 28;  break;
                case 000: /* 000000 */
                case 001: /* 000001 */
                case 002: /* 000010 */
                case 004: /* 000100 */
                case 010: /* 001000 */
                case 020: /* 010000 */
                case 040: /* 100000 */
                case 044: /* 100100 */
                case 050: /* 101000 */
                case 052: /* 101010 */
                case 042: /* 100010 */
                case 012: /* 001010 */
                case 025: /* 010101 */
                case 005: /* 000101 */
                case 011: /* 001001 */
                case 022: /* 010010 */
                case 021: /* 010001 */ 
                case 024: /* 010100 */
                    tile_no[FOREST] = 29;  break;
                default : 
                {
                    if (C.DEBUG_PRINT > 0) {
                        if (!missingForestTiles.contains(forestTile)) {
                            missingForestTiles.add(forestTile); // to only print message one time
                            System.out.println("missing forest tile #0" + Integer.toOctalString(forestTile) + " at (" + Integer.toString(u) + "," + Integer.toString(v) + ")." );
                        }
                    }
                    tile_no[FOREST] = 116;
                }
            }
        }
        if (isOcean) {
            switch(landNeighbourFlags) { // duplicates removed
                case 076: tile_no[BASE] = 33 + DEFAULT;   break; /* 111110 */
                case 075: tile_no[BASE] = 33 + FLIP;      break; /* 111101 */
                case 057: tile_no[BASE] = 33 + FLOP;      break; /* 101111 */
                case 067: tile_no[BASE] = 33 + FLIPFLOP;  break; /* 110111 */

                case 037: tile_no[BASE] = 34 + DEFAULT;   break; /* 011111 */
                case 073: tile_no[BASE] = 34 + FLIP;      break; /* 111011 */

                case 066: tile_no[BASE] = 35 + DEFAULT;   break; /* 110110 */
                case 055: tile_no[BASE] = 35 + FLIP;      break; /* 101101 */

                case 033: tile_no[BASE] = 36 + DEFAULT;   break; /* 011011 */

                case 027: tile_no[BASE] = 37 + DEFAULT;   break; /* 010111 */
                case 053: tile_no[BASE] = 37 + FLIP;      break; /* 101011 */
                case 035: tile_no[BASE] = 37 + FLOP;      break; /* 011101 */
                case 072: tile_no[BASE] = 37 + FLIPFLOP;  break; /* 111010 */

                case 025: tile_no[BASE] = 38 + DEFAULT;   break; /* 010101 */
                case 052: tile_no[BASE] = 38 + FLIP;      break; /* 101010 */

                case 026: tile_no[BASE] = 39 + DEFAULT;   break; /* 010110 */
                case 051: tile_no[BASE] = 39 + FLIP;      break; /* 101001 */
                case 015: tile_no[BASE] = 39 + FLOP;      break; /* 001101 */
                case 062: tile_no[BASE] = 39 + FLIPFLOP;  break; /* 110010 */

                case 054: tile_no[BASE] = 40 + DEFAULT;   break; /* 101100 */
                case 064: tile_no[BASE] = 40 + FLIP;      break; /* 110100 */
                case 046: tile_no[BASE] = 40 + FLOP;      break; /* 100110 */
                case 045: tile_no[BASE] = 40 + FLIPFLOP;  break; /* 100101 */

                case 016: tile_no[BASE] = 41 + DEFAULT;   break; /* 001110 */
                case 061: tile_no[BASE] = 41 + FLIP;      break; /* 110001 */

                case 034: tile_no[BASE] = 42 + DEFAULT;   break; /* 011100 */
                case 070: tile_no[BASE] = 42 + FLIP;      break; /* 111000 */
                case 007: tile_no[BASE] = 42 + FLOP;      break; /* 000111 */
                case 043: tile_no[BASE] = 42 + FLIPFLOP;  break; /* 100011 */

                case 012: tile_no[BASE] = 43 + DEFAULT;   break; /* 001010 */
                case 021: tile_no[BASE] = 43 + FLIP;      break; /* 010001 */
  
                case 024: tile_no[BASE] = 44 + DEFAULT;   break; /* 010100 */
//              case 050: tile_no[BASE] = 44 + FLIP;      break; /* 101000 */
                case 005: tile_no[BASE] = 44 + FLOP;      break; /* 000101 */
//              case 042: tile_no[BASE] = 44 + FLIPFLOP;  break; /* 100010 */

                case 014: tile_no[BASE] = 45 + DEFAULT;   break; /* 001100 */
                case 060: tile_no[BASE] = 45 + FLIP;      break; /* 110000 */
                case 006: tile_no[BASE] = 45 + FLOP;      break; /* 000110 */
                case 041: tile_no[BASE] = 45 + FLIPFLOP;  break; /* 100001 */

                case 030: tile_no[BASE] = 46 + DEFAULT;   break; /* 011000 */
                case 003: tile_no[BASE] = 46 + FLOP;      break; /* 000011 */

                case 010: tile_no[BASE] = 47 + DEFAULT;   break; /* 001000 */
                case 020: tile_no[BASE] = 47 + FLIP;      break; /* 010000 */
                case 002: tile_no[BASE] = 47 + FLOP;      break; /* 000010 */
                case 001: tile_no[BASE] = 47 + FLIPFLOP;  break; /* 000001 */

                case 004: tile_no[BASE] = 48 + DEFAULT;   break; /* 000100 */
                case 040: tile_no[BASE] = 48 + FLIP;      break; /* 100000 */

                case 000: tile_no[BASE] = 49 + DEFAULT;   break; /* 000000 */

                case 044: tile_no[BASE] = 50 + DEFAULT;   break; /* 100100 */

                case 022: tile_no[BASE] = 51 + DEFAULT;   break; /* 010010 */
                case 011: tile_no[BASE] = 51 + FLIP;      break; /* 001001 */

                case 077: tile_no[BASE] = 52 + DEFAULT;   break; /* 111111 */

                case 036: tile_no[BASE] = 53 + DEFAULT;   break; /* 011110 */
                case 071: tile_no[BASE] = 53 + FLIP;      break; /* 111001 */
                case 017: tile_no[BASE] = 53 + FLOP;      break; /* 001111 */
                case 063: tile_no[BASE] = 53 + FLIPFLOP;  break; /* 110011 */

                case 074: tile_no[BASE] = 54 + DEFAULT;   break; /* 111100 */
                case 047: tile_no[BASE] = 54 + FLOP;      break; /* 100111 */

                case 056: tile_no[BASE] = 55 + DEFAULT;   break; /* 101110 */
                case 065: tile_no[BASE] = 55 + FLIP;      break; /* 110101 */

                case 050: tile_no[BASE] = 56 + DEFAULT;   break; /* 101000 */
//              case 024: tile_no[BASE] = 56 + FLIP;      break; /* 010100 */
                case 042: tile_no[BASE] = 56 + FLOP;      break; /* 100010 */
//              case 005: tile_no[BASE] = 56 + FLIPFLOP;  break; /* 000101 */

                case 032: tile_no[BASE] = 57 + DEFAULT;   break; /* 011010 */
                case 031: tile_no[BASE] = 57 + FLIP;      break; /* 011001 */
                case 013: tile_no[BASE] = 57 + FLOP;      break; /* 001011 */
                case 023: tile_no[BASE] = 57 + FLIPFLOP;  break; /* 010011 */
                default : tile_no[BASE] = 116; break;
            } 
        } else {
            if      (terrain[C.GRASS])      tile_no[BASE] =  73;
            else if (terrain[C.ARID_GRASS]) tile_no[BASE] =  80;
            else if (terrain[C.DESERT])     tile_no[BASE] =  87;
            else if (terrain[C.ICE])        tile_no[BASE] = 124;
            else if (terrain[C.TUNDRA])     tile_no[BASE] = 117;
            for (int i = FADEN; i <= FADENW; ++i) {
                if (neighbour[i-FADEN] == null) { continue; }
                boolean[] neighbour_terrain = neighbour[i-FADEN].getTerrain();
                if (neighbour_terrain[C.GRASS]      && !terrain[C.GRASS])      { tile_no[i] =  74 + (i-FADEN); } 
                if (neighbour_terrain[C.ARID_GRASS] && !terrain[C.ARID_GRASS]) { tile_no[i] =  81 + (i-FADEN); } 
                if (neighbour_terrain[C.DESERT]     && !terrain[C.DESERT])     { tile_no[i] =  88 + (i-FADEN); } 
                if (neighbour_terrain[C.ICE] && !terrain[C.ICE]) {
                    tile_no[i] = 125 + (i - FADEN);
                }
                if (neighbour_terrain[C.TUNDRA]     && !terrain[C.TUNDRA])     { tile_no[i] = 118 + (i-FADEN); }
            }            
        }
        if (landNeighbourFlags != 0) { 
            assert (EDGEN == 0 && EDGENW == 5);
            for (int i = EDGEN; i <= EDGENW; ++i) {
              if (neighbour[i-EDGEN] == null) { continue; }
              if      (neighbour[i-EDGEN].getTerrain(C.GRASS))      { tile_no[i] =  73; }
              else if (neighbour[i-EDGEN].getTerrain(C.ARID_GRASS)) { tile_no[i] =  80; }
              else if (neighbour[i-EDGEN].getTerrain(C.DESERT))     { tile_no[i] =  87; }
              else if (neighbour[i-EDGEN].getTerrain(C.ICE))        { tile_no[i] = 124; }
              else if (neighbour[i-EDGEN].getTerrain(C.TUNDRA))     { tile_no[i] = 117; }        
            }
        }
        {
            int flags = planet_map_flags[u][v];
            final int mask = 0b0000_1111_1111;
            flags &= mask;
            String s_flags = Util.createFlagString(flags);

            switch (s_flags) {
                case "0124": tile_no[MOUNTAIN] = 132; break;
                case "0125": tile_no[MOUNTAIN] = 133; break;
                case "012": tile_no[MOUNTAIN] = 131; break;
                case "124": tile_no[MOUNTAIN] = 31; break;
                case "125": tile_no[MOUNTAIN] = 32; break;
                case "12": tile_no[MOUNTAIN] = 30; break;
                default: break;
            }
        }
        return tile_no;
    }

    public static int processFlags(int flags) {

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
        g.setColor(gui.getResources().getColor(32));
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

                int[] x_p = {x + 11, x + 0, x + 11, x + 38, x + 49, x + 38};
                int[] y_p = {y + 40, y + 20, y + 0, y + 0, y + 20, y + 40};
                int dip = 0;
//                if (i == 9) {
//                    y_p[0] = y + 39;
//                }

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

                    g.drawPolygon(x_p, y_p, x_p.length);
                }
//                }
                x += 19;
            }

            y += 20;
        }

    }

    public void drawShield(Graphics g) {
        Structure shield = game.getPlanet(game.getCurrentPlanetNr()).getShield();
        if (shield != null && game.getEfs_ini().shield_radius > -1) {
            shielded_hexes = Util.getHexesWithinRadiusOf(game.getHexFromPXY(shield.p_idx, shield.x, shield.y), game.getEfs_ini().shield_radius);
        } else {
            return;
        }
        Point origin = game.getMapOrigin();
        g.setColor(gui.getColorCycleColor());
        int origin_x = origin.x;
        int origin_y = origin.y;

        int x = 0;
        int y = 0;
        int counter = 0;
        //need to roll-over i to 0 when i = 43, see end of loop
        for (int i = origin_x; counter < 13; i++) {
            y = 0;

            for (int j = origin_y; j < origin_y + 10; j++) {
//                if ((i % 2 != 0 || j != 31) && (i % 2 != 0 || j != origin_y + 9)) {
                int dip = 0;

                if (i % 2 == 0) {
                    dip = +20;
                }
                current_hex = hex_grid[i][j];
                if (shielded_hexes.isEmpty()) {
                    return;
                }
                if (shielded_hexes.remove(current_hex) && current_hex.isSpotted(current_faction)) {
                    int[] x_p = {x + 11, x + 0, x + 11, x + 38, x + 49, x + 38, x + 11};
                    int[] y_p = {y + 40, y + 20, y + 0, y + 0, y + 20, y + 40, y + 40};
//                        int[] x_p = {x + 10, x + 0, x + 10, x + 37, x + 48, x + 37};
//                        int[] y_p = {y + 39, y + 19, y + 0, y + 0, y + 19, y + 39};

                    if (j - origin_y == 9) {
                        y_p[0] = y + 39;
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
                    if (i % 2 != game.getMapOrigin().x % 2 || j - origin_y != 9) {

                        g.drawPolyline(x_p, y_p, x_p.length);
                        for (int k = 0; k < x_p.length; k++) {
                            y_p[k]--;
                        }
                        g.drawPolyline(x_p, y_p, x_p.length);
                    }
                }
//                }
                y += 20;

            }

            x += 19;

            // need to roll-over i to 0 when i = 43
            if (i == C.PLANET_MAP_WIDTH - 1) {
                i = -1;

            }
            counter++;

        }

    }
    public enum Orientation {
        DEFAULT, FLIP, FLOP, FLIPFLOP;
    };
    public enum Layer {
        EDGEN,
        EDGENE,
        EDGEE,
        EDGES,
        EDGESW,
        EDGENW,
        BASE,
        FADEN,
        FADENE,
        FADESE,
        FADES,
        FADESW,
        FADENW,
        FOREST,
        MOUNTAIN,
        RIVER,
        RESOURCE,
        ROAD,
        ROADN,
        ROADNE,
        ROADSE,
        ROADS,
        ROADSW,
        ROADNW,
        STRUCTURE;
    }
    static final Polygon[] TILE_EDGES  = new Polygon[]{
        new Polygon(new int[]{10,37,27,20},      new int[]{ 0, 0,10,10}, 4)
    ,   new Polygon(new int[]{26,37,48,48,26},   new int[]{10, 0,11,21,21}, 5)
    ,   new Polygon(new int[]{27,48,48,37,27},   new int[]{20,20,28,40,28}, 5)
    ,   new Polygon(new int[]{20,27,38,9},       new int[]{29,29,40,40}, 4)
    ,   new Polygon(new int[]{ 0,20,26,10, 0},   new int[]{20,20,27,39,31}, 5)
    ,   new Polygon(new int[]{ 9+1,20,20, 0, 0}, new int[]{ 0,11,19,20, 9}, 5)
    };
    static Set<Integer> missingForestTiles = new HashSet<>();
}
