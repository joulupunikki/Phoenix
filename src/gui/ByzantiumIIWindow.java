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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import util.C;
import util.FN;
import util.Util;
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
    }

    public void renderWindow(Graphics g) {
        byte[][] pallette = gui.getPallette();
        BufferedImage bi = Util.loadImage(FN.S_BYZSECU_PCX, ws.is_double, pallette, 640, 480);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);       
    }

}
