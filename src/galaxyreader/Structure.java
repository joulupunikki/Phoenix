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

import dat.UnitType;
import game.Game;
import game.Hex;
import game.Message;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import util.C;
import util.Util;

/**
 * A class representing a structure object.
 *
 * @author joulupunikki
 */
public class Structure implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public int p_idx;      //short
    public int x;       //short
    public int y;     //short 
    public int type;       //short
    public int owner;      //short 
    public int prev_owner;  //short
    int prod_ruin_type; //union short/short
    int prod_info;  //union short/short
    public int turns_left;  //short 
    int city_info;        //union short/short/short
    int prev_type;           //short 
    int unit_loyalty;        //short 
    public int loyalty;   //short
    int stack_info;      //union short/short
    int used_unitt_lvl;    //short
    int tech_type; //short
    public int health; //short - RSW
    int sect; //short
    int Flags; //UINT
    int unit_health; //short 
    int temp_count;  //short 
    int temp_count2; //short 
    int temp_count3; //short 
    int Damage;     //short 

    public int turns_starving;    // RSW
    public LinkedList<int[]> build_queue;
    public boolean on_hold_no_res; // queue on hold because of no resources or input units
    Unit upgraded; // input unit used in building

    /**
     * Creates a structure object. Reads in coordinates, owner, loyalty and
     * other fields.
     *
     * @param fc the FileChannel which contains the file from which the data is
     * read.
     * @param count the Counter containing the position from which the data is
     * read.
     * @throws IOException
     */
    public Structure(FileChannel fc, Counter count) throws IOException {

        count.getSet(-2);

        p_idx = GalaxyReader.readShort(fc, count.getSet(2));
        x = GalaxyReader.readShort(fc, count.getSet(2));
        y = GalaxyReader.readShort(fc, count.getSet(2));

        type = GalaxyReader.readShort(fc, count.getSet(2));
        owner = GalaxyReader.readShort(fc, count.getSet(2));
        GalaxyReader.readShort(fc, count.getSet(2));
        prev_owner = owner;
        prod_ruin_type = GalaxyReader.readShort(fc, count.getSet(2));
        prod_info = GalaxyReader.readShort(fc, count.getSet(2));
        turns_left = GalaxyReader.readShort(fc, count.getSet(2));
        city_info = GalaxyReader.readShort(fc, count.getSet(2));
        prev_type = GalaxyReader.readShort(fc, count.getSet(2));
        unit_loyalty = GalaxyReader.readShort(fc, count.getSet(2));

        loyalty = GalaxyReader.readShort(fc, count.getSet(2));

        stack_info = GalaxyReader.readShort(fc, count.getSet(2));
        used_unitt_lvl = GalaxyReader.readShort(fc, count.getSet(2));

        tech_type = GalaxyReader.readShort(fc, count.getSet(2));

        health = GalaxyReader.readShort(fc, count.getSet(2));
        sect = GalaxyReader.readShort(fc, count.getSet(2));
        Flags = GalaxyReader.readInt(fc, count.getSet(4));

        unit_health = 0;
        temp_count = 0;
        temp_count2 = 0;
        temp_count3 = 0;
        Damage = 0;
        turns_starving = 0;    // RSW
        build_queue = new LinkedList<>();
        on_hold_no_res = false;
        upgraded = null;
    }

    /**
     * Creates a skeleton city, for calculating resource production in build
     * city panel.
     *
     * @param type
     * @param p_idx
     * @param x
     * @param y
     */
    public Structure(int type, int p_idx, int x, int y) {
        this.type = type;
        this.p_idx = p_idx;
        this.x = x;
        this.y = y;
    }

    public Structure(int owner, int prev_owner, int type, int p_idx, int x, int y, int health) {
        this.owner = owner;
        this.prev_owner = prev_owner;
        this.type = type;
        this.p_idx = p_idx;
        this.x = x;
        this.y = y;
        this.health = health;
        this.build_queue = new LinkedList<>();
    }

    public static Unit findInputUnit(int[] unit_type, UnitType[][] unit_types, Game game, Structure city) {
        int input = unit_types[unit_type[0]][unit_type[1]].unit;
        Unit input_unit = null;
        // need to use this.p_idx building city may not be on current planet
        List<Unit> stack = game.getPlanetGrid(city.p_idx).getHex(city.x, city.y).getStack();
        if (input > -1) {
            for (Unit unit : Util.xS(stack)) {
                if (unit.type == input && unit.t_lvl == 0) {
                    input_unit = unit;
                    break;
                }
            }

        }
        return input_unit;
    }

    /**
     * Tries to start unit building. Will check for existence of input unit and
     * resources, if these are not found will put queue on hold else will stash
     * away input unit and subtract resources and start building (not put queue
     * on hold).
     *
     * @param unit_type
     * @param unit_types
     * @param game
     */
    public void tryToStartBuild(int[] unit_type, UnitType[][] unit_types, Game game) {
        int input = unit_types[unit_type[0]][unit_type[1]].unit;
        //System.out.println("input = " + input);
        boolean input_found = false;
        Unit input_unit = null;
        // need to use this.p_idx building city may not be on current planet
        List<Unit> stack = game.getPlanetGrid(this.p_idx).getHex(this.x, this.y).getStack();
        boolean resources_found = false;
        if (input > -1) {
            input_unit = findInputUnit(unit_type, unit_types, game, this);
//            for (Unit unit : Util.xS(stack)) {
//                if (unit.type == input && unit.t_lvl == 0) {
//                    input_found = true;
//                    input_unit = unit;
//                    break;
//                }
//            }

        }

        //check for existence of resources
        resources_found = checkForResources(unit_type, game);

        on_hold_no_res = false;
        if ((input > -1 && input_unit == null) || !resources_found) {
            on_hold_no_res = true;
            return;
        }
        //subtract resources
        subtractResources(unit_type, game);
        if (input > -1) {

            if (input_unit.carrier != null) {
                input_unit.carrier.disembark(input_unit);
            } else {
                stack.remove(input_unit);
            }
            if (!input_unit.cargo_list.isEmpty()) {
                for (Unit unit : input_unit.cargo_list) {
                    input_unit.disembark(unit);
                    stack.add(unit);
                }
            }
            game.deleteUnitNotInCombat(input_unit);
            upgraded = input_unit;
        }
    }

    public void subtractResources(int[] unit, Game game) {
        UnitType[][] unit_types = game.getUnitTypes();
        int[] res_needed = game.getUnitTypes()[unit[0]][unit[1]].reqd_res;
        if (!game.getResources().consumeResources(this.p_idx, this.owner, res_needed)) {
            throw new AssertionError();
        }
    }

    public boolean checkForResources(int[] unit, Game game) {
        boolean ret_val = true;
        UnitType[][] unit_types = game.getUnitTypes();
        int[] res_needed = game.getUnitTypes()[unit[0]][unit[1]].reqd_res;
        int[] res_avail = game.getResources().getResourcesAvailable(this.p_idx, this.owner);
        for (int i = 0; i < C.REQUIRED_RESOURCES.length; i++) {
            if (res_avail[C.REQUIRED_RESOURCES[i]] - res_needed[C.REQUIRED_RESOURCES[i]] < 0) {
                ret_val = false;
                break;
            }
        }
        return ret_val;
    }

    /**
     *
     * @param unit_type the value of unit_type
     * @param unit_types the value of unit_types
     * @param game the value of game
     */
    public void addToQueue(int[] unit_type, UnitType[][] unit_types, Game game) {
        if (build_queue.isEmpty()) {
            turns_left = unit_types[unit_type[0]][unit_type[1]].turns_2_bld;
            tryToStartBuild(unit_type, unit_types, game);
        }
        build_queue.add(unit_type);

    }

    /**
     * Removes unit form build queue. If index in queue was 0 check if turns
     *
     * @param index the value of index
     * @param unit_types the value of unit_types
     * @param game the value of game
     */
    public void removeFromQueue(int index, UnitType[][] unit_types, Game game) {
        int[] removed = build_queue.remove(index);
        if (index == 0) {
            // turns left == 0 if unit just built
            if (turns_left != 0) {
                if (!on_hold_no_res) {
                    //return resources
                    returnResources(game, removed);
                    //try to return input unit if any
                    if (upgraded != null) {
                        System.out.println("game = " + game);
                        Hex hex = game.findRoom(this, upgraded.move_type);
                        System.out.println("hex = " + hex);
                        if (hex != null) {
                            Unit unit = game.createUnitInHex(p_idx, hex.getX(),
                                    hex.getY(), owner, prev_owner, upgraded.type, upgraded.t_lvl,
                                    upgraded.res_relic, upgraded.amount);
                            unit.health = upgraded.health;
//                            List<Unit> stack = hex.getStack();
//                            stack.add(upgraded);
//                            game.getUnits().add(upgraded);
//                            game.getUnmovedUnits().add(upgraded);
//                            game.unSpot(stack);
//                            game.getHexProc().spotProc(hex, stack);
                        } else {
                            game.getFaction(game.getTurn()).addMessage(new Message(null, C.Msg.CITY_FULL, game.getYear(), this));
                        }
                        upgraded = null;
                    }
                } else {
                    on_hold_no_res = false;
                }
            }

            if (!build_queue.isEmpty()) {
                int[] u = build_queue.getFirst();
                turns_left = unit_types[u[0]][u[1]].turns_2_bld;
                tryToStartBuild(u, unit_types, game);
            }
        }
    }

    public void returnResources(Game game, int[] unit) {
        int[] amount = game.getUnitTypes()[unit[0]][unit[1]].reqd_res;
        game.getResources().addResourcesToHex(this.p_idx, this.x, this.y, this.owner, this.prev_owner, amount);
    }

    /**
     *
     * @param unit_types
     * @param game
     * @param hex
     * @return null
     */
    public Unit buildUnits(UnitType[][] unit_types, Game game, Hex hex) {
        Unit unit = null;
        turns_left--;
        if (turns_left == 0) {

//            unit = new Unit(p_idx, x, y, owner);
            int[] u_type = build_queue.getFirst();
            game.createUnitInHex(p_idx, hex.getX(), hex.getY(), owner, prev_owner, u_type[0], u_type[1], 0, 0);
//            unit.type = u_type[0];
//            unit.t_lvl = u_type[1];
//            unit.move_points = unit_types[u_type[0]][u_type[1]].move_pts;
//            unit.move_type = unit_types[u_type[0]][u_type[1]].move_type;
//            unit.type_data = unit_types[u_type[0]][u_type[1]];
//            upgraded = null;
//            hex.addUnit(unit);
//            // called before resetUnmovedUnits();
//            game.getUnits().add(unit);
            removeFromQueue(0, unit_types, game);

        }
        return unit;
    }

    /**
     * Prints a structure object. Prints coordinates and owner. For debugging
     * purposes.
     */
    public void print() {
        System.out.println("p_idx: " + p_idx);
        System.out.println("x:     " + x);
        System.out.println("y:     " + y);
        System.out.println("owner: " + owner);
    }

    /**
     * Game state printout method, prints the Structure object. A CSV record is
     * produced of the Structure * object's identifying statistics.
     */
    public void record(File file) {
        Util.printString(file, "  "
                + p_idx + "," + x + "," + y + "," + loyalty + "," + owner + ","
                + prev_owner + "," + type + "," + health + "," + turns_starving
                + "," + turns_left);
    }

    public static String getName(int structure_nr) {
        String s = null;

        switch (structure_nr) {
            case C.PALACE:
                s = "Palace";
                break;
            case C.CHURCH:
                s = "Church";
                break;
            case C.MONASTERY:
                s = "Monastery";
                break;
            case C.FACTORY:
                s = "Factory";
                break;
            case C.AGORA:
                s = "Agora";
                break;
            case C.WETWARE:
                s = "Wetware";
                break;
            case C.ELECTRONICS:
                s = "Electronics";
                break;
            case C.HIVE:
                s = "Hive";
                break;
            case C.CERAMSTEEL:
                s = "Ceramsteel";
                break;
            case C.BIOPLANT:
                s = "Bioplant";
                break;
            case C.VAU_CITY:
                s = "Vau city";
                break;
            case C.CHEMICALS:
                s = "Chemicals";
                break;
            case C.CYCLOTRON:
                s = "Cyclotron";
                break;
            case C.FORT:
                s = "Fort";
                break;
            case C.STARPORT:
                s = "Starport";
                break;
            case C.RUINS:
                s = "Ruins";
                break;
            case C.ALIEN_RUINS:
                s = "Alien ruins";
                break;
            case C.SHIELD:
                s = "Shield";
                break;
            case C.MINE:
                s = "Mine";
                break;
            case C.WELL:
                s = "Well";
                break;
            case C.FUSORIUM:
                s = "Fusorium";
                break;
            case C.UNIVERSITY:
                s = "University";
                break;
            case C.HOSPITAL:
                s = "Hospital";
                break;
            case C.LAB:
                s = "Lab";
                break;
            case C.FARM:
                s = "Farm";
                break;
            case C.ARBORIUM:
                s = "Arborium";
                break;
            case C.TRACE:
                s = "Trace";
                break;
            case C.GEMS:
                s = "Gems";
                break;
            case C.EXOTICA:
                s = "Exotica";
                break;
            case C.FERTILE:
                s = "Fertile";
                break;
            case C.METAL:
                s = "Metal";
                break;
            case C.ENERGY:
                s = "Energy";
                break;
            default:
                throw new AssertionError();
        }

        return s;
    }

}
