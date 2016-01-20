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
package ai;

import galaxyreader.Planet;
import game.Game;
import java.io.Serializable;
import java.util.LinkedHashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Holds the "galaxy map" of a faction. Has mapped planets in an index queryable
 * set and the unmapped planets one jump from a mapped planet in a returnable
 * set.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class KnownGalaxy implements Serializable {

    private static final Logger logger = LogManager.getLogger(KnownGalaxy.class);
    private static final long serialVersionUID = 1L;
    LinkedHashSet<Integer> mapped_planets;
    LinkedHashSet<Integer> edge_planets;

    public KnownGalaxy(Game game, int faction) {
        mapped_planets = new LinkedHashSet<>(game.getPlanets().size() * 2);
        edge_planets = new LinkedHashSet<>(game.getPlanets().size() * 2);
        String s_mapped = "";
        String s_edge = "";
        next_planet:
        for (Planet planet : game.getPlanets()) {
            if (planet.spotted[faction]) {
                mapped_planets.add(planet.index);
            } else {
                for (Planet neighbour : planet.neighbours) {
                    //prkl += neighbour.index + " ";
                    if (neighbour.spotted[faction]) {
                        edge_planets.add(planet.index);
                        continue next_planet;
                    }
                }
            }
        }
        for (Integer idx : mapped_planets) {
            s_mapped += game.getPlanets().get(idx).name + " ";
        }
        for (Integer idx : edge_planets) {
            s_edge += game.getPlanets().get(idx).name + " ";
        }
        logger.debug("  mapped: " + s_mapped + "edge: " + s_edge);
    }

    /**
     * Return true iff Plane p_idx is mapped.
     *
     * @param p_idx
     * @return
     */
    public boolean isMapped(int p_idx) {
        return mapped_planets.contains(p_idx);
    }

    /**
     * Return set of Planets 1 jump from a mapped planet.
     *
     * @return
     */
    public LinkedHashSet<Integer> getEdge() {
        return edge_planets;
    }
}
