/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import dat.Tech;
import galaxyreader.Unit;
import game.Game;
import game.Research;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
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
public class TechDBPanel extends JPanel {

    private Gui gui;
    private Game game;
    private WindowSize ws;

    private JTable tech_db_table;

    private JButton exit;
    private static Object[] tech_db_table_header = {"Tech Name", "Maint"};

    public TechDBPanel(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        game = gui.getGame();

        addTechTable();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        byte[][] pallette = gui.getPallette();
        String file = "PCX" + System.getProperty("file.separator") + "BG0.PCX";
        BufferedImage bi = Util.loadImage(file, ws.is_double, pallette, 640, ws.tech_window_h);

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);
    }

    public void setUpButtons() {
        exit = new JButton("Exit");
        exit.setFont(ws.font_default);
        exit.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        this.add(exit);
        exit.setBounds(ws.build_exit_button_x_offset, ws.build_exit_button_y_offset,
                ws.build_exit_button_w, ws.build_exit_button_h);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                gui.getBuildWindow().setVisible(false);
            }
        });
    }

    public void addTechTable() {
        tech_db_table = new JTable();
        tech_db_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tech_db_table_view = new JScrollPane(tech_db_table);
        tech_db_table.setFillsViewportHeight(true);
        tech_db_table.setBackground(Color.BLACK);
        tech_db_table_view.setPreferredSize(new Dimension(250, 80));
        this.add(tech_db_table_view);
        tech_db_table_view.setBounds(ws.tech_table_x_offset, ws.tech_table_y_offset,
                ws.tech_table_w, ws.tech_table_h);
//        ListSelectionModel list_selection_model;
//        list_selection_model = build_table.getSelectionModel();
//        list_selection_model.addListSelectionListener(
//                new ListSelectionListener() {
//                    public void valueChanged(ListSelectionEvent e) {
//                        buildSelected(e);
//                    }
//                });
        JTableHeader header = tech_db_table.getTableHeader();
        header.setFont(ws.font_default);
        header.setBackground(Color.black);
        header.setForeground(C.COLOR_GOLD);
        tech_db_table.setRowHeight(ws.city_table_row_height);
        tech_db_table.setDefaultRenderer(Object.class, new TechTableRenderer());
//        tech_table.setDefaultRenderer(Integer.class, new BuildPanel.BuildTableRenderer());

        tech_db_table.addMouseListener(new MouseAdapter() {
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

                    // if category allready reseached && != "nothing"
                    int cost = ((Integer) tech_db_table.getValueAt(row, 1)).intValue();
                    if (cost == -1 && row != 0) {
                        return;
                    }

                    // set researched technology && do research
                    int tech_no = ((Integer) tech_db_table.getValueAt(row, 0)).intValue();
                    game.setResearch(tech_no);
                    game.getFaction(game.getTurn()).doResearch();
                    setTechData();
                    if (game.getFaction(game.getTurn()).getResearch().techs[tech_no]) {

                        gui.showInfoWindow("Research on "
                                + game.getResources().getTech()[tech_no].name
                                + "\n has been completed!");
                    }

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

        Research research = game.getFaction(game.getTurn()).getResearch();
        boolean[] owned_tech = research.techs;
        Tech[] techs = game.getResources().getTech();

        // find out research categories
        ArrayList<Integer> categories = new ArrayList<>();
        for (int j = 0; j < techs.length; j++) {
            if (techs[j].stats[C.TECH0] >= 900) {
                int i_cat;
                if (techs[j].stats[C.TECH0] == 900) {
                    i_cat = 0;
                } else {
                    i_cat = techs[j].stats[C.TECH0] - 989;
                }
                categories.add(i_cat, new Integer(j));
                System.out.println("vol " + (techs[j].stats[C.TECH_VOL] - 1));
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
            if (owned_tech[i]) {
                category_lists.get(techs[i].stats[C.TECH_VOL] - 1).add(new Integer(i));
            }
        }

        // create tech table
        int nr_techs = categories.size() + getResearchableTechsNr(category_lists) - 1;
        Object[][] tech_db_table_data = new Object[nr_techs][];
        for (int i = 0; i < nr_techs; i++) {
            tech_db_table_data[i] = new Object[2];
        }

        // populate tech table
        int index = 0;
        for (int i = 0; i < categories.size(); i++) {
            if (i == 0) {
                continue;
            }
            Integer cat_value = categories.get(i);
            tech_db_table_data[index][0] = cat_value;
            List<Integer> category_list = category_lists.get(i);
            if (!owned_tech[cat_value.intValue()] || techs[cat_value.intValue()].stats[C.TECH_COST] == 0) {
                tech_db_table_data[index][1] = new Integer(-1);
            } else {
                tech_db_table_data[index][1] = new Integer(techs[cat_value.intValue()].stats[C.TECH_COST] / C.TECH_MAINT);
            }

            index++;
            for (Integer integer : category_list) {
                tech_db_table_data[index][0] = integer;
                tech_db_table_data[index][1] = new Integer(techs[integer.intValue()].stats[C.TECH_COST] / C.TECH_MAINT);
                index++;
            }

        }

        TechTableModel tech_model = new TechTableModel(tech_db_table_data,
                tech_db_table_header);
        tech_db_table.setModel(tech_model);
//        BuildPanel.BuildTableModel build_model = new BuildPanel.BuildTableModel(tech_table_data,
//                build_table_header);
//        build_table.setModel(build_model);
////        System.out.println("CellRend 0 " + build_table.getCellRenderer(0, 0));
////        System.out.println("CellRend 1 " + build_table.getCellRenderer(0, 1));
//        TableColumn column = tech_db_table.getColumnModel().getColumn(0);
//        column.setPreferredWidth(ws.tech_column_0_w);
//        column = tech_db_table.getColumnModel().getColumn(1);
//        column.setPreferredWidth(ws.tech_column_1_w);
//        column = tech_db_table.getColumnModel().getColumn(2);
//        column.setPreferredWidth(ws.tech_column_2_w);
//        column = tech_db_table.getColumnModel().getColumn(3);
//        column.setPreferredWidth(ws.tech_column_3_w);
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
            if (((Integer) table.getValueAt(row, 1)).intValue() == -1) {
                c_f = Color.BLUE;
            }

            if (isSelected) {
                setBackground(c_f);
                setForeground(c_b);
            } else {
                setBackground(c_b);
                setForeground(c_f);
            }
            System.out.println("column = " + column);
            String val = "";
            int i_val = ((Integer) value).intValue();
            switch (column) {
                case 0:
                    if (game.getResources().getTech()[((Integer) value).intValue()].stats[C.TECH0] < 800) {
                        val = "    ";
                    }
                    val += game.getResources().getTech()[((Integer) value).intValue()].name;
                    setFont(ws.font_default);
                    setText(val);
                    break;
                case 1:

//                    val = "" + game.getResources().getTech()[((Integer) value).intValue()].stats[C.TECH_COST];
//                    GameResources gr = game.getResources();
//                    Tech[] techs = gr.getTech();
//                    Tech tech = techs[((Integer) value).intValue()];
                    if (i_val == -1) {
                        val = "";
                    } else {
                        val = "" + i_val;
                    }
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
