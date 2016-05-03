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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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
import org.apache.commons.math3.util.FastMath;
import util.C;
import util.C.GC;
import util.FN;
import util.Util;
import util.UtilG;
import util.UtilG.DarkSliderUI;
import util.WindowSize;

/**
 * Agora buy/sell window.
 *
 * @author joulupunikki
 */
public class AgoraWindow extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final double SELL_PREMIUM = 1.3;
    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;
    private JButton buy_sell;
    private JButton cancel;
    private JSlider[] sliders;
    private JTextField[] amount;
    private JTextField[] cost;
    private int[] buys;
    private int[] avails;
    private int[] sells;
    private int[] amounts;
    private JTextField bank;
    private JTextField total;
    private JTextField result;
    // pointer to map holding gui element coordinates
    private Map<Enum, Integer> c;
    // null iff buying, othewise pointer to agora hex to which we are selling
    private Hex sell;

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
        buy_sell = new JButton("Purchase");
        buy_sell.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        buy_sell.setBackground(Color.BLACK);
        buy_sell.setForeground(C.COLOR_GOLD);
        this.add(buy_sell);
        buy_sell.setBounds(c.get(GC.PURCHASE_X), c.get(GC.PURCHASE_Y), c.get(GC.PURCHASE_W), c.get(GC.BOX_H));
        buy_sell.setEnabled(true);
        buy_sell.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.getCurrentState().pressBuySellButton();
            }
        });
        

        cancel = new JButton("Cancel");
        cancel.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
        cancel.setBackground(Color.BLACK);
        cancel.setForeground(C.COLOR_GOLD);
        cancel.setBounds(c.get(GC.CANCEL_X), c.get(GC.PURCHASE_Y), c.get(GC.PURCHASE_W), c.get(GC.BOX_H));
        cancel.setEnabled(true);
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.getCurrentState().pressCancelButton();
            }
        });
        this.add(cancel);
        
        setUpSliders();
        setUpAmount();
        setUpCost();
        setUpInventory();
        amounts = new int[C.NR_RESOURCES];
        bank = getResultJTF(0);
        total = getResultJTF(1);
        result = getResultJTF(2);

    }

    private JTextField getResultJTF(int i) {
        JTextField jtf = new JTextField();
        jtf.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        jtf.setBounds(c.get(GC.BANK_V_X), c.get(GC.BANK_V_Y) + i * c.get(GC.BOX_H), c.get(GC.BANK_V_W), c.get(GC.BOX_H));
        jtf.setHorizontalAlignment(JTextField.RIGHT);
        this.add(jtf);
        return jtf;
    }

    private void setUpSliders() {
        sliders = new JSlider[C.NR_RESOURCES];
        ChangeListener cl = (ChangeEvent e) -> {
            updateShoppingCart();
        };

        for (int i = 0; i < C.NR_RESOURCES; i++) {
            sliders[i] = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
            //sliders[i].setUI(new CustomSliderUI(sliders[i], C.COLOR_GOLD_DARK));
            sliders[i].setUI(new DarkSliderUI());
            sliders[i].setBackground(Color.BLACK);
            sliders[i].setForeground(Color.BLACK);
            sliders[i].addChangeListener(cl);
            final JSlider tmp = sliders[i];
            MouseWheelListener mwl = new MouseAdapter() {

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    int old = tmp.getValue();
                    tmp.setValue(old - e.getWheelRotation() * 10);
                }

            };
            sliders[i].addMouseWheelListener(mwl);
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

    private void setUpInventory() {
        buys = new int[C.NR_RESOURCES];
        avails = new int[C.NR_RESOURCES];
        sells = new int[C.NR_RESOURCES];
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
        UtilG.drawStringGrad(g, "Bank", ws.font_large, c.get(GC.BANK_H_X), c.get(GC.BANK_H_Y));
        UtilG.drawStringGrad(g, "Total Cost", ws.font_large, c.get(GC.BANK_H_X), c.get(GC.BANK_H_Y) + c.get(GC.BOX_H));
        UtilG.drawStringGrad(g, "Result", ws.font_large, c.get(GC.BANK_H_X), c.get(GC.BANK_H_Y) + 2 * c.get(GC.BOX_H));

    }

    private void drawLines(Graphics2D g) {
        FontMetrics fm = this.getFontMetrics(ws.font_large);
        String s;
        for (int i = 0; i < 13; i++) {
            drawField(i, g, Util.getResName(i), ws.font_large, c.get(GC.FOOD_X), c.get(GC.FOOD_Y) + c.get(GC.LINE_H) * i);
            s = "" + buys[i];
            drawField(i, g, s, ws.font_large, c.get(GC.SELL_V_X) - fm.stringWidth(s), c.get(GC.FOOD_Y) + c.get(GC.LINE_H) * i);
            //UtilG.drawStringGrad(g, s, ws.font_large, c.get(GC.SELL_V_X) - fm.stringWidth(s), c.get(GC.FOOD_Y) + c.get(GC.LINE_H) * i);
            s = "" + avails[i];
            drawField(i, g, s, ws.font_large, c.get(GC.AVAIL_V_X) - fm.stringWidth(s), c.get(GC.FOOD_Y) + c.get(GC.LINE_H) * i);
            //UtilG.drawStringGrad(g, s, ws.font_large, c.get(GC.AVAIL_V_X) - fm.stringWidth(s), c.get(GC.FOOD_Y) + c.get(GC.LINE_H) * i);
            s = "" + sells[i];
            drawField(i, g, s, ws.font_large, c.get(GC.BUY_V_X) - fm.stringWidth(s), c.get(GC.FOOD_Y) + c.get(GC.LINE_H) * i);
            //UtilG.drawStringGrad(g, s, ws.font_large, c.get(GC.BUY_V_X) - fm.stringWidth(s), c.get(GC.FOOD_Y) + c.get(GC.LINE_H) * i);
        }
    }

    private void drawField(int i, Graphics2D g, String s, Font f, int x, int y) {
        if (sliders[i].isEnabled()) {
            UtilG.drawStringGrad(g, s, f, x, y);
        } else {
            g.setColor(C.COLOR_GOLD_DARK);
            g.drawString(s, x, y);
        }
    } 
    
    public void enterAgora(Hex sell) {
        this.sell = sell;
        if (sell == null) {
            buy_sell.setText("Purchase");
        } else {
            buy_sell.setText("Sell");
        }
        resetInventory();
        fillInventory();
        adjustSliders(true, 0);
    }

    private void adjustSliders(boolean zero, int sum) {
        for (int i = 0; i < sliders.length; i++) {
            int max;
            if (sell != null) {
                max = C.STACK_SIZE * C.MAX_CARGO;
            } else {
                max = FastMath.min(((game.getFaction(game.getTurn()).getFirebirds() - sum) / sells[i]) + amounts[i], C.MAX_CARGO);
            }
            if (max < 1) {
                sliders[i].setEnabled(false);
            } else {
                sliders[i].setEnabled(true);
            }
            sliders[i].setMaximum(FastMath.max(0, FastMath.min(avails[i], max)));
            if (zero) {
                int zero_val = 0;
                if (sell != null) {
                    zero_val = sliders[i].getMaximum();
                }
                sliders[i].setValue(zero_val);
            }
        }
    }

    private void fillInventory() {
        List<Unit> stock = game.getSelectedStack();
        if (sell != null) {
            stock = Util.getSelectedUnits(stock);
        }
        for (Unit u : stock) {
            if (u.type == C.CARGO_UNIT_TYPE) {
                avails[u.res_relic] += u.amount;
            }
        }
    }

    private void resetInventory() {
        for (int i = 0; i < buys.length; i++) {
            buys[i] = game.getResTypes()[i].price;
            avails[i] = 0;
            sells[i] = (int) FastMath.ceil(buys[i] * SELL_PREMIUM);
        }
    }

    private void updateShoppingCart() {
        int sum = 0;
        int tmp = 0;
        for (int i = 0; i < sliders.length; i++) {
            amounts[i] = sliders[i].getValue();
            amount[i].setText("" + amounts[i]);
            if (sell != null) {
                tmp = amounts[i] * buys[i];
            } else {
                tmp = amounts[i] * sells[i];
            }
            cost[i].setText("" + tmp);
            sum += tmp;
        }
        adjustSliders(false, sum);
        bank.setText("" + game.getFaction(game.getTurn()).getFirebirds());
        total.setText("" + sum);
        if (sell != null) {
            result.setText("" + (game.getFaction(game.getTurn()).getFirebirds() + sum));
        } else {
            result.setText("" + (game.getFaction(game.getTurn()).getFirebirds() - sum));
        }
    }

    /**
     * @return the amounts
     */
    public int[] getAmounts() {
        return amounts;
    }

    /**
     * @return the sell
     */
    public Hex getAgoraHex() {
        return sell;
    }

    /**
     * @return the buys
     */
    public int[] getBuys() {
        return buys;
    }

    /**
     * @return the sells
     */
    public int[] getSells() {
        return sells;
    }
}
