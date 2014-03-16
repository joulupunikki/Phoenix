/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 *
 * @author joulupunikki
 */
public class RingCounter {

    private int counter;
    private int length;

    public RingCounter(int length, int start_val) {
        if (length < 1) {
            this.length = 1;
        } else {
            this.length = length;
        }

        if (start_val > length) {
            counter = 1;
        } else {
            counter = start_val;
        }
    }

    public int getSet() {
        int ret_val = counter;
        counter++;
        if (counter > length) {
            counter = 0;
        }
        return ret_val;
    }

    public int get() {
        return counter;
    }
}
