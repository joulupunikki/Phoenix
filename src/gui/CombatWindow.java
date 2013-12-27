/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import galaxyreader.Unit;
import game.Game;
import java.awt.Color;
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
import javax.swing.JPanel;
import state.CW1;
import state.CW2;
import util.C;
import util.Util;
import util.WindowSize;

/**
 *
 * @author joulupunikki
 */
public class CombatWindow extends JPanel {
    /*
     * To change this template, choose Tools | Templates
     * and open the template in the editor.
     */

    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;
// 
    private JCheckBox[] hc;
    private JButton fight;

    public CombatWindow(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();

        game = gui.getGame();

        setUpWindow();
    }

    public void setGame(Game game) {
        this.game = game;
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        renderCombatWindow(g);

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
    }

    public void renderCombatWindow(Graphics g) {

        byte[][] pallette = gui.getPallette();
        BufferedImage bi = Util.loadImage("pcx/bg0.pcx", ws.is_double, pallette, 640, 480);

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

        int[] skull = Util.loadSquare("bin/skull.bin", 0, C.SKULL_SIDE * C.SKULL_SIDE);
        int[] flag = Util.loadSquare("bin/flag.bin", 0, C.SKULL_SIDE * C.SKULL_SIDE);
        List<Unit> attacker = game.getCombatStack("a");
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

//                System.out.println("color = " + color);
                Util.fillRaster(wr_att_u, color);
                Util.drawUnitIconEdges(wr_att_u, ws);
                Util.writeUnit(pixel_data, e.type, unit_icons, wr_att_u, ws);


                int dx = (int) ((0.15 + j * 1.15) * ws.unit_icon_size);
                int dy = (int) ((0.15 + i * 1.15) * ws.unit_icon_size);
                wr_att.setRect(dx, dy, wr_att_u);

//                Util.drawUnitDetails(g, e, dx, dy);

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

                if (e.health > 0 && !e.routed) {
                    Util.drawUnitDetails(g, e, dx, dy);
                }

            }

        }

        List<Unit> defender = game.getCombatStack("b");
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

//                System.out.println("color = " + color);
                Util.fillRaster(wr_att_u, color);
                Util.drawUnitIconEdges(wr_att_u, ws);
                Util.writeUnit(pixel_data, e.type, unit_icons, wr_att_u, ws);


                int dx = (int) ((0.15 + j * 1.15) * ws.unit_icon_size);
                int dy = (int) ((0.15 + i * 1.15) * ws.unit_icon_size);
                wr_att.setRect(dx, dy, wr_att_u);

//                Util.drawUnitDetails(g, e, dx, dy);
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

                if (e.health > 0 && !e.routed) {
                    Util.drawUnitDetails(g, e, dx, dy);
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
