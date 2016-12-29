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

import dat.Tech;
import dat.UnitSpot;
import java.io.FileReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import static org.junit.Assert.*;
import util.C;
import util.FN;
import util.Util;

/**
 * Loaded data used by the game object.
 *
 * @author joulupunikki
 */
public class GameResources implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private double[][][] unit_spot;
    private Tech[] techs;
    private ArrayList<ArrayList<int[]>> ruin_guards;
    private ArrayList<ArrayList<int[]>> alien_ruin_guards;

    public GameResources() {

        unit_spot = UnitSpot.readUnitSpot();
        techs = Tech.readTech();
        loadRuinGuardData(FN.S_RUINGUARD_CSV, ruin_guards);
    }

    private void loadRuinGuardData(String file_name, ArrayList<ArrayList<int[]>> guard_list) {
        // TODO read ruin guard data
        Iterable<CSVRecord> records = null;
        try {
            Reader in = new FileReader(file_name);
            records = CSVFormat.EXCEL.withCommentMarker('#').parse(in);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.out.println("Exception: " + e.getMessage());
            System.out.println("Failed to read " + FN.S_RUINGUARD_CSV);
            Util.logEx(null, e);
            Util.logFFErrorAndExit(FN.S_RUINGUARD_CSV, -1, e);
        }
        guard_list = new ArrayList<>();
        ArrayList<int[]> tmp = null;
        int stack_size = 0;
        for (CSVRecord record : records) {
            if (record.size() < 3) {
                tmp = new ArrayList<>();
                guard_list.add(tmp);
                stack_size = 0;
                continue;
            }
            int[] unit_data = {Integer.parseInt(record.get(AMOUNT)),
                Integer.parseInt(record.get(UNIT_IDX)),
                Integer.parseInt(record.get(TECH_LVL))};
            assertTrue(unit_data[AMOUNT] > 0);
            stack_size += unit_data[AMOUNT];
            assertTrue(stack_size <= C.STACK_SIZE);
            assertTrue(unit_data[UNIT_IDX] >= 0 && unit_data[UNIT_IDX] <= 91);
            assertTrue(unit_data[TECH_LVL] >= 0 && unit_data[TECH_LVL] <= 5);

            tmp.add(unit_data);
        }
//        Tech.print(techs);
//        System.exit(0);
    }
    private static final int TECH_LVL = 2;
    private static final int UNIT_IDX = 1;
    private static final int AMOUNT = 0;

    public double[][][] getUnitSpot() {
        return unit_spot;
    }

    public Tech[] getTech() {
        return techs;
    }
}
