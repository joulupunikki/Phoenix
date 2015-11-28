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

import galaxyreader.Structure;
import galaxyreader.Unit;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import util.C;

/**
 *
 * @author joulupunikki
 */
public class Hex implements Comparable<Hex>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Hex[] neighbours;
    private boolean[] terrain = new boolean[12];
    private boolean[] visible = new boolean[14];
    // for Dijkstra
    private boolean visited;
    private int[] move_cost = new int[C.MoveType.values().length];
    private int min_dist;
    private Hex previous;

    private int flags;
    private int x;
    private int y;
    private List<Unit> stack;
    private Structure city;
    private Structure resource = null;

    //***** ai support data
    private int land_nr = -1;
    private int hex_idx;
    private byte recon_time = 127;

    public Hex() {
        neighbours = new Hex[6];
        flags = 0;
        stack = new LinkedList<>();
        city = null;
    }

    public Hex(int x, int y) {

        neighbours = new Hex[6];
        flags = 0;
        this.x = x;
        this.y = y;
        this.hex_idx = x + y * C.PLANET_MAP_WIDTH;
        stack = new LinkedList<>();
//        stack = null;
        city = null;

    }

    public void initVisibility() {
        for (int i = 0; i < visible.length; i++) {
            visible[i] = false;
        }
    }

    public boolean isSpotted(int faction) {
        return visible[faction];
    }

    public void spot(int faction) {
        visible[faction] = true;
    }

    public Hex getNeighbour(int i) {
        return neighbours[i];
    }

    public Hex[] getNeighbours() {
        return neighbours;
    }

    public int compareTo(Hex h) {
        return Integer.compare(getMinDist(), h.getMinDist());
    }

    public void setTerrain(boolean[] terrain) {
        this.terrain = terrain;
    }

    public boolean[] getTerrain() {
        return terrain;
    }

    public boolean getTerrain(int type) {
        return terrain[type];
    }

    public List<Unit> getStack() {
        return stack;
    }

    public void addStack(List<Unit> stack) {
        this.stack.addAll(stack);
    }

    public void addUnit(Unit u) {
        this.stack.add(u);
    }

    public void minusStack(List<Unit> stack) {
        this.stack.removeAll(stack);
    }

    public void placeUnit(Unit e) {
//        if (stack == null) {
//            stack = new LinkedList<>();
//        }

        stack.add(e);

    }

    public void placeResource(Structure e) {
        resource = e;
    }

    public Structure getResource() {
        return resource;
    }

    public void placeStructure(Structure e) {
        city = e;
    }

    public Structure getStructure() {
        return city;
    }

    public void setN(Hex h, int direction) {
        neighbours[direction] = h;
    }

    public Hex getN(int direction) {
        return neighbours[direction];
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void print() {
        System.out.println("Hex (x, y): " + x + ", " + y);
    }

    /**
     * @return the min_dist
     */
    public int getMinDist() {
        return min_dist;
    }

    /**
     * @param min_dist the min_dist to set
     */
    public void setMinDist(int min_dist) {
        this.min_dist = min_dist;
    }

    /**
     * @return the visited
     */
    public boolean isVisited() {
        return visited;
    }

    /**
     * @param visited the visited to set
     */
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    /**
     * @return the move_cost
     */
    public int getMoveCost(int move_type) {
        return move_cost[move_type];
    }

    /**
     * @param move_cost the move_cost to set
     */
    public void setMoveCost(int move_cost, int move_type) {
        this.move_cost[move_type] = move_cost;
    }

    /**
     * @return the previous
     */
    public Hex getPrevious() {
        return previous;
    }

    /**
     * @param previous the previous to set
     */
    public void setPrevious(Hex previous) {
        this.previous = previous;
    }

    /**
     * Game state printout method, prints the contents of a hex. City and units
     * in stack.
     *
     * @param file
     */
    public void record(PrintWriter pw) {
        if (city != null) {
            city.record(pw);
        }
        for (Unit unit : stack) {
            unit.record(pw);
        }
    }

    /**
     * @return the land_nr
     */
    public int getLandNr() {
        return land_nr;
    }

    /**
     * @param land_nr the land_nr to set
     */
    public void setLandNr(int land_nr) {
        this.land_nr = land_nr;
    }

    /**
     * @return the hex_idx
     */
    public int getHexIdx() {
        return hex_idx;
    }
}
