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

import game.PBEM;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Properties;
import util.FN;
import util.Util;

/**
 * Reads and stores game parameters from the EFS.INI and phoenix.ini files and
 * creates and stores the PBEM object.
 *
 * Parameters read as java Properties object. As you need new parameters convert
 * them in the constructor from Properties to EfsIni members.
 *
 * @author RSW
 * @author joulupunikki
 */
public class EfsIni implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ////     [Options]
//    public int video_on = 0;
//    public int grid = 0;
//    public int sound = 0;
//    public int autosave = 0;
//
//    // [Computer]
//    public int Player_1 = 0;
//    public int Player_2 = 0;
//    public int Player_3 = 0;
//    public int Player_4 = 0;
//    public int Player_5 = 0;
//
//    // [Defaults]
//    public int turns_til_vote = 0;
//    public int turns_til_patriarch_dies = 0;
//    public int normal_damage = 0;
//    public int feint_damage = 0;
//    public int loyalty_noble_bonus = 0;
//    public int loyalty_officer_bonus = 0;
//    public int default_leadership = 0;
    public int starting_credits = -1;
    public int default_tax_rate = -1;
    public int default_tithe_rate = -1;
//    public int give_back_res_per = 0;
//    public int default_church_like = 0;
//    public int default_league_like = 0;
//    public int default_interest_rate = 0;
//    public int default_loan_turns = 0;
//    public int percent_asset_is_loan = 0;
//    public int minimum_loan_amount = 0;
//    public int health_check_for_plague = 0;
    public int health_loss_for_famine = -1;
//    public int immune_plague_bonus = 0;
//    public int shield_radius = 0;
//    public int move_pause = 0;     // in ms
    public int city_heal_rate = -1;
    public int unit_heal_in_city = -1;
    public int unit_heal = -1;
//    public int third_republic_min = 0;
//    public int third_republic_max = 0;
//    public int not_enuf_garrison = 0;
//    public int credits_received = 0;
//    public int per_tech_receieved = 0;
//    public int receive_unfound_city = 0;
//    public int promise_votes = 0;
//
//    // [League]
//    public int leaguehall_stock = 0;
//    public int league_like_up = 0;
//    public int league_like_down = 0;
//    public int league_int_rate_up = 0;
//    public int league_int_rate_down = 0;
//
//    // [Church]
//    public int excommunicate_expire = 0;
//    public int excommunicate_turns = 0;
//    public int excom_peasant_loyalty_hit = 0;
//    public int sign_holy_writ = 0;
//    public int default_sect = 0;

    // Game options normally entered in GUI
    public boolean universal_warehouse = false;
    public boolean consume_food = true;
    public boolean plague = true;
    public boolean rebellion = true;
    public PBEM pbem = null;

    // Phoenix game options
    public int lab_points = -1;
    public int lab_cost = -1;
    public boolean wizard_mode = false;
    public int regency_term_length = -1;

    /**
     * Converts EFS.INI and phoenix.ini values from Properties to internal game
     * representation, and stores them; also creates and stores PBEM object.
     *
     * @param efs_ini
     * @param phoenix_ini
     */
    public EfsIni(Properties efs_ini, Properties phoenix_ini) {
        starting_credits = Integer.parseInt((efs_ini.getProperty("starting_credits")).trim());
        default_tax_rate = Integer.parseInt((efs_ini.getProperty("default_tax_rate")).trim());
        default_tithe_rate = Integer.parseInt((efs_ini.getProperty("default_tithe_rate")).trim());
        city_heal_rate = Integer.parseInt((efs_ini.getProperty("city_heal_rate")).trim());
        unit_heal_in_city = Integer.parseInt((efs_ini.getProperty("unit_heal_in_city")).trim());
        unit_heal = Integer.parseInt((efs_ini.getProperty("unit_heal")).trim());
        health_loss_for_famine = Integer.parseInt((efs_ini.getProperty("health_loss_for_famine")).trim());

        //phoenix.ini values
        lab_points = Integer.parseInt((phoenix_ini.getProperty("lab_points")).trim());
        lab_cost = Integer.parseInt((phoenix_ini.getProperty("lab_cost")).trim());
        wizard_mode = Boolean.parseBoolean((phoenix_ini.getProperty("wizard_mode")).trim());
        regency_term_length = Integer.parseInt((phoenix_ini.getProperty("regency_term_length")).trim());
        pbem = new PBEM();
    }

    /**
     * Convert Properties format EFS.INI and phoenix.ini to EfsIni object.
     *
     * @param efs_ini
     * @param phoenix_ini
     * @return
     */
    public static EfsIni readEfsIni(Properties efs_ini, Properties phoenix_ini) {
        EfsIni ret_val = new EfsIni(efs_ini, phoenix_ini);

        return ret_val;
    }

    /**
     * Read phoenix.ini as a Properties object.
     *
     * @return Properties object
     */
    public static Properties readPhoenixIni() {
        Properties phoenix_ini = new Properties();

        try (BufferedReader in = new BufferedReader(new FileReader(FN.S_PHOENIX_INI))) {
            phoenix_ini.load(in);
        } catch (Exception e) {
            Util.logEx(null, e);
            Util.logFFErrorAndExit(FN.S_PHOENIX_INI, -1);
        }

        return phoenix_ini;
    }

    /**
     * Read EFS.INI as a Properties object.
     *
     * @return Properties object
     */
    public static Properties readEFSINI() {
        Properties efs_ini = new Properties();
        convertToProperties(FN.S_EFS_INI, FN.S_EFS_INI_TMP);

        try (BufferedReader in = new BufferedReader(new FileReader(FN.S_EFS_INI_TMP))) {
            efs_ini.load(in);
        } catch (Exception e) {
            Util.logEx(null, e);
            Util.logFFErrorAndExit(FN.S_EFS_INI, -1);
        }
        File remove = new File(FN.S_EFS_INI_TMP);
        remove.delete();
//        System.out.println("Properties:");
//        Set<String> test_set = efs_ini.stringPropertyNames();
//        for (String string : test_set) {
//            System.out.println(string + " " + efs_ini.getProperty(string));
//        }
//        System.exit(0);
        return efs_ini;
    }

    /**
     * Convert EFS.INI to properties readable format.
     *
     * @param file_name name of EFS.INI
     * @param tmp_file name of tmp file
     */
    public static void convertToProperties(String file_name, String tmp_file) {
        int line_nr = 1;
        try (BufferedReader in = new BufferedReader(new FileReader(file_name));
                PrintWriter out = new PrintWriter(tmp_file)) {
            String input = in.readLine();

            while (input != null) {
                //System.out.println("input = " + input);
                if (!(input.equals("") || input.startsWith("["))) {
                    String output = processLine(input);
                    //System.out.println("output = " + output);
                    out.println(output);
                }
                input = in.readLine();
                line_nr++;
            }
            out.flush();
        } catch (Exception e) {
            Util.logEx(null, e);
            Util.logFFErrorAndExit(file_name, line_nr);
        }
    }

    /**
     * Convert a EFS.INI line to Properties format.
     *
     * @param line to convert
     * @return Properties format line
     */
    public static String processLine(String line) {
        String s = "";
        boolean loop = true;
        final int BEGIN = 0;
        final int PROPERTY = 1;
        final int VALUE = 2;
        int state = BEGIN;
        for (int i = 0; i < line.length() && loop; i++) {

            switch (state) {
                case BEGIN:
                    if (line.charAt(i) == ' ') {
                    } else if (line.charAt(i) == '=') {
                        state = VALUE;
                    } else {
                        state = PROPERTY;
                    }
                    s = s + line.charAt(i);
                    break;
                case PROPERTY:
                    if (line.charAt(i) == ' ') {
                        s = s + "_";
                    } else if (line.charAt(i) == '=') {
                        state = VALUE;
                        s = s + line.charAt(i);
                    } else {
                        s = s + line.charAt(i);
                    }
                    break;
                case VALUE:
                    if (line.charAt(i) == ';') {
                        loop = false;
                    } else {
                        s = s + line.charAt(i);
                    }
                    break;
                default:
                    throw new AssertionError();
            }
        }
        return s;
    }

}
