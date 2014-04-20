/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import gui.Gui;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.WindowConstants;
import util.C;
import util.Util;

/**
 * Class containing variables and methods to enable secure PBEM play.
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class PBEM implements Serializable {

    public static final int PASSWORD_FAIL = 0;
    public static final int PASSWORD_OK = 1;
    public static final int PASSWORD_REVOKE = 2;

    public boolean pbem = false;
    public boolean end_turn = false;
    public byte[] salt = null;
    public Map<String, byte[]> checksums = new HashMap<>();
    public byte[][] passwd_hashes = new byte[C.NR_FACTIONS][];

    private JDialog dialog;
    private JLabel text;
    private JPasswordField pwf1;
    private JPasswordField pwf2;
    private int f_idx;
    private int tries;
    private boolean passwd_ok;

    public PBEM() {
        for (int i = 0; i < passwd_hashes.length; i++) {
            passwd_hashes[i] = null;

        }
        long long_val = System.currentTimeMillis();
        salt = String.valueOf(long_val).getBytes();
    }

    public File[] getDataFiles() {
        // get DAT-files in DAT directory
        File dat_dir = new File("DAT");
        FilenameFilter dat_filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lc_name = name.toLowerCase();
                if (lc_name.endsWith(".dat") || lc_name.endsWith(".ini")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        return dat_dir.listFiles(dat_filter);
    }

    /**
     * Return MessageDigest initialized to SHA-256
     *
     * @return
     */
    public MessageDigest getMessageDigest() {
        return getMessageDigest("SHA-256");
    }

    /**
     * Return MessageDigest initialized to hash algo.
     *
     * @param algo
     * @return
     */
    public MessageDigest getMessageDigest(String algo) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algo);
        } catch (NoSuchAlgorithmException ex) {
            Util.logEx(null, ex);
            System.exit(1);
        }
        return md;
    }

    public byte[] digest(MessageDigest md, byte[] data, byte[] salt) {
        md.update(data);
        return md.digest(salt);
    }

    public byte[] getHash(File file, String dir) {
        String file_name;
        if (dir == null) {
            file_name = file.getName();
        } else {
            file_name = dir + C.S_SEPAR + file.getName();
        }
        // count on file length fitting into an int
        byte[] data = Util.readFile(file_name, (int) file.length(), ByteOrder.BIG_ENDIAN);
        MessageDigest md = getMessageDigest();
        return digest(md, data, salt);

    }

    public void getDATAHashes() {
        File[] dat_files = getDataFiles();
        for (File file : dat_files) {
            System.out.println("file = " + file.getName());
            checksums.put(file.getName(), getHash(file, "DAT"));
        }
        File file = new File("PHOENIX" + C.S_SEPAR + "PHOENIX.INI");
        checksums.put(file.getName(), getHash(file, "PHOENIX"));
        String[] main_args = Gui.getMainArgs();
        String galaxy_name;
        String dir = null;
        if (main_args.length == 2) {
            galaxy_name = main_args[1];
            if (galaxy_name.startsWith("GAL/") || galaxy_name.startsWith("GAL\\")) {
                galaxy_name = galaxy_name.substring(4, galaxy_name.length());
                dir = "GAL";
            }
        } else {
            galaxy_name = "GALAXY.GAL";
        }
        file = new File(galaxy_name);
        checksums.put(file.getName(), getHash(file, dir));
//        System.out.println(file.getAbsolutePath());
//        System.exit(0);
    }

    public boolean compHashes(byte[] a, byte[] b) {
        boolean rv = true;
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < b.length; i++) {
            if (a[i] != b[i]) {
                rv = false;
                break;
            }
        }
        return rv;
    }

    public void testDATAHashes(Gui gui) {
        String diff = "";
        for (String key : checksums.keySet()) {
            String file_name;
            String dir = null;
            if (key.startsWith("GAL/") || key.startsWith("GAL\\")) {
                file_name = key.substring(4, key.length());
                dir = "GAL";
            } else if (key.equals("GALAXY.GAL")) {
                file_name = key;
                dir = null;
            } else if (key.equals("PHOENIX.INI")) {
                file_name = key;
                dir = "PHOENIX";
            } else {
                file_name = key;
                dir = "DAT";
            }
            String file_path;
            if (dir == null) {
                file_path = key;
            } else {
                file_path = dir + C.S_SEPAR + key;
            }
            File file = new File(file_path);
            byte[] hash = getHash(file, dir);
            byte[] stored = checksums.get(key);
            if (!compHashes(hash, stored)) {
                diff = diff + " " + key;
            }
        }
        if (diff.length() > 0) {
            gui.showInfoWindow("Your game data digests do not match the starting"
                    + " player's. The following datafiles differ : " + diff);
        }
    }

    public void getPasswd(int faction, Gui gui) {
        this.f_idx = faction;
        dialog = new JDialog(gui, null, true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
        text = new JLabel("Enter new password twice, noble " + Util.getFactionName(f_idx));
        text.setForeground(C.COLOR_GOLD);
        text.setFont(Gui.getWindowSize().font_default);
        pwf1 = new JPasswordField(10);
        pwf2 = new JPasswordField(10);

        char[] pw = null;
        JButton button = new JButton("OK");
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                char[] pw1 = pwf1.getPassword();
                char[] pw2 = pwf2.getPassword();
                boolean match = true;
                if (pw1.length != pw2.length) {
                    match = false;
                } else {
                    for (int i = 0; i < pw1.length; i++) {
                        if (pw1[i] != pw2[i]) {
                            match = false;
                            break;
                        }

                    }
                }
                if (!match) {
                    JOptionPane.showMessageDialog(dialog, "Passwords do not match.");
                    // zero pwds
                    Arrays.fill(pw1, '0');
                    Arrays.fill(pw2, '0');
                    pwf1.setText("");
                    pwf2.setText("");
                    return;
                }
                MessageDigest md = null;
                try {
                    md = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException ex) {
                    Util.logEx(null, ex);
                    System.exit(1);
                }
                passwd_hashes[f_idx] = digest(md, Util.toBytes(pw1), salt);
                Arrays.fill(pw1, '0');
                Arrays.fill(pw2, '0');

                dialog.dispose(); //To change body of generated methods, choose Tools | Templates.
            }
        });

//        dialog.getContentPane().add(text, BorderLayout.PAGE_START);
//        dialog.getContentPane().add(pwf1, BorderLayout.PAGE_START);
//        dialog.getContentPane().add(pwf2, BorderLayout.CENTER);
//        dialog.getContentPane().add(button, BorderLayout.PAGE_END);
//        dialog.add(button);
        dialog.getContentPane().add(text);
        dialog.getContentPane().add(pwf1);
        dialog.getContentPane().add(pwf2);
        dialog.getContentPane().add(button);
        dialog.pack();
        dialog.setVisible(true);
    }

    public boolean testPasswd(int faction, Gui gui) {
        this.f_idx = faction;
        dialog = new JDialog(gui, null, true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
        text = new JLabel("Enter password, noble " + Util.getFactionName(f_idx));
        text.setForeground(C.COLOR_GOLD);
        text.setFont(Gui.getWindowSize().font_default);
        pwf1 = new JPasswordField(10);
        tries = 0;
        passwd_ok = false;
        final Gui gui_ref = gui;
//        JLabel text_revoke = new JLabel("Initiate Password Revocation Sequence");
//        JButton revoke_button = new JButton("Password Revocation");
//        revoke_button.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                char[] pw1 = pwf1.getPassword();
//
//                boolean match = true;
//
//                MessageDigest md = null;
//                try {
//                    md = MessageDigest.getInstance("SHA-256");
//                } catch (NoSuchAlgorithmException ex) {
//                    Util.logEx(null, ex);
//                    System.exit(1);
//                }
//                byte[] passwd_hash = digest(md, Util.toBytes(pw1), salt);
//                System.out.print("Provided hash: ");
//                System.out.println(Util.byteToHex(passwd_hash));
//                System.out.print("Stored hash  : ");
//                System.out.println(Util.byteToHex(passwd_hashes[f_idx]));
//                for (int i = 0; i < passwd_hash.length; i++) {
//                    if (passwd_hash[i] != passwd_hashes[f_idx][i]) {
//                        match = false;
//                        break;
//                    }
//                }
//                if (!match) {
//                    Arrays.fill(pw1, '0');
//                    if (++tries >= 3) {
//                        gui_ref.toMainMenu();
//                        dialog.dispose();
//                        return;
//                    }
//                    JOptionPane.showMessageDialog(dialog, "Invalid password.");
//                    pwf1.setText("");
//                    return;
//                }
//                passwd_ok = true;
//                Arrays.fill(pw1, '0');
//
//                dialog.dispose(); //To change body of generated methods, choose Tools | Templates.
//            }
//        });

        JButton button = new JButton("OK");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                char[] pw1 = pwf1.getPassword();

                boolean match = true;

                MessageDigest md = null;
                try {
                    md = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException ex) {
                    Util.logEx(null, ex);
                    System.exit(1);
                }
                byte[] passwd_hash = digest(md, Util.toBytes(pw1), salt);
                System.out.print("Provided hash: ");
                System.out.println(Util.byteToHex(passwd_hash));
                System.out.print("Stored hash  : ");
                System.out.println(Util.byteToHex(passwd_hashes[f_idx]));
                for (int i = 0; i < passwd_hash.length; i++) {
                    if (passwd_hash[i] != passwd_hashes[f_idx][i]) {
                        match = false;
                        break;
                    }
                }
                if (!match) {
                    Arrays.fill(pw1, '0');
                    if (++tries >= 3) {
                        gui_ref.toMainMenu();
                        dialog.dispose();
                        return;
                    }
                    JOptionPane.showMessageDialog(dialog, "Invalid password.");
                    pwf1.setText("");
                    return;
                }
                passwd_ok = true;
                Arrays.fill(pw1, '0');

                dialog.dispose(); //To change body of generated methods, choose Tools | Templates.
            }
        });
        dialog.getContentPane().add(text);
        dialog.getContentPane().add(pwf1);
        dialog.getContentPane().add(button);
//        dialog.add(button);
        dialog.pack();
        dialog.setVisible(true);

        return passwd_ok;
    }
}
