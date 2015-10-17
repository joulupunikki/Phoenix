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

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Class F(ile)N(ames).java, defines final strings of all filenames in EFS and
 * Phoenix. Rules for creating Strings: Prefix with "S_". Capitalize all
 * letters. Remove parent directory names and file separators. Convert "." to
 * "_".
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class FN {

    public static final String F_S = System.getProperty("file.separator");
    public static final String L_S = System.getProperty("line.separator");
    public static final String S_GAL = "GAL";
    public static final String S_SAV = "SAV";
    public static final String S_BIN = "BIN";
    public static final String S_PCX = "PCX";
    public static final String S_FLC = "FLC";
    public static final String S_MANOWITZ = "MANOWITZ";
    public static final String S_PHOENIX_INI = "PHOENIX" + F_S + "PHOENIX.INI";
    public static final String S_PHOENIXTEST_INI = "PHOENIX" + F_S + "PHOENIXTEST.INI";

    public static final String S_EFS_INI = "DAT" + F_S + "EFS.INI";
    public static final String S_EFS_INI_TMP = "EFS.INI.TMP";
    public static final String S_BG0_PCX = S_PCX + F_S + "BG0.PCX";
    public static final String S_HOUSE_PCX = S_PCX + F_S + "HOUSE.PCX";
    public static final String S_BYZSECU_PCX = S_PCX + F_S + "BYZSECU.PCX";
    public static final String S_UNITBG2_PCX = S_PCX + F_S + "UNITBG2.PCX";
    public static final String S_DIPLOMA_PCX = S_PCX + F_S + "DIPLOMA.PCX";
    public static final String S_CONTRACT_PCX = S_PCX + F_S + "CONTRACT.PCX";
    public static final String S_SKULL_BIN = S_BIN + F_S + "SKULL.BIN";
    public static final String S_FLAG_BIN = S_BIN + F_S + "FLAG.BIN";
    public static final String S_HOUSE1_BIN = S_BIN + F_S + "HOUSE1.BIN";
    public static final String S_HOUSE2_BIN = S_BIN + F_S + "HOUSE2.BIN";
    public static final String S_HOUSE3_BIN = S_BIN + F_S + "HOUSE3.BIN";
    public static final String S_HOUSE4_BIN = S_BIN + F_S + "HOUSE4.BIN";
    public static final String S_HOUSE5_BIN = S_BIN + F_S + "HOUSE5.BIN";

    public static final String S_EFS_PAL = "EFS.PAL";
    public static final String S_GALAXY_GAL = "GALAXY.GAL";
    public static final String S_MOUSE_MSK = "MOUSE.MSK";
    public static final String S_MOUSE1_MSK = "MOUSE1.MSK";
    public static final String S_MOUSE2_MSK = "MOUSE2.MSK";
    public static final String S_MOUSE3_MSK = "MOUSE3.MSK";
    // these three help define EFSTILE?.BIN and STRUCT?.BIN with ? being index
    public static final String S_EFSTILE = S_BIN + F_S + "EFSTILE";
    public static final String S_STRUCT = S_BIN + F_S + "STRUCT";
    public static final String S__BIN = ".BIN";
    public static final String S_CATHED3_PCX = S_PCX + F_S + "CATHED3.PCX";
    public static final String S_BOOK5H_PCX = S_MANOWITZ + F_S + "BOOK5H.PCX";
    public static final String S_CLOSE_BIN = S_MANOWITZ + F_S + "CLOSE.BIN";
    public static final String S_NEXT_BIN = S_MANOWITZ + F_S + "NEXT.BIN";
    public static final String S_PREV_BIN = S_MANOWITZ + F_S + "PREV.BIN";
    public static final String S_CONTENTS_BIN = S_MANOWITZ + F_S + "CONTENTS.BIN";
    // these two help define VOLUME?.TXT with ? being index
    public static final String S_VOLUME = S_MANOWITZ + F_S
            + "VOLUME";
    public static final String S__TXT = ".TXT";
    public static final String S_PLNPLAT3_PCX = S_PCX + F_S + "PLNPLAT3.PCX";
    public static final String S_STARFLD2_PCX = S_PCX + F_S + "STARFLD2.PCX";
    public static final String S_EFSPLAN_BIN = S_BIN + F_S + "EFSPLAN.BIN";
    public static final String S_STARMAP3_PCX = S_PCX + F_S + "STARMAP3.PCX";
    public static final String S_UNITINFO_PCX = S_PCX + F_S + "UNITINFO.PCX";
    // filenames of buttons 1-16 in array slots 1-16 (slot 0 is unused)
    public static final int EFSBUT_NR = 16;
    public static final String[] S_EFSBUT_BIN;
    static {
        S_EFSBUT_BIN = initEfsBut();
    }
    ;
    public static final String S_ARBORIUM_DAT = "DAT" + F_S + "ARBORIUM.DAT";
    public static final String S_EFSUNIT_BIN = "BIN" + F_S + "EFSUNIT.BIN";
    public static final String S_TECH_DAT = "DAT" + F_S + "TECH.DAT";
    public static final String S_FARM_DAT = "DAT" + F_S + "FARM.DAT"; // RSW
    public static final String S_STRBUILD_DAT = "DAT" + F_S + "STRBUILD.DAT";
    public static final String S_CARGO_BIN = "BIN" + F_S + "CARGO.BIN";
    public static final String S_TARGET_DAT = "DAT" + F_S + "TARGET.DAT";
    public static final String S_VERSION = setVersion();
    public static final String S_UNITSPOT_DAT = "DAT" + F_S + "UNITSPOT.DAT";
    public static final String S_DAMAGE_DAT = "DAT" + F_S + "DAMAGE.DAT";
    public static final String S_WELL_DAT = "DAT" + F_S + "WELL.DAT";
    public static final String S_PROD_DAT = "DAT" + F_S + "PROD.DAT"; // RSW
    public static final String S_RES_DAT = "DAT" + F_S + "RES.DAT"; // RSW
    public static final String S_UNIT_DAT = "DAT" + F_S + "UNIT.DAT";
    public static final String S_TERRCOST_DAT = "DAT" + F_S + "TERRCOST.DAT";
    public static final String S_LOG_FILE = "phoenixlog.txt";
    public static final String S_STATIC_INI = "static.ini";
    public static final String S_GAME_STATE_RECORD_FILE = "phoenixstaterecord.txt";
    public static final String S_MINE_DAT = "DAT" + F_S + "MINE.DAT";
    public static final String S_TERCOLOR_DAT = "DAT" + F_S + "TERCOLOR.DAT";

    private static String[] initEfsBut() {
        String[] efs_but = new String[EFSBUT_NR + 1];
        for (int i = 1; i < efs_but.length; i++) {
            efs_but[i] = S_BIN + F_S + "EFSBUT" + i + S__BIN;

        }
        return efs_but;
    }

    private static String setVersion() {
        Properties props = new Properties();
        URL url = ClassLoader.getSystemResource(S_STATIC_INI);
        try {
            props.load(url.openStream());
        } catch (IOException ex) {
            Util.logEx(null, ex, "Failed to read " + S_STATIC_INI);
        }
        return props.getProperty("phoenix.version");
    }
}
