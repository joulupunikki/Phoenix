package dat;

import game.Game;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import util.C;
import util.Util;

/**
 * @author RSW Reads in PROD.DAT, which give data about secondary resource
 * production. For each type of secondary production city (e.g. chemicals) there
 * are up to 3 input resource types ("need") and one output resource type
 * ("make"). Unused needs slots are left null. For each resource type needed or
 * made, there is a type and amount.
 */
public class Prod implements Serializable {

    public ResPair[] need = new ResPair[3];   // Up to 3 resources (type + amount) which the city needs
    public ResPair make;                      // One resource (type + amount) which the city makes

    private static String s;    // This is just here so that readProdDat can print lines read by getProdForCity

    /**
     * Reads production data for one city type
     */
    private static Prod getProdForCity(int city_type, BufferedReader in, Game game) throws Exception {

        Prod ret_val = new Prod();

        // Read opening brace line
        s = Util.cleanLine(in);
        if (!s.startsWith("{")) {
            throw new Exception("Character { expected. Found: " + s);
        }

        // Read "city" line
        s = Util.cleanLine(in);
        Pattern pattern = Pattern.compile("\"[^\"]*\"");    // Find anything in quotes, including empty string
        Matcher m = pattern.matcher(s);
        m.find();
        String first_string = s.substring(m.start() + 1, m.end() - 1).trim();
        if (!first_string.equalsIgnoreCase("city")) {
            throw new Exception("String \"city\" expected. Found: " + s);
        }
        // For now, don't check city type
        // Read "need" lines

        s = Util.cleanLine(in);    // Read one line ahead, as we don't know how many "need" lines there are
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

            s = Util.cleanLine(in);    // Read next line
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
        s = Util.cleanLine(in);    // Skip closing brace
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

        try (BufferedReader in = new BufferedReader(new FileReader(C.S_PROD_DAT))) {

            for (int city_type = 0; city_type < C.PROD_CITIES; city_type++) {    // For each city type

                ret_val[city_type] = getProdForCity(city_type, in, game);
            }

        } catch (Exception e) {
//            e.printStackTrace(System.out);
            System.out.println("Error reading file: " + C.S_PROD_DAT);
            if (e.getMessage() != null) {
                System.out.println("Exception: " + e.getMessage());
            } else {
                System.out.println("Unknown error. Probably file missing or insufficient data.");
            }
            System.out.println("Last line read: " + s);
            System.exit(1);
        }

        return ret_val;
    }
}
