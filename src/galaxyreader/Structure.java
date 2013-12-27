/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package galaxyreader;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;

/**
 * A class representing a structure object.
 *
 * @author joulupunikki
 */
public class Structure  implements Serializable{

    public int p_idx;      //short
    public int x;       //short
    public int y;     //short 
    public int type;       //short
    public int owner;      //short 
    int prev_owner;  //short 
    int prod_ruin_type; //union short/short
    int prod_info;  //union short/short
    int turns_left;  //short 
    int city_info;        //union short/short/short
    int prev_type;           //short 
    int unit_loyalty;        //short 
    int loyalty;   //short 
    int stack_info;      //union short/short
    int used_unitt_lvl;    //short
    int tech_type; //short
    int health; //short
    int sect; //short
    int Flags; //UINT
    int unit_health; //short 
    int temp_count;  //short 
    int temp_count2; //short 
    int temp_count3; //short 
    int Damage;     //short 

    /**
     * Creates a structure object. Reads in coordinates, owner, loyalty and other fields.
     * @param fc the FileChannel which contains the file from which the data is
     * read.
     * @param count the Counter containing the position from which the data is
     * read.
     * @throws IOException
     */
    public Structure(FileChannel fc, Counter count) throws IOException {

        count.getSet(-2);
        p_idx = GalaxyReader.readShort(fc, count.getSet(2));      
        x = GalaxyReader.readShort(fc, count.getSet(2));       
        y = GalaxyReader.readShort(fc, count.getSet(2));     
        
        type = GalaxyReader.readShort(fc, count.getSet(2));       
        owner = GalaxyReader.readShort(fc, count.getSet(2));      
        prev_owner = GalaxyReader.readShort(fc, count.getSet(2));  
 
        prod_ruin_type = GalaxyReader.readShort(fc, count.getSet(2)); 
        prod_info = GalaxyReader.readShort(fc, count.getSet(2)); 
        turns_left = GalaxyReader.readShort(fc, count.getSet(2));  
        city_info = GalaxyReader.readShort(fc, count.getSet(2));        
        prev_type = GalaxyReader.readShort(fc, count.getSet(2));           
        unit_loyalty = GalaxyReader.readShort(fc, count.getSet(2));        
    
        loyalty = GalaxyReader.readShort(fc, count.getSet(2));   
      
        stack_info = GalaxyReader.readShort(fc, count.getSet(2));      
        used_unitt_lvl = GalaxyReader.readShort(fc, count.getSet(2));    
     
        tech_type = GalaxyReader.readShort(fc, count.getSet(2)); 
      
        health = GalaxyReader.readShort(fc, count.getSet(2)); 
        sect = GalaxyReader.readShort(fc, count.getSet(2)); 
        Flags = GalaxyReader.readInt(fc, count.getSet(4)); 
      
        unit_health = 0; 
        temp_count = 0;  
        temp_count2 = 0; 
        temp_count3 = 0; 
        Damage = 0;     
    }
    /**
     * Prints a structure object. Prints coordinates and owner. For debugging purposes.
     */
    public void print() {
        System.out.println("p_idx: " + p_idx);
        System.out.println("x:     " + x);
        System.out.println("y:     " + y);
        System.out.println("owner: " + owner);
    }
}
