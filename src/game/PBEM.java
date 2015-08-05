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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import util.C;

/**
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class PBEM implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
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

    public PBEM() {
        for (int i = 0; i < passwd_hashes.length; i++) {
            passwd_hashes[i] = null;
            revoke_confirm[i] = null;
        }
        long long_val = System.currentTimeMillis();
        salt = String.valueOf(long_val).getBytes();
    }

}
