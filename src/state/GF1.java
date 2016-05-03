/*
 * Copyright (C) 2016 joulupunikki joulupunikki@gmail.communist.invalid.
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

import galaxyreader.Planet;
import galaxyreader.Unit;
import game.Hex;
import gui.UnitInfoWindow;
import java.awt.Point;
import java.util.List;
import util.C;
import util.Util;
import static gui.UnitInfoWindow.TYPE_FILTER.TRANSPORTS;

/**
 * Unit info window/Group finder mode
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class GF1 extends UIW1 {

    private static GF1 instance = new GF1();

    private GF1() {
    }
    enum S_State {
        PLANET,
        SPACE,
        BEGIN,
        END
    }
    public static State get() {
        return instance;
    }

    @Override
    public void pressExitButton() {
        gui.getUnitInfoWindow().restoreSelectedStack();
        super.pressExitButton();
    }

    /**
     *
     */
    @Override
    public void pressGoButton() {
        gui.getUnitInfoWindow().forgetSelected();
        boolean none_selected = true;
        boolean in_space = false;
        for (Unit unit : game.getSelectedStack()) {
            in_space = unit.in_space;
            if (unit.selected) {
                none_selected = false;
                break;
            }
        }
        State tmp = null;
        if (none_selected) {

            if (in_space) {
                tmp = SW1B.get();
            } else {
                tmp = PW1B.get();
            }

        } else {
            if (in_space) {
                tmp = SW2.get();
            } else {
                tmp = PW2.get();
            }
            // netbeans kills the next } on save unless this is here
        }

        gui.setCurrentState(tmp);
        main_game_state = tmp;
        SU.restoreMainWindow();
        SU.centerMapOnUnit(game.getSelectedStack().get(0), false);
        main_game_state = null;
        gui.setInfo_unit(null);

    }

    @Override
    protected void setState() {
        gui.setCurrentState(GF2.get());
    }

//    @Override
//    public void pressNextButton() {
//        List<Unit> stack = game.getSelectedStack();
//
//    }

//    @Override
//    public void pressDisbandButton() {
//        super.pressDisbandButton();
//        gui.getUnitInfoWindow().initStacks();
//    }

    private void setSelectedStack(List<Unit> stack) {
        if (stack.isEmpty()) {
            return;
        }
        int x;
        int y;
        Unit tmp = stack.get(0);
        game.setCurrentPlanetNr(tmp.p_idx);
        x = tmp.x;
        y = tmp.y;
        int f_idx = -1;
        if (tmp.in_space) {
            Planet planet = game.getPlanet(tmp.p_idx);
            x = planet.x;
            y = planet.y;
            f_idx = tmp.prev_owner;
            game.setSelectedFaction(tmp.owner, tmp.prev_owner);
        } else {
            game.setSelectedFaction(-1);
        }
        game.setSelectedPoint(new Point(x, y), f_idx);
    }

    @Override
    public void pressPrevButton() {
        System.out.println("Prev *****************");
        gui.getUnitInfoWindow().enableNext(true);
        long time = System.currentTimeMillis();
        List<Unit> stack = game.getSelectedStack();
        int p_idx = 0;
        int x = 0;
        int y = 0;
        int space_slot = C.NR_FACTIONS;
        S_State state = S_State.PLANET;
        Util.HexIter hex_iter = null;
        Hex hex = null;
        if (stack == null) {
            p_idx = game.getCurrentPlanetNr();
        } else {
            p_idx = stack.get(0).p_idx;
            x = stack.get(0).x;
            y = stack.get(0).y;
            if (stack.get(0).in_space) {
                state = S_State.SPACE;
                space_slot = stack.get(0).prev_owner;
            }
        }

        if (state == S_State.PLANET) { // initialize hex iterator properly for planets
            hex_iter = Util.getHexIter(game, p_idx);
            hex_iter.setPos(x, y);
            if (stack != null) {
                hex_iter.prev(); //!
            } else {
                hex_iter.setPosLast();
            }
        }
//        else if (p_idx == game.getPlanets().size() - 1) {
//            state = S_State.END;
//        } else { // if in space and not last planet
//            p_idx++;
//            hex_iter = Util.getHexIter(game, p_idx);
//            state = S_State.PLANET;
//        }
        boolean loop = true;
        int counter = 0;

        while (loop) {
            switch (state) {
//                case BEGIN:
//                    loop = false;
//                    break;
                case PLANET:
                    hex = hex_iter.prev();
                    if (hex == null) {
                        if (p_idx <= 0) {
                            state = S_State.BEGIN;
                        } else {
                            --p_idx;
                            space_slot = C.NR_FACTIONS;
                            state = S_State.SPACE;
                        }
                    } else {
                        stack = hex.getStack();
                        if (foundGroup(stack)) {
                            loop = false;
                            System.out.println("found stack");
                        }
                    }
                    break;
                case SPACE:
                    --space_slot;
                    if (space_slot < 0) {
                        System.out.println(state + " " + p_idx);
                        hex_iter = Util.getHexIter(game, p_idx);
                        hex_iter.setPosLast();
                        state = S_State.PLANET;
                    } else {
                        stack = game.getPlanet(p_idx).space_stacks[space_slot];
                        if (foundGroup(stack)) {
                            loop = false;
                        }
                    }

//                    if (!stack.isEmpty() && stack.get(0).owner == game.getTurn()) {
//                        loop = false;
//                    } else if (p_idx == game.getPlanets().size() - 1) {
//                        state = S_State.END;
//                        loop = false;
//                    } else {
//                        p_idx++;
//                        hex_iter = Util.getHexIter(game, p_idx);
//                        state = S_State.PLANET;
//                    }
                    break;
                case BEGIN:
                    gui.getUnitInfoWindow().enablePrev(false);
                    loop = false;
                    break;
                default:
                    throw new AssertionError();
            }
            counter++;

        }
        System.out.println("loop " + counter + " time " + (System.currentTimeMillis() - time) + "ms");
        setSelectedStack(stack);
//        List<Unit> stack = game.getSelectedStack();
//        int p_idx = 0;
//        int x = 0;
//        int y = 0;
//        S_State state = S_State.PLANET;
//        Util.HexIter hex_iter = null;
//        Hex hex = null;
//        if (!stack.isEmpty()) {
//            p_idx = game.getCurrentPlanetNr();
//        } else {
//            p_idx = stack.get(0).p_idx;
//            x = stack.get(0).x;
//            y = stack.get(0).y;
//            if (stack.get(0).in_space) {
//                state = S_State.SPACE;
//            }
//        }
//        // initialize hex iterator properly
//        if (state == S_State.PLANET) {
//            hex_iter = Util.getHexIter(game, p_idx);
//            hex_iter.setPos(x, y);
//            if (!stack.isEmpty()) {
//                hex_iter.prev(); //!
//            }
//        }
//        boolean loop = true;
//        while (loop) {
//            System.out.println("prev " + state);
//            switch (state) {
//                case BEGIN:
//                    loop = false;
//                    break;
//                case PLANET:
//                    hex = hex_iter.prev();
//                    if (hex == null) {
//                        if (p_idx == 0) {
//                            state = S_State.BEGIN;
//                        } else {
//                            state = S_State.SPACE;
//                            p_idx--;
//                        }
//
//                    } else {
//                        stack = hex.getStack();
//                        if (!stack.isEmpty() && stack.get(0).owner == game.getTurn()) {
//                            loop = false;
//                        }
//                    }
//                    break;
//                case SPACE:
//                    stack = game.getPlanet(p_idx).space_stacks[game.getTurn()];
//                    if (!stack.isEmpty() && stack.get(0).owner == game.getTurn()) {
//                        loop = false;
//                    } else {
//                        hex_iter = Util.getHexIter(game, p_idx);
//                        hex_iter.setPosLast();
//                        state = S_State.PLANET;
//                    }
//                    break;
////                case END:
////                    loop = false;
////                    break;
//                default:
//                    throw new AssertionError();
//            }
//        }
//        setSelectedStack(stack);
    }

    private boolean foundGroup(List<Unit> stack) {

        if (stack.isEmpty() || stack.get(0).owner != game.getTurn()) {
            return false;
        }
        UnitInfoWindow.TYPE_FILTER type_filter = gui.getUnitInfoWindow().getTypeFilter();
        if (type_filter.equals(UnitInfoWindow.TYPE_FILTER.ALL_TYPES)) {
            return true;
        }
        for (Unit unit : stack) {
            switch (type_filter) {
                case TRANSPORTS:
                    if (unit.type_data.cargo > 0) {
                        return true;
                    }
                    break;
                default:
                    if (unit.type == type_filter.type_no) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    @Override
    public void pressNextButton() {
        System.out.println("Next *****************");
        gui.getUnitInfoWindow().enablePrev(true);
        long time = System.currentTimeMillis();
        List<Unit> stack = game.getSelectedStack();
        int p_idx = 0;
        int x = 0;
        int y = 0;
        int space_slot = -1;
        S_State state = S_State.PLANET;
        Util.HexIter hex_iter = null;
        Hex hex = null;
        if (stack == null) {
            p_idx = game.getCurrentPlanetNr();
        } else {
            p_idx = stack.get(0).p_idx;
            x = stack.get(0).x;
            y = stack.get(0).y;
            if (stack.get(0).in_space) {
                state = S_State.SPACE;
                space_slot = stack.get(0).prev_owner;
            }
        }

        if (state == S_State.PLANET) { // initialize hex iterator properly for planets
            hex_iter = Util.getHexIter(game, p_idx);
            hex_iter.setPos(x, y);
            if (stack != null) {
                hex_iter.next(); //!
            }
        }
//        else if (p_idx == game.getPlanets().size() - 1) {
//            state = S_State.END;
//        } else { // if in space and not last planet
//            p_idx++;
//            hex_iter = Util.getHexIter(game, p_idx);
//            state = S_State.PLANET;
//        }
        boolean loop = true;
        int counter = 0;

        while (loop) {           
            switch (state) {
//                case BEGIN:
//                    loop = false;
//                    break;
                case PLANET:
                    hex = hex_iter.next();
                    if (hex == null) {
                        space_slot = -1;
                        state = S_State.SPACE;
                    } else {
                        stack = hex.getStack();
                        if (foundGroup(stack)) {
                            loop = false;
                            System.out.println("found stack");
                        }
                    }
                    break;
                case SPACE:
                    ++space_slot;
                    if (space_slot >= C.NR_FACTIONS) {
                        System.out.println(state + " " + p_idx);
                        if (p_idx >= game.getPlanets().size() - 1) {
                            state = S_State.END;
                        } else {
                            p_idx++;
                            hex_iter = Util.getHexIter(game, p_idx);
                            state = S_State.PLANET;
                        }
                    } else {
                        stack = game.getPlanet(p_idx).space_stacks[space_slot];
                        if (foundGroup(stack)) {
                            loop = false;
                        }
                    }

//                    if (!stack.isEmpty() && stack.get(0).owner == game.getTurn()) {
//                        loop = false;
//                    } else if (p_idx == game.getPlanets().size() - 1) {
//                        state = S_State.END;
//                        loop = false;
//                    } else {
//                        p_idx++;
//                        hex_iter = Util.getHexIter(game, p_idx);
//                        state = S_State.PLANET;
//                    }
                    break;
                case END:
                    gui.getUnitInfoWindow().enableNext(false);
                    loop = false;
                    break;
                default:
                    throw new AssertionError();
            }
            counter++;

        }
        System.out.println("loop " + counter + " time " + (System.currentTimeMillis() - time) + "ms");
        setSelectedStack(stack);

    }

}
