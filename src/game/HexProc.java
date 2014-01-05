/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

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

    private int faction;
    private Hex hex;

    // variables for spotting
    private int spotting;
    private List<Unit> stack;

    public void proc(Hex hex, int range, int oper) {
        switch (oper) {
            case C.INIT_SPOT:
                initSpot(hex);
                break;
            case C.SPOT:
//                spot(h1, h2, stack, range, faction);
                break;
            default:
                throw new AssertionError();
        }
    }

    public void initSpot(Hex hex) {
        hex.spot(faction);
        List<Unit> stack = hex.getStack();
        if (stack.isEmpty()) {
            return;
        }
        StackIterator iter = new StackIterator(stack);
        Unit unit = iter.next();
        while(unit != null) {
            unit.spotted[faction] = true;
            unit = iter.next();
        }
    }
    
    public void spot(Hex h1, Hex h2, List<Unit> stack, int range, int faction) {
        List<Unit> stack_b = h2.getStack();
        if (stack_b.isEmpty() || stack_b.get(0).owner == stack.get(0).owner) {
            return;
        }
        int spotting = 0;
        StackIterator iter = new StackIterator(stack);
        Unit unit = iter.next();
        while (unit != null) {
            if (spotting < unit.type_data.spot) {
                spotting = unit.type_data.spot;
            }
        }
    }

    public void initSpotProc(Hex hex) {
        List<Unit> stack = hex.getStack();
        Structure struct = hex.getStructure();
        if (stack.isEmpty() && struct == null) {
            return;
        }
        int range = 0;
        if(struct != null) {
            range = 5;
            this.faction = struct.owner;
        }
        
        if (!stack.isEmpty()) {

            this.faction = stack.get(0).owner;
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
    }

    public void spotProc(Hex hex, List<Unit> stack) {

        int spotting = 0;
        StackIterator iter = new StackIterator(stack);
        Unit unit = iter.next();
        while (unit != null) {
            if (spotting < unit.type_data.spot) {
                spotting = unit.type_data.spot;
            }
        }
        if (spotting == 0) {
            return;
        }
        int range = Unit.spotRange(spotting);
        this.spotting = spotting;
        this.stack = stack;
//        hexProc(hex, null, )
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
            for (int j = 1; j <= range && circ.getN(C.NORTHWEST) != null; j++) {
                circ = circ.getN(C.NORTHWEST);
                proc(circ, i, oper);
            }
            circ = radial;
            for (int j = 1; j <= range && circ.getN(C.SOUTH) != null; j++) {
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
            for (int j = 1; j <= range && circ.getN(C.SOUTHWEST) != null; j++) {
                circ = circ.getN(C.SOUTHWEST);
                proc(circ, i, oper);
            }
            circ = radial;
            for (int j = 1; j <= range && circ.getN(C.NORTH) != null; j++) {
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
            for (int j = 1; j <= range && circ.getN(C.SOUTHEAST) != null; j++) {
                circ = circ.getN(C.SOUTHEAST);
                proc(circ, i, oper);
            }
            circ = radial;
            for (int j = 1; j <= range && circ.getN(C.NORTH) != null; j++) {
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
            for (int j = 1; j <= range && circ.getN(C.NORTHEAST) != null; j++) {
                circ = circ.getN(C.NORTHEAST);
                proc(circ, i, oper);
            }
            circ = radial;
            for (int j = 1; j <= range && circ.getN(C.SOUTH) != null; j++) {
                circ = circ.getN(C.SOUTH);
                proc(circ, i, oper);
            }
            radial = radial.getN(C.NORTHWEST);
        }
    }

}
