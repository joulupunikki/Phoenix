/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import util.C;
import util.Util;
import util.WindowSize;

/**
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class BuildPanel extends JPanel {

    private Gui gui;
    private Game game;
    private WindowSize ws;
    private JList planet_list;
    private JList city_list;
    private JTable city_table;
    private JTable build_table;
    private JTable queue_table;
    private static Object[] build_table_header = {"Unit", "Turns Left"};
//    private Object[][] city_table_data;
//    private Object[][] build_table_data;

    public BuildPanel(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        game = gui.getGame();
        addLists();
//        List<Planet> planets = game.getPlanets();
    }

    public void addLists() {

        addPlanetList();
        addCityTable();
        addBuildTable();
        addQueueTable();
    }

    public void addQueueTable() {
        queue_table = new JTable();
        queue_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane queue_table_view = new JScrollPane(queue_table);
        queue_table.setFillsViewportHeight(true);
        queue_table.setBackground(Color.BLACK);
        queue_table_view.setPreferredSize(new Dimension(250, 80));
        this.add(queue_table_view);
        queue_table_view.setBounds(ws.queue_table_x_offset, ws.queue_table_y_offset,
                ws.queue_table_width, ws.queue_table_height);
        JTableHeader header = queue_table.getTableHeader();
        header.setFont(ws.font_default);
        header.setBackground(Color.black);
        header.setForeground(C.COLOR_GOLD);
        queue_table.setRowHeight(ws.city_table_row_height);
        queue_table.setDefaultRenderer(Object.class, new BuildTableRenderer());
        queue_table.setDefaultRenderer(Integer.class, new BuildTableRenderer());
        queue_table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JTable table = (JTable) e.getSource();
                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                if (row == -1) {
                    return;
                }
                if (e.getClickCount() == 1) {
                    System.out.println("Single clicked row " + row);
                }
                if (e.getClickCount() == 2) {
                    System.out.println("Double clicked row " + row);
                    int selected_city = city_table.getSelectedRow();
                    Structure city = (Structure) city_table.getValueAt(selected_city, 0);
                    city.removeFromQueue(row, game.getUnitTypes());
                    setQueueData(null, city);
                }
            }
        });
    }

    public void addBuildTable() {
        build_table = new JTable();
        build_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane build_table_view = new JScrollPane(build_table);
        build_table.setFillsViewportHeight(true);
        build_table.setBackground(Color.BLACK);
        build_table_view.setPreferredSize(new Dimension(250, 80));
        this.add(build_table_view);
        build_table_view.setBounds(ws.build_table_x_offset, ws.build_table_y_offset,
                ws.build_table_width, ws.build_table_height);
//        ListSelectionModel list_selection_model;
//        list_selection_model = build_table.getSelectionModel();
//        list_selection_model.addListSelectionListener(
//                new ListSelectionListener() {
//                    public void valueChanged(ListSelectionEvent e) {
//                        buildSelected(e);
//                    }
//                });
        JTableHeader header = build_table.getTableHeader();
        header.setFont(ws.font_default);
        header.setBackground(Color.black);
        header.setForeground(C.COLOR_GOLD);
        build_table.setRowHeight(ws.city_table_row_height);
        build_table.setDefaultRenderer(Object.class, new BuildTableRenderer());
        build_table.setDefaultRenderer(Integer.class, new BuildTableRenderer());

        build_table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JTable table = (JTable) e.getSource();
                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                if (row == -1) {
                    return;
                }
                if (e.getClickCount() == 1) {
                    System.out.println("Single clicked row " + row);
                }
                if (e.getClickCount() == 2) {
                    System.out.println("Double clicked row " + row);
                    int selected_city = city_table.getSelectedRow();
                    Structure city = (Structure) city_table.getValueAt(selected_city, 0);
                    int[] tmp = (int[]) build_table.getValueAt(row, 0);
                    int[] unit = {tmp[0], tmp[1]};
                    city.addToQueue(unit, game.getUnitTypes());
                    setQueueData(null, city);
                }
            }
        });
    }

    public void addCityTable() {
        city_table = new JTable();
//        city_table.setAutoCreateRowSorter(true);
        city_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane city_table_view = new JScrollPane(city_table);
        city_table.setFillsViewportHeight(true);
        city_table_view.setPreferredSize(new Dimension(250, 80));
//        city_table_view.setBackground(Color.BLACK);
        city_table.setBackground(Color.BLACK);
        this.add(city_table_view);
        city_table_view.setBounds(ws.city_table_x_offset, ws.planet_list_y_offset,
                ws.planet_list_width, ws.planet_list_height);
        JTableHeader header = city_table.getTableHeader();
        header.setFont(ws.font_default);
        header.setBackground(Color.BLACK);
        header.setForeground(C.COLOR_GOLD);
        city_table.setGridColor(Color.BLACK);
        city_table.setRowHeight(ws.city_table_row_height);
        city_table.setDefaultRenderer(Object.class, new CityTableRenderer());
        DefaultListSelectionModel list_selection_model;
        list_selection_model = (DefaultListSelectionModel) city_table.getSelectionModel();
        list_selection_model.addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if (!e.getValueIsAdjusting()) {
                            citySelected(e);
                        }
                    }
                });
    }

    public void addPlanetList() {
        planet_list = new JList();
        planet_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        planet_list.setLayoutOrientation(JList.VERTICAL);
        CustomRendererInt renderer = new CustomRendererInt();
//        renderer.setPreferredSize(new Dimension(ws.planet_list_cell_w, ws.planet_list_cell_h));
        planet_list.setCellRenderer(renderer);
        JScrollPane planet_view = new JScrollPane(planet_list);
        planet_view.setPreferredSize(new Dimension(250, 80));
        planet_view.setBackground(Color.BLACK);
        planet_list.setBackground(Color.BLACK);

        this.add(planet_view);
        planet_view.setBounds(ws.planet_list_x_offset, ws.planet_list_y_offset,
                ws.planet_list_width, ws.planet_list_height);
        DefaultListSelectionModel list_selection_model;
        list_selection_model = (DefaultListSelectionModel) planet_list.getSelectionModel();
        ListSelectionListener[] lsl_list = list_selection_model.getListSelectionListeners();
        for (ListSelectionListener lsl : lsl_list) {
            System.out.println("lsl = " + lsl);
        }
        list_selection_model.addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if (!e.getValueIsAdjusting()) {
                            planetSelected(e);
                        }
                    }
                });
    }

    public void clearSelection() {
        planet_list.clearSelection();
    }

    public void planetSelected(ListSelectionEvent e) {
        System.out.println("planet_list selected value = " + planet_list.getSelectedValue());
        Object tmp = planet_list.getSelectedValue();
        if (tmp == null) {
            return;
        }
        int planet = ((Integer) tmp).intValue();
        if (planet == -1) {
            return;
        }
        List<Structure> cl = game.getStructures();
        List<Structure> cl2 = new LinkedList<>();
        for (Structure structure : cl) {
            if (structure.owner == game.getTurn() && structure.p_idx == planet) {
                if (!Structure.can_build.get(structure.type).isEmpty()) {
                    cl2.add(structure);
                }
            }
        }
        Structure[] cities = new Structure[cl2.size()];
        int idx = 0;
        for (Structure structure : cl2) {
            cities[idx++] = structure;
        }
//        city_list.setListData(cities);
        int data_len = cities.length;
//        int padded_len = 18;
//        data_len = data_len < padded_len ? padded_len : data_len;
        Object[][] city_table_data = new Object[data_len][];
        for (int i = 0; i < data_len; i++) {

            city_table_data[i] = new Object[2];
            if (i < cities.length) {
                city_table_data[i][0] = cities[i];
                city_table_data[i][1] = "Militia lgn";
            } else {
                city_table_data[i][0] = null;
                city_table_data[i][1] = null;
            }
        }

        CityTableModel city_model = new CityTableModel(city_table_data);
        city_table.setModel(city_model);

        System.out.println("row height" + city_table.getRowHeight());
    }

    public void setBuildData(ListSelectionEvent e, Structure city) {

        ArrayList<int[]> al = Structure.can_build.get(city.type);
        int nr_units = al.size();
        UnitType[][] unit_types = game.getUnitTypes();
        int data_len = nr_units;
//        int padded_len = 9;
//        data_len = data_len < padded_len ? padded_len : data_len;
        Object[][] build_table_data = new Object[data_len][];
        for (int i = 0; i < data_len; i++) {
            build_table_data[i] = new Object[2];
            if (i < nr_units) {
                int[] unit_type = al.get(i);
                build_table_data[i][0] = unit_type;
                build_table_data[i][1] = new Integer(unit_types[unit_type[0]][unit_type[1]].turns_2_bld);
            } else {
                build_table_data[i][0] = null;
                build_table_data[i][1] = null;
            }
        }

        BuildTableModel build_model = new BuildTableModel(build_table_data,
                build_table_header);
        build_table.setModel(build_model);
//        System.out.println("CellRend 0 " + build_table.getCellRenderer(0, 0));
//        System.out.println("CellRend 1 " + build_table.getCellRenderer(0, 1));
        TableColumn column = build_table.getColumnModel().getColumn(0);
        column.setPreferredWidth(ws.build_table_cell_0_width);
        column = build_table.getColumnModel().getColumn(1);
        column.setPreferredWidth(ws.build_table_cell_1_width);
    }

    public void setQueueData(ListSelectionEvent e, Structure city) {
        List<int[]> queue = city.build_queue;
        int nr_units = queue.size();
        if (nr_units == 0) {
            if (queue_table.getRowCount() != 0) {
                ((BuildTableModel) queue_table.getModel()).setRowCount(nr_units);
            }
            return;
        }
        UnitType[][] unit_types = game.getUnitTypes();
        int data_len = nr_units;
//        int padded_len = 9;
//        data_len = data_len < padded_len ? padded_len : data_len;
        Object[][] queue_table_data = new Object[data_len][];
        ListIterator<int[]> iter = queue.listIterator();
        int[] unit_type = null;
        if (iter.hasNext()) {
            unit_type = iter.next();
        }
        for (int i = 0; i < data_len; i++) {
            queue_table_data[i] = new Object[2];
//            if (i == 0 && nr_units == 0) {
//                int[] dummy = {-1, -1};
//                queue_table_data[i][0] = dummy;
//                queue_table_data[i][1] = new Integer(-1);
//            } else 
            if (i < nr_units) {

                queue_table_data[i][0] = unit_type;
                queue_table_data[i][1] = new Integer(unit_types[unit_type[0]][unit_type[1]].turns_2_bld);
                if (iter.hasNext()) {
                    unit_type = iter.next();
                }
            } else {
                queue_table_data[i][0] = null;
                queue_table_data[i][1] = null;
            }
        }

        BuildTableModel queue_model = new BuildTableModel(queue_table_data,
                build_table_header);
        queue_table.setModel(queue_model);

        System.out.println("CellRend 0 " + queue_table.getCellRenderer(0, 0));
        System.out.println("CellRend 1 " + queue_table.getCellRenderer(0, 1));
        TableColumn column = queue_table.getColumnModel().getColumn(0);
        column.setPreferredWidth(ws.queue_table_cell_0_width);
        column = queue_table.getColumnModel().getColumn(1);
        column.setPreferredWidth(ws.queue_table_cell_1_width);
    }

    public void citySelected(ListSelectionEvent e) {
        int row = city_table.getSelectedRow();
        if (row == -1) {
            return;
        }
        Object tmp = city_table.getValueAt(row, 0);
        if (tmp == null) {
            return;
        }
        Structure city = (Structure) tmp;
        setBuildData(e, city);
        setQueueData(e, city);
    }

    public void buildSelected(ListSelectionEvent e) {
        int row = build_table.getSelectedRow();
        if (row == -1) {
            return;
        }
        Object tmp = build_table.getValueAt(row, 0);
    }

    public void setPlanets() {
        boolean[] planets = new boolean[game.getPlanets().size()];
        for (int i = 0; i < planets.length; i++) {
            planets[i] = false;
        }
        List<Structure> cities = game.getStructures();
        for (Structure structure : cities) {
            if (structure.owner == game.getTurn()) {
                planets[structure.p_idx] = true;
            }
        }
        ArrayList<Integer> planet_indexes = new ArrayList();
        for (int i = 0; i < planets.length; i++) {
            if (planets[i]) {
                planet_indexes.add(new Integer(i));
            }
        }
//        int padding = 16 - planet_indexes.size();
//        for (int i = 0; i < padding; i++) {
//            planet_indexes.add(new Integer(-1));
//
//        }

        planet_list.setListData(planet_indexes.toArray());

    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(33, 33, 33));
        g.fillRect(0, 0, ws.planet_map_width, ws.planet_map_height);
        byte[][] pallette = gui.getPallette();
        String file = "PCX" + System.getProperty("file.separator") + "UNITBG2.PCX";
        BufferedImage bi = Util.loadImage(file, ws.is_double, pallette, 504, 209);

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);
    }

    class CustomRendererInt extends JLabel
            implements ListCellRenderer {

        public CustomRendererInt() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
        }

        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

//            int selectedIndex = ((Integer) value).intValue();
            Color c_f = C.COLOR_GOLD;
            Color c_b = Color.BLACK;
            int val = ((Integer) value).intValue();
            String s_val;
            if (val > -1) {
                s_val = game.getPlanet(val).name;
            } else {
                s_val = " ";
            }
            if (isSelected) {
                setBackground(c_f);
                setForeground(c_b);
            } else {
                setBackground(c_b);
                setForeground(c_f);
            }

//            String planet = planets_strings[selectedIndex];
            setText(s_val);
            setFont(ws.font_default);

            return this;
        }

    }

    class CustomRendererStruct extends JLabel
            implements ListCellRenderer {

        public CustomRendererStruct() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
        }

        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

//            int selectedIndex = ((Integer) value).intValue();
            Color c_f = C.COLOR_GOLD;
            Color c_b = Color.BLACK;
            Structure str = (Structure) value;
//            String val = Structure.getName(str.type);
            String val = game.getStrBuild(str.type).name;
            if (isSelected) {
                setBackground(c_f);
                setForeground(c_b);
            } else {
                setBackground(c_b);
                setForeground(c_f);
            }

//            String planet = planets_strings[selectedIndex];
            setText(val);
            setFont(list.getFont());

            return this;
        }

    }

    public class CityTableRenderer extends JLabel
            implements TableCellRenderer {

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
                val = game.getStrBuild(str.type).name;
            } else {
                val = (String) value;
            }
            setFont(ws.font_default);
            setText(val);
            return this;
        }
    }

    public class BuildTableRenderer extends JLabel
            implements TableCellRenderer {

        public BuildTableRenderer() {
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
            } else if (value instanceof int[]) {
                int[] tmp = (int[]) value;
//            String val = Structure.getName(str.type);
                if (tmp[0] == -1) {
                    val = " ";
                }
                UnitType[][] unit_types = game.getUnitTypes();
                val = unit_types[tmp[0]][tmp[1]].name;
            } else {
                val = "" + ((Integer) value).intValue();
                setHorizontalTextPosition(RIGHT);
            }

            setFont(ws.font_default);
            setText(val);

            return this;
        }
    }

    class CityTableModel extends AbstractTableModel {

        private String[] columnNames = {"City", "Building"};
        private Object[][] data;

        public CityTableModel(Object[][] data) {
            this.data = data;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {

            return false;

        }

    }

    class QueueTableModel extends AbstractTableModel {

        private String[] columnNames = {"Unit", "Turns"};
        private Object[][] data;

        public QueueTableModel(Object[][] data) {
            this.data = data;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {

            return false;

        }
    }

    class BuildTableModel extends DefaultTableModel {

//        public BuildTableModel(Object[][] data) {
//            Object[] column_names = {"Unit", "Turns Left"};
//            BuildTableModel(data, column_names);
//        }
        public BuildTableModel(Object[][] data, Object[] column_names) {
            super(data, column_names);
        }

        public boolean isCellEditable(int row, int col) {

            return false;

        }
    }

//    class BuildTableModel extends AbstractTableModel {
//
//        private String[] columnNames = {"Unit", "Turns Left"};
//        private Object[][] data;
//
//        public BuildTableModel(Object[][] data) {
//            this.data = data;
//        }
//
//        public int getColumnCount() {
//            return columnNames.length;
//        }
//
//        public int getRowCount() {
//            return data.length;
//        }
//
//        public String getColumnName(int col) {
//            return columnNames[col];
//        }
//
//        public Object getValueAt(int row, int col) {
//            return data[row][col];
//        }
//
//        public Class getColumnClass(int c) {
//            return getValueAt(0, c).getClass();
//        }
//
//        /*
//         * Don't need to implement this method unless your table's
//         * editable.
//         */
//        public boolean isCellEditable(int row, int col) {
//
//            return false;
//
//        }
//
//    }
}
