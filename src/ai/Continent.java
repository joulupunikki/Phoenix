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

import galaxyreader.Structure;
import galaxyreader.Unit;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import util.C;

/**
 * AI support structure representing a continent. Holds lists of assets on a
 * continent for each faction.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
class Continent implements Serializable {

    private static final long serialVersionUID = 1L;

    List<List<Structure>> structures;
    List<List<Unit>> units;
    List<List<Unit>> ground_troops;
    public Continent() {
        structures = new ArrayList<>(C.NR_FACTIONS);
        units = new ArrayList<>(C.NR_FACTIONS);
        ground_troops = new ArrayList<>(C.NR_FACTIONS);
        for (int i = 0; i < C.NR_FACTIONS; i++) {
            structures.add(new LinkedList<>());
            units.add(new LinkedList<>());
            ground_troops.add(new LinkedList<>());
        }
    }

    public void add(Structure s) {
        structures.get(s.owner).add(s);
    }

    public void add(Unit u) {
        units.get(u.owner).add(u);
        if (AI.isGroundTroop(u)) {
            ground_troops.get(u.owner).add(u);
        }
    }

    public AssetCount getAssetCount(int faction) {
        return new AssetCount(structures.get(faction).size(), units.get(faction).size(), ground_troops.get(faction).size());
    }

    void clear() {
        for (int i = 0; i < C.NR_FACTIONS; i++) {
            structures.get(i).clear();
            units.get(i).clear();
            ground_troops.get(i).clear();
        }

    }

}
