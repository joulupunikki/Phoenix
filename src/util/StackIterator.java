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
