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
package state;

import game.Game;
import gui.Gui;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JCheckBox;
import util.WindowSize;

/**
 * Superclass for all state implementing objects.
 *
 * @author joulupunikki
 */
public abstract class State {
//    public static final Logger logger = LogManager.getLogger();
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
    }

    public void clickOnPlanetMap(MouseEvent e) {
    }

    public void clickOnPlanetWindow(MouseEvent e) {
    }

    public void clickOnSpaceWindow(MouseEvent e) {
    }

    public void clickOnSpaceMap(MouseEvent e) {
    }

    public void clickOnByzantiumIIWindow(MouseEvent e) {
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

    public void pressBuySellButton() {
    }

    public void pressCancelButton() {
    }

    public void pressTradeButton() {
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

    public void pressBombardAgainButton() {
    }

    public void clickOnGalacticMap(MouseEvent e) {
    }

    public void clickOnGlobeMap(MouseEvent e) {
    }

    public void pressStartNew() {

    }

    public void pressLoadGame() {

    }

    public void pressQuit() {

    }

    public void pressVoteButton() {
    }

    public void pressAbstainButton() {
    }

    public void pressDeclareEmperorButton() {
    }

    public void pressDisbandButton() {
    }

    public void pressUnloadButton() {
    }

    public void razeCity() {
    }
}
