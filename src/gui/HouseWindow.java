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

import game.Faction;
import game.Game;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import util.C;
import util.FN;
import util.G;
import util.G.CH;
import util.Util;
import util.UtilG;
import util.UtilG.DarkSliderUI;
import util.WindowSize;

/**
 * House Window Gui
 *
 * @author joulupunikki
 */
public class HouseWindow extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int TAX = 0;
    private static final int TITHE_SKIM = 1;
    private static final int UNIT_PAY = 2;
    private static final int NR_SLIDERS = 3;
    private static final int DEBT = 3;
    private static final int BANK = 4;
    private static final int TOTAL = 5;
    private static final int TEXT_BOXES = 6;
    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;
    private JButton exit;
    private JSlider[] sliders;
    private JTextField[] text_boxes;
    // pointer to map holding gui element coordinates
    private Map<Enum, Integer> c;

    private int gdp;
    private int base_pay;

    private HouseWindow() {
    }

    private HouseWindow(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        c = ws.house;
        game = gui.getGame();
        setUpWindow();
    }

    public static HouseWindow getHouseWindow(Gui gui) {
        HouseWindow w = new HouseWindow(gui);
        w.setLayout(null);
        w.setPreferredSize(new Dimension(Gui.getWindowSize().main_window_width,
                Gui.getWindowSize().main_window_height));
        return w;
    }

    public void setPolicy() {
        Faction house = game.getFaction(game.getTurn());
        house.setTaxRate(sliders[TAX].getValue());
        house.setTitheRate(sliders[TITHE_SKIM].getValue());
        house.setPayRate(sliders[UNIT_PAY].getValue());
    }

    public void enterHouseWindow() {
        Faction house = game.getFaction(game.getTurn());
        gdp = house.calculateGDP();
        base_pay = house.calculateUnitPay();
        sliders[TAX].setValue(house.getTaxRate());
        sliders[TITHE_SKIM].setValue(house.getTitheRate());
        sliders[UNIT_PAY].setValue(house.getPayRate());
        updateBudget();
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
        setUpSliders();
        setUpTextFields();

    }

    private void setUpTextFields() {
        text_boxes = new JTextField[TEXT_BOXES];
        for (int i = 0; i < text_boxes.length; i++) {
            text_boxes[i] = getResultJTF(i);    
        }
    }

    private JTextField getResultJTF(int i) {
        JTextField jtf = new JTextField();
        jtf.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        int x, y, w, h;
        x = c.get(CH.TAX_V_X);
        w = c.get(CH.TAX_V_W);
        h = c.get(CH.DEBT_H_H);
        if (i <= UNIT_PAY) {
            y = c.get(CH.SLIDER_Y) + i * c.get(CH.BUDGET_H);
        } else {
            y = c.get(CH.DEBT_V_Y) + (i - NR_SLIDERS) * h;
        }
        jtf.setBounds(x, y, w, h);
        jtf.setHorizontalAlignment(JTextField.RIGHT);
        this.add(jtf);
        return jtf;
    }
    private void setUpButtons() {
        exit = new JButton("Exit");
        exit.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
        exit.setBackground(Color.BLACK);
        exit.setForeground(C.COLOR_GOLD);
        exit.setBounds(ws.fw_eb_x, ws.fw_eb_y, ws.fw_eb_w, ws.fw_eb_h);
        exit.setEnabled(true);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.getCurrentState().pressExitButton();
            }
        });
        this.add(exit);
    }

    private void setUpSliders() {
        sliders = new JSlider[3];
        for (int i = 0; i < NR_SLIDERS; i++) {
            sliders[i] = new JSlider(JSlider.HORIZONTAL, 1, 100, 10);
            //sliders[i].setUI(new CustomSliderUI(sliders[i], C.COLOR_GOLD_DARK));
            sliders[i].setUI(new DarkSliderUI());
            sliders[i].setBackground(Color.BLACK);
            sliders[i].setForeground(Color.BLACK);
            sliders[i].addChangeListener((ChangeEvent e) -> {
                updateBudget();
            });
            sliders[i].setBounds(c.get(CH.SLIDER_X), c.get(CH.SLIDER_Y) + i * c.get(CH.BUDGET_H), c.get(CH.SLIDER_W), c.get(CH.DEBT_H_H));
            this.add(sliders[i]);
        }

    }

    private void updateBudget() {
        Faction house = game.getFaction(game.getTurn());
        int tax, tithe_skim, unit_pay, debt, bank, total;
        tax = sliders[TAX].getValue() * gdp / 100;
        tithe_skim = sliders[TITHE_SKIM].getValue() * gdp / 1000;
        unit_pay = sliders[UNIT_PAY].getValue() * base_pay / 100;
        debt = 0;
        bank = house.getFirebirds();
        total = tax + tithe_skim - unit_pay - debt + bank;
        text_boxes[TAX].setText("" + tax);
        text_boxes[TITHE_SKIM].setText("" + tithe_skim);
        text_boxes[UNIT_PAY].setText("" + unit_pay);
        text_boxes[DEBT].setText("" + debt);
        text_boxes[BANK].setText("" + bank);
        text_boxes[TOTAL].setText("" + total);
        
    }
    
    private void renderWindow(Graphics g) {
        drawBackground(g);
        drawDetails(g);
    }
    
    private void drawBackground(Graphics g) {
        byte[][] pallette = gui.getPallette();
        BufferedImage bi = Util.loadImage(FN.S_HOUSE_PCX, ws.is_double, pallette, 640, 480);
        Graphics2D g2d = (Graphics2D) g;
        WritableRaster wr = bi.getRaster();
        drawTreatyFlags(g2d, wr);
        g2d.drawImage(bi, null, 0, 0);
    }

    private void drawDetails(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
        drawBudgetHeaders(g);
        drawLeader(g);
        
    }

    private void drawTreatyFlags(Graphics2D g, WritableRaster wr) {
        int[] pixel_data = new int[1];
        for (int i = 0; i < C.NR_HOUSES; i++) {
            for (int j = 0; j < C.NR_HOUSES; j++) {
                if (i == j) {
                    continue;
                }
                if (game.getDiplomacy().getDiplomaticState(i, j) == C.DS_WAR) {
                    Util.writeRect(pixel_data, gui.getResources().getTreatyFlag(C.DS_WAR), wr, ws, c.get(G.CH.STATE_X) + i * c.get(G.CH.STATE_S), c.get(G.CH.STATE_Y) + j * c.get(G.CH.STATE_S), c.get(G.CH.STATE_W), c.get(G.CH.STATE_W));
                }
            }
        }

    }
    
    private void drawLeader(Graphics2D g) {
        String s = Util.factionNameDisplay(game.getTurn());
        int x = UtilG.center(g, c.get(CH.LEADER_H_X), c.get(CH.LEADER_H_W), ws.font_large, s);
        UtilG.drawStringGrad(g, s, ws.font_large, x, c.get(CH.LEADER_H_Y));
    }

    private void drawBudgetHeaders(Graphics2D g) {
        UtilG.drawStringGrad(g, "Tax", ws.font_large, c.get(CH.TAX_H_X), c.get(CH.TAX_H_Y));
        UtilG.drawStringGrad(g, "Tithe Skim", ws.font_large, c.get(CH.TITHE_SKIM_H_X), c.get(CH.TAX_H_Y) + c.get(CH.BUDGET_H));
        UtilG.drawStringGrad(g, "Unit Pay", ws.font_large, c.get(CH.UNIT_PAY_H_X), c.get(CH.TAX_H_Y) + 2 * c.get(CH.BUDGET_H));

        UtilG.drawStringGrad(g, "Debt", ws.font_large, c.get(CH.DEBT_H_X), c.get(CH.DEBT_H_Y));
        UtilG.drawStringGrad(g, "Bank", ws.font_large, c.get(CH.DEBT_H_X), c.get(CH.DEBT_H_Y) + c.get(CH.DEBT_H_H));
        UtilG.drawStringGrad(g, "Total", ws.font_large, c.get(CH.DEBT_H_X), c.get(CH.DEBT_H_Y) + 2 * c.get(CH.DEBT_H_H));
    }
}
