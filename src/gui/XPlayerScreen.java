/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import util.Util;
import util.WindowSize;

/**
 * Window to display on main windows during player change (to cover details of
 * current player from prying eyes)
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class XPlayerScreen extends JPanel {

    private Gui gui;
    private WindowSize ws;

    public XPlayerScreen(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        renderPanel(g);

    }

    public void renderPanel(Graphics g) {

        byte[][] pallette = gui.getPallette();
        BufferedImage bi = Util.loadImage("pcx/cathed3.pcx", ws.is_double, pallette, 640, 480);

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);

    }
}
