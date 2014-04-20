/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package state;

/**
 * Wait State WS, no input accepted.
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class WS extends State {

    private static WS instance = new WS();

    public WS() {
    }

    public static State get() {
        return instance;
    }
}
