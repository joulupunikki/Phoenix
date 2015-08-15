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
import game.Square;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import state.State;
import util.C;
import util.FN;
import util.StackIterator;
import util.Util;
import util.WindowSize;

/**
 *
 * @author joulupunikki
 */
public class UnitInfoWindow extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Gui gui;
    Game game;
    WindowSize ws;
    JButton exit_button;
    JTextField spot_name;
    JTextField spot_stat;
    JTextField camo_name;
    JTextField camo_stat;

    UnitStats.Left left_stats;
    UnitStats.Right right_stats;
    UnitStats.Attack attack_stats;

    JTextField attack1_stat;
    JTextField attack1_type;
    JTextField attack2_type;
    JTextField attack2_stat;
    JTextField attack3_type;
    JTextField attack3_stat;
    JTextField attack4_type;
    JTextField attack4_stat;

    public UnitInfoWindow(Gui gui) {
        this.gui = gui;
        game = gui.getGame();
        ws = gui.getWindowSize();
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setUpWindow() {
        setUpListeners();
        setUpStatDisplay();
    }

    public void setUpListeners() {
        exit_button = new JButton("Exit");
        this.add(exit_button);
        exit_button.setBackground(Color.BLACK);
        exit_button.setForeground(C.COLOR_GOLD);
        exit_button.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        exit_button.setBounds(ws.stack_window_exit_button_x, ws.stack_window_exit_button_y,
                ws.stack_window_exit_button_w, ws.stack_window_exit_button_h);
        exit_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                State state = gui.getCurrentState();
                state.pressExitButton();
            }
        });

        MouseAdapter mouse_adapter = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                State state = gui.getCurrentState();
                state.clickOnWindow(e);
            }

            public void mouseDragged(MouseEvent e) {
                State state = gui.getCurrentState();
                state.dragOnWindow(e);
            }

            public void mouseReleased(MouseEvent e) {
                State state = gui.getCurrentState();
                state.releaseOnWindow(e);
            }
        };
        this.addMouseListener(mouse_adapter);
        this.addMouseMotionListener(mouse_adapter);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        renderStackWindow(g);

    }

    public void renderStackWindow(Graphics g) {
        setStats();
        drawBackground(g);
        drawUnits(g);
//        drawUnitDetails(g);
        drawDraggedUnit(g);
    }

    public void drawBackground(Graphics g) {

        byte[][] pallette = gui.getPallette();
        BufferedImage bi = Util.loadImage(FN.S_UNITINFO_PCX, ws.is_double, pallette, 640, 480);

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);
    }

    public void setStats() {
        Unit u = gui.getInfo_unit();

        left_stats.setValues(null);
        right_stats.setValues(null);
        attack_stats.setValues(null);

        if (u == null) {
            return;
        }

        left_stats.setValues(u.type_data);
        right_stats.setValues(u.type_data);
        attack_stats.setValues(u.type_data);

    }

    public void setUpStatDisplay() {

        left_stats = new UnitStats.Left(gui);
        left_stats.setBounds(ws.sw_lsp_x, ws.sw_lsp_y, ws.sw_lsp_w, ws.sw_lsp_h);
        this.add(left_stats);

        right_stats = new UnitStats.Right(gui);
        right_stats.setBounds(ws.sw_rsp_x, ws.sw_lsp_y, ws.sw_lsp_w, ws.sw_lsp_h);
        this.add(right_stats);

        attack_stats = new UnitStats.Attack(gui);
        attack_stats.setBounds(ws.sw_ap_x, ws.sw_lsp_y, ws.sw_ap_w, ws.sw_ap_h);
        this.add(attack_stats);

    }

    public void drawUnits(Graphics g) {
        int faction = game.getSelectedFaction().x;
        Point p = game.getSelectedPoint();
        int[][] unit_icons = Gui.getUnitIcons();
        BufferedImage bi = new BufferedImage(ws.unit_icon_size, ws.unit_icon_size, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
        WritableRaster wr = bi.getRaster();
        int[] pixel_data = new int[1];
        List<Unit> stack = null;
        if (faction == -1) {
            stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y).getStack();
        } else {
            Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
            stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[game.getSelectedFaction().y];
        }

        if (game.getTurn() != stack.get(0).owner) {

            List<Unit> tmp = new LinkedList<>();
            for (Unit unit : stack) {
                if (unit.spotted[game.getTurn()]) {
                    tmp.add(unit);
                }
            }
            stack = tmp;
        }

        StackIterator iterator = new StackIterator(stack);
        Unit e = iterator.next();

        Graphics2D g2d = (Graphics2D) g;

        for (int i = 0; i < C.STACK_WINDOW_UNITS_Y; i++) {
            for (int j = 0; j < C.STACK_WINDOW_UNITS_X; j++) {
                g.setColor(Color.BLACK);
                g.fillRect((int) (ws.unit_panel_x_offset + j * 3.5 * ws.unit_icon_size),
                        (int) (ws.unit_panel_y_offset + i * 1.15 * ws.unit_icon_size),
                        3 * ws.unit_icon_size,
                        ws.unit_icon_size);

                if (e == null) {
                    continue;
                }
                int color = Util.getOwnerColor(e.owner);
                if (e.selected) {
                    color += 3;
                }
//                System.out.println("color = " + color);
                Util.fillRaster(wr, color);
                Util.drawUnitIconEdges(wr, ws);
                Util.writeUnit(pixel_data, e.type, unit_icons, wr, ws);

                int dx = (int) (ws.unit_panel_x_offset + j * 3.5 * ws.unit_icon_size);
                int dy = (int) (ws.unit_panel_y_offset + i * 1.15 * ws.unit_icon_size);

                g2d.drawImage(bi, null, dx, dy);

                g.setColor(C.COLOR_GOLD);
                g.setFont(ws.font_abbrev);

                if (e.type == C.CARGO_UNIT_TYPE) {
                    g.drawString(game.getResTypes()[e.res_relic].name, dx + ws.unit_icon_size + ws.carry_symbol_x,
                            dy + (int) 1.5 * ws.font_default_size);    // For resource pod, show resource type instead of abbrev - RSW
                    g.drawString("  " + String.valueOf(e.amount) + " pts", dx + ws.unit_icon_size + ws.carry_symbol_x,
                            dy + (int) 2.8 * ws.font_default_size);    // Also show num of resource points - RSW 
                } else {
                    g.drawString(e.type_data.abbrev, dx + ws.unit_icon_size + ws.carry_symbol_x,
                            dy + (int) 1.5 * ws.font_default_size);
                }

                for (int k = 0; k < e.type_data.cargo; k++) {
                    g.setColor(Color.GRAY);
                    g.fill3DRect(dx + (k + 1) * ws.carry_symbol_x + k * ws.carry_symbol_w + ws.unit_icon_size,
                            dy + ws.carry_symbol_y, ws.carry_symbol_w, ws.carry_symbol_h,
                            true);

                }

                if (e.carrier != null) {
                    g.setColor(Color.GRAY);
                    for (int k = 0; k < 4; k++) {
                        g.drawString("+", dx - ws.font_unit_icon_size, (int) (dy + (k + 0.5) * ws.font_unit_icon_size));
                    }
                }

                Util.drawUnitDetails(g, game, e, dx, dy);

                if (e != null) {
                    e = iterator.next();
                }
            }

        }
    }

    public void drawDraggedUnit(Graphics g) {
        Point p = gui.getDragPoint();
        if (p == null) {
            return;
        }
        Unit u = gui.getDragUnit();
        int[][] unit_icons = Gui.getUnitIcons();
        BufferedImage bi = new BufferedImage(ws.unit_icon_size, ws.unit_icon_size, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
        WritableRaster wr = bi.getRaster();
        int[] pixel_data = new int[1];
        Graphics2D g2d = (Graphics2D) g;
        int color = Util.getOwnerColor(u.owner);
        if (u.selected) {
            color += 3;
        }
//                System.out.println("color = " + color);
        Util.fillRaster(wr, color);
        Util.drawUnitIconEdges(wr, ws);
        Util.writeUnit(pixel_data, u.type, unit_icons, wr, ws);

        int dx = p.x;
        int dy = p.y;

        g2d.drawImage(bi, null, dx, dy);

        Util.drawUnitDetails(g, game, u, dx, dy);

    }
}
