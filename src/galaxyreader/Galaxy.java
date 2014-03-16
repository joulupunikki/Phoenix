/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package galaxyreader;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import util.C;

/**
 * Class representing a galaxy object. Contains a starmap, planets, jump gates,
 * units, and structures.
 *
 * @author joulupunikki
 */
public class Galaxy implements Serializable {

    int version;
    int[][] map_tiles;
    int planet_count; //short
    int jump_gate_count; //short
    int structure_count;
    int unit_count;
    int units_in_file;
    List<Planet> planets;
    List<JumpGate> jump_gates;
    List<Unit> units;
    List<Structure> structures;
    // placeholder
    int[][] hex_buffer;

    /**
     * Creates a galaxy object. Reads version number, number of units and a star
     * map. Reads planets, jump gates, units and structures into lists.
     *
     * @param fc the file channel containing the galaxy.gal file.
     * @throws IOException
     */
    public Galaxy(FileChannel fc) throws IOException {
//        C.print();
        Counter count = new Counter();

        version = GalaxyReader.readInt(fc, count.getSet(4));

        units_in_file = GalaxyReader.readInt(fc, count.getSet(4));

        map_tiles = new int[C.STAR_MAP_HEIGHT][C.STAR_MAP_WIDTH];

        for (int i = 0; i < C.STAR_MAP_HEIGHT; i++) {
            for (int j = 0; j < C.STAR_MAP_WIDTH; j++) {

                map_tiles[i][j] = GalaxyReader.readInt(fc, count.getSet(4));

            }
        }
        //count.getSet(2);
        short section = GalaxyReader.readShort(fc, count.getSet(2));
        int index = 0;

        planets = new LinkedList<>();
        while (section != C.END_OF_SECTION) {
            planets.add(new Planet(fc, count, this, index++));
            section = GalaxyReader.readShort(fc, count.getSet(2));

            if (index > C.MAX_PLANETS) {
                System.out.println("Error reading galaxy file, too many planets: " + index);
                System.exit(1);
            }
        }

        for (int i = 0; i < planets.size(); i++) {

            if (i == 14) {
//                planets.get(i).print();
                hex_buffer = planets.get(i).getBuffer();

//                System.out.println("index :" + i);
            }
        }

        index = 0;
        section = GalaxyReader.readShort(fc, count.getSet(2));
        jump_gates = new LinkedList<>();
        while (section != C.END_OF_SECTION) {

            jump_gates.add(new JumpGate(fc, count));

            section = GalaxyReader.readShort(fc, count.getSet(2));

            if (index > C.MAX_JUMP_GATES) {
                System.out.println("Error reading galaxy file, too many jump gates: " + index);
                System.exit(1);
            }
            index++;

        }

        section = GalaxyReader.readShort(fc, count.getSet(2));
        units = new LinkedList<>();
        index = 0;
        while (section != C.END_OF_SECTION) {
            units.add(new Unit(fc, count));

            section = GalaxyReader.readShort(fc, count.getSet(2));

            if (index > C.MAX_UNITS) {
                System.out.println("Error reading galaxy file, too many units: " + index);
                System.exit(1);
            }
            index++;
        }

//        for (Unit e: units) {
//
//            e.print();
//          
//        }
        section = GalaxyReader.readShort(fc, count.getSet(2));
        structures = new LinkedList<>();
        index = 0;
        while (section != C.END_OF_SECTION) {
            structures.add(new Structure(fc, count));

            section = GalaxyReader.readShort(fc, count.getSet(2));

            if (index > C.MAX_STRUCTURES) {
                System.out.println("Error reading galaxy file, too many structures: " + index);
                System.exit(1);
            }
            index++;

        }
    }

    public List<Planet> getPlanets() {
        return planets;
    }

    public List<JumpGate> getJumpGates() {
        return jump_gates;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public List<Structure> getStructures() {
        return structures;
    }

    public int[][] getBuffer() {
        return hex_buffer;
    }

    public int[][] getPlanetMap(int planet_nr) {
        if (planet_nr > planets.size() || planet_nr < 0) {
            System.err.println("Galaxy.getPlanetMap wrong planet number: " + planet_nr);
        }

        return planets.get(planet_nr).getMap();
    }

//    public Planet getPlanet(int planet_nr) {
//        
//    }
    /**
     * Prints info on the galaxy object, version number, number of units and the
     * star map. For debugging purposes.
     */
    public void print() {
        System.out.println("version:       " + version);
        System.out.println("units in file: " + units_in_file);
        for (int i = 0; i < C.STAR_MAP_HEIGHT; i++) {
            for (int j = 0; j < C.STAR_MAP_WIDTH; j++) {
                System.out.print(map_tiles[i][j]);
            }
            System.out.println();
        }
    }

    public static Galaxy loadGalaxy(String filename) {

        Galaxy galaxy = null;
        Path path = FileSystems.getDefault().getPath(filename);
        try (FileChannel fc = (FileChannel.open(path))) {

            galaxy = new Galaxy(fc);

        } catch (IOException e) {
            System.err.println(e);
            System.out.println(e);
        }

        return galaxy;
    }
}
