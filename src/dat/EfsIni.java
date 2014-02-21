/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Set;
import util.Util;

/**
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class EfsIni {

    public static Properties readEFSINI() {
        Properties efs_ini = new Properties();
        String file_name = "DAT/EFS.INI";
        String tmp_file = "EFS.INI.TMP";
        convertToProperties(file_name, tmp_file);

        try (BufferedReader in = new BufferedReader(new FileReader(tmp_file))) {
            efs_ini.load(in);
        } catch (Exception e) {
            Util.logEx(null, e);
            Util.logFFErrorAndExit(file_name, -1);
        }
        File remove = new File(tmp_file);
        remove.delete();
//        System.out.println("Properties:");
//        Set<String> test_set = efs_ini.stringPropertyNames();
//        for (String string : test_set) {
//            System.out.println(string + " " + efs_ini.getProperty(string));
//        }
//        System.exit(0);
        return efs_ini;
    }

    public static void convertToProperties(String file_name, String tmp_file) {
        int line_nr = 1;
        try (BufferedReader in = new BufferedReader(new FileReader(file_name));
                PrintWriter out = new PrintWriter(tmp_file)) {
            String input = in.readLine();

            while (input != null) {
                System.out.println("input = " + input);
                if (!(input.equals("") || input.startsWith("["))) {
                    String output = processLine(input);
                    System.out.println("output = " + output);
                    out.println(output);
                }
                input = in.readLine();
                line_nr++;
            }
            out.flush();
        } catch (Exception e) {
            Util.logEx(null, e);
            Util.logFFErrorAndExit(file_name, line_nr);
        }
    }

    public static String processLine(String line) {
        String s = "";
        boolean loop = true;
        final int BEGIN = 0;
        final int PROPERTY = 1;
        final int VALUE = 2;
        int state = PROPERTY;
        for (int i = 0; i < line.length() && loop; i++) {

            switch (state) {
                case BEGIN:
                    if (line.charAt(i) == ' ') {
                    } else if (line.charAt(i) == '=') {
                        state = VALUE;
                    } else {
                        state = PROPERTY;
                    }
                    s = s + line.charAt(i);
                    break;
                case PROPERTY:
                    if (line.charAt(i) == ' ') {
                        s = s + "_";
                    } else if (line.charAt(i) == '=') {
                        state = VALUE;
                        s = s + line.charAt(i);
                    } else {
                        s = s + line.charAt(i);
                    }
                    break;
                case VALUE:
                    if (line.charAt(i) == ';') {
                        loop = false;
                    } else {
                        s = s + line.charAt(i);
                    }
                    break;
                default:
                    throw new AssertionError();
            }
        }
        return s;
    }
}
