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
 * Read in planetary minimap color data from TERCOLOR.DAT.
 *
 * @author joulupunikki
 */
public class TerColor {

    /**
     * Parse a line of TERCOLOR.DAT data
     *
     * @param s
     * @param file_name
     * @param line_nr
     * @return int[]
     */
    public static int[] getCosts(String s, String file_name, int line_nr) {

        int[] ret_val = new int[C.TER_COLOR_PLANET];

        Pattern terrain_type = Pattern.compile("\"[a-zA-Z ]+\"");
        Matcher m = terrain_type.matcher(s);
        //skip "planet type"
        Util.testFFErrorAndExit(m.find(), file_name, line_nr);
        //last one is a big string of five of integers
        Pattern colors_pattern = Pattern.compile("\"[0-9 ]+[0-9]+[0-9 ]+\"");
        String values = s.substring(m.end() + 1, s.length());
        //System.out.println(values);
        m = colors_pattern.matcher(values);
//        System.out.println("s = " + s);

        Util.testFFErrorAndExit(m.find(), file_name, line_nr);
//        String costs = s.substring(m.start() + 1, m.end() - 1);
        String colors = values.substring(m.start() + 1, m.end());
        //System.out.println("colors = " + colors);
//        costs_pattern = Pattern.compile("[0-9]+\\.[0-9]+");
//
//        m = costs_pattern.matcher(costs);
//
//        for (int i = 0; i < C.UNIT_SPOT_MOVE; i++) {
//            System.out.println("i = " + i);
//            ret_val[i] = processDoubleVal(costs, m, file_name, line_nr);
//        }

        Util.processIntVals(colors, ret_val, file_name, line_nr);
        return ret_val;

    }

    /**
     * Read and parse TERCOLOR.DAT
     *
     * @return int[][]
     */
    public static int[][] readTerColor() {

        String file_name = FN.S_TERCOLOR_DAT;
        int[][] ter_color = new int[C.TER_COLOR_HEX][];
        int line_nr = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(file_name))) {
            String s = in.readLine();
            line_nr++;
            //System.out.println("s = " + s);
            //true if between { and } false if between } and {
            boolean read = false;

            int terrain_type = 0;

            Pattern mark_begin = Pattern.compile("^\\{");
            Pattern mark_end = Pattern.compile("^\\}");
            Pattern comment = Pattern.compile("^//");

            while (s != null) {
                //System.out.println("s = " + s);
                Matcher matcher = comment.matcher(s);

                //if not comment
                if (!(matcher.find())) {
                    //if between { and }
                    if (read) {
                        matcher = mark_end.matcher(s);
                        // if found } at beginning of line
                        if (matcher.find()) {
                            if (terrain_type != C.TER_COLOR_HEX) {
                                Util.logFFErrorAndExit(s, line_nr, "Wrong number of data lines.");
                            }
                            break;
                            // else read data
                        } else {

                            ter_color[terrain_type] = getCosts(s, file_name, line_nr);
                            terrain_type++;

                        }
                        // else between } and {
                    } else {
                        matcher = mark_begin.matcher(s);
                        // if found { at beginning of line
                        if (matcher.find()) {
                            read = true;
                            // incorrect data file
                        } else {
                            throw new Exception();
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

//        printData(unit_spot);
//        System.exit(0);
        return ter_color;

    }

    /**
     * For debugging purposes.
     *
     * @param array
     */
    public static void print(int[][] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                System.out.print(array[i][j] + " ");

            }
            System.out.println("");

        }

    }
}
