/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

/**
 * A class containing constants used in project Phoenix classes.
 *
 * @author joulupunikki
 */
public class C {

    public static final int DEBUG_PRINT = 0;
    public static final int NORTH = 0;
    public static final int NORTHEAST = 1;
    public static final int SOUTHEAST = 2;
    public static final int SOUTH = 3;
    public static final int SOUTHWEST = 4;
    public static final int NORTHWEST = 5;
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final int FOUR = 4;
    public static final int PLANET_NAME = 32;
    public static final int PLANET_MAP_WIDTH = 44;
    public static final int PLANET_MAP_HEIGHT = 65;
    public static final int PLANET_MAP_COLUMNS = 32;
    public static final int PLANET_MAP_SQUARES_X = 13;
    public static final int PLANET_MAP_SQUARES_Y = 10;
    public static final int UNIT_POS = 8;
    public static final int STAR_MAP_HEIGHT = 48;
    public static final int STAR_MAP_WIDTH = 50;
    public static final int STAR_MAP_SQUARES_X = 15;
    public static final int STAR_MAP_SQUARES_Y = 13;
    public static final int END_OF_SECTION = -2;
    public static final int END_OF_GROUP = -3;
    public static final int STRUCT_BIN_HEIGHT = 40;
    public static final int STRUCT_BIN_WIDTH = 48;
    public static final int EFSUNIT_BIN_HEIGHT = 32;
    public static final int EFSUNIT_BIN_WIDTH = 32;
    public static final int PALLETTE_LENGTH = 768;
    public static final int EFSPLAN_BIN_LENGTH = 128;
    public static final int EFSPLAN_BIN_P_SIZE = 1024;
    public static final int MAX_PLANETS = 500;
    public static final int MAX_JUMP_GATES = 2000;
    public static final int MAX_STRUCTURES = 10000;
    public static final int MAX_UNITS = 40000;
    public static final int UNIT_TYPES = 92;
    public static final int UNIT_T_LVLS = 6;
    public static final int PLANET_MAP_ORIGIN_X_OFFSET = 6;
    public static final int PLANET_MAP_ORIGIN_Y_OFFSET = 4;
    public static final int STRBUILD = 32;
    public static final int TERR_COST_HEX = 12;
    public static final int TERR_COST_PLANET = 5;
    public static final int TERR_COST_MOVE = 10;
    public static final int UNIT_SPOT_HEX = 13;
    public static final int UNIT_SPOT_PLANET = 5;
    public static final int UNIT_SPOT_MOVE = 10;
    public static final int TER_COLOR_HEX = 12;
    public static final int TER_COLOR_PLANET = 5;

    public static final int TILE_SETS = 5;
    public static final int STACK_SIZE = 20;
    public static final int STACK_WINDOW_UNITS_X = 5;
    public static final int STACK_WINDOW_UNITS_Y = 4;
    public static final int NR_FACTIONS = 14;
    public static final int DAMAGE_DAT_X = 12;
    public static final int DAMAGE_DAT_Y = 10;
    public static final int TARGET_DAT_X = 9;
    public static final int TARGET_DAT_Y = 10;
    public static final int COMBAT_LOOP_ITER = 30;
    public static final int SKULL_SIDE = 40;
    // HexProc procedures
    public static final int INIT_SPOT = 0;
    public static final int SPOT = 1;
//    public static final int WATER_ATTK = 4;
//    public static final int INDIRECT_ATTK = 2;
//    public static final int AIR_ATTK = 2;
//    public static final int DIRECT_ATTK = 3;
//    public static final int CLOSE_ATTK = 4;
//    public static final int PSYCH_ATTK = 2;
    public static final int[] NR_ATTACKS = {4, 2, 2, 3, 4, 2, 2, 3, 4};
    public static final int[] GROUND_COMBAT_PHASES = {0, 1, 2, 3, 4, 5};
//    public static final int[] GROUND_COMBAT_ATTACKS = {4, 2, 2, 3, 4, 2};
    public static final int[] SPACE_COMBAT_PHASES = {6, 7, 8, 5};
//    public static final int[] SPACE_COMBAT_ATTACKS = {2, 3, 4, 2};
    public static final int[] PTS_COMBAT_PHASES = {6};
//    public static final int[] PTS_COMBAT_ATTACKS = {2};
//    public static final int GROUND_COMBAT_PHASES_NR = 6;
//    public static final int RANGED_SPACE_ATTK = 2;
//    public static final int DIRECT_SPACE_ATTK = 3;
//    public static final int CLOSE_SPACE_ATTK = 4;
//    public static final int[] SC_ATTACKS = {2, 3, 4, 2};
//    public static final int SPACE_COMBAT_PHASES_NR = 4;
    public static final int WATER = 0;
    public static final int INDIRECT = 1;
    public static final int AIR = 2;
    public static final int DIRECT = 3;
    public static final int CLOSE = 4;
    public static final int PSYCH = 5;
    public static final int RANGED_SPACE = 6;
    public static final int DIRECT_SPACE = 7;
    public static final int CLOSE_SPACE = 8;

    public enum PhasesGC {

        WATER,
        INDIRECT,
        AIR,
        DIRECT,
        CLOSE,
        PSYCH
    }

    public enum PhasesSC {

        RANGED_SPACE,
        DIRECT_SPACE,
        CLOSE_SPACE,
        PSYCH
    }

    // message types
    public enum Msg {
        CITY_FULL
    }
    
    // movement types
    public enum MoveType {

        FOOT,
        WHEEL,
        TREAD,
        AIR,
        NAVAL,
        SPACE,
        HOVER,
        JUMP,
        CRAWLER,
        LANDER
    }
    public static final int PALACE = 0;
    public static final int CHURCH = 1;
    public static final int MONASTERY = 2;
    public static final int FACTORY = 3;
    public static final int AGORA = 4;
    public static final int WETWARE = 5;
    public static final int ELECTRONICS = 6;
    public static final int HIVE = 7;
    public static final int CERAMSTEEL = 8;
    public static final int BIOPLANT = 9;
    public static final int VAU_CITY = 10;
    public static final int CHEMICALS = 11;
    public static final int CYCLOTRON = 12;
    public static final int FORT = 13;
    public static final int STARPORT = 14;
    public static final int RUINS = 15;
    public static final int ALIEN_RUINS = 16;
    public static final int SHIELD = 17;
    public static final int MINE = 18;
    public static final int WELL = 19;
    public static final int FUSORIUM = 20;
    public static final int UNIVERSITY = 21;
    public static final int HOSPITAL = 22;
    public static final int LAB = 23;
    public static final int FARM = 24;
    public static final int ARBORIUM = 25;
    public static final int TRACE = 26;
    public static final int GEMS = 27;
    public static final int EXOTICA = 28;
    public static final int FERTILE = 29;
    public static final int METAL = 30;
    public static final int ENERGY = 31;
    public static final int CITY_TYPES = 26;
    public static final int STRUCTURE_TYPES = 32;
    // terrain types
    public static final int OCEAN = 0;
    public static final int GRASS = 1;
    public static final int ARID_GRASS = 2;
    public static final int DESERT = 3;
    public static final int ICE = 4;
    public static final int TUNDRA = 5;
    public static final int MOUNTAIN = 6;
    public static final int HILL = 7;
    public static final int TREE = 8;
    public static final int RIVER = 9;
    public static final int DELTA = 10;
    public static final int ROAD = 11;
    public static final int STRUCTURE = 12;
    // faction id's
    public static final int HOUSE1 = 0;
    public static final int HOUSE2 = 1;
    public static final int HOUSE3 = 2;
    public static final int HOUSE4 = 3;
    public static final int HOUSE5 = 4;
    public static final int LEAGUE = 5;
    public static final int THE_CHURCH = 6;
    public static final int SYMBIOT = 7;
    public static final int VAU = 8;
    public static final int IMPERIAL = 9;
    public static final int FLEET = 10;
    public static final int STIGMATA = 11;
    public static final int THE_SPY = 12;
    public static final int NEUTRAL = 13;

    public static final String S_PLANET_MAP = "planet map";
    public static final String S_STAR_MAP = "star map";
    public static final String S_UNIT_INFO = "unit info";
    public static final String S_MAIN_MENU = "main menu";
    public static final String S_COMBAT_WINDOW = "combat window";
    public static final String S_UNIT_DAT = "DAT/UNIT.DAT";
    public static final String S_EFSUNIT_BIN = "BIN/EFSUNIT.BIN";
    public static final String S_STRBUILD_DAT = "DAT/STRBUILD.DAT";
    public static final String S_TERRCOST_DAT = "DAT/TERRCOST.DAT";
    public static final String S_UNITSPOT_DAT = "DAT/UNITSPOT.DAT";
    public static final String S_DAMAGE_DAT = "DAT/DAMAGE.DAT";
    public static final String S_TARGET_DAT = "DAT/TARGET.DAT";
    public static final String S_TERCOLOR_DAT = "DAT/TERCOLOR.DAT";
    public static final String S_ALL = "all";
    public static final String S_COMBAT = "combat";
    public static final String S_NONCOMBAT = "noncombat";
    public static final String S_TRANSPORT = "transport";
    public static final String S_LOG_FILE = "phoenixlog.txt";

    public static final Color COLOR_GOLD = new Color(255, 215, 0);
    public static final Color COLOR_DARK_GREEN = new Color(53, 94, 59);
    public static final Color COLOR_LIGHT_GREEN = new Color(76, 187, 23);
    public static final int INDEX_COLOR_EFS_BLACK = 14;
    public static final int INDEX_COLOR_BLACK = -96;
    public static final String GROUND_COMBAT = "ground_combat";
    public static final String SPACE_COMBAT = "space_combat";

    public static final String ATTACKING_CMBT_STACK = "attacking_combat_stack";
    public static final String DEFENDING_CMBT_STACK = "defending_combat_stack";
    public static final double GRAY_SCALING_FACTOR = 0.5;

    public static final int STARTING_FIREBIRDS = 4500;
    
    public static final Border GOLD_BORDER = BorderFactory.createLineBorder(C.COLOR_GOLD);
    
    /**
     * Prints out the constants which determine unit, structure and planet
     * allegiance. For debugging purposes.
     */
    public static void print() {
        System.out.println("HOUSE1:" + HOUSE1);
        System.out.println("HOUSE2:" + HOUSE2);
        System.out.println("HOUSE3:" + HOUSE3);
        System.out.println("HOUSE4:" + HOUSE4);
        System.out.println("HOUSE5:" + HOUSE5);
        System.out.println("LEAGUE:" + LEAGUE);
        System.out.println("THE_CHURCH:" + THE_CHURCH);
        System.out.println("SYMBIOT:" + SYMBIOT);
        System.out.println("VAU:" + VAU);
        System.out.println("IMPERIAL:" + IMPERIAL);
        System.out.println("FLEET:" + FLEET);
        System.out.println("STIGMATA:" + STIGMATA);
        System.out.println("THE_SPY:" + THE_SPY);
        System.out.println("NEUTRAL:" + NEUTRAL);
    }
}
/*
 * forest
 */
