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
 * Reads in attack damage table from DAMAGE.DAT.
 *
 * @author joulupunikki
 */
public class Damage {

    /**
     * Parse a line of DAMAGE.DAT.
     *
     * @param s line.
     * @return int[] of damage values.
     */
    public static int[] getDamage(String s) {

        int[] ret_val = new int[C.DAMAGE_DAT_X];

        Pattern damages = Pattern.compile("\"[0-9 ]+\"");
        Matcher m = damages.matcher(s);
        //System.out.println("s = " + s);
        m.find();
        //System.out.println("start: " + m.start() + " end: " + m.end());
        String line = s.substring(m.start() + 1, m.end() - 1);
        //System.out.println("costs = " + line);
        damages = Pattern.compile("[0-9]+");

        m = damages.matcher(line);
        //m.find();

        //System.out.println("getCosts");
        for (int i = 0; i < C.DAMAGE_DAT_X; i++) {
            ret_val[i] = UnitType.processIntVal(line, m);
        }

        return ret_val;

    }

    /**
     * Read DAMAGE.DAT.
     *
     * @return int[][] of damage values
     */
    public static int[][] readDamageDat() {

        int[][] damage_dat = new int[C.DAMAGE_DAT_Y][];
        int line_nr = 1;
        try (BufferedReader in = new BufferedReader(new FileReader(FN.S_DAMAGE_DAT))) {
            String s = in.readLine();

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
                            return damage_dat;
                            // else read data
                        } else {

                            damage_dat[index++] = getDamage(s);

                        }
                        // else between } and {
                    } else {
                        matcher = mark_begin.matcher(s);
                        // if found { at beginning of line
                        if (matcher.find()) {
                            read = true;
                        }
                    }
                }
                line_nr++;
                s = in.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.out.println("Failed to read " + FN.S_DAMAGE_DAT);
            Util.logEx(null, e);
            Util.logFFErrorAndExit(FN.S_DAMAGE_DAT, line_nr);
            System.exit(1);
        }

        return damage_dat;
    }

    /**
     * For debugging purposes.
     *
     * @param damages
     */
    public static void printDamage(int[][] damages) {
        for (int i = 0; i < damages.length; i++) {
            for (int j = 0; j < damages[i].length; j++) {
                System.out.print(damages[i][j] + " ");

            }
            System.out.println("");
        }
    }
}
