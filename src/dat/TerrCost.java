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
import util.Util;

/**
 *
 * @author joulupunikki
 */
public class TerrCost {

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

    public static double processDoubleVal(String s, Matcher m) {

//        System.out.println("s = " + s);
        double ret_val = Double.parseDouble(s.substring(m.start(), m.end()));
//        System.out.println("ret_val = " + ret_val);
        m.find();

        return ret_val;
    }

    public static double[][][] readTerrCost() {

        double[][][] terr_cost = new double[C.TERR_COST_HEX][C.TERR_COST_PLANET][];

        try (BufferedReader in = new BufferedReader(new FileReader(C.S_TERRCOST_DAT))) {
            String s = in.readLine();
//            System.out.println("s = " + s);
            //true if between { and } false if between } and {
            boolean read = false;

            int terrain_type = -1; // initialized to -1 on purpose
            int planet_type = 0;

            Pattern mark_begin = Pattern.compile("^\\{");
            Pattern mark_end = Pattern.compile("^\\}");
            Pattern comment = Pattern.compile("^\\\\");


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
                        }
                    }
                }
                s = in.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Failed to read " + C.S_UNIT_DAT);
            System.exit(1);
        }

        return terr_cost;

    }
}
