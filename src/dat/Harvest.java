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
 * Reads in harvesting data from FARM.DAT, ARBORIUM.DAT, MINE.DAT and WELL.DAT.
 * The harvest table is 4-D: city-types (4) X terrain-types X planet-types X
 * resource-types (up to 3) and each item in the table is a ResTypeAmount
 * object, i.e. a resource type and amount. If there are less than 3 resource
 * types, the unused slots are left null.
 *
 * @author RSW (based on TerrCost.java)
 */
public class Harvest implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Gets harvest data from one one row of harvest file, from line passed to
     * it
     */
    private static ResPair[] getOneRow(String s, Game game) throws Exception {

        ResPair[] ret_val = new ResPair[3];

        Pattern pattern = Pattern.compile("\"[^\"]*\"");    // Find anything in quotes, including empty string

        // Skip planet type
        Matcher m = pattern.matcher(s);
        m.find();

        // Get up to 3 resource type-amount pairs
        for (int i = 0; i < 3; i++) {

            ResPair temp = ResPair.get(s, m, game);
            if (temp == null) {    // Null used to signal end of row (at an "@")
                break;
            } else {
                ret_val[i] = temp;
            }
        }
        return ret_val;
    }

    /**
     * Gets harvest data for one city type, i.e. one file
     */
    private static ResPair[][][] getOneCity(String file_name, Game game) {

        ResPair[][][] ret_val = new ResPair[C.HARVEST_TERRAINS][C.HARVEST_PLANETS][];

        String s = "";

        try (BufferedReader in = new BufferedReader(new FileReader(file_name))) {

            for (int terrain_type = 0; terrain_type < C.HARVEST_TERRAINS; terrain_type++) {    // For each terrain type

                s = Util.cleanLine(in);    // Skip opening brace
                if (!s.startsWith("{")) {
                    throw new Exception("Character { expected. Found: " + s);
                }

                for (int planet_type = 0; planet_type < C.HARVEST_PLANETS; planet_type++) {    // For each planet type
                    s = Util.cleanLine(in);
                    ret_val[terrain_type][planet_type] = getOneRow(s, game);
                }

                s = Util.cleanLine(in);    // Skip closing brace
                if (!s.startsWith("}")) {
                    throw new Exception("Character } expected. Found: " + s);
                }
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Error reading file: " + file_name);
            System.out.println("Last line read: " + s);

            System.exit(1);
        }

        return ret_val;
    }

    /**
     * Creates and returns harvest table, reading all 4 harvest data files
     *
     * @param game Needed to access the resource names
     */
    public static ResPair[][][][] readHarvestDats(Game game) {

        ResPair[][][][] harvest_table = new ResPair[4][][][];

        harvest_table[C.FARM_HARVESTING] = getOneCity(FN.S_FARM_DAT, game);
        harvest_table[C.WELL_HARVESTING] = getOneCity(FN.S_WELL_DAT, game);
        harvest_table[C.MINE_HARVESTING] = getOneCity(FN.S_MINE_DAT, game);
        harvest_table[C.ARBORIUM_HARVESTING] = getOneCity(FN.S_ARBORIUM_DAT, game);

        return harvest_table;
    }
}
