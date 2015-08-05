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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import util.C;
import util.FN;
import util.Util;

/**
 * Read and return TERRCOST.DAT data.
 *
 * @author joulupunikki
 */
public class TerrCost {

    /**
     * Parse a line of TERRCOST.DAT data.
     *
     * @param s
     * @return double[]
     */
    public static double[] getCosts(String s) {

        double[] ret_val = new double[C.TERR_COST_MOVE];

        Pattern planet_type = Pattern.compile("\"[a-zA-Z]+\"");
        Matcher m = planet_type.matcher(s);
        //skip "planet type"
        m.find();
        //last one is a big string of ten of decimal numbers
        Pattern costs_pattern = Pattern.compile("\"[0-9][0-9\\. ]+[0-9]\"");
        m = costs_pattern.matcher(s);
//        System.out.println("s = " + s);
        m.find();
//        System.out.println("start: " + m.start() + " end: " + m.end());
        String costs = s.substring(m.start() + 1, m.end() - 1);
//        System.out.println("costs = " + costs);
        costs_pattern = Pattern.compile("[0-9]+\\.[0-9]+");

        m = costs_pattern.matcher(costs);
        m.find();

//        System.out.println("getCosts");
        for (int i = 0; i < C.TERR_COST_MOVE; i++) {
            ret_val[i] = processDoubleVal(costs, m);
        }

        return ret_val;

    }

    /**
     * Parse a double value.
     *
     * @param s
     * @param m
     * @return
     */
    public static double processDoubleVal(String s, Matcher m) {

//        System.out.println("s = " + s);
        double ret_val = Double.parseDouble(s.substring(m.start(), m.end()));
//        System.out.println("ret_val = " + ret_val);
        m.find();

        return ret_val;
    }

    /**
     * Read in terrain movement cost data from TERRCOST.DAT.
     *
     * @return double[][][]
     */
    public static double[][][] readTerrCost() {

        String file_name = FN.S_TERRCOST_DAT;
        double[][][] terr_cost = new double[C.TERR_COST_HEX][C.TERR_COST_PLANET][];
        int line_nr = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(file_name))) {
            String s = in.readLine();
            line_nr++;
//            System.out.println("s = " + s);
            //true if between { and } false if between } and {
            boolean read = false;

            int terrain_type = -1; // initialized to -1 on purpose
            int planet_type = 0;

            Pattern mark_begin = Pattern.compile("^\\{");
            Pattern mark_end = Pattern.compile("^\\}");
            Pattern comment = Pattern.compile("^//");

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

                            terr_cost[terrain_type][planet_type] = getCosts(s);
                            Util.debugPrint("Process record");

                            planet_type++;

                        }
                        // else between } and {
                    } else {
                        matcher = mark_begin.matcher(s);
                        // if found { at beginning of line
                        if (matcher.find()) {
                            read = true;
                            planet_type = 0;
                            terrain_type++; // initialized to -1

                            // incorrect data file
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

        return terr_cost;

    }
}
