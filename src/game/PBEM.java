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
import javax.swing.JPanel;
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

    // true iff game is pbem false otherwise
    public boolean pbem = false;
    // true iff end_turn button has been pressed to save game
    public boolean end_turn = false;
    // true iff password revocation sequence has been started
    public boolean password_revocation = false;
    // faction/player whose password will be revoked
    public int revoked_player = -1;
    // true == zero password; false == set to computer control
    public boolean revocation_action = false;
    // salt for digests
    public byte[] salt = null;
    // starting player data file checksums
    public Map<String, byte[]> checksums = new HashMap<>();
    // revocation confirmation passwords, hash of passwd_hashes and salt
    public byte[][] revoke_confirm = new byte[C.NR_FACTIONS][];
    public byte[][] passwd_hashes = new byte[C.NR_FACTIONS][];

    private JDialog dialog;
    private JLabel text;
    private JPasswordField pwf1;
    private JPasswordField pwf2;
    private int f_idx;
    private int tries;
    private int passwd_ok;

    public PBEM() {
        for (int i = 0; i < passwd_hashes.length; i++) {
            passwd_hashes[i] = null;
            revoke_confirm[i] = null;
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
                file_path = file_name;
            } else {
                file_path = dir + C.S_SEPAR + file_name;
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
        text = new JLabel("Enter new PBEM password, noble " + Util.getFactionName(f_idx));
        text.setForeground(C.COLOR_GOLD);
        text.setFont(Gui.getWindowSize().font_default);
        JLabel text2 = new JLabel("Confirm password");
        text2.setForeground(C.COLOR_GOLD);
        text2.setFont(Gui.getWindowSize().font_default);
        System.out.println("Font size: " + text2.getFont().getSize());
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
        if (revocation_action) {
            revoke_info_str = "Zeroing password of " + Util.getFactionName(revoked_player);
        } else {
            revoke_info_str = "Setting " + Util.getFactionName(revoked_player) + " to computer control";
        }
        JLabel text_revoke_info = new JLabel("Password revocation of " + Util.getFactionName(revoked_player));
        JLabel text_revoke_info2 = new JLabel(revoke_info_str);
        text = new JLabel("Enter password, noble " + Util.getFactionName(f_idx));
        text.setForeground(C.COLOR_GOLD);
        text.setFont(Gui.getWindowSize().font_default);
        pwf1 = new JPasswordField(10);
        tries = 0;
        passwd_ok = PASSWORD_FAIL;
        final Gui gui_ref = gui;
        final Game game_ref = game;
        JLabel text_revoke = new JLabel("Initiate Password Revocation Sequence");
        JButton revoke_button = new JButton("Password Revocation");
        revoke_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (!gui_ref.showConfirmWindow("Are you sure you want to initiate password revocation sequence ?")) {
                    return;
                }
                getAction(gui_ref, game_ref);
                password_revocation = true;
                revoked_player = game_ref.getTurn();
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
                passwd_ok = PASSWORD_OK;
                Arrays.fill(pw1, '0');
                dialog.setVisible(false);
                dialog.dispose(); //To change body of generated methods, choose Tools | Templates.
            }
        });
        if (password_revocation) {
            dialog.getContentPane().add(text_revoke_info);
            dialog.getContentPane().add(text_revoke_info2);
        }
        dialog.getContentPane().add(text);
        dialog.getContentPane().add(pwf1);
        dialog.getContentPane().add(button);
        if (!password_revocation && testConfirmPasswdsNull(game) && moreThan2Humans(game)) {
            dialog.getContentPane().add(text_revoke);
            dialog.getContentPane().add(revoke_button);
        }
//        dialog.add(button);
        dialog.pack();
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
            if (players[i] == true && revoke_confirm[i] != null) {
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
        revoke_confirm[faction] = digest(md, passwd_hashes[faction], salt);
    }

    /**
     * Zero (set null) password of <code> revoked_player </code>
     */
    public void revokePassword() {
        passwd_hashes[revoked_player] = null;
    }

    /**
     * Zero (set null) revocation confirmation passwords
     */
    public void zeroRevocationConfirm() {
        for (int i = 0; i < revoke_confirm.length; i++) {
            revoke_confirm[i] = null;
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
            if (i != revoked_player && players[i] == true) {
                if (revoke_confirm[i] == null) {
                    ret_val = false;
                    break;
                }
                MessageDigest md = getMessageDigest();
                byte[] confirm_hash = digest(md, passwd_hashes[i], salt);
                if (!compHashes(confirm_hash, revoke_confirm[i])) {
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
        int idx = revoked_player;
        idx++;
        for (;; idx++) {
            if (idx == players.length) {
                idx = 0;
            }
            if (idx == revoked_player) {
                break;
            }
            if (players[idx] == true && revoke_confirm[idx] == null) {
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
        text.setForeground(C.COLOR_GOLD);
        text.setFont(Gui.getWindowSize().font_default);
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

                revocation_action = true;
                dialog.setVisible(false);
                dialog.dispose(); //To change body of generated methods, choose Tools | Templates.
            }
        });

        JButton comp_control_button = new JButton("Comp Control");
        comp_control_button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                revocation_action = false;
                dialog.setVisible(false);
                dialog.dispose(); //To change body of generated methods, choose Tools | Templates.
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(zero_passwd_button);
        panel.add(comp_control_button);
        dialog.getContentPane().add(panel);
//        dialog.add(button);
        dialog.pack();
        dialog.setVisible(true);

    }
}
