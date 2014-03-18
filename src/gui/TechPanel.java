/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import dat.Tech;
import galaxyreader.Structure;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
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
public class TechPanel extends JPanel {

    private Gui gui;
    private Game game;
    private WindowSize ws;

    private JTable tech_table;
    private JTextArea tech_info;
    private JTextField labs_cost;
    private JTextField lab_researches;
    private JTextField rp_available;

    // number of labs, set in setLabsCost()
    private int nr_labs;

    private JButton tech_db;
    private JButton exit;
    private JButton archive;
    private static Object[] tech_table_header = {"Tech Name", "Cost", "# Labs", "Pts Left"};

    public TechPanel(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        game = gui.getGame();

        addTechTable();
        addTechInfo();
        addLabsCost();
        addRPAvailable();
        addLabResearches();
        setUpArchiveButton();
        setUpTechDBButton();
        setUpExitButton();
        setUpButtonListener();
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setUpButtonListener() {
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
//                clickOnPlanetMap(e);
                Point p = e.getPoint();
                System.out.println("TechPanel (x,y): " + p.x + ", " + p.y);

            }
        });
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        byte[][] pallette = gui.getPallette();
        String file = "PCX" + System.getProperty("file.separator") + "BG0.PCX";
        BufferedImage bi = Util.loadImage(file, ws.is_double, pallette, 640, ws.tech_window_h);

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);

//        g.setColor(C.COLOR_GOLD);
//        g.drawRect(0, 0, ws.tech_window_w - 1, ws.tech_window_h - 1);
    }

    public void setUpTechDBButton() {
        tech_db = new JButton("Database");
        tech_db.setFont(ws.font_default);
        tech_db.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        this.add(tech_db);
        tech_db.setBounds(ws.tech_db_button_x_offset, ws.tech_db_button_y_offset,
                ws.tech_db_button_w, ws.tech_db_button_h);
        tech_db.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                gui.showTechDBWindow();
            }
        });
    }

    public void setUpExitButton() {
        exit = new JButton("Exit");
        exit.setFont(ws.font_default);
        exit.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        this.add(exit);
        exit.setBounds(ws.tech_exit_button_x_offset, ws.tech_exit_button_y_offset,
                ws.tech_exit_button_w, ws.tech_exit_button_h);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                gui.hideTechWindow();
            }
        });
    }

    public void setUpArchiveButton() {
        archive = new JButton("Archive");
        archive.setFont(ws.font_default);
        archive.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        this.add(archive);
        archive.setBounds(ws.tech_archive_button_x_offset, ws.tech_archive_button_y_offset,
                ws.tech_archive_button_w, ws.tech_archive_button_h);
        archive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = tech_table.getSelectedRow();
                if (row <= 0) {
                    return;
                }
                int tech_nr = ((Integer) tech_table.getValueAt(row, 0)).intValue();
                Tech tech = game.getGameResources().getTech()[tech_nr];
                gui.showManowitz(tech.stats[C.TECH_VOL], tech.stats[C.TECH_CH]);
            }
        });
    }

    public void addTechInfo() {
        tech_info = new JTextArea();
        tech_info.setFont(ws.font_default);
        tech_info.setEditable(false);
        JScrollPane tech_info_scroller = new JScrollPane(tech_info);
        this.add(tech_info_scroller);
        tech_info_scroller.setBounds(ws.tech_info_x_offset, ws.tech_info_y_offset,
                ws.tech_info_w, ws.tech_info_h);
        tech_info.setBounds(ws.tech_info_x_offset, ws.tech_info_y_offset,
                ws.tech_info_w, ws.tech_info_h);
        tech_info.setLineWrap(true);
        tech_info.setWrapStyleWord(true);
    }

    public void setLabsCost() {
        List<Structure> cities = game.getStructures();
        int cost = 0;
        int nr_labs = 0;
        for (Structure city : cities) {
            if (city.owner == game.getTurn() && city.type == C.LAB) {
                cost += game.getEfs_ini().lab_cost;
                nr_labs++;
            }
        }
        this.nr_labs = nr_labs;
        String lab_text = "";
        if (nr_labs == 1) {
            lab_text = " lab requires ";
        } else {
            lab_text = " labs require ";
        }
        labs_cost.setText("" + nr_labs + lab_text + cost + " Firebirds per turn.");
    }

    public void addLabsCost() {
        labs_cost = new JTextField();
        labs_cost.setFont(ws.font_default);
        labs_cost.setForeground(C.COLOR_GOLD);
        labs_cost.setOpaque(false);
        labs_cost.setEditable(false);
        labs_cost.setBounds(ws.tech_labs_cost_x_offset, ws.tech_labs_cost_y_offset,
                ws.tech_labs_cost_w, ws.tech_labs_cost_h);
        this.add(labs_cost);

    }

    public void setLabResearches() {
        lab_researches.setText("A lab researches "
                + game.getEfs_ini().lab_points
                + " points/turn.");
    }

    public void addLabResearches() {
//        JTextField lab_researches;
        lab_researches = new JTextField();
        lab_researches.setFont(ws.font_default);
        lab_researches.setForeground(C.COLOR_GOLD);
        lab_researches.setOpaque(false);
        lab_researches.setEditable(false);
        lab_researches.setBounds(ws.tech_labs_cost_x_offset, ws.tech_labs_cost_y_offset + ws.tech_labs_cost_h,
                ws.tech_labs_cost_w, ws.tech_labs_cost_h);
        this.add(lab_researches);
    }

    public void setRPAvailable() {
        rp_available.setText("You have "
                + game.getFaction(game.getTurn()).getResearch().points_left
                + " research points available.");
    }

    public void addRPAvailable() {
//        JTextField rp_available;
        rp_available = new JTextField();
        rp_available.setFont(ws.font_default);
        rp_available.setForeground(C.COLOR_GOLD);
        rp_available.setOpaque(false);
        rp_available.setEditable(false);
        rp_available.setBounds(ws.tech_labs_cost_x_offset, ws.tech_labs_cost_y_offset + 2 * ws.tech_labs_cost_h,
                ws.tech_labs_cost_w, ws.tech_labs_cost_h);
        this.add(rp_available);

    }

    public static void setInfoText(JTable table, int row, JTextArea info, Game game) {
        int tech_nr = ((Integer) table.getValueAt(row, 0)).intValue();
        if (tech_nr == 0) {
            info.setText("");
            return;
        }
        Tech[] techs = game.getGameResources().getTech();
        String info_text = techs[tech_nr].extra;

        for (Tech tech : techs) {
            if (tech.stats[C.TECH0] == 800) {
                continue;
            }
            if (tech.stats[C.TECH0] == tech_nr
                    || tech.stats[C.TECH1] == tech_nr
                    || tech.stats[C.TECH2] == tech_nr) {
                if (!info_text.equals("")) {
                    info_text += "\n";
                }
                info_text += " Allows " + tech.name;
                boolean with = false;
                if (tech.stats[C.TECH0] != tech_nr
                        && tech.stats[C.TECH0] != 0) {
                    with = true;
                    info_text += " with " + techs[tech.stats[C.TECH0]].name;
                }
                if (tech.stats[C.TECH1] != tech_nr
                        && tech.stats[C.TECH1] != 0) {
                    if (with != true) {
                        with = true;
                        info_text += " with ";
                    } else {
                        info_text += " and ";
                    }
                    info_text += techs[tech.stats[C.TECH1]].name;
                }
                if (tech.stats[C.TECH2] != tech_nr
                        && tech.stats[C.TECH2] != 0) {
                    if (with != true) {
                        with = true;
                        info_text += " with ";
                    } else {
                        info_text += " and ";
                    }
                    info_text += techs[tech.stats[C.TECH2]].name;
                }

            }
        }

        info.setText(info_text);
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
                    // update info text
                    System.out.println("Single clicked row " + row);
                    TechPanel.setInfoText(table, row, tech_info, game);

                }
                if (e.getClickCount() == 2) {

                    // if category allready reseached && != "nothing"
                    int cost = ((Integer) tech_table.getValueAt(row, 1)).intValue();
                    if (cost == -1 && row != 0) {
                        return;
                    }

                    // set researched technology && do research
                    int tech_no = ((Integer) tech_table.getValueAt(row, 0)).intValue();
                    game.setResearch(tech_no);
                    game.getFaction(game.getTurn()).getResearch().doResearch();
                    setTechData();
                    setRPAvailable();
                    if (row != 0 && game.getFaction(game.getTurn()).getResearch().techs[tech_no]) {
                        tech_table.setRowSelectionInterval(0, 0);
//                        gui.showInfoWindow("Research on "
//                                + game.getResources().getTech()[tech_no].name
//                                + "\n has been completed!");
                    } else {
                        tech_table.setRowSelectionInterval(row, row);
                        TechPanel.setInfoText(table, row, tech_info, game);
                    }
                }
            }
        });
    }

    public void setTechData() {
        tech_info.setText("");
        Research research = game.getFaction(game.getTurn()).getResearch();
        boolean[] owned_tech = research.techs;
        int researched = research.researched;
        Tech[] techs = game.getGameResources().getTech();

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

        /* populate category lists take into account eg. Nova mod where new
         * technologies have VOL == 0 and CH == 0
         */
        int cat_nr = 0;
        for (int i = 0; i < techs.length; i++) {
            if (techs[i].stats[C.TECH0] >= 990) {
                cat_nr = techs[i].stats[C.TECH0] - 989;
            }
            if (techs[i].stats[C.TECH0] >= 800) {
                continue;
            }
            if (!owned_tech[i] && owned_tech[techs[i].stats[C.TECH0]]
                    && owned_tech[techs[i].stats[C.TECH1]]
                    && owned_tech[techs[i].stats[C.TECH2]]) {
//                category_lists.get(techs[i].stats[C.TECH_VOL] - 1).add(new Integer(i));
                category_lists.get(cat_nr).add(new Integer(i));
            }
        }

        // create tech table
        int nr_techs = categories.size() + getResearchableTechsNr(category_lists);
        Object[][] tech_table_data = new Object[nr_techs][];
        for (int i = 0; i < nr_techs; i++) {
            tech_table_data[i] = new Object[4];
        }

        // populate tech table
        int index = 0;
        for (int i = 0; i < categories.size(); i++) {
            Integer cat_value = categories.get(i);
            tech_table_data[index][0] = cat_value;
            List<Integer> category_list = category_lists.get(i);
            if (owned_tech[cat_value.intValue()] || techs[cat_value.intValue()].stats[C.TECH_COST] == 0) {
                tech_table_data[index][1] = new Integer(-1);
                tech_table_data[index][2] = new Integer(-1);
                tech_table_data[index][3] = new Integer(-1);
            } else {
                tech_table_data[index][1] = new Integer(techs[cat_value.intValue()].stats[C.TECH_COST]);
                if (cat_value.intValue() == researched) {
                    tech_table_data[index][2] = new Integer(nr_labs);
                } else {
                    tech_table_data[index][2] = new Integer(0);
                }
                tech_table_data[index][3] = new Integer(techs[cat_value.intValue()].stats[C.TECH_COST] - research.points[cat_value.intValue()]);
            }

            index++;
            for (Integer integer : category_list) {
                tech_table_data[index][0] = integer;
                tech_table_data[index][1] = new Integer(techs[integer.intValue()].stats[C.TECH_COST]);

                if (integer.intValue() == researched) {
                    tech_table_data[index][2] = new Integer(nr_labs);
                } else {
                    tech_table_data[index][2] = new Integer(0);
                }
                tech_table_data[index][3] = new Integer(techs[integer.intValue()].stats[C.TECH_COST] - research.points[integer.intValue()]);
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
            if (row != 0 && ((Integer) table.getValueAt(row, 1)).intValue() == -1) {
                c_f = Color.BLUE;
            }
            if (game.getFaction(game.getTurn()).getResearch().researched
                    == ((Integer) table.getValueAt(row, 0)).intValue()) {
                c_f = Color.GREEN;
            }
            if (isSelected) {
                setBackground(c_f);
                setForeground(c_b);
            } else {
                setBackground(c_b);
                setForeground(c_f);
            }
//            System.out.println("column = " + column);
            String val = "";
            int i_val = ((Integer) value).intValue();
            switch (column) {
                case 0:
                    if (game.getGameResources().getTech()[((Integer) value).intValue()].stats[C.TECH0] < 800) {
                        val = "    ";
                    }
                    val += game.getGameResources().getTech()[((Integer) value).intValue()].name;
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
                case 2:
                    if (i_val == -1) {
                        val = "";
                    } else {
                        val = "" + i_val; //game.getResources().getTech()[((Integer) value).intValue()].name;
                    }
                    setFont(ws.font_default);
                    setText(val);
                    break;
                case 3:
//                    int pts_left = game.getResources().getTech()[((Integer) value).intValue()].stats[C.TECH_COST];
//                    pts_left -= game.getFaction(game.getTurn()).getResearch().points[((Integer) value).intValue()];
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
