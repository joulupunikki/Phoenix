/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import phoenix.Phoenix;
import util.C;
import util.Util;

/**
 *
 * @author doa
 */
public class UnitSpot {

    public static double[] getCosts(String s, String file_name, int line_nr) {

        double[] ret_val = new double[C.UNIT_SPOT_MOVE];

        Pattern planet_type = Pattern.compile("\"[a-zA-Z]+\"");
        Matcher m = planet_type.matcher(s);
        //skip "planet type"
        Util.testFFErrorAndExit(m.find(), file_name, line_nr);
        //last one is a big string of ten of decimal numbers
        Pattern costs_pattern = Pattern.compile("\"[0-9][0-9\\. ]+[0-9]\"");
        m = costs_pattern.matcher(s);
//        System.out.println("s = " + s);
        Util.testFFErrorAndExit(m.find(), file_name, line_nr);
//        String costs = s.substring(m.start() + 1, m.end() - 1);
        String costs = s.substring(m.start() + 1, m.end());
//        System.out.println("costs = " + costs);
//        costs_pattern = Pattern.compile("[0-9]+\\.[0-9]+");
//
//        m = costs_pattern.matcher(costs);
//
//        for (int i = 0; i < C.UNIT_SPOT_MOVE; i++) {
//            System.out.println("i = " + i);
//            ret_val[i] = processDoubleVal(costs, m, file_name, line_nr);
//        }

        processDoubleVals(costs, ret_val, file_name, line_nr);

        return ret_val;

    }

    public static double processDoubleVal(String s, Matcher m, String file_name, int line_nr) {
        Util.testFFErrorAndExit(m.find(), file_name, line_nr);
//        System.out.println("s = " + s);
        double ret_val = Double.parseDouble(s.substring(m.start(), m.end()));
//        System.out.println("ret_val = " + ret_val);

        return ret_val;
    }

    public static double[][][] readUnitSpot() {

        String file_name = C.S_UNITSPOT_DAT;
        double[][][] unit_spot = new double[C.UNIT_SPOT_HEX][C.UNIT_SPOT_PLANET][];
        int line_nr = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(file_name))) {
            String s = in.readLine();
            line_nr++;
            System.out.println("s = " + s);
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

                            unit_spot[terrain_type][planet_type] = getCosts(s, file_name, line_nr);
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
            Util.logEx(null, e);
            System.exit(1);
        }

//        printData(unit_spot);
//        System.exit(0);
        return unit_spot;

    }

    public static void processDoubleVals(String s, double[] vals, String file_name, int line_nr) {
        int start = 0;
        int state = 1;
        int index = 0;
        int counter = 0;
        boolean loop = true;

        while (loop) {

            switch (state) {
                case 1:
                    if (s.charAt(index) == ' ') {

                    } else if (s.charAt(index) >= '0' && s.charAt(index) <= '9') {

                        start = index;
                        state = 2;
                    } else {
                        Util.logFFErrorAndExit(file_name, line_nr);
                    }
                    break;
                case 2:
                    if (s.charAt(index) == '.') {
                        state = 3;
                    } else {
                        Util.logFFErrorAndExit(file_name, line_nr);
                    }
                    break;

                case 3:
                    if (s.charAt(index) >= '0' && s.charAt(index) <= '9') {
                        state = 4;

                    } else {
                        Util.logFFErrorAndExit(file_name, line_nr);
                    }
                    break;
                case 4:
                    if (counter == C.UNIT_SPOT_MOVE - 1) {
                        if (s.charAt(index) == '"') {
                            vals[counter++] = Double.parseDouble(s.substring(start, index));
                            loop = false;
                        } else {
                            Util.logFFErrorAndExit(file_name, line_nr);
                        }
                    } else {
                        if (s.charAt(index) == ' ') {
                            vals[counter++] = Double.parseDouble(s.substring(start, index));
                            state = 1;
                        } else {
                        Util.logFFErrorAndExit(file_name, line_nr);
                        }
                    }
                    break;
                default:
                    throw new AssertionError();

            }
            index++;
        }
    }

    public static int finalSpotting(int spotting, int range) {

        int r_v;
        switch (spotting) {
            case 1:
                r_v = range == 1 ? 1 : -1;
                break;
            case 2:
                r_v = range == 1 ? 2 : -1;
                break;
            case 3:
                r_v = range == 1 ? 3 : -1;
                break;
            default:
                r_v = 2 * spotting - 4 * range;
                if (r_v < 4) {
                    r_v = -1;
                }
        }
        return r_v;
    }
    
    public static void printData(double[][][] data) {

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                for (int k = 0; k < data[i][j].length; k++) {
                    System.out.print(" " + data[i][j][k]);

                }
                System.out.println("");
            }
            System.out.println("");
        }

    }

}
