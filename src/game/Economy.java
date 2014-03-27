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
import java.util.Arrays;
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

    private int turn;    // = faction number. Obtained from game.turn each turn

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

        // Initialize production/consumption data
        this.resources.initializeProdCons(this, prod_table);
//        testPrintProdTable();
//        testPrintHarvestTable();
//        testPrintResTypeTable();
    }

    public ResType[] getResType() {
        return res_types;
    }

    public String getResName(int resource) {
        return res_types[resource].name;
    }

    public void updateProdConsForCity(Structure city, boolean add) {
        resources.updateProdConsForCity(this, prod_table, city, add);
    }

    /**
     * Update one faction's holdings at start of its turn
     *
     */
    public void updateEconomy(int turn) {

        this.turn = turn;

        System.out.println(" ");    // TESTING
        System.out.println("Starting turn for FACTION " + turn);
        System.out.println(" ");

        if (turn < C.NR_HOUSES) {    // Houses only for now
            collectResources();
        }

        regainHealth();

        if (turn < C.NR_HOUSES) {    // Houses only for now
            if (efs_ini.consume_food) {
                feedUnitsAndCities();
            }
        }

        if (turn == 0) {
            resources.printPodLists();    // TESTING
            resources.verifyPodLists();    // TESTING
        }

    }

    /**
     * Current faction's units and cities regain health.
     *
     */
    private void regainHealth() {

        for (Structure city : structures) {
            if (city.owner == turn) {
                game.adjustCityHealth(city, Math.min(city.health + efs_ini.city_heal_rate, 100));
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
     * NOTE: Units eat a maximum of 1 food (if type_data.eat > 0). Cities eat 1
     * food per 10% health (i.e. per unit of population).
     *
     */
    private void feedUnitsAndCities() {

        for (Planet planet : planets) {    // For each planet

            List<Unit> dead_units = new LinkedList<>();
            int food_available = resources.getResourcesAvailable(planet.index, turn)[C.RES_FOOD];
            int food_needed = 0;

            // First work out how much food is needed on this planet
            Util.HexIter iter1 = Util.getHexIter(game, planet.index);    // For each hex of the planet
            for (Hex hex = iter1.next(); hex != null; hex = iter1.next()) {

                // Feed any city in the hex (if it has the right owner)
                Structure city = hex.getStructure();
                if (city != null && city.owner == turn) {      // There's a city of the current faction

                    int this_city_needs = city.health / 10;    // City consumes 1 food for each complete 10% health
                    food_needed += this_city_needs;
                    int deficit = food_needed - food_available;
                    if (deficit > 0) {
                        city.turns_starving += 1;
                        double health_loss = (city.health / 2.0);
                        if (deficit < this_city_needs) {    // Had enough to feed city partly, so reduce health loss proportionally
                            health_loss = health_loss * ((double) deficit / this_city_needs);
                        }
                        game.adjustCityHealth(city, Math.max(city.health - (int) health_loss, 0));
//                        if (city.health == 0) {
//                            game.deleteStructure(city);    // Not needed at present; cities can't fall to 0 health without plague.
//                        }
                    } else {
                        city.turns_starving = 0;
                    }
                }

                // Feed any units in the hex's stack (if they have the right owner)
                List<Unit> stack = hex.getStack();
                StackIterator iter2 = new StackIterator(stack);
                for (Unit unit = iter2.next(); unit != null; unit = iter2.next()) {    // For each unit in stack (including cargo)
                    if (unit.owner == turn && unit.type_data.eat && !unit.in_space) {    // Unit needs feeding
                        food_needed += 1;
                        if (food_needed > food_available) {
                            unit.turns_starving += 1;
                            unit.health = Math.max(unit.health - efs_ini.health_loss_for_famine, 0);
                            if (unit.health == 0) {
                                dead_units.add(unit);    // Wait to delete unit until we leave loop over its stack
                            }
                        } else {
                            unit.turns_starving = 0;
                        }
                    }
                }
            }

            // Now consume the resources
            if (food_needed > 0) {

                System.out.println("Faction " + turn + " on planet " + planet.name + " needs "
                        + food_needed + " food for cities and units. " + food_available + " available.");
                testPrintResources(planet.index);

                if (food_available > 0) {
                    int food_consumed = Math.min(food_needed, food_available);
                    resources.consumeOneResourceType(planet.index, turn, C.RES_FOOD, food_consumed);
                }

                testPrintResources(planet.index);

                if (food_needed > food_available) {
                    String msg = "Famine on planet " + planet.name + "!";
                    game.getFaction(turn).addMessage(new Message(msg, C.Msg.FAMINE, game.getYear(), planet));
                    System.out.println(msg);
                }

                // Now delete dead units
                for (Unit unit : dead_units) {
                    game.deleteUnitNotInCombat(unit);
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

        List<Structure> cities = new LinkedList<>();
        for (Structure city : structures) {    // For each structure in structure list
            if (city.owner == turn) {          // Current faction only
                cities.add(city);
            }
        }
        // produce in order so that prerequisite resources are produced first
        for (int i : C.PRODUCTION_ORDER) {
            for (Structure city : cities) {

                if (city.type == i) {
                    resource_amounts = calculateActualProduction(city);
                    resources.addResourcesToHex(city.p_idx, city.x, city.y, city.owner, resource_amounts);
                }
            }
        }
//        for (Structure city : structures) {    // For each structure in structure list
//            if (city.owner == turn) {          // Current faction only
//                resource_amounts = calculateActualProduction(city);
//                resources.addResourcesToHex(city.p_idx, city.x, city.y, city.owner, resource_amounts);
//            }
//        }
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
    public int[] calculateActualProduction(Structure city) {

        int[] resource_amounts;    // Resources of each type

        resource_amounts = calculateBaseProduction(city);

        //    NOT YET IMPLEMENTED. Will adjust for loyalty and health
        return resource_amounts;
    }

    /**
     * Calculate how many resources a city potentially produces per turn
     * (ignoring loyalty etc). This method can be called on cities that don't
     * produce resources, and will return values set to zero.
     *
     * @param city Structure
     * @return Array of resource amounts, one per resource type
     *
     */
    public int[] calculateBaseProduction(Structure city) {

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
            case C.CHEMICALS:
                resource_amounts = calculateSecondaryProduction(city, C.CHEMICALS_PRODUCTION);
                break;
            case C.ELECTRONICS:
                resource_amounts = calculateSecondaryProduction(city, C.ELECTRONICS_PRODUCTION);
                break;
            case C.BIOPLANT:
                resource_amounts = calculateSecondaryProduction(city, C.BIOPLANT_PRODUCTION);
                break;
            case C.CERAMSTEEL:
                resource_amounts = calculateSecondaryProduction(city, C.CERAMSTEEL_PRODUCTION);
                break;
            case C.WETWARE:
                resource_amounts = calculateSecondaryProduction(city, C.WETWARE_PRODUCTION);
                break;
            case C.CYCLOTRON:
                resource_amounts = calculateSecondaryProduction(city, C.CYCLOTRON_PRODUCTION);
                break;
            case C.FUSORIUM:
                resource_amounts = calculateSecondaryProduction(city, C.FUSORIUM_PRODUCTION);
                break;
            default:
                resource_amounts = new int[C.RES_TYPES];    // Must create new array, so values will be initialised to zero
                break;
        }
        return resource_amounts;
    }

    /**
     * Calculate how many resources a harvesting city potentially produces per
     * turn (ignoring loyalty etc).
     *
     * @param city Structure
     * @param harvest_type Harvesting city type, 0-3 (index into harvest table)
     * @return Array of resource amounts, one per resource type
     *
     */
    public int[] calculateHarvest(Structure city, int harvest_type) {

        int[] resource_amounts = new int[C.RES_TYPES];    // Will return resource amounts. Initialised to 0 by default.

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
                ResPair pair = harvest_table[harvest_type][terrain][planet.tile_set_type][j];
                if (pair == null) {    // When < 3 types, remaining items in array will be null, so check for that
                    break;
                }
                resource_amounts[pair.resource_type] += pair.resource_amount;    // Accumulate the resource
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

    /**
     * Calculate how many resources a secondary production city potentially
     * produces per turn (ignoring loyalty etc).
     *
     * @param city Structure
     * @param production_type Secondary production type, 0-6 (index into prod
     * table)
     * @return Array of resource amounts, one per resource type
     *
     */
    public int[] calculateSecondaryProduction(Structure city, int production_type) {

        int[] resource_amounts = new int[C.RES_TYPES];    // Will return resource amounts. Initialised to 0 by default.

        // Check whether the city has enough of all the resource types it needs
        boolean enough_of_all = true;
        for (int i = 0; i < 3; i++) {    // For up to 3 possible resource needs
            ResPair need = prod_table[production_type].need[i];
            if (need == null) {    // When < 3 needs, remaining items in array will be null, so check for that
                break;
            }
            boolean enough = resources.checkOneResourceType(city.p_idx, city.owner, need.resource_type, need.resource_amount);
            if (!enough) {
                enough_of_all = false;
                break;
            }
        }

        testPrintResources(city.p_idx);    // TESTING

        ResPair make = prod_table[production_type].make;

        if (enough_of_all) {

            // Consume the resources needed
            for (int i = 0; i < 3; i++) {    // For up to 3 possible resource needs
                ResPair need = prod_table[production_type].need[i];
                if (need == null) {    // When < 3 needs, remaining items in array will be null, so check for that
                    break;
                }
                resources.consumeOneResourceType(city.p_idx, city.owner, need.resource_type, need.resource_amount);
                System.out.println("City type " + game.getStrBuild(city.type).name + " consuming "
                        + need.resource_amount + " " + game.getResTypes()[need.resource_type].name);
            }

            // Return the resources made
            resource_amounts[make.resource_type] = make.resource_amount;

            System.out.println("City type " + game.getStrBuild(city.type).name + " producing "
                    + make.resource_amount + " " + game.getResTypes()[make.resource_type].name);
        } else {
            String res_name = game.getResTypes()[make.resource_type].name;
            Planet planet = planets.get(city.p_idx);
            String msg = "You do not have all the required resources to produce " + res_name + " on planet " + planet.name;
            game.getFaction(turn).addMessage(new Message(msg, C.Msg.CANNOT_PRODUCE, game.getYear(), planet));
            System.out.println(msg);
        }
        return resource_amounts;
    }

    /**
     * Find and return the set of all hexes within a certain radius of a given
     * hex.
     *
     * Typically this is called with radius 2, for harvesting cities, in which
     * case it returns a set of 17 hexes (unless the radius extends off the top
     * or bottom of the map.
     *
     * @param hex The central hex
     * @param radius The distance in hexes to extend the search
     * @return Set of hexes within the radius
     *
     */
    public Set<Hex> getHexesWithinRadiusOf(Hex hex, int radius) {

        // Find the neighbours of the given hex, and then the neighbours of those neighbours, etc. 
        // To avoid adding the same hex more than once, use Set instead of List, as that leaves it
        // to the JRE to avoid duplicates 
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

    // ================================================================================
    //
    //          METHODS BELOW THIS LINE ARE ONLY FOR TESTING
    //
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
                    + Arrays.toString(resources.getResourcesAvailable(p_idx, turn)));
        }
    }
}
