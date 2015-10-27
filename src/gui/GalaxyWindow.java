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
import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Game;
import game.Hex;
import game.Square;
import java.awt.Color;
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
 * Grand galactic map.
 *
 * @author joulupunikki
 */
public class GalaxyWindow extends JPanel {
    final int STARFLD2_W = 480;
    final int STARFLD2_H = 418;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;

    JButton exit;
    private Map<Enum, Integer> c;

    private int square_side;

    //private int[][] mini_planet_images;
    private Image[] mini_planet_images;
    private int[] planet_owners;

    public GalaxyWindow(Gui gui) {
        this.gui = gui;

        ws = Gui.getWindowSize();
        c = ws.galaxy_window;
        game = gui.getGame();
        square_side = c.get(CGW.SQUARE);
        setUpButtons();

        int[][] planet_images = SpaceMap.getPlanet_images();
        //mini_planet_images = new int[planet_images.length][square_side * square_side];
        mini_planet_images = new Image[planet_images.length];
        BufferedImage bi_big = new BufferedImage(ws.planet_image_side, ws.planet_image_side, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
        WritableRaster wr_big = bi_big.getRaster();
        BufferedImage bi_small = new BufferedImage(square_side, square_side, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
        WritableRaster wr_small = bi_small.getRaster();
        for (int i = 0; i < planet_images.length; i++) {
            wr_big.setPixels(0, 0, ws.planet_image_side, ws.planet_image_side, planet_images[i]);
            mini_planet_images[i] = bi_big.getScaledInstance(square_side, square_side, Image.SCALE_AREA_AVERAGING);
        }
        planet_owners = new int[game.getPlanets().size()];
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void initWindow() {
        //System.out.println(planet_owners.length);
        for (int i = 0; i < planet_owners.length; i++) {
            planet_owners[i] = -1;
            //System.out.println(i);
            Util.HexIter it = Util.getHexIter(game, i);
            for (Hex h = it.next(); h != null; h = it.next()) {
                Structure stru = h.getStructure();
                if (stru != null && stru.type == C.PALACE) {
                    planet_owners[i] = stru.owner;
                    break;
                }
            }
        }
    }

    private void setUpButtons() {
        exit = new JButton("Exit");
        this.add(exit);
        exit.setBounds(c.get(CGW.BUT_X), c.get(CGW.BUT_Y),
                c.get(CGW.BUT_W), c.get(CGW.BUT_H));
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
        drawJumpRoutes(g);
        drawSquares(g);

    }

    private void drawSquares(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
        int x = c.get(CGW.MAP_X) + c.get(CGW.MAP_MARGIN);
        int y = 0;
        for (int i = 0; i < C.STAR_MAP_WIDTH; i++) {
            y = c.get(CGW.MAP_Y);
            for (int j = 0; j < C.STAR_MAP_HEIGHT; j++) {

                g.setColor(Color.DARK_GRAY);
                Square sqr = galaxy_grid[i][j];
                if (sqr.stack_owner > -1) {
                    g.drawRect(x, y, square_side, square_side);
                    drawStack(g, galaxy_grid[i][j], x, y);
                }
                if (sqr.planet != null) {
                    g.setColor(Color.WHITE);
                    if (planet_owners[sqr.planet.index] > -1) {
                        g.setColor(gui.getResources().getColor(Util.getOwnerColor(planet_owners[sqr.planet.index])));
                    }
                    int cx = UtilG.center(g, x, 9, g.getFont(), sqr.planet.name);
                    g.drawString(sqr.planet.name, cx, y - square_side);
                    g.drawImage(mini_planet_images[10], x, y, null);
                }
                y += square_side;
            }
            x += square_side;
        }
    }

    private void drawBackground(Graphics g) {
        byte[][] pallette = gui.getPallette();
        BufferedImage bi = Util.loadImage(FN.S_BG0_PCX, ws.is_double, pallette, 640, 480);

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);

        BufferedImage bi2 = Util.loadStarFld2(FN.S_STARFLD2_PCX, ws.is_double, pallette, STARFLD2_W, STARFLD2_H);

        g2d.drawImage(bi2, null, (ws.main_window_width - bi2.getWidth()) / 2, c.get(CGW.MAP_Y));
        g2d.setColor(Color.BLACK);
        g2d.fillRect((ws.main_window_width - bi2.getWidth()) / 2, c.get(CGW.MAP_Y) + STARFLD2_H, STARFLD2_W, c.get(CGW.MAP_H) - bi2.getHeight());
        UtilG.drawFrameRect(g, (ws.main_window_width - bi2.getWidth()) / 2, c.get(CGW.MAP_Y), STARFLD2_W, c.get(CGW.MAP_H));
    }

    /**
     * Draw unit icons on galaxy map. Draws up to three icons on lowest squares.
     *
     */
    private void drawStack(Graphics g, Square sqr, int x, int y) {
        Planet planet = sqr.parent_planet;
        int owner = sqr.stack_owner;
        int factions = 1;
        if (owner >= C.LEAGUE) {
            factions = 3;
        }

        int counter = 0;
        for (int i = 0; i < factions; i++) {

            List<Unit> stack = planet.space_stacks[owner + i];
            if (stack != null && Util.stackSize(stack) > 0) {
                Unit e = null; 
                boolean spotted = false;
                for (Unit unit : stack) {
                    if (unit.spotted[game.getTurn()]) {
                        spotted = true;
                        e = unit;
                    }
                }
                if (spotted) {
                    int dip = counter++;
                    if (ws.is_double) {
                        dip *= 2;
                    }
                    g.setColor(gui.getResources().getColor(Util.getOwnerColor(e.owner)));
                    g.fillRect(x, y + dip, square_side, square_side);
                }
            }
        }
    }

    private void drawJumpRoutes(Graphics g) {
        List<JumpGate> jump_routes = game.getJumpGates();
        int x_off = square_side / 2 + c.get(CGW.MAP_X) + c.get(CGW.MAP_MARGIN);
        g.setColor(gui.getColorCycleColor());

        int y_off = square_side / 2 + c.get(CGW.MAP_Y);
        for (JumpGate jg : jump_routes) {
            g.drawLine((jg.getX1()) * square_side + x_off,
                    (jg.getY1()) * square_side + y_off,
                    (jg.getX2()) * square_side + x_off,
                    (jg.getY2()) * square_side + y_off);
            g.setColor(gui.getColorCycleColor());
        }

    }
}
