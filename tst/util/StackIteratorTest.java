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
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class StackIteratorTest {

    public StackIteratorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testNext2() {
        // create test stack and units        
        List<Unit> s = new LinkedList<>();
        Unit[] u = new Unit[10];
        for (int i = 0; i < u.length; i++) {
            u[i] = new Unit(i, 0, 0, 0);
        }
        
        for (int i = 0; i < 3; i++) {
            // test stacks with no cargo
            s.clear();
            clearCargo(u);
            for (int j = 0; j <= i; j++) {
                s.add(u[j]);
            }
            assertStack(s, i + 1);
            for (int j = 0; j <= i; j++) {
                // test with 1 cargo
                s.clear();
                clearCargo(u);
                for (int k = 0; k <= i + 1; k++) {
                    if(k == j + 1) {
                        s.get(k - 1).cargo_list.add(u[k]);
                    } else {
                        s.add(u[k]);
                    }
                }
                assertStack(s, i + 2);
            }
            for (int j = 0; j <= i; j++) {
                // test with 2 cargo
                s.clear();
                clearCargo(u);
                for (int k = 0; k <= i + 2; k++) {
                    if (k == j + 1) {
                        s.get(k - 1).cargo_list.add(u[k]);
                    } else if (k == j + 2) {
                        s.get(k - 2).cargo_list.add(u[k]);
                    } else {
                        s.add(u[k]);
                    }
                }
                assertStack(s, i + 3);
            }

        }
        // test with 2 * 2 cargo
        int[][] cargo = {{2, 2}, {0, 2, 2}, {2, 0, 2}, {2, 2, 0}};
        for (int[] cargo1 : cargo) {
            s.clear();
            clearCargo(u);
            int idx = 0;
            for (int i = 0; i < cargo1.length; i++) {
                s.add(u[idx++]);
                for (int j = 0; j < cargo1[i]; j++) {
                    s.get(i).cargo_list.add(u[idx++]);
                }
            }
            assertStack(s, idx);
        }
    }

    private void assertStack(List<Unit> s, int size) {
        System.out.print(s.size() + "," + size + ",");
        StackIterator si = null;
        si = new StackIterator(s);
        for (int j = 0; j < size; j++) {
            System.out.print("," + j);
            Assert.assertEquals(si.next().p_idx, j);
        }
        System.out.println("");
        Assert.assertNull(si.next());
    }

    private void clearCargo(Unit[] u) {
        for (Unit u1 : u) {
            u1.cargo_list.clear();
        }

    }




//    /**
//     * Test of next method, of class StackIterator.
//     */
//    @Test
//    public void testNext() {
//        System.out.println("next");
//        StackIterator instance = null;
//        Unit expResult = null;
//        Unit result = instance.next();
//        Assert.assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
