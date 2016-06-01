/*
 * Copyright (C) 2016 joulupunikki joulupunikki@gmail.communist.invalid.
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import phoenix.Phoenix;

/**
 * Ensures EFS file names are capitalized.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class FileNameCapitalizer {
    private static final FileNameCapitalizer FNC = new FileNameCapitalizer();
    private static final String[] EFS_SUBDIRS = {
        "BIN", "DAT", "FLC",
        "MANOWITZ", "PCX",
        "RAND", "S", "SAV"
    };

    private static final String[] EFS_TOPFILES = {
        //_ISREG32.DLL
        "610FULL.FNT",
        "BLANK.FLC",
        //"CDLIST.TXT",
        "CHAR1216.FNT",
        "CHAR610.FNT",
        "CHAR77.FNT",
        "COMPSTR.RES",
        //dat_0527.exe
        //"DEISL1.ISU",
        "DIPLO.RES",
        "DIPLOSTR.RES",
        "EFS.EXE",
        "EFS.PAL",
        "EFS.RES",
        "EFSFNT4.FNT",
        //"EMPEROR.REG",
        "GALAXY.GAL",
        "MOUSE.MSK",
        "MOUSE1.MSK",
        "MOUSE2.MSK",
        "MOUSE3.MSK",
        //PATCH.EXE
        //PATCH.RTD
        //PATCH.RTP
        //PATCHRUN.BAT
        "SCROLL.BIN",
        //"SMACKPLW.EXE",
        "STRINGS.RES",
        "TUTSTR.RES",
        //"WHATSNEW.TXT",
        "WIN.RES"
    };

    private FileNameCapitalizer() {
    }

    public static boolean run() {
        return FNC.runSub();

    }

    private boolean runSub() {
        String top_dir_name = ".";
        if (!FN.S_DIST_PREFIX.equals("")) {
            top_dir_name = FN.S_DIST_PREFIX;
        }

        // before we do anything permanent, check for the existence of EFS top
        // dir files, so that we are pointed to EFS top dir with high confidence
        File top_dir = new File(top_dir_name);
        String[] actual_names = top_dir.list(FileFileFilter.FILE);
        int count = EFS_TOPFILES.length;
        for (int i = 0; i < actual_names.length; i++) {

            File current = new File(actual_names[i]);
            String cap_name = isEFSTopFile(current);
            if (cap_name != null && actual_names[i].equalsIgnoreCase(cap_name)) {
                count--;
            }
        }

        if (count > 0) {
            Phoenix.addBootMsg("\nError: not all EFS1.4 files present.");
            return false;
        }

        top_dir = new File(top_dir_name);
        actual_names = top_dir.list(DirectoryFileFilter.DIRECTORY);
        count = EFS_SUBDIRS.length;
        for (int i = 0; i < actual_names.length; i++) {

            File current = new File(actual_names[i]);
            String cap_name = isEFSDir(current);
            if (cap_name != null && actual_names[i].equalsIgnoreCase(cap_name)) {
                count--;
            }
        }

        if (count > 0) {
            Phoenix.addBootMsg("\nError: not all EFS1.4 sub dirs present.");
            return false;
        }

        // capitalize EFS subdir names
        actual_names = top_dir.list(DirectoryFileFilter.INSTANCE);
        for (int i = 0; i < actual_names.length; i++) {

            File current = new File(actual_names[i]);
            String cap_name = isEFSDir(current);
            if (cap_name != null && !actual_names[i].equals(cap_name)) {
                try {
                    Files.move(Paths.get(top_dir_name, actual_names[i]), Paths.get(top_dir_name, cap_name));
                    System.out.println("Capitalized " + actual_names[i] + " -> " + cap_name);
                } catch (Exception e) {
                    Phoenix.addBootMsg("\nError: Capitalization " + actual_names[i] + " -> " + cap_name + " failed");
                    return false;
                }

            }
        }

        // capitalize topdir file names
        actual_names = top_dir.list(FileFileFilter.FILE);
        for (int i = 0; i < actual_names.length; i++) {

            File current = new File(actual_names[i]);
            String cap_name = isEFSTopFile(current);
            if (cap_name != null && !actual_names[i].equals(cap_name)) {
                try {
                    Files.move(Paths.get(top_dir_name, actual_names[i]), Paths.get(top_dir_name, cap_name));
                    System.out.println("Capitalized " + actual_names[i] + " -> " + cap_name);
                } catch (Exception e) {
                    Phoenix.addBootMsg("\nError: Capitalization " + actual_names[i] + " -> " + cap_name + " failed");
                    return false;
                }

            }
        }
        // capitalize file names in EFS subdirs (except SAV)
        for (String string : EFS_SUBDIRS) {
            if (string.equals("SAV")) {
                continue;
            }
            File sub_dir = new File(top_dir_name + FN.F_S + string);
            actual_names = sub_dir.list(FileFileFilter.FILE);
            for (int i = 0; i < actual_names.length; i++) {
                File current = new File(actual_names[i]);
                String cap_name = current.getName().toUpperCase(Locale.ROOT);
                if (!actual_names[i].equals(cap_name)) {
                    try {
                        Files.move(Paths.get(top_dir_name, string, actual_names[i]), Paths.get(top_dir_name, string, cap_name));
                        System.out.println("Capitalized " + string + FN.F_S + actual_names[i] + " -> " + string + FN.F_S + cap_name);
                    } catch (Exception e) {
                        Phoenix.addBootMsg("\nError: Capitalization " + string + FN.F_S + actual_names[i] + " -> " + string + FN.F_S + cap_name + " failed");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private String isEFSDir(File file) {
        for (String string : EFS_SUBDIRS) {
            if (file.getName().equalsIgnoreCase(string)) {
                return string;
            }
        }
        return null;
    }

    private String isEFSTopFile(File file) {
        for (String string : EFS_TOPFILES) {
            if (file.getName().equalsIgnoreCase(string)) {
                return string;
            }
        }
        return null;
    }

}
