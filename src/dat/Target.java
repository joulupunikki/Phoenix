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

/**
 *
 * @author joulupunikki
 */
public class Target {

    public static int[] getTarget(String s) {

        int[] ret_val = new int[C.TARGET_DAT_X];

        Pattern name_pat = Pattern.compile("\"[0-9a-zA-Z ]+\"");

        Matcher m = name_pat.matcher(s);
        System.out.println("s = " + s);
        //skip "name"
        m.find();
        m.find();

        String line = s.substring(m.start() + 1, m.end() - 1);
        System.out.println("line = " + line);
//        Pattern targets = Pattern.compile("\"[0-9 ]+\"");
//        m = targets.matcher(s2);
//        System.out.println("line = " + s2);
//        m.find();
//        System.out.println("start: " + m.start() + " end: " + m.end());
//        String line = s2.substring(m.start() + 1, m.end() - 1);
//        System.out.println("costs = " + line);
        Pattern targets = Pattern.compile("[0-9]+");

        m = targets.matcher(line);
        m.find();

        System.out.println("getCosts");
        for (int i = 0; i < C.TARGET_DAT_X; i++) {
            ret_val[i] = UnitType.processIntVal(line, m);
        }

        return ret_val;

    }

    public static int[][] readTargetDat() {

        int[][] target_dat = new int[C.TARGET_DAT_Y][];

        try (BufferedReader in = new BufferedReader(new FileReader(C.S_TARGET_DAT))) {
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
                        }
                    }
                }
                s = in.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Failed to read " + C.S_TARGET_DAT);
            System.exit(1);
        }

        return target_dat;
    }

    public static void printTarget(int[][] target) {
        for (int i = 0; i < target.length; i++) {
            for (int j = 0; j < target[i].length; j++) {
                System.out.print(target[i][j] + " ");

            }
            System.out.println("");
        }
    }
}
