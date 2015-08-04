/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import game.Game;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import util.C;
import util.WindowSize;

/**
 * Notice: unused class ?
 *
 * @author joulupunikki
 */
public class StackWindow extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JPanel stack_panel;
    private WindowSize ws;
//    private JPanel[][] units;
    private Game game;
    private Gui gui;

    private JButton exit_button;

    public StackWindow(Gui gui) {
        super(gui, true);

        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                ;
            }
        });
        this.gui = gui;
        ws = gui.getWindowSize();
        game = gui.getGame();
//        units = new JPanel[C.STACK_WINDOW_UNITS_X][C.STACK_WINDOW_UNITS_Y];

        this.setSize(ws.main_window_width, ws.main_window_height);
//        this.setPreferredSize(new Dimension(ws.main_window_width, ws.main_window_height));
        stack_panel = new UnitInfoWindow(gui);
        stack_panel.setPreferredSize(new Dimension(ws.main_window_width,
                ws.main_window_height));
        stack_panel.setLayout(null);
        this.add(stack_panel);
        stack_panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            }
        });

        exit_button = new JButton("Exit");
        stack_panel.add(exit_button);
        Dimension size = exit_button.getSize();
        exit_button.setBackground(Color.BLACK);
        exit_button.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        exit_button.setBounds(ws.stack_window_exit_button_x, ws.stack_window_exit_button_y, 60, 25);
        exit_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                disposeWindow();
            }
        });
//                size.width, size.height);
//        UnitPanel.setReferences(gui, game);
//        for (int i = 0; i < C.STACK_WINDOW_UNITS_X; i++) {
//            for (int j = 0; j < C.STACK_WINDOW_UNITS_Y; j++) {
//                units[i][j] = new UnitPanel();
//                units[i][j].setBounds((int)(ws.unit_panel_x_offset + i * 3.5 * ws.unit_icon_size),
//                        (int) (ws.unit_panel_y_offset + j * 1.15 * ws.unit_icon_size), 
//                        3 * ws.unit_icon_size, 
//                        ws.unit_icon_size);
////                units[i][j].setPreferredSize(Dimension(3 * ws.unit_icon_size, ws.unit_icon_size));
//                stack_panel.add(units[i][j]);
//            }
//            
//        }

        this.pack();
        this.setVisible(true);
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void disposeWindow() {
        this.dispose();
    }

}
