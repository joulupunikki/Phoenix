/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import gui.Gui;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import util.C;
import util.Util;

/**
 * Class containing variables and methods to enable secure PBEM play.
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class PBEM implements Serializable {

    public boolean pbem = false;
    public boolean end_turn = false;
    public List<byte[]> checksums = new LinkedList<>();
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
    }

    public void getPasswd(int faction, Gui gui) {
        this.f_idx = faction;
        dialog = new JDialog(gui, "Enter new password twice, noble " + Util.getFactionName(f_idx), true);
//        dialog.setLayout(new BoxLayout(dialog, BoxLayout.PAGE_AXIS));
        text = new JLabel("Enter new password twice, noble " + Util.getFactionName(f_idx));
        pwf1 = new JPasswordField(20);
        pwf2 = new JPasswordField(20);

        char[] pw = null;
        JButton button = new JButton("OK");
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
                passwd_hashes[f_idx] = md.digest(Util.toBytes(pw1));
                Arrays.fill(pw1, '0');
                Arrays.fill(pw2, '0');

                dialog.dispose(); //To change body of generated methods, choose Tools | Templates.
            }
        });

        dialog.getContentPane().add(text, BorderLayout.PAGE_START);
        dialog.getContentPane().add(pwf1, BorderLayout.PAGE_START);
        dialog.getContentPane().add(pwf2, BorderLayout.CENTER);
        dialog.getContentPane().add(button, BorderLayout.PAGE_END);
//        dialog.add(button);
        dialog.pack();
        dialog.setVisible(true);
    }

    public boolean testPasswd(int faction, Gui gui) {
        this.f_idx = faction;
        dialog = new JDialog(gui, "Enter password, noble " + Util.getFactionName(f_idx), true);
        pwf1 = new JPasswordField(20);
        tries = 0;
        passwd_ok = false;
        final Gui gui_ref = gui;

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
                byte[] passwd_hash = md.digest(Util.toBytes(pw1));
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

        dialog.getContentPane().add(pwf1, BorderLayout.PAGE_START);
        dialog.getContentPane().add(button, BorderLayout.PAGE_END);
//        dialog.add(button);
        dialog.pack();
        dialog.setVisible(true);

        return passwd_ok;
    }
}
