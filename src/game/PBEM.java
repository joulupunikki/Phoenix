/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
