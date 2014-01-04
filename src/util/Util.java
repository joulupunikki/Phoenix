/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import galaxyreader.Unit;
import game.Game;
import game.Hex;
import game.PlanetGrid;
import game.Square;
import gui.Gui;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author joulupunikki
 */
public class Util {
    
    public static void debugPrint(String s) {
        if (C.DEBUG_PRINT == 1) {
            System.out.println(s);
        }
    }
    
    public static Point resolveSpaceMapOrigin(Point p, WindowSize ws) {
        
        int x = p.x;
        int y = p.y;
        int space_map_x_origin_offset = ws.space_map_width / (2 * ws.space_map_square_width);
        int space_map_y_origin_offset = ws.space_map_height / (2 * ws.space_map_square_height);
        
        x -= space_map_x_origin_offset;
        y -= space_map_y_origin_offset;
        
        if (x < 0) {
            x = 0;
        } else if (C.STAR_MAP_WIDTH - 15 < x) {
            x = C.STAR_MAP_WIDTH - 15;
        }
        
        if (y < 0) {
            y = 0;
        } else if (C.STAR_MAP_HEIGHT - 13 < y) {
            y = C.STAR_MAP_HEIGHT - 13;
        }
        
        return new Point(x, y);
    }
    
    public static Point forceSpaceMapCoordinates(Point p) {
        int x = p.x;
        int y = p.y;
        if (x < 0) {
            x = 0;
        } else if (C.STAR_MAP_WIDTH - 15 < x) {
            x = C.STAR_MAP_WIDTH - 15;
        }
        
        if (y < 0) {
            y = 0;
        } else if (C.STAR_MAP_HEIGHT - 13 < y) {
            y = C.STAR_MAP_HEIGHT - 13;
        }
        
        return new Point(x, y);
    }
    
    public static Point forcePlanetMapCoordinates(Point p) {
        int x = p.x;
        int y = p.y;

        // roll-over x at x = 44
        if (x < 0) {
            x = C.PLANET_MAP_WIDTH - 1 + x;
        } else if (x > 43) {
            x = x - C.PLANET_MAP_WIDTH;
        }

        // limit y to between 0 and (32 - 10)
        if (y < 0) {
            y = 0;
        } else if (y > 32 - 10) {
            y = 32 - 10;
        }
        
        return new Point(x, y);
    }
    
    public static Point resolvePlanetMapOrigin(Point p) {
        int map_origin_x = p.x;
        int map_origin_y = p.y;
        // roll-over x at x = 44
        if (map_origin_x < 0) {
            map_origin_x = C.PLANET_MAP_WIDTH - 1 + map_origin_x;
        } else if (map_origin_x > 43) {
            map_origin_x = map_origin_x - C.PLANET_MAP_WIDTH;
        }

        // limit y to between 0 and (32 - 10)
        if (map_origin_y < 0) {
            map_origin_y = 0;
        } else if (map_origin_y > 32 - 10) {
            map_origin_y = 32 - 10;
        }
        
        return new Point(map_origin_x, map_origin_y);
        
    }
    
    public static boolean isDelta(int flags) {
        boolean ret_val = false;
        
        if ((flags & 0b0000_1000_0000_1010) == 0b0000_1000_0000_1010) {
            ret_val = true;
        } else if ((flags & 0b0001_0000_0000_1010) == 0b0001_0000_0000_1010) {
            ret_val = true;
        } else if ((flags & 0b0010_0000_0000_1010) == 0b0010_0000_0000_1010) {
            ret_val = true;
        }
        
        return ret_val;
        
    }
    
    public static boolean isForestRiver(int flags) {
        boolean ret_val = false;
        
        if ((flags & 0b0001_0000_0000_1001) == 0b0001_0000_0000_1001) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isForestMtn(int flags) {
        boolean ret_val = false;
        
        if ((flags & 0b0001_0000_0000_0110) == 0b0001_0000_0000_0110) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isForestHill(int flags) {
        boolean ret_val = false;
        
        if ((flags & 0b0001_0000_0000_0111) == 0b0001_0000_0000_0111) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isWater(int flags) {
        boolean is_water = false;
        
        if (((flags & 0x01) != 0x01) && ((flags & 0x02) != 0x02)
                && ((flags & 0x04) != 0x04) && ((flags & 0x08) != 0x08)) {
            is_water = true;
        }
        
        return is_water;
    }
    
    public static boolean isRoad(int flags) {
        boolean is_road = false;
        
        if (((flags & 0x01) == 0x01) && ((flags & 0x02) == 0x02)
                && ((flags & 0x08) == 0x08)) {
            is_road = true;
        }
        
        return is_road;
    }
    
    public static boolean isOnlyGrass(int flags) {
        boolean ret_val = false;
        
        if ((flags & 0b1111_1111_1111) == 0b0000_0000_0001) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isOnlyAridGrass(int flags) {
        boolean ret_val = false;
        
        if ((flags & 0b1111_1111_1111) == 0b0000_0000_0010) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isOnlyDesert(int flags) {
        boolean ret_val = false;
        
        if ((flags & 0b1111_1111_1111) == 0b0000_0000_0011) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isOnlyIce(int flags) {
        boolean ret_val = false;
        
        if ((flags & 0b1111_1111_1111) == 0b0000_0000_0100) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isOnlyTundra(int flags) {
        boolean ret_val = false;
        
        if ((flags & 0b1111_1111_1111) == 0b0000_0000_0101) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isGrass(int flags) {
        boolean ret_val = false;
        
        flags >>>= 9;
        if ((flags & 0b0111) == 0b0001) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isAridGrass(int flags) {
        boolean ret_val = false;
        
        flags >>>= 9;
        if ((flags & 0b0111) == 0b010) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isDesert(int flags) {
        boolean ret_val = false;
        
        flags >>>= 9;
        if ((flags & 0b0111) == 0b0011) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isIce(int flags) {
        boolean ret_val = false;
        
        flags >>>= 9;
        if ((flags & 0b0111) == 0b0100) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isTundra(int flags) {
        boolean ret_val = false;
        
        flags >>>= 9;
        if ((flags & 0b0111) == 0b0101) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isHill(int flags) {
        boolean ret_val = false;
        
        if ((flags & 0b0111) == 0b0111) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isRiver(int flags) {
        boolean ret_val = false;
        
        if ((flags & 0b1001) == 0b1001) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isMountain(int flags) {
        boolean ret_val = false;
        
        if ((flags & 0b0110) == 0b0110) {
            ret_val = true;
        }
        
        return ret_val;
    }
    
    public static boolean isForest(int flags) {
        boolean ret_val = false;
        
        if ((flags & 0b1000) == 0b1000) {
            ret_val = true;
        }
        
        return ret_val;
    }
//    public static void drawName(Graphics g, String s, int x, int y, Font a, Font b, Color c) {
//        for (int i = 0; i < s.length(); i++) {
//            String t = s.substring(i, i + 1);
//            g.drawString(t, x + i * , y);
//            
//        }
//    }

    public static String terrainTypeAbbrev(int type) {
        
        String rv = null;
        switch (type) {
            case C.OCEAN:
                rv = "ocean";
                break;
            case C.GRASS:
                rv = "grass";
                break;
            case C.ARID_GRASS:
                rv = "arid";
                break;
            case C.DESERT:
                rv = "desert";
                break;
            case C.ICE:
                rv = "ice";
                break;
            case C.TUNDRA:
                rv = "tundra";
                break;
            case C.MOUNTAIN:
                rv = "mountain";
                break;
            case C.HILL:
                rv = "hill";
                break;
            case C.TREE:
                rv = "tree";
                break;
            case C.RIVER:
                rv = "river";
                break;
            case C.DELTA:
                rv = "delta";
                break;
            case C.ROAD:
                rv = "road";
                break;
            default:
                throw new AssertionError();
        }
        
        return rv;
        
    }
    
    public static void sortRank(List<Unit> stack) {
        Collections.sort(stack, new Comparator<Unit>() {
            public int compare(Unit o1, Unit o2) {
                return o1.type_data.rank - o2.type_data.rank;
            }
        });
    }
    
    public static int stackSize(List<Unit> stack) {
        int rv = 0;
        for (Unit unit : stack) {
            rv++;
            rv += unit.cargo_list.size();
        }
        return rv;
    }
    
    public static void drawBlip(Graphics g, int x, int y, int side) {
        g.fill3DRect(x, y, side, side, true);
    }

    /**
     * Draws movement points, health bar and tech level on unit icon.
     *
     * @param g
     * @param e
     * @param x
     * @param y
     */
    public static void drawUnitDetails(Graphics g, Unit e, int x, int y) {
        /*
         * draw move points
         */
        WindowSize ws = Gui.getWindowSize();
        int move = e.move_points;
        int side = ws.unit_icon_size;
        int width = 1;
        if (move > 9) {
            width = 2;
        }
        if (move > 99) {
            width = 3;
        }
        g.setColor(Color.BLACK);
        g.fillRect(x + side - (int) (ws.font_unit_icon_size * width * 0.6) - ws.font_unit_icon_offset, y + ws.font_unit_icon_offset,
                (int) (ws.font_unit_icon_size * width * 0.6), (int) (ws.font_unit_icon_size * 0.9));
        g.setColor(Color.WHITE);
        g.setFont(ws.font_unit_icon);
        g.drawString("" + move, x + side - (int) (ws.font_unit_icon_size * width * 0.6) - ws.font_unit_icon_offset, y + ws.font_unit_icon_offset + (int) (ws.font_unit_icon_size * 0.9));
        
        int health = e.health;
        if (health > 66) {
            g.setColor(Color.GREEN);
        } else if (health > 33) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(Color.RED);
        }
        
        g.fillRect(x + ws.font_unit_icon_offset, y + side - 2 * ws.font_unit_icon_offset, (int) Math.ceil((side - 2.0 * ws.font_unit_icon_offset) * health / 100), ws.health_bar_width);
        
        char tech_lvl = 'a';
        
        g.setColor(Color.BLACK);
        g.fillRect(x + ws.font_unit_icon_offset, y + side - 2 * ws.font_unit_icon_offset - (int) (ws.font_unit_icon_size * 0.9),
                (int) (ws.font_unit_icon_size * 0.6), (int) (ws.font_unit_icon_size * 0.9));
        g.setColor(Color.WHITE);
        g.drawString("" + ((char) (tech_lvl + e.t_lvl)), x + ws.font_unit_icon_offset, y + side - 2 * ws.font_unit_icon_offset);
        
        if (e.carrier != null) {
            g.drawString("+", x + 2 * ws.font_unit_icon_offset, y + 8 * ws.font_unit_icon_offset);
        }
    }
    
    public static void drawStackDisplay(Graphics g, Game game, Point p, int faction) {
        
        WindowSize ws = Gui.getWindowSize();
        int[][] unit_icons = Gui.getUnitIcons();
        BufferedImage bi = new BufferedImage(ws.unit_icon_size, ws.unit_icon_size, BufferedImage.TYPE_BYTE_INDEXED, Gui.getICM());
        WritableRaster wr = bi.getRaster();
        int[] pixel_data = new int[1];
        List<Unit> stack = null;
        if (faction == -1) {
            stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y).getStack();
        } else {
            Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
            stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[faction];
        }
        
        boolean is_cargo_listing = false;
        Iterator<Unit> iterator = stack.listIterator();
        Iterator<Unit> cargo_it = null;
//        if (!iterator.hasNext()) {
//            return;
//        }
//        System.out.println("p = " + p);
//        System.out.println("faction = " + faction);
        Unit e = iterator.next();
        for (int i = 0; i < 7; i++) {
            int cols = i == 6 ? 2 : 3;
            for (int j = 0; j < cols; j++) {
                
                int color = Util.getOwnerColor(e.owner);
                if (e.selected) {
                    color += 3;
                }
//                System.out.println("color = " + color);
                Util.fillRaster(wr, color);
                Util.drawUnitIconEdges(wr, ws);
                Util.writeUnit(pixel_data, e.type, unit_icons, wr, ws);
                
                Graphics2D g2d = (Graphics2D) g;
                int dx = ws.unit_icon_size * j + ws.stack_display_x_offset;
                int dy = ws.unit_icon_size * i + ws.stack_display_y_offset;
                
                g2d.drawImage(bi, null, dx, dy);
                
                Util.drawUnitDetails(g, e, dx, dy);

//                if (iterator.hasNext()) {
//                    e = iterator.next();
//                } else {
//                    return;
//                }
                if (is_cargo_listing) {
                    e = cargo_it.next();
                    if (!cargo_it.hasNext()) {
                        cargo_it = null;
                        is_cargo_listing = false;
                    }
                } else if (e.cargo_list.isEmpty()) {
                    if (iterator.hasNext()) {
                        e = iterator.next();
                    } else {
                        return;
                    }
                } else {
                    cargo_it = e.cargo_list.listIterator();
                    e = cargo_it.next();
                    if (cargo_it.hasNext()) {
                        is_cargo_listing = true;
                    }
                }
            }
        }
    }
    
    public static void writeUnitCount(Graphics2D g2d, WindowSize ws, int count, int x, int y) {
        int side = ws.unit_icon_size;
        int width = count > 9 ? 2 : 1;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(x + ws.font_unit_icon_offset, y + side - ws.font_unit_icon_size,
                (int) (ws.font_unit_icon_size * width * 0.6), (int) (ws.font_unit_icon_size * 0.9));
        g2d.setColor(Color.WHITE);
        g2d.setFont(ws.font_unit_icon);
        g2d.drawString("" + count, x + ws.font_unit_icon_offset, side + y - ws.font_unit_icon_offset);
    }
    
    public static String getFactionName(int owner) {
        String faction_name = null;
        
        switch (owner) {
            case C.HOUSE1:
                faction_name = "Li Halan";
                break;
            case C.HOUSE2:
                faction_name = "Hazat";
                break;
            case C.HOUSE3:
                faction_name = "Decados";
                break;
            case C.HOUSE4:
                faction_name = "Hawkwood";
                break;
            case C.HOUSE5:
                faction_name = "Al Malik";
                break;
            case C.LEAGUE:
                faction_name = "League";
                break;
            case C.THE_CHURCH:
                faction_name = "Church";
                break;
            case C.SYMBIOT:
                faction_name = "Symbiot";
                break;
            case C.VAU:
                faction_name = "Vau";
                break;
            case C.IMPERIAL:
                faction_name = "Guard";
                break;
            case C.FLEET:
                faction_name = "Fleet";
                break;
            case C.STIGMATA:
                faction_name = "Stigmata";
                break;
            case C.THE_SPY:
                faction_name = "Eye";
                break;
            case C.NEUTRAL:
                faction_name = "Rebels";
                break;
            
        }
        return faction_name;
    }

    /**
     * Fill raster with color.
     *
     * @param wr raster to fill.
     * @param color fill color.
     */
    public static void fillRaster(WritableRaster wr, int color) {
        int[] pixel_data = new int[1];
        pixel_data[0] = color;
        for (int i = 0; i < wr.getWidth(); i++) {
            for (int j = 0; j < wr.getHeight(); j++) {
                wr.setPixel(i, j, pixel_data);
            }
            
        }
        
    }
    
    public static void drawUnitIconEdges(WritableRaster wr, WindowSize ws) {
        int[] light = {27};
        int[] dark = {18};
        
        int width = wr.getWidth();
        int thickness = 1;
        if (ws.is_double) {
            thickness = 2;
        }
        
        for (int i = 0; i < thickness; i++) {
            for (int j = 0; j < width; j++) {
                
                wr.setPixel(j, width - 1 - i, dark);
                wr.setPixel(width - 1 - i, j, dark);
                wr.setPixel(j, i, light);
                wr.setPixel(i, j, light);
                
            }
            
        }
        
    }
    
    public static void writeRect(int[] pixel_data, int[] picture,
            WritableRaster wr, WindowSize ws, int x, int y, int height, int width) {
        
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                writeRectPixel(x + j, y + i, i * width + j,
                        pixel_data, picture, wr, ws);
            }
        }
    }
    
    public static void writeRectPixel(int x, int y, int t_idx, int[] pixel_data, int[] picture, WritableRaster wr, WindowSize ws) {
        
        pixel_data[0] = picture[t_idx];
        if (pixel_data[0] != 0) {

            // if double window size scale image
            if (ws.is_double) {
                wr.setPixel(2 * x, 2 * y, pixel_data);
                wr.setPixel(2 * x + 1, 2 * y, pixel_data);
                wr.setPixel(2 * x, 2 * y + 1, pixel_data);
                wr.setPixel(2 * x + 1, 2 * y + 1, pixel_data);
//                wr.setPixel(x, y, pixel_data);
//                wr.setPixel(x + 1, y, pixel_data);
//                wr.setPixel(x, y + 1, pixel_data);
//                wr.setPixel(x + 1, y + 1, pixel_data);
            } else {
                wr.setPixel(x, y, pixel_data);
            }
        }
        
    }
    
    public static void writeUnit(int[] pixel_data, int unit_no,
            int[][] unit_pics, WritableRaster wr, WindowSize ws) {

//        int x_offset = (C.STRUCT_BIN_WIDTH - C.EFSUNIT_BIN_WIDTH) / 2;
//        int y_offset = (C.STRUCT_BIN_HEIGHT - C.EFSUNIT_BIN_HEIGHT) / 2;
        for (int i = 0; i < C.EFSUNIT_BIN_HEIGHT; i++) {
            for (int j = 0; j < C.EFSUNIT_BIN_WIDTH; j++) {
                
                writeUnitPixel(j, i, i * C.EFSUNIT_BIN_WIDTH + j,
                        pixel_data, unit_pics, unit_no, wr, ws);
                
            }
        }
    }
    
    public static void writeUnitPixel(int x, int y, int t_idx, int[] pixel_data, int[][] unit_pics, int unit_nr, WritableRaster wr, WindowSize ws) {
        
        pixel_data[0] = unit_pics[unit_nr][t_idx];
        if (pixel_data[0] != 0) {

            // if double window size scale image
            if (ws.is_double) {
                wr.setPixel(2 * x, 2 * y, pixel_data);
                wr.setPixel(2 * x + 1, 2 * y, pixel_data);
                wr.setPixel(2 * x, 2 * y + 1, pixel_data);
                wr.setPixel(2 * x + 1, 2 * y + 1, pixel_data);
            } else {
                wr.setPixel(x, y, pixel_data);
            }
        }
        
    }

    /**
     * Get faction color.
     *
     * @param owner faction number.
     * @return faction color.
     */
    public static int getOwnerColor(int owner) {
        
        int owner_color = 0;
        
        switch (owner) {
            
            case 0:
                owner_color = 0x83;
                break;
            case 1:
                owner_color = 0xE4;
                break;
            case 2:
                owner_color = 0x43;
                break;
            case 3:
                owner_color = 0x64;
                break;
            case 4:
                owner_color = 0xCC;
                break;
            case 5:
                owner_color = 0x51;
                break;
            case 6:
                owner_color = 0xF0;
                break;
            case 7:
                owner_color = 0x14;
                break;
            case 8:
                owner_color = 0x14;
                break;
            case 9:
                owner_color = 0x14;
                break;
            case 10:
                owner_color = 0x14;
                break;
            case 11:
                owner_color = 0x14;
                break;
            case 12:
                owner_color = 0x14;
                break;
            case 13:
                owner_color = 0x19;
                break;
            
        }
        
        return owner_color;
        
    }
    
    public static void drawPicture(Graphics g) {
        // load first portrait image data
        long startTime = System.nanoTime();
        
        Path path1 = FileSystems.getDefault().getPath("PCX/PLNPLAT3.PCX");
        Path path2 = FileSystems.getDefault().getPath("efspallette.hex");
        
        try (FileChannel fc1 = (FileChannel.open(path1));
                FileChannel fc2 = (FileChannel.open(path2))) {
            
            int image_height = 480;
            int image_width = 640;
            int image_size = image_height * image_width;
            int image_skip = 1520;
            byte[] image_data = readBytes(fc1, 0, (int) fc1.size(), ByteOrder.BIG_ENDIAN);
            byte[] pallette_data = readBytes(fc2, 0, C.PALLETTE_LENGTH, ByteOrder.BIG_ENDIAN);

//            System.out.println("File2");
            // extract and assign color data from pallette to color channels
            byte[] red_data = new byte[256];
            byte[] green_data = new byte[256];
            byte[] blue_data = new byte[256];
            //extractPallette(red_data, green_data, blue_data, pallette_data);

            // cast image data to int
            int[] i_data_array = new int[image_size];

            // rle-decode pcx 256 color image
            int src_idx = 128;
            int tgt_idx = 0;
            
            for (; src_idx < image_data.length - 769; src_idx++) {
                
                int datum = image_data[src_idx] & 0xff;
                
                if (datum < 192) {
//                    System.out.println("Datum = " + datum);
                    i_data_array[tgt_idx] = datum;
                    ++tgt_idx;
                } else {
                    src_idx++;
                    int d = image_data[src_idx] & 0xff;
                    
                    for (int j = 0; j < datum - 192; j++) {
//                        System.out.println("Datum > 11000000");
                        i_data_array[tgt_idx] = d;
                        ++tgt_idx;
                    }
                }
            }

//            System.out.println("Image data convert " + s_idx + " " + t_idx);
            // extract and assign color data from pallette to color channels
//            System.out.println("Pallette data");
//            System.out.println("data convert " + s_idx + " " + t_idx);
            // create ICM based on pallette data, BGR-format
            IndexColorModel icm = new IndexColorModel(8, 256, blue_data, green_data, red_data, 256);
//            System.out.println("data convert");
            // create empty byte-indexed buffered image
            BufferedImage bi = new BufferedImage(image_width, image_height, BufferedImage.TYPE_BYTE_INDEXED, icm);
//            System.out.println("data convert");
            // write portrait data to buffered image raster  
            WritableRaster wr = bi.getRaster();
//            System.out.println("data convert");
            wr.setPixels(0, 0, image_width, image_height, i_data_array);
//            System.out.println("data convert");

            // draw image
            Graphics2D g2d = (Graphics2D) g;
//            System.out.println("Draw");
            g2d.drawImage(bi, null, 0, 0);

//            System.out.println("Draw");
        } catch (Exception e) {
            System.out.println(e);
        }
        long estimatedTime = System.nanoTime() - startTime;
//        System.out.println("Time: " + estimatedTime);
    }
    
    public static BufferedImage loadStarFld2(String file_name, boolean double_size_window, byte[][] rgb_data, int width, int height) {
        
        BufferedImage bi = null;
        int image_size = height * width;
        
        byte[] image_data = readFile(file_name, -1, ByteOrder.BIG_ENDIAN);
        
        int[] i_data_array = pcxDecode(image_data, image_size);
        
        for (int i = 0; i < i_data_array.length; i++) {
            if (i_data_array[i] == 0) {
                i_data_array[i] = -96;
            }
        }

        // if double size main window double image dimensions
        if (double_size_window) {
            
            i_data_array = scale2XImage(i_data_array, image_size, width);
            
            height = 2 * height;
            width = 2 * width;
            
        }

        // create ICM based on pallette data, BGR-format
        IndexColorModel icm = new IndexColorModel(8, 256, rgb_data[2], rgb_data[1], rgb_data[0], 256);
        // create empty byte-indexed buffered image
        bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, icm);
        // write portrait data to buffered image raster  
        WritableRaster wr = bi.getRaster();
        wr.setPixels(0, 0, width, height, i_data_array);
        
        return bi;
        
    }
    
    public static BufferedImage loadImage(String file_name, boolean double_size_window, byte[][] rgb_data, int width, int height) {
        
        BufferedImage bi = null;
        int image_size = height * width;
        
        byte[] image_data = readFile(file_name, -1, ByteOrder.BIG_ENDIAN);
        
        int[] i_data_array = pcxDecode(image_data, image_size);

        // if double size main window double image dimensions
        if (double_size_window) {
            
            i_data_array = scale2XImage(i_data_array, image_size, width);
            
            height = 2 * height;
            width = 2 * width;
            
        }

        // create ICM based on pallette data, BGR-format
        IndexColorModel icm = new IndexColorModel(8, 256, rgb_data[2], rgb_data[1], rgb_data[0], 256);
        // create empty byte-indexed buffered image
        bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, icm);
        // write portrait data to buffered image raster  
        WritableRaster wr = bi.getRaster();
        wr.setPixels(0, 0, width, height, i_data_array);
        
        return bi;
        
    }
    
    public static int[] scale2XImage(int[] i_data_array, int image_size, int width) {
        
        int[] tmp = i_data_array;
        int[] target_array = new int[image_size * 4];
        
        for (int i = 0; i < tmp.length; i++) {
            int x = i % width;
            int y = i / width;
            
            target_array[2 * x + 4 * y * width] = tmp[i];
            target_array[2 * x + 1 + 4 * y * width] = tmp[i];
            target_array[2 * x + 2 * (2 * y + 1) * width] = tmp[i];
            target_array[2 * x + 1 + 2 * (2 * y + 1) * width] = tmp[i];
        }
        
        return target_array;
    }
    
    public static Color getColor(byte[][] pallette, int color) {

//        System.out.println(pallette[2][color] + " " + pallette[1][color] + " " + pallette[0][color]);
        int red = (int) pallette[2][color] & 0xff;
        int green = (int) pallette[1][color] & 0xff;
        int blue = (int) pallette[0][color] & 0xff;
        
        if (red < 0) {
            red = 127 - red;
        }
        if (green < 0) {
            green = 127 - green;
        }
        if (blue < 0) {
            blue = 127 - blue;
        }
        return new Color(red, green, blue);
    }
    
    public static byte[][] loadPallette(String file_name) {
        
        byte[][] rgb_data = new byte[3][256];
        byte[] pallette_data = readFile(file_name, 768, ByteOrder.BIG_ENDIAN);

        // extract and assign color data from pallette to color channels
        extractPallette(rgb_data[2], rgb_data[1], rgb_data[0], pallette_data);
        
        return rgb_data;
    }
    
    public static int[] pcxDecode(byte[] encoded_data, int image_size) {

// rle-decode pcx 256 color image
        int[] target_array = new int[image_size];
        int src_idx = 128;
        int tgt_idx = 0;
        
        for (; src_idx < encoded_data.length - 769; src_idx++) {
            
            int datum = encoded_data[src_idx] & 0xff;
            
            if (datum < 192) {
//                    System.out.println("Datum = " + datum);
                target_array[tgt_idx] = datum;
                ++tgt_idx;

                //dirty hack, otherwise starfld2.pcx won't show
                if (tgt_idx == image_size) {
                    return target_array;
                }
                
            } else {
                
                src_idx++;
                int d = encoded_data[src_idx] & 0xff;
                
                for (int j = 0; j < datum - 192; j++) {
//                        System.out.println("Datum > 11000000");
                    target_array[tgt_idx] = d;
                    
                    ++tgt_idx;
                    //dirty hack, otherwise starfld2.pcx won't show
                    if (tgt_idx == image_size) {
                        return target_array;
                    }
                }
            }
        }
        
        return target_array;
    }
    
    public static int[] readImageData(String file_name, long pos, int size, ByteOrder byte_order) {
        
        int[] file_data = new int[size];
        
        byte[] tmp = null;
        
        Path path = FileSystems.getDefault().getPath(file_name);
        
        try (FileChannel fc = (FileChannel.open(path))) {
            
            if (size < 1) {
                size = (int) fc.size();
            }
            tmp = readBytes(fc, pos, size, byte_order);
            
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Failed to read " + file_name);
            System.exit(1);
        }
        
        for (int i = 0; i < tmp.length; i++) {
            file_data[i] = (int) tmp[i];
            
        }
        
        return file_data;
    }
    
    public static byte[] readFile(String file_name, int size, ByteOrder byte_order) {
        
        byte[] file_data = null;
        
        Path path = FileSystems.getDefault().getPath(file_name);
        
        try (FileChannel fc = (FileChannel.open(path))) {
            
            if (size < 1) {
                size = (int) fc.size();
            }
            file_data = readBytes(fc, 0, size, byte_order);
            
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Failed to read " + file_name);
            System.exit(1);
        }
        return file_data;
    }
    
    public static void extractPallette(byte[] red, byte[] green, byte[] blue, byte[] pallette) {
        for (int i = 0; i <= 255; i++) {
            red[i] = (byte) (4 * pallette[3 * i]);
            green[i] = (byte) (4 * pallette[3 * i + 1]);
            blue[i] = (byte) (4 * pallette[3 * i + 2]);
        }
    }
    
    public static void extractPallette2(byte[] red, byte[] green, byte[] blue, byte[] pallette) {
        for (int i = 0; i <= 255; i++) {
            red[i] = pallette[4 * i];
            green[i] = pallette[4 * i + 1];
            blue[i] = pallette[4 * i + 2];
        }
    }
    
    public static byte[] readBytes(FileChannel fc, long index, int length, ByteOrder bo) throws IOException {
        byte[] ret_val = new byte[length];
        int nread;
        ByteBuffer bf = ByteBuffer.allocate(length);
        bf.order(bo);
        fc.position(index);
        do {
            nread = fc.read(bf);
        } while (nread != -1 && bf.hasRemaining());
        bf.rewind();
        byte[] tmp = bf.array();
        System.arraycopy(tmp, 0, ret_val, 0, length);
        return ret_val;
    }
    
    public static int[] loadSquare(String file_name, long pos, int length) {
        int[] square = new int[length];
        Path path = FileSystems.getDefault().getPath(file_name);
        
        try (FileChannel fc = (FileChannel.open(path))) {
            byte[] raw = readBytes(fc, pos, length, ByteOrder.BIG_ENDIAN);
            
            for (int i = 0; i < raw.length; i++) {
                square[i] = (int) raw[i];
                
            }
            
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Failed to read " + file_name);
            System.exit(1);
        }
        
        return square;
    }
    
    public static int[][] loadSquares(String file_name, int nr, int image_size) {
        int[][] squares = null;
        
        Path path = FileSystems.getDefault().getPath(file_name);
        
        try (FileChannel fc = (FileChannel.open(path))) {
            long startTime = System.nanoTime();
            Counter count = new Counter();
            byte[] image_data = null;
            
            squares = new int[nr][];
            for (int i = 0; i < nr; i++) {
                
                int[] i_data_array = new int[image_size];
                image_data = readBytes(fc, count.getSet(image_size), image_size, ByteOrder.BIG_ENDIAN);
                
                for (int j = 0; j < i_data_array.length; j++) {
                    i_data_array[j] = (int) image_data[j];
                    
                }
                
                squares[i] = i_data_array;
                
            }
            
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Failed to read " + file_name);
            System.exit(1);
        }
        
        return squares;
    }
    
    public static int[][] loadHexTiles(String file_name, int length) {
        int[][] hex_tiles = null;
        
        Path path = FileSystems.getDefault().getPath(file_name);
        
        try (FileChannel fc = (FileChannel.open(path))) {
            long startTime = System.nanoTime();
            Counter count = new Counter();
            int image_size = C.STRUCT_BIN_HEIGHT * C.STRUCT_BIN_WIDTH;
            int image_skip = 1520;
            byte[] image_data = null;
            
            hex_tiles = new int[length][];
            for (int i = 0; i < length; i++) {
                
                int offset = 0;//i + j;
                int[] i_data_array = new int[image_size + offset];
                image_data = readBytes(fc, count.getSet(image_skip), image_skip, ByteOrder.BIG_ENDIAN);
                readHexData(i_data_array, image_data, offset);
                hex_tiles[i] = i_data_array;
                
            }
            
            long estimatedTime = System.nanoTime() - startTime;
//            System.out.println("Time: " + estimatedTime);

        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Failed to read " + file_name);
            System.exit(1);
        }
        
        return hex_tiles;
    }
    
    public static void readHexData(int[] i_data_array, byte[] image_data, int offset) {
        //read hexagonal source image to square target
        int s_idx = 0;
        int t_idx = 0 + offset;
        // ... top
        for (int i = 10; i > 0; i--) {
            
            for (int k = 0; k < 2; k++) {
                if (i != 10 || k != 0) {
                    t_idx += i;
                    int counter = 0;
                    for (int j = 0; j < (C.STRUCT_BIN_WIDTH - (2 * i)); ++j) {
                        int tmp = (int) image_data[s_idx] & 0xff;
                        i_data_array[t_idx] = tmp;
                        s_idx++;
                        t_idx++;
                        ++counter;
                    }
//                    System.out.println("Counter: " + counter);
                    t_idx += i;
                }
            }
        }
        // ... center
        for (int i = 0; i < C.STRUCT_BIN_WIDTH * 2; ++i) {
            int tmp = (int) image_data[s_idx] & 0xff;
            i_data_array[t_idx] = tmp;
            s_idx++;
            t_idx++;
        }
        // ... bottom
        for (int i = 1; i <= 10; i++) {
            
            for (int k = 0; k < 2; k++) {
                if (i != 10 || k != 0) {
                    t_idx += i;
                    for (int j = 0; j < (C.STRUCT_BIN_WIDTH - (2 * i)); ++j) {
                        int tmp = (int) image_data[s_idx] & 0xff;
                        i_data_array[t_idx] = tmp;
                        s_idx++;
                        t_idx++;
                    }
                    t_idx += i;
                }
            }
        }

//        System.out.println("data convert " + s_idx + " " + t_idx);
    }
    
    public static Point[][] createBufferConversionTable() {
        /* The height of the hex_buffer
         * is 2x - 1 where x is the planet map height. And there is a value of map flags
         * in every other location in the hex buffer. 
         * Thus y falls behind all the time.In the following table x marks the spot
         * where there is map data in the hex buffer. There appear to be values
         * in some of the o spots as well but their meaning is unclear. 
         * 
         * hex_buffer
         * 
         *      0   1   2   3
         *  0   x   o   x   o
         *  1   o   x   o   x
         *  2   x   o   x   o
         *  3   o   x   o   x
         * ...
         * 59   o   x   o   x
         * 60   x   o   x   o
         * 61   o   x   o   x
         * 62   x   o   x   o
         * 63   o   o   o   o
         * 64   x   o   x   o
         * 
         */
        int height = 32;
        
        Point[][] buf_tab = new Point[C.PLANET_MAP_WIDTH][height];
        
        int count_j = C.PLANET_MAP_HEIGHT - 2;
        RingCounter rc_i = new RingCounter(C.PLANET_MAP_WIDTH - 1, 0);
        RingCounter rc_j = new RingCounter(count_j, 0);
        
        for (int i = 0; i < C.PLANET_MAP_WIDTH; i++) {
            
            for (int j = 0; j < height; j++) {
                
                int p_x = 0;
                int p_y = 0;
                
                p_y = rc_j.getSet();
                
                if (p_y == count_j) {
                    p_x = rc_i.getSet();
                } else {
                    p_x = rc_i.get();
                    
                }
                /*
                 * for even x skip odd y
                 */
                if (p_x % 2 == 0 && p_y % 2 == 1) {
                    p_y = rc_j.getSet();
                    
                    if (p_y == count_j) {
                        p_x = rc_i.getSet();
                    } else {
                        p_x = rc_i.get();
                        
                    }
                }

                /*
                 * for odd x skip even y
                 */
                if (p_x % 2 == 1 && p_y % 2 == 0) {
                    p_y = rc_j.getSet();
                    
                    if (p_y == count_j) {
                        p_x = rc_i.getSet();
                    } else {
                        p_x = rc_i.get();
                        
                    }
                }
                
                Point p = new Point(p_x, p_y);
                buf_tab[i][j] = p;
//                System.out.println("buf_tab (x, y):" + p_x + ", " + p_y);

            }
        }
        
        return buf_tab;
    }
    
    public static int[] getExponentsOf2() {
        
        int[] powers = new int[32];
        
        for (int i = 0; i < powers.length; i++) {
            powers[i] = (int) Math.pow(2, i);
        }
        
        return powers;
    }
    
    public static String createFlagString(int flags) {
        
        String s_flags = "";
        int[] powers = Util.getExponentsOf2();
        
        for (int k = 0; k < powers.length; k++) {
            if ((flags & powers[k]) == powers[k]) {
//                System.out.println("powers k:" + powers[k]);
                char c = 'a';
                int tmp = Math.getExponent(powers[k]);
                
                if (tmp < 10) {
                    s_flags = s_flags + String.valueOf(tmp);
                } else {
                    s_flags = s_flags + Character.toString((char) (c + tmp - 10));
                }
                
            }
        }
        
        return s_flags;
    }
    
    public static boolean moveCapable(Game game) {
        boolean move_capable = false;
        
        Hex destination = game.getPath().get(1);
        int current_planet = game.getCurrentPlanetNr();
        PlanetGrid planet_grid = game.getPlanetGrid(current_planet);
        Point sel = game.getSelectedPoint();
        List<Unit> stack = planet_grid.getHex(sel.x, sel.y).getStack();
        List<Unit> selected = new LinkedList<>();
        for (Unit unit : stack) {
            if (unit.selected) {
                selected.add(unit);
            }
        }
        for (Unit e : selected) {
            int move_cost = destination.getMoveCost(e.move_type.ordinal());
            int max_move = e.type_data.move_pts;
            if (move_cost > max_move) {
                move_cost = max_move;
            }
            if (move_cost <= e.move_points) {
                move_capable = true;
            } else {
                move_capable = false;
                break;
            }
        }
        return move_capable;
    }
    
    public static void logFileFormatError(String file, int line) {
        try (
                FileWriter log_stream = new FileWriter(C.S_LOG_FILE, true);
                PrintWriter log = new PrintWriter(log_stream)) {
            Date date = new Date();
            log.println("***** Begin Log Entry " + date.toString() + " *****");
            log.println("Error reading file: " + file + ", line: " + line);
            log.flush();              
            
        } catch (Exception ex) {
        }
    }
    
    public static void logFFErrorAndExit(String file, int line) {
        logFileFormatError(file, line);
        System.exit(1);
    }
    
    public static void testFFErrorAndExit(boolean found, String file, int line) {
        System.out.println("found = " + found);
        if (!found) {
            logFFErrorAndExit(file, line);
        }
    }

    public static void logEx(Thread t, Throwable e) {
        try (final FileWriter log_stream = new FileWriter(C.S_LOG_FILE, true);final PrintWriter log = new PrintWriter(log_stream)) {
            Date date = new Date();
            log.println("***** Begin Stack Trace " + date.toString() + " *****");
            if (t != null) {
                log.println(t.toString());
                System.out.println(t.toString());
            }
            e.printStackTrace(log);
            date = new Date();
            log.println("***** End Stack Trace " + date.toString() + " *****");
            log.flush();
            e.printStackTrace(System.out);
        } catch (Exception ex) {
        }
    }
}
