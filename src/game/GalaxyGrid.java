/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import galaxyreader.Galaxy;
import galaxyreader.Planet;
import galaxyreader.Unit;
import java.io.Serializable;
import java.util.List;
import util.C;

/**
 *
 * @author joulupunikki
 */
public class GalaxyGrid implements Serializable {

    private Square[][] galaxy_grid;

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
}
