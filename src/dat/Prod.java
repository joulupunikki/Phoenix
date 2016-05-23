/*
 * Copyright (C) 2015 joulupunikki joulupunikki@gmail.communist.invalid.
 * Copyright (C) 2014 Richard Wein
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

import game.Game;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import util.C;
import util.FN;
import util.Util;

/**
 * Reads in PROD.DAT, which give data about secondary resource production. For
 * each type of secondary production city (e.g. chemicals) there are up to 3
 * input resource types ("need") and one output resource type ("make"). Unused
 * needs slots are left null. For each resource type needed or made, there is a
 * type and amount.
 *
 * @author RSW
 */
public class Prod implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public ResPair[] need = new ResPair[3];   // Up to 3 resources (type + amount) which the city needs
    public ResPair make;                      // One resource (type + amount) which the city makes

    private static String s;    // This is just here so that readProdDat can print lines read by getProdForCity

    /**
     * Reads production data for one city type
     */
    private static Prod getProdForCity(int city_type, BufferedReader in, Game game, int[] line_nr) throws Exception {

        Prod ret_val = new Prod();

        // Read opening brace line
        s = Util.cleanLine(in, line_nr);
        if (!s.startsWith("{")) {
            throw new Exception("Character { expected. Found: " + s);
        }

        // Read "city" line
        s = Util.cleanLine(in, line_nr);
        Pattern pattern = Pattern.compile("\"[^\"]*\"");    // Find anything in quotes, including empty string
        Matcher m = pattern.matcher(s);
        m.find();
        String first_string = s.substring(m.start() + 1, m.end() - 1).trim();
        if (!first_string.equalsIgnoreCase("city")) {
            throw new Exception("String \"city\" expected. Found: " + s);
        }
        // For now, don't check city type
        // Read "need" lines

        s = Util.cleanLine(in, line_nr);    // Read one line ahead, as we don't know how many "need" lines there are
        m = pattern.matcher(s);
        m.find();
        first_string = s.substring(m.start() + 1, m.end() - 1).trim();

        int need_count = 0;
        while (first_string.equalsIgnoreCase("need")) {    // Note: allow zero "need" lines
            if (need_count >= 3) {
                throw new Exception("Exceeded maximum of 3 needs per production city");
            }

            ret_val.need[need_count] = ResPair.get(s, m, game);

            need_count++;

            s = Util.cleanLine(in, line_nr);    // Read next line
            m = pattern.matcher(s);
            m.find();
            first_string = s.substring(m.start() + 1, m.end() - 1).trim();
        }

        // Read "make" line
        if (!first_string.equalsIgnoreCase("make")) {
            throw new Exception("String \"make\" expected. Found: " + s);
        }

        ret_val.make = ResPair.get(s, m, game);

        // Read closing brace line
        s = Util.cleanLine(in, line_nr);    // Skip closing brace
        if (!s.startsWith("}")) {
            throw new Exception("Character } expected. Found: " + s);
        }

        return ret_val;
    }

    /**
     * Reads and returns the whole production table from PROD.DAT *
     *
     * @param game Needed to access the resource names
     */
    public static Prod[] readProdDat(Game game) {

        Prod[] ret_val = new Prod[C.PROD_CITIES];

        s = "";
        int[] line_nr = {0};
        try (BufferedReader in = new BufferedReader(new FileReader(FN.S_PROD_DAT))) {

            for (int city_type = 0; city_type < C.PROD_CITIES; city_type++) {    // For each city type

                ret_val[city_type] = getProdForCity(city_type, in, game, line_nr);
            }

        } catch (Exception e) {
//            e.printStackTrace(System.out);
            System.out.println("Error reading file: " + FN.S_PROD_DAT);
            if (e.getMessage() != null) {
                System.out.println("Exception: " + e.getMessage());
            } else {
                System.out.println("Unknown error. Probably file missing or insufficient data.");
            }
            System.out.println("Last line read: " + s);
            Util.logEx(null, e);
            Util.logFFErrorAndExit(FN.S_PROD_DAT, line_nr[0]);
            System.exit(1);
        }

        return ret_val;
    }
}
