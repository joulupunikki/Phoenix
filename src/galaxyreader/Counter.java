/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package galaxyreader;

/**
 * A class which implements a simple counter. Intended for representing
 * positions in FileChannels.
 *
 * @author joulupunikki
 */
public class Counter {

    long counter;

    /**
     * Creates a new Counter and sets it to zero.
     */
    public Counter() {
        counter = 0;
    }

    /**
     * Adjusts the Counter by increment and returns the new Counter value.
     *
     * @param increment the amount by which the Counter is adjusted.
     * @return the adjusted Counter value.
     */
    public long getSet(long increment) {
        long ret_val = counter;
        counter += increment;
        return ret_val;
    }

}
