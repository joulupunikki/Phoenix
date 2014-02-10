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
import util.C;
import util.Util;

/**
 *
 * @author joulupunikki
 */
public class TerColor {

    public static int[] getCosts(String s, String file_name, int line_nr) {

        int[] ret_val = new int[C.TER_COLOR_PLANET];

        Pattern terrain_type = Pattern.compile("\"[a-zA-Z ]+\"");
        Matcher m = terrain_type.matcher(s);
        //skip "planet type"
        Util.testFFErrorAndExit(m.find(), file_name, line_nr);
        //last one is a big string of five of integers
        Pattern colors_pattern = Pattern.compile("\"[0-9 ]+[0-9]+[0-9 ]+\"");
        String values = s.substring(m.end() + 1, s.length());
        System.out.println(values);
        m = colors_pattern.matcher(values);
//        System.out.println("s = " + s);

        Util.testFFErrorAndExit(m.find(), file_name, line_nr);
//        String costs = s.substring(m.start() + 1, m.end() - 1);
        String colors = values.substring(m.start() + 1, m.end());
        System.out.println("colors = " + colors);
//        costs_pattern = Pattern.compile("[0-9]+\\.[0-9]+");
//
//        m = costs_pattern.matcher(costs);
//
//        for (int i = 0; i < C.UNIT_SPOT_MOVE; i++) {
//            System.out.println("i = " + i);
//            ret_val[i] = processDoubleVal(costs, m, file_name, line_nr);
//        }

        processIntVals(colors, ret_val, file_name, line_nr);
        return ret_val;

    }

    public static int[][] readTerColor() {

        String file_name = C.S_TERCOLOR_DAT;
        int[][] ter_color = new int[C.TER_COLOR_HEX][];
        int line_nr = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(file_name))) {
            String s = in.readLine();
            line_nr++;
            System.out.println("s = " + s);
            //true if between { and } false if between } and {
            boolean read = false;

            int terrain_type = 0;

            Pattern mark_begin = Pattern.compile("^\\{");
            Pattern mark_end = Pattern.compile("^\\}");
            Pattern comment = Pattern.compile("^//");

            while (s != null) {
                System.out.println("s = " + s);
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

//        printData(unit_spot);
//        System.exit(0);
        return ter_color;

    }

    public static void processIntVals(String s, int[] vals, String file_name, int line_nr) {
        int start = 0;

        final int SPACE = 1;
        final int NUM = 2;
        final int FAIL = 999;
        int index = 0;
        int counter = 0;
        boolean loop = true;
        int state = SPACE;
        while (loop) {

            switch (state) {
                case SPACE:
                    System.out.println("SPACE");
                    if (s.charAt(index) == ' ') {

                    } else if (s.charAt(index) >= '1' && s.charAt(index) <= '9') {

                        start = index;
                        state = NUM;
                    } else {
                        state = FAIL;
                    }
                    break;
                case NUM:
                    System.out.println("NUM");
                    if (s.charAt(index) >= '0' && s.charAt(index) <= '9') {

                    } else if (counter == C.TER_COLOR_PLANET - 1) {
                        if (s.charAt(index) == '"') {
                            vals[counter++] = Integer.parseInt(s.substring(start, index));
                            loop = false;
                        } else {
                            state = FAIL;
                        }
                    } else {
                        if (s.charAt(index) == ' ') {
                            vals[counter++] = Integer.parseInt(s.substring(start, index));
                            state = SPACE;
                        } else {
                            state = FAIL;
                        }
                    }
                    break;
                case FAIL:
                    Util.logFFErrorAndExit(file_name, line_nr);
                    break;
                default:
                    throw new AssertionError();

            }
            index++;
        }
    }

    public static void print(int[][] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                System.out.print(array[i][j] + " ");

            }
            System.out.println("");

        }

    }
}
