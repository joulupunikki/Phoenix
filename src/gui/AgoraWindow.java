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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import util.C;
import util.C.GC;
import util.FN;
import util.Util;
import util.UtilG;
import util.UtilG.CustomSliderUI;
import util.WindowSize;

/**
 *
 * @author joulupunikki
 */
public class AgoraWindow extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;
    private JButton purchase;
    private JButton cancel;
    private JSlider[] sliders;
    private JTextField[] amount;
    private JTextField[] cost;
    private int[] buys;
    private int[] avails;
    private int[] sells;
    // pointer to map holding gui element coordinates
    private Map<Enum, Integer> c;

    public static AgoraWindow getAgoraWindow(Gui gui) {
        AgoraWindow aw = new AgoraWindow(gui);
        aw.setLayout(null);
        aw.setPreferredSize(new Dimension(Gui.getWindowSize().main_window_width,
                Gui.getWindowSize().main_window_height));
        return aw;
    }

    private AgoraWindow() {
    }

    private AgoraWindow(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        c = ws.agora;
        game = gui.getGame();
        setUpWindow();
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
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                System.out.println(p.x + "," + p.y);
            }
        });
        System.out.println(c.get(GC.PURCHASE_X) + "," + c.get(GC.PURCHASE_Y));
        purchase = new JButton("Purchase");
        purchase.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        purchase.setBackground(Color.BLACK);
        purchase.setForeground(C.COLOR_GOLD);
        this.add(purchase);
        purchase.setBounds(c.get(GC.PURCHASE_X), c.get(GC.PURCHASE_Y), c.get(GC.PURCHASE_W), c.get(GC.BOX_H));
        purchase.setEnabled(true);
        purchase.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.getCurrentState().pressPurchaseButton();
            }
        });
        

        cancel = new JButton("Cancel");
        cancel.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
        cancel.setBackground(Color.BLACK);
        cancel.setForeground(C.COLOR_GOLD);
        cancel.setBounds(c.get(GC.CANCEL_X), c.get(GC.PURCHASE_Y), c.get(GC.PURCHASE_W), c.get(GC.BOX_H));
        cancel.setEnabled(false);
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.getCurrentState().pressAbstainButton();
            }
        });
        this.add(cancel);
        
        setUpSliders();
        setUpAmount();
        setUpCost();
    }

    private void setUpSliders() {
        sliders = new JSlider[C.NR_RESOURCES];
        for (int i = 0; i < C.NR_RESOURCES; i++) {
            sliders[i] = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
            sliders[i].setUI(new CustomSliderUI(sliders[i], C.COLOR_GOLD_DARK));
            sliders[i].setBackground(Color.BLACK);
            sliders[i].setForeground(Color.BLACK);
            sliders[i].addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    JSlider src = (JSlider) e.getSource();
                }
            });
            sliders[i].setBounds(c.get(GC.SLIDER_X), c.get(GC.SLIDER_Y) + i * c.get(GC.LINE_H), c.get(GC.SLIDER_W), c.get(GC.BOX_H));
            this.add(sliders[i]);
        }
    }

    private void setUpAmount() {
        amount = new JTextField[C.NR_RESOURCES];
        for (int i = 0; i < C.NR_RESOURCES; i++) {
            amount[i] = new JTextField(i);
            amount[i].setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
            amount[i].setBounds(c.get(GC.AMT_V_X), c.get(GC.AMT_V_Y) + i * c.get(GC.LINE_H), c.get(GC.AMT_V_W), c.get(GC.BOX_H));
            amount[i].setHorizontalAlignment(JTextField.RIGHT);
            this.add(amount[i]);
        }
    }

    private void setUpCost() {
        cost = new JTextField[C.NR_RESOURCES];
        for (int i = 0; i < C.NR_RESOURCES; i++) {
            cost[i] = new JTextField(i);
            cost[i].setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
            cost[i].setBounds(c.get(GC.COST_V_X), c.get(GC.AMT_V_Y) + i * c.get(GC.LINE_H), c.get(GC.AMT_V_W), c.get(GC.BOX_H));
            cost[i].setHorizontalAlignment(JTextField.RIGHT);
            this.add(cost[i]);
        }
    }

    private void renderWindow(Graphics g) {
        drawBackground(g);
        drawDetails(g);
    }
    
    private void drawBackground(Graphics g) {
        byte[][] pallette = gui.getPallette();
        BufferedImage bi = Util.loadImage(FN.S_BG0_PCX, ws.is_double, pallette, 640, 480);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);
    }

    private void drawDetails(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        drawHeaders(g2d);
        drawLines(g2d);
    }

    private void drawHeaders(Graphics2D g) {
        UtilG.drawStringGrad(g, "Sell", ws.font_large, c.get(GC.SELL_H_X), c.get(GC.HEADER_Y));
        UtilG.drawStringGrad(g, "Avail", ws.font_large, c.get(GC.AVAIL_H_X), c.get(GC.HEADER_Y));
        UtilG.drawStringGrad(g, "Buy", ws.font_large, c.get(GC.BUY_H_X), c.get(GC.HEADER_Y));
        UtilG.drawStringGrad(g, "Amt", ws.font_large, c.get(GC.AMT_H_X), c.get(GC.HEADER_Y));
        UtilG.drawStringGrad(g, "Cost", ws.font_large, c.get(GC.COST_H_X), c.get(GC.HEADER_Y));

    }

    private void drawLines(Graphics2D g) {
        FontMetrics fm = this.getFontMetrics(ws.font_large);
        String s;
        for (int i = 0; i < 13; i++) {
            UtilG.drawStringGrad(g, Util.getResName(i), ws.font_large, c.get(GC.FOOD_X), c.get(GC.FOOD_Y) + c.get(GC.LINE_H) * i);
            s = String.valueOf(i);
            UtilG.drawStringGrad(g, s, ws.font_large, c.get(GC.SELL_V_X) - fm.stringWidth(s), c.get(GC.FOOD_Y) + c.get(GC.LINE_H) * i);
            UtilG.drawStringGrad(g, s, ws.font_large, c.get(GC.AVAIL_V_X) - fm.stringWidth(s), c.get(GC.FOOD_Y) + c.get(GC.LINE_H) * i);
            UtilG.drawStringGrad(g, s, ws.font_large, c.get(GC.BUY_V_X) - fm.stringWidth(s), c.get(GC.FOOD_Y) + c.get(GC.LINE_H) * i);          
        }
    }

    private void recalculateTradeDeal() {

    }

    private void enterAgora() {
        List<Unit> stock = game.getSelectedStack();

    }
}
