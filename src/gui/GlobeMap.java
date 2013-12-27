/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import galaxyreader.Structure;
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

    private Gui gui;
    private Game game;
    private WindowSize ws;
    private int yard;

    public GlobeMap(Gui gui, Game game, WindowSize ws) {
        this.gui = gui;
        this.game = game;
        this.ws = ws;
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
        drawWindowArea(g);
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
        
        Hex[][] map_array = game.getPlanetGrid(game.getCurrentPlanetNr()).getMapArray();
        for (int i = 0; i < map_array.length; i++) {
            for (int j = 0; j < map_array[i].length; j++) {
                boolean[] terrain = map_array[i][j].getTerrain();
                List<Unit> stack = map_array[i][j].getStack();
//                Structure struct = map_array[i][j].getStructure();
                if (!stack.isEmpty()) {
                    Point p = game.getSelectedPoint();
                    if (p != null && p.x == i && p.y == j && !gui.getAnimationBlink()) {
                        g.setColor(Color.WHITE);
                    } else {
                        Unit unit = stack.get(0);
                        g.setColor(Util.getColor(pallette, Util.getOwnerColor(unit.owner)));
                    }
                } else if (terrain[C.OCEAN]) {
                    g.setColor(Color.BLUE);
                } else if (terrain[C.ROAD]) {
                    g.setColor(Color.BLACK);
                } else if (terrain[C.MOUNTAIN] || terrain[C.HILL]) {
                    g.setColor(Color.DARK_GRAY);
                } else if (terrain[C.TREE]) {
                    g.setColor(C.COLOR_DARK_GREEN);
                } else if (terrain[C.GRASS] || terrain[C.ARID_GRASS]) {
                    g.setColor(C.COLOR_LIGHT_GREEN);
                } else if (terrain[C.DESERT]) {
                    g.setColor(Color.YELLOW);
                } else if (terrain[C.ICE] || terrain[C.TUNDRA]) {
                    g.setColor(Color.WHITE);
                }

                g.fillRect(i * yard, j * yard, yard, yard);
            }

        }
    }
}
