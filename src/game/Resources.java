package game;

import dat.EfsIni;
import galaxyreader.Planet;
import galaxyreader.Structure;
import galaxyreader.Unit;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Arrays;    // DEBUG
import util.C;
import util.StackIterator;
import util.Util;

/**
 * General methods for handling resources
 *
 * @author RSW
 */
public class Resources implements Serializable {

    private Game game;
    private EfsIni efs_ini;
    private int turn;
    private List<Planet> planets;
    private List<Unit> units;
    private List<Structure> structures;

    public Resources(Game game) {

        this.game = game;
        this.efs_ini = game.getEfs_ini();
        this.planets = game.getPlanets();
        this.units = game.getUnits();
        this.structures = game.getStructures();
    }

    /**
     * Add resources to a hex, creating new cargo pod if needed
     *
     * @param p_idx, x, y Planet index and hex coordinates of location
     * @param owner Faction number of new owner
     * @param resource_amounts Array of resource amounts to add, one per
     * resource type
     */
    public void addResourcesToHex(int p_idx, int x, int y, int owner, int[] resource_amounts) {    //RSW

        List<Unit> stack = game.getHexFromPXY(p_idx, x, y).getStack();    // Get the stack in the city hex

        for (int resource_type = 0; resource_type < resource_amounts.length; resource_type++) {

            int amount = resource_amounts[resource_type];
            if (amount <= 0) {
                continue;
            }

            // Look for same cargo already in stack (every hex has a stack, possibly empty)      
            boolean found = false;
            Unit old_unit = null;

            StackIterator iterator = new StackIterator(stack);    // For each unit in stack (including cargo)
            Unit unit = iterator.next();
            while (unit != null) {
                if (unit.type == C.CARGO_UNIT_TYPE && unit.res_relic == resource_type) {
                    found = true;
                    old_unit = unit;
                }
                unit = iterator.next();
            }

            if (found) {
                old_unit.amount = Math.min(old_unit.amount + amount, 999);    // Add resources to existing cargo pod, up to 999.
            } else {
                if (Util.stackSize(stack) < 20) {     // Create new cargo pod (if there's room in stack)
                    game.createUnitInHex(p_idx, x, y, owner, C.CARGO_UNIT_TYPE, 0, resource_type, amount);
                }
            }
        }
    }

    /**
     * Count how many resources of each type are available on a given planet.
     * With Universal Warehouse ON this will count resources on all planets.
     *
     * @param owner Faction number
     * @param p_idx Planet index
     * @return Array of resource amounts, one per resource type
     *
     */
    public int[] countResourcesAvailable(int p_idx, int owner) {

        int[] ret_val = new int[C.S_RESOURCE.length];    // Will accumulate resource amounts. Initialised to 0 by default.

        for (Unit unit : units) {    // Go through whole unit list, looking for this faction's cargo pods
            if (unit.type == C.CARGO_UNIT_TYPE && unit.owner == owner) {
                if (efs_ini.universal_warehouse || unit.p_idx == p_idx) {    // If UW is ON, or pod is on the right planet
                    ret_val[unit.res_relic] += unit.amount;    // Accumulate the resources
                }
            }
        }
        return ret_val;
    }
//    
//    /**
//     * Count food available on every planet.
//     *
//     * @param owner Faction number
//     * @return Array of food amounts, one per planet 
//     * IMPORTANT: With Universal Warehouse ON, there is only one food amount, returned in the first element of the array
//     * 
//     */
//    private int[] countFoodAvailable(int owner) {
//
//        int[] ret_val = new int[planets.size()];    // Will accumulate resource amounts. Initialised to 0 by default.
//
//        for (Unit unit : units) {    // Go through whole unit list, looking for this faction's cargo pods
//            if (unit.type == C.CARGO_UNIT_TYPE && unit.owner == owner) {
//                if (efs_ini.universal_warehouse) {
//                    ret_val[0] += unit.amount;    // Accumulate the resources in the first element of the array
//                } else {
//                    ret_val[unit.p_idx] += unit.amount;    // Accumulate the resources under the right planet
//                }
//            }
//        }
//        return ret_val;
//    }
//    

    private List<Unit> getExpandedStack(List<Unit> stack) {    // Returns a temporary stack with cargo listed separately 
        List<Unit> ret_val = new LinkedList<>();
        for (Unit unit : stack) {
            ret_val.add(unit);
            for (Unit cargo : unit.cargo_list) {
                ret_val.add(cargo);
            }
        }
        return ret_val;
    }

//    // ALTERNATIVE VERSION, which searches through the unit list, instead of through hexes.           
//    /**
//     * Consumes resources on a particular planet.
//     * With Universal Warehouse ON the resources may be taken from another planet.
//     * Consumes multiple resource types. This is more efficient than searching for each
//     *
//     * @param owner Faction number
//     * @param p_idx Planet index
//     * @param resource_type Type of resource
//     * @param amount_needed Amount to be consumed
//     * @return True if there were enough resources (including amount_needed = 0); false if not enough, in which case none are consumed.
//     * 
//     */
//    public boolean consumeResources1(int p_idx, int owner, int resource_type, int amount_needed) {
//            
//        if (amount_needed <= 0) System.out.println("WARNING: Trying to consume amount <= 0 in method consumeResources");
////        if (amount_needed <= 0) {
////            return true;
////        }
//        
//        List<Unit> pod_list = new LinkedList<>();    // We'll create a temporary list of the suitable cargo pods we find
//        int resources_found = 0;                     // and keep track of how many resources are in them
//        
//        // First check that there are enough resources available
//
//    search:
//        for (Unit unit : units) {    // Go through whole unit list, looking for suitable cargo pods
//            if (unit.type == C.CARGO_UNIT_TYPE && unit.res_relic == resource_type && unit.owner == owner) {
//                if (efs_ini.universal_warehouse || unit.p_idx == p_idx) {    // If UW is ON, or pod is on the right planet
//                    pod_list.add(unit);
//                    resources_found += unit.amount;
//                    if (resources_found >= amount_needed) {
//                        break search;    // Stop searching as soon as we've found enough
//                    }
//                }
//            }
//        }
//        if (resources_found < amount_needed) {
//            return false;
//        }
//            
//        // There are enough resources, so take them from the pods we found
//        
//        int resources_still_to_take = amount_needed;
//        for (Unit unit : pod_list) {
//                        resources_still_to_take -= takeResourcesFromPod(unit, resources_still_to_take);
//        }
//        return true;
//     }
    public boolean consumeOneResourceType(int p_idx, int owner, int type, int amount) {

        int[] resource_amounts = new int[C.S_RESOURCE.length];    // Initialised to 0 by default.

        resource_amounts[type] = amount;

        boolean success = consumeResources(p_idx, owner, resource_amounts);

        return success;
    }

    /**
     * Consumes resources on a particular planet. With Universal Warehouse ON
     * the resources may be taken from another planet. Consumes multiple
     * resource types. This is more efficient than searching for each
     *
     * @param owner Faction number
     * @param p_idx Index of planet where resources are needed
     * @param resource_amounts Array of resource amounts to be consumed, one per
     * resource type
     * @return True if there were enough resources (including amount_needed =
     * 0); false if not enough, in which case none are consumed.
     *
     */
    public boolean consumeResources(int p_idx, int owner, int[] resource_amounts) {

        List<List<Unit>> list_of_lists = new LinkedList<>();
        List<Integer> list_of_types = new LinkedList<>();

        for (int resource_type = 0; resource_type < resource_amounts.length; resource_type++) {
            int amount_needed = resource_amounts[resource_type];
            if (amount_needed <= 0) {
                continue;
            }
            List<Unit> pod_list = findPods(p_idx, owner, resource_type, amount_needed);
            if (pod_list == null) {
                return false;    // Return immediately as there are not enough of this resource type
            } else {
                list_of_lists.add(pod_list);
                list_of_types.add(new Integer(resource_type));
            }
        }
        //  If we got to here, there are enough resources; now go through the list and consume them

        for (int i = 0; i < list_of_lists.size(); i++) {
            List<Unit> pod_list = list_of_lists.get(i);
            int resource_type = list_of_types.get(i);
            int amount_still_needed = resource_amounts[resource_type];
            // We have just the right number of pods to meet our needs, but the last one
            for (Unit unit : pod_list) {    // may not be all needed, so we must keep track of how much still to go
                amount_still_needed -= takeResourcesFromPod(unit, amount_still_needed);
            }
        }
        return true;
    }

    /**
     * Consumes resources on a particular planet. With Universal Warehouse ON
     * the resources may be taken from another planet. Consumes multiple
     * resource types. This is more efficient than searching for each
     *
     * @param owner Faction number
     * @param p_idx Index of planet where resources are needed
     * @param resource_type Type of resource
     * @param amount_needed Amount to be consumed
     * @return True if there were enough resources (including amount_needed =
     * 0); false if not enough, in which case none are consumed.
     *
     */
    public List<Unit> findPods(int p_idx, int owner, int resource_type, int amount_needed) {

        if (amount_needed <= 0) {
            System.out.println("WARNING: Trying to consume amount <= 0 in method findPods");
            return null;
        }

        List<Unit> pod_list = new LinkedList<>();    // Keep a list of the suitable cargo pods we find
        int resources_found = 0;          // and keep track of how many resources are in them

        // First search planet surfaces
        for (Planet planet : planets) {                            // Go through all planets, for the sake of UW...
            if (efs_ini.universal_warehouse || planet.index == p_idx) {    // ...but only proceed if UW is ON, or it's the right planet
                for (int x = 0; x < C.PLANET_MAP_WIDTH; x++) {         // Go through all hexes on planet, by coordinates
                    int hexes_in_col = (x % 2 == 0) ? C.PLANET_MAP_COLUMNS - 1 : C.PLANET_MAP_COLUMNS;    // Even columns have one less hex
                    for (int y = 0; y < hexes_in_col; y++) {
                        List<Unit> stack = planet.planet_grid.getHex(x, y).getStack();    // Get stack in that hex
                        for (Unit unit : getExpandedStack(stack)) {
                            if (unit.type == C.CARGO_UNIT_TYPE && unit.res_relic == resource_type && unit.owner == owner) {
                                pod_list.add(unit);    // This is a suitable cargo pod; add it to the list
                                resources_found += unit.amount;
                                if (resources_found >= amount_needed) {
                                    return pod_list;    // Stop searching as soon as we've found enough
                                }
                            }
                        }
                    }
                }
            }
        }
        // If necessary, search units in orbit

        if (resources_found < amount_needed) {
            for (Planet planet : planets) {                            // Go through all planets, for the sake of UW...
                if (efs_ini.universal_warehouse || planet.index == p_idx) {    // ...but only proceed if UW is ON, or it's the right planet
                    List<Unit> stack = planet.space_stacks[owner];    // Faction's stack in orbit about the planet
                    for (Unit unit : getExpandedStack(stack)) {
                        if (unit.type == C.CARGO_UNIT_TYPE && unit.res_relic == resource_type) {
                            pod_list.add(unit);    // This is a suitable cargo pod; add it to the list
                            resources_found += unit.amount;
                            if (resources_found >= amount_needed) {
                                return pod_list;    // Stop searching as soon as we've found enough
                            }
                        }
                    }
                }
            }
        }
        return null;    // If we get to here, we didn't find enough
    }

    /**
     * Consume resources from one cargo pod.
     *
     * @param unit Cargo pod
     * @param amount_needed Amount to be consumed
     * @return Amount actually consumed (less than amount_needed, if pod doesn't
     * have enough)
     *
     */
    public int takeResourcesFromPod(Unit unit, int amount_needed) {

        int ret_val;

        if (unit.amount >= amount_needed) {
            ret_val = amount_needed;    // Pod has enough; take all we need
            unit.amount -= amount_needed;
        } else {
            ret_val = unit.amount;    // Not enough; take all in pod
            unit.amount = 0;
        }
        if (unit.amount == 0) {    // Pod now empty
            game.deleteUnit(unit);
        }
        return ret_val;
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

        // FOR TESTING
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
