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
import game.Game;
import game.Square;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;
import javax.swing.JPanel;
import util.C;
import util.WindowSize;

/**
 * Panel containing galactic map
 *
 * @author joulupunikki
 */
public class GalacticMap extends JPanel {

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

    public GalacticMap(Gui gui, Game game, WindowSize ws, boolean map_type) {
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

        renderGalacticMap(g);

    }

    public void renderGalacticMap(Graphics g) {
        yard = ws.galactic_map_width / C.STAR_MAP_WIDTH;
        drawBackground(g);
        drawJumpRoutes(g);
        drawPlanets(g);
        if (map_type) {
            drawWindowArea(g);
        }
    }

    public void drawJumpRoutes(Graphics g) {
        List<JumpGate> jump_routes = game.getJumpGates();

        JumpGate jump_path = game.getJumpPath();
        g.setColor(gui.getColorCycleColor());

        for (JumpGate jg : jump_routes) {
            if (jump_path != null && jump_path.getX1() == jg.getX1()
                    && jump_path.getY1() == jg.getY1()
                    && jump_path.getX2() == jg.getX2()
                    && jump_path.getY2() == jg.getY2()) {
                g.setColor(Color.GREEN);
            }
            g.drawLine((jg.getX1() * yard) + 1,
                    (jg.getY1() * yard) + 1,
                    (jg.getX2() * yard) + 1,
                    (jg.getY2() * yard) + 1);
            g.setColor(gui.getColorCycleColor());
        }

    }

    public void drawWindowArea(Graphics g) {
        Point smo = game.getSpaceMapOrigin();
        g.setColor(Color.WHITE);
        g.drawRect(smo.x * yard, smo.y * yard, (int) (C.STAR_MAP_SQUARES_X * yard * 0.97),
                (int) (C.STAR_MAP_SQUARES_Y * yard * 0.97));
    }

    public void drawBackground(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, ws.galactic_map_width, ws.galactic_map_height);
    }

    public void drawPlanets(Graphics g) {
        Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
        Point p = game.getSelectedPoint();
        Point p1 = null;
        Point q = null;
        if (map_type) {
            if (p != null) {
                p1 = new Point(p);
                int faction = game.getSelectedFaction().y;
                q = game.convertSpaceUnit(p1, faction);
            }
        } else {
            //System.out.println("stack b: " + game.getCombatStack("b"));
            int p_idx = game.getCombatStack("b").get(0).p_idx;
            Planet planet = game.getPlanet(p_idx);
            q = new Point(planet.x, planet.y);
        }
        g.setColor(Color.WHITE);
        for (int i = 0; i < C.STAR_MAP_HEIGHT; i++) {
            for (int j = 0; j < C.STAR_MAP_WIDTH; j++) {
                if (galaxy_grid[j][i].planet != null) {
                    if (map_type) {
                        if (p == null || q.x != j || q.y != i || !gui.getAnimationBlink()) {
                            g.fillRect(j * yard, i * yard, yard, yard);
                        }
                    } else {
                        if (q.x != j || q.y != i || !gui.getAnimationBlink()) {
                            g.fillRect(j * yard, i * yard, yard, yard);
                        }
                    }
                }

            }

        }
    }

}
