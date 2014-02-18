/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Game;
import java.util.Comparator;

/**
 *
 * @author joulupunikki
 */
public class Comp {

    private static Game game = null;
    public static CityType city_type = new CityType();
    public static CityName city_name = new CityName();
    public static UnitXY unit_xy = new UnitXY();
    public static UnitPIdx unit_pidx = new UnitPIdx();
    public static UnitInSpace unit_in_space = new UnitInSpace();
    public static UnitResRelic unit_res_relic = new UnitResRelic();
//    public static class Rank implements Comparator {
//
//        public int compare(Unit o1, Unit o2) {
//                return o1.type_data.rank - o2.type_data.rank;
//            }
//
//        }

    public static void setGame(Game g) {
        game = g;
    }
    /**
     * Sorts by resource/relic type.
     */
    public static class UnitResRelic implements Comparator<Unit> {
        public int compare(Unit a, Unit b) {
            return a.res_relic - b.res_relic;
        }
    }
    
    /**
     * Sorts by city type.
     */
    public static class CityType implements Comparator<Structure> {

        public int compare(Structure a, Structure b) {
            return a.type - b.type;
        }
    }

    /**
     * Sorts by city name.
     */
    public static class CityName implements Comparator<Structure> {

        public int compare(Structure a, Structure b) {
            return game.getStrBuild(a.type).name.compareTo(game.getStrBuild(b.type).name);
        }
    }

    /**
     * Sorts by unit x,y coordinates.
     */
    public static class UnitXY implements Comparator<Unit> {

        public int compare(Unit o1, Unit o2) {
            int x = o1.x - o2.x;
            if (x != 0) {
                return x;
            } else {
                return o1.y - o2.y;
            }

        }
    }

    /**
     * Sorts by unit planet index.
     */
    public static class UnitPIdx implements Comparator<Unit> {

        public int compare(Unit o1, Unit o2) {
            return o1.p_idx - o2.p_idx;
        }
    }

    /**
     * Sorts by unit in_space. Those in space will be last
     */
    public static class UnitInSpace implements Comparator<Unit> {

        public int compare(Unit o1, Unit o2) {
            int s1;
            int s2;
            if (o1.in_space) {
                s1 = 1;
            } else {
                s1 = 0;
            }
            if (o2.in_space) {
                s2 = 1;
            } else {
                s2 = 0;
            }
            return s1 - s2;
        }
    }
}
