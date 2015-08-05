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
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import util.C;
import util.FN;
import util.Util;

/**
 * Reads in resource type data from files RES.DAT.
 *
 * @author RSW
 */
public class ResType implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public String name;
    public String singular;
    public int price;
    public String description;

    private ResType() {
    }

    /**
     * Gets data about one resource type, from line passed to it
     */
    private static ResType getOneType(String s) throws Exception {

        ResType ret_val = new ResType();

        Pattern pattern = Pattern.compile("\"[^\"]*\"");    // Find anything in quotes, including empty string
        Matcher m = pattern.matcher(s);

        // Get plural name (the one usually used)
        m.find();    // Skip word "name"
        m.find();
        String name = s.substring(m.start() + 1, m.end() - 1).trim();
        if (name.equals("")) {
            throw new Exception("Empty string where resource name expected.");
        } else {
            ret_val.name = name;
        }

        // Get singular name
        m.find();    // Skip word "singular"
        m.find();
        String singular = s.substring(m.start() + 1, m.end() - 1).trim();
        if (singular.equals("")) {
            throw new Exception("Empty string where resource name expected.");
        } else {
            ret_val.singular = singular;
        }

        // Get price
        m.find();    // Skip word "price"
        m.find();
        int price = Integer.parseInt(s.substring(m.start() + 1, m.end() - 1));
        if (price < 0) {
            throw new Exception("Price cannot be negative (" + price + "), in line: " + s);
        } else {
            ret_val.price = price;
        }

        // Get description
        m.find();    // Skip word "description"
        m.find();
        String description = s.substring(m.start() + 1, m.end() - 1).trim();
        ret_val.description = description;

        return ret_val;
    }

    /**
     * Creates and returns resource types table, reading RES.DAT
     *
     * @return
     */
    public static ResType[] readResDat() {    // Creates and returns resource type table

        ResType[] res_types = new ResType[C.RES_TYPES];

        String s = "";

        try (BufferedReader in = new BufferedReader(new FileReader(FN.S_RES_DAT))) {

            s = Util.cleanLine(in);    // Skip opening brace
            if (!s.startsWith("{")) {
                throw new Exception("Character { expected. Found: " + s);
            }

            for (int i = 0; i < C.RES_TYPES; i++) {
                s = Util.cleanLine(in);
                res_types[i] = getOneType(s);
            }

            s = Util.cleanLine(in);    // Skip closing brace
            if (!s.startsWith("}")) {
                throw new Exception("Character } expected. Found: " + s);
            }

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Error reading file: " + FN.S_RES_DAT);
            System.out.println("Last line read: " + s);

            System.exit(1);
        }

        return res_types;
    }
}
