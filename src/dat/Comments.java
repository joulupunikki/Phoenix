/*
 * Copyright (C) 2015 joulupunikki joulupunikki@gmail.communist.invalid.
 *
 *  Disclaimer of Warranties and Limitation of Liability.
 *
 *     The creators and distributors offer this software as-is and
 *     as-available, and make no representations or warranties of any
 *     kind concerning this software, whether express, implied, statutory,
 *     or other. This includes, without limitation, warranties of title,
 *     merchantability, fitness for a particular purpose, non-infringement,
 *     absence of latent or other defects, accuracy, or the presence or
 *     absence of errors, whether or not known or discoverable.
 *
 *     To the extent possible, in no event will the creators or distributors
 *     be liable on any legal theory (including, without limitation,
 *     negligence) or otherwise for any direct, special, indirect,
 *     incidental, consequential, punitive, exemplary, or other losses,
 *     costs, expenses, or damages arising out of the use of this software,
 *     even if the creators or distributors have been advised of the
 *     possibility of such losses, costs, expenses, or damages.
 *
 *     The disclaimer of warranties and limitation of liability provided
 *     above shall be interpreted in a manner that, to the extent possible,
 *     most closely approximates an absolute disclaimer and waiver of
 *     all liability.
 *
 */
package dat;

/**
 *
 * @author joulupunikki
 */
public class Comments {
    /*
     * UNIT.DAT: 
     * entries with the text "Reserved" in them are not ignored and units of this
     * type will be entered into the game (change this to remove these units ?)
     * 
     * GALAXY.GAL: In original EFS the map contained non-visible squares at the
     * bottom of the map. These squares do not exist in Phoenix and 
     * units and cities which are placed in those squares are simply removed
     * from the game. This should not be a too much of a problem, but in the 
     * Hyperion 1.4g version of Random19FEB01 galaxy
     * on Criticorum Hawkwood has nine units at the bottom of the map and on
     * Istakhr Al-Malik has three units and a mine at the bottom of the map. 
     * Original GALAXY.GAL seems to have several cities with column number = 32
     * for example on Nowhere at 9,32
     *
     * Some galaxy files seem to have up to 21 units in some stacks. Eg. 
     * Hyperion 1.4g version of Random19FEB01 galaxy league has 21 units
     * in a stack on Leagueheim at x:43 y:10. and at x:37 y:29
     * 
     
     Faction positions on space squares is different from original EFS eg.
     in original EFS league and church appear on the lower right corner but in
     phoenix the are on the lower left corner. During unit placement space unit 
     coordinates are now set using parent planet coordinates

     Current spotting function only works at ranges <= 15 so for spotting ability greater
     than 32 spotting doesn't work properly.

     On Hyperion 1.4g in DAT/UNITSPOT.DAT the 67th line reads
     "desert"  "2.0 1.0 2.2.0.0 1.0 0.5 2.0 0.5 1.0 1.0"
     when it should probably read
     "desert"  "2.0 1.0 2.0 2.0 1.0 0.5 2.0 0.5 1.0 1.0"
     Phoenix fails on this.
     */
}
