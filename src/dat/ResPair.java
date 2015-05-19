package dat;

import game.Game;
import java.io.Serializable;
import java.util.regex.Matcher;
import util.C;
import util.FN;

/**
 * @author RSW A ResPair is a resource type and amount, representing an amount
 * of one resource.
 */
public class ResPair implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public int resource_type;
    public int resource_amount;

    public ResPair(int resource_type, int resource_amount) {

        this.resource_type = resource_type;
        this.resource_amount = resource_amount;

    }

    /**
     * Get one pair (resource type and amount) from a DAT file. Used by Harvest
     * and Prod. Needs game to access the resource names.
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
