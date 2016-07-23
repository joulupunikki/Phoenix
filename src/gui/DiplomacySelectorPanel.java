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

import game.Game;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import state.SU;
import util.C;
import util.FN;
import util.G;
import util.G.CD;
import util.Util;
import util.UtilG;
import util.WindowSize;

/**
 * Diplomacy Selector Gui, where you can select a faction to conduct diplomatic
 * talks with.
 *
 * @author joulupunikki
 */
public class DiplomacySelectorPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;
    private JButton exit;

    // pointer to map holding gui element coordinates
    private Map<Enum, Integer> c;

    private JDialog dialog;
    private BufferedImage bi;
    private DiplomacySelectorPanel() {
    }

    private DiplomacySelectorPanel(Gui gui, JDialog dialog) {
        this.dialog = dialog;
        this.gui = gui;
        ws = Gui.getWindowSize();
        c = ws.diplomacy_selector;
        game = gui.getGame();
        setUpWindow();
        dialog.setUndecorated(true);
        dialog.add(this);
    }

    public static DiplomacySelectorPanel getWindow(Gui gui) {
        JDialog dialog = new JDialog(gui);
        Map<Enum, Integer> c = Gui.getWindowSize().diplomacy_selector;
        dialog.setBounds(c.get(CD.WIN_X) + gui.getX(), c.get(CD.WIN_Y) + gui.getY(), c.get(CD.WIN_W), c.get(CD.WIN_H));
        dialog.setModal(true);
        DiplomacySelectorPanel w = new DiplomacySelectorPanel(gui, dialog);
        w.setLayout(null);
        return w;
    }

    public void setWindowVisiblity(boolean visible) {
        dialog.setBounds(c.get(CD.WIN_X) + gui.getX(), c.get(CD.WIN_Y) + gui.getY(), c.get(CD.WIN_W), c.get(CD.WIN_H));
        if (visible) {
            bi = Util.loadImage(FN.S_BG0_PCX, ws.is_double, gui.getPallette(), 640, 480);
        }
        dialog.setVisible(visible);
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderWindow(g);
    }

    private void setUpWindow() {
        setUpButtons();
    }

    private void setUpButtons() {
        exit = new JButton("Exit");
        exit.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
        exit.setBackground(Color.BLACK);
        exit.setForeground(C.COLOR_GOLD);
        exit.setBounds(c.get(G.CD.EXIT_X), c.get(G.CD.EXIT_Y), c.get(G.CD.EXIT_W), c.get(G.CD.EXIT_H));
        exit.setEnabled(true);
        final DiplomacySelectorPanel self = this;
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                self.setWindowVisiblity(false);
            }
        });
        this.add(exit);
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int faction = getFaction(e.getPoint());
                if (faction > -1) {
                    gui.getDiplomacyWindow().enterWindow(faction);
                    SU.showDiplomacyWindow();
                    self.setWindowVisiblity(false);
                }
            }

        });
    }
    
    private void renderWindow(Graphics g) {
        drawBackground(g);
        drawDetails(g);
    }
    
    private void drawBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        WritableRaster wr = bi.getRaster();
        int row1_x = c.get(G.CD.ROW1_X); //Fix #86
        int row2_x = c.get(G.CD.ROW2_X);
        int row1_y = c.get(G.CD.ROW1_Y);
        int row2_y = c.get(G.CD.ROW2_Y);
        int row_w = c.get(G.CD.ROW_W);
        if (ws.is_double) {
            row1_x /= 2;
            row2_x /= 2;
            row1_y /= 2;
            row2_y /= 2;
            row_w /= 2;
        }
        int[] pixel_data = new int[1];
        int count = 0;
        for (int i = 0; i <= C.HOUSE5; i++) {
            if (i == game.getTurn()) {
                continue;
            }
            Util.writeRect(pixel_data, gui.getResources().getBanner100(i), wr, ws, row1_x + count * row_w, row1_y, C.BANNER100_SIDE, C.BANNER100_SIDE);
            count++;
        }
        count = 0;
        for (int i = C.LEAGUE; i <= C.VAU; i++) {
            if (i == C.SYMBIOT) {
                i++;
            }
            Util.writeRect(pixel_data, gui.getResources().getBanner100(i), wr, ws, row2_x + count * row_w, row2_y, C.BANNER100_SIDE, C.BANNER100_SIDE);
            count++;
        }
        g2d.drawImage(bi, null, 0, 0);
        UtilG.drawFrameRectIn(g, 0, 0, c.get(CD.WIN_W), c.get(CD.WIN_H));
    }

    private void drawDetails(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
        drawTexts(g);
    }

    private void drawTexts(Graphics2D g) {
        String s = "Click on the banner of the diplomat you would like to contact";
        int x = UtilG.center(g, c.get(CD.WIN_H_X), c.get(CD.WIN_W) - 2 * c.get(CD.WIN_H_X), ws.font_large, s);
        UtilG.drawStringGrad(g, s, ws.font_large, x, c.get(CD.WIN_H_Y));
        int count = 0;
        for (int i = 0; i <= C.HOUSE5; i++) {
            if (i == game.getTurn()) {
                continue;
            }
            s = Util.factionNameDisplay(i);
            x = UtilG.center(g, c.get(CD.ROW1_X) + count * c.get(CD.ROW_W), ws.banner100_side, ws.font_large, s);
            UtilG.drawStringGrad(g, s, ws.font_large, x, c.get(CD.ROW_H_Y));
            count++;
        }
    }

    private int getFaction(Point p) {
        int faction = -1;
        if (c.get(G.CD.ROW1_Y) <= p.y && p.y <= c.get(G.CD.ROW1_Y) + ws.banner100_side) {
            for (int i = 0; i < 4; i++) {
                int tmp = c.get(G.CD.ROW1_X) + i * c.get(G.CD.ROW_W);
                if (tmp <= p.x && p.x <= tmp + ws.banner100_side) {
                    faction = i;
                    if (game.getTurn() <= i) {
                        faction++;
                    }
                    System.out.println(faction);
                    return faction;
                }
                
            }
 
        }
        faction = DiplomacyWindow.clickedInRects(p, c.get(G.CD.ROW2_X), c.get(G.CD.ROW2_Y), ws.banner100_side, ws.banner100_side, 3, c.get(G.CD.ROW_W));
        if (faction > -1) {
            faction += C.LEAGUE;
            if (faction == C.SYMBIOT) {
                faction++;
            }
        }
        return faction;
    }

}
