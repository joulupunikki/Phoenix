/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import dat.Tech;
import dat.UnitType;
import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Game;
import game.GameResources;
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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import static javax.swing.SwingConstants.RIGHT;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import state.PW1;
import util.C;
import util.Util;
import util.WindowSize;

/**
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class TechPanel extends JPanel {

    private Gui gui;
    private Game game;
    private WindowSize ws;

    private JTable tech_table;

    private JButton exit;
    private static Object[] tech_table_header = {"Tech Name", "Cost", "# Labs", "Pts Left"};

    public TechPanel(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        game = gui.getGame();

        addTechTable();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        byte[][] pallette = gui.getPallette();
        String file = "PCX" + System.getProperty("file.separator") + "BG0.PCX";
        BufferedImage bi = Util.loadImage(file, ws.is_double, pallette, 640, 300);

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);
    }

    public void addTechTable() {
        tech_table = new JTable();
        tech_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tech_table_view = new JScrollPane(tech_table);
        tech_table.setFillsViewportHeight(true);
        tech_table.setBackground(Color.BLACK);
        tech_table_view.setPreferredSize(new Dimension(250, 80));
        this.add(tech_table_view);
        tech_table_view.setBounds(ws.tech_table_x_offset, ws.tech_table_y_offset,
                ws.tech_table_w, ws.tech_table_h);
//        ListSelectionModel list_selection_model;
//        list_selection_model = build_table.getSelectionModel();
//        list_selection_model.addListSelectionListener(
//                new ListSelectionListener() {
//                    public void valueChanged(ListSelectionEvent e) {
//                        buildSelected(e);
//                    }
//                });
        JTableHeader header = tech_table.getTableHeader();
        header.setFont(ws.font_default);
        header.setBackground(Color.black);
        header.setForeground(C.COLOR_GOLD);
        tech_table.setRowHeight(ws.city_table_row_height);
        tech_table.setDefaultRenderer(Object.class, new TechTableRenderer());
//        tech_table.setDefaultRenderer(Integer.class, new BuildPanel.BuildTableRenderer());

        tech_table.addMouseListener(new MouseAdapter() {
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

                    int[] tmp = (int[]) tech_table.getValueAt(row, 0);
                    int[] unit = {tmp[0], tmp[1]};

                    // if input unit was alone in selected stack
                    Point q = game.getSelectedPoint();
                    if (q != null) {
                        List<Unit> stack = game.getSelectedStack();
                        if (stack.isEmpty()) {
                            game.setSelectedPoint(null, -1);
                            game.setSelectedFaction(-1);
                            gui.setCurrentState(PW1.get());
                        }
                    }

                }
            }
        });
    }

    public void setTechData() {

        boolean[] owned_tech = game.getFaction(game.getTurn()).getResearch().techs;
        Tech[] techs = game.getResources().getTech();

        // find out research categories
        ArrayList<Integer> categories = new ArrayList<>();
        for (int j = 0; j < techs.length; j++) {
            if (techs[j].stats[C.TECH0] >= 900) {
                categories.add(new Integer(j));
            }
        }
        // create category lists
        ArrayList<List<Integer>> category_lists = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            category_lists.add(new LinkedList<Integer>());

        }

        // populate category lists
        for (int i = 0; i < techs.length; i++) {
            if (techs[i].stats[C.TECH0] >= 800) {
                continue;
            }
            if (!owned_tech[i] && owned_tech[techs[i].stats[C.TECH0]]
                    && owned_tech[techs[i].stats[C.TECH1]]
                    && owned_tech[techs[i].stats[C.TECH2]]) {
                category_lists.get(techs[i].stats[C.TECH_VOL]).add(new Integer(i));
            }
        }

        int nr_techs = categories.size() + getResearchableTechsNr(category_lists);

        Object[][] tech_table_data = new Object[nr_techs][];
        for (int i = 0; i < nr_techs; i++) {
            tech_table_data[i] = new Object[4];
        }

        int index = 0;
        for (int i = 0; i < categories.size(); i++) {
            Integer cat_value = categories.get(i);
            tech_table_data[index][0] = cat_value;
            List<Integer> category_list = category_lists.get(i);
            if (category_list.isEmpty()) {
                tech_table_data[index][1] = techs[cat_value.intValue()].stats[C.TECH_COST];
            } else {
                tech_table_data[index][1] = new Integer(-1);
            }
            tech_table_data[index][2] = new Integer(0);
            tech_table_data[index][3] = new Integer(0);
            index++;
            for (Integer integer : category_list) {
                tech_table_data[index][0] = integer;
                tech_table_data[index][1] = techs[integer.intValue()].stats[C.TECH_COST];

                tech_table_data[index][2] = new Integer(0);
                tech_table_data[index][3] = new Integer(0);
                index++;
            }

        }

        TechTableModel tech_model = new TechTableModel(tech_table_data,
                tech_table_header);
        tech_table.setModel(tech_model);
//        BuildPanel.BuildTableModel build_model = new BuildPanel.BuildTableModel(tech_table_data,
//                build_table_header);
//        build_table.setModel(build_model);
////        System.out.println("CellRend 0 " + build_table.getCellRenderer(0, 0));
////        System.out.println("CellRend 1 " + build_table.getCellRenderer(0, 1));
        TableColumn column = tech_table.getColumnModel().getColumn(0);
        column.setPreferredWidth(ws.tech_column_0_w);
        column = tech_table.getColumnModel().getColumn(1);
        column.setPreferredWidth(ws.tech_column_1_w);
        column = tech_table.getColumnModel().getColumn(2);
        column.setPreferredWidth(ws.tech_column_2_w);
        column = tech_table.getColumnModel().getColumn(3);
        column.setPreferredWidth(ws.tech_column_3_w);
//                column = tech_table.getColumnModel().getColumn(4);
//        column.setPreferredWidth(ws.tech_column_4_w);
    }

    public int getResearchableTechsNr(ArrayList<List<Integer>> category_lists) {
        int ret_val = 0;
        for (List<Integer> list : category_lists) {
            ret_val += list.size();
        }
        return ret_val;
    }

    class TechTableModel extends DefaultTableModel {

//        public BuildTableModel(Object[][] data) {
//            Object[] column_names = {"Unit", "Turns Left"};
//            BuildTableModel(data, column_names);
//        }
        public TechTableModel(Object[][] data, Object[] column_names) {
            super(data, column_names);
        }

        public boolean isCellEditable(int row, int col) {

            return false;

        }
    }

    public class TechTableRenderer extends JLabel
            implements TableCellRenderer {

        public TechTableRenderer() {
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
            System.out.println("column = " + column);
            String val = null;
            switch (column) {
                case 0:

                    val = game.getResources().getTech()[((Integer) value).intValue()].name;
                    setFont(ws.font_default);
                    setText(val);
                    break;
                case 1:

//                    val = "" + game.getResources().getTech()[((Integer) value).intValue()].stats[C.TECH_COST];
//                    GameResources gr = game.getResources();
//                    Tech[] techs = gr.getTech();
//                    Tech tech = techs[((Integer) value).intValue()];
                    val = "" + ((Integer) value).intValue();
                    setFont(ws.font_default);
                    setText(val);
                    break;
                case 2:

                    val = "" + 0; //game.getResources().getTech()[((Integer) value).intValue()].name;
                    setFont(ws.font_default);
                    setText(val);
                    break;
                case 3:
//                    int pts_left = game.getResources().getTech()[((Integer) value).intValue()].stats[C.TECH_COST];
//                    pts_left -= game.getFaction(game.getTurn()).getResearch().points[((Integer) value).intValue()];
                    val = "" + 0;
                    setFont(ws.font_default);
                    setText(val);
                    break;
                default:
                    throw new AssertionError();
            }

            return this;
        }
    }
}
