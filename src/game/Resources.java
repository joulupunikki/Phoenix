package game;

import dat.EfsIni;
import dat.Prod;
import dat.ResPair;
import galaxyreader.Planet;
import galaxyreader.Structure;
import galaxyreader.Unit;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import util.C;
import util.StackIterator;
import util.Util;

/**
 * General methods for handling resources
 *
 * @author RSW
 */
public class Resources implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    // Game data
    private Game game;
    private EfsIni efs_ini;
    private List<Planet> planets;
    private List<Unit> units;
    private List<Structure> structures;

    // Local data
    private List<List<List<LinkedList<Unit>>>> all_pods;
    private int resource_total[][][];
    // production and consumption of resources by production/consumption, faction, planet, resource
    private int prod_cons[][][][];

    public Resources(Game game) {

        this.game = game;
        this.efs_ini = game.getEfs_ini();
        this.planets = game.getPlanets();
        this.units = game.getUnits();
        this.structures = game.getStructures();

        generatePodLists();
    }

//    public int getProdCons(int pc, int f_idx, int p_idx, int res) {
//        return prod_cons[pc][f_idx][p_idx][res];
//    }
    /**
     * Add amount to prod_cons. Can be negative.
     *
     * @param pc
     * @param f_idx
     * @param p_idx
     * @param res
     * @param amount
     */
    public void addToProdCons(int pc, int f_idx, int p_idx, int res, int amount) {
        prod_cons[pc][f_idx][p_idx][res] += amount;
    }

    public int[][][][] getProdCons() {
        return prod_cons;
    }

    /**
     * Get resources budget balance ie (production - consumption) for each
     * resource. Returns planet balance if universal warehouse is off, galaxy
     * balance otherwise.
     *
     * @param f_idx
     * @param p_idx
     * @return
     */
    public int[] getProdConsBalance(int f_idx, int p_idx) {
        int[] ret_val = new int[C.RES_TYPES];
        if (game.getEfs_ini().universal_warehouse) {
            for (int i = 0; i < prod_cons[C.PROD][f_idx].length; i++) {
                for (int j = 0; j < ret_val.length; j++) {
                    ret_val[j] -= prod_cons[C.CONS][f_idx][i][j];
                    ret_val[j] += prod_cons[C.PROD][f_idx][i][j];

                }

            }
        } else {
            for (int j = 0; j < ret_val.length; j++) {
                ret_val[j] -= prod_cons[C.CONS][f_idx][p_idx][j];
                ret_val[j] += prod_cons[C.PROD][f_idx][p_idx][j];

            }
        }

        return ret_val;
    }

    /**
     * Initializes production and consumption data. The prod_cons variable
     * contains production and consumption of resources by
     * production/consumption, faction, planet, resource.
     *
     * @param econ
     */
    public void initializeProdCons(Economy econ, Prod[] prod_table) {

        int nr_planets = planets.size();
        prod_cons = new int[2][][][];
        for (int i = 0; i < prod_cons.length; i++) {

            prod_cons[i] = new int[C.NR_FACTIONS][][];

            for (int f_idx = 0; f_idx < C.NR_FACTIONS; f_idx++) {

                prod_cons[i][f_idx] = new int[nr_planets][];

                for (int p_idx = 0; p_idx < nr_planets; p_idx++) {

                    prod_cons[i][f_idx][p_idx] = new int[C.RES_TYPES];

                    for (int r_type = 0; r_type < C.RES_TYPES; r_type++) {

                        prod_cons[i][f_idx][p_idx][r_type] = 0;
                    }
                }
            }
        }

        for (Structure city : structures) {
            updateProdConsForCity(econ, prod_table, city, true);
        }
        for (Unit unit : units) {
            if (unit.type_data.eat && !unit.in_space) {
                prod_cons[C.CONS][unit.owner][unit.p_idx][C.RES_FOOD] += 1;
            }
        }
    }

    /**
     * Update prod_cons with city's data.
     *
     * @param econ
     * @param prod_table
     * @param city
     * @param add add if true subtract if false
     */
    public void updateProdConsForCity(Economy econ, Prod[] prod_table, Structure city, boolean add) {
        if (add) {
            prod_cons[C.CONS][city.owner][city.p_idx][C.RES_FOOD] += city.health / 10;
        } else {
            prod_cons[C.CONS][city.owner][city.p_idx][C.RES_FOOD] -= city.health / 10;
        }
        switch (city.type) {

            case C.FARM:

            case C.ARBORIUM:

            case C.WELL:

            case C.MINE:
                addHarvestToProdCons(econ.calculateActualProduction(city), city, add);
                break;
            case C.CHEMICALS:

            case C.BIOPLANT:

            case C.ELECTRONICS:

            case C.CERAMSTEEL:

            case C.WETWARE:

            case C.FUSORIUM:

            case C.CYCLOTRON:
                addProductionToProdCons(city, prod_table, Util.productionType(city), add);
                break;
            default:
                break;
        }
    }

    /**
     * Add or subtract harvest of a city to or from prod_cons
     *
     * @param amounts
     * @param city
     * @param add add if true, subtract if false
     */
    public void addHarvestToProdCons(int[] amounts, Structure city, boolean add) {
        for (int i = 0; i < amounts.length; i++) {
            //should be allready adjusted for loyalty & health
            //by call to calculateActualProduction
            if (add) {
                prod_cons[C.PROD][city.owner][city.p_idx][i] += amounts[i];
            } else {
                prod_cons[C.PROD][city.owner][city.p_idx][i] -= amounts[i];
            }
        }
    }

    /**
     * Add or subtract production and consumption of a city to or from prod_cons
     *
     * @param city
     * @param prod_table
     * @param prod_type
     * @param add add if true, subtract if false
     */
    public void addProductionToProdCons(Structure city, Prod[] prod_table, int prod_type, boolean add) {
        for (int i = 0; i < 3; i++) {    // For up to 3 possible resource needs
            ResPair need = prod_table[prod_type].need[i];
            if (need == null) {    // When < 3 needs, remaining items in array will be null, so check for that
                break;
            }
            if (add) {
                prod_cons[C.CONS][city.owner][city.p_idx][need.resource_type] += need.resource_amount;
            } else {
                prod_cons[C.CONS][city.owner][city.p_idx][need.resource_type] -= need.resource_amount;
            }
        }
        ResPair make = prod_table[prod_type].make;
        //TODO adjust for loyalty and health
        if (add) {
            prod_cons[C.PROD][city.owner][city.p_idx][make.resource_type] += make.resource_amount; // * (city.health / 100.0) * (city.loyalty / 100.0);
        } else {
            prod_cons[C.PROD][city.owner][city.p_idx][make.resource_type] -= make.resource_amount;
        }
    }

    /**
     * Add multiple resource types to a hex, creating new cargo pod if needed
     *
     * @param p_idx, x, y Planet index and hex coordinates of location
     * @param owner Faction number of new owner
     * @param amount[] Array of resource amounts to add, one per resource type
     */
    public void addResourcesToHex(int p_idx, int x, int y, int owner, int[] amount) {

        for (int type = 0; type < C.RES_TYPES; type++) {
            addOneResourceTypeToHex(p_idx, x, y, owner, type, amount[type]);
        }
    }

    /**
     * Add one type of resource to a hex, creating new cargo pod if needed (Any
     * resources that can't be fitted into the hex will be lost.)
     *
     * @param p_idx, x, y Planet index and hex coordinates of location
     * @param owner Faction number of new owner
     * @param resource_type Type of resource
     * @param amount Amount to be added
     */
    public void addOneResourceTypeToHex(int p_idx, int x, int y, int owner, int resource_type, int amount) {

        List<Unit> stack = game.getHexFromPXY(p_idx, x, y).getStack();    // Get the stack in the hex

        int amount_still_to_add = amount;
        while (amount_still_to_add > 0) {

            // Look for a suitable pod in the stack already that we can add to
            StackIterator iterator = new StackIterator(stack);    // Go through stack (including passengers)
            Unit unit = iterator.next();
            while (unit != null) {
                if (unit.type == C.CARGO_UNIT_TYPE && unit.res_relic == resource_type && unit.amount < 999) {
                    break;
                }
                unit = iterator.next();
            }
            // Now unit = null or suitable pod

            if (unit == null) {    // Need a new pod, so create an empty one
                unit = game.createUnitInHex(p_idx, x, y, owner, C.CARGO_UNIT_TYPE, 0, resource_type, 0);
                if (unit == null) {
                    break;    // But couldn't create unit in this hex, so give up on this resource type
                }
            }
            // Now add resources to the pod
            int amount_can_be_added = 999 - unit.amount;
            int amount_to_add_here = Math.min(amount_still_to_add, amount_can_be_added);
            adjustPodResources(unit, amount_to_add_here);
            amount_still_to_add -= amount_to_add_here;
        }
    }

    /**
     * Check if there are sufficient resources to take some action (e.g.
     * building a unit) on a particular planet. With Universal Warehouse ON the
     * resources may be taken from another planet.
     *
     * @param p_idx Index of planet where resources are needed
     * @param owner Faction number
     * @param amount_needed[] Array of resource amounts needed, one per resource
     * type
     * @return True if there are enough resources (including all amounts = 0).
     */
    public boolean checkResources(int p_idx, int owner, int[] amount_needed) {

        int[] amount_available = getResourcesAvailable(p_idx, owner);
        boolean enough = true;

        for (int type = 0; type < C.RES_TYPES; type++) {
            if (amount_available[type] < amount_needed[type]) {
                enough = false;
                break;
            }
        }

        return enough;
    }

    /**
     * Check if there is enough of one resource type available to use on a
     * particular planet. With Universal Warehouse ON the resources may be taken
     * from another planet.
     *
     * @param p_idx Index of planet where resources are needed
     * @param owner Faction number
     * @param type Type of resource
     * @param amount Amount to be consumed
     * @return True if there are enough resources (including amount = 0).
     */
    public boolean checkOneResourceType(int p_idx, int owner, int type, int amount) {

        int[] amount_available = getResourcesAvailable(p_idx, owner);
        boolean enough = true;

        if (amount_available[type] < amount) {
            enough = false;
        }

        return enough;
    }

    /**
     * Consume multiple resource types needed on a particular planet. With
     * Universal Warehouse ON the resources may be taken from another planet.
     *
     * @param p_idx Index of planet where resources are needed
     * @param owner Faction number
     * @param amount_needed Array of resource amounts needed, one per resource
     * type
     * @return True if there were enough resources (including all amounts = 0);
     * false if not enough, in which case none are consumed.
     */
    public boolean consumeResources(int p_idx, int owner, int[] amount_needed) {

        boolean enough = checkResources(p_idx, owner, amount_needed);

        if (enough) {
            for (int type = 0; type < C.RES_TYPES; type++) {
                consumeOneResourceType(p_idx, owner, type, amount_needed[type]);
            }
        }

        return enough;
    }

    /**
     * Consume one type of resource needed on a particular planet. With
     * Universal Warehouse ON the resources may be taken from another planet.
     *
     * @param p_idx Planet (index) where resources are needed
     * @param owner Faction number
     * @param type Type of resource
     * @param amount Amount to be consumed
     * @return True if there were enough resources (including amount = 0); false
     * if not enough, in which case none are consumed.
     */
    public boolean consumeOneResourceType(int p_idx, int owner, int type, int amount) {

        int still_needed = amount;
        boolean enough = true;

        int list_p_idx = (efs_ini.universal_warehouse) ? 0 : p_idx;
        LinkedList<Unit> pod_list = all_pods.get(owner).get(list_p_idx).get(type);

        while (still_needed > 0) {
            if (pod_list.isEmpty()) {
                enough = false;
                break;
            }
            Unit pod = pod_list.element();    // Get first pod on list (without removing)
            still_needed -= takeResourcesFromPod(pod, still_needed);
        }

        return enough;
    }

    /**
     * Get the total resources of each type available to use on a given planet.
     * With Universal Warehouse ON this will include resources on all planets.
     *
     * @param owner Faction number
     * @param p_idx Planet index
     * @return Array of resource amounts, one per resource type
     */
    public int[] getResourcesAvailable(int p_idx, int owner) {

        int list_p_idx = (efs_ini.universal_warehouse) ? 0 : p_idx;
        return resource_total[owner][list_p_idx];

    }

    /**
     * Consume resources from a particular cargo pod.
     *
     * @param unit Cargo pod
     * @param amount_needed Amount to be consumed
     * @return Amount actually consumed (less than amount_needed, if pod doesn't
     * have enough)
     *
     */
    public int takeResourcesFromPod(Unit unit, int amount_needed) {

        int amount_consumed = Math.min(amount_needed, unit.amount);

        adjustPodResources(unit, -1 * amount_consumed);

        if (unit.amount == 0) {    // Pod now empty
            game.deleteUnitNotInCombat(unit);
        }
        return amount_consumed;
    }

    /**
     * Generates a set of pod lists, so that pods can easily be found when
     * resources are consumed. There's one list of pods for each faction, for
     * each planet, for each resource type. These lists are continually updated,
     * by addToPodLists and removeFromPodListsLists. Resource totals are
     * initialised at the same time.
     */
    public void generatePodLists() {

        // With universal warehouse, pretend there's only one planet
        int nr_planets = (efs_ini.universal_warehouse) ? 1 : planets.size();

        // Create tree of empty lists (leaf lists in tree are lists of units)
        // Also create tree of arrays for resource totals (leaf arrays will be zeroed automatically)
        all_pods = new ArrayList<>(C.NR_FACTIONS);
        resource_total = new int[C.NR_FACTIONS][][];

        for (int f_idx = 0; f_idx < C.NR_FACTIONS; f_idx++) {
            List<List<LinkedList<Unit>>> faction_pods = new ArrayList<>(nr_planets);
            all_pods.add(faction_pods);
            resource_total[f_idx] = new int[nr_planets][];

            for (int p_idx = 0; p_idx < nr_planets; p_idx++) {
                List<LinkedList<Unit>> planet_pods = new ArrayList<>(C.RES_TYPES);
                faction_pods.add(planet_pods);
                resource_total[f_idx][p_idx] = new int[C.RES_TYPES];

                for (int r_type = 0; r_type < C.RES_TYPES; r_type++) {

                    LinkedList<Unit> res_type_pods = new LinkedList<>();
                    planet_pods.add(res_type_pods);
                }
            }
        }

        // Now get initial data from the general unit list
        for (Unit unit : units) {
            addToPodLists(unit);
        }
    }

    /**
     * Adds a cargo pod to the pod lists
     *
     * @param unit A cargo pod
     */
    public void addToPodLists(Unit unit) {

        // Pod in space are added to the end of the list for a given planet, so they get consumed last
        // With universal warehouse, pretend there's only one planet
        if (unit.type == C.CARGO_UNIT_TYPE) {
            int p_idx = (efs_ini.universal_warehouse) ? 0 : unit.p_idx;
            if (unit.in_space) {
                all_pods.get(unit.owner).get(p_idx).get(unit.res_relic).addLast(unit);
            } else {
                all_pods.get(unit.owner).get(p_idx).get(unit.res_relic).addFirst(unit);
            }
            resource_total[unit.owner][p_idx][unit.res_relic] += unit.amount;
        }
    }

    /**
     * Removes a cargo pod from the pod lists
     *
     * @param unit A cargo pod
     */
    public void removeFromPodLists(Unit unit) {

        if (unit.type == C.CARGO_UNIT_TYPE) {
            int p_idx = (efs_ini.universal_warehouse) ? 0 : unit.p_idx;
            all_pods.get(unit.owner).get(p_idx).get(unit.res_relic).removeFirstOccurrence(unit);
            resource_total[unit.owner][p_idx][unit.res_relic] -= unit.amount;
        }
    }

    /**
     * Adjusts the amount of resource in a cargo pod. Only to be called by
     * addResourcesToHex and takeResourcesFromPod (made public to enable calling
     * from CargoPanel.) Assumes that caller has already checked adjustment is
     * legal (i.e. result is in range 0 to 999).
     *
     * @param unit A cargo pod
     * @param amount Amount to be added; may be negative for a decrease.
     *
     */
    public void adjustPodResources(Unit unit, int amount) {

        unit.amount += amount;

        // Update running count of resource totals
        int p_idx = (efs_ini.universal_warehouse) ? 0 : unit.p_idx;
        resource_total[unit.owner][p_idx][unit.res_relic] += amount;
    }

    /**
     * TEST UTILITY for creating lots of units
     *
     * @param num_planets Number of planets on which to create units (starting
     * from planet 0).
     * @param num_stacks Number of stacks to create per planet. Will be paced
     * starting from hex [0,0], land hexes only.
     * @param num_factions You'll get a mix of this number of factions on each
     * planet (starting from faction 0). 1 = all Li Halan.
     * @param num_units Number of units to put in each stack. You'll get a
     * variety of types.
     * @param first_type Each stack will contain one unit of each type starting
     * at this point in UNIT.DAT (e.g. 45 = Noble).
     */
    public void testFillGalaxy(int num_planets, int num_stacks, int num_factions, int num_units, int first_type) {
        for (int p_idx = 0; p_idx < Math.min(num_planets, planets.size()); p_idx++) {

            // Do one planet
            Planet planet = planets.get(p_idx);
            int stack_count = 0;
            int faction = 0;

            for (int x = 0; x < C.PLANET_MAP_WIDTH; x++) {
                for (int y = 0; y < C.PLANET_MAP_COLUMNS - 1; y++) {
                    if (stack_count >= num_stacks) {
                        break;
                    }

                    // Do one hex
                    Hex hex = planet.planet_grid.getHex(x, y);    // Get hex
                    List<Unit> stack = hex.getStack();    // Get stack
                    if (!stack.isEmpty()) {
                        continue;    // Skip hex if it contains units already
                    }
                    if (hex.getStructure() != null) {
                        continue;    // Skip hex if it contains a city/structure
                    }
                    if (hex.getTerrain()[C.OCEAN]) {
                        continue;    // Skip ocean hexes
                    }
                    // Hex OK. Make a stack

                    int unit_type = first_type;
                    for (int unit_count = 0; unit_count < Math.min(num_units, 20); unit_count++) {

                        if (unit_type == C.MoveType.NAVAL.ordinal()) {
                            continue;    // Skip Naval types
                        }
                        Unit unit = game.createUnitInHex(p_idx, x, y, faction, unit_type, 0, 0, 0);

                        unit_type++;
                        if (unit_type > 91) {
                            unit_type = 0;    // Cycle back to start of the unit type list 
                        }
                    }
                    stack_count++;
                    faction++;
                    if (faction == num_factions) {
                        faction = 0;
                    }
                }
            }
        }
    }

    // ================================================================================
    //
    //           METHODS BELOW THIS LINE ARE ONLY FOR TESTING
    //
    /**
     * For testing purposes, check that the pod lists have remained accurate.
     * Generate a new set of pod lists from the general unit list, and check it
     * against the accumulated set. Do the same for the resource totals.
     */
    public void verifyPodLists() {

        generateTestPodLists();
        comparePodLists();
    }

    List<List<List<LinkedList<Unit>>>> test_pods;
    private int test_total[][][];

    /**
     * Make a complete test of the pod lists (all_pods to test_pods) Also test
     * the resource totals
     */
    private void generateTestPodLists() {

        // With universal warehouse, pretend there's only one planet
        int nr_planets = (efs_ini.universal_warehouse) ? 1 : planets.size();

        // Create tree of empty lists (leaf lists in tree are lists of units)
        // Also create tree of arrays for resource totals (leaf arrays will be zeroed automatically)
        test_pods = new ArrayList<>(C.NR_FACTIONS);
        test_total = new int[C.NR_FACTIONS][][];

        for (int f_idx = 0; f_idx < C.NR_FACTIONS; f_idx++) {
            List<List<LinkedList<Unit>>> faction_pods = new ArrayList<>(nr_planets);
            test_pods.add(faction_pods);
            test_total[f_idx] = new int[nr_planets][];

            for (int p_idx = 0; p_idx < nr_planets; p_idx++) {
                List<LinkedList<Unit>> planet_pods = new ArrayList<>(C.RES_TYPES);
                faction_pods.add(planet_pods);
                test_total[f_idx][p_idx] = new int[C.RES_TYPES];

                for (int r_type = 0; r_type < C.RES_TYPES; r_type++) {

                    LinkedList<Unit> res_type_pods = new LinkedList<>();
                    planet_pods.add(res_type_pods);
                }
            }
        }

        // Now get initial data from the general unit list
        for (Unit unit : units) {
            if (unit.type == C.CARGO_UNIT_TYPE) {
                int p_idx = (efs_ini.universal_warehouse) ? 0 : unit.p_idx;
                if (unit.in_space) {
                    test_pods.get(unit.owner).get(p_idx).get(unit.res_relic).addLast(unit);
                } else {
                    test_pods.get(unit.owner).get(p_idx).get(unit.res_relic).addFirst(unit);
                }
                test_total[unit.owner][p_idx][unit.res_relic] += unit.amount;
            }
        }
    }

    /**
     * Compare all_pods with test_pods. The lists needn't be identical, just
     * contain the same units (in any order). Also compare resource_totals with
     * test_totals
     *
     * If mismatch found, prints message and terminates
     */
    private void comparePodLists() {

        int nr_planets = (efs_ini.universal_warehouse) ? 1 : planets.size();

        for (int f_idx = 0; f_idx < C.NR_FACTIONS; f_idx++) {
            for (int p_idx = 0; p_idx < nr_planets; p_idx++) {
                for (int r_type = 0; r_type < C.RES_TYPES; r_type++) {

                    LinkedList<Unit> list1 = all_pods.get(f_idx).get(p_idx).get(r_type);
                    LinkedList<Unit> list2 = test_pods.get(f_idx).get(p_idx).get(r_type);

                    for (int i = 0; i < list1.size(); i++) {
                        Unit unit = list1.get(i);
                        if (!list2.contains(unit)) {
                            podMismatch(f_idx, p_idx, r_type, "Unit in position " + i
                                    + " of accumulated list doesn't appear in newly-generated list");
                        }
                    }
                    for (int i = 0; i < list2.size(); i++) {
                        Unit unit = list2.get(i);
                        if (!list1.contains(unit)) {
                            podMismatch(f_idx, p_idx, r_type, "Unit in position " + i
                                    + " of newly-generated list doesn't appear in accumulated list");
                        }
                    }

                    if (list1.size() != list2.size()) {
                        podMismatch(f_idx, p_idx, r_type, "Accumulated list and newly-generated list are different sizes");
                    }

                    if (test_total[f_idx][p_idx][r_type] != resource_total[f_idx][p_idx][r_type]) {
                        podMismatch(f_idx, p_idx, r_type, "Resource totals don't match");
                    }

                }
            }
        }
    }

    private void podMismatch(int f_idx, int p_idx, int r_type, String message) {

        LinkedList<Unit> list1 = all_pods.get(f_idx).get(p_idx).get(r_type);
        LinkedList<Unit> list2 = test_pods.get(f_idx).get(p_idx).get(r_type);

        System.out.println("=====================================================");
        System.out.println("*** Error in Class Resources (method verifyPodLists)");
        System.out.println("*** Mis-match between accumulated pod lists (or resource totals) and newly-generated ones");
        System.out.println("*** " + message);

        String res_name = game.getResTypes()[r_type].name;
        String planet_name = game.getPlanet(p_idx).name;
        if (efs_ini.universal_warehouse) {
            planet_name += " (UW)";
        }
        System.out.println("FACTION = " + f_idx + ", PLANET = " + planet_name + ", TYPE = " + res_name);

        System.out.println("Pods on accumulated list...");
        for (int i = 0; i < list1.size(); i++) {
            Unit unit = list1.get(i);
            res_name = game.getResTypes()[unit.res_relic].name;
            planet_name = game.getPlanet(unit.p_idx).name;
            if (efs_ini.universal_warehouse) {
                planet_name += " (UW)";
            }
            System.out.println(i + ": owner = " + unit.owner + ", planet = " + planet_name + ", "
                    + "type = " + res_name + " (" + unit.amount + "), in_space = " + unit.in_space);
        }
        System.out.println("Accumulated resource total = " + resource_total[f_idx][p_idx][r_type]);

        System.out.println("Pods on newly-generated list...");
        for (int i = 0; i < list2.size(); i++) {
            Unit unit = list2.get(i);
            res_name = game.getResTypes()[unit.res_relic].name;
            planet_name = game.getPlanet(unit.p_idx).name;
            if (efs_ini.universal_warehouse) {
                planet_name += " (UW)";
            }
            System.out.println(i + ": owner = " + unit.owner + ", planet = " + planet_name + ", "
                    + "type = " + res_name + " (" + unit.amount + "), in_space = " + unit.in_space);
        }
        System.out.println("Newly-generated resource total = " + test_total[f_idx][p_idx][r_type]);

        System.exit(1);
    }

    public void printPodLists() {

        System.out.println("PODS IN POD LISTS...");

        int nr_planets = (efs_ini.universal_warehouse) ? 1 : planets.size();
//        for (int f_idx = 0; f_idx < C.NR_FACTIONS; f_idx++) {
        for (int f_idx = 0; f_idx < 1; f_idx++) {    // LI HALAN ONLY!
            for (int p_idx = 0; p_idx < nr_planets; p_idx++) {
                for (int r_type = 0; r_type < C.RES_TYPES; r_type++) {
                    LinkedList<Unit> pod_list = all_pods.get(f_idx).get(p_idx).get(r_type);
                    for (Unit unit : pod_list) {
                        String res_name = game.getResTypes()[unit.res_relic].name;
                        String planet_name = game.getPlanet(unit.p_idx).name;
                        if (efs_ini.universal_warehouse) {
                            planet_name += " (UW)";
                        }
                        System.out.println("Pod owner = " + unit.owner + ", "
                                + "planet = " + planet_name + ", "
                                + "type = " + res_name + " (" + unit.amount + "), "
                                + "in_space = " + unit.in_space);
                    }
                }
            }
        }
    }

//        if (turn == 0) {
//            int[] resource_amounts = {1,0,3,4,0,0,0,0,0,0,0,0,0};
//            boolean b = consumeResources(14, turn, resource_amounts) ;
//            System.out.println("b1: " + b);
//
//            int[] resource_amounts2 = {50,2,3,4,0,0,0,0,0,0,0,0,50};
//            b = consumeResources(14, turn, resource_amounts2) ;
//            System.out.println("b2 " + b);
//        }
//        if (turn == 0) {
//            
//            for (Planet planet : planets) {
//                System.out.println("Planet: " + planet.index + " " + planet.name);
//            }
//            
//            efs_ini.universal_warehouse = false;
//            testCon (true, "Version 1, Univeral Warehouse OFF"); 
//            testCon (false, "Version 2, Univeral Warehouse OFF"); 
//
//            efs_ini.universal_warehouse = true;
//            testCon (true, "Version 1, Univeral Warehouse ON"); 
//            testCon (false, "Version 2, Univeral Warehouse ON");
//        }
//   
//    private void testCon(boolean first, String message) {
//        
//        int loops = 1000;
//        long time1;
//        long time2;
//        boolean success;
//        
//        System.out.println(message);
//        time1 = System.currentTimeMillis();
//        for (int i = 0; i < loops; i++) {
//            if (first) {
//                success = consumeResources(36, 0, 10, 1);
//            } else {
//                success = consumeResources(36, 0, 10, 1);
//            }
//            if (success) System.out.println("Found some resources, but shouldn't have");
//        }
//        time2 = System.currentTimeMillis();
//        System.out.println("    Elapsed milliseconds: " + (time2 - time1));
//    }
}
