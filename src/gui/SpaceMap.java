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
package gui;

import galaxyreader.JumpGate;
import galaxyreader.Planet;
import galaxyreader.Unit;
import game.Game;
import game.Square;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.nio.ByteOrder;
import java.util.List;
import javax.swing.JPanel;
import util.C;
import util.FN;
import util.Util;
import util.WindowSize;

/**
 *
 * @author joulupunikki
 */
public class SpaceMap extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Gui gui;
    private Game game;
    private int[][] planet_images;
    private int[][] unit_icons;
    private WindowSize ws;
    private IndexColorModel color_index;

    public SpaceMap(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();

        game = gui.getGame();

        loadPlanetImages();

        unit_icons = Gui.getUnitIcons();

        color_index = gui.getICM();

    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        renderSpaceMap(g);

    }

    public void renderSpaceMap(Graphics g) {

        drawMap(g);
        drawJumpRoutes(g);
        drawPlanets(g);
        drawUnits(g);
    }

    public void drawJumpRoutes(Graphics g) {
        List<JumpGate> jump_routes = game.getJumpGates();

        Point origin = game.getSpaceMapOrigin();
        JumpGate jump_path = game.getJumpPath();
        g.setColor(gui.getColorCycleColor());
        int s_w = ws.space_map_square_width;
        int s_h = ws.space_map_square_height;

        int x_offset = s_w / 2;
        int y_offset = s_h / 2;

        for (JumpGate jg : jump_routes) {
            if (jump_path != null && jump_path.getX1() == jg.getX1()
                    && jump_path.getY1() == jg.getY1()
                    && jump_path.getX2() == jg.getX2()
                    && jump_path.getY2() == jg.getY2()) {
                g.setColor(Color.GREEN);
            }
            g.drawLine((jg.getX1() - origin.x) * s_w + x_offset,
                    (jg.getY1() - origin.y) * s_h + y_offset,
                    (jg.getX2() - origin.x) * s_w + x_offset,
                    (jg.getY2() - origin.y) * s_h + y_offset);
            g.setColor(gui.getColorCycleColor());
        }

    }

    public void drawPlanets(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        byte[][] pallette = gui.getPallette();

        Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();

        g.setColor(Color.WHITE);

        Point origin = game.getSpaceMapOrigin();

        int dx = ws.space_map_square_width;
        int dy = ws.space_map_square_height;

        int x = 0;
        int y = 0;
        for (int i = origin.x; i < origin.x + 15; i++) {
            y = 0;
            for (int j = origin.y; j < origin.y + 13; j++) {

                if (j < galaxy_grid[0].length - 1 && galaxy_grid[i][j + 1].planet != null) {
                    g.setColor(Color.WHITE);
                    g.drawString(galaxy_grid[i][j + 1].planet.name, x, y);
                }

                if (galaxy_grid[i][j].planet != null) {

                    int[] planet_image = null;
                    if (ws.is_double) {
                        planet_image = Util.scale2XImage(planet_images[127], 1024, 32);
                    } else {
                        planet_image = planet_images[127];
                    }
//                    planet_image = Util.loadSquare("bin/efsplan.bin", 1024, 1024);

                    BufferedImage bi2 = new BufferedImage(ws.planet_image_side, ws.planet_image_side, BufferedImage.TYPE_BYTE_INDEXED, color_index);
                    WritableRaster wr = bi2.getRaster();
//                    System.out.println("black: " + planet_image[0]);
                    wr.setPixels(0, 0, ws.planet_image_side, ws.planet_image_side, planet_image);
                    g2d.drawImage(bi2, null, x, y);
                }
                y += dy;

            }
            x += dx;
        }

    }

    /**
     * Draw planets and squares.
     *
     * @param g
     */
    public void drawMap(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, ws.space_map_width, ws.space_map_height);

        byte[][] pallette = gui.getPallette();
        BufferedImage bi = Util.loadStarFld2(FN.S_STARFLD2_PCX, ws.is_double, pallette, 480, 416);

        g2d.drawImage(bi, null, ws.starfld2_x_pos, ws.starfld2_y_pos);

        Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();

        g.setColor(Color.WHITE);

        Point origin = game.getSpaceMapOrigin();

        int dx = ws.space_map_square_width;
        int dy = ws.space_map_square_height;

        int x = 0;
        int y = 0;
        for (int i = origin.x; i < origin.x + 15; i++) {
            y = 0;
            for (int j = origin.y; j < origin.y + 13; j++) {

                g.setColor(Color.DARK_GRAY);

                if (galaxy_grid[i][j].stack_owner > -1) {
                    g.drawRect(x, y, dx, dy);
                }

                /*
                 * draw grid squares
                 */
//                if (i > 0 && j > 0) {
//                    if (galaxy_grid[i - 1][j - 1].planet != null) {
//                        g.drawRect(x, y, dx, dy);
//                    }
//
//                }
//
//                if (j > 0) {
//                    if (galaxy_grid[i][j - 1].planet != null) {
//                        g.drawRect(x, y, dx, dy);
//
//                    }
//                }
//
//                if (i < galaxy_grid.length - 1 && j > 0) {
//                    if (galaxy_grid[i + 1][j - 1].planet != null) {
//                        g.drawRect(x, y, dx, dy);
//                    }
//                }
//                if (i > 0) {
//                    if (galaxy_grid[i - 1][j].planet != null) {
//                        g.drawRect(x, y, dx, dy);
//                    }
//                }
//                if (i < galaxy_grid.length - 1) {
//                    if (galaxy_grid[i + 1][j].planet != null) {
//                        g.drawRect(x, y, dx, dy);
//                    }
//                }
//                if (i > 0 && j < galaxy_grid[0].length - 1) {
//                    if (galaxy_grid[i - 1][j + 1].planet != null) {
//                        g.drawRect(x, y, dx, dy);
//                    }
//                }
//                if (j < galaxy_grid[0].length - 1) {
//                    if (galaxy_grid[i][j + 1].planet != null) {
//                        g.drawRect(x, y, dx, dy);
//                    }
//                }
//                if (i < galaxy_grid.length - 1 && j < galaxy_grid[0].length - 1) {
//                    if (galaxy_grid[i + 1][j + 1].planet != null) {
//                        g.drawRect(x, y, dx, dy);
//                    }
//                }
//                if (j < galaxy_grid[0].length - 1 && galaxy_grid[i][j + 1].planet != null) {
//                    g.setColor(Color.WHITE);
//                    g.drawString(galaxy_grid[i][j + 1].planet.name, x, y);
//                }
//
//
//
//                if (galaxy_grid[i][j].planet != null) {
//
//                    int[] planet_image = null;
//                    if (ws.is_double) {
//                        planet_image = Util.scale2XImage(planet_images[127], 1024, 32);
//                    } else {
//                        planet_image = planet_images[127];
//                    }
////                    planet_image = Util.loadSquare("bin/efsplan.bin", 1024, 1024);
//
//                    BufferedImage bi2 = new BufferedImage(ws.planet_image_side, ws.planet_image_side, BufferedImage.TYPE_BYTE_INDEXED, color_index);
//                    WritableRaster wr = bi2.getRaster();
////                    System.out.println("black: " + planet_image[0]);
//                    wr.setPixels(0, 0, ws.planet_image_side, ws.planet_image_side, planet_image);
//                    g2d.drawImage(bi2, null, x, y);
//                }
                y += dy;

            }
            x += dx;
        }

    }

    /**
     * Draw units.
     *
     * @param g
     */
    public void drawUnits(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        BufferedImage bi = new BufferedImage(ws.unit_icon_size, ws.unit_icon_size, BufferedImage.TYPE_BYTE_INDEXED, color_index);
//        WritableRaster wr = bi.getRaster();

//        int x_offset = (C.STRUCT_BIN_WIDTH - C.EFSUNIT_BIN_WIDTH) / 2;
//        int y_offset = (C.STRUCT_BIN_HEIGHT - C.EFSUNIT_BIN_HEIGHT) / 2;
        int[] pixel_data = new int[1];

        Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();

        g.setColor(Color.WHITE);

        Point origin = game.getSpaceMapOrigin();

        int dx = ws.space_map_square_width;
        int dy = ws.space_map_square_height;

        Planet planet;
        int x = 0;
        int y = 0;
        for (int i = origin.x; i < origin.x + 15; i++) {
            y = 0;
            for (int j = origin.y; j < origin.y + 13; j++) {

                int owner = galaxy_grid[i][j].stack_owner;
                if (owner > -1) {
//                                    System.out.println("owner " + owner);

                    drawUnit(galaxy_grid[i][j].parent_planet, pixel_data, ws, bi, g2d, x, y, owner, i, j);

                }
//                if (i > 0 && j > 0) {
//                    planet = galaxy_grid[i - 1][j - 1].planet;
//                    if (planet != null) {
//                        drawUnit(planet, pixel_data, ws, bi, g2d, x, y, C.STIGMATA);
//                    }
//                }
//
//                if (j > 0) {
//                    planet = galaxy_grid[i][j - 1].planet;
//                    if (planet != null) {
//                        drawUnit(planet, pixel_data, ws, bi, g2d, x, y, C.VAU);
//                    }
//                }
//
//                if (i < galaxy_grid.length - 1 && j > 0) {
//                    planet = galaxy_grid[i + 1][j - 1].planet;
//                    if (planet != null) {
//                        drawUnit(planet, pixel_data, ws, bi, g2d, x, y, C.LEAGUE);
//                    }
//                }
//                if (i > 0) {
//                    planet = galaxy_grid[i - 1][j].planet;
//                    if (planet != null) {
//                        drawUnit(planet, pixel_data, ws, bi, g2d, x, y, C.HOUSE5);
//                    }
//                }
//                if (i < galaxy_grid.length - 1) {
//                    planet = galaxy_grid[i + 1][j].planet;
//                    if (planet != null) {
//                        drawUnit(planet, pixel_data, ws, bi, g2d, x, y, C.HOUSE4);
//                    }
//                }
//                if (i > 0 && j < galaxy_grid[0].length - 1) {
//                    planet = galaxy_grid[i - 1][j + 1].planet;
//                    if (planet != null) {
//                        drawUnit(planet, pixel_data, ws, bi, g2d, x, y, C.HOUSE3);
//                    }
//                }
//                if (j < galaxy_grid[0].length - 1) {
//                    planet = galaxy_grid[i][j + 1].planet;
//                    if (planet != null) {
//                        drawUnit(planet, pixel_data, ws, bi, g2d, x, y, C.HOUSE2);
//                    }
//                }
//                if (i < galaxy_grid.length - 1 && j < galaxy_grid[0].length - 1) {
//                    planet = galaxy_grid[i + 1][j + 1].planet;
//                    if (planet != null) {
//
//                        drawUnit(planet, pixel_data, ws, bi, g2d, x, y, C.HOUSE1);
//
//
//                    }
//                }

                y += dy;

            }
            x += dx;
        }
    }

    /**
     * Draw unit icons on space map. Draws up to three icons on lowest squares.
     *
     * @param planet
     * @param pixel_data
     * @param ws
     * @param bi
     * @param g2d
     * @param x
     * @param y
     * @param owner
     */
    public void drawUnit(Planet planet, int[] pixel_data, WindowSize ws,
            BufferedImage bi, Graphics2D g2d, int x, int y, int owner, int a, int b) {

        Point p = game.getSelectedPoint();
        int factions = 1;
        if (p != null && p.x == a && p.y == b) {
            if (gui.getAnimationBlink()) {
                return;
            } else {
                owner = game.getSelectedFaction();
            }
        } else if (owner >= C.LEAGUE) {
            factions = 3;
        }

        int counter = 0;
        for (int i = 0; i < factions; i++) {

            List<Unit> stack = planet.space_stacks[owner + i];
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
                    WritableRaster wr = bi.getRaster();
                    Util.fillRaster(wr, Util.getOwnerColor(e.owner));
                    Util.drawUnitIconEdges(wr, ws);
                    Util.writeUnit(pixel_data, e.type, unit_icons, wr, ws);

                    int dip = 4 * counter++;

                    if (ws.is_double) {
                        dip *= 2;
                    }
                    g2d.drawImage(bi, null, x, y + dip);
                    Util.writeUnitCount(g2d, ws, nr_spotted, x, y + dip);
                }
            }
        }
    }

    public void loadPlanetImages() {

        planet_images = new int[C.EFSPLAN_BIN_LENGTH][C.EFSPLAN_BIN_P_SIZE];
        for (int i = 0; i < planet_images.length; i++) {
            planet_images[i] = Util.readImageData(FN.S_EFSPLAN_BIN,
                    i * C.EFSPLAN_BIN_P_SIZE,
                    C.EFSPLAN_BIN_P_SIZE, ByteOrder.BIG_ENDIAN);

        }

    }
}
