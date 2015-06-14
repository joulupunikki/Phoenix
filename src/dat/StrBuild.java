/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import util.C;
import util.FN;
import util.Util;

/**
 *
 * @author joulupunikki
 */
public class StrBuild implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String name;
    public int water;
    public int land;
    public int road;
    public int barren;
    public int neutral;
    public int build;
    public int area;
    public int crd_trn;
    public int credits;
    public int turns_2_bld;
    public int tech;
    public int value;

    public StrBuild(String s, String file_name, int line_nr) {

        Pattern name_pat = Pattern.compile("\"[0-9a-zA-Z ]+\"");

        Matcher m = name_pat.matcher(s);

        //skip "name"
        m.find();
        m.find();

        name = s.substring(m.start() + 1, m.end() - 1);

        //skip "stats"
        m.find();
        m.find();

        String values = s.substring(m.start() + 1, m.end());
        //System.out.println("values = " + values);
        int[] int_vals = new int[12];
        Util.processIntVals(values, int_vals, file_name, line_nr);
        water = int_vals[0];
        land = int_vals[1];
        road = int_vals[2];
        barren = int_vals[3];
        neutral = int_vals[4];
        build = int_vals[5];
        area = int_vals[6];
        crd_trn = int_vals[7];
        credits = int_vals[8];
        turns_2_bld = int_vals[9];
        tech = int_vals[10];
        value = int_vals[11];

    }

    public static StrBuild[] readStrBuildDat() {

        String file_name = FN.S_STRBUILD_DAT;
        StrBuild[] str_build = new StrBuild[C.STRBUILD];
        int line_nr = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(file_name))) {
            Util.debugPrint("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
            Util.debugPrint(FN.S_STRBUILD_DAT);
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
                Util.debugPrint(s);
                Matcher matcher = comment.matcher(s);

                //if not comment
                if (!(matcher.find())) {

                    //if between { and }
                    if (read) {
                        matcher = mark_end.matcher(s);
                        // if found } at beginning of line
                        if (matcher.find()) {
                            return str_build;
                            // else read data
                        } else {

                            str_build[index++] = new StrBuild(s, file_name, line_nr);

                        }
                        // else between } and {
                    } else {
                        matcher = mark_begin.matcher(s);
                        // if found { at beginning of line
                        if (matcher.find()) {
                            read = true;
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

        return str_build;

    }

}
