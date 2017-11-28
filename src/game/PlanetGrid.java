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
package game;

import galaxyreader.Planet;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import util.C;
import util.Util;

/**
 * Organizes the individual planet map Hexes in a 2-dimensional table, and holds
 * routing information and other support structures for AI.
 *
 * @author joulupunikki
 */
public class PlanetGrid implements Serializable {

    /**
     */
    private static final long serialVersionUID = 1L;
    //map origin
    private Hex origin;
    //array of pointers to map hexes
    private Hex[][] map_array;
    //***** AI support data structures
    // every continent has all its hexes in one LinkedHashMap
    private transient ArrayList<LinkedHashMap<Hex, Hex>> continent_maps;
    // intra continent hex distances, triangular 2-D byte array
    /*
     notice: map height == 32, width == 44 hexes, and straight line max hex
     distance on a planet map is 22 hexes,
     byte is signed and thus we can store distances 0-127, so theoretically it is
     possible to construct pathological "continents" with maximum intra hex
     distance > 127, for the unlikely event that such a continent occurs, the
     hex distance for such hexes will be set to -128.
     */
    private transient volatile byte[][] intra_cont_hex_dist = null;

    public PlanetGrid() {

        // create hexes and populate map array
        map_array = new Hex[C.PLANET_MAP_WIDTH][C.PLANET_MAP_COLUMNS];
        for (int i = 0; i < map_array.length; i++) {
            for (int j = 0; j < map_array[i].length; j++) {
                map_array[i][j] = new Hex(i, j);
            }
        }
        // link hexes to form a graph of planet map
        for (int i = 0; i < map_array.length - 1; i++) {
//            System.out.println("i: " + i);
            for (int j = 0; j < map_array[i].length; j++) {
//                System.out.println("j: " + i);
                if (j != 0 && i % 2 == 1) {
                    map_array[i][j].setN(map_array[i + 1][j - 1], C.NORTHEAST);
                    map_array[i + 1][j - 1].setN(map_array[i][j], C.SOUTHWEST);
                }
                if (i % 2 == 1) {
                    map_array[i][j].setN(map_array[i + 1][j], C.SOUTHEAST);
                    map_array[i + 1][j].setN(map_array[i][j], C.NORTHWEST);
                }

                if (i % 2 == 0) {
                    map_array[i][j].setN(map_array[i + 1][j], C.NORTHEAST);
                    map_array[i + 1][j].setN(map_array[i][j], C.SOUTHWEST);
                }
                if (j != map_array[i].length - 1 && i % 2 == 0) {
                    map_array[i][j].setN(map_array[i + 1][j + 1], C.SOUTHEAST);
                    map_array[i + 1][j + 1].setN(map_array[i][j], C.NORTHWEST);
                }

                if (j != map_array[i].length - 1) {
                    map_array[i][j].setN(map_array[i][j + 1], C.SOUTH);
                    map_array[i][j + 1].setN(map_array[i][j], C.NORTH);
                }

            }

        }

        for (int j = 0; j < map_array[map_array.length - 1].length; j++) {
            int i = map_array.length - 1;
            if (j != map_array[map_array.length - 1].length - 1) {
                map_array[i][j].setN(map_array[i][j + 1], C.SOUTH);
                map_array[i][j + 1].setN(map_array[i][j], C.NORTH);

            }
            if (j != 0) {
                map_array[i][j].setN(map_array[0][j - 1], C.NORTHEAST);
                map_array[0][j - 1].setN(map_array[i][j], C.SOUTHWEST);
            }

            if (j != map_array[map_array.length - 1].length - 1) {
                map_array[i][j].setN(map_array[0][j], C.SOUTHEAST);
                map_array[0][j].setN(map_array[i][j], C.NORTHWEST);
            }
        }

        origin = map_array[0][0];

    }

    public Hex getHex(int i, int j) {
        return map_array[i][j];
    }

    public Hex[][] getMapArray() {
        return map_array;
    }

    public void setTerrainTypes(Planet planet) {
        for (int i = 0; i < map_array.length; i++) {
            for (int j = 0; j < map_array[i].length; j++) {
                map_array[i][j].setTerrain(planet.resolveTerrainType(i, j));

            }

        }
    }

    /**
     * Create per Galaxy static AI support data structures, slow calculations
     * not suitable for serial execution (will delay game initialization).
      *
     * @param planet
     */
    public void parallelSetAIDataStructures(Planet planet) {
        defineIntraContinentHexDist();
    }

    /**
     * Create per Galaxy static AI support data structures, fast calculations
     * suitable for serial execution.
     *
     * @param planet
     * @param game the value of game
     */
    public void serialSetAIDataStructures(Planet planet, Game game) {
        defineContinents(planet, game);
    }

    private void defineIntraContinentHexDist() {
        byte tmp_dists[][] = new byte[C.PLANET_MAP_COLUMNS * C.PLANET_MAP_WIDTH][];
        for (int i = 0; i < tmp_dists.length; i++) {
            tmp_dists[i] = new byte[i + 1];
            for (int j = 0; j < tmp_dists[i].length; j++) {
                tmp_dists[i][j] = -1;

            }
        }
        Set<Hex> all_hexes = new LinkedHashSet<>();  // set of all visited hexes
        LinkedList<Hex> queue = new LinkedList<>();
        all_hexes.add(origin);
        queue.add(origin);
        while (!queue.isEmpty()) {
            Hex father = queue.pop();
            Hex[] neighbours = father.getNeighbours();
            for (Hex child : neighbours) {
                if (child != null && all_hexes.add(child)) {
                    queue.add(child);
                }
            }
            if (father.getLandNr() < 0) {
                continue;
            }
            Set<Hex> all_hexes2 = new LinkedHashSet<>();  // set of all visited hexes
            LinkedList<Hex> queue2 = new LinkedList<>();
            LinkedList<Integer> dist_q = new LinkedList<>();
            all_hexes2.add(father);
            queue2.add(father);
            dist_q.add(0);
            while (!queue2.isEmpty()) {
                Hex father2 = queue2.pop();
                int dist = dist_q.pop();
                if (father2.getLandNr() != father.getLandNr()) {
                    continue;
                }
                initIntraContHexDist(father.getHexIdx(), father2.getHexIdx(), dist, tmp_dists);
                Hex[] neighbours2 = father2.getNeighbours();
                for (Hex child2 : neighbours2) {
                    if (child2 != null && all_hexes2.add(child2)) {
                        queue2.add(child2);
                        dist_q.add(dist + 1);
                    }
                }

            }
        }
        intra_cont_hex_dist = tmp_dists;
    }

    /**
     * Divides land hexes of a planet into ArrayList<LinkedHashMap<Hex,Hex>> of
     * continents.
     *
     * @param planet
     */
    private void defineContinents(Planet planet, Game game) {
        continent_maps = new ArrayList<>();
        //LinkedHashMap<Hex, Hex> continent = new LinkedHashMap<>(C.PLANET_MAP_COLUMNS * C.PLANET_MAP_WIDTH);    // list of hexes to be returned
        Set<Hex> all_hexes = new LinkedHashSet<>();  // set of all visited hexes
        LinkedList<Hex> ocean = new LinkedList<>();
        LinkedList<Hex> land = new LinkedList<>();
        if (!origin.getTerrain(C.OCEAN) || planet.tile_set_type == C.BARREN_TILE_SET) {
            land.add(origin);
        } else {
            ocean.add(origin);
        }
        all_hexes.add(origin);
        while (!(ocean.isEmpty() && land.isEmpty())) {
            Hex parent = null;
            if (land.isEmpty()) {
                parent = ocean.pop();
                if (!continent_maps.isEmpty() && !continent_maps.get(continent_maps.size() - 1).isEmpty()) {
                    continent_maps.add(new LinkedHashMap<>(C.PLANET_MAP_COLUMNS * C.PLANET_MAP_WIDTH));
                    //Util.dP(planet.name + " " + continent_maps.size());
                }
            } else {
                if (continent_maps.isEmpty()) {
                    continent_maps.add(new LinkedHashMap<>(C.PLANET_MAP_COLUMNS * C.PLANET_MAP_WIDTH));
                    //Util.dP(planet.index + " " + planet.name + " " + continent_maps.size());
                }
                parent = land.pop();   
            }
            if (!parent.getTerrain(C.OCEAN) || planet.tile_set_type == C.BARREN_TILE_SET) {
                continent_maps.get(continent_maps.size() - 1).put(parent, parent);
            } else {
                parent.setLandNr(-1); // Fix #124
            }
            Hex[] neighbours = parent.getNeighbours();
            for (Hex child : neighbours) {
                if (child != null && all_hexes.add(child)) {
                    if (!child.getTerrain(C.OCEAN) || planet.tile_set_type == C.BARREN_TILE_SET) {
                        land.add(child);
                    } else {
                        ocean.add(child);
                    }
                }
            }
        }
        int count = 0;
        for (LinkedHashMap<Hex, Hex> continent_map : continent_maps) {
            for (Map.Entry<Hex, Hex> entrySet : continent_map.entrySet()) {
                entrySet.getValue().setHexIdx().setLandNr(count); // method chaining
            }
            count++;
        }
        // Fix #124: assert that if planet.tile_set_type != C.BARREN_TILE_SET ocean hexes have land_nr of -1
        Util.HexIter iter = Util.getHexIter(game, planet.index);
        Hex tmp = iter.next();
        System.out.print(" Creating AI continent mappings ... Planet: " + planet.index + "," + planet.name + " ...");
        while (tmp != null) {
            assert planet.tile_set_type == C.BARREN_TILE_SET || tmp.getTerrain(C.OCEAN) == false || tmp.getLandNr() < 0;
            tmp = iter.next();
        }
        System.out.println(" OK");
    }

//    public Hex getNextStack(Point p) {
//
//    }
    public boolean test() {
        boolean ret_val = true;

        for (int i = 0; i < map_array.length; i++) {
            for (int j = 0; j < map_array[i].length; j++) {
//                System.out.println("Hex (x ,y): " + i + ", " + j);
//                System.out.println("Neighbours:");
                for (int k = 0; k < 6; k++) {
                    Hex neighbour = map_array[i][j].getN(k);
                    if (neighbour != null) {

                        System.out.print("           " + k + ": ");
                        neighbour.print();
                    }
                }

            }

        }

        for (int i = 0; i < map_array.length; i++) {

            for (int j = 0; j < map_array[i].length; j++) {
                if (j != 0 || i % 2 != 1) {
                    Hex first = map_array[i][j];
                    Hex current = first;
//                    current.print();
//                    System.out.println("Test");
                    for (int k = 0; k < map_array.length; k++) {
                        if (k % 2 != 1) {
                            current = current.getN(C.NORTHEAST);

                        } else {
                            current = current.getN(C.SOUTHEAST);
                        }
//                        current.print();
//                        System.out.println("Test2");
                    }

                    if (!(first.equals(current))) {
                        ret_val = false;
                    }

                }
                if (j != map_array[i].length || i % 2 != 1) {
                    Hex first = map_array[i][j];
                    Hex current = first;
                    for (int k = 0; k < map_array.length; k++) {
//                        System.out.println("k: " + k);
                        if (k % 2 != 1) {
                            current = current.getN(C.SOUTHEAST);

                        } else {
                            current = current.getN(C.NORTHEAST);
                        }

                    }

                    if (!(first.equals(current))) {
                        ret_val = false;
                    }

                }
            }
            Hex first = map_array[i][0];
            Hex last = map_array[i][map_array[i].length - 1];
            Hex current = first;
            for (int j = 0; j < map_array[i].length - 1; j++) {
                current = current.getN(C.SOUTH);
            }
            if (!(last.equals(current))) {
                ret_val = false;
            }
            for (int j = 0; j < map_array[i].length - 1; j++) {
                current = current.getN(C.NORTH);
            }
            if (!(first.equals(current))) {
                ret_val = false;
            }

        }
        return ret_val;
    }

    /**
     * Game state printout method, prints the contents of a PlanetGrid. Objects
     * in hexes.
     */
    public void record(PrintWriter pw) {
        for (Hex[] map_array1 : map_array) {
            for (Hex hex : map_array1) {
                hex.record(pw);
            }
        }
    }

    public byte getIntraContHexDist(Hex a, Hex b) {
        return getIntraContHexDist(a.getHexIdx(), b.getHexIdx());
        //return getIntraContHexDist(a.getX() + a.getY() * C.PLANET_MAP_WIDTH, b.getX() + b.getY() * C.PLANET_MAP_WIDTH);
    }

    public byte getIntraContHexDist(int a, int b) {
        if (a >= b) {
            return intra_cont_hex_dist[a][b];
        } else {
            return intra_cont_hex_dist[b][a];
        }
    }

    public byte[][] getIntraContHexDist() {
        return intra_cont_hex_dist;
    }

    private void initIntraContHexDist(int a, int b, int dist, byte[][] array) {
        if (a >= b) {
            if (dist > 127) {
                dist = -128;
            }
            array[a][b] = (byte) dist;
        }
    }

    private void setIntraContHexDist(int a, int b, int dist) {
        if (a >= b) {
            if (dist > 127) {
                dist = -128;
            }
            intra_cont_hex_dist[a][b] = (byte) dist;
        }
    }

    public void setIntraContHexDist(byte[][] b) {
        intra_cont_hex_dist = b;
    }

    public ArrayList<LinkedHashMap<Hex, Hex>> getContinentMaps() {
        return continent_maps;
    }

    public void omniscience(int turn) {
        for (int i = 0; i < map_array.length; i++) {
            for (int j = 0; j < map_array[i].length; j++) {
                map_array[i][j].omniscience(turn);

            }

        }
    }

}
