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
import java.awt.image.WritableRaster;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import util.C;
import util.FN;
import util.G.OW;
import util.Util;
import util.UtilG;
import util.WindowSize;

/**
 * Diplomacy Selector Gui, where you can select a faction to conduct diplomatic
 * talks with.
 *
 * @author joulupunikki
 */
public class OptionsPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;
    private JButton exit;
    private EnumMap<GUI_OPT, JCheckBox> gui_opt;
    //private JCheckBox[] jcb1;
    // pointer to map holding gui element coordinates
    private Map<Enum, Integer> c;

    private JDialog dialog;
    
    private OptionsPanel() {
    }

    private OptionsPanel(Gui gui, JDialog dialog) {
        this.dialog = dialog;
        this.gui = gui;
        ws = Gui.getWindowSize();
        c = ws.options_window;
        game = gui.getGame();
        setUpWindow();
        dialog.setUndecorated(true);
        dialog.add(this);
    }

    public static OptionsPanel getWindow(Gui gui) {
        JDialog dialog = new JDialog(gui);
        Map<Enum, Integer> c = Gui.getWindowSize().options_window;
        dialog.setBounds(c.get(OW.WIN_X) + gui.getX(), c.get(OW.WIN_Y) + gui.getY(), c.get(OW.WIN_W), c.get(OW.WIN_H));
        dialog.setModal(true);
        OptionsPanel w = new OptionsPanel(gui, dialog);
        w.setLayout(null);
        return w;
    }

    public void setWindowVisiblity(boolean visible) {
        dialog.setBounds(c.get(OW.WIN_X) + gui.getX(), c.get(OW.WIN_Y) + gui.getY(), c.get(OW.WIN_W), c.get(OW.WIN_H));
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
        exit.setBounds(c.get(OW.EXIT_X), c.get(OW.EXIT_Y), c.get(OW.EXIT_W), c.get(OW.EXIT_H));
        exit.setEnabled(true);
        final OptionsPanel self = this;
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                self.setWindowVisiblity(false);
            }
        });
        this.add(exit);
        gui_opt = new EnumMap(GUI_OPT.class);
        int idx = 0;
        for (GUI_OPT opt : GUI_OPT.values()) {
            JCheckBox tmp = new JCheckBox(opt.txt);
            tmp.setSelected(opt.init);
            gui_opt.put(opt, tmp);
            tmp.setBounds(c.get(OW.COL1_X), c.get(OW.COL1_Y) + idx++ * c.get(OW.BOX_H), c.get(OW.BOX_W), c.get(OW.BOX_H));
            tmp.setBackground(Color.BLACK);
            tmp.setForeground(C.COLOR_GOLD);
            this.add(tmp);
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
        
        WritableRaster wr = bi.getRaster(); 
        g2d.drawImage(bi, null, 0, 0);
        UtilG.drawFrameRectIn(g, 0, 0, c.get(OW.WIN_W), c.get(OW.WIN_H));
    }

    private void drawDetails(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
        g.setColor(C.COLOR_GOLD);
        g.setFont(ws.font_default);
        g.drawString("Show ...", c.get(OW.COL1_X), c.get(OW.COL1_Y));
    }

    public enum GUI_OPT {

        LAND_NR("land nr", false),
        HEX_XY("hex x,y", false),
        FACTION("faction", false);
        private final String txt;
        private final boolean init;

        private GUI_OPT(String txt, boolean value) {
            this.txt = txt;
            this.init = value;
        }
    }

    public EnumMap getGuiOpt() {
        return gui_opt;
    }

}
