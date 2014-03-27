/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dat;

import galaxyreader.Unit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import util.C;
import util.C.MoveType;
import util.Util;

/**
 *
 * @author joulupunikki
 */
public class UnitType implements Serializable {

    public int index;
    public int t_lvl;
    public String name;
    public int plural;
    public String abbrev;
    public MoveType move_type;
    public int move_pts;
    public int spot;
    public int camo;
    public int ag;
    public int armor;
    public int psy_def;
    public int water_acc;
    public int water_str;
    public int indirect_acc;
    public int indirect_str;
    public int air_acc;
    public int air_str;
    public int direct_acc;
    public int direct_str;
    public int close_acc;
    public int close_str;
    public int psy_acc;
    public int psy_str;
    public int ranged_sp_acc;
    public int ranged_sp_str;
    public int direct_sp_acc;
    public int direct_sp_str;
    public int close_sp_acc;
    public int close_sp_str;
    public int cargo;
    public int can_b_cargo;
    public int non_combat;
    public int crd_trn;
    public int cred;
    public int[] reqd_res;
//    public int food;
//    public int energy;
//    public int metal;
//    public int trac;
//    public int exot;
//    public int chems;
//    public int bio;
//    public int elec;
//    public int c_steel;
//    public int wet;
//    public int mono;
//    public int gems;
//    public int sing;
    public int unit;
    public int bldgs;
    public int turns_2_bld;
    public int[] reqd_tech;
    public int tax;
    public int flock;
    public int range;
    public boolean eat;
    public int rank;
    public int rop;

    public UnitType(String s, int index, int t_lvl) {

        this.index = index;
        this.t_lvl = t_lvl;
        /*
         * accepts alphanum, -, ' and .
         */
        Pattern unit_type = Pattern.compile("\"[0-9a-zA-Z ,\\(\\)\\[\\]\\!\\*\\-'\\.]+\"");

        Matcher m = unit_type.matcher(s);

        //skip "name"
        m.find();
        m.find();

        name = s.substring(m.start() + 1, m.end() - 1);
        Util.debugPrint("Name: " + name);
        System.out.println("name = " + name);
        m.find();

        plural = Integer.parseInt(s.substring(m.start() + 1, m.end() - 1));
        Util.debugPrint("Plural: " + plural);

        //skip "abbrev"
        m.find();
        m.find();

        abbrev = s.substring(m.start() + 1, m.end() - 1);
        Util.debugPrint("Abbrev: " + abbrev);

        //skip "stats"
        m.find();
        m.find();

        //last one is a big string of one text and dozens of integer values
        String stats = s.substring(m.start() + 1, m.end() - 1);
        Util.debugPrint("Stats: " + stats);

        Pattern stats_pattern = Pattern.compile("[0-9a-zA-Z]+");

        m = stats_pattern.matcher(stats);

        m.find();

        move_type = processMoveType(stats.substring(m.start(), m.end()));
        Util.debugPrint("MoveType: " + move_type);

        stats_pattern = Pattern.compile("-?[0-9]+");

        m = stats_pattern.matcher(stats);

        m.find();

        // m.find() is done in processIntVal
        move_pts = processIntVal(stats, m);
        spot = processIntVal(stats, m);
        camo = processIntVal(stats, m);
        ag = processIntVal(stats, m);
        armor = processIntVal(stats, m);
        psy_def = processIntVal(stats, m);
        water_acc = processIntVal(stats, m);
        water_str = processIntVal(stats, m);
        indirect_acc = processIntVal(stats, m);
        indirect_str = processIntVal(stats, m);
        air_acc = processIntVal(stats, m);
        air_str = processIntVal(stats, m);
        direct_acc = processIntVal(stats, m);
        direct_str = processIntVal(stats, m);
        close_acc = processIntVal(stats, m);
        close_str = processIntVal(stats, m);
        psy_acc = processIntVal(stats, m);
        psy_str = processIntVal(stats, m);
        ranged_sp_acc = processIntVal(stats, m);
        ranged_sp_str = processIntVal(stats, m);
        direct_sp_acc = processIntVal(stats, m);
        direct_sp_str = processIntVal(stats, m);
        close_sp_acc = processIntVal(stats, m);
        close_sp_str = processIntVal(stats, m);
        cargo = processIntVal(stats, m);

        can_b_cargo = processIntVal(stats, m);
        System.out.println("can_b_cargo = " + can_b_cargo);
        non_combat = processIntVal(stats, m);
        crd_trn = processIntVal(stats, m);
        cred = processIntVal(stats, m);
        reqd_res = new int[C.RES_TYPES];
        for (int i = 0; i < reqd_res.length; i++) {
            reqd_res[i] = processIntVal(stats, m);

        }
//        food = processIntVal(stats, m);
//        energy = processIntVal(stats, m);
//        metal = processIntVal(stats, m);
//        trac = processIntVal(stats, m);
//        exot = processIntVal(stats, m);
//        chems = processIntVal(stats, m);
//        bio = processIntVal(stats, m);
//        elec = processIntVal(stats, m);
//        c_steel = processIntVal(stats, m);
//        wet = processIntVal(stats, m);
//        mono = processIntVal(stats, m);
//        gems = processIntVal(stats, m);
//        sing = processIntVal(stats, m);
        unit = processIntVal(stats, m);
        bldgs = processIntVal(stats, m);
        turns_2_bld = processIntVal(stats, m);
        reqd_tech = new int[4];
        reqd_tech[0] = processIntVal(stats, m);
        reqd_tech[1] = processIntVal(stats, m);
        reqd_tech[2] = processIntVal(stats, m);
        reqd_tech[3] = processIntVal(stats, m);
        Util.debugPrint("Reqd_tech: " + reqd_tech[0] + " " + reqd_tech[1] + " " + reqd_tech[2] + " " + reqd_tech[3]);
        tax = processIntVal(stats, m);
        flock = processIntVal(stats, m);
        range = processIntVal(stats, m);
        if (processIntVal(stats, m) == 1) {
            eat = true;
        } else {
            eat = false;
        }
        Util.debugPrint("eat: " + eat);
        rank = processIntVal(stats, m);
        Util.debugPrint("rank: " + rank);
        rop = processIntVal(stats, m);
        Util.debugPrint("rop: " + rop);

    }

    public static int processIntVal(String s, Matcher m) {

        int ret_val = Integer.parseInt(s.substring(m.start(), m.end()));

        m.find();

        return ret_val;
    }

    public MoveType processMoveType(String s) {

        Util.debugPrint("Move type: " + s);

        MoveType move_type = C.MoveType.FOOT;

        if (s.equalsIgnoreCase("foot")) {
            move_type = C.MoveType.FOOT;
        }

        if (s.equalsIgnoreCase("wheel")) {
            move_type = C.MoveType.WHEEL;
        }

        if (s.equalsIgnoreCase("tread")) {
            move_type = C.MoveType.TREAD;
        }

        if (s.equalsIgnoreCase("crawler")) {
            move_type = C.MoveType.CRAWLER;
        }

        if (s.equalsIgnoreCase("naval")) {
            move_type = C.MoveType.NAVAL;
        }

        if (s.equalsIgnoreCase("water")) {
            move_type = C.MoveType.WHEEL;
        }

        if (s.equalsIgnoreCase("air")) {
            move_type = C.MoveType.AIR;
        }

        if (s.equalsIgnoreCase("hover")) {
            move_type = C.MoveType.HOVER;
        }

        if (s.equalsIgnoreCase("space")) {
            move_type = C.MoveType.SPACE;
        }

        if (s.equalsIgnoreCase("jump")) {
            move_type = C.MoveType.JUMP;
        }

        if (s.equalsIgnoreCase("lander")) {
            move_type = C.MoveType.LANDER;
        }

        return move_type;
    }

    public static void main(String[] args) {
        UnitType[][] unit_types = readUnitDat();

        for (int i = 0; i < unit_types.length; i++) {
            for (int j = 0; j < unit_types[0].length; j++) {
                if (unit_types[i][j] != null) {
                    Util.debugPrint("Unit type: " + i + " " + unit_types[i][j].eat + " " + unit_types[i][j].rank + " " + unit_types[i][j].rop);
                }
            }

        }
    }

    public static UnitType[][] readUnitDat() {

        String file_name = C.S_UNIT_DAT;
        UnitType[][] unit_types = new UnitType[C.UNIT_TYPES][C.UNIT_T_LVLS];
        int line_nr = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(file_name))) {
            String s = in.readLine();
            line_nr++;
            //true if between { and } false if between } and {
            boolean read = false;

            int index = -1;
            int t_lvl = -1;

            Pattern mark_begin = Pattern.compile("^\\{[0-9]+");
            Pattern mark_end = Pattern.compile("^\\}");
            Pattern comment = Pattern.compile("^//");
            Pattern reserved = Pattern.compile("Reserved");

            while (s != null) {

                Matcher matcher = comment.matcher(s);

                //if not comment
                if (!(matcher.find())) {

                    //if between { and }
                    if (read) {
                        matcher = mark_end.matcher(s);
                        // if found } at beginning of line
                        if (matcher.find()) {
                            read = false;
                            // else read data
                        } else {
                            /**
                             * cannot ignore reserved since they appear in some
                             * galaxies
                             */
//                            matcher = reserved.matcher(s);
//                            // if data is marked "Reserved"
//                            if (matcher.find()) {
//                                unit_types[index][t_lvl] = null;
//                                // else create new UnitType
//                            } else {
                            unit_types[index][t_lvl] = new UnitType(s, index, t_lvl);
                            Util.debugPrint("Process record");
//                            }
                            t_lvl++;

                        }
                        // else between } and {
                    } else {
                        matcher = mark_begin.matcher(s);
                        // if found {n at beginning of line
                        if (matcher.find()) {
                            read = true;
                            t_lvl = 0;
                            int start = matcher.start();
                            int end = matcher.end();
                            index = Integer.parseInt(s.substring(start + 1, end));
                            Util.debugPrint("index: " + index);
                        } else {
                            Util.logFFErrorAndExit(file_name, line_nr);
                        }
                    }
                }
                s = in.readLine();
                line_nr++;
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Failed to read " + file_name);
            Util.logEx(null, e);
            Util.logFFErrorAndExit(file_name, line_nr);
            System.exit(1);
        }

        return unit_types;

    }
//    public static UnitType processRecord(String s, int index, int t_lvl) {
//        
//        Pattern unit_type = Pattern.compile("\"[0-9a-zA-Z ]+\"");
//        
//        Matcher m = unit_type.matcher(s);
//        
//        m.find();
//        
//    }

    public static boolean isAttackCapable(Unit unit) {
        boolean r_v = false;

        if (unit.type_data.water_str > 0
                || unit.type_data.indirect_str > 0
                || unit.type_data.air_str > 0
                || unit.type_data.direct_str > 0
                || unit.type_data.close_str > 0
                || unit.type_data.psy_str > 0
                || unit.type_data.ranged_sp_str > 0
                || unit.type_data.direct_sp_str > 0
                || unit.type_data.close_sp_str > 0) {
            r_v = true;
        }

        return r_v;
    }
}
