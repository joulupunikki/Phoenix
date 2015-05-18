/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import game.Game;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author joulupunikki
 */
public class UnitPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static Gui gui;
    private static Game game;

    public UnitPanel() {
        setBackground(Color.BLACK);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
//        renderPlanetMap(g);

    }

    public static void setReferences(Gui gu, Game gam) {
        gui = gu;
        game = gam;
    }
}
