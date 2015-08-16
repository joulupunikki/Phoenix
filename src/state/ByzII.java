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

/**
 * Byzantium II Window
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
    public void clickOnByzantiumIIWindow(MouseEvent e) {
        Point p = e.getPoint();
        int pos = getPosition(p);
        Regency r = game.getRegency();

        switch (pos) {
            case C.STIGMATA:
            case C.THE_SPY:
            case C.FLEET:
//                if (game.getTurn() != game.getRegency().getRegency()) {
//                    gui.showInfoWindow("Only the Regent may assign ministries.");
//                    return;
//                }
                break;
            default:
                break;
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
        for (Unit u1 : u) {
            if (u1.prev_owner == ministry) {
                u1.owner = faction;
            }
        }
        for (Structure s1 : s) {
            if (s1.prev_owner == ministry) {
                game.getEconomy().updateProdConsForCity(s1, false);
                s1.owner = faction;
                game.getEconomy().updateProdConsForCity(s1, true);
            }
        }
    }
    
    private int getPosition(Point p) {
        int pos = -1;
        if (181 <= p.y && p.y <= 303) {
            if (66 <= p.x && p.x <= 185) {
                pos = C.STIGMATA;
            } else if (261 <= p.x && p.x <= 380) {
                pos = C.THE_SPY;
            } else if (456 <= p.x && p.x <= 575) {
                pos = C.FLEET;
            }
        }
        return pos;
    }

}
