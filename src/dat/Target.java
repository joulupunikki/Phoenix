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
 * Reads in targeting data from TARGET.DAT.
 *
 * @author joulupunikki
 */
public class Target {

    /**
     * Parse and return a line of TARGET.DAT.
     *
     * @param s
     * @return
     */
    public static int[] getTarget(String s) {

        int[] ret_val = new int[C.TARGET_DAT_X];

        Pattern name_pat = Pattern.compile("\"[0-9a-zA-Z ]+\"");

        Matcher m = name_pat.matcher(s);
        //System.out.println("s = " + s);
        //skip "name"
        m.find();
        m.find();

        String line = s.substring(m.start() + 1, m.end() - 1);
        //System.out.println("line = " + line);
//        Pattern targets = Pattern.compile("\"[0-9 ]+\"");
//        m = targets.matcher(s2);
//        System.out.println("line = " + s2);
//        m.find();
//        System.out.println("start: " + m.start() + " end: " + m.end());
//        String line = s2.substring(m.start() + 1, m.end() - 1);
//        System.out.println("costs = " + line);
        Pattern targets = Pattern.compile("[0-9]+");

        m = targets.matcher(line);
        //m.find();

        //System.out.println("getCosts");
        for (int i = 0; i < C.TARGET_DAT_X; i++) {
            ret_val[i] = UnitType.processIntVal(line, m);
        }

        return ret_val;

    }

    /**
     * Read and parse TARGET.DAT.
     *
     * @return int[][]
     */
    public static int[][] readTargetDat() {

        String file_name = FN.S_TARGET_DAT;
        int[][] target_dat = new int[C.TARGET_DAT_Y][];
        int line_nr = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(file_name))) {
            String s = in.readLine();
            line_nr++;
//            System.exit(0);
            //true if between { and } false if between } and {
            boolean read = false;

            int index = 0;

            Pattern mark_begin = Pattern.compile("^\\{");
            Pattern mark_end = Pattern.compile("^\\}");
            Pattern comment = Pattern.compile("^//");
//            Pattern reserved = Pattern.compile("Reserved");

            while (s != null) {

                Matcher matcher = comment.matcher(s);

                //if not comment
                if (!(matcher.find())) {

                    //if between { and }
                    if (read) {
                        matcher = mark_end.matcher(s);
                        // if found } at beginning of line
                        if (matcher.find()) {
                            return target_dat;
                            // else read data
                        } else {

                            //error (?) in Hyperion 1.4g TARGET.DAT requires this
                            matcher = mark_begin.matcher(s);
                            if (matcher.find()) {
                                return target_dat;
                            }
                            //end error

                            target_dat[index++] = getTarget(s);
                        }
                        // else between } and {
                    } else {
                        matcher = mark_begin.matcher(s);
                        // if found { at beginning of line
                        if (matcher.find()) {
                            read = true;
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
            Util.logFFErrorAndExit(file_name, line_nr, e);
            //CrashReporter.showCrashReport(e);
        }

        return target_dat;
    }

    /**
     * Print target data, for debugging purposes.
     *
     * @param target
     */
    public static void printTarget(int[][] target) {
        for (int i = 0; i < target.length; i++) {
            for (int j = 0; j < target[i].length; j++) {
                System.out.print(target[i][j] + " ");

            }
            System.out.println("");
        }
    }

    /**
     * Check and set EFS1.4 hardwired lander ground attack vulnerability if
     * requested.
     *
     * @param target
     * @param efs_ini
     */
    public static void setLanderVulnerability(int[][] target, EfsIni efs_ini) {
        if (efs_ini.lander_vulnerability_EFS) {
            int t = C.MoveType.LANDER.ordinal();
            target[t][C.INDIRECT] = 1;
            target[t][C.DIRECT] = 1;
            target[t][C.CLOSE] = 1;
        }
    }
}
