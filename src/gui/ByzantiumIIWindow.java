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
import game.Regency;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import util.C;
import util.FN;
import util.Util;
import util.UtilG;
import util.WindowSize;

/**
 *
 * @author joulupunikki
 */
public class ByzantiumIIWindow extends JPanel {
    /*
     * To change this template, choose Tools | Templates
     * and open the template in the editor.
     */

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;
    private JButton exit;
    private JButton declare_emperor;
    private JButton abstain;
    private JButton vote;

    public ByzantiumIIWindow(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
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

    public void enterWindow() {
        if (game.getRegency().needToVote(game.getTurn(), game.getEfs_ini(), game.getYear())) {
            vote.setEnabled(true);
        } else {
            vote.setEnabled(false);
        }
        if (game.getRegency().getRegent() == game.getTurn() && game.getRegency().getYearsSinceThroneClaim() < 0) {
            declare_emperor.setVisible(true);
        } else {
            declare_emperor.setVisible(false);
        }
    }

    public void setUpWindow() {
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

        vote = new JButton("Vote");
        vote.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
        vote.setBackground(Color.BLACK);
        vote.setForeground(C.COLOR_GOLD);
        vote.setBounds(ws.fw_eb_x - ws.main_window_width / 4, ws.fw_eb_y, ws.fw_eb_w, ws.fw_eb_h);
        vote.setEnabled(true);
        vote.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.getCurrentState().pressVoteButton();
            }
        });
        this.add(vote);

        abstain = new JButton("Abstain");
        abstain.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
        abstain.setBackground(Color.BLACK);
        abstain.setForeground(C.COLOR_GOLD);
        abstain.setBounds(ws.fw_eb_x - ws.main_window_width / 2, ws.fw_eb_y, ws.fw_eb_w, ws.fw_eb_h);
        abstain.setEnabled(false);
        abstain.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.getCurrentState().pressAbstainButton();
            }
        });
        this.add(abstain);

        declare_emperor = new JButton("Declare Yourself Emperor");
        declare_emperor.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
        declare_emperor.setBackground(Color.BLACK);
        declare_emperor.setForeground(C.COLOR_GOLD);
        declare_emperor.setBounds(ws.bz2_button1_x, ws.bz2_button1_y, ws.bz2_button1_w, ws.bz2_button1_h);
        declare_emperor.setEnabled(true);
        declare_emperor.setVisible(false);
        declare_emperor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Declare Yourself Emperor -pressed");
                gui.getCurrentState().pressDeclareEmperorButton();
            }
        });
        this.add(declare_emperor);
    }

    public void enableAbstainButton(boolean enabled) {
        abstain.setEnabled(enabled);
    }

    public void enableVoteButton(boolean enabled) {
        vote.setEnabled(enabled);
    }

    public void hideDeclareEmperorButton() {
        declare_emperor.setVisible(false);
    }

    public void renderWindow(Graphics g) {
        drawBackground(g);
        drawDetails(g);
    }
    
    private void drawBackground(Graphics g) {
        byte[][] pallette = gui.getPallette();
        BufferedImage bi = Util.loadImage(FN.S_BYZSECU_PCX, ws.is_double, pallette, 640, 480);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);
    }

    private void drawDetails(Graphics g) {
        Regency regency = game.getRegency();

        drawMinistryDetails(g, C.STIGMATA, regency.getGarrison());
        drawMinistryDetails(g, C.THE_SPY, regency.getEye());
        drawMinistryDetails(g, C.FLEET, regency.getFleet());
        drawMinistryDetails(g, C.IMPERIAL, regency.getRegent());
        drawMinistryDetails(g, C.HOUSE1, C.HOUSE1);
        drawMinistryDetails(g, C.HOUSE2, C.HOUSE2);
        drawMinistryDetails(g, C.HOUSE3, C.HOUSE3);
        drawMinistryDetails(g, C.HOUSE4, C.HOUSE4);
        drawMinistryDetails(g, C.HOUSE5, C.HOUSE5);

        if (regency.getCrownedEmperor() > -1) {
            UtilG.drawStringGrad((Graphics2D) g, Util.getFactionName(regency.getCrownedEmperor()) + " Victor", ws.font_large,
                    ws.bz2_button1_x, ws.bz2_button1_y + ws.bz2_button1_h, 1, false);
        } else if (regency.getYearsSinceThroneClaim() > -1) {
            UtilG.drawStringGrad((Graphics2D) g, "Emperor declared", ws.font_large,
                    ws.bz2_button1_x, ws.bz2_button1_y + ws.bz2_button1_h, 1, false);
        }
    }

    private void drawMinistryDetails(Graphics g, int ministry, int house) {
        final int X_OFF = 10;
        final int Y_OFF = 20;
        int x = ws.bz2_house_names_w;
        int y = ws.bz2_house_names_y2;
        if (ministry > C.HOUSE5) {
            x = X_OFF;
            y = Y_OFF + ws.bz2_ministry_y1;
        }
        switch (ministry) {
            case C.STIGMATA:
                x += ws.bz2_stigmata_x1;
                break;
            case C.THE_SPY:
                x += ws.bz2_eye_x1;
                break;
            case C.FLEET:
                x += ws.bz2_fleet_x1;
                break;
            case C.IMPERIAL:
                y = Y_OFF + ws.bz2_regent_y1;
                x += ws.bz2_regent_x1;
                break;
            case C.HOUSE1:
                x = ws.bz2_house_names_x11;
                break;
            case C.HOUSE2:
                x = ws.bz2_house_names_x12;
                break;
            case C.HOUSE3:
                x = ws.bz2_house_names_x13;
                break;
            case C.HOUSE4:
                x = ws.bz2_house_names_x14;
                break;
            case C.HOUSE5:
                x = ws.bz2_house_names_x15;
                break;
            default:
                throw new AssertionError();
        }
        String officer = "vacant";
        if (house > -1) {
            officer = Util.getFactionName(house);
        }
        g.setColor(C.COLOR_GOLD);
        if (game.getFaction(ministry).isEliminated()) {
            g.setColor(C.COLOR_GOLD_DARK);
        }
        g.drawString(officer, x, y);
    }
}
