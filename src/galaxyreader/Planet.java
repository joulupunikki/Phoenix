/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package galaxyreader;

import game.PlanetGrid;
import java.awt.Point;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import util.C;
import util.Util;

/**
 * A class representing a planet object.
 *
 * @author joulupunikki
 */
public class Planet implements Serializable {

    public int index; // original code
    public int x; // short
    public int y; // short
    int cur_rot_pos; // short
    int[] unit_pos; // UINT[8]
    public String name; // char[32]
    int owner; // short
    int sect; // short
    int flags; // UINT
    public int tile_set_type;
    int[][] hex_tiles;
    // planet map encoded in GALAXY.GAL
    int[][] hex_buffer;
    int[][] prod_map;
    // planet map encoded with bottom hexes of even numbered columns removed
    int[][] planet_map;
    // units in space around planet
    public List<Unit>[] space_stacks;
    public PlanetGrid planet_grid;
    public List<JumpGate> jump_routes;
    public boolean[] spotted;

    /**
     * Creates a planet object. Reads in positional coordinates, name, owner and
     * other fields.
     *
     * @param fc the FileChannel containing the file from which the data is
     * read.
     * @param count the Counter containing the position from which the data is
     * read.
     * @param gal reference to the galaxy object which invokes this constructor.
     * @throws IOException
     */
    public Planet(FileChannel fc, Counter count, Galaxy gal, int index) throws IOException {

        count.getSet(-2);
        this.index = index;
        x = GalaxyReader.readShort(fc, count.getSet(2));
        y = GalaxyReader.readShort(fc, count.getSet(2));
        cur_rot_pos = GalaxyReader.readShort(fc, count.getSet(2));

        unit_pos = new int[C.UNIT_POS];
        for (int i = 0; i < C.UNIT_POS; i++) {
            unit_pos[i] = GalaxyReader.readInt(fc, count.getSet(4));

        }

//        count.getSet(-1);
        char[] ca = new char[C.PLANET_NAME];
        for (int i = 0; i < C.PLANET_NAME; i++) {
            ca[i] = (char) GalaxyReader.readByte(fc, count.getSet(1));
//            System.out.print(ca[i]);
//            System.out.println(" " + (int) ca[i]);
        }
//        System.out.println();
        name = new String(ca);
        //remove garbage from end of name
        int end_of_name = 0;
        for (int i = 0; (i < ca.length) && (((int) ca[i]) != 0); i++) {
            end_of_name++;
        }
        name = name.substring(0, end_of_name);
//        count.getSet(1);

        owner = GalaxyReader.readShort(fc, count.getSet(2));

        sect = GalaxyReader.readShort(fc, count.getSet(2));

        flags = GalaxyReader.readInt(fc, count.getSet(4));

        tile_set_type = GalaxyReader.readInt(fc, count.getSet(4));

//        System.out.println(gal.version);
        if (gal.version < 961024) {

            for (int i = 0; i < 3; i++) {
                GalaxyReader.readInt(fc, count.getSet(4));

            }
        }
        count.getSet(-4);
        hex_buffer = new int[C.PLANET_MAP_WIDTH][C.PLANET_MAP_HEIGHT];
        for (int i = 0; i < C.PLANET_MAP_WIDTH; i++) {
            for (int j = 0; j < C.PLANET_MAP_HEIGHT; j++) {
                hex_buffer[i][j] = GalaxyReader.readInt(fc, count.getSet(4));
//System.out.println("hex_buffer:" + hex_buffer[i][j]);
            }
        }
//        System.out.println("read buf");
        count.getSet(4);

        Point[][] buf_tab = Util.createBufferConversionTable();

        int[][] tmp = new int[C.PLANET_MAP_WIDTH][C.PLANET_MAP_COLUMNS];

        for (int i = 0; i < C.PLANET_MAP_WIDTH; i++) {
            for (int j = 0; j < C.PLANET_MAP_COLUMNS; j++) {
                tmp[i][j] = hex_buffer[buf_tab[i][j].x][buf_tab[i][j].y];

            }

        }

        planet_map = new int[C.PLANET_MAP_WIDTH][];

        for (int i = 0; i < C.PLANET_MAP_WIDTH; i++) {
            if (i % 2 == 0) {
                planet_map[i] = new int[C.PLANET_MAP_COLUMNS - 1];

            } else {
                planet_map[i] = new int[C.PLANET_MAP_COLUMNS];

            }
        }

        for (int i = 0; i < planet_map.length; i++) {
            for (int j = 0; j < planet_map[i].length; j++) {
                if (i % 2 == 0) {
                    planet_map[i][j] = tmp[i][j + 1];
                } else {
                    planet_map[i][j] = tmp[i][j];

                }
            }

        }

        // google "java generic array creation"
        space_stacks = (LinkedList<Unit>[]) new LinkedList[14];
//        space_stacks = new LinkedList<Unit>[13]; doesn't work
        for (int i = 0; i < space_stacks.length; i++) {
            space_stacks[i] = new LinkedList<>();

        }

        jump_routes = new LinkedList<>();
        planet_grid = new PlanetGrid();

        planet_grid.setTerrainTypes(this);

        spotted = new boolean[C.NR_FACTIONS];
        for (int i = 0; i < spotted.length; i++) {
            spotted[i] = false;
            
        }
        
    }

    public void addStack(List<Unit> stack, int faction) {
        space_stacks[faction].addAll(stack);
    }

    public void minusStack(List<Unit> stack, int faction) {
        space_stacks[faction].removeAll(stack);
    }

    public void placeUnit(Unit e) {
//        if (space_stacks[e.owner] == null) {
//            space_stacks[e.owner] = new LinkedList<>();
//        }

        space_stacks[e.owner].add(e);

    }

    public int[][] getMap() {
        return planet_map;
    }

    public int[][] getBuffer() {
        return hex_buffer;
    }

    /**
     * Prints the planet object. For debugging purposes.
     */
    public void print() {
        System.out.println("x:" + x);
        System.out.println("y:" + y);
        System.out.println("cur rot pos:" + cur_rot_pos);
        for (int i = 0; i < C.UNIT_POS; i++) {
            System.out.println("unit pos:" + i + " " + unit_pos[i]);

        }
        System.out.println("name:" + name);
        System.out.println("owner:" + owner);
        System.out.println("sect:" + sect);
        System.out.println("flags:" + flags);
        System.out.println("tile set type:" + tile_set_type);

//        for (int i = 0; i < C.PLANET_MAP_WIDTH; i++) {
//            for (int j = 0; j < C.PLANET_MAP_HEIGHT; j++) {
//                System.out.println("i, j" + i + ", " + j + ": " + "hex_buffer: " + hex_buffer[i][j]);
//            }
//            System.out.println("print buf");
//        }
    }

    public boolean[] resolveTerrainType(int u, int v) {

        boolean[] terr_type = new boolean[12];

        int flags = planet_map[u][v];

        if (Util.isWater(flags)) {

            terr_type[C.OCEAN] = true;
//            System.out.println("water found");
            return terr_type;

        } else if (Util.isRoad(flags)) {
//            System.out.println("road found");
            terr_type[C.ROAD] = true;
            flags >>>= 9;
        }

        if (Util.isDelta(flags)) {
            terr_type[C.DELTA] = true;
            flags >>>= 11;
        }

        if (Util.isForestHill(flags)) {
            terr_type[C.HILL] = true;
            flags >>>= 9;
        } else if (Util.isForestMtn(flags)) {
            terr_type[C.MOUNTAIN] = true;
            flags >>>= 9;
        } else if (Util.isForestRiver(flags)) {
//            System.out.println("isForestRiver");
//            System.out.println("raw flags: " + Util.createFlagString(flags));
            terr_type[C.RIVER] = true;
//            System.out.println("terr_type[2]: " + terr_type[2]);
            flags >>>= 9;
        }

        if (Util.isOnlyGrass(flags)) {
            terr_type[C.GRASS] = true;
            return terr_type;
        }

        if (Util.isOnlyAridGrass(flags)) {
            terr_type[C.ARID_GRASS] = true;
            return terr_type;
        }

        if (Util.isOnlyDesert(flags)) {
            terr_type[C.DESERT] = true;
            return terr_type;
        }

        if (Util.isOnlyIce(flags)) {
            terr_type[C.ICE] = true;
            return terr_type;
        }

        if (Util.isOnlyTundra(flags)) {
            terr_type[C.TUNDRA] = true;
            return terr_type;
        }

        if (Util.isGrass(flags)) {
            terr_type[C.GRASS] = true;
            flags = flags ^ 0b0010_0000_0000;
        } else if (Util.isAridGrass(flags)) {
            terr_type[C.ARID_GRASS] = true;
            flags = flags ^ 0b0100_0000_0000;
        } else if (Util.isDesert(flags)) {
            terr_type[C.DESERT] = true;
            flags = flags ^ 0b0110_0000_0000;
        } else if (Util.isIce(flags)) {
            terr_type[C.ICE] = true;
            flags = flags ^ 0b1000_0000_0000;
        } else if (Util.isTundra(flags)) {
            terr_type[C.TUNDRA] = true;
            flags = flags ^ 0b1010_0000_0000;
        }
//        String s_flags = Util.createFlagString(flags);
//                System.out.println("flags u, v: " + u + ", " + v + ": " + s_flags);

        if (Util.isHill(flags)) {
            terr_type[C.HILL] = true;
        } else if (Util.isRiver(flags)) {
            terr_type[C.RIVER] = true;
        } else if (Util.isMountain(flags)) {
            terr_type[C.MOUNTAIN] = true;
        } else if (Util.isForest(flags)) {
            terr_type[C.TREE] = true;
        }

        return terr_type;
    }
}
