/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import galaxyreader.Unit;
import game.Game;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.BorderUIResource;
import util.C;
import util.WindowSize;

/**
 * Handles transferring (splitting and joining of pods by players) of cargo.
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class CargoPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Gui gui;
    private Game game;
    private WindowSize ws;

    private JLabel title;
    private JTextField amount;
    private JButton all_button;
    private JButton cancel_button;
    private JButton ok_button;
    private JSlider slider;

    private Unit u_from;
    private Unit u_to;
    private List<Unit> stack;

    public CargoPanel(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        game = gui.getGame();

        this.setLayout(null);

        setUpTitle();
        setUpAmount();
        setUpAll();
        setUpCancel();
        setUpOK();
        setUpSlider();

    }

    private void setUpTitle() {
        title = new JLabel("Transfer Cargo");
        title.setBounds(ws.cp_t_x, ws.cp_t_y, ws.cp_t_w, ws.cp_t_h);
        this.add(title);
    }

    private void setUpAmount() {
        amount = new JTextField("0");
        amount.setBounds(ws.cp_t_x, ws.cp_t_y + ws.cp_b_y_gap, ws.cp_b_w, ws.cp_t_h);
        amount.setBorder(new BorderUIResource(new LineBorder(C.COLOR_GOLD)));
        add(amount);
    }

    private void setUpAll() {
        all_button = new JButton("All");
        all_button.setBounds(ws.cp_t_x, ws.cp_t_y + 2 * ws.cp_b_y_gap, ws.cp_b_w, ws.cp_t_h);
        all_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transferCargo(u_from.amount);
            }
        });
        add(all_button);
    }

    private void setUpCancel() {
        cancel_button = new JButton("Cancel");
        cancel_button.setBounds(ws.cp_t_x, ws.cp_t_y + 3 * ws.cp_b_y_gap, ws.cp_b_w, ws.cp_t_h);
        cancel_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setNull();
                gui.showCargoWin(false);
            }
        });
        add(cancel_button);

    }

    private void setUpOK() {
        ok_button = new JButton("O.K.");
        ok_button.setBounds(ws.cp_t_x, ws.cp_t_y + 4 * ws.cp_b_y_gap, ws.cp_b_w, ws.cp_t_h);
        ok_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transferCargo(0);
            }
        });
        add(ok_button);
    }

    private void setUpSlider() {
        slider = new JSlider(JSlider.VERTICAL);
        slider.setMinimum(0);
        slider.setBounds(ws.cp_s_x, ws.cp_t_y + ws.cp_b_y_gap, ws.cp_s_w, ws.cp_s_h);
        slider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
//                if (!source.getValueIsAdjusting()) {
                int value = (int) source.getValue();
                amount.setText("" + value);
//                }
            }
        });
        add(slider);

    }

    private void transferCargo(int t_amount) {
        //read transfer amount
        int transfer_amount = t_amount;
        if (transfer_amount == 0) {
            try {
                transfer_amount = Integer.parseInt(amount.getText());
            } catch (NumberFormatException nfe) {
                amount.setText("0");
                return;
            }
        }
        if (transfer_amount <= 0) {
            return;
        }
        // set to amount
        int to_amount = 0;
        if (u_to != null) {
            to_amount = u_to.amount;
        }

        int from_amount = u_from.amount;
        transfer_amount = Math.max(transfer_amount, 0);
        transfer_amount = Math.min(transfer_amount, from_amount);
        transfer_amount = Math.min(transfer_amount, C.MAX_CARGO - to_amount);

        if (u_to == null) {
            u_to = game.createUnitInHex(u_from.p_idx, u_from.x, u_from.y,
                    u_from.owner, u_from.type, u_from.t_lvl,
                    u_from.res_relic, 0);
            if (u_to == null) {
                setNull();
                gui.showCargoWin(false);
                return;
            }
        } else {
//                    u_to.amount = u_to.amount + transfer_amount;
        }
//                u_from.amount = u_from.amount - transfer_amount;
        game.getResources().adjustPodResources(u_to, transfer_amount);
        game.getResources().adjustPodResources(u_from, -transfer_amount);

        if (u_from.amount == 0) {
            game.deleteUnitNotInCombat(u_from);
        }

        setNull();
        gui.showCargoWin(false);
    }

    public void setNull() {
        u_from = null;
        u_to = null;
        stack = null;
        amount.setText("0");
    }

    public void init(Unit u_from, Unit u_to, List<Unit> stack) {
        this.u_from = u_from;
        this.u_to = u_to;
        this.stack = stack;
        slider.setMaximum(u_from.amount);
        slider.setValue(0);
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public static CargoPanel.Win getCargoWin(Gui gui) {

        WindowSize ws = Gui.getWindowSize();
        CargoPanel cargo_panel = new CargoPanel(gui);
        final CargoPanel.Win cargo_win = new CargoPanel.Win(gui, cargo_panel);
        cargo_win.setDefaultCloseOperation(
                JDialog.DO_NOTHING_ON_CLOSE);
        cargo_win.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {

                cargo_win.setVisible(false);
            }
        });

        cargo_panel.setLayout(null);
        cargo_win.add(cargo_panel);
        cargo_win.setBounds(ws.cpw_x, ws.cpw_y,
                ws.cpw_w, ws.cpw_h);
        cargo_win.pack();
        return cargo_win;
    }

    public static class Win extends JDialog {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        CargoPanel cargo_panel;

        public Win(Gui gui, CargoPanel cargo_panel) {
            super(gui, true);
            this.cargo_panel = cargo_panel;
        }

        public void setGame(Game game) {
            cargo_panel.setGame(game);
        }

        public void init(Unit u_from, Unit u_to, List<Unit> stack) {
            cargo_panel.init(u_from, u_to, stack);
        }
    }
}
