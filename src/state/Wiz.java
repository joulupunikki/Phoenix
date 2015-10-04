/*
 * Copyright (C) 2015 joulupunikki joulupunikki@gmail.communist.invalid.
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
package state;

import dat.UnitType;
import game.Hex;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import static state.State.game;
import util.C;

/**
 * Wizard mode insert units. Mouse wheel returns to game.
 *
 * @author joulupunikki
 */
public class Wiz extends State {

    private static Wiz instance = new Wiz();

    private JPopupMenu unit_menu;

    private Point p;

    private Wiz() {
    }

    public static State get() {
        return instance;
    }

    public void clickOnPlanetMap(MouseEvent e) {

        // if button 3
        //on stack
        //on city
        //on empty hex
        // if button 1
        //on hex
        p = SU.getPlanetMapClickPoint(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            Hex h = game.getHexFromPXY(game.getCurrentPlanetNr(), p.x, p.y);
            if (h.getStructure() == null && h.getStack().isEmpty()) {

                if (unit_menu == null) {
                    setUpStackMenu();
                }
                unit_menu.show(gui.getPlanetMap(), p.x, p.y);
            }
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            SU.setPlanetMapOrigin(p.x, p.y);
        }

    }

    public void clickOnPlanetWindow(MouseEvent e) {
        Point p = e.getPoint();
        int res = SU.isOnResourceIcon(p);
        if (res > -1) {
            SU.clickOnResourceIcon(res);
        }
    }

    public void wheelRotated(MouseWheelEvent e) {
        gui.setCurrentState(main_game_state);
        main_game_state = null;

    }

    private void setUpStackMenu() {
        unit_menu = new JPopupMenu("Select");
        unit_menu.setBackground(Color.DARK_GRAY);
        JMenu[] cats = new JMenu[C.MoveType.values().length];
        ArrayList<List<Integer>> moves_lists = new ArrayList<>();
        for (int i = 0; i < cats.length; i++) {
            moves_lists.add(new LinkedList<>());
        }
        UnitType[][] ut = game.getUnitTypes();
        for (int i = 0; i < ut.length; i++) {
            moves_lists.get(ut[i][0].move_type.ordinal()).add(i);
        }

        for (int j = 0; j < cats.length; j++) {
            cats[j] = new JMenu(C.MoveType.values()[j].name());
            JMenu[] types = new JMenu[moves_lists.get(j).size()];
            Integer[] arr = new Integer[types.length];
            moves_lists.get(j).toArray(arr);
            for (int i = 0; i < types.length; i++) {
                types[i] = new JMenu(game.getUnitTypes()[arr[i]][0].abbrev);
                cats[j].add(types[i]);
                final int u_type = arr[i];
                JMenuItem[] amount = new JMenuItem[C.STACK_SIZE];
                for (int k = 0; k < amount.length; k++) {
                    amount[k] = new JMenuItem("" + (1 + k));
                    final int u_count = 1 + k;

                    amount[k].addActionListener(new ActionListener() {
                        int type = u_type;
                        int count = u_count;

                        public void actionPerformed(ActionEvent e) {
                            for (int l = 0; l < u_count; l++) {
                                game.createUnitInHex(game.getCurrentPlanetNr(), p.x, p.y, game.getTurn(), game.getTurn(), type, 0, 0, 0);
                            }
                        }
                    });
                    types[i].add(amount[k]);
                }
                types[i].setBackground(Color.DARK_GRAY);
                types[i].setForeground(Color.DARK_GRAY);
            }
            unit_menu.add(cats[j]);
            cats[j].setBackground(Color.DARK_GRAY);
        }

    }

    private int getNumberOfTypes(C.MoveType move) {
        int types = 0;
        UnitType[][] ut = game.getUnitTypes();
        for (int i = 0; i < ut.length; i++) {
            if (ut[i][0].move_type.equals(move)) {
                types++;
            }
        }
        return types;
    }
}
