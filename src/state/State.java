/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import game.Game;
import gui.Gui;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JCheckBox;
import util.WindowSize;

/**
 * Superclass for state objects. PW planet window SW space window SU State Util
 *
 * @author joulupunikki
 */
public abstract class State {

    static Gui gui;
    static Game game;
    static WindowSize ws;
    static State main_game_state;

    public static void setReferences(Gui g, Game ga, WindowSize w) {
        gui = g;
        game = ga;
        ws = w;
    }

    public static void setGameRef(Game game_ref) {
        game = game_ref;
    }

    public static void saveMainGameState() {
        main_game_state = gui.getCurrentState();
    }

    public void wheelRotated(MouseWheelEvent e) {
//        error();
    }

    public void clickOnPlanetMap(MouseEvent e) {
//        error();
    }

    public void clickOnPlanetWindow(MouseEvent e) {
//        error();
    }

    public void clickOnSpaceWindow(MouseEvent e) {
//         error();
    }

    public void clickOnSpaceMap(MouseEvent e) {
//        error();
    }

    public void clickOnStackWindow() {
    }

    public void pressExitButton() {
    }

    public void pressSpaceButton() {        
    }
    
    public void pressLaunchButton() {
    }

    public void pressEndTurnButton() {
    }

    public void pressNextStackButton() {
    }

 
    public void pressSkipStackButton() {        
    }
    public void pressBuildButton() {        
 
    }
    
    public void clickOnWindow(MouseEvent e) {
    }

    public void dragOnWindow(MouseEvent e) {
    }

    public void releaseOnWindow(MouseEvent e) {
    }

    public void selectFactionControl(ItemEvent e, JCheckBox[] hc) {
    }

    public void pressPlayButton() {
    }

    public void stackMoveEvent() {
    }

    public void pressFightButton() {
    }

    public void clickOnGalacticMap(MouseEvent e) {
    }

    public void clickOnGlobeMap(MouseEvent e) {
    }

    public void error() {
        try {
            error2();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void error2() throws NoSuchMethodException {
        System.out.println("Error, unsupported event handler for state.");
        throw new NoSuchMethodException();
    }
}
