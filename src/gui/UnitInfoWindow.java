/*
 * Copyright (C) 2015 joulupunikki joulupunikki@gmail.communist.invalid.
 * Copyright (C) 2014 Richard Wein
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

import com.ontalsoft.flc.lib.FLCAnimation;
import galaxyreader.Unit;
import game.Game;
import game.Square;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.Timer;
import state.State;
import util.C;
import util.FN;
import util.G.GF;
import util.G.UIW;
import util.StackIterator;
import util.Util;
import util.UtilG;
import util.WindowSize;

/**
 * Displays detailed information of the selected stack. Also functions as the
 * Group Finder.
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
    JButton disband_button;
    JButton unload_button;
    JButton exit_button;

//    private LinkedList<Unit> stacks;
    private JButton planet_name;

    JTextField spot_name;
    JTextField spot_stat;
    JTextField camo_name;
    JTextField camo_stat;

    UnitStats.Top top_stats;
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
    Unit prev;
    private Point selected_point;
    private int current_planet;
    private int selected_faction;
    BufferedImage unit_image;
    List<BufferedImage> unit_images;
    BufferedImage bi;

    private JButton prev_button;
    private JButton next_button;
    private JButton go_button;
    private JPopupMenu filter_move;
    private JMenuItem[] move_items;
    private JButton filter_move_button;
    private JPopupMenu filter_type;
    private JMenuItem[] type_items;
    private JButton filter_type_button;
    private JTextField[] res_display;

    private TYPE_FILTER type_filter;
    private Map<Enum, Integer> c;
    private Map<Enum, Integer> c_uiw;

    private JPanel unit_image_panel;
    private FLCAnimation unit_flc_animation;
    private Timer anim_timer;
    private int frame_number;

    public UnitInfoWindow(Gui gui) {
        this.gui = gui;
        game = gui.getGame();
        ws = gui.getWindowSize();
        c = ws.group_finder;
        c_uiw = ws.unit_info;
        type_filter = TYPE_FILTER.ALL_TYPES;
//        stacks = new LinkedList<>();
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setUpWindow() {
        setUpListeners();
        setUpButtons();
        setUpStatDisplay();
        setUpResDisplay();
        byte[][] pallette = gui.getPallette();
        bi = Util.loadImage(FN.S_UNITINFO_PCX, ws.is_double, pallette, 640, 480);
        setUpUnitImagePanel();
    }

    private void setUpUnitImagePanel() {
        unit_image_panel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawImage(unit_image, null, null);
//                g.drawImage(unit_image, 0, 0, null);
//                
//                g2d.drawImage(unit_image, null, 0, 0);
            }
        };
        this.add(unit_image_panel);
        int uix = 175;
        int uiy = 150;
        if (ws.is_double) {
            uix *= 2;
            uiy *= 2;
        }
        unit_image_panel.setBounds(ws.sw_flc_x, ws.sw_flc_y, uix, uiy);
        //unit_image_panel.setDoubleBuffered(true);
    }

    public void setUpResDisplay() {
        res_display = new JTextField[C.REQUIRED_RESOURCES.length];
        for (int i = 0; i < res_display.length; i++) {
            res_display[i] = new JTextField();
            this.add(res_display[i]);
            int x_offset = 123;
            if (ws.is_double) {
                x_offset *= 2;
            }
            res_display[i].setBounds(x_offset + ws.bp_res_display_x_offset + i * ws.pw_res_display_x_gap, ws.bp_res_display_y_offset, ws.bp_res_display_w, ws.bp_res_display_h);
//            res_display[i].setBackground(Color.WHITE);
            res_display[i].setOpaque(false);
            res_display[i].setForeground(C.COLOR_RES_DISP_GREEN);
            res_display[i].setEditable(false);
            res_display[i].setHorizontalAlignment(JTextField.CENTER);
            res_display[i].setBorder(null);
            res_display[i].setFont(ws.font_default);
//            res_display[i].setText("123");
        }

    }

    /**
     * Set mode, true for Group Finder, false for Unit Info.
     *
     * @param mode
     */
    public void setMode(boolean mode) {
        prev_button.setVisible(mode);
        next_button.setVisible(mode);
        go_button.setVisible(mode);
        prev_button.setEnabled(mode);
        next_button.setEnabled(mode);

//        filter_move_button.setVisible(mode);
        filter_type_button.setVisible(mode);

    }

    public void enablePrev(boolean mode) {
        prev_button.setEnabled(mode);
    }

    public void enableNext(boolean mode) {
        next_button.setEnabled(mode);
    }

    public void saveSelectedStack() {
        selected_point = game.getSelectedPoint();
        current_planet = game.getCurrentPlanetNr();
        selected_faction = game.getSelectedFaction().y;
    }

    public void restoreSelectedStack() {
        restoreSelected();
        forgetSelected();

    }

    public void forgetSelected() {
        selected_point = null;
        current_planet = -1;
        selected_faction = -1;
    }

    private boolean restoreSelected() {
        if (selected_point == null) {
            return true;
        }
        if (selected_faction == -1) {
            List<Unit> stack = game.getPlanet(current_planet).planet_grid.getHex(selected_point.x, selected_point.y).getStack();
            if (stack.isEmpty()) {
                return true;
            }
            game.setCurrentPlanetNr(current_planet);
            game.setSelectedFaction(-1);
            game.setSelectedPoint(new Point(selected_point.x, selected_point.y), -1);
        } else {
            List<Unit> stack = game.getPlanet(current_planet).space_stacks[selected_faction];
            if (stack.isEmpty()) {
                return true;
            }
            game.setCurrentPlanetNr(current_planet);
            game.setSelectedFaction(-1);
            game.setSelectedPoint(new Point(selected_point.x, selected_point.y), -1);
        }
        return false;
    }

//    public void initStacks() {
//        resetUnits();
//        sortStacks();
//
//    }
//
//    public List< getStacks() {
//        return stacks;
//    }
//
//    private void resetUnits() {
//        stacks.clear();
//        for (Unit unit : game.getUnits()) {
//            if (unit.owner == game.getTurn()) {
//                stacks.add(unit);
//            }
//        }
//    }
//
//    private void sortStacks() {
//        stacks.sort(Comp.unit_in_space);
//        stacks.sort(Comp.unit_xy);
//        stacks.sort(Comp.unit_pidx);
//    }
    public void setUpListeners() {
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

    private void setUpButtons() {
        setUpExit();
        setUpDisband();
        setUpUnload();
        setUpPrev();
        setUpNext();
        setUpGo();
        setUpPlanetName();
        setUpFilterType();
        setUpFilterTypeMenu();
    }

    private void setUpPlanetName() {
        planet_name = new JButton("Prev");
        this.add(planet_name);
        planet_name.setBackground(Color.BLACK);
        planet_name.setForeground(C.COLOR_GOLD);
        planet_name.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        planet_name.setBounds(c.get(GF.GAL_MAP_X), c.get(GF.PLAN_NAME_Y),
                ws.galactic_map_width, c.get(GF.PLAN_NAME_H));
    }

    private void setUpPrev() {
        prev_button = new JButton("Prev");
        this.add(prev_button);
        prev_button.setBackground(Color.BLACK);
        prev_button.setForeground(C.COLOR_GOLD);
        prev_button.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        prev_button.setBounds(c.get(GF.BUTTON_X), c.get(GF.BUTTON_Y),
                c.get(GF.BUTTON_W), c.get(GF.BUTTON_H));
        prev_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                State state = gui.getCurrentState();
                state.pressPrevButton();
            }
        });
    }

    private void setUpNext() {
        next_button = new JButton("Next");
        this.add(next_button);
        next_button.setBackground(Color.BLACK);
        next_button.setForeground(C.COLOR_GOLD);
        next_button.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        next_button.setBounds(c.get(GF.BUTTON2_X), c.get(GF.BUTTON_Y),
                c.get(GF.BUTTON_W), c.get(GF.BUTTON_H));
        next_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                State state = gui.getCurrentState();
                state.pressNextButton();
            }
        });
    }

    private void setUpGo() {
        go_button = new JButton("Go");
        this.add(go_button);
        go_button.setBackground(Color.BLACK);
        go_button.setForeground(C.COLOR_GOLD);
        go_button.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        go_button.setBounds(c.get(GF.BUTTON3_X), c.get(GF.BUTTON_Y),
                c.get(GF.BUTTON_W), c.get(GF.BUTTON_H));
        go_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                State state = gui.getCurrentState();
                state.pressGoButton();
            }
        });
    }

    private void setUpExit() {
        exit_button = new JButton("Exit");
        this.add(exit_button);
        exit_button.setBackground(Color.BLACK);
        exit_button.setForeground(C.COLOR_GOLD);
        exit_button.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        exit_button.setBounds(ws.ui_unload_x, ws.ui_unload_y + 2 * ws.ui_button_gap,
                ws.ui_unload_w, ws.std_button_h);
        exit_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                State state = gui.getCurrentState();
                state.pressExitButton();
            }
        });
    }

    private void setUpDisband() {
        disband_button = new JButton("Disband");
        this.add(disband_button);
        disband_button.setBackground(Color.BLACK);
        disband_button.setForeground(C.COLOR_GOLD);
        disband_button.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        disband_button.setBounds(ws.ui_disband_x, ws.ui_disband_y,
                ws.ui_disband_w, ws.std_button_h);
        disband_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                State state = gui.getCurrentState();
                state.pressDisbandButton();
            }
        });
    }

    private void setUpUnload() {
        unload_button = new JButton("Unload All");
        this.add(unload_button);
        unload_button.setBackground(Color.BLACK);
        unload_button.setForeground(C.COLOR_GOLD);
        unload_button.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        unload_button.setBounds(ws.ui_unload_x, ws.ui_unload_y,
                ws.ui_unload_w, ws.std_button_h);
        unload_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                State state = gui.getCurrentState();
                state.pressUnloadButton();
            }
        });
    }

    private void setUpFilterMove() {
        filter_move_button = new JButton("MT:All");
        this.add(filter_move_button);
        filter_move_button.setBounds(c.get(GF.BUTTON_X), c.get(GF.SENTRY_Y),
                c.get(GF.BUTTON_W), c.get(GF.BUTTON_H));
        filter_move_button.setBackground(Color.BLACK);
        filter_move_button.setForeground(C.COLOR_GOLD);
        filter_move_button.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        filter_move_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFilterMoveMenu();
            }
        });
    }

    private void showFilterMoveMenu() {
        filter_move.show(this, 0, 0);
    }

    private void setUpFilterMoveMenu() {
        filter_move = new JPopupMenu("Move Type");
        filter_move.setVisible(false);
        move_items = new JMenuItem[C.MoveType.values().length + 1];
        final int ZERO = 0;
        move_items[ZERO] = new JMenuItem("All");
        filter_move.add(move_items[ZERO]);
        move_items[ZERO].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectMove(ZERO);
            }
        });
        for (int i = 1; i < move_items.length; i++) {
            move_items[i] = new JMenuItem(C.MoveType.values()[i - 1].name());
            filter_move.add(move_items[i]);
            final int final_i = i;
            move_items[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectMove(final_i);
                }
            });
        }
    }

    private void selectMove(int move) {

    }

    private void setUpFilterType() {
        filter_type_button = new JButton("Type:All");
        this.add(filter_type_button);
        filter_type_button.setBounds(c.get(GF.BUTTON_X), c.get(GF.SENTRY_Y),
                c.get(GF.BUTTON_W), c.get(GF.BUTTON_H));
        filter_type_button.setBackground(Color.BLACK);
        filter_type_button.setForeground(C.COLOR_GOLD);
        filter_type_button.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        filter_type_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFilterTypeMenu();
            }
        });
    }

    private void showFilterTypeMenu() {
        filter_type.show(this, c.get(GF.BUTTON_X), c.get(GF.SENTRY_Y) - filter_type.getHeight());
    }

    public TYPE_FILTER getTypeFilter() {
        return type_filter;
    }

    public void stopAnimation() {
        anim_timer.stop();
    }

    public enum TYPE_FILTER {
        ALL_TYPES(-1),
        ENGINEERS(C.ENGINEER_UNIT_TYPE),
        TRANSPORTS(-1),
        CARGO_PODS(C.CARGO_UNIT_TYPE),
        NOBLES(C.NOBLE_UNIT_TYPE),
        ASSASSINS(C.SPY_UNIT_TYPE),
        SCEPTORS(C.SCEPTER_UNIT_TYPE);

        public int type_no;

        TYPE_FILTER(int type_no) {
            this.type_no = type_no;
        }
    }

    private void setUpFilterTypeMenu() {
        filter_type = new JPopupMenu("Unit Type");
        type_items = new JMenuItem[TYPE_FILTER.values().length];
        int idx = -1; //type_items.length;
        for (TYPE_FILTER value : TYPE_FILTER.values()) {
            type_items[++idx] = new JMenuItem(value.name());

            final TYPE_FILTER final_value = value;
            type_items[idx].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    enableNext(true);
                    enablePrev(true);
                    selectType(final_value);
                }
            });
        }
        for (int i = type_items.length - 1; i > -1; i--) {
            filter_type.add(type_items[i]);
        }
        filter_type.pack();
        filter_type.show(null, 0, 0);  // fixes menu starting size and pos
        filter_type.setVisible(false);
    }

    private void selectType(TYPE_FILTER value) {
        type_filter = value;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        renderUnitInfoWindow(g);

    }

    public void renderUnitInfoWindow(Graphics g) {
        setStats();
        drawBackground(g);
        drawUnits(g);
//        drawUnitDetails(g);
        drawDraggedUnit(g);
        drawTopStats(g);
        UtilG.drawCityArea(g, game, ws, ws.sw_city_x, ws.sw_city_y, null);
        planet_name.setText(game.getPlanet(game.getCurrentPlanetNr()).name);
        filter_type_button.setText(type_filter.name());
    }

    private void drawTopStats(Graphics g) {
        FontMetrics fm = this.getFontMetrics(ws.font_large);
        String s = "Name:";
        UtilG.drawStringGrad((Graphics2D) g, s, ws.font_large, ws.sw_tp_x - fm.stringWidth(s), ws.sw_tp_y);
        s = "Sect:";
        UtilG.drawStringGrad((Graphics2D) g, s, ws.font_large, ws.sw_tp_x - fm.stringWidth(s), ws.sw_tp_y2);
        s = "Health:";
        int offset = ws.sw_tp_h;
        UtilG.drawStringGrad((Graphics2D) g, s, ws.font_large, ws.sw_tp_x - fm.stringWidth(s), ws.sw_tp_y2 + offset);
        s = "Loyalty:";
        offset += ws.sw_tp_h;
        UtilG.drawStringGrad((Graphics2D) g, s, ws.font_large, ws.sw_tp_x - fm.stringWidth(s), ws.sw_tp_y2 + offset);
        s = "Experience:";
        offset += ws.sw_tp_h;
        UtilG.drawStringGrad((Graphics2D) g, s, ws.font_large, ws.sw_tp_x - fm.stringWidth(s), ws.sw_tp_y2 + offset);
        s = "Moves Left:";
        offset += ws.sw_tp_h;
        UtilG.drawStringGrad((Graphics2D) g, s, ws.font_large, ws.sw_tp_x - fm.stringWidth(s), ws.sw_tp_y2 + offset);
        Unit u = gui.getInfo_unit();
        if (u == null || (u.carrier != null && u.owner != game.getTurn())) {
            return;
        }
        s = u.type_data.name;
        UtilG.drawStringGrad((Graphics2D) g, s, ws.font_large, ws.sw_tp_x2, ws.sw_tp_y);
        s = "" + u.sect; //Util.getSectName(u.sect);
        UtilG.drawStringGrad((Graphics2D) g, s, ws.font_large, ws.sw_tp_x2, ws.sw_tp_y2);
        s = "" + u.health + "%";
        offset = ws.sw_tp_h;
        UtilG.drawStringGrad((Graphics2D) g, s, ws.font_large, ws.sw_tp_x2, ws.sw_tp_y2 + offset);
        s = "" + u.loyalty + "%";
        offset += ws.sw_tp_h;
        UtilG.drawStringGrad((Graphics2D) g, s, ws.font_large, ws.sw_tp_x2, ws.sw_tp_y2 + offset);
        s = "" + Unit.XP.values()[u.experience].getTitle();
        offset += ws.sw_tp_h;
        UtilG.drawStringGrad((Graphics2D) g, s, ws.font_large, ws.sw_tp_x2, ws.sw_tp_y2 + offset);
        s = "" + u.move_points;
        offset += ws.sw_tp_h;
        UtilG.drawStringGrad((Graphics2D) g, s, ws.font_large, ws.sw_tp_x2, ws.sw_tp_y2 + offset);
    }

    public void drawDetails(Graphics g, int owner, int prev_owner) {
        g.setColor(C.COLOR_GOLD);
        int scale = 1;
        if (ws.is_double) {
            scale = 2;
        }
        if (owner != prev_owner) {
            UtilG.drawStringGrad((Graphics2D) g, Util.factionNameDisplay(prev_owner), ws.font_large, 10 * scale, ws.main_window_height - 30 * scale);
            //g.drawString(factionNameDisplay(prev_owner), 10 * scale, ws.main_window_height - 30 * scale);
        }
        UtilG.drawStringGrad((Graphics2D) g, Util.factionNameDisplay(owner), ws.font_large, 10 * scale, ws.main_window_height - 10 * scale);
        //g.drawString(factionNameDisplay(owner), 10 * scale, ws.main_window_height - 10 * scale);
    }

    public void drawBackground(Graphics g) {

        byte[][] pallette = gui.getPallette();
        //BufferedImage bi = Util.loadImage(FN.S_UNITINFO_PCX, ws.is_double, pallette, 640, 480);

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);
        drawUnitImage(pallette, g);
    }

    private void drawUnitImage(byte[][] pallette, Graphics g) {
        Unit u = gui.getInfo_unit();
        if (u != null && (u.carrier == null || u.owner == game.getTurn())) {
            if (!u.equals(prev)) {
                String filename = FN.S_DIST_PREFIX + FN.S_FLC + FN.F_S + u.type_data.art;
                File flc = new File(filename);
                if (flc.exists()) {
                    try {
                        unit_flc_animation = new FLCAnimation(filename);
                        List<BufferedImage> tmp = unit_flc_animation.getFrames();
                        unit_images = new LinkedList<>();
                        for (Iterator<BufferedImage> iterator = tmp.iterator(); iterator.hasNext();) {

                            BufferedImage next = iterator.next();
                            if (ws.is_double) {
                                unit_images.add((BufferedImage) next.getScaledInstance(350, 300, BufferedImage.SCALE_FAST));
                            } else {
                                unit_images.add(next);
                            }
                        }
                        frame_number = 0;
                        unit_image = unit_images.get(frame_number);
                        setUpAnimTimer(unit_flc_animation.getDelayMs());
                        anim_timer.restart();
                    } catch (Exception e) {
                        e.printStackTrace(System.out);
                        unit_image = UtilG.loadFLCFirst(filename, ws.is_double, pallette, 175, 150);
                    }

                } else {
                    anim_timer.stop();
                    unit_image = UtilG.loadFLCFirst(FN.S_BLANK_FLC, ws.is_double, pallette, 175, 150);
                }
                prev = u;
            }
        } else {
            unit_image = UtilG.loadFLCFirst(FN.S_BLANK_FLC, ws.is_double, pallette, 175, 150);
        }
//        g.drawImage(unit_image, ws.sw_flc_x, ws.sw_flc_y, null);
//        g2d.drawImage(unit_image, null, ws.sw_flc_x, ws.sw_flc_y);
    }

    private void setUpAnimTimer(int delay) {
        /*
         * set animation timer
         */
        if (anim_timer != null) {
            anim_timer.setDelay(delay);
            return;
        }
        ActionListener timer_listener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (++frame_number >= unit_images.size()) {
                    frame_number = 0;
                }
                unit_image = unit_images.get(frame_number);
                unit_image_panel.repaint();

            }
        };
        anim_timer = new Timer(delay, timer_listener);
        //anim_timer.start();
    }

    public void setStats() {
        Unit u = gui.getInfo_unit();

        top_stats.setValues(null);
        left_stats.setValues(null);
        right_stats.setValues(null);
        attack_stats.setValues(null);

        if (u == null || (u.carrier != null && u.owner != game.getTurn())) {
            for (JTextField item : res_display) {
                item.setText("");
            }
            return;
        }
        UtilG.drawResourceIcons(bi.getRaster(), u.type_data.unit, gui, ws, 134, 166);
        int[] unit = {u.type, u.t_lvl};
        UtilG.drawResAmounts(unit, -1, game, res_display);
        top_stats.setValues(u);
        left_stats.setValues(u.type_data);
        right_stats.setValues(u.type_data);
        attack_stats.setValues(u.type_data);

    }

    public void setUpStatDisplay() {

        top_stats = new UnitStats.Top();

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
        drawDetails(g, stack.get(0).owner, stack.get(0).prev_owner); // TODO this should be elsewhere

        int mul = 1;
        if (ws.is_double) {
            mul = 2;
        }
        BufferedImage bi_tmp = new BufferedImage(C.BANNER100_SIDE * mul, C.BANNER100_SIDE * mul, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
        Util.writeRect(pixel_data, gui.getResources().getBanner100(stack.get(0).prev_owner), bi_tmp.getRaster(), ws, 0, 0, C.BANNER100_SIDE, C.BANNER100_SIDE);
        Image im;
        im = bi_tmp.getScaledInstance(c_uiw.get(UIW.BNR_S), c_uiw.get(UIW.BNR_S), Image.SCALE_FAST);
        g.drawImage(im, c_uiw.get(UIW.BNR_X), c_uiw.get(UIW.BNR_Y), null);

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
                if (e.isSelected()) {
                    color += 3;
                }
//                System.out.println("color = " + color);
                Util.fillRaster(wr, color);
                Util.drawUnitIconEdges(wr, ws);
                if (e.carrier == null || e.owner == game.getTurn()) {
                    Util.writeUnit(pixel_data, e.type, unit_icons, wr, ws);
                }
                int dx = (int) (ws.unit_panel_x_offset + j * 3.5 * ws.unit_icon_size);
                int dy = (int) (ws.unit_panel_y_offset + i * 1.15 * ws.unit_icon_size);

                g2d.drawImage(bi, null, dx, dy);

                if (e.carrier == null || e.owner == game.getTurn()) {

                    g.setColor(C.COLOR_GOLD);
                    g.setFont(ws.font_abbrev);

                    if (e.experience > 0 && e.type != C.CARGO_UNIT_TYPE) { // fix #100
                        if (e.experience == 1) {
                            g.setColor(Color.LIGHT_GRAY);
                        }
                        g.drawString("  " + Unit.XP.values()[e.experience].getTitle(), dx + ws.unit_icon_size + ws.carry_symbol_x,
                                dy + (int) 2.8 * ws.font_default_size);
                        g.setColor(C.COLOR_GOLD);
                    }

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
        if (u.isSelected()) {
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
