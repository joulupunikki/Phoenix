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
package gui;

import dat.Prod;
import dat.ResPair;
import dat.StrBuild;
import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Economy;
import game.Game;
import game.Hex;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import state.PW1;
import util.C;
import util.FN;
import util.Util;
import util.Util.HexIter;
import util.UtilG;
import util.WindowSize;

/**
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class BuildCityPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Gui gui;
    private Game game;
    private WindowSize ws;
    private BufferedImage bi;
    private int[][] structures;
    private JButton exit;
    private JButton build;
    private StrBuild[] str_build;
    private static int[] color_scaler;
    private int selected_slot;
    private int buildables_nr;
    private boolean[] reqd_tech;
    private boolean[] harvest_in_4;
    private int[] buildables;
    private Unit unit;

    public BuildCityPanel(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        game = gui.getGame();
        str_build = game.getStrBuild();

        int tile_set = game.getPlanet(game.getCurrentPlanetNr()).tile_set_type;
        structures = Gui.getStructureTiles(tile_set);
        color_scaler = gui.getResources().getColorScaler();
        setUpButtons();
        setUpMouseListener();
    }

    public void setUpMouseListener() {
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();

                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (ws.bcw_ci_x + i * ws.main_window_width / 2 < p.x
                                && p.x < ws.bcw_ci_x + i * ws.main_window_width / 2 + ws.bcw_ci_w
                                && ws.bcw_ci_y + j * ws.bcw_ci_gap < p.y
                                && p.y < ws.bcw_ci_y + j * ws.bcw_ci_gap + ws.bcw_ci_h) {
                            int selected = i * 10 + j;
                            System.out.println("In box");
                            if (selected < 19 && reqd_tech[selected] && !harvest_in_4[selected]) {
                                System.out.println("Selected set");
                                selected_slot = selected;
                                repaint();
                            }
                        }

                    }

                }

            }
        });
    }

    public void zeroUnit() {
        unit = null;
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
                unit = null;
                gui.hideBuildCityWindow();
            }
        });

        build = new JButton("Build");
        build.setFont(ws.font_default);
        build.setBorder(BorderFactory.createLineBorder(C.COLOR_GOLD));
        this.add(build);
        build.setBounds(ws.bcw_bb_x, ws.fw_eb_y,
                ws.sb_w, ws.sb_h);
        build.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int city_type = buildables[selected_slot];

                Hex hex = game.getHexFromPXY(unit.p_idx, unit.x, unit.y);

                if (hex.getStructure() != null) {
                    gui.showInfoWindow("Already a structure here.");
                    return;
                }

                if (!gui.showConfirmWindow("Are you sure you want to build a "
                        + game.getStrBuild(city_type).name + " ?")) {
                    return;
                }

                Structure new_city = game.createCity(unit.owner, unit.prev_owner, unit.p_idx, unit.x, unit.y, city_type, 50);
//                hex.placeStructure(new_city);
//                game.getStructures().add(new_city);
                game.deleteUnitNotInCombat(unit);
                unit = null;
                if (game.getSelectedStack().isEmpty()) {
                    game.setSelectedPoint(null, -1);
                    game.setPath(null);
                    gui.setCurrentState(PW1.get());
                }
                gui.hideBuildCityWindow();
            }
        });
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderPanel(g);
    }

    public boolean initPanel() {

        //find engineer in selected stack
        List<Unit> stack = game.getSelectedStack();
        unit = null;
        for (Unit unit1 : Util.xS(stack)) {
            if (unit1.type == C.ENGINEER_UNIT_TYPE && !unit1.in_space) {
                unit = unit1;
            }
        }

        if (unit == null) {
            return false;
        }

        byte[][] pallette = gui.getPallette();
        bi = Util.loadImage(FN.S_BG0_PCX, ws.is_double, pallette, 640, 480);

        // get number of buildable cities
        buildables_nr = 0;
        for (int i = 0; i < str_build.length; i++) {
            if (str_build[i].build == 1) {
                buildables_nr++;
            }
        }

        // create index of buildable cities and indication of reqd_tech possession
        boolean[] techs = game.getFaction(game.getTurn()).getResearch().techs;
        buildables = new int[buildables_nr];
        reqd_tech = new boolean[buildables_nr];
        int index = 0;
        for (int i = 0; i < str_build.length; i++) {
            System.out.println("index = " + index);
            System.out.println("str_build[i].name = " + str_build[i].name);
            if (str_build[i].build == 1) {
                buildables[index] = i;
                if (techs[str_build[i].tech]) {
                    reqd_tech[index] = true;
                }
                index++;
            }

        }

        // for harvesting cities check that no harvesting city within 4 hexes
        harvest_in_4 = new boolean[buildables_nr];
        Hex hex = game.getHexFromPXY(unit.p_idx, unit.x, unit.y);
        Set<Hex> hexes_in_4 = Util.getHexesWithinRadiusOf(hex, 4);
        loop:
        for (Hex hex1 : hexes_in_4) {
            Structure str = hex1.getStructure();
            if (str != null) {
                for (int i : C.HARVESTING_CITIES) {
                    if (str.type == i) {
                        for (int j = 0; j < buildables_nr; j++) {
                            for (int k : C.HARVESTING_CITIES) {
                                if (buildables[j] == k) {
                                    harvest_in_4[j] = true;
                                }
                            }
                        }
                        break loop;
                    }
                }
            }
        }

        // for palaces and shields, check that no such structure exists on planet
        // mark this in harvest_in_4
        HexIter iter = Util.getHexIter(game, unit.p_idx);
        Hex hex1 = iter.next();
        while (hex1 != null) {
            Structure city = hex1.getStructure();
            if (city != null) {
                if (city.type == C.PALACE) {
                    for (int i = 0; i < buildables_nr; i++) {
                        if (buildables[i] == C.PALACE) {
                            harvest_in_4[i] = true;
                            break;
                        }

                    }
                } else if (city.type == C.SHIELD) {
                    for (int i = 0; i < buildables_nr; i++) {
                        if (buildables[i] == C.SHIELD) {
                            harvest_in_4[i] = true;
                            break;
                        }

                    }
                }
            }
            hex1 = iter.next();
        }

        // set first eligible buildable as selected
        for (int i = 0; i < buildables.length; i++) {
            if (reqd_tech[i] && !harvest_in_4[i]) {
                selected_slot = i;
                break;
            }
        }

        return true;
    }

    public void renderPanel(Graphics g) {

        // draw city icons
        int divide = (ws.is_double) ? 2 : 1;
        int[] pixel_data = new int[1];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 10; j++) {
                int idx = i * 10 + j;
                if (idx < buildables_nr) {
                    UtilG.writeStruct(ws.bcw_ci_x / divide + i * 320,
                            (ws.bcw_ci_y + j * ws.bcw_ci_gap) / divide, pixel_data, structures, buildables[idx], bi.getRaster(), ws);
                }
            }
        }

        // darken unavailable cities
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 10; j++) {
                int idx = i * 10 + j;
                if (idx < buildables_nr) {
                    if (!reqd_tech[idx] || harvest_in_4[idx]) {
                        System.out.println("darken");
                        darken(ws.bcw_ci_x / divide + i * 320,
                                (ws.bcw_ci_y + j * ws.bcw_ci_gap) / divide, bi.getRaster(), ws);
                    }
                }
            }
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);

        // draw edges
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 10; j++) {
                g.setColor(C.COLOR_GOLD);
                if (selected_slot == i * 10 + j) {
                    g.setColor(Color.WHITE);
                }
                g.drawRect(ws.bcw_ci_x + i * ws.main_window_width / 2,
                        ws.bcw_ci_y + j * ws.bcw_ci_gap, ws.bcw_ci_w, ws.bcw_ci_h);
            }
        }

        // print city type, missing tech, harvesting, producing and consuming
        Structure city = new Structure(0, unit.p_idx, unit.x, unit.y);

        Economy econ = game.getEconomy();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 10; j++) {
                int idx = i * 10 + j;

                if (idx < buildables_nr) {
                    int city_type = buildables[idx];
                    g.setFont(ws.font_default);
                    g.setColor(C.COLOR_GOLD);
                    g.drawString(str_build[city_type].name,
                            ws.bcw_ul_x + i * ws.main_window_width / 2,
                            ws.bcw_ul_y + ws.bcw_ul_h + j * ws.bcw_ci_gap);
                    if (!reqd_tech[idx]) {
                        g.setColor(Color.RED);
                        g.drawString("Missing required tech",
                                i * ws.main_window_width / 2 + ws.main_window_width / 4,
                                ws.bcw_ul_y + ws.bcw_ul_h + j * ws.bcw_ci_gap);
                    }

                    if (isHarvesting(city_type)) {
                        printHarvest(g, econ, city, city_type, i, j);
                    }

                    if (isProducing(city_type)) {
                        printProduction(g, econ, city, city_type, i, j);
                    }

                }

            }

        }
    }

    public void printProduction(Graphics g, Economy econ, Structure city, int city_type, int i, int j) {
        city.type = city_type;
        Prod[] prod_table = econ.getProd();
        int prod_type = Util.productionType(city);

        ResPair make = prod_table[prod_type].make;
        String manifest = "Produces " + make.resource_amount
                + " " + Util.getResName(make.resource_type);
        g.setColor(C.COLOR_RES_DISP_GREEN);
        g.setFont(ws.font_bcw_2);
        g.drawString(manifest, ws.bcw_ll_x + i * ws.main_window_width / 2, ws.bcw_ll_y + ws.bcw_ll_h + j * ws.bcw_ci_gap);

        manifest = "Consumes";
        for (int k = 0; k < 3; k++) {    // For up to 3 possible resource needs

            ResPair need = prod_table[prod_type].need[k];
            if (need == null) {    // When < 3 needs, remaining items in array will be null, so check for that
                break;
            }
            if (k > 0) {
                manifest += ",";
            }
            manifest += " " + need.resource_amount
                    + " " + Util.getResName(need.resource_type);

        }
        g.drawString(manifest, ws.bcw_ll_x + i * ws.main_window_width / 2, ws.bcw_ll_y + 2 * ws.bcw_ll_h + j * ws.bcw_ci_gap);

    }

    public boolean isProducing(int city) {
        boolean ret_val = false;
        for (int i = 0; i < C.PRODUCING_CITIES.length; i++) {
            if (city == C.PRODUCING_CITIES[i]) {
                ret_val = true;
                break;
            }

        }

        return ret_val;
    }

    public void printHarvest(Graphics g, Economy econ, Structure city, int city_type, int i, int j) {
        city.type = city_type;
        int[] harvest = econ.calculateBaseProduction(city);
        String manifest = "Harvests ";
        boolean begin = true;
        boolean nothing = true;
        for (int k = 0; k < harvest.length; k++) {
            if (harvest[k] > 0) {
                nothing = false;
                if (!begin) {
                    manifest += ", ";
                } else {
                    begin = false;
                }
                manifest += "" + harvest[k] + " ";
                String res;
                switch (k) {
                    case C.RES_FOOD:
                        res = "Food";
                        break;
                    case C.RES_METAL:
                        res = "Metal";
                        break;
                    case C.RES_TRACE:
                        res = "Trace";
                        break;
                    case C.RES_ENERGY:
                        res = "Energy";
                        break;
                    case C.RES_EXOTICA:
                        res = "Exotica";
                        break;
                    case C.RES_GEMS:
                        res = "Gems";
                        break;
                    default:
                        throw new AssertionError();
                }
                manifest += res;
            }

        }
        if (nothing) {
            manifest += "nothing";
        }
        g.setColor(C.COLOR_RES_DISP_GREEN);
        g.setFont(ws.font_bcw_2);
        g.drawString(manifest, ws.bcw_ll_x + i * ws.main_window_width / 2, ws.bcw_ll_y + ws.bcw_ll_h + j * ws.bcw_ci_gap);

    }

    public boolean isHarvesting(int city) {
        boolean ret_val = false;
        for (int i = 0; i < C.HARVESTING_CITIES.length; i++) {
            if (city == C.HARVESTING_CITIES[i]) {
                ret_val = true;
                break;
            }

        }

        return ret_val;
    }

    public static void darken(int dx, int dy, WritableRaster wr, WindowSize ws) {
        int x = 0;
        int y = 0;

        for (int i = 0; i < C.STRUCT_BIN_HEIGHT; i++) {
            for (int j = 0; j < C.STRUCT_BIN_WIDTH; j++) {

                x = dx + j;
                y = dy + i;
                int[] pixel_data = {0};
                // if double window size scale image
                if (ws.is_double) {
                    wr.getPixel(2 * x, 2 * y, pixel_data);
                    pixel_data[0] = color_scaler[pixel_data[0]];
                    wr.setPixel(2 * x, 2 * y, pixel_data);
                    wr.getPixel(2 * x + 1, 2 * y, pixel_data);
                    pixel_data[0] = color_scaler[pixel_data[0]];
                    wr.setPixel(2 * x + 1, 2 * y, pixel_data);
                    wr.getPixel(2 * x, 2 * y + 1, pixel_data);
                    pixel_data[0] = color_scaler[pixel_data[0]];
                    wr.setPixel(2 * x, 2 * y + 1, pixel_data);
                    wr.getPixel(2 * x + 1, 2 * y + 1, pixel_data);
                    pixel_data[0] = color_scaler[pixel_data[0]];
                    wr.setPixel(2 * x + 1, 2 * y + 1, pixel_data);

                } else {
                    wr.getPixel(x, y, pixel_data);
                    pixel_data[0] = color_scaler[pixel_data[0]];
                    wr.setPixel(x, y, pixel_data);
                }

            }
        }
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
