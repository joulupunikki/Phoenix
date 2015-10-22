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
import java.io.Serializable;
import java.util.regex.Matcher;
import util.C;
import util.FN;

/**
 * A ResPair is a resource type and amount, representing an amount of one
 * resource.
 *
 * @author RSW
 */
public class ResPair implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public int resource_type;
    public int resource_amount;

    /**
     * Create a ResPair object.
     *
     * @param resource_type
     * @param resource_amount
     */
    public ResPair(int resource_type, int resource_amount) {

        this.resource_type = resource_type;
        this.resource_amount = resource_amount;

    }

    /**
     * Get one pair (resource type and amount) from a DAT file. Used by Harvest
     * and Prod. Needs game to access the resource names.
     *
     * @param s
     * @param m
     * @param game
     * @return
     * @throws java.lang.Exception
     */
    public static ResPair get(String s, Matcher m, Game game) throws Exception {

        m.find();            // Get resource type
        String resource_name = s.substring(m.start() + 1, m.end() - 1).trim();

        if (resource_name.equals("@")) {
            return null;    //    Used in harvest files to signal no more resource types
        }

        int resource_type = getResourceTypeFromName(resource_name, game);

        m.find();        // Get resource amount
        int resource_amount = Integer.parseInt(s.substring(m.start() + 1, m.end() - 1));
        if (resource_amount < 0) {
            throw new Exception("Number cannot be negative (" + resource_amount + "), in line: " + s);
        }
        return new ResPair(resource_type, resource_amount);
    }

    /**
     * Given a resource name (string), returns the corresponding resource type
     * number
     *
     * @param resource_name
     * @param game
     * @return
     * @throws java.lang.Exception
     */
    public static int getResourceTypeFromName(String resource_name, Game game) throws Exception {

        int resource_type;

        boolean found = false;    // Find corresponding resource type number
        for (resource_type = 0; resource_type < C.RES_TYPES; resource_type++) {
            if (resource_name.equalsIgnoreCase(game.getResTypes()[resource_type].name)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new Exception("\"" + resource_name + "\" is not a resource type defined in " + FN.S_RES_DAT);
        }

        return resource_type;
    }

}
