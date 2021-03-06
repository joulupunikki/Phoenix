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
package dat;

import galaxyreader.Unit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import util.C;
import util.C.MoveType;
import util.FN;
import util.Util;

/**
 * Reads in and stores static unit type data from UNIT.DAT (this breaks the
 * naming convention of classes in package dat.)
 *
 * @author joulupunikki
 */
public class UnitType implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
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
    public String art;

    /**
     * Parse and store a line from UNITTYPE.DAT
     *
     * FIXME With no knowledge of the characters accepted by "EFS.EXE", new mods
     * might use some symbols digested by "EFS.EXE" but which cause Phoenix the
     * hiccups.
     *
     * @param s
     * @param index
     * @param t_lvl
     */
    public UnitType(String s, int index, int t_lvl) {

        this.index = index;
        this.t_lvl = t_lvl;
        int stats_end;
        Util.debugPrint("Line: " + s);
        /*
         * accepts alphanum, -, ' and .
         */
        Pattern unit_type = Pattern.compile("\"[0-9a-zA-Z_ ,\\(\\)\\[\\]\\!\\*\\-'\\.\\&\t]+\"");

        Matcher m = unit_type.matcher(s);

        //skip "name"
        m.find();
        m.find();
        //System.out.println(s);
        name = s.substring(m.start() + 1, m.end() - 1);
        Util.debugPrint("Name: " + name);
        //System.out.println("name = " + name);
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
        Util.debugPrint("Interval: " + (m.start() + 1) + "," + (m.end() - 1));
        //last one is a big string of one text and dozens of integer values
        String stats = s.substring(m.start() + 1, m.end() - 1);
        Util.debugPrint("Stats: " + stats);
        stats_end = m.end();
        Pattern stats_pattern = Pattern.compile("[0-9a-zA-Z]+");

        m = stats_pattern.matcher(stats);

        m.find();

        move_type = processMoveType(stats.substring(m.start(), m.end()));
        Util.debugPrint("MoveType: " + move_type);

        stats_pattern = Pattern.compile("-?[0-9]+");

        m = stats_pattern.matcher(stats);

        //m.find();

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
        //System.out.println("can_b_cargo = " + can_b_cargo);
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

        // name of FLC file
        stats = s.substring(stats_end + 1, s.length());
        //System.out.println("art :" + stats);
        m = unit_type.matcher(stats);
        //skip "art"
        m.find();
        //System.out.println(stats.substring(m.start() + 1, m.end() - 1));
        m.find();
        
        art = stats.substring(m.start() + 1, m.end() - 1).toUpperCase(Locale.ROOT);
        //System.out.println(art);

    }

    /**
     * Parse int value.
     *
     * @param s
     * @param m
     * @return
     */
    public static int processIntVal(String s, Matcher m) {
        m.find();
        int ret_val = Integer.parseInt(s.substring(m.start(), m.end()));
        return ret_val;
    }

    /**
     * Convert movement type string to MoveType.
     *
     * @param s
     * @return
     */
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

    /**
     * For debugging purposes.
     *
     * @param args
     */
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

    /**
     * Read and store data from UNITTYPE.DAT.
     *
     * @return
     */
    public static UnitType[][] readUnitDat() {

        String file_name = FN.S_UNIT_DAT;
        UnitType[][] unit_types = new UnitType[C.UNIT_TYPES][C.UNIT_T_LVLS];
        int line_nr = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(file_name))) {
            String s = in.readLine();
            line_nr++;
            //true if between { and } false if between } and {
            boolean read = false;

            int index = -1;
            int t_lvl = -1;

            Pattern mark_begin = Pattern.compile("^" + C.WS + "\\{" + C.WS + "[0-9]+");
            Pattern mark_end = Pattern.compile("^" + C.WS + "\\}");
            Pattern comment = Pattern.compile("^" + C.WS + "(//|$)");
            //Pattern empty = Pattern.compile("^" + C.WS + "$");
            //Pattern reserved = Pattern.compile("Reserved");

            while (s != null) {

                Matcher matcher = comment.matcher(s);

                //if not comment or empty
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
                            Util.logFileFormatError(FN.S_UNIT_DAT, line_nr, "comments must begin with //"); // fix #66
                            //Util.logFFErrorAndExit(file_name, line_nr);
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
            Util.logFFErrorAndExit(file_name, line_nr, e);
            //CrashReporter.showCrashReport(e);
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

    /**
     * Return true iff unit is capable of attacking.
     *
     * @param unit
     * @return
     */
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
