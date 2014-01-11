/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import dat.UnitSpot;
import galaxyreader.Planet;
import galaxyreader.Structure;
import galaxyreader.Unit;
import java.io.Serializable;
import java.util.List;
import util.C;
import util.StackIterator;

/**
 * Contains procedures where something is done or calculated based on distance
 * between two hexes, usually a central hex and surrounding hexes.
 *
 * @author joulupunikki
 */
public class HexProc implements Serializable {

    private Game game;
    private int faction_a;
    private Hex hex;

    // variables for spotting
    private int spotting_a;
    private List<Unit> stack_a;
    private int max_spot_range;
    private Planet planet;

    public HexProc(Game game) {
        this.game = game;
    }

    public void proc(Hex hex, int range, int oper) {
        switch (oper) {
            case C.INIT_SPOT:
                initSpot(hex);
                break;
            case C.SPOT:
                spot(hex, range);
                break;
            default:
                throw new AssertionError();
        }
    }

    public void initSpot(Hex hex) {
        hex.spot(faction_a);
        List<Unit> stack = hex.getStack();
        if (stack.isEmpty()) {
            return;
        }
        StackIterator iter = new StackIterator(stack);
        Unit unit = iter.next();
        while (unit != null) {
            unit.spotted[faction_a] = true;
            unit = iter.next();
        }
    }

    public void spot(Hex hex, int range) {
//        int faction_a = this.stack_a.get(0).owner;
        int range_a = Unit.spotRange(this.spotting_a);
        if (range_a >= range) {
            hex.spot(faction_a);
        }
        List<Unit> stack_b = hex.getStack();
        if (stack_b.isEmpty() || stack_b.get(0).owner == stack_a.get(0).owner) {
            return;
        }
        int spotting_b = 0;
        StackIterator iter = new StackIterator(stack_b);
        Unit unit = iter.next();
        while (unit != null) {
            if (spotting_b < unit.type_data.spot) {
                spotting_b = unit.type_data.spot;
            }
            unit = iter.next();
        }

        if (range_a >= range) {
            spotStack(range, spotting_a, faction_a, stack_b, hex);
//            int final_spot = UnitSpot.finalSpotting(this.spotting_a, range);
////            double camo_mul = 1;
////            boolean[] terrain = hex.getTerrain();
//            for (Unit unit1 : stack_b) {
//                if (unit1.spotted[faction_a]) {
//                    continue;
//                }
//                if (final_spot >= unit1.type_data.camo) {
//                    unit1.spotted[faction_a] = true;
//                }
//
//            }
        }

        int faction_b = stack_b.get(0).owner;
        int range_b = Unit.spotRange(spotting_b);
        if (range_b >= range) {
            spotStack(range, spotting_b, faction_b, stack_a, this.hex);
        }

    }

    public void spotStack(int range, int spotting, int faction, List<Unit> stack, Hex hex) {
        int final_spot = UnitSpot.finalSpotting(spotting, range);
        for (Unit unit1 : stack) {
            if (unit1.spotted[faction]) {
                continue;
            }
            double camo_mod = 1.0;
            double[][][] unit_spot = game.getResources().getUnitSpot();
            boolean[] terrain = hex.getTerrain();
            for (int i = 0; i < terrain.length; i++) {
                if (terrain[i]) {
                    camo_mod *= (unit_spot[i][this.planet.tile_set_type][unit1.move_type.ordinal()] / 2);
                    if (unit1.type == 49) {
                        System.out.println("i = " + i);
                        System.out.println("camo_mod = " + camo_mod);
                    }
                }

            }
            if (hex.getStructure() != null) {
                camo_mod *= (unit_spot[C.STRUCTURE][this.planet.tile_set_type][unit1.move_type.ordinal()] / 2);
                if (unit1.type == 49) {
                    System.out.println("structure");
                    System.out.println("camo_mod = " + camo_mod);
                }
            }
            int final_camo = (int) (unit1.type_data.camo * camo_mod);
            if (final_spot >= final_camo) {
                unit1.spotted[faction] = true;
            }

        }
    }

    public void initSpotProc(Hex hex, Planet planet) {
        List<Unit> stack = hex.getStack();
        Structure struct = hex.getStructure();
        if (stack.isEmpty() && struct == null) {
            return;
        }
        int range = 0;
        if (struct != null) {
            range = 5;
            this.faction_a = struct.owner;
        }

        if (!stack.isEmpty()) {

            this.faction_a = stack.get(0).owner;
            int spotting = 0;
            StackIterator iter = new StackIterator(stack);
            Unit unit = iter.next();
            while (unit != null) {
                if (unit.type_data.spot > spotting) {
                    spotting = unit.type_data.spot;
                }
                unit = iter.next();
            }
            int tmp = Unit.spotRange(spotting);
            range = range > tmp ? range : tmp;
        }

        hexProc(hex, range, C.INIT_SPOT);
        planet.spotted[this.faction_a] = true;
    }

    public void spotProc(Hex hex, List<Unit> stack) {

        int spotting = 0;
        StackIterator iter = new StackIterator(stack);
        Unit unit = iter.next();
        while (unit != null) {
            if (spotting < unit.type_data.spot) {
                spotting = unit.type_data.spot;
            }
            unit = iter.next();
        }
//        if (spotting == 0) {
//            return;
//        }
//        int range = Unit.spotRange(spotting);
        this.planet = game.getPlanet(stack.get(0).p_idx);
        this.faction_a = stack.get(0).owner;
        this.spotting_a = spotting;
        this.stack_a = stack;
        this.hex = hex;
        hexProc(hex, this.max_spot_range, C.SPOT);
    }

    public void hexProc(Hex hex, int range, int oper) {
        proc(hex, 0, oper);
        Hex radial = hex;
        radial = radial.getN(C.NORTHEAST);
        for (int i = 1; i <= range; i++) {
            if (radial == null) {
                break;
            }
            Hex circ = radial;
            proc(circ, i, oper);
            for (int j = 1; j <= i && circ.getN(C.NORTHWEST) != null; j++) {
                circ = circ.getN(C.NORTHWEST);
                proc(circ, i, oper);
            }
            circ = radial;
            for (int j = 1; j <= i && circ.getN(C.SOUTH) != null; j++) {
                circ = circ.getN(C.SOUTH);
                proc(circ, i, oper);
            }
            radial = radial.getN(C.NORTHEAST);
        }
        radial = hex;
        radial = radial.getN(C.SOUTHEAST);
        for (int i = 1; i <= range; i++) {
            if (radial == null) {
                break;
            }
            Hex circ = radial;
            proc(circ, i, oper);
            for (int j = 1; j <= i && circ.getN(C.SOUTHWEST) != null; j++) {
                circ = circ.getN(C.SOUTHWEST);
                proc(circ, i, oper);
            }
            circ = radial;
            for (int j = 1; j <= i && circ.getN(C.NORTH) != null; j++) {
                circ = circ.getN(C.NORTH);
                proc(circ, i, oper);
            }
            radial = radial.getN(C.SOUTHEAST);
        }
        radial = hex;
        radial = radial.getN(C.SOUTHWEST);
        for (int i = 1; i <= range; i++) {
            if (radial == null) {
                break;
            }
            Hex circ = radial;
            proc(circ, i, oper);
            for (int j = 1; j <= i && circ.getN(C.SOUTHEAST) != null; j++) {
                circ = circ.getN(C.SOUTHEAST);
                proc(circ, i, oper);
            }
            circ = radial;
            for (int j = 1; j <= i && circ.getN(C.NORTH) != null; j++) {
                circ = circ.getN(C.NORTH);
                proc(circ, i, oper);
            }
            radial = radial.getN(C.SOUTHWEST);
        }
        radial = hex;
        radial = radial.getN(C.NORTHWEST);
        for (int i = 1; i <= range; i++) {
            if (radial == null) {
                break;
            }
            Hex circ = radial;
            proc(circ, i, oper);
            for (int j = 1; j <= i && circ.getN(C.NORTHEAST) != null; j++) {
                circ = circ.getN(C.NORTHEAST);
                proc(circ, i, oper);
            }
            circ = radial;
            for (int j = 1; j <= i && circ.getN(C.SOUTH) != null; j++) {
                circ = circ.getN(C.SOUTH);
                proc(circ, i, oper);
            }
            radial = radial.getN(C.NORTHWEST);
        }
    }

    public void setMaxSpotRange(int range) {
        max_spot_range = range;
    }

}
