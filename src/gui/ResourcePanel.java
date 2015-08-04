/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import game.Game;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import util.C;
import util.FN;
import util.Util;
import util.WindowSize;

/**
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class ResourcePanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Gui gui;
    private Game game;
    private WindowSize ws;
    private BufferedImage bi;
    private JTextField planet_cons;
    private JTextField galaxy_cons;
    private JTextField planet_prod;
    private JTextField galaxy_prod;
    private JTextField res_name;
    private JButton exit;

    public ResourcePanel(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        game = gui.getGame();

        byte[][] pallette = gui.getPallette();
        String file = FN.S_BG0_PCX;
        BufferedImage tmp = Util.loadImage(file, ws.is_double, pallette, 640, 480);
        bi = tmp.getSubimage(0, 0, ws.rw_width, ws.rw_height);

        setUpExitButton();
        setUpGalaxyProd();
        setUpPlanetProd();
        setUpGalaxyCons();
        setUpPlanetCons();
        setUpResName();
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

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);

//        g.setColor(C.COLOR_GOLD);
//        g.drawRect(0, 0, ws.tech_window_w - 1, ws.tech_window_h - 1);
    }

    public void setUpExitButton() {
        exit = new JButton("Exit");
        exit.setFont(ws.font_default);
        exit.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        this.add(exit);
        exit.setBounds(ws.rw_exit_x_offset, ws.rw_exit_y_offset,
                ws.rw_exit_width, ws.rw_exit_height);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                gui.hideResourceWindow();
            }
        });
    }

    public void setUpGalaxyProd() {
//        JTextField galaxy_prod;
        galaxy_prod = new JTextField();

        this.add(galaxy_prod);
        galaxy_prod.setBounds(ws.rw_pct_x_offset, ws.rw_pct_y_offset,
                ws.rw_pct_width, ws.rw_pct_height);
        galaxy_prod.setOpaque(false);
        galaxy_prod.setForeground(C.COLOR_GOLD);
        galaxy_prod.setEditable(false);
        galaxy_prod.setHorizontalAlignment(JTextField.LEFT);
        galaxy_prod.setBorder(null);
        galaxy_prod.setFont(ws.font_default);
    }

    public void setUpPlanetProd() {
//        JTextField planet_prod;
        planet_prod = new JTextField();

        this.add(planet_prod);
        planet_prod.setBounds(ws.rw_pct_x_offset, ws.rw_pct_y_offset - ws.rw_pct_height,
                ws.rw_pct_width, ws.rw_pct_height);
        planet_prod.setOpaque(false);
        planet_prod.setForeground(C.COLOR_GOLD);
        planet_prod.setEditable(false);
        planet_prod.setHorizontalAlignment(JTextField.LEFT);
        planet_prod.setBorder(null);
        planet_prod.setFont(ws.font_default);
    }

    public void setUpGalaxyCons() {
//        JTextField galaxy_cons;
        galaxy_cons = new JTextField();

        this.add(galaxy_cons);
        galaxy_cons.setBounds(ws.rw_pct_x_offset, ws.rw_pct_y_offset - 2 * ws.rw_pct_height,
                ws.rw_pct_width, ws.rw_pct_height);
        galaxy_cons.setOpaque(false);
        galaxy_cons.setForeground(C.COLOR_GOLD);
        galaxy_cons.setEditable(false);
        galaxy_cons.setHorizontalAlignment(JTextField.LEFT);
        galaxy_cons.setBorder(null);
        galaxy_cons.setFont(ws.font_default);
    }

    public void setUpPlanetCons() {
//        JTextField planet_cons;
        planet_cons = new JTextField();

        this.add(planet_cons);
        planet_cons.setBounds(ws.rw_pct_x_offset, ws.rw_pct_y_offset - 3 * ws.rw_pct_height,
                ws.rw_pct_width, ws.rw_pct_height);
//        planet_cons.setBackground(Color.BLACK);
        planet_cons.setOpaque(false);
        planet_cons.setForeground(C.COLOR_GOLD);
        planet_cons.setEditable(false);
        planet_cons.setHorizontalAlignment(JTextField.LEFT);
        planet_cons.setBorder(null);
        planet_cons.setFont(ws.font_default);
    }

    public void setUpResName() {
//        JTextField res_name;
        res_name = new JTextField();

        this.add(res_name);
        res_name.setBounds(ws.rw_rn_x, ws.rw_rn_y,
                ws.rw_rn_w, ws.rw_rn_h);
//        planet_cons.setBackground(Color.BLACK);
        res_name.setOpaque(false);
        res_name.setForeground(C.COLOR_GOLD);
        res_name.setEditable(false);
        res_name.setHorizontalAlignment(JTextField.LEFT);
        res_name.setBorder(null);
        res_name.setFont(ws.font_default);
    }

    public void setText(int resource) {
        String producing = "Producing ";
        String planet = " points on this planet.";
        String consuming = "Consuming ";
        String galaxy = " points galaxywide.";
        int f_idx = game.getTurn();
        int p_idx = game.getCurrentPlanetNr();
        int[][][][] pc = game.getResources().getProdCons();
        int p_prod = pc[C.PROD][f_idx][p_idx][resource];

        int p_cons = pc[C.CONS][f_idx][p_idx][resource];
        int g_cons = 0;
        for (int i = 0; i < pc[C.CONS][f_idx].length; i++) {
            g_cons += pc[C.CONS][f_idx][i][resource];
        }
        int g_prod = 0;
        for (int i = 0; i < pc[C.PROD][f_idx].length; i++) {
            g_prod += pc[C.PROD][f_idx][i][resource];
        }
        planet_cons.setText(consuming + p_cons + planet);
        galaxy_cons.setText(consuming + g_cons + galaxy);
        planet_prod.setText(producing + p_prod + planet);
        galaxy_prod.setText(producing + g_prod + galaxy);

        res_name.setText(game.getEconomy().getResName(resource));
    }
}
