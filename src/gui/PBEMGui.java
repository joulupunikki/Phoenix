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
package gui;

import game.Game;
import game.PBEM;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.WindowConstants;
import org.apache.commons.cli.CommandLine;
import util.C;
import util.FN;
import util.Util;
import util.UtilG;

/**
 * Class containing variables and methods to enable secure PBEM play.
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class PBEMGui implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final int PASSWORD_FAIL = 0;
    public static final int PASSWORD_OK = 1;
    public static final int PASSWORD_REVOKE = 2;

    private JDialog dialog;
    private JLabel text;
    private JPasswordField pwf1;
    private JPasswordField pwf2;
    private int f_idx;
    private int tries;
    private int passwd_ok;
    private PBEM pbem;

    public PBEMGui(Game game) {
        pbem = game.getEfs_ini().pbem;
    }

    public void setPBEMRef(Game game) {
        pbem = game.getEfs_ini().pbem;
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

    /**
     * Compute <code> md </code> digest of data and salt.
     *
     * @param md MessageDigest
     * @param data
     * @param salt
     * @return
     */
    public byte[] digest(MessageDigest md, byte[] data, byte[] salt) {
        md.update(data);
        return md.digest(salt);
    }

    public byte[] getHash(File file, String dir) {
        String file_name;
        if (dir == null) {
            file_name = file.getName();
        } else {
            file_name = dir + FN.F_S + file.getName();
        }
        // count on file length fitting into an int
        byte[] data = Util.readFile(file_name, (int) file.length(), ByteOrder.BIG_ENDIAN);
        MessageDigest md = getMessageDigest();
        return digest(md, data, pbem.salt);

    }

    public void getDATAHashes() {
        File[] dat_files = getDataFiles();
        for (File file : dat_files) {
            //System.out.println("file = " + file.getName());
            pbem.checksums.put(file.getName(), getHash(file, "DAT"));
        }
        File file = new File(FN.S_PHOENIX_INI);
        pbem.checksums.put(file.getName(), getHash(file, "PHOENIX"));
        CommandLine main_args = Gui.getMainArgs();
        String galaxy_name;
        String dir = null;
        if (main_args.hasOption(C.OPT_NAMED_GALAXY)) {
            galaxy_name = main_args.getOptionValue(C.OPT_NAMED_GALAXY);
            if (galaxy_name.startsWith(FN.S_GAL + FN.F_S)) {
                galaxy_name = galaxy_name.substring(4, galaxy_name.length());
                dir = FN.S_GAL;
            }
        } else {
            galaxy_name = FN.S_GALAXY_GAL;
        }
        file = new File(galaxy_name);
        pbem.checksums.put(file.getName(), getHash(file, dir));
//        System.out.println(file.getAbsolutePath());
//        System.exit(0);
    }

    /**
     * Compare two byte arrays.
     *
     * @param a
     * @param b
     * @return
     */
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
        for (String key : pbem.checksums.keySet()) {
            String file_name;
            String dir = null;
            if (key.startsWith(FN.S_GAL + FN.F_S)) {
                file_name = key.substring(4, key.length());
                dir = FN.S_GAL;
            } else if (key.equals(FN.S_GALAXY_GAL)) {
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
                file_path = file_name;
            } else {
                file_path = dir + FN.F_S + file_name;
            }
            File file = new File(file_path);
            byte[] hash = getHash(file, dir);
            byte[] stored = pbem.checksums.get(key);
            if (!compHashes(hash, stored)) {
                diff = diff + " " + key;
            }
        }
        if (diff.length() > 0) {
            gui.showInfoWindow("Your game data digests do not match the starting"
                    + " player's. The following datafiles differ : " + diff);
        }
    }

    /**
     * Ask for a same new password twice.
     *
     * @param faction
     * @param gui
     */
    public void getPasswd(int faction, Gui gui) {
        this.f_idx = faction;
        dialog = new JDialog(gui, null, true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
        text = new JLabel("Enter new PBEM password, noble " + Util.getFactionName(f_idx) + ":");
        text.setAlignmentX(Component.CENTER_ALIGNMENT);
        text.setForeground(C.COLOR_GOLD);
        text.setFont(Gui.getWindowSize().font_large);
        JLabel text2 = new JLabel("Confirm password:");
        text2.setAlignmentX(Component.RIGHT_ALIGNMENT);
        text2.setForeground(C.COLOR_GOLD);
        text2.setFont(Gui.getWindowSize().font_large);
        System.out.println("Font size: " + text2.getFont().getSize());
        pwf1 = new JPasswordField(10);
        pwf1.setAlignmentX(Component.CENTER_ALIGNMENT);
        pwf2 = new JPasswordField(10);
        pwf2.setAlignmentX(Component.CENTER_ALIGNMENT);
        char[] pw = null;
        JButton button = new JButton("OK");
        button.setFont(Gui.getWindowSize().font_large);
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
                pbem.passwd_hashes[f_idx] = digest(md, Util.toBytes(pw1), pbem.salt);
                Arrays.fill(pw1, '0');
                Arrays.fill(pw2, '0');
                dialog.setVisible(false);
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
        dialog.getContentPane().add(text2);
        dialog.getContentPane().add(pwf2);
        dialog.getContentPane().add(button);

        dialog.pack();
        UtilG.setDialogLocation(dialog, gui);
        dialog.setVisible(true);
    }

    /**
     * Ask and test password and provide a button to initiate password
     * revocation sequence.
     *
     * @param faction
     * @param gui
     * @param game
     * @return
     */
    public int testPasswd(int faction, Gui gui, Game game) {
        this.f_idx = faction;
        dialog = new JDialog(gui, null, true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
        String revoke_info_str;
        if (pbem.revocation_action) {
            revoke_info_str = "Zeroing password of " + Util.getFactionName(pbem.revoked_player);
        } else {
            revoke_info_str = "Setting " + Util.getFactionName(pbem.revoked_player) + " to computer control";
        }
        JLabel text_revoke_info = new JLabel("Password revocation of " + Util.getFactionName(pbem.revoked_player));
        text_revoke_info.setFont(Gui.getWindowSize().font_large);
        JLabel text_revoke_info2 = new JLabel(revoke_info_str);
        text_revoke_info2.setFont(Gui.getWindowSize().font_large);
        text = new JLabel("Enter password, noble " + Util.getFactionName(f_idx) + ":");
        text.setForeground(C.COLOR_GOLD);
        text.setFont(Gui.getWindowSize().font_large);
        pwf1 = new JPasswordField(10);
        tries = 0;
        passwd_ok = PASSWORD_FAIL;
        final Gui gui_ref = gui;
        final Game game_ref = game;
        JLabel text_revoke = new JLabel("Initiate Password Revocation Sequence");
        text_revoke.setFont(Gui.getWindowSize().font_large);
        JButton revoke_button = new JButton("Password Revocation");
        revoke_button.setFont(Gui.getWindowSize().font_large);
        revoke_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (!gui_ref.showConfirmWindow("Are you sure you want to initiate password revocation sequence ?")) {
                    return;
                }
                getAction(gui_ref, game_ref);
                pbem.password_revocation = true;
                pbem.revoked_player = game_ref.getTurn();
                passwd_ok = PASSWORD_REVOKE;
                dialog.setVisible(false);
                dialog.dispose(); //To change body of generated methods, choose Tools | Templates.
            }
        });

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
                byte[] passwd_hash = digest(md, Util.toBytes(pw1), pbem.salt);
                System.out.print("Provided hash: ");
                System.out.println(Util.byteToHex(passwd_hash));
                System.out.print("Stored hash  : ");
                System.out.println(Util.byteToHex(pbem.passwd_hashes[f_idx]));
                for (int i = 0; i < passwd_hash.length; i++) {
                    if (passwd_hash[i] != pbem.passwd_hashes[f_idx][i]) {
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
                passwd_ok = PASSWORD_OK;
                Arrays.fill(pw1, '0');
                dialog.setVisible(false);
                dialog.dispose(); //To change body of generated methods, choose Tools | Templates.
            }
        });

        text_revoke_info.setAlignmentX(Component.CENTER_ALIGNMENT);
        text_revoke_info2.setAlignmentX(Component.CENTER_ALIGNMENT);
        text.setAlignmentX(Component.CENTER_ALIGNMENT);
        pwf1.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        text_revoke.setAlignmentX(Component.CENTER_ALIGNMENT);
        revoke_button.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (pbem.password_revocation) {
            dialog.getContentPane().add(text_revoke_info);
            dialog.getContentPane().add(text_revoke_info2);
        }
        dialog.getContentPane().add(text);
        dialog.getContentPane().add(pwf1);
        dialog.getContentPane().add(button);
        if (!pbem.password_revocation && testConfirmPasswdsNull(game) && moreThan2Humans(game)) {
            dialog.getContentPane().add(text_revoke);
            dialog.getContentPane().add(revoke_button);
        }
//        dialog.add(button);
        dialog.pack();
        UtilG.setDialogLocation(dialog, gui);
        dialog.setVisible(true);

        return passwd_ok;
    }

    /**
     * Test that more that 2 human players are present
     *
     * @param game
     * @return
     */
    public boolean moreThan2Humans(Game game) {
        boolean ret_val = false;
        boolean[] players = game.getHumanControl();
        int count = 0;
        for (int i = 0; i < players.length; i++) {
            if (players[i]) {
                if (++count > 2) {
                    ret_val = true;
                    break;
                }
            }
        }
        return ret_val;
    }

    /**
     * Test that all revocation confirmation passwords are null
     *
     * @param game
     * @return
     */
    public boolean testConfirmPasswdsNull(Game game) {
        boolean ret_val = true;
        boolean[] players = game.getHumanControl();
        for (int i = 0; i < players.length; i++) {
            if (players[i] == true && pbem.revoke_confirm[i] != null) {
                ret_val = false;
                break;
            }
        }
        return ret_val;
    }

    /**
     * Set revocation confirmation password of <code> faction </code>
     *
     * @param faction
     */
    public void setRevokeConfirm(int faction) {
        MessageDigest md = getMessageDigest();
        pbem.revoke_confirm[faction] = digest(md, pbem.passwd_hashes[faction], pbem.salt);
    }

    /**
     * Zero (set null) password of <code> revoked_player </code>
     */
    public void revokePassword() {
        pbem.passwd_hashes[pbem.revoked_player] = null;
    }

    /**
     * Zero (set null) revocation confirmation passwords
     */
    public void zeroRevocationConfirm() {
        for (int i = 0; i < pbem.revoke_confirm.length; i++) {
            pbem.revoke_confirm[i] = null;
        }
    }

    /**
     * Test that all required revocation confirmation passwords are set.
     *
     * @param game
     * @return
     */
    public boolean testConfirmPasswdsSet(Game game) {
        boolean ret_val = true;
        boolean[] players = game.getHumanControl();
        for (int i = 0; i < players.length; i++) {
            if (i != pbem.revoked_player && players[i] == true) {
                if (pbem.revoke_confirm[i] == null) {
                    ret_val = false;
                    break;
                }
                MessageDigest md = getMessageDigest();
                byte[] confirm_hash = digest(md, pbem.passwd_hashes[i], pbem.salt);
                if (!compHashes(confirm_hash, pbem.revoke_confirm[i])) {
                    ret_val = false;
                    break;
                }
            }
        }
        return ret_val;
    }

    /**
     * Determine current player for asking revocation confirmation password.
     *
     * @param game
     * @return
     */
    public int passwordTurn(Game game) {
        int ret_val = -1;
        boolean[] players = game.getHumanControl();
        int idx = pbem.revoked_player;
        idx++;
        for (;; idx++) {
            if (idx == players.length) {
                idx = 0;
            }
            if (idx == pbem.revoked_player) {
                break;
            }
            if (players[idx] == true && pbem.revoke_confirm[idx] == null) {
                ret_val = idx;
                break;
            }

        }
        return ret_val;
    }

    /**
     * Ask for password revocation action (zero password or set to computer
     * control.)
     *
     * @param gui
     * @param game
     */
    public void getAction(Gui gui, Game game) {
//        this.f_idx = faction;
        final JDialog dialog = new JDialog(gui, null, true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
        JLabel text = new JLabel("Select action for password revocation");
        text.setAlignmentX(Component.CENTER_ALIGNMENT);
        text.setForeground(C.COLOR_GOLD);
        text.setFont(Gui.getWindowSize().font_large);
//        int faction = -1;

//        List<JRadioButton> buttons = new ArrayList<>();
//        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//        ButtonGroup b_group = new ButtonGroup();
//        boolean[] players = game.getHumanControl();
//        for (int i = 0; i < players.length; i++) {
//            if (players[i] == true) {
//                JRadioButton button = new JRadioButton(Util.getFactionName(i));
//                b_group.add(button);
//                panel.add(button);
//                revoked_holder = i;
//                button.addActionListener(new ActionListener() {
//                    public void actionPerformed(ActionEvent e) {
//                        password_revocation = revoked_holder;
//                    }
//                });
//            }
//
//        }
        dialog.getContentPane().add(text);

        JButton zero_passwd_button = new JButton("Zero Password");
        zero_passwd_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                pbem.revocation_action = true;
                dialog.setVisible(false);
                dialog.dispose(); //To change body of generated methods, choose Tools | Templates.
            }
        });

        JButton comp_control_button = new JButton("Comp Control");
        comp_control_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                pbem.revocation_action = false;
                dialog.setVisible(false);
                dialog.dispose(); //To change body of generated methods, choose Tools | Templates.
            }
        });

        JPanel panel = new JPanel();
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
//        panel.add(Box.createRigidArea(new Dimension(25, 0)));
        panel.add(zero_passwd_button);
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        panel.add(comp_control_button);
        dialog.getContentPane().add(panel);
//        dialog.add(button);
        dialog.pack();
        UtilG.setDialogLocation(dialog, gui);
        dialog.setVisible(true);

    }
}
