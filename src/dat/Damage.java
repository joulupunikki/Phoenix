/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author joulupunikki
 */
public class Damage {

    public static int[] getDamage(String s) {

        int[] ret_val = new int[C.DAMAGE_DAT_X];

        Pattern damages = Pattern.compile("\"[0-9 ]+\"");
        Matcher m = damages.matcher(s);
        System.out.println("s = " + s);
        m.find();
        System.out.println("start: " + m.start() + " end: " + m.end());
        String line = s.substring(m.start() + 1, m.end() - 1);
        System.out.println("costs = " + line);
        damages = Pattern.compile("[0-9]+");

        m = damages.matcher(line);
        m.find();

        System.out.println("getCosts");
        for (int i = 0; i < C.DAMAGE_DAT_X; i++) {
            ret_val[i] = UnitType.processIntVal(line, m);
        }

        return ret_val;

    }

    public static int[][] readDamageDat() {

        int[][] damage_dat = new int[C.DAMAGE_DAT_Y][];

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
                s = in.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Failed to read " + FN.S_DAMAGE_DAT);
            System.exit(1);
        }

        return damage_dat;
    }

    public static void printDamage(int[][] damages) {
        for (int i = 0; i < damages.length; i++) {
            for (int j = 0; j < damages[i].length; j++) {
                System.out.print(damages[i][j] + " ");

            }
            System.out.println("");
        }
    }
}
