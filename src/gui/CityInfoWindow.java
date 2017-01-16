/*
 * Copyright (C) 2016 joulupunikki joulupunikki@gmail.communist.invalid.
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

import dat.UnitType;
import galaxyreader.Structure;
import game.Game;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import util.C;
import util.Comp;
import util.FN;
import util.G;
import util.Util;
import util.UtilG;
import util.WindowSize;

/**
 * City Info Window Gui
 *
 * @author joulupunikki
 */
public class CityInfoWindow extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;
    private JButton exit;
    private JTable city_table;
    private JScrollPane city_table_view;
    private static Object[] city_table_header = {"Unit Production", "City Type", "Planet"};
    // pointer to map holding gui element coordinates
    private Map<Enum, Integer> c;

    private BufferedImage bi;

    private int current_planet_ptr;
    private Point selected_point_ptr;
    private Point selected_faction_ptr;
    private CityInfoWindow() {
    }

    private CityInfoWindow(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        c = ws.city_info;
        game = gui.getGame();
        this.bi = Util.loadImage(FN.S_BG0_PCX, ws.is_double, gui.getPallette(), 640, 480);
        setUpWindow();
    }

    public static CityInfoWindow getCityInfoWindow(Gui gui) {
        CityInfoWindow w = new CityInfoWindow(gui);
        w.setLayout(null);
        w.setPreferredSize(new Dimension(Gui.getWindowSize().main_window_width,
                Gui.getWindowSize().main_window_height));
        return w;
    }

    public void enterCityInfoWindow() {
        saveCurrentSelectedStack();
        Object[][] city_table_data = refreshCityTable();

        CityInfoWindow.CityTableModel city_model = new CityInfoWindow.CityTableModel(city_table_data, city_table_header);
        city_table.setModel(city_model);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(city_table.getModel());
        sorter.setComparator(1, Comp.city_name);
        city_table.setRowSorter(sorter);
        JScrollBar tmp = city_table_view.getVerticalScrollBar();
//        tmp.setBackground(Color.BLACK);
//        tmp.setForeground(C.COLOR_GOLD);
        UtilG.setJComponentChildrenToDark(tmp);
    }

    public Object[][] refreshCityTable() {
        List<Structure> cl = game.getStructures();
        List<Structure> cl2 = new LinkedList<>();
        for (Structure structure : cl) {
            if (structure.owner == game.getTurn()) {
                cl2.add(structure);
            }
        }
        Collections.sort(cl2, Comp.city_name);
        Structure[] cities = new Structure[cl2.size()];
        int idx = 0;
        for (Structure structure : cl2) {
            cities[idx++] = structure;
        }
        //        city_list.setListData(cities);
        int data_len = cities.length;
        //        int padded_len = 18;
//        data_len = data_len < padded_len ? padded_len : data_len;
        UnitType[][] unit_types = game.getUnitTypes();
        Object[][] city_table_data = new Object[data_len][];
        for (int i = 0; i < data_len; i++) {

            city_table_data[i] = new Object[3];
            if (i < cities.length) {
                city_table_data[i][1] = cities[i];
                if (cities[i].build_queue.isEmpty()) {
                    city_table_data[i][0] = "";
                } else {
                    int[] t = cities[i].build_queue.getFirst();
                    city_table_data[i][0] = unit_types[t[0]][t[1]].abbrev + " +" + cities[i].turns_left;
                }
                city_table_data[i][2] = game.getPlanet(cities[i].p_idx).name;

            } else {
                city_table_data[i][0] = null;
                city_table_data[i][1] = null;
                city_table_data[i][2] = null;

            }
        }
        return city_table_data;
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
        addCityTable();
    }

    private void setUpButtons() {
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
    public void addCityTable() {
        city_table = new JTable();
        city_table.setAutoCreateRowSorter(true);
        city_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        city_table_view = new JScrollPane(city_table);
        city_table.setFillsViewportHeight(true);
        city_table_view.setPreferredSize(new Dimension(250, 80));
        city_table.setBackground(Color.BLACK);
        this.add(city_table_view);
        city_table_view.setBounds(c.get(G.CIW.T_X), c.get(G.CIW.T_Y),
                c.get(G.CIW.T_W), c.get(G.CIW.T_H));
        JTableHeader header = city_table.getTableHeader();
        header.setFont(ws.font_default);
        header.setBackground(Color.BLACK);
        header.setForeground(C.COLOR_GOLD);
        city_table.setGridColor(Color.BLACK);
        city_table.setRowHeight(ws.city_table_row_height);
        city_table.setDefaultRenderer(Object.class, new CityTableRenderer());
        UtilG.setJComponentChildrenToDark(city_table_view);
        DefaultListSelectionModel list_selection_model;
        list_selection_model = (DefaultListSelectionModel) city_table.getSelectionModel();
        list_selection_model.addListSelectionListener(
                new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
//                    citySelected(e);
//                    zeroResources();
                }
            }
        });
        city_table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                JTable table = (JTable) e.getSource();
                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                if (row == -1) {
                    return;
                }
//                int selected_city = table.convertRowIndexToModel(city_table.getSelectedRow());
                int selected_city = city_table.getSelectedRow();
                System.out.println("DEBUG " + selected_city);
                //System.out.println("selected_city = " + selected_city);
                Structure city = (Structure) city_table.getValueAt(selected_city, 1);
                if (e.getClickCount() == 1) {
                    game.setCurrentPlanetNr(city.p_idx);
                    game.setSelectedPoint(new Point(city.x, city.y), -1);
                    game.setSelectedFaction(-1);
                }
                if (e.getClickCount() == 2) {
                    //System.out.println("Double clicked row " + row);

                    gui.showBuildWindow(null, city.p_idx, city);
                    String val = null;

                    int len = city_table.getRowCount();
                    Structure city_ptr = null;
                    for (int i = 0; i < len; i++) {
                        city_ptr = (Structure) city_table.getValueAt(i, 1);
                        if (!city_ptr.build_queue.isEmpty()) {
                            int[] unit = city_ptr.build_queue.get(0);
                            val = game.getUnitTypes()[unit[0]][unit[1]].abbrev + " +" + city_ptr.turns_left;
                        } else {
                            val = null;
                        }
                        city_table.setValueAt(val, i, 0);
                    }

                }
            }
        });
    }

    private void saveCurrentSelectedStack() {
        current_planet_ptr = game.getCurrentPlanetNr();
        selected_point_ptr = new Point(game.getSelectedPoint()); // fix #105
        selected_faction_ptr = new Point(game.getSelectedFaction());
    }

    public void restoreCurrentSelectedStack() {
        game.setCurrentPlanetNr(current_planet_ptr);
        game.setSelectedPoint(selected_point_ptr, -1); // fix #105
        game.setSelectedFaction(selected_faction_ptr.x, selected_faction_ptr.y);
    }

    private void renderWindow(Graphics g) {
        drawBackground(g);
        drawDetails(g);
    }
    
    private void drawBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        WritableRaster wr = bi.getRaster();
        g2d.drawImage(bi, null, 0, 0);
    }

    private void drawDetails(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
//        drawHeader(g);
        drawHeader(g);
        
    }

    
    private void drawHeader(Graphics2D g) {
        String s = "Unit Production Information";
        int x = UtilG.center(g, 0, ws.main_window_width, ws.font_large, s);
        UtilG.drawStringGrad(g, s, ws.font_large, x, c.get(G.CIW.H_Y));
    }

//    private void drawHeader(Graphics2D g) {
//        UtilG.drawStringGrad(g, "Tax", ws.font_large, c.get(CH.TAX_H_X), c.get(CH.TAX_H_Y));
//    }

    class CityTableModel extends DefaultTableModel {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        //        public BuildTableModel(Object[][] data) {
//            Object[] column_names = {"Unit", "Turns Left"};
//            BuildTableModel(data, column_names);
//        }
        public CityTableModel(Object[][] data, Object[] column_names) {
            super(data, column_names);
        }

        public boolean isCellEditable(int row, int col) {

            return false;

        }
    }

    public class CityTableRenderer extends JLabel
            implements TableCellRenderer {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public CityTableRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            Color c_b = Color.BLACK;
            Color c_f = C.COLOR_GOLD;
            if (isSelected) {
                setBackground(c_f);
                setForeground(c_b);
            } else {
                setBackground(c_b);
                setForeground(c_f);
            }
            String val = null;
            if (value == null) {
                val = " ";
            } else if (value instanceof Structure) {
                Structure str = (Structure) value;
//            String val = Structure.getName(str.type);
                val = game.getStrBuild(str.type).name.toLowerCase(Locale.ROOT);
            } else {
                val = (String) value;
            }
            setFont(ws.font_default);
            setText(val);
            return this;
        }
    }
}
