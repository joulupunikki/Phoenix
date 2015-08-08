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

import galaxyreader.Planet;
import galaxyreader.Unit;
import game.Game;
import game.Hex;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;
import javax.swing.JPanel;
import util.C;
import util.Util;
import util.WindowSize;

/**
 * Panel containing a global planet map:
 *
 * @author joulupunikki
 */
public class GlobeMap extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Gui gui;
    private Game game;
    private WindowSize ws;
    private int yard;
    // false for combat window version, true otherwise
    private boolean map_type;

    public GlobeMap(Gui gui, Game game, WindowSize ws, boolean map_type) {
        this.gui = gui;
        this.game = game;
        this.ws = ws;
        this.map_type = map_type;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        renderGlobeMap(g);

    }

    public void renderGlobeMap(Graphics g) {
        yard = ws.globe_map_width / C.PLANET_MAP_WIDTH;
        renderMapItems(g);
        if (map_type) {
            drawWindowArea(g);
        }
    }

    public void drawWindowArea(Graphics g) {
        Point smo = game.getMapOrigin();
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(smo.x * yard, smo.y * yard, (int) (C.PLANET_MAP_SQUARES_X * yard * 0.97),
                (int) (C.PLANET_MAP_SQUARES_Y * yard * 0.97));
        if (smo.x > C.PLANET_MAP_WIDTH - C.PLANET_MAP_SQUARES_X) {
            g.drawRect((smo.x - C.PLANET_MAP_WIDTH) * yard, smo.y * yard, (int) (C.PLANET_MAP_SQUARES_X * yard * 0.97),
                    (int) (C.PLANET_MAP_SQUARES_Y * yard * 0.97));
        }
    }

    public void renderMapItems(Graphics g) {
        byte[][] pallette = gui.getPallette();
        int[][] ter_color = gui.getResources().getTerColor();
        int[] color_scaler = gui.getResources().getColorScaler();
        int p_idx = -1;
        Unit replay_unit = null;
        if (map_type) {
            p_idx = game.getCurrentPlanetNr();
        } else {
            replay_unit = game.getCombatStack("b").get(0);
//            System.out.println("replay_unit.in_space = " + replay_unit.in_space);
//            System.exit(0);
            p_idx = replay_unit.p_idx;
        }
        Hex[][] map_array = game.getPlanetGrid(p_idx).getMapArray();
        Planet planet = game.getPlanet(p_idx);
        int tile_set = planet.tile_set_type;
        int color = 0;
        for (int i = 0; i < map_array.length; i++) {
            for (int j = 0; j < map_array[i].length; j++) {
                boolean[] terrain = map_array[i][j].getTerrain();
                List<Unit> stack = map_array[i][j].getStack();
                boolean spotted = map_array[i][j].isSpotted(game.getTurn());
//                Structure struct = map_array[i][j].getStructure();
                if ((map_type && !stack.isEmpty() && spotted) || (!map_type && !replay_unit.in_space
                        && replay_unit.x == i && replay_unit.y == j)) {
                    if (map_type) {
                        Point p = game.getSelectedPoint();
                        if (p != null && p.x == i && p.y == j && !gui.getAnimationBlink()) {
                            g.setColor(Color.WHITE);
                        } else {
                            Unit unit = stack.get(0);
                            g.setColor(Util.getColor(pallette, Util.getOwnerColor(unit.owner)));
                        }
                    } else {
                        if (!gui.getAnimationBlink()) {
                            g.setColor(Color.WHITE);
                        } else {
                            g.setColor(Util.getColor(pallette, Util.getOwnerColor(replay_unit.owner)));
                        }
                    }
                } else if (terrain[C.OCEAN]) {
//                    g.setColor(Color.BLUE);
                    color = ter_color[C.OCEAN][tile_set];
                    if (spotted) {
                        g.setColor(gui.getResources().getColor(color));
                    } else {
                        g.setColor(gui.getResources().getColor(color_scaler[color]));
                    }
                } else if (terrain[C.ROAD]) {
//                    g.setColor(Color.BLACK);
                    color = ter_color[C.ROAD][tile_set];
                    if (spotted) {
                        g.setColor(gui.getResources().getColor(color));
                    } else {
                        g.setColor(gui.getResources().getColor(color_scaler[color]));
                    }
                } else if (terrain[C.MOUNTAIN]) {
//                    g.setColor(Color.DARK_GRAY);
                    color = ter_color[C.MOUNTAIN][tile_set];
                    if (spotted) {
                        g.setColor(gui.getResources().getColor(color));
                    } else {
                        g.setColor(gui.getResources().getColor(color_scaler[color]));
                    }
                } else if (terrain[C.HILL]) {
//                    g.setColor(Color.DARK_GRAY);
                    color = ter_color[C.HILL][tile_set];
                    if (spotted) {
                        g.setColor(gui.getResources().getColor(color));
                    } else {
                        g.setColor(gui.getResources().getColor(color_scaler[color]));
                    }
                } else if (terrain[C.TREE]) {
//                    g.setColor(C.COLOR_DARK_GREEN);
                    color = ter_color[C.TREE][tile_set];
                    if (spotted) {
                        g.setColor(gui.getResources().getColor(color));
                    } else {
                        g.setColor(gui.getResources().getColor(color_scaler[color]));
                    }
                } else if (terrain[C.GRASS]) {
//                    g.setColor(C.COLOR_LIGHT_GREEN);
                    color = ter_color[C.GRASS][tile_set];
                    if (spotted) {
                        g.setColor(gui.getResources().getColor(color));
                    } else {
                        g.setColor(gui.getResources().getColor(color_scaler[color]));
                    }
                } else if (terrain[C.ARID_GRASS]) {
//                    g.setColor(C.COLOR_LIGHT_GREEN);
                    color = ter_color[C.ARID_GRASS][tile_set];
                    if (spotted) {
                        g.setColor(gui.getResources().getColor(color));
                    } else {
                        g.setColor(gui.getResources().getColor(color_scaler[color]));
                    }
                } else if (terrain[C.DESERT]) {
//                    g.setColor(Color.YELLOW);
                    color = ter_color[C.DESERT][tile_set];
                    if (spotted) {
                        g.setColor(gui.getResources().getColor(color));
                    } else {
                        g.setColor(gui.getResources().getColor(color_scaler[color]));
                    }
                } else if (terrain[C.ICE]) {
//                    g.setColor(Color.WHITE);
                    color = ter_color[C.ICE][tile_set];
                    if (spotted) {
                        g.setColor(gui.getResources().getColor(color));
                    } else {
                        g.setColor(gui.getResources().getColor(color_scaler[color]));
                    }
                } else if (terrain[C.TUNDRA]) {
//                    g.setColor(Color.WHITE);
                    color = ter_color[C.TUNDRA][tile_set];
                    if (spotted) {
                        g.setColor(gui.getResources().getColor(color));
                    } else {
                        g.setColor(gui.getResources().getColor(color_scaler[color]));
                    }
                }

                int dip = 0;
                if (i % 2 == 0) {
                    dip = (int) (yard / 2);
                }
                g.fillRect(i * yard, j * yard + dip, yard, yard);
            }

        }
    }
}
