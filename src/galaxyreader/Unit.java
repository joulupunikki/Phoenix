/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package galaxyreader;

import dat.UnitType;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
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
    int res_relic; //char/Union of char/char 
    int amount;     //short 
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

    public Unit(int p_idx, int x, int y, int owner) {
        this.p_idx = p_idx;
        this.x = x;
        this.y = y;
        this.loyalty = 100;
        this.owner = owner;
        this.prev_owner = 0;
        this.type = 0;
        this.t_lvl = 0;
//        this.move_type = move_type;
        this.orders = 0;
        this.experience = 0;
        this.move_points = 0;
        this.res_relic = 0;
        this.amount = 0;
        this.health = 100;
        this.health_tmp = 100;
        this.sect = 0;
        this.unit_no = 0;
        this.flags = 0;
        this.used_unit_type = 0;
        this.used_unitt_lvl = 0;
        this.camo = 0;
        this.dest_x = 0;
        this.dest_y = 0;
        this.move_cost = 0;
        this.t_flags = 0;
        this.ai_orders = 0;
        this.ai_data = 0;
        this.ai_data2 = 0;
        this.wait_level = 0;
        this.unit_list = null;
        this.group_list = null;
        this.cargo_list = new LinkedList<>();
        this.group_end_mark = 0;
        this.in_space = false;
        this.is_sentry = false;
        this.is_cargo = false;
        this.on_loan = false;
        spotted = new boolean[14];
        for (int i = 0; i < spotted.length; i++) {
            spotted[i] = false;            
        }
        spotted[owner] = true;
        this.selected = false;
        this.routed = false;
        this.type_data = null;
    }
    
    
    
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
