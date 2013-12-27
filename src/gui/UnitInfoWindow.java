/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import galaxyreader.Unit;
import game.Game;
import game.Square;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import state.State;
import util.C;
import util.StackIterator;
import util.Util;
import util.WindowSize;

/**
 *
 * @author joulupunikki
 */
public class UnitInfoWindow extends JPanel {

    Gui gui;
    Game game;
    WindowSize ws;
    JButton exit_button;
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
        exit_button.setBounds(ws.stack_window_exit_button_x, ws.stack_window_exit_button_y, 40, 15);
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
        BufferedImage bi = Util.loadImage("pcx/unitinfo.pcx", ws.is_double, pallette, 640, 480);

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);
    }

    public void setAttkStat(int field_no, String type, int acc, int str) {
        switch (field_no) {
            case 0:
                attack1_type.setText(type);
                attack1_stat.setText("" + acc + "/" + str);
                break;

            case 1:
                attack2_type.setText(type);
                attack2_stat.setText("" + acc + "/" + str);
                break;
            case 2:
                attack3_type.setText(type);
                attack3_stat.setText("" + acc + "/" + str);
                break;
            case 3:
                attack4_type.setText(type);
                attack4_stat.setText("" + acc + "/" + str);
                break;
            default:
                throw new AssertionError();
        }
    }

    public void setStats() {
        Unit u = gui.getInfo_unit();

        attack1_type.setText("");
        attack1_stat.setText("");

        attack2_type.setText("");
        attack2_stat.setText("");

        attack3_type.setText("");
        attack3_stat.setText("");

        attack4_type.setText("");
        attack4_stat.setText("");

        if (u == null) {
            return;
        }

        int field_no = 0;
        if (u.type_data.water_str > 0) {
            setAttkStat(field_no++, "Water", u.type_data.water_acc, u.type_data.water_str);
        }
        if (u.type_data.indirect_str > 0) {
            setAttkStat(field_no++, "Indirect", u.type_data.indirect_acc, u.type_data.indirect_str);
        }
        if (u.type_data.air_str > 0) {
            setAttkStat(field_no++, "Air", u.type_data.air_acc, u.type_data.air_str);
        }
        if (u.type_data.direct_str > 0) {
            setAttkStat(field_no++, "Direct", u.type_data.direct_acc, u.type_data.direct_str);
        }
        if (u.type_data.close_str > 0) {
            setAttkStat(field_no++, "Close", u.type_data.close_acc, u.type_data.close_str);
        }
        if (u.type_data.psy_str > 0) {
            setAttkStat(field_no++, "Psych", u.type_data.psy_acc, u.type_data.psy_str);
        }
        if (u.type_data.ranged_sp_str > 0) {
            setAttkStat(field_no++, "Ranged Sp", u.type_data.ranged_sp_acc, u.type_data.ranged_sp_str);
        }
        if (u.type_data.direct_sp_str > 0) {
            setAttkStat(field_no++, "Direct Sp", u.type_data.direct_sp_acc, u.type_data.direct_sp_str);
        }
        if (u.type_data.close_sp_str > 0) {
            setAttkStat(field_no++, "Close Sp", u.type_data.close_sp_acc, u.type_data.close_sp_str);
        }

    }

//    public void drawText(Graphics g) {
//        JTextField attack4_type;
//        attack4_type = new JTextField("Indirect");
//
//        this.add(attack4_type);
//        attack4_type.setBounds(ws.unit_info_attack_type_x, ws.unit_info_attack_type_y + ws.unit_info_attack_type_h,
//                ws.unit_info_attack_type_w, ws.unit_info_attack_type_h);
//        attack4_type.setBackground(Color.BLACK);
//        attack4_type.setForeground(C.COLOR_GOLD);
//        attack4_type.setEditable(false);
//        attack4_type.setHorizontalAlignment(JTextField.LEFT);
//        attack4_type.setBorder(null);
//        attack4_type.setFont(ws.font_default);
//
//        JTextField attack4_stat;
//        attack4_stat = new JTextField("8/90");
//
//        this.add(attack4_stat);
//        attack4_stat.setBounds(ws.unit_info_attack_stat_x, ws.unit_info_attack_stat_y + ws.unit_info_attack_stat_h,
//                ws.unit_info_attack_stat_w, ws.unit_info_attack_stat_h);
//        attack4_stat.setBackground(Color.BLACK);
//        attack4_stat.setForeground(C.COLOR_GOLD);
//        attack4_stat.setEditable(false);
//        attack4_stat.setHorizontalAlignment(JTextField.RIGHT);
//        attack4_stat.setBorder(null);
//        attack4_stat.setFont(ws.font_default);
//
//
//    }
    public void setUpStatDisplay() {

        attack1_type = new JTextField("Indirect");

        this.add(attack1_type);
        attack1_type.setBounds(ws.unit_info_attack_type_x, ws.unit_info_attack_type_y,
                ws.unit_info_attack_type_w, ws.unit_info_attack_type_h);
        attack1_type.setBackground(Color.BLACK);
        attack1_type.setForeground(C.COLOR_GOLD);
        attack1_type.setEditable(false);
        attack1_type.setHorizontalAlignment(JTextField.LEFT);
        attack1_type.setBorder(null);
        attack1_type.setFont(ws.font_default);

        attack1_stat = new JTextField("8/90");

        this.add(attack1_stat);
        attack1_stat.setBounds(ws.unit_info_attack_stat_x, ws.unit_info_attack_stat_y,
                ws.unit_info_attack_stat_w, ws.unit_info_attack_stat_h);
        attack1_stat.setBackground(Color.BLACK);
        attack1_stat.setForeground(C.COLOR_GOLD);
        attack1_stat.setEditable(false);
        attack1_stat.setHorizontalAlignment(JTextField.RIGHT);
        attack1_stat.setBorder(null);
        attack1_stat.setFont(ws.font_default);

        attack2_type = new JTextField("Indirect");

        this.add(attack2_type);
        attack2_type.setBounds(ws.unit_info_attack_type_x, ws.unit_info_attack_type_y + ws.unit_info_attack_type_h,
                ws.unit_info_attack_type_w, ws.unit_info_attack_type_h);
        attack2_type.setBackground(Color.BLACK);
        attack2_type.setForeground(C.COLOR_GOLD);
        attack2_type.setEditable(false);
        attack2_type.setHorizontalAlignment(JTextField.LEFT);
        attack2_type.setBorder(null);
        attack2_type.setFont(ws.font_default);

        attack2_stat = new JTextField("8/90");

        this.add(attack2_stat);
        attack2_stat.setBounds(ws.unit_info_attack_stat_x, ws.unit_info_attack_stat_y + ws.unit_info_attack_stat_h,
                ws.unit_info_attack_stat_w, ws.unit_info_attack_stat_h);
        attack2_stat.setBackground(Color.BLACK);
        attack2_stat.setForeground(C.COLOR_GOLD);
        attack2_stat.setEditable(false);
        attack2_stat.setHorizontalAlignment(JTextField.RIGHT);
        attack2_stat.setBorder(null);
        attack2_stat.setFont(ws.font_default);

        attack3_type = new JTextField("Indirect");

        this.add(attack3_type);
        attack3_type.setBounds(ws.unit_info_attack_type_x, ws.unit_info_attack_type_y + 2 * ws.unit_info_attack_type_h,
                ws.unit_info_attack_type_w, ws.unit_info_attack_type_h);
        attack3_type.setBackground(Color.BLACK);
        attack3_type.setForeground(C.COLOR_GOLD);
        attack3_type.setEditable(false);
        attack3_type.setHorizontalAlignment(JTextField.LEFT);
        attack3_type.setBorder(null);
        attack3_type.setFont(ws.font_default);

        attack3_stat = new JTextField("8/90");

        this.add(attack3_stat);
        attack3_stat.setBounds(ws.unit_info_attack_stat_x, ws.unit_info_attack_stat_y + 2 * ws.unit_info_attack_stat_h,
                ws.unit_info_attack_stat_w, ws.unit_info_attack_stat_h);
        attack3_stat.setBackground(Color.BLACK);
        attack3_stat.setForeground(C.COLOR_GOLD);
        attack3_stat.setEditable(false);
        attack3_stat.setHorizontalAlignment(JTextField.RIGHT);
        attack3_stat.setBorder(null);
        attack3_stat.setFont(ws.font_default);

        attack4_type = new JTextField("Indirect");

        this.add(attack4_type);
        attack4_type.setBounds(ws.unit_info_attack_type_x, ws.unit_info_attack_type_y + 3 * ws.unit_info_attack_type_h,
                ws.unit_info_attack_type_w, ws.unit_info_attack_type_h);
        attack4_type.setBackground(Color.BLACK);
        attack4_type.setForeground(C.COLOR_GOLD);
        attack4_type.setEditable(false);
        attack4_type.setHorizontalAlignment(JTextField.LEFT);
        attack4_type.setBorder(null);
        attack4_type.setFont(ws.font_default);

        attack4_stat = new JTextField("8/90");

        this.add(attack4_stat);
        attack4_stat.setBounds(ws.unit_info_attack_stat_x, ws.unit_info_attack_stat_y + 3 * ws.unit_info_attack_stat_h,
                ws.unit_info_attack_stat_w, ws.unit_info_attack_stat_h);
        attack4_stat.setBackground(Color.BLACK);
        attack4_stat.setForeground(C.COLOR_GOLD);
        attack4_stat.setEditable(false);
        attack4_stat.setHorizontalAlignment(JTextField.RIGHT);
        attack4_stat.setBorder(null);
        attack4_stat.setFont(ws.font_default);
    }

    public void drawUnits(Graphics g) {
        int faction = game.getSelectedFaction();
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
            stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[faction];
        }

//        boolean is_cargo_listing = false;
//        Iterator<Unit> iterator = stack.listIterator();
//        Iterator<Unit> cargo_it = null;
//        Unit e = iterator.next();
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
                g.drawString(e.type_data.abbrev, dx + ws.unit_icon_size + ws.carry_symbol_x,
                        dy + (int) 1.5 * ws.font_default_size);

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

                Util.drawUnitDetails(g, e, dx, dy);

                if (e != null) {
                    e = iterator.next();
                }
//                if (e == null) {
//                    return;
//                }

//                if (is_cargo_listing) {
//                    e = cargo_it.next();
//                    if (!cargo_it.hasNext()) {
//                        cargo_it = null;
//                        is_cargo_listing = false;
//                    }
//                } else if (e.cargo_list.isEmpty()) {
//                    if (iterator.hasNext()) {
//                        e = iterator.next();
//                    } else {
//                        return;
//                    }
//                } else {
//                    cargo_it = e.cargo_list.listIterator();
//                    e = cargo_it.next();
//                    if (cargo_it.hasNext()) {
//                        is_cargo_listing = true;
//                    }
//                }
            }

        }
    }

    public void drawDraggedUnit(Graphics g) {
        int faction = game.getSelectedFaction();
        Point p = gui.getDragPoint();
        if (p == null) {
            return;
        }
        Unit u = gui.getDragUnit();
        int[][] unit_icons = Gui.getUnitIcons();
        BufferedImage bi = new BufferedImage(ws.unit_icon_size, ws.unit_icon_size, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
        WritableRaster wr = bi.getRaster();
        int[] pixel_data = new int[1];
//        List<Unit> stack = null;
//        if (faction == -1) {
//            stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y).getStack();
//        } else {
//            Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
//            stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[faction];
//        }
//
//        Iterator<Unit> iterator = stack.listIterator();
//        Unit e = iterator.next();
        Graphics2D g2d = (Graphics2D) g;

//        for (int i = 0; i < C.STACK_WINDOW_UNITS_Y; i++) {
//            for (int j = 0; j < C.STACK_WINDOW_UNITS_X; j++) {
//                g.setColor(Color.BLACK);
//                g.fillRect((int) (ws.unit_panel_x_offset + j * 3.5 * ws.unit_icon_size),
//                        (int) (ws.unit_panel_y_offset + i * 1.15 * ws.unit_icon_size),
//                        3 * ws.unit_icon_size,
//                        ws.unit_icon_size);
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

        Util.drawUnitDetails(g, u, dx, dy);

    }
}
