package game;

import dat.EfsIni;
import dat.StrBuild;
import dat.Harvest;
import dat.Prod;
import dat.ResPair;
import dat.ResType;
import galaxyreader.Planet;
import galaxyreader.Structure;
import galaxyreader.Unit;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;    // DEBUG
import util.C;
import util.StackIterator;
import util.Util;

/**
 * Handles economic stuff that's not tied to Faction data
 *
 * @author RSW
 */
public class Economy implements Serializable {

    private Game game;
    private EfsIni efs_ini;
    private List<Planet> planets;
    private List<Unit> units;
    private List<Structure> structures;
    private Resources resources;

    private ResPair[][][][] harvest_table;
    private Prod[] prod_table;
    private ResType[] res_types;

    private int turn;    // = faction number. Copied from game.turn each turn

    public Economy(Game game, Resources resources) {
        // Store references to game data, for later use
        this.game = game;
        this.efs_ini = game.getEfs_ini();
        this.planets = game.getPlanets();
        this.units = game.getUnits();
        this.structures = game.getStructures();
        this.res_types = game.getResTypes();
        this.resources = resources;

        harvest_table = Harvest.readHarvestDats(game);    // Read in harvest data from FARM.DAT, etc        
        prod_table = Prod.readProdDat(game);    // Read in secondary production data from PROD.DAT

//        testPrintProdTable();
//        testPrintHarvestTable();
//        testPrintResTypeTable();
    }

    /**
     * Update one faction's holdings at start of its turn
     *
     */
    public void updateEconomy(int turn) {

        this.turn = turn;

        System.out.println(" ");
        System.out.println("Starting turn for FACTION " + turn);
        System.out.println(" ");

        collectResources();

        regainHealth();

        if (turn < C.NUMBER_OF_HOUSES) {    // Some updates are for Houses only
            if (efs_ini.consume_food) {
                feedUnitsAndCities();
            }
        }

    }

    /**
     * Current faction's units and cities regain health.
     *
     */
    private void regainHealth() {

        for (Structure city : structures) {
            if (city.owner == turn) {
                city.health = Math.min(city.health + efs_ini.city_heal_rate, 100);
            }
        }

        for (Unit unit : units) {
            if (unit.owner == turn) {
                if (game.unitInCity(unit)) {
                    unit.health = Math.min(unit.health + efs_ini.unit_heal_in_city, 100);
                } else {
                    unit.health = Math.min(unit.health + efs_ini.unit_heal, 100);
                }
            }
        }
    }

    /**
     * Current faction's units and cities consume food.
     *
     * NOTE: Units eat a maximum of 1 food (if type_data.eat > 0). Cities all
     * eat 10 food.
     *
     */
    private void feedUnitsAndCities() {    // RSW

        // Feed cities one planet at a time.
        for (Planet planet : planets) {
            int food_available = resources.countResourcesAvailable(planet.index, turn)[C.RES_FOOD];
            int food_needed = 0;

            for (Structure city : structures) {    // Search the structure list for relevant cities and count them
                if (city.p_idx == planet.index) {
                    if (city.owner == turn) {      // Relevant city
                        int this_city_needs = city.health / 10;    // City consumes 1 food for each complete 10% health
                        food_needed += this_city_needs;
                        int deficit = food_needed - food_available;
                        if (deficit > 0) {
                            city.turns_starving += 1;
                            double health_loss = (city.health / 2.0);
//                            System.out.println("A Deficit = "+deficit+", Health = "+city.health+", Loss = "+ health_loss);
                            if (deficit < this_city_needs) {    // Had enough to feed city partly, so reduce health loss proportionally
                                health_loss = health_loss * ((double) deficit / this_city_needs);
                            }
                            city.health = Math.max(city.health - (int) health_loss, 0);
//                            System.out.println("B Deficit = "+deficit+", Health = "+city.health+", Loss = "+ health_loss);
                        } else {
                            city.turns_starving = 0;
                        }
                    }
                }
            }
            if (food_needed > 0) {
                System.out.println("Faction " + turn + " on planet " + planet.index + " needs "
                        + food_needed + " food for cities. " + food_available + " available.");
                testPrintResources(planet.index);

                if (food_available > 0) {
                    resources.consumeOneResourceType(planet.index, turn, C.RES_FOOD,
                            Math.min(food_needed, food_available));
                }

                testPrintResources(planet.index);

                if (food_needed > food_available) {
                    System.out.println("FAMINE!!! Faction " + turn + " has "
                            + (food_needed - food_available) + " food deficit on planet " + planet.index);
                }
            }
        }

        // Feed units one planet at a time
        for (Planet planet : planets) {
            int food_available = resources.countResourcesAvailable(planet.index, turn)[C.RES_FOOD];
            int food_needed = 0;

            for (Unit unit : units) {    // Search the unit list for relevant units and count them
                if (unit.p_idx == planet.index) {
                    if (unit.owner == turn && unit.type_data.eat != 0 && !unit.in_space) {    // Relevant unit
                        food_needed += 1;
                        if (food_needed > food_available) {
                            unit.turns_starving += 1;
                            unit.health = Math.max(unit.health - efs_ini.health_loss_for_famine, 0);
                        } else {
                            unit.turns_starving = 0;
                        }
                    }
                }
            }
            if (food_needed > 0) {
                System.out.println("Faction " + turn + " on planet " + planet.index + " needs "
                        + food_needed + " food for cities. " + food_available + " available.");
                testPrintResources(planet.index);

                if (food_available > 0) {
                    resources.consumeOneResourceType(planet.index, turn, C.RES_FOOD,
                            Math.min(food_needed, food_available));
                }

                testPrintResources(planet.index);

                if (food_needed > food_available) {
                    System.out.println("FAMINE!!! Faction " + turn + " has "
                            + (food_needed - food_available) + " food deficit on planet " + planet.index);
                }
            }
        }
    }

    /**
     * Do all production (harvesting and secondary) for current faction.
     *
     */
    private void collectResources() {

        int[] resource_amounts;    // Resources of each type

        for (Structure city : structures) {    // For each structure in structure list
            if (city.owner == turn) {          // Current faction only
                resource_amounts = calculateActualProduction(city);
                resources.addResourcesToHex(city.p_idx, city.x, city.y, city.owner, resource_amounts);
            }
        }
    }

    /**
     * Calculate how many resources a city produces per turn (modified for
     * loyalty, traits, etc). Is not affected by whether secondary city has
     * enough inputs (on a particular turn).
     *
     * @param city Structure
     * @return Array of resource amounts, one per resource type
     *
     */
    public int[] calculateActualProduction(Structure city) {    //RSW

        int[] resource_amounts;    // Resources of each type

        resource_amounts = calculateBaseProduction(city);

        //    NOT YET IMPLEMENTED. Will adjust for loyalty and health
        return resource_amounts;
    }

    /**
     * Calculate how many resources a city potentially produces per turn
     * (ignoring loyalty etc).
     *
     * @param city Structure
     * @return Array of resource amounts, one per resource type
     *
     */
    public int[] calculateBaseProduction(Structure city) {    //RSW

        int[] resource_amounts;    // Resources of each type. Initialised to 0 by default.

        switch (city.type) {
            case C.FARM:
                resource_amounts = calculateHarvest(city, C.FARM_HARVESTING);
                break;
            case C.WELL:
                resource_amounts = calculateHarvest(city, C.WELL_HARVESTING);
                break;
            case C.MINE:
                resource_amounts = calculateHarvest(city, C.MINE_HARVESTING);
                break;
            case C.ARBORIUM:
                resource_amounts = calculateHarvest(city, C.ARBORIUM_HARVESTING);
                break;
//            case C.CHEMICALS:
//                resource_amounts = calculateSecondaryProduction(city, C.CHEMICALS_PRODUCTION);
//                break; 
//            case C.ELECTRONICS:
//                resource_amounts = calculateSecondaryProduction(city, C.ELECTRONICS_PRODUCTION);
//                break; 
//            case C.BIOPLANT:
//                resource_amounts = calculateSecondaryProduction(city, C.BIOPLANT_PRODUCTION);
//                break; 
//            case C.CERAMSTEEL:
//                resource_amounts = calculateSecondaryProduction(city, C.CERAMSTEEL_PRODUCTION);
//                break; 
//            case C.WETWARE:
//                resource_amounts = calculateSecondaryProduction(city, C.WETWARE_PRODUCTION);
//                break; 
//            case C.CYCLOTRON:
//                resource_amounts = calculateSecondaryProduction(city, C.CYCLOTRON_PRODUCTION);
//                break; 
//            case C.FUSORIUM:
//                resource_amounts = calculateSecondaryProduction(city, C.FUSORIUM_PRODUCTION);
//                break;
            default:
                resource_amounts = new int[C.RES_TYPES];    // Must create new, so values will be initialised to zero
                break;
        }
        return resource_amounts;
    }

    /**
     * Calculate how many resources a city harvests per turn.
     *
     * @param city Structure
     * @param harvest_type Harvesting city type, 0-3
     * @return Array of resource amounts, one per resource type
     *
     */
    public int[] calculateHarvest(Structure city, int harvest_type) {    //RSW

        int[] resource_amounts = new int[C.RES_TYPES];    // Will accumulate resource amounts. Initialised to 0 by default.

        Planet planet = planets.get(city.p_idx);    // Get planet that city's on
        Hex city_hex = game.getHexFromPXY(city.p_idx, city.x, city.y);    // Get hex that city's in

        int radius = 2;    // TEMPORARY - should get from STRBUILD.DAT

        Set<Hex> hex_set = getHexesWithinRadiusOf(city_hex, radius);    // Get the set of all hexes within 2 hexes of the city
        for (Hex hex : hex_set) {    // For each hex in the set

            // Get resources from terrain
            boolean[] terrain_array = hex.getTerrain();    // Get boolean array of terrain types for the hex

            // Find top-most terrain in hex, excluding road
            int terrain = 0;
            for (int i = 0; i < C.HARVEST_TERRAINS; i++) {    // C.HARVEST_TERRAINS is number of possible terrains, excluding road, i.e. 11
                if (terrain_array[i]) {
                    terrain = i;    // Remember the last true terrain
                }
            }

            for (int j = 0; j < 3; j++) {    // For up to 3 possible resource types per hex
                // When < 3 types, remaining items in array will be null, so check for that
                if (harvest_table[harvest_type][terrain][planet.tile_set_type][j] == null) {
                    break;
                }
                int resource_type = harvest_table[harvest_type][terrain][planet.tile_set_type][j].resource_type;
                int resource_amount = harvest_table[harvest_type][terrain][planet.tile_set_type][j].resource_amount;
                resource_amounts[resource_type] += resource_amount;    // Accumulate the resource
            }
        }

        // For cities close to map edge, up-rate pro-rata for imaginary hexes off the edge of the map
        for (int i = 0; i < resource_amounts.length; i++) {    // For each resource type
            int resource_amount = resource_amounts[i];
            if (resource_amount > 0) {
                int hexes = hex_set.size();
                int max_hexes = 3 * radius * (radius + 1) + 1;
                if (hexes < max_hexes) {
                    resource_amounts[i] = (int) Math.ceil((float) resource_amount * max_hexes / hexes);    // Round up
                }
            }
        }

        // Add resources from any special resource symbols
        for (Hex hex : hex_set) {    // For each hex within the city radius

            Structure special = hex.getResource();

            if (special != null) {
                switch (special.type) {
                    case C.TRACE_SPECIAL:
                        if (harvest_type == C.MINE_HARVESTING) {
                            resource_amounts[C.RES_TRACE] += 20;
                        }
                        break;
                    case C.GEMS_SPECIAL:
                        if (harvest_type == C.MINE_HARVESTING) {
                            resource_amounts[C.RES_GEMS] += 5;
                        }
                        break;
                    case C.EXOTICA_SPECIAL:
                        if (harvest_type == C.FARM_HARVESTING || harvest_type == C.ARBORIUM_HARVESTING) {
                            resource_amounts[C.RES_EXOTICA] += 10;
                        }
                        break;
                    case C.FERTILE_SPECIAL:
                        if (harvest_type == C.FARM_HARVESTING || harvest_type == C.ARBORIUM_HARVESTING) {
                            resource_amounts[C.RES_FOOD] += 20;
                        }
                        break;
                    case C.METAL_SPECIAL:
                        if (harvest_type == C.MINE_HARVESTING) {
                            resource_amounts[C.RES_METAL] += 20;
                        }
                        break;
                    case C.ENERGY_SPECIAL:
                        if (harvest_type == C.WELL_HARVESTING) {
                            resource_amounts[C.RES_ENERGY] += 20;
                        }
                        break;
                }
            }
        }
        return resource_amounts;
    }

    public Set<Hex> getHexesWithinRadiusOf(Hex hex, int radius) {   //RSW

        // Find the neighbours of the given hex, and then the neighbours of those neighbours. 
        // To avoid adding the same hex more than once, use Set instead of List, as that leaves it to the RTE to avoid duplicates 
        Set<Hex> ret_val = new HashSet<>();    // Set of hexes to be returned 
        LinkedList<Hex> queue = new LinkedList<>();
        LinkedList<Integer> queueR = new LinkedList<>();

        ret_val.add(hex);
        if (radius < 1) {
            return ret_val;
        }

        queue.add(hex);
        queueR.add(new Integer(0));
        while (!queue.isEmpty()) {
            Hex father = queue.pop();
            int r = queueR.pop().intValue();
            Hex[] neighbours = father.getNeighbours();
            for (Hex child : neighbours) {
                if (child != null && ret_val.add(child)) {
                    int child_r = r + 1;
                    if (child_r < radius) {
                        queue.add(child);
                        queueR.add(new Integer(child_r));
                    }
                }
            }
        }
        return ret_val;
    }

    // FOR TESTING
    public void testPrintProdTable() {

        System.out.println("PROD TABLE");
        for (int i = 0; i < C.PROD_CITIES; i++) { // TESTING Print the prod table
            System.out.println("City prod type: " + i);
            for (int j = 0; j < 3; j++) {
                if (prod_table[i].need[j] != null) {
                    System.out.println("    Needs " + res_types[prod_table[i].need[j].resource_type].name
                            + " " + prod_table[i].need[j].resource_amount);
                }
            }
            System.out.println("    Makes " + res_types[prod_table[i].make.resource_type].name
                    + " " + prod_table[i].make.resource_amount);
        }
    }

    public void testPrintHarvestTable() {

        System.out.println("HARVEST TABLE");
        for (int i = 0; i < 4; i++) {    // For each city type
            System.out.println("City type: " + i);
            for (int j = 0; j < C.HARVEST_TERRAINS; j++) {    // For each terrain type
                System.out.println("    Terrain type: " + j);
                for (int k = 0; k < C.HARVEST_PLANETS; k++) {    // For each planet type
                    System.out.println("        Planet type: " + k);
                    for (int l = 0; l < 3; l++) {
                        if (harvest_table[i][j][k][l] != null) {
                            System.out.println("            Resource type: " + harvest_table[i][j][k][l].resource_type
                                    + ", amount: " + harvest_table[i][j][k][l].resource_amount);
                        }
                    }
                }
            }
        }
    }

    public void testPrintResTypeTable() {

        System.out.println("RES TYPE TABLE");
        for (int i = 0; i < C.RES_TYPES; i++) {    // For each city type
            System.out.println(res_types[i].name + ", " + res_types[i].singular
                    + ", " + res_types[i].price + ", " + res_types[i].description);
        }
    }

    public void testPrintResources(int p_idx) {

        if (turn == 0) {
            System.out.println("Faction " + turn + " resources: "
                    + Arrays.toString(resources.countResourcesAvailable(p_idx, turn)));
        }
    }
}
