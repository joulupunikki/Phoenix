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

import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Regency;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;
import util.C;
import util.Util;

/**
 * Byzantium II Window base state.
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class ByzII extends State {

    private static ByzII instance = new ByzII();

    private ByzII() {
    }

    public static State get() {
        return instance;
    }

    @Override
    public void pressExitButton() {
        game.initVisibilitySpot(false); // re-spot with ministry assets
        SU.restoreMainWindow();
        gui.setCurrentState(main_game_state);
        main_game_state = null;
    }

    @Override
    public void pressVoteButton() {
        if (game.getRegency().needToVote(game.getTurn(), game.getEfs_ini(), game.getYear())) {
            gui.setCurrentState(ByzII2.get());
            gui.showInfoWindow("Cast your votes, Lord " + Util.getFactionName(game.getTurn()) + ".");
            gui.getByzantium_ii_window().enableAbstainButton(true);
            gui.getByzantium_ii_window().enableVoteButton(false);
        }
    }

    @Override
    public void clickOnByzantiumIIWindow(MouseEvent e) {
        Point p = e.getPoint();
        int pos = getPosition(p);
        Regency r = game.getRegency();

        switch (pos) {
            case C.STIGMATA:
            case C.THE_SPY:
            case C.FLEET:
                if (game.getTurn() != game.getRegency().getRegent()) {
                    gui.showInfoWindow("Only the Regent may assign offices!");
                    return;
                }
                break;
            case C.IMPERIAL: // regent banner
                break;
            default:
                return;
        }

        switch (pos) {
            case C.STIGMATA:
                r.setGarrison(cycleMinistry(r.getGarrison()));
                setAssets(pos, r.getGarrison());
                break;
            case C.THE_SPY:
                r.setEye(cycleMinistry(r.getEye()));
                setAssets(pos, r.getEye());
                break;
            case C.FLEET:
                r.setFleet(cycleMinistry(r.getFleet()));
                setAssets(pos, r.getFleet());
                break;
            case C.IMPERIAL: // this is used during debugging
                r.setRegent(cycleMinistry(r.getRegent()));
                break;
            default:
                throw new AssertionError();
        }
    }

    private int cycleMinistry(int value) {
        value++;
        if (value > C.HOUSE5) {
            value = -1;
        }
        return value;
    }

    private void setAssets(int ministry, int faction) {
        List<Unit> u = game.getUnits();
        List<Structure> s = game.getStructures();
        Point p = new Point(faction, ministry);
        if (faction == -1) {
            p.x = ministry;
        }
        
        for (Unit u1 : u) {
            if (u1.prev_owner == ministry) {
                game.changeOwnerOfUnit(p, u1);
            }
        }
        for (Structure s1 : s) {
            if (s1.prev_owner == ministry) {
                game.getEconomy().updateProdConsForCity(s1, false);
                s1.owner = p.x;
                game.getEconomy().updateProdConsForCity(s1, true);
            }
        }
    }
    
    static protected int getPosition(Point p) {
        int pos = -1;
        if (ws.bz2_ministry_y1 <= p.y && p.y <= ws.bz2_ministry_y2) {
            if (ws.bz2_stigmata_x1 <= p.x && p.x <= ws.bz2_stigmata_x2) {
                pos = C.STIGMATA;
            } else if (ws.bz2_eye_x1 <= p.x && p.x <= ws.bz2_eye_x2) {
                pos = C.THE_SPY;
            } else if (ws.bz2_fleet_x1 <= p.x && p.x <= ws.bz2_fleet_x2) {
                pos = C.FLEET;
            }
        } else if (ws.bz2_regent_x1 <= p.x && p.x <= ws.bz2_regent_x2
                && ws.bz2_regent_y1 <= p.y && p.y <= ws.bz2_regent_y2) {
            pos = C.IMPERIAL; // using faction ID of IMPERIAL for regent
        } else if (ws.bz2_house_banner_y1 <= p.y && p.y <= ws.bz2_house_banner_y2) {
            if (ws.bz2_house_names_x11 <= p.x && p.x <= ws.bz2_house_names_x11 + ws.bz2_house_names_w) {
                pos = C.HOUSE1;
            } else if (ws.bz2_house_names_x12 <= p.x && p.x <= ws.bz2_house_names_x12 + ws.bz2_house_names_w) {
                pos = C.HOUSE2;
            } else if (ws.bz2_house_names_x13 <= p.x && p.x <= ws.bz2_house_names_x13 + ws.bz2_house_names_w) {
                pos = C.HOUSE3;
            } else if (ws.bz2_house_names_x14 <= p.x && p.x <= ws.bz2_house_names_x14 + ws.bz2_house_names_w) {
                pos = C.HOUSE4;
            } else if (ws.bz2_house_names_x15 <= p.x && p.x <= ws.bz2_house_names_x15 + ws.bz2_house_names_w) {
                pos = C.HOUSE5;
            }
            System.out.println(pos);
        }
        return pos;
    }

}
