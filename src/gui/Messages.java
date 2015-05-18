/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import galaxyreader.Planet;
import galaxyreader.Structure;
import galaxyreader.Unit;
import game.CombatReport;
import game.Game;
import game.Message;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
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
import state.SU;
import util.C;
import util.FN;
import util.Util;
import util.WindowSize;

/**
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class Messages extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Gui gui;
    private WindowSize ws;
    private Game game;
    private JTable message_table;
    private JButton exit;
    private JButton view_replays;
    private static Object[] message_table_header = {"Message", "Location"};
    private CombatReport current_cmbt_report;

    public Messages(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        game = gui.getGame();
        setUpButtons();
        addMessageTable();
    }

    public void setUpButtons() {
        exit = new JButton("Exit");
        exit.setFont(ws.font_default);
        exit.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        this.add(exit);
        exit.setBounds(ws.fw_eb_x, ws.fw_eb_y,
                ws.fw_eb_w, ws.fw_eb_h);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.getCurrentState().pressExitButton();
            }
        });
        view_replays = new JButton("View Combat Replays");
        view_replays.setFont(ws.font_default);
        view_replays.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        this.add(view_replays);
        view_replays.setBounds(ws.mw_vr_x, ws.fw_eb_y - 5 - ws.fw_eb_h,
                ws.mw_vr_w, ws.fw_eb_h);
        view_replays.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int next_replay = findNextReplay(-1);
                if (next_replay > -1) {
                    setReplay(next_replay);
                    SU.showCombatReplay();
                }
            }
        });

    }

    public JTable getMessageTable() {
        return message_table;
    }

    public CombatReport getCombatReport() {
        return current_cmbt_report;
    }

    public void setCombatReport(CombatReport cr) {
        current_cmbt_report = cr;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        renderMessages(g);

    }

    public void renderMessages(Graphics g) {
        byte[][] pallette = gui.getPallette();
        BufferedImage bi = Util.loadImage(FN.S_BG0_PCX, ws.is_double, pallette, 640, 480);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);
    }

    /**
     * Return row of next combat replay or -1 of no more replays. If row > -2
     * start search form row + 1.
     *
     * @return
     */
    public int findNextReplay(int row) {
        if (row == -2) {
            row = message_table.getSelectedRow();
        }
        int next_replay = -1;
        for (int i = row + 1; i < message_table.getRowCount(); i++) {
            C.Msg message = (C.Msg) message_table.getValueAt(i, 0);
            if (message == C.Msg.COMBAT_REPORT) {
                next_replay = i;
                break;
            }

        }
        return next_replay;
    }

    public void setReplay(int row) {
        Message msg = (Message) message_table.getValueAt(row, 1);
        msg.setRead(true);
        CombatReport report = (CombatReport) msg.getSource();
        game.getBattle().setCombatStacks(report.attacker, report.defender);
        System.out.println("combat stacks set");
        current_cmbt_report = report;
        message_table.setRowSelectionInterval(row, row);
    }

    public void addMessageTable() {
        message_table = new JTable();
        message_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane message_table_view = new JScrollPane(message_table);
        message_table.setFillsViewportHeight(true);
        message_table.setBackground(Color.BLACK);
        message_table_view.setPreferredSize(new Dimension(250, 80));
        this.add(message_table_view);
        message_table_view.setBounds(ws.mwmt_x, ws.mwmt_y,
                ws.mwmt_w, ws.mwmt_h);
        JTableHeader header = message_table.getTableHeader();
        header.setFont(ws.font_default);
        header.setBackground(Color.black);
        header.setForeground(C.COLOR_GOLD);
        message_table.setRowHeight(ws.city_table_row_height);
        message_table.setDefaultRenderer(Object.class, new MessageTableRenderer());
//        queue_table.setDefaultRenderer(Integer.class, new QueueTableRenderer());
        message_table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                gui.getCurrentState().clickOnWindow(e);
            }
        });
    }

    public void addMessageData() {
        List<Message> messages = game.getFaction(game.getTurn()).getMessages();
        int nr_msgs = messages.size();
        if (nr_msgs == 0) {
            if (message_table.getRowCount() != 0) {
                ((Messages.MessageTableModel) message_table.getModel()).setRowCount(nr_msgs);
            }
            return;
        }

        Object[][] message_table_data = new Object[nr_msgs][];

        int idx = 0;
        for (Message message : messages) {

            message_table_data[idx] = new Object[2];

            message_table_data[idx][0] = message.getType();
            message_table_data[idx][1] = message;
            idx++;
        }

        Messages.MessageTableModel messages_model = new Messages.MessageTableModel(message_table_data,
                message_table_header);
        message_table.setModel(messages_model);

//        TableColumn column = queue_table.getColumnModel().getColumn(0);
//        column.setPreferredWidth(ws.queue_table_cell_0_width);
//        column = queue_table.getColumnModel().getColumn(1);
//        column.setPreferredWidth(ws.queue_table_cell_1_width);
    }

    class MessageTableModel extends DefaultTableModel {

/**
         * 
         */
        private static final long serialVersionUID = 1L;

        //        public BuildTableModel(Object[][] data) {
//            Object[] column_names = {"Unit", "Turns Left"};
//            BuildTableModel(data, column_names);
//        }
        public MessageTableModel(Object[][] data, Object[] column_names) {
            super(data, column_names);
        }

        public boolean isCellEditable(int row, int col) {

            return false;

        }
    }

    public class MessageTableRenderer extends JLabel
            implements TableCellRenderer {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public MessageTableRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            Color c_b = Color.BLACK;
            Color c_f = C.COLOR_GOLD;
            Message ms = (Message) table.getValueAt(row, 1);
            if (ms.isRead()) {
                c_f = Color.LIGHT_GRAY;
            }
            String val = "";
            if (value instanceof Message) {
                Message message = (Message) value;
                Object source = message.getSource();
                if (source instanceof Planet) {

                    val = ((Planet) source).name;
                } else if (source instanceof Structure) {
                    Structure city = (Structure) source;
                    val = game.getPlanet(city.p_idx).name + "(" + city.x + ","
                            + city.y + ")";
                } else if (source instanceof CombatReport) {
                    CombatReport report = (CombatReport) source;
                    Unit unit = report.defender.get(0);
                    val = game.getPlanet(unit.p_idx).name;
                    if (!unit.in_space) {
                        val += " (" + unit.x + ","
                                + unit.y + ")";
                    }
                }
            } else {
                C.Msg msg = (C.Msg) value;
                switch (msg) {
                    case COMBAT_REPORT:
                        val = "Combat report";
                        break;
                    case CANNOT_PRODUCE:
                        val = "Cannot produce";
                        break;
                    case CITY_FULL:
                        val = "City full";
                        break;
                    case FAMINE:
                        val = "Famine";
                        break;
                    default:
                        throw new AssertionError();
                }
            }
            if (isSelected) {
                setBackground(c_f);
                setForeground(c_b);
            } else {
                setBackground(c_b);
                setForeground(c_f);
            }
            setFont(ws.font_default);
            setText(val);

            return this;
        }
    }
}
