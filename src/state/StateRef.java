/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

/**
 *
 * @author joulupunikki
 */
public class StateRef {

    private static StateRef state_ref = new StateRef(null);
    private State state;

    private StateRef(State state) {
        this.state = state;
    }
    
    public static StateRef getStateRef() {
        return state_ref;
    }
    
    public void setState(State state) {
        this.state = state;
    }
    
    public State getState() {
        return state;
    }
}
