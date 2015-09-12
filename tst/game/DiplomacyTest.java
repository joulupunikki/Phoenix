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
package game;

import static org.hamcrest.CoreMatchers.is;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import util.C;

/**
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class DiplomacyTest {

    public DiplomacyTest() {
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

    /**
     * Test of set/getDiplomaticState method, of class Diplomacy.
     */
    @Test
    public void testDiplomaticStateGetSet() {
        int faction_a = 1;
        int faction_b = 2;
        int old_state = 0;
        int new_state = 1;
        Diplomacy instance = new Diplomacy(null);
        Assert.assertEquals(old_state, instance.getDiplomaticState(faction_a, faction_b));
        instance.setDiplomaticState(faction_a, faction_b, new_state);
        Assert.assertEquals(new_state, instance.getDiplomaticState(faction_a, faction_b));
        instance.setDiplomaticState(faction_a, faction_b, old_state);
        Assert.assertEquals(old_state, instance.getDiplomaticState(faction_a, faction_b));
        instance.setDiplomaticState(faction_b, faction_a, new_state);
        Assert.assertEquals(new_state, instance.getDiplomaticState(faction_a, faction_b));
    }

    @Test
    public void testExceptionMessage() {
        try {
            new Diplomacy(null).setDiplomaticState(C.HOUSE2, C.HOUSE3, -5);
            Assert.fail("Expected an AssertionError to be thrown");
        } catch (AssertionError e) {
            Assert.assertThat(e.getMessage(), is("Invalid diplomatic state -5"));
        }
    }
}
