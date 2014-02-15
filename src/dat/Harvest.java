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

/**
 *
 * @author RSW (based on TerrCost.java) Reads in harvesting data from FARM.DAT,
 * ARBORIUM.DAT, MINE.DAT and WELL.DAT. The harvest table is 4-D: city-types (4)
 * X terrain-types X planet-types X resource-types (up to 3) and each item in
 * the table is a Harvest object, i.e. a resource type and amount. Unused slots
 * left null.
 */
public class Harvest implements Serializable {

    public int resource_type;
    public int resource_amount;

    public Harvest(int resource_type, int resource_amount) {
        this.resource_type = resource_type;
        this.resource_amount = resource_amount;
    }

    public static Harvest[] getHarvests(String s) throws Exception {    // Get harvest data from one one row of harvest file, up to 3 resource types

        int resource_type;
        int resource_amount;
        String resource_name;

        Harvest[] ret_val = new Harvest[3];

        Pattern pattern = Pattern.compile("\"[^\"]*\"");    // Find anything in quotes, including empty string
        Matcher m = pattern.matcher(s);
        m.find();   //skip "planet type"

        for (int i = 0; i < 3; i++) {    // For up to 3 resource types in row
            m.find();   // Get resource name
            resource_name = s.substring(m.start() + 1, m.end() - 1).trim();

            if (resource_name.equals("@")) {
                break;    // "@" signals end of row -- no resource here
            }
            boolean found = false;    // Find corresponding resource name
            for (resource_type = 0; resource_type < C.S_RESOURCE.length; resource_type++) {
                if (resource_name.equalsIgnoreCase(C.S_RESOURCE[resource_type])) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new Exception(resource_name + " is not a valid resource type, in line: " + s);
            }

            m.find();    // Get resource amount
            resource_amount = Integer.parseInt(s.substring(m.start() + 1, m.end() - 1));
            if (resource_amount < 0) {
                throw new Exception("Number cannot be negative (" + resource_amount + "), in line:" + s);
            }

            ret_val[i] = new Harvest(resource_type, resource_amount);
        }
        return ret_val;
    }

    public static String cleanLine(BufferedReader in) throws Exception {    // Get one line of input, skipping comments and white space

        String s = in.readLine().trim();
        while (s.startsWith("//") || s.equals("")) {
            s = in.readLine().trim();
        }
        return s;
    }

    public static Harvest[][][] getHarvestsForCity(String file_name) {    // Get harvest data for one city type, i.e. one file

        Harvest[][][] ret_val = new Harvest[C.HARVEST_TERRAINS][C.HARVEST_PLANETS][];

        String s = "(None)";    // Initialisation just ensures a suitable error message if the file can't be read at all

        try (BufferedReader in = new BufferedReader(new FileReader(file_name))) {

            for (int terrain_type = 0; terrain_type < C.HARVEST_TERRAINS; terrain_type++) {    // For each terrain type

                s = cleanLine(in);    // Skip opening brace
                if (!s.startsWith("{")) {
                    throw new Exception("Character { expected. Found: " + s);
                }

                for (int planet_type = 0; planet_type < C.HARVEST_PLANETS; planet_type++) {    // For each planet type
                    s = cleanLine(in);
                    ret_val[terrain_type][planet_type] = getHarvests(s);    // Process one line of data
                }

                s = cleanLine(in);    // Skip closing brace
                if (!s.startsWith("}")) {
                    throw new Exception("Character } expected. Found: " + s);
                }
            }

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Error reading file: " + file_name);
            System.out.println("Last line read: " + s);

            System.exit(1);
        }

        return ret_val;
    }

    public static Harvest[][][][] readHarvest() {    // Creates and returns harvest table, reading all 4 harvest data files

        Harvest[][][][] harvest_table = new Harvest[4][][][];

        harvest_table[C.FARM_HARVESTING] = getHarvestsForCity(C.S_FARM_DAT);
        harvest_table[C.WELL_HARVESTING] = getHarvestsForCity(C.S_WELL_DAT);
        harvest_table[C.MINE_HARVESTING] = getHarvestsForCity(C.S_MINE_DAT);
        harvest_table[C.ARBORIUM_HARVESTING] = getHarvestsForCity(C.S_ARBORIUM_DAT);

        return harvest_table;
    }
}
