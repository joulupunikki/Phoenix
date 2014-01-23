/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import galaxyreader.Structure;
import game.Game;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import util.C;
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
    private String[] planets_strings;
    private String[] cities_strings;

    public BuildPanel(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        game = gui.getGame();
        addLists();
//        List<Planet> planets = game.getPlanets();
    }

    public void addLists() {
        String[] s = {"Kish_0", "Aragon_1", "Delphi_0"};
        planets_strings = s;
        planet_list = new JList();
        planet_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        planet_list.setLayoutOrientation(JList.VERTICAL);
        CustomRendererInt renderer = new CustomRendererInt();
        renderer.setPreferredSize(new Dimension(ws.planet_list_cell_w, ws.planet_list_cell_h));
        planet_list.setCellRenderer(renderer);
        JScrollPane planet_view = new JScrollPane(planet_list);
        planet_view.setPreferredSize(new Dimension(250, 80));
        this.add(planet_view);
        ListSelectionModel list_selection_model;
        list_selection_model = planet_list.getSelectionModel();
        list_selection_model.addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        planetSelected(e);
                    }
                });

        String[] s2 = {"Lab", "Mine", "Well", "Farm"};
        cities_strings = s2;
        city_list = new JList(cities_strings);
        city_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        city_list.setLayoutOrientation(JList.VERTICAL);
        CustomRenderer renderer2 = new CustomRenderer();
        renderer2.setPreferredSize(new Dimension(ws.planet_list_cell_w, ws.planet_list_cell_h));
        city_list.setCellRenderer(renderer2);
        JScrollPane city_view = new JScrollPane(city_list);
        city_view.setPreferredSize(new Dimension(250, 80));
        this.add(city_view);

    }

    public void setPlanets() {        
        boolean[] planets = new boolean[game.getPlanets().size()];
        for (int i = 0; i < planets.length; i++) {
            planets[i] = false;            
        }
        List<Structure> cities = game.getStructures();
        for (Structure structure : cities) {
            if (structure.owner == game.getTurn())
                planets[structure.p_idx] = true;
        }
        ArrayList<Integer> planet_indexes = new ArrayList();
        for (int i = 0; i < planets.length; i++) {
            if (planets[i]) {
                planet_indexes.add(new Integer(i));
            }            
        }
        planet_list.setListData(planet_indexes.toArray());
        
    }
    
    public void planetSelected(ListSelectionEvent e) {
        
    }
    
    public void setGame(Game game) {
        this.game = game;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(33, 33, 33));
        g.fillRect(0, 0, ws.planet_map_width, ws.planet_map_height);
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
            int val = ((Integer)value).intValue();
            String s_val = game.getPlanet(val).name;
            if (isSelected) {
                setBackground(c_f);
                setForeground(c_b);
            } else {
                setBackground(c_b);
                setForeground(c_f);
            }

//            String planet = planets_strings[selectedIndex];
            setText(s_val);
            setFont(list.getFont());

            return this;
        }

    }
    
    class CustomRenderer extends JLabel
            implements ListCellRenderer {

        public CustomRenderer() {
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
            String val = (String) value;

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

    class CustomRendererX extends JLabel
            implements ListCellRenderer {

        public CustomRendererX() {
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
            Color c_f;
            Color c_b = Color.BLACK;
            String raw_val = (String) value;
            int idx = raw_val.indexOf("_");
            String val = raw_val.substring(0, idx);
            int affordable = Integer.parseInt(raw_val.substring(idx + 1, idx + 2));
            if (affordable == 0) {
                c_f = Color.RED;
            } else {
                c_f = C.COLOR_GOLD;
            }
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

}
