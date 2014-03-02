/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import galaxyreader.Unit;
import java.util.Iterator;
import java.util.List;

/**
 * Iterator to go through a stack. Will iterate through cargo. Next returns null
 * if there are no more elements.
 *
 * @author joulupunikki
 */
public class StackIterator {

    boolean start;
    boolean is_cargo_listing;
    Iterator<Unit> iterator;
    Iterator<Unit> cargo_it;
    Unit next;

    public StackIterator(List<Unit> stack) {
        is_cargo_listing = false;
        iterator = stack.listIterator();
        cargo_it = null;
        start = true;
    }

    public Unit next() {

        if (start) {
            if (iterator.hasNext()) {
                next = iterator.next();
            } else {
                next = null;
            }
            start = false;
        } else if (next != null) {

            if (is_cargo_listing) {
                next = cargo_it.next();
                if (!cargo_it.hasNext()) {
                    cargo_it = null;
                    is_cargo_listing = false;
                }
            } else if (next.cargo_list.isEmpty()) {
                if (iterator.hasNext()) {
                    next = iterator.next();
                } else {
                    next = null;
                }
            } else {
                cargo_it = next.cargo_list.listIterator();
                next = cargo_it.next();
                if (cargo_it.hasNext()) {
                    is_cargo_listing = true;
                }
            }
        }
        return next;

    }
}
