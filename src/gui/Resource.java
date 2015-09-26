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

import dat.EfsIni;
import dat.TerColor;
import dat.UnitSpot;
import java.awt.Color;
import java.awt.Cursor;
import java.util.HashMap;
import java.util.Properties;
import util.C;
import util.FN;
import util.Util;
import util.UtilG;
import util.WindowSize;

/**
 *
 * @author joulupunikki
 */
public class Resource {

    private static final int BANNER_100_SIZE = 10000;
    private static final int EFSBUT1_SIZE = 729;
    private int[] color_scaler;
    private double[][][] unit_spot;
    private Color[] colors;
    private int[][] ter_color;
    private Properties efs_ini;
    private Properties phoenix_ini;
    private int[][] res_icons;
    private HashMap<String, Cursor> cursors;
    private int[][] banners_100;
    private int[][] treaty_flags;

    public Resource(Gui gui) {
        WindowSize ws = Gui.getWindowSize();
        color_scaler = UtilG.scaleColorsToDark(C.GRAY_SCALING_FACTOR, gui.getPallette());
        unit_spot = UnitSpot.readUnitSpot();
        setColors(gui.getPallette());
        ter_color = TerColor.readTerColor();
        efs_ini = EfsIni.readEFSINI();
        phoenix_ini = EfsIni.readPhoenixIni();
        res_icons = Util.loadSquares(FN.S_CARGO_BIN, C.RES_TYPES,
                C.CARGO_WIDTH * C.CARGO_HEIGHT);
        createMouseCursors(gui);
        loadBanners100();
        loadTreatyFlags();

    }

    private void loadTreatyFlags() {
        treaty_flags = Util.loadSquares(FN.S_EFSBUT_BIN[1], 2, EFSBUT1_SIZE);
        
    }

    private void loadBanners100() {
        banners_100 = new int[C.NR_FACTIONS][100000];
        banners_100[C.HOUSE1] = Util.loadSquares(FN.S_HOUSE1_BIN, 1, BANNER_100_SIZE)[0];
        banners_100[C.HOUSE2] = Util.loadSquares(FN.S_HOUSE2_BIN, 1, BANNER_100_SIZE)[0];
        banners_100[C.HOUSE3] = Util.loadSquares(FN.S_HOUSE3_BIN, 1, BANNER_100_SIZE)[0];
        banners_100[C.HOUSE4] = Util.loadSquares(FN.S_HOUSE4_BIN, 1, BANNER_100_SIZE)[0];
        banners_100[C.HOUSE5] = Util.loadSquares(FN.S_HOUSE5_BIN, 1, BANNER_100_SIZE)[0];
        banners_100[C.LEAGUE] = Util.loadSquares(FN.S_HOUSE1_BIN, 1, BANNER_100_SIZE)[0];
        banners_100[C.THE_CHURCH] = Util.loadSquares(FN.S_HOUSE1_BIN, 1, BANNER_100_SIZE)[0];
        banners_100[C.SYMBIOT] = Util.loadSquares(FN.S_HOUSE1_BIN, 1, BANNER_100_SIZE)[0];
        banners_100[C.VAU] = Util.loadSquares(FN.S_HOUSE1_BIN, 1, BANNER_100_SIZE)[0];
        banners_100[C.IMPERIAL] = Util.loadSquares(FN.S_HOUSE1_BIN, 1, BANNER_100_SIZE)[0];
        banners_100[C.FLEET] = Util.loadSquares(FN.S_HOUSE1_BIN, 1, BANNER_100_SIZE)[0];
        banners_100[C.STIGMATA] = Util.loadSquares(FN.S_HOUSE1_BIN, 1, BANNER_100_SIZE)[0];
        banners_100[C.THE_SPY] = Util.loadSquares(FN.S_HOUSE1_BIN, 1, BANNER_100_SIZE)[0];
        banners_100[C.NEUTRAL] = Util.loadSquares(FN.S_HOUSE1_BIN, 1, BANNER_100_SIZE)[0];
    }

    public Cursor getCursor(String cursor) {
        return cursors.get(cursor);
    }

    private void createMouseCursors(Gui gui) {
        cursors = new HashMap<>(4);
        cursors.put(C.S_CURSOR_SCEPTOR, gui.createMouseCursor(FN.S_MOUSE_MSK, C.S_CURSOR_SCEPTOR, false));
        cursors.put(C.S_CURSOR_LAND, gui.createMouseCursor(FN.S_MOUSE1_MSK, C.S_CURSOR_LAND, false));
        cursors.put(C.S_CURSOR_BOMBARD, gui.createMouseCursor(FN.S_MOUSE2_MSK, C.S_CURSOR_BOMBARD, true));
    }

    public void setColors(byte[][] pallette) {
        colors = new Color[pallette[0].length];
        for (int i = 0; i < pallette[0].length; i++) {

            int red = (int) pallette[2][i] & 0xff;
            int green = (int) pallette[1][i] & 0xff;
            int blue = (int) pallette[0][i] & 0xff;

            colors[i] = new Color(red, green, blue);
        }
    }

    public Color getColor(int color) {
        return colors[color];
    }

    public int[] getColorScaler() {
        return color_scaler;
    }

    public double[][][] getUnitSpot() {
        return unit_spot;
    }

    public int[][] getTerColor() {
        return ter_color;
    }

    public Properties getEFSIni() {
        return efs_ini;
    }

    public Properties getPhoenixIni() {
        return phoenix_ini;
    }

    public int[][] getResIcons() {
        return res_icons;
    }

    public int[] getBanner100(int faction) {
        return banners_100[faction];
    }

    public int[] getTreatyFlag(int flag) {
        return treaty_flags[flag];
    }
}
