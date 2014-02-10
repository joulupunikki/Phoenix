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
import util.Util;

/**
 *
 * @author joulupunikki
 */
public class StrBuild implements Serializable {

    public String name;
//    public int water public int land public int road public int barren public int neutral public int build public int area public int Crd/Trn public int Credits public int Turns2Bld public int Tech public int Value

    public StrBuild(String s) {

        Pattern name_pat = Pattern.compile("\"[0-9a-zA-Z ]+\"");

        Matcher m = name_pat.matcher(s);

        //skip "name"
        m.find();
        m.find();

        name = s.substring(m.start() + 1, m.end() - 1);
//        System.out.println("name = " + name);
//                //skip "stats"
//        m.find();
//        m.find();
//
//                    Pattern stats_pattern = Pattern.compile("\"[0-9]+\"");
//
//        m = stats_pattern.matcher(s);
//        
//        //last one is a big string of one text and dozens of integer values
//        String stats = s.substring(m.start() + 1, m.end() - 1);
//        
//        stats_pattern = Pattern.compile("[0-9]+");
//        
//        m = stats_pattern.matcher(stats);
//        
//        m.find();     
//        
//        water = UnitType.processIntVal(stats, m);

    }

    public static StrBuild[] readStrBuildDat() {

        String file_name = C.S_STRBUILD_DAT;
        StrBuild[] str_build = new StrBuild[C.STRBUILD];
        int line_nr = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(file_name))) {
            Util.debugPrint("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
            Util.debugPrint(C.S_STRBUILD_DAT);
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

                            str_build[index++] = new StrBuild(s);

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
