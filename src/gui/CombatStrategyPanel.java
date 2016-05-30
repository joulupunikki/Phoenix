/*
 * Copyright (C) 2016 joulupunikki joulupunikki@gmail.communist.invalid.
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
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import util.C;
import util.FN;
import util.G;
import util.Util;
import util.UtilG;
import util.WindowSize;

/**
 * Asks for combat strategy before each combat.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class CombatStrategyPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;
    private JButton normal;
    private JButton assault;
    private JButton feint;
    private JButton cancel;
    // pointer to map holding gui element coordinates
    private Map<Enum, Integer> c;

    private JDialog dialog;

    private Strategy[] response_array = null;
    private BufferedImage bi;
    public enum Strategy {
        NORMAL,
        ASSAULT,
        FEINT,
        CANCEL
    }

    private CombatStrategyPanel() {
    }

    private CombatStrategyPanel(Gui gui, JDialog dialog) {
        this.dialog = dialog;
        this.gui = gui;
        ws = Gui.getWindowSize();
        c = ws.strategy_selector;
        game = gui.getGame();
        this.bi = Util.loadImage(FN.S_BG0_PCX, ws.is_double, gui.getPallette(), 640, 480);
        setUpWindow();
        dialog.setUndecorated(true);
        dialog.add(this);
    }

    public static CombatStrategyPanel getWindow(Gui gui) {
        JDialog dialog = new JDialog(gui, true);
        Map<Enum, Integer> c = Gui.getWindowSize().diplomacy_selector;
        //dialog.setBounds(c.get(G.SS.WIN_X) + gui.getX(), c.get(G.SS.WIN_Y) + gui.getY(), c.get(G.SS.WIN_W), c.get(G.SS.WIN_H));
        dialog.setModal(true);
        CombatStrategyPanel w = new CombatStrategyPanel(gui, dialog);
        w.setLayout(null);
        return w;
    }

    public void setWindowVisiblity(boolean visible) {
        dialog.setBounds(c.get(G.SS.WIN_X) + gui.getX(), c.get(G.SS.WIN_Y) + gui.getY(), c.get(G.SS.WIN_W), c.get(G.SS.WIN_H));
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
        normal = new JButton();
        setUpButtonVisuals(normal, "Normal", Strategy.NORMAL);
        assault = new JButton();
        setUpButtonVisuals(assault, "Assault: Attack Plus, Defence Minus", Strategy.ASSAULT);
        feint = new JButton();
        setUpButtonVisuals(feint, "Feint: Attack Minus, Defence Plus", Strategy.FEINT);
        cancel = new JButton();
        setUpButtonVisuals(cancel, "Cancel", Strategy.CANCEL);

    }

    private void setUpButtonVisuals(JButton b, String s, Strategy strategy) {
        b.setText(s);
        b.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
        b.setBackground(Color.BLACK);
        b.setForeground(C.COLOR_GOLD);
        b.setBounds(c.get(G.SS.BUTTON_X), c.get(G.SS.BUTTON_Y) + strategy.ordinal() * c.get(G.SS.ROW_H), c.get(G.SS.BUTTON_W), c.get(G.SS.BUTTON_H));
        b.setEnabled(true);
        final CombatStrategyPanel self = this;
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                self.selection(strategy);
            }
        });
        this.add(b);
    }

    private void renderWindow(Graphics g) {
        drawBackground(g);
        drawDetails(g);
    }

    private void drawBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);
        UtilG.drawFrameRectIn(g, 0, 0, c.get(G.SS.WIN_W), c.get(G.SS.WIN_H));
    }

    private void drawDetails(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
        drawTexts(g);
    }

    private void drawTexts(Graphics2D g) {
        String s = "How should we attack, my lord ?";
        //int x = UtilG.center(g, c.get(G.SS.WIN_H_X), c.get(G.SS.WIN_W) - 2 * c.get(G.SS.WIN_H_X), ws.font_large, s);
        UtilG.drawStringGrad(g, s, ws.font_large, c.get(G.SS.HEADER_X), c.get(G.SS.HEADER_Y));
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

    private void selection(Strategy strategy) {
        response_array[0] = strategy;
        this.setWindowVisiblity(false);

    }

    void setResponseArray(Strategy[] strategy) {
        response_array = strategy;
    }
}
