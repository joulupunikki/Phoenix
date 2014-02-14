/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import galaxyreader.Structure;
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
    
    public static class CityType implements Comparator<Structure> {

        public int compare(Structure a, Structure b) {
            return a.type - b.type;
        }
    }
    
    public static class CityName implements Comparator<Structure> {

        public int compare(Structure a, Structure b) {
            return game.getStrBuild(a.type).name.compareTo(game.getStrBuild(b.type).name);
        }
    }
}
