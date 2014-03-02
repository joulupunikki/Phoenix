/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import galaxyreader.Planet;
import java.io.Serializable;
import util.C;

/**
 *
 * @author joulupunikki
 */
public class PlanetGrid implements Serializable {

    //map origin
    private Hex origin;
    //array of pointers to map hexes
    private Hex[][] map_array;

    public PlanetGrid() {

        // create hexes and populate map array
        map_array = new Hex[C.PLANET_MAP_WIDTH][];
        for (int i = 0; i < map_array.length; i++) {
            if (i % 2 == 0) {
                map_array[i] = new Hex[C.PLANET_MAP_COLUMNS - 1];
            } else {
                map_array[i] = new Hex[C.PLANET_MAP_COLUMNS];
            }

            for (int j = 0; j < map_array[i].length; j++) {
                map_array[i][j] = new Hex(i, j);
//                map_array[i][j].print();
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
                if (j != map_array[i].length - 1 && i % 2 == 1) {
                    map_array[i][j].setN(map_array[i + 1][j], C.SOUTHEAST);
                    map_array[i + 1][j].setN(map_array[i][j], C.NORTHWEST);
                }

                if (i % 2 == 0) {
                    map_array[i][j].setN(map_array[i + 1][j], C.NORTHEAST);
                    map_array[i + 1][j].setN(map_array[i][j], C.SOUTHWEST);
                }
                if (i % 2 == 0) {
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
}
