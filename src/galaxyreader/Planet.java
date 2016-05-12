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
package galaxyreader;

import game.PlanetGrid;
import java.io.IOException;
import java.io.PrintWriter;
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

    /**
     *
     */
    private static final long serialVersionUID = 1L;
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
    public Planet[] neighbours;
    private Structure shield;

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
        // read in planet hextile data
        hex_buffer = new int[C.PLANET_MAP_WIDTH][C.PLANET_MAP_HEIGHT];
        int[][] planet_map_tmp = new int[C.PLANET_MAP_WIDTH][C.PLANET_MAP_COLUMNS];
        for (int i = 0; i < C.PLANET_MAP_WIDTH; i++) {
            for (int j = 0; j < C.PLANET_MAP_HEIGHT; j++) {
                hex_buffer[i][j] = GalaxyReader.readInt(fc, count.getSet(4));
            }
        }
        // cull hextile data
        int row = 0;
        int col = 0;
        for (int i = 0; i < C.PLANET_MAP_WIDTH; i++) {
            row = 0;
            for (int j = 0; j < C.PLANET_MAP_HEIGHT - 1; j++) {
                if ((i % 2 == 0 && j % 2 == 1) || (i % 2 == 1 && j % 2 == 0)) {
                    planet_map_tmp[col][row++] = hex_buffer[i][j];
                }
            }
            col++;
        }
        planet_map = planet_map_tmp;

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

    public void setNeighbours(List<Planet> planets) {
        neighbours = new Planet[jump_routes.size()];
        //System.out.print(name + " ");
        for (int i = 0; i < neighbours.length; i++) {

            neighbours[i] = planets.get(jump_routes.get(i).planet_1_index);
            if (neighbours[i].index == this.index) {
                neighbours[i] = planets.get(jump_routes.get(i).planet_2_index);
            }
            //System.out.print(neighbours[i].name + " ");
        }
        //System.out.println("");
    }

    /**
     * Used to place units in space read from GALAXY.GAL at the (re)start of a
     * game.
     *
     * @param e the unit to place
     */
    public void placeUnit(Unit e) {
//        if (space_stacks[e.owner] == null) {
//            space_stacks[e.owner] = new LinkedList<>();
//        }

        space_stacks[e.prev_owner].add(e);

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

    /**
     * Game state printout method, prints the contents of a Planet object. Name,
     * owner, space stacks, hexes.
     */
    public void record(PrintWriter pw) {
        pw.println( name + "," + owner + "\n #SPACE_STACKS");
        for (List<Unit> stack : space_stacks) {
            for (Unit u : stack) {
                u.record(pw);
            }
        }
        pw.println( " #HEXES");
        planet_grid.record(pw);
    }

    /**
     * @return the shield
     */
    public Structure getShield() {
        return shield;
    }

    /**
     * @param shield the shield to set
     */
    public void setShield(Structure shield) {
        this.shield = shield;
    }

    public void omniscience(int turn) {
        spotted[turn] = true;
        planet_grid.omniscience(turn);
    }
}
