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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import util.C;
import util.FN;
import util.G;
import util.G.CH;
import util.Util;
import util.UtilG;
import util.WindowSize;

/**
 * Diplomacy Window Gui, where you can formulate and send a diplomatic agreement
 * to a previously selected faction.
 *
 * @author joulupunikki
 */
public class DiplomacyWindow extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public enum IfYouWill {

        PEACE("Sign A Peace Treaty With Us"),
        MONEY("Compensate Us With Firebirds"),
        RESOURCES("Compensate Us With Resources"),
        UNITS("Compensate Us With Units"),
        CITIES("Compensate Us With Cities");

        private final String text;

        private IfYouWill(String option) {
            this.text = option;
        }
    }

    public enum ThenWeWill {

        PEACE("Sign A Peace Treaty With You"),
        MONEY("Compensate You With Firebirds"),
        RESOURCES("Compensate You With Resources"),
        UNITS("Compensate You With Units"),
        CITIES("Compensate You With Cities");
        private final String text;

        private ThenWeWill(String option) {
            this.text = option;
        }

    }

    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;
    private JButton exit;
    private JButton cancel;
    private JPopupMenu if_you_menu;
    private JPopupMenu then_we_menu;

    private JMenuItem[] if_you_items;
    private JMenuItem[] then_we_items;
    private boolean[] if_you_selected;
    private boolean[] then_we_selected;

    private int faction;
    private Map<Enum, Integer> c;
    private Map<Enum, Integer> c2;
    private DiplomacyWindow() {
    }

    private DiplomacyWindow(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        c = ws.diplomacy_window;
        c2 = ws.house;
        game = gui.getGame();
        setUpWindow();
    }

    public static DiplomacyWindow getWindow(Gui gui) {
        DiplomacyWindow w = new DiplomacyWindow(gui);
        w.setLayout(null);
        w.setPreferredSize(new Dimension(Gui.getWindowSize().main_window_width,
                Gui.getWindowSize().main_window_height));
        return w;
    }

    public void enterWindow(int faction) {
        this.faction = faction;
    }

    public void showIfYouMenu(Point p) {        
        if_you_menu.show(this, c.get(G.CDW.GIVE_X), c.get(G.CDW.GIVE_Y));
    }

    public void showThenWeMenu(Point p) {
        then_we_menu.show(this, c.get(G.CDW.GIVE_X), c.get(G.CDW.TAKE_Y));
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
//        give_menu.setBounds(c.get(G.CDW.GIVE_X), c.get(G.CDW.GIVE_Y), c.get(G.CDW.GIVE_W), c.get(G.CDW.GIVE_H));
        setUpIfYouMenu();
        setUpThenWeMenu();
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                gui.getCurrentState().clickOnWindow(e);
            }

        });
    }

    public void doClick(MouseEvent e) {
        Point p = e.getPoint();
        if (c.get(G.CDW.GIVE_X) <= p.x && p.x <= c.get(G.CDW.GIVE_X) + c.get(G.CDW.GIVE_W)
                && c.get(G.CDW.GIVE_Y) <= p.y && p.y <= c.get(G.CDW.GIVE_Y) + c.get(G.CDW.GIVE_H)) {
            for (int i = 0; i < if_you_items.length; i++) {
                if (if_you_selected[i]) {
                    if_you_items[i].setEnabled(false);
                } else {
                    if_you_items[i].setEnabled(true);
                }

            }
            showIfYouMenu(p);
        } else if (c.get(G.CDW.GIVE_X) <= p.x && p.x <= c.get(G.CDW.GIVE_X) + c.get(G.CDW.GIVE_W)
                && c.get(G.CDW.TAKE_Y) <= p.y && p.y <= c.get(G.CDW.TAKE_Y) + c.get(G.CDW.GIVE_H)) {
            for (int i = 0; i < then_we_items.length; i++) {
                if (then_we_selected[i]) {
                    then_we_items[i].setEnabled(false);
                } else {
                    then_we_items[i].setEnabled(true);
                }

            }
            showThenWeMenu(p);
        }
    }

    public void clear() {
        for (int i = 0; i < if_you_selected.length; i++) {
            if_you_selected[i] = false;
        }
        for (int i = 0; i < then_we_selected.length; i++) {
            then_we_selected[i] = false;
        }

    }

    private void setUpButtons() {
        exit = new JButton("Done");
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

        cancel = new JButton("Clear");
        cancel.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
        cancel.setBackground(Color.BLACK);
        cancel.setForeground(C.COLOR_GOLD);
        cancel.setBounds(ws.fw_eb_x - (int) (ws.fw_eb_w * 1.2), ws.fw_eb_y, ws.fw_eb_w, ws.fw_eb_h);
        cancel.setEnabled(true);
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.getCurrentState().pressCancelButton();
            }
        });
        this.add(cancel);
    }

    private void renderWindow(Graphics g) {
        drawBackground(g);
        drawDetails(g);
    }
    
    private void drawBackground(Graphics g) {
        byte[][] pallette = gui.getPallette();
        BufferedImage bi = Util.loadImage(FN.S_DIPLOMA_PCX, ws.is_double, pallette, 640, 480);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);
    }

    private void drawDetails(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
//        drawBudgetHeaders(g);
        drawLeader(g);
    }

    private void drawLeader(Graphics2D g) {
        String s = Util.factionNameDisplay(faction);
        int x = UtilG.center(g, c2.get(CH.LEADER_H_X), c2.get(CH.LEADER_H_W), ws.font_large, s);
        UtilG.drawStringGrad(g, s, ws.font_large, x, c2.get(CH.LEADER_H_Y));
    }

    private void setUpIfYouMenu() {
        if_you_menu = new JPopupMenu("Give Me ...");
        IfYouWill[] if_you_options = IfYouWill.values();
        if_you_items = new JMenuItem[if_you_options.length];
        if_you_selected = new boolean[if_you_options.length];
        for (int i = 0; i < if_you_options.length; i++) {
            if_you_items[i] = new JMenuItem(if_you_options[i].text);
            if_you_menu.add(if_you_items[i]);
            final int final_i = i;
            if_you_items[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectIfYou(final_i);
                }
            });
        }
    }

    private void setUpThenWeMenu() {
        then_we_menu = new JPopupMenu("We Will ...");
        ThenWeWill[] then_we_options = ThenWeWill.values();
        then_we_items = new JMenuItem[then_we_options.length];
        then_we_selected = new boolean[then_we_options.length];
        for (int i = 0; i < then_we_options.length; i++) {
            then_we_items[i] = new JMenuItem(then_we_options[i].text);
            then_we_menu.add(then_we_items[i]);
            final int final_i = i;
            then_we_items[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectThenWe(final_i);
                }
            });
        }
    }

    private void selectIfYou(int selection) {
        switch (IfYouWill.values()[selection]) {
            case PEACE:
                System.out.println("\"Peace in our time ...\"");
                break;
            case MONEY:
                System.out.println("I'll forward you to alcoholics anonymous ...");
                break;
            case RESOURCES:
                System.out.println("A Hawkwood, a Hazat and a Decados went to a bar ...");
                break;
            case UNITS:
                System.out.println("A Hawkwood, a Hazat and a Decados went to a bar ...");
                break;
            case CITIES:
                System.out.println("A Hawkwood, a Hazat and a Decados went to a bar ...");
                break;
            default:
                throw new AssertionError();
        }
        if_you_selected[selection] = true;
    }

    private void selectThenWe(int selection) {
        switch (ThenWeWill.values()[selection]) {
            case PEACE:
                System.out.println("\"Peace in our time ...\"");
                break;
            case MONEY:
                System.out.println("I'll forward you to alcoholics anonymous ...");
                break;
            case RESOURCES:
                System.out.println("A Hawkwood, a Hazat and a Decados went to a bar ...");
                break;
            case UNITS:
                System.out.println("A Hawkwood, a Hazat and a Decados went to a bar ...");
                break;
            case CITIES:
                System.out.println("A Hawkwood, a Hazat and a Decados went to a bar ...");
                break;
            default:
                throw new AssertionError();
        }
        then_we_selected[selection] = true;
    }
//    private void drawBudgetHeaders(Graphics2D g) {
//        UtilG.drawStringGrad(g, "Tax", ws.font_large, c.get(CH.TAX_H_X), c.get(CH.TAX_H_Y));
//        UtilG.drawStringGrad(g, "Tithe Skim", ws.font_large, c.get(CH.TITHE_SKIM_H_X), c.get(CH.TAX_H_Y) + c.get(CH.BUDGET_H));
//        UtilG.drawStringGrad(g, "Unit Pay", ws.font_large, c.get(CH.UNIT_PAY_H_X), c.get(CH.TAX_H_Y) + 2 * c.get(CH.BUDGET_H));
//
//        UtilG.drawStringGrad(g, "Debt", ws.font_large, c.get(CH.DEBT_H_X), c.get(CH.DEBT_H_Y));
//        UtilG.drawStringGrad(g, "Bank", ws.font_large, c.get(CH.DEBT_H_X), c.get(CH.DEBT_H_Y) + c.get(CH.DEBT_H_H));
//        UtilG.drawStringGrad(g, "Total", ws.font_large, c.get(CH.DEBT_H_X), c.get(CH.DEBT_H_Y) + 2 * c.get(CH.DEBT_H_H));
//    }
}
