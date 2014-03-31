/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import dat.UnitType;
import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Game;
import game.Hex;
import game.PlanetGrid;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;

/**
 * Class to handle unit path and movement cost calculations. Has to take into
 * account multiple units with different movement types and amount of movement
 * points traveling together and that terrain types may have changed since
 * previous calculations (eg. roads/cities built.) Also has to take into account
 * spotted enemy units and enemy cities.
 *
 * @author joulupunikki
 */
public class PathFind {

    /**
     * Dijkstra's shortest path algorithm on PlanetGrid.
     *
     * @param graph
     * @param source
     * @param destination
     * @return Path ordered List of Hexes or null if path not found;
     */
    public static LinkedList<Hex> shortestPath(PlanetGrid graph, Hex source, Hex destination, int move_type) {

        long time = System.currentTimeMillis();
        int capacity = 0;
        Hex[][] hex_array = graph.getMapArray();
        for (int i = 0; i < hex_array.length; i++) {
            for (int j = 0; j < hex_array[i].length; j++) {
                hex_array[i][j].setMinDist(Integer.MAX_VALUE);
                hex_array[i][j].setVisited(false);
                hex_array[i][j].setPrevious(null);
                ++capacity;
            }

        }
        source.setMinDist(0);
        PriorityQueue<Hex> queue = new PriorityQueue<>(capacity);
        for (int i = 0; i < hex_array.length; i++) {
            for (int j = 0; j < hex_array[i].length; j++) {
                queue.add(hex_array[i][j]);
            }

        }
        Hex h = null;
        while (!queue.isEmpty()) {
            h = queue.poll();
            if (h.getMinDist() == Integer.MAX_VALUE) {
                System.out.println("No path");
                return null;

            }
            h.setVisited(true);
//            path.add(h);
//           System.out.println("XXX");
            if (h.equals(destination)) {
                System.out.println("time = " + (System.currentTimeMillis() - time));
                return getPath(h);
            }

            Hex[] neighbours = h.getNeighbours();
            for (int i = 0; i < neighbours.length; i++) {
                Hex hex = neighbours[i];
//                System.out.println("i = " + i);
                if (hex != null && !hex.isVisited()) {
                    if (hex.getMoveCost(move_type) > 0) {
                        int new_dist = h.getMinDist() + hex.getMoveCost(move_type);
                        if (hex.getMinDist() > new_dist) {
                            queue.remove(hex);
                            hex.setMinDist(new_dist);
                            queue.add(hex);
                            hex.setPrevious(h);
                        }
                    }
                }

            }

        }

        return getPath(h);
    }

    public static LinkedList<Hex> getPath(Hex h) {
        long time = System.currentTimeMillis();
        LinkedList<Hex> rv = new LinkedList<>();

        rv.addFirst(h);
        h = h.getPrevious();
        long index = 0;
        while (h != null) {
            rv.addFirst(h);
            h = h.getPrevious();
//             System.out.println("index = " + index++);
        }
        System.out.println("time = " + (System.currentTimeMillis() - time));
        return rv;
    }

    public static int pathCost(LinkedList<Hex> path, Unit u, Game g) {
        int rv = 0;

        UnitType[][] unit_types = g.getUnitTypes();
        C.MoveType move_type = unit_types[u.type][u.t_lvl].move_type;
        int move_points = unit_types[u.type][u.t_lvl].move_pts;
        if (move_points == 0) {
            return Integer.MAX_VALUE;
        }
        for (ListIterator<Hex> it = path.listIterator(1); it.hasNext();) {
            Hex hex = it.next();
            int tmp = hex.getMoveCost(move_type.ordinal());
            if (tmp == 0) {
                return Integer.MAX_VALUE;
            }
            if (tmp > move_points) {
                tmp = move_points;
            }
            rv += tmp;

        }

        return rv;
    }

    public static void printPath(LinkedList<Hex> path) {
        if (path == null) {
            System.out.println("path = " + path);
            return;
        }
        for (Hex hex : path) {
            Hex prev = hex.getPrevious();
            System.out.print("Path(x,y): " + hex.getX() + ", " + hex.getY() + " Previous(x,y): ");
            if (prev != null) {
                System.out.println(prev.getX() + ", " + prev.getY());
            } else {
                System.out.println("null");
            }
        }
    }

    public static void setMoveCosts(PlanetGrid planet_grid, double[][][] terr_cost, int tile_set, int stack_size, int faction, Hex destination) {

//        printMoveCost(terr_cost);
        Hex[][] hex_array = planet_grid.getMapArray();
        double move_cost = 1;
        for (int i = 0; i < hex_array.length; i++) {
            for (int j = 0; j < hex_array[i].length; j++) {
                boolean[] terr_types = hex_array[i][j].getTerrain();

                for (int l = 0; l < C.MoveType.values().length; l++) {

                    move_cost = 1;

                    for (int k = 0; k < terr_types.length; k++) {
                        if (terr_types[k] == true) {
                            move_cost *= terr_cost[k][tile_set][l];
//                        System.out.println("Move_cost: " + move_cost);
                        }

                    }

                    Hex current_hex = hex_array[i][j];
                    //if own city move_cost == 1 if enemy city move_cost = 0
                    //except when destination hex
                    Structure struct = hex_array[i][j].getStructure();
                    if (struct != null) {
                        if (struct.owner == faction || current_hex.equals(destination)) {
                            move_cost = 1;
                        } else {
                            move_cost = 0;
                        }
                    }

                    //if enemy units (except when destination hex) or would be 
                    // more than 20 own units move_cost = 0
                    List<Unit> stack = hex_array[i][j].getStack();
                    if (Util.stackSize(stack) > 0) {
                        if (stack.get(0).owner != faction) {
                            if (!current_hex.equals(destination)) {
                                move_cost = 0;
                            }
                        } else if (Util.stackSize(stack) + stack_size > 20) {
                            if (!current_hex.equals(destination)) {
                                move_cost = 0;
                            }
                        }
                    }

                    int result = 0;
                    if (move_cost == 0) {
                        result = 0;
                    } else {
                        result = (int) Math.floor(move_cost);
                        if (result == 0) {
                            result = 1;
                        }
                    }
                    hex_array[i][j].setMoveCost(result, l);
                }
            }

        }

    }

    public static void setLandingCosts(PlanetGrid planet_grid, double[][][] terr_cost, int tile_set, int stack_size, int faction, Hex destination) {

//        printMoveCost(terr_cost);
        Hex[][] hex_array = planet_grid.getMapArray();
        double move_cost = 1;
        for (int i = 0; i < hex_array.length; i++) {
            for (int j = 0; j < hex_array[i].length; j++) {
                boolean[] terr_types = hex_array[i][j].getTerrain();

                for (int l = 0; l < C.MoveType.values().length; l++) {

                    move_cost = 1;

                    for (int k = 0; k < terr_types.length; k++) {
                        if (terr_types[k] == true) {
                            move_cost *= terr_cost[k][tile_set][l];
//                        System.out.println("Move_cost: " + move_cost);
                        }

                    }

                    Hex current_hex = hex_array[i][j];
                    //if own city move_cost == 1 if enemy city move_cost = 0
                    //except when destination hex
                    Structure struct = hex_array[i][j].getStructure();
                    if (struct != null) {
                        if (struct.owner == faction || current_hex.equals(destination)) {
                            move_cost = 1;
                        } else {
                            move_cost = 0;
                        }
                    }

                    //if enemy units (except when destination hex) or would be 
                    // more than 20 own units move_cost = 0
                    List<Unit> stack = hex_array[i][j].getStack();
                    if (Util.stackSize(stack) > 0) {
                        if (stack.get(0).owner != faction) {
                            if (!current_hex.equals(destination)) {
                                move_cost = 0;
                            }
                        }
//                        else if (Util.stackSize(stack) + stack_size > 20) {
//                            move_cost = 0;
//                        }
                    }

                    int result = 0;
                    if (move_cost == 0) {
                        result = 0;
                    } else {
                        result = (int) Math.floor(move_cost);
                        if (result == 0) {
                            result = 1;
                        }
                    }
                    hex_array[i][j].setMoveCost(result, l);
                }
            }

        }

    }

    public static void printMoveCost(double[][][] terr_cost) {
        for (int i = 0; i < terr_cost.length; i++) {
            for (int j = 0; j < terr_cost[i].length; j++) {
                for (int k = 0; k < terr_cost[i][j].length; k++) {
                    System.out.print(" " + terr_cost[i][j][k]);

                }
                System.out.println("");
            }
            System.out.println("");
        }
    }
}
