/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package galaxyreader;

import dat.UnitType;
import game.Game;    // RSW
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import util.C;

/**
 * A class representing a unit object. Contains the units location, loyalty, owner and other fields.
 * @author joulupunikki
 */
public class Unit  implements Serializable{

    public int p_idx; // short  
    public int x; // short     
    public int y; //short       
    public int loyalty; // char  
    public int owner;      //char  
    public int prev_owner;  //char  
    public int type;       //char 
    public int t_lvl;      //char 
    public C.MoveType move_type;  //short 
    int orders;    //short 
    int experience; //char 
    public int move_points; //char 
    public int res_relic; //char/Union of char/char - RSW made public
    public int amount;     //short - RSW made public
    public int health;     //char 
    public int health_tmp; //original
    int sect;       //char 
    int unit_no;     //int 
    int flags;      //UINT 
    int used_unit_type; //char  
    int used_unitt_lvl; //char 
    int camo;       //char  
    int dest_x;      //UCHAR 
    int dest_y;      //UCHAR 
    int move_cost;   //int 
    int t_flags;     //UCHAR 
    int ai_orders;   //char 
    int ai_data;     //char 
    int ai_data2;    //short 
    int wait_level;  //UCHAR 

    /*
     * the following Lists are 
     * probably better handled 
     * at a higher level
     */
    List<Unit> unit_list; 
    List<Unit> group_list; 
    public List<Unit> cargo_list; //  
    int group_end_mark; 
    
    // original code
    public boolean in_space;
    boolean is_sentry;
    boolean is_cargo;
    boolean on_loan;
    public boolean[] spotted;
    public boolean selected;
    public boolean routed;
    public UnitType type_data;
    public Unit carrier = null; //unit which carries this unit

    /**
     * Creates a unit object. Reads in coordinates, loyalty, owner and other fields.
     * @param fc the FileChannel which contains the file from which the data is
     * read.
     * @param count the Counter containing the position from which the data is
     * read.
     * @throws IOException
     */
    public Unit(FileChannel fc, Counter count) throws IOException {
        count.getSet(-2);
        p_idx = GalaxyReader.readShort(fc, count.getSet(2));
        x = GalaxyReader.readShort(fc, count.getSet(2));
        y = GalaxyReader.readShort(fc, count.getSet(2));
        
        owner = GalaxyReader.readByte(fc, count.getSet(1));
        
        type = GalaxyReader.readByte(fc, count.getSet(1));
        t_lvl = GalaxyReader.readByte(fc, count.getSet(1));
        loyalty = GalaxyReader.readByte(fc, count.getSet(1));
        move_type = C.MoveType.FOOT; // GalaxyReader.readShort(fc, count.getSet(2));  
        orders = GalaxyReader.readShort(fc, count.getSet(2));    
        experience = GalaxyReader.readByte(fc, count.getSet(1)); 
        move_points = GalaxyReader.readByte(fc, count.getSet(1)); 
        res_relic = GalaxyReader.readByte(fc, count.getSet(1)); 
        amount = GalaxyReader.readShort(fc, count.getSet(2));     
        health = GalaxyReader.readByte(fc, count.getSet(1));     
        health_tmp = health;
        sect = GalaxyReader.readByte(fc, count.getSet(1));       
        prev_owner = GalaxyReader.readByte(fc, count.getSet(1));
        unit_no = GalaxyReader.readInt(fc, count.getSet(4));     
        flags = GalaxyReader.readInt(fc, count.getSet(4));      
        used_unit_type = GalaxyReader.readByte(fc, count.getSet(1)); 
        used_unitt_lvl = GalaxyReader.readByte(fc, count.getSet(1)); 
        camo = GalaxyReader.readByte(fc, count.getSet(1));       
        dest_x = GalaxyReader.readByte(fc, count.getSet(1));      
        dest_y = GalaxyReader.readByte(fc, count.getSet(1));      
        move_cost = 0; //GalaxyReader.readInt(fc, count.getSet(4));   
        t_flags = 0; //GalaxyReader.readByte(fc, count.getSet(1));     
        ai_orders = GalaxyReader.readByte(fc, count.getSet(1));   
        ai_data = GalaxyReader.readByte(fc, count.getSet(1));     
// if ((orig_version >= 961025) || (save_version >= 961027)) {
        ai_data2 = GalaxyReader.readShort(fc, count.getSet(2));    
        wait_level = GalaxyReader.readByte(fc, count.getSet(1));  
// }
        /*
         * the following Lists are 
         * probably better handled 
         * at a higher level
         */
        unit_list = null; //  
        group_list = null; //  
        cargo_list = new LinkedList<>(); //  
        short section = GalaxyReader.readShort(fc, count.getSet(2)); 
        if (section == -3) { 
            group_end_mark = section;
        } else {
            count.getSet(-2);
        } //GalaxyReader.readShort(fc, count.getSet(2)); //short 
        
        if ((flags & 0x01) == 0x01) {
            in_space = false;
        } else {
            in_space = true;
        }
        
        if ((flags & 0x04) == 0x04) {
            is_sentry = true;
        } else {
            is_sentry = false;
        }
        
        if ((flags & 0x0040) == 0x0040) {
            is_cargo = true;
        } else {
            is_cargo = false;
        }
        
        if ((flags & 0x0080) == 0x0080) {
            on_loan = true;
        } else {
            on_loan = false;
        }
        
        spotted = new boolean[14];
        for (int i = 0; i < spotted.length; i++) {
            spotted[i] = false;
            
        }
        
        selected = false;
        
    }

    /**
     * Create fresh unit from scratch, not from Galaxy file.
     * @param p_idx, x, y: planet and hex co-ordinates of location
     * @param owner: owning faction
     * @param type: type number (position in UNIT.DAT, 0-91)
     * @param t_lvl: subtype (subordinate position in UNIT.DAT)
     * @param res_relic: resource or relic type (cargo pods and relics only, set to 0 for other units)
     * @param amount: quantity of resources (cargo pods only, set to 0 for other units)
     * @param game: needed for access to the unit type table (UNIT.DAT)
     * @param random: random number seed
     * Other Unit fields will be set to defaults.
     * --RSW
     */
 
    public Unit(int p_idx, int x, int y, int owner, int type, int t_lvl, int res_relic, int amount, Game game, Random random) {

        this.p_idx = p_idx;
        this.x = x;
        this.y = y;
        this.owner = owner;
        this.type = type;
        this.t_lvl = t_lvl;
        this.res_relic = res_relic; 
        this.amount = amount;  

        type_data = game.getUnitTypes()[type][t_lvl];    // Get type data from UNIT.DAT   
        move_type = type_data.move_type;
        camo = Math.max(type_data.camo + random.nextInt(5) - 2, 0);    //    New unit gets randomized camo value
        
         System.out.println("Creating new Unit"); //DEBUG
         System.out.println("p_idx " + p_idx);
         System.out.println("x "+x);
         System.out.println("y "+y);
         System.out.println("owner "+owner);
         System.out.println("type "+type);
         System.out.println("t_lvl "+t_lvl);
         System.out.println("res_relic "+res_relic);
         System.out.println("amount "+amount);
         System.out.println("type_data.camo "+type_data.camo);
         System.out.println("camo "+camo);
         System.out.println("");

        move_points = type_data.move_pts;
        loyalty = 100;
        health = 100;     
        health_tmp = health;
        experience = 0;    // Set all the rest to zero for now
        orders = 0;   
        sect = 0;       
        prev_owner = 0;
        unit_no = 0;     
        flags = 0;      
        used_unit_type = 0; 
        used_unitt_lvl = 0; 
        dest_x = 0;      
        dest_y = 0;      
        move_cost = 0;  
        t_flags = 0;
        ai_orders = 0;
        ai_data = 0;
        ai_data2 = 0;  
        wait_level = 0; 

        unit_list = null;    // I don't think this is used. RSW
        group_list = null;    // I don't think this is used. RSW
        cargo_list = new LinkedList<>(); // Unit has no cargo
        group_end_mark = -3;    // I don't know what this is. I just got this value from the first constructor. RSW

        in_space = false;
        is_sentry = false;
        is_cargo = false;
        on_loan = false;
        
        spotted = new boolean[14];
        for (int i = 0; i < spotted.length; i++) {
            spotted[i] = false;
        }
        spotted[owner] = true;
        selected = false;
        routed = false;
    }

    /**
     * Load unit u as cargo on this unit.
     * @param u unit to be loaded.
     * @return true if loading succeeds.
     */
    public boolean embark(Unit u) {
        boolean rv = false;
        if (cargo_list.size() < this.type_data.cargo) {
            u.carrier = this;
            cargo_list.add(u);
            u.selected = this.selected;
            rv = true;
        }
        return rv;
    }
    
    /**
     * Unload unit u as cargo from this unit.
     * @param u unit to be unloaded.
     * @return true if unloading succeeds.
     */
    public boolean disembark(Unit u) {
        boolean rv = false;
        if (cargo_list.remove(u)) {
            u.carrier = null;
            rv = true;
        }
        return rv;
    }
    
    public boolean isCargo(Unit u) {
        return cargo_list.contains(u);
    }
    
    public static int spotRange(int spot) {
        int r_v = 0;
        r_v = spot / 2 - 1;
        if (r_v <= 0) {
            r_v = 1;
        }
        return r_v;
    }
    
    /**
     * Prints the unit object. For debugging purposes.
     */
    public void print() {
        System.out.println("unit p_idx:   " + p_idx);
        System.out.println("unit x:       " + x);
        System.out.println("unit y:       " + y);
        System.out.println("unit loyalty: " + loyalty);
        System.out.println("unit owner:   " + owner);
        System.out.println("unit type:    " + type);
        System.out.println("unit t_lvl:   " + t_lvl);
    }

 
    public static class CompMoveType implements Comparator<Unit> {
        public int compare(Unit u1, Unit u2) {
            return Integer.compare(u1.move_type.ordinal(), u2.move_type.ordinal());
        }
    }
    
}
