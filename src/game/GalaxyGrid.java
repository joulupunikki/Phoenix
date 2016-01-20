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

import galaxyreader.Galaxy;
import galaxyreader.Planet;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import util.C;

/**
 *
 * @author joulupunikki
 */
public class GalaxyGrid implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Square[][] galaxy_grid;
    //***** AI support data structures
    //planet pair jump distances
    private int[][] jump_dist;
    /*
    Jump route navigation road signs: contains planet indexes, like a routing
    table in an internet router. Let routing_table[a][c] == b, then if a ship is
    in orbit of planet a, then to move towards planet c, the ship must jump to
    planet b.
     */
    private int[][] routing_table;
    public GalaxyGrid(Galaxy galaxy) {

        List<Planet> planets = galaxy.getPlanets();

        galaxy_grid = new Square[C.STAR_MAP_WIDTH][C.STAR_MAP_HEIGHT];

        for (int i = 0; i < galaxy_grid.length; i++) {
            for (int j = 0; j < galaxy_grid[i].length; j++) {
                galaxy_grid[i][j] = new Square();

            }

        }

        for (Planet e : planets) {
            galaxy_grid[e.x][e.y].planet = e;
        }

        Planet planet;
        for (int i = 0; i < galaxy_grid.length; i++) {
            for (int j = 0; j < galaxy_grid[i].length; j++) {
                if (i > 0 && j > 0) {
                    planet = galaxy_grid[i - 1][j - 1].planet;
                    if (planet != null) {
                        galaxy_grid[i][j].setStackData(C.STIGMATA, planet);
                    }
                }

                if (j > 0) {
                    planet = galaxy_grid[i][j - 1].planet;
                    if (planet != null) {
                        galaxy_grid[i][j].setStackData(C.VAU, planet);
                    }
                }

                if (i < galaxy_grid.length - 1 && j > 0) {
                    planet = galaxy_grid[i + 1][j - 1].planet;
                    if (planet != null) {
                        galaxy_grid[i][j].setStackData(C.LEAGUE, planet);
                    }
                }
                if (i > 0) {
                    planet = galaxy_grid[i - 1][j].planet;
                    if (planet != null) {
                        galaxy_grid[i][j].setStackData(C.HOUSE5, planet);
                    }
                }
                if (i < galaxy_grid.length - 1) {
                    planet = galaxy_grid[i + 1][j].planet;
                    if (planet != null) {
                        galaxy_grid[i][j].setStackData(C.HOUSE4, planet);
                    }
                }
                if (i > 0 && j < galaxy_grid[0].length - 1) {
                    planet = galaxy_grid[i - 1][j + 1].planet;
                    if (planet != null) {
                        galaxy_grid[i][j].setStackData(C.HOUSE3, planet);
                    }
                }
                if (j < galaxy_grid[0].length - 1) {
                    planet = galaxy_grid[i][j + 1].planet;
                    if (planet != null) {
                        galaxy_grid[i][j].setStackData(C.HOUSE2, planet);
                    }
                }
                if (i < galaxy_grid.length - 1 && j < galaxy_grid[0].length - 1) {
                    planet = galaxy_grid[i + 1][j + 1].planet;
                    if (planet != null) {

                        galaxy_grid[i][j].setStackData(C.HOUSE1, planet);
                    }

                }

            }
        }
    }

//    public void placeUnit(Unit e) {
//        galaxy_grid[e.x][e.y].stacks.add(e);
//    }
    public Square[][] getGalaxyGrid() {
        return galaxy_grid;
    }

    void defineJumpRouteTables(List<Planet> planets) {
        int tmp_jump_dists[][] = new int[planets.size()][planets.size()];
        int tmp_routing_table[][] = new int[planets.size()][planets.size()];
        for (int i = 0; i < tmp_routing_table.length; i++) {
            tmp_routing_table[i][i] = i;
        }
        Set<Planet> all_planets = new LinkedHashSet<>();  // set of all visited hexes
        LinkedList<Planet> queue = new LinkedList<>();
        all_planets.add(planets.get(0));
        queue.add(planets.get(0));
        while (!queue.isEmpty()) {
            Planet father = queue.pop();
            for (Planet child : father.neighbours) {
                if (child != null && all_planets.add(child)) {
                    queue.add(child);
                }
            }

            Set<Planet> all_planets2 = new LinkedHashSet<>();  // set of all visited hexes
            LinkedList<Planet> queue2 = new LinkedList<>();
            LinkedList<Integer> dist_q = new LinkedList<>();
            all_planets2.add(father);
            queue2.add(father);
            dist_q.add(0);
            while (!queue2.isEmpty()) {
                Planet father2 = queue2.pop();
                int dist = dist_q.pop();
                tmp_jump_dists[father.index][father2.index] = dist;
                for (Planet child2 : father2.neighbours) {
                    if (child2 != null && all_planets2.add(child2)) {
                        queue2.add(child2);
                        dist_q.add(dist + 1);
                        tmp_routing_table[child2.index][father.index] = father2.index;
                    }
                }

            }
        }
        jump_dist = tmp_jump_dists;
        routing_table = tmp_routing_table;
        for (int i = 0; i < routing_table.length; i++) {
            System.out.print(planets.get(i).name + ": ");
            for (int j = 0; j < routing_table.length; j++) {
                System.out.print(planets.get(j).name + "->" + planets.get(routing_table[i][j]).name + " ");

            }
            System.out.println("");
        }
    }

    public int getJumpDistance(int a_idx, int b_idx) {
        return jump_dist[a_idx][b_idx];
    }

    /**
     * If a ship is in orbit of a planet with index current, and wants to go
     * towards a planet with index destination, then it must jump next to a
     * planet with index of the returned value.
     *
     * @param current ship is in orbit of this planet
     * @param destination ship wants to go towards this planet
     * @return ship must jump to this planet
     */
    public int nextInRoutingTable(int current, int destination) {
        return routing_table[current][destination];
    }
}
