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

import galaxyreader.Unit;
import game.Game;
import game.Hex;
import gui.PlanetMap.Layer;
import gui.PlanetMap.Orientation;
import static gui.PlanetMap.TILE_EDGES;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import util.C;
import util.FN;
import util.G.CGW;
import util.Util;
import util.UtilG;
import util.WindowSize;

/**
 * Grand global planetary map.
 *
 * @author joulupunikki
 */
public class GlobeWindow extends JPanel {

    static final int FULL_GLOBE_W = 1662;
    static final int FULL_GLOBE_H = 1260;

    private static final long serialVersionUID = 1L;
    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;

    JButton exit;
    private Map<Enum, Integer> c;

    private int square_side;
    private int[][] unit_icons;
    private Hex[][] hex_grid;
    private int current_faction;
    private int[][] hex_tiles;
    private int[][] structures;
    Hex current_hex;
    private final int[] color_scaler;
    BufferedImage bi;
    Image im;
    public GlobeWindow(Gui gui) {
        this.gui = gui;
        byte[][] pallette = gui.getPallette();
        ws = Gui.getWindowSize();
        c = ws.galaxy_window;
        game = gui.getGame();
        square_side = c.get(CGW.SQUARE);
        setUpButtons();
        color_scaler = gui.getResources().getColorScaler();
        bi = Util.loadImage(FN.S_BG0_PCX, ws.is_double, pallette, 640, 480);
    }

    public void setGame(Game game) {
        this.game = game;
    }

    private void setUpButtons() {
        exit = new JButton("Exit");
        this.add(exit);
        exit.setBounds(c.get(CGW.PBUT_X), c.get(CGW.PBUT_Y),
                c.get(CGW.PBUT_W), c.get(CGW.PBUT_H));
        exit.setEnabled(true);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.getCurrentState().pressExitButton();
            }
        });

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        renderWindow(g);

    }

    private void renderWindow(Graphics g) {
        drawBackground(g);
        g.drawImage(im, c.get(CGW.PMAP_X), c.get(CGW.PMAP_Y), null);
        UtilG.drawStringGrad((Graphics2D) g, game.getPlanet(game.getCurrentPlanetNr()).name, getFont(), c.get(CGW.PNAME_X), c.get(CGW.PNAME_Y));
    }

    public void initWindow() {
        int tile_set = game.getPlanet(game.getCurrentPlanetNr()).tile_set_type;
        hex_tiles = Gui.getHexTiles(tile_set);
        structures = Gui.getStructureTiles(tile_set);

        unit_icons = Gui.getUnitIcons();

        hex_grid = game.getPlanetGrid(game.getCurrentPlanetNr()).getMapArray();
        current_faction = game.getTurn();
        
        BufferedImage bi_tmp = new BufferedImage(FULL_GLOBE_W, FULL_GLOBE_H, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
        drawHexTiles(bi_tmp.getGraphics(), hex_tiles, bi_tmp);
        im = bi_tmp.getScaledInstance(c.get(CGW.PMAP_W), c.get(CGW.PMAP_H), Image.SCALE_FAST);
    }

    private void drawBackground(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);
        UtilG.drawFrameRect(g, c.get(CGW.PMAP_X), c.get(CGW.PMAP_Y), c.get(CGW.PMAP_W), c.get(CGW.PMAP_H));
    }

    /**
     * Draw a hexagonal image into square target raster
     *
     * @param g
     * @param hex_tiles
     * @param bi
     */
    public void drawHexTiles(Graphics g, int[][] hex_tiles, BufferedImage bi) {
        BufferedImage bi_unit = new BufferedImage(ws.unit_icon_size, ws.unit_icon_size, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
        WritableRaster wr_unit = bi_unit.getRaster();
        WritableRaster wr = bi.getRaster();
        byte[][] pallette = gui.getPallette();
        int current_planet = game.getCurrentPlanetNr();
        int[] pixel_data = new int[1];
        int x = 0;
        int y = 0;
        int x_offset = (C.STRUCT_BIN_WIDTH - C.EFSUNIT_BIN_WIDTH) / 2;
        int y_offset = (C.STRUCT_BIN_HEIGHT - C.EFSUNIT_BIN_HEIGHT) / 2;
        int dx = 0;
        int dy = 0;
        for (int i = 0; i < C.PLANET_MAP_WIDTH; i++) {
            y = 0;
            for (int j = 0; j < C.PLANET_MAP_COLUMNS; j++) {
                if (i % 2 != 0 || j != 31) {
                    int dip = 0;
                    if (i % 2 == 0) {
                        dip = +20;
                    }
                    // for setting spotted squares
                    current_hex = hex_grid[i][j];
                    // hex tile numbers in efstile*.bin
                    int[] tile_no;
                    tile_no = PlanetMap.getTileNo(i, j, game);
                    writeHex2(x, y, dip, pixel_data, hex_tiles, tile_no, wr);

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

                                Util.fillRaster(wr_unit, Util.getOwnerColor(e.owner));
                                Util.drawUnitIconEdges(wr_unit, ws);
                                Util.writeUnit(pixel_data, e.type, unit_icons, wr_unit, ws);

                                Graphics2D g2d = (Graphics2D) g;

                                g2d.drawImage(bi_unit, null, dx, dy);
                                Util.writeUnitCount(g2d, ws, nr_spotted, dx, dy, e.owner != e.prev_owner);
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
                if (dx < FULL_GLOBE_W && dy < FULL_GLOBE_H) {

                    writePixel(dx, dy, i * C.STRUCT_BIN_WIDTH + j,
                            pixel_data, hex_tiles, tile_no, wr);

                }

            }
        }
    }

    public void writePixel(int x, int y, int t_idx, int[] pixel_data, int[][] hex_tiles, int[] tile_no, WritableRaster wr) {
        final int origTidx = t_idx;

        for (int i = 0; i < tile_no.length; i++) {
            if (tile_no[i] == -1) {
                continue;
            }

            Orientation orientation = Orientation.DEFAULT;
            if (tile_no[i] >= (Orientation.FLIPFLOP.ordinal() * 100000)) {
                orientation = Orientation.FLIPFLOP;
            } else if (tile_no[i] >= (Orientation.FLOP.ordinal() * 100000)) {
                orientation = Orientation.FLOP;
            } else if (tile_no[i] >= (Orientation.FLIP.ordinal() * 100000)) {
                orientation = Orientation.FLIP;
            }
            int t_y = (t_idx) / C.STRUCT_BIN_WIDTH;
            int t_x = (t_idx) - (t_y * C.STRUCT_BIN_WIDTH);
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
//                // if double window size scale image
//                if (ws.is_double) {
//                    wr.setPixel(2 * x, 2 * y, pixel_data);
//                    wr.setPixel(2 * x + 1, 2 * y, pixel_data);
//                    wr.setPixel(2 * x, 2 * y + 1, pixel_data);
//                    wr.setPixel(2 * x + 1, 2 * y + 1, pixel_data);
//                } else {
                    wr.setPixel(x, y, pixel_data);
//                }
            }

            t_idx = origTidx;
        }
    }
}
