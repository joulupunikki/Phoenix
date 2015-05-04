/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 * Class F(ile)N(ames).java, defines final strings of all filenames in EFS and
 * Phoenix. Rules for creating Strings: Prefix with "S_". Capitalize all
 * letters. Remove parent directory names and file separators. Convert "." to
 * "_".
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class FN {

    //public static final String S_FS = System.getProperty("file.separator");
    public static final String S_GAL = "GAL";
    public static final String S_SAV = "SAV";
    public static final String S_BIN = "BIN";
    public static final String S_PCX = "PCX";
    public static final String S_MANOWITZ = "MANOWITZ";
    public static final String S_PHOENIX_INI = "PHOENIX" + C.S_SEPAR + "PHOENIX.INI";
    public static final String S_EFS_INI = "DAT" + C.S_SEPAR + "EFS.INI";
    public static final String S_EFS_INI_TMP = "EFS.INI.TMP";
    public static final String S_BG0_PCX = S_PCX + C.S_SEPAR + "BG0.PCX";
    public static final String S_UNITBG2_PCX = S_PCX + C.S_SEPAR + "UNITBG2.PCX";
    public static final String S_SKULL_BIN = S_BIN + C.S_SEPAR + "SKULL.BIN";
    public static final String S_FLAG_BIN = S_BIN + C.S_SEPAR + "FLAG.BIN";
    public static final String S_EFS_PAL = "EFS.PAL";
    public static final String S_GALAXY_GAL = "GALAXY.GAL";
    public static final String S_MOUSE_MSK = "MOUSE.MSK";
    // these three help define EFSTILE?.BIN and STRUCT?.BIN with ? being index
    public static final String S_EFSTILE = S_BIN + C.S_SEPAR + "EFSTILE";
    public static final String S_STRUCT = S_BIN + C.S_SEPAR + "STRUCT";
    public static final String S__BIN = ".BIN";
    public static final String S_CATHED3_PCX = S_PCX + C.S_SEPAR + "CATHED3.PCX";
    public static final String S_BOOK5H_PCX = S_MANOWITZ + C.S_SEPAR + "BOOK5H.PCX";
    public static final String S_CLOSE_BIN = S_MANOWITZ + C.S_SEPAR + "CLOSE.BIN";
    public static final String S_NEXT_BIN = S_MANOWITZ + C.S_SEPAR + "NEXT.BIN";
    public static final String S_PREV_BIN = S_MANOWITZ + C.S_SEPAR + "PREV.BIN";
    public static final String S_CONTENTS_BIN = S_MANOWITZ + C.S_SEPAR + "CONTENTS.BIN";
    // these two help define VOLUME?.TXT with ? being index
    public static final String S_VOLUME = S_MANOWITZ + C.S_SEPAR
            + "VOLUME";
    public static final String S__TXT = ".TXT";
    public static final String S_PLNPLAT3_PCX = S_PCX + C.S_SEPAR + "PLNPLAT3.PCX";
    public static final String S_STARFLD2_PCX = S_PCX + C.S_SEPAR + "STARFLD2.PCX";
    public static final String S_EFSPLAN_BIN = S_BIN + C.S_SEPAR + "EFSPLAN.BIN";
    public static final String S_STARMAP3_PCX = S_PCX + C.S_SEPAR + "STARMAP3.PCX";
    public static final String S_UNITINFO_PCX = S_PCX + C.S_SEPAR + "UNITINFO.PCX";
    // filenames of buttons 1-16 in array slots 1-16 (slot 0 is unused)
    public static final String[] S_EFSBUT_BIN = initEfsBut();
    public static final int EFSBUT_NR = 16;
    private static String[] initEfsBut() {
        String[] efs_but = new String[EFSBUT_NR + 1];
        for (int i = 1; i < efs_but.length; i++) {
            efs_but[i] = S_BIN + C.S_SEPAR + "EFSBUT" + i + S__BIN;

        }
        return efs_but;
    }
}
