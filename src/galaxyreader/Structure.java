/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package galaxyreader;

import dat.UnitType;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import util.C;

/**
 * A class representing a structure object.
 *
 * @author joulupunikki
 */
public class Structure implements Serializable {

    public int p_idx;      //short
    public int x;       //short
    public int y;     //short 
    public int type;       //short
    public int owner;      //short 
    int prev_owner;  //short 
    int prod_ruin_type; //union short/short
    int prod_info;  //union short/short
    public int turns_left;  //short 
    int city_info;        //union short/short/short
    int prev_type;           //short 
    int unit_loyalty;        //short 
    int loyalty;   //short 
    int stack_info;      //union short/short
    int used_unitt_lvl;    //short
    int tech_type; //short
    int health; //short
    int sect; //short
    int Flags; //UINT
    int unit_health; //short 
    int temp_count;  //short 
    int temp_count2; //short 
    int temp_count3; //short 
    int Damage;     //short 
    public static ArrayList<ArrayList<int[]>> can_build;
    public LinkedList<int[]> build_queue;
    boolean on_hold;

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
        prev_owner = GalaxyReader.readShort(fc, count.getSet(2));

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
        build_queue = new LinkedList<>();
        on_hold = false;
    }

//    /**
//     * Tries to start unit building. Will check for existence of input unit,
//     * TODO resources and TODO technologies
//     *
//     * @param unit_type
//     * @param unit_types
//     */
//    public void tryToBuild(int[] unit_type, UnitType[][] unit_types) {
//        int input = unit_types[unit_type[0]][unit_types[1]].unit;
//        if (input > -1) {
//            
//        }
//    }

    public void addToQueue(int[] unit_type, UnitType[][] unit_types) {
        if (build_queue.isEmpty()) {
            turns_left = unit_types[unit_type[0]][unit_type[1]].turns_2_bld;
//            tryToBuild(unit_type, unit_types);
        }
        build_queue.add(unit_type);
    }

    public void removeFromQueue(int index, UnitType[][] unit_types) {
        build_queue.remove(index);
        if (index == 0 && !build_queue.isEmpty()) {
            int[] u = build_queue.getFirst();
            turns_left = unit_types[u[0]][u[1]].turns_2_bld;
        }
    }

    public Unit buildUnits(UnitType[][] unit_types) {
        Unit unit = null;
        turns_left--;
        if (turns_left == 0) {
            unit = new Unit(p_idx, x, y, owner);
            int[] u_type = build_queue.getFirst();
            unit.type = u_type[0];
            unit.t_lvl = u_type[1];
            unit.move_points = unit_types[u_type[0]][u_type[1]].move_pts;
            unit.move_type = unit_types[u_type[0]][u_type[1]].move_type;
            unit.type_data = unit_types[u_type[0]][u_type[1]];
            removeFromQueue(0, unit_types);
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

    public static void setCanBuild(UnitType[][] unit_types) {
        can_build = new ArrayList<>();
        can_build.ensureCapacity(C.CITY_TYPES);
        for (int i = 0; i < C.CITY_TYPES; i++) {
            can_build.add(new ArrayList<int[]>());
        }

        for (int i = 0; i < unit_types.length; i++) {
            for (int j = 0; j < unit_types[i].length; j++) {
                if (unit_types[i][j] == null) {
                    continue;
                }
                int building = unit_types[i][j].bldgs;
                if (building > -1) {
                    int[] unit_type = new int[2];
                    unit_type[0] = i;
                    unit_type[1] = j;
                    if (building < 99) {
                        can_build.get(building).add(unit_type);
                    } else if (building == 99) {
                        for (int k = 0; k < can_build.size(); k++) {
                            can_build.get(k).add(unit_type);
                        }
                    }
                }

            }

        }
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
