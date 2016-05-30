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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.ListIterator;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import state.CW1;
import state.CW2;
import util.C;
import util.FN;
import util.Util;
import util.UtilG;
import util.WindowSize;

/**
 * Combats are displayed in this window.
 *
 * @author joulupunikki
 */
public class CombatWindow extends JPanel {
    /*
     * To change this template, choose Tools | Templates
     * and open the template in the editor.
     */

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int UNSPOTTED_COLOR_SHIFT = 2;
    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;
// 
    private JCheckBox[] hc;
    private JButton fight;
    private JButton exit;
    private JButton bombard_again;
    private JLabel planet_name;
    private BufferedImage bi;
    public CombatWindow(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();

        game = gui.getGame();
        this.bi = Util.loadImage(FN.S_BG0_PCX, ws.is_double, gui.getPallette(), 640, 480);
        setUpWindow();
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        renderCombatWindow(g);

    }

    public void initWindow() {
        int p_idx = game.getBattle().getCombatStack("b").get(0).p_idx;
        String name = game.getPlanet(p_idx).name;
        System.out.println("name = " + name);
        planet_name.setText(name);
    }

    public void toggleButtons(boolean exit_visible, boolean fight_enabled, boolean again_visible) {
        if (exit_visible) {
            this.add(exit);
        } else {
            this.remove(exit);
        }
        if (again_visible) {
            this.add(bombard_again);
        } else {
            this.remove(bombard_again);
        }
        fight.setEnabled(fight_enabled);

    }

    public void setFightText(String text) {

        fight.setText(text);

    }

    public void setUpWindow() {
        fight = new JButton("Do Combat");
        fight.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));

        fight.setBackground(Color.BLACK);
        fight.setForeground(C.COLOR_GOLD);

        this.add(fight);
        fight.setBounds(ws.combat_window_fight_button_x, ws.combat_window_fight_button_y,
                ws.combat_window_fight_button_w, ws.combat_window_fight_button_h);
        fight.setEnabled(true);
        fight.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.getCurrentState().pressFightButton();
            }
        });

        bombard_again = new JButton("Bombard Again");
        bombard_again.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));

        bombard_again.setBackground(Color.BLACK);
        bombard_again.setForeground(C.COLOR_GOLD);

        bombard_again.setBounds(ws.combat_window_fight_button_x, ws.combat_window_bombard_again_button_y,
                ws.combat_window_fight_button_w, ws.combat_window_fight_button_h);
        bombard_again.setEnabled(true);
        bombard_again.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.getCurrentState().pressBombardAgainButton();
            }
        });

        exit = new JButton("Exit");
        exit.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));

        exit.setBackground(Color.BLACK);
        exit.setForeground(C.COLOR_GOLD);

//        this.add(fight);
        exit.setBounds(ws.fw_eb_x, ws.fw_eb_y, ws.fw_eb_w, ws.fw_eb_h);
        exit.setEnabled(true);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.getCurrentState().pressExitButton();
            }
        });

        planet_name = new JLabel();
        planet_name.setBounds(ws.cw_pn_x, ws.cw_pn_y, ws.cw_pn_w, ws.cw_pn_h);
//        planet_name.setBounds(10, 10, 100, 15);
        planet_name.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(planet_name);

    }

    public void renderCombatWindow(Graphics g) {
        drawUnits(g);
        drawFrameRect(g);
        drawFactionDetails(g);
    }

    private void drawFactionDetails(Graphics g) {
        FontMetrics fm = this.getFontMetrics(ws.font_large);
        String s = Util.getFactionName(game.getBattle().getCombatStack("a").get(0).owner);
        UtilG.drawStringGrad((Graphics2D) g, s, ws.font_large, ws.cw_att_n_x, ws.cw_att_n_y1);
        s = Util.getFactionName(game.getBattle().getCombatStack("b").get(0).owner);
        UtilG.drawStringGrad((Graphics2D) g, s, ws.font_large, ws.cw_def_n_x - fm.stringWidth(s), ws.cw_att_n_y1);
    }
    /**
     * Draw golden frames around window items.
     *
     * @param g
     */
    public void drawFrameRect(Graphics g) {
        UtilG.drawFrameRect(g, ws.cw_gm_x, ws.cw_gm_y, ws.cw_gm_w, ws.cw_gm_h);
        UtilG.drawFrameRect(g, ws.cw_glm_x, ws.cw_glm_y, ws.cw_glm_w, ws.cw_glm_h);
        UtilG.drawFrameRect(g, ws.combat_window_stack_display_x, ws.combat_window_stack_display_y,
                ws.combat_window_stack_display_w, ws.combat_window_stack_display_h);
        UtilG.drawFrameRect(g, ws.combat_window_stack_display_x2, ws.combat_window_stack_display_y,
                ws.combat_window_stack_display_w, ws.combat_window_stack_display_h);
    }

    public void drawUnits(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);

//        g.setColor(Color.BLACK);
//        g.fillRect(ws.combat_window_stack_display_x, ws.combat_window_stack_display_y, 
//                ws.combat_window_stack_display_w, ws.combat_window_stack_display_h);
//        g.fillRect(ws.combat_window_stack_display_x2, ws.combat_window_stack_display_y, 
//                ws.combat_window_stack_display_w, ws.combat_window_stack_display_h);        
        BufferedImage bi_att = new BufferedImage(ws.combat_window_stack_display_w, ws.combat_window_stack_display_h, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
        WritableRaster wr_att = bi_att.getRaster();
        Util.fillRaster(wr_att, C.INDEX_COLOR_BLACK);

        int[] pixel_data = new int[1];
        int[][] unit_icons = Gui.getUnitIcons();

        int[] skull = Util.loadSquare(FN.S_SKULL_BIN, 0, C.SKULL_SIDE * C.SKULL_SIDE);
        int[] flag = Util.loadSquare(FN.S_FLAG_BIN, 0, C.SKULL_SIDE * C.SKULL_SIDE);
        List<Unit> attacker = game.getCombatStack("a");
        List<Unit> defender = game.getCombatStack("b");
        ListIterator<Unit> it = attacker.listIterator();

        BufferedImage bi_att_u = new BufferedImage(ws.unit_icon_size, ws.unit_icon_size, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
        WritableRaster wr_att_u = bi_att_u.getRaster();
        loop_att:
        for (int i = 0; i < C.STACK_WINDOW_UNITS_Y; i++) {
            for (int j = 0; j < C.STACK_WINDOW_UNITS_X; j++) {
                Unit e;
                if (it.hasNext()) {
                    e = it.next();
                } else {
                    break loop_att;
                }
                int color = Util.getOwnerColor(e.owner);
                if (!e.spotted[defender.get(0).owner]) {
                    color += UNSPOTTED_COLOR_SHIFT;
                }
//                System.out.println("color = " + color);
                Util.fillRaster(wr_att_u, color);
                Util.drawUnitIconEdges(wr_att_u, ws);
//                if (e.health <= 0) {
//                    Util.writeSubRect(pixel_data, skull, wr_att_u, ws, 0, 0, 3, 3, C.SKULL_SIDE - 6, C.SKULL_SIDE - 6);
//                } else if (e.routed) {
//                    Util.writeSubRect(pixel_data, flag, wr_att_u, ws, 0, 0, 3, 3, C.SKULL_SIDE - 6, C.SKULL_SIDE - 6);
//                }
                Util.writeUnit(pixel_data, e.type, unit_icons, wr_att_u, ws);

                int dx = (int) ((0.15 + j * 1.15) * ws.unit_icon_size);
                int dy = (int) ((0.15 + i * 1.15) * ws.unit_icon_size);
                wr_att.setRect(dx, dy, wr_att_u);

//                Util.drawUnitDetails(g, game, e, dx, dy);
                dx -= ws.skull_offset;
                dy -= ws.skull_offset;
                if (ws.is_double) {
                    dx /= 2;
                    dy /= 2;
                }
                if (e.health <= 0) {
                    Util.writeRect(pixel_data, skull, wr_att, ws, dx, dy, C.SKULL_SIDE, C.SKULL_SIDE);
                } else if (e.routed) {
                    Util.writeRect(pixel_data, flag, wr_att, ws, dx, dy, C.SKULL_SIDE, C.SKULL_SIDE);
                }

            }

        }
        g2d.drawImage(bi_att, null, ws.combat_window_stack_display_x, ws.combat_window_stack_display_y);

        it = attacker.listIterator();
        loop_att_det:
        for (int i = 0; i < C.STACK_WINDOW_UNITS_Y; i++) {
            for (int j = 0; j < C.STACK_WINDOW_UNITS_X; j++) {
                Unit e;
                if (it.hasNext()) {
                    e = it.next();
                } else {
                    break loop_att_det;
                }

                int dx = (int) (((0.15 + j * 1.15) * ws.unit_icon_size) + ws.combat_window_stack_display_x);
                int dy = (int) (((0.15 + i * 1.15) * ws.unit_icon_size) + ws.combat_window_stack_display_y);

                if (e.health > 0) { // && !e.routed) {
                    Util.drawUnitDetails(g, game, e, dx, dy);
                }

            }

        }

        it = defender.listIterator();

        bi_att = new BufferedImage(ws.combat_window_stack_display_w, ws.combat_window_stack_display_h, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
        wr_att = bi_att.getRaster();
        Util.fillRaster(wr_att, C.INDEX_COLOR_BLACK);
        bi_att_u = new BufferedImage(ws.unit_icon_size, ws.unit_icon_size, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
        wr_att_u = bi_att_u.getRaster();
        loop_def:
        for (int i = 0; i < C.STACK_WINDOW_UNITS_Y; i++) {
            for (int j = 0; j < C.STACK_WINDOW_UNITS_X; j++) {
                Unit e;
                if (it.hasNext()) {
                    e = it.next();
                } else {
                    break loop_def;
                }
                int color = Util.getOwnerColor(e.owner);
                if (!e.spotted[attacker.get(0).owner]) {
                    color += UNSPOTTED_COLOR_SHIFT;
                }
//                System.out.println("color = " + color);
                Util.fillRaster(wr_att_u, color);
                Util.drawUnitIconEdges(wr_att_u, ws);
//                if (e.health <= 0) {
//                    Util.writeSubRect(pixel_data, skull, wr_att_u, ws, 0, 0, 3, 3, C.SKULL_SIDE - 6, C.SKULL_SIDE - 6);
//                } else if (e.routed) {
//                    Util.writeSubRect(pixel_data, flag, wr_att_u, ws, 0, 0, 3, 3, C.SKULL_SIDE - 6, C.SKULL_SIDE - 6);
//                }
                Util.writeUnit(pixel_data, e.type, unit_icons, wr_att_u, ws);

                int dx = (int) ((0.15 + j * 1.15) * ws.unit_icon_size);
                int dy = (int) ((0.15 + i * 1.15) * ws.unit_icon_size);
                wr_att.setRect(dx, dy, wr_att_u);

//                Util.drawUnitDetails(g, game, e, dx, dy);
                dx -= ws.skull_offset;
                dy -= ws.skull_offset;
                if (ws.is_double) {
                    dx /= 2;
                    dy /= 2;
                }
                if (e.health <= 0) {
                    Util.writeRect(pixel_data, skull, wr_att, ws, dx, dy, C.SKULL_SIDE, C.SKULL_SIDE);
                } else if (e.routed) {
                    Util.writeRect(pixel_data, flag, wr_att, ws, dx, dy, C.SKULL_SIDE, C.SKULL_SIDE);
                }

            }

        }
        g2d.drawImage(bi_att, null, ws.combat_window_stack_display_x2, ws.combat_window_stack_display_y);

        it = defender.listIterator();
        loop_def_det:
        for (int i = 0; i < C.STACK_WINDOW_UNITS_Y; i++) {
            for (int j = 0; j < C.STACK_WINDOW_UNITS_X; j++) {
                Unit e;
                if (it.hasNext()) {
                    e = it.next();
                } else {
                    break loop_def_det;
                }

                int dx = (int) (((0.15 + j * 1.15) * ws.unit_icon_size) + ws.combat_window_stack_display_x2);
                int dy = (int) (((0.15 + i * 1.15) * ws.unit_icon_size) + ws.combat_window_stack_display_y);

                if (e.health > 0) { // && !e.routed) {
                    Util.drawUnitDetails(g, game, e, dx, dy);
                }

            }

        }
        if (gui.getCurrentState() instanceof CW1) {
            fight.setText("Do Combat");
        } else if (gui.getCurrentState() instanceof CW2) {
            fight.setText("Combat Done");
        }
    }
}
