/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import galaxyreader.Planet;
import galaxyreader.Unit;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author joulupunikki
 */
public class Square implements Serializable {

    public Planet planet;
    public int stack_owner;
//    public List<Unit>[] stacks;
    /**
     * The planet of whose orbit this square represents. Total of 8 such squares
     * around each planet.
     */
    public Planet parent_planet;

    public Square() {
        planet = null;
        stack_owner = -1;
        parent_planet = null;
//        stacks = null;
    }

//    public void placeUnit(Unit e) {        
//        
//    }
    public void setStackData(int stack_owner, Planet parent_planet) {
        this.stack_owner = stack_owner;
        this.parent_planet = parent_planet;
    }

}
