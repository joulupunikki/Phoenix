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

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import util.C;
import util.Util;

/**
 * Class representing a galaxy object. Contains a starmap, planets, jump gates,
 * units, and structures.
 *
 * @author joulupunikki
 */
public class Galaxy implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
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
    public Galaxy(FileChannel fc) throws Exception {
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
                throw new Exception("Error reading galaxy file, too many planets: " + index);

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
                throw new Exception("Error reading galaxy file, too many jump gates: " + index);

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
                throw new Exception("Error reading galaxy file, too many units: " + index);

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
                throw new Exception("Error reading galaxy file, too many structures: " + index);

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
            e.printStackTrace();
            Util.logFFErrorAndExit(filename, -1);
        } catch (Exception e) {
            Util.logEx(null, e);
            System.exit(1);
        }

        return galaxy;
    }
}
