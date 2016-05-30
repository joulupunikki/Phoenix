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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import util.FN;
import util.Util;
import util.WindowSize;

/**
 * Window to display on main windows during player change (to cover details of
 * current player from prying eyes)
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class XPlayerScreen extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Gui gui;
    private WindowSize ws;
    private BufferedImage bi;

    public XPlayerScreen(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        this.bi = Util.loadImage(FN.S_CATHED3_PCX, ws.is_double, gui.getPallette(), 640, 480);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        renderPanel(g);

    }

    public void renderPanel(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);

    }
}
