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

import java.awt.Point;
import java.awt.event.MouseEvent;
import static state.State.ws;
import util.C;
import util.Util;

/**
 * Planet Window PW, superclass of all Planet Windows
 *
 * @author joulupunikki
 */
public class PW extends MW {

//    public void pressNextStackButton() {
//        SU.pressNextStackButtonSU();
//    }
//
//    public void pressSkipStackButton() {
//        SU.pressSkipStackButtonSU();
//    }
//
//    public void pressEndTurnButton() {
//        if (game.getEfs_ini().pbem) {
//            game.setSelectedPoint(null, -1);
//            game.setSelectedFaction(-1);
//            gui.saveGame();
//            return;
//        }
//        game.endTurn();
//        game.setPath(null);
//
//        SU.selectNextUnmovedUnit();
//
//    }
    public void pressSpaceButton() {
        SU.pressSpaceButtonSU();
    }

    public void clickOnGlobeMap(MouseEvent e) {

        Point p = e.getPoint();
        int x = p.x / (ws.globe_map_width / C.PLANET_MAP_WIDTH);
        int y = p.y / (ws.globe_map_height / (C.PLANET_MAP_COLUMNS - 1));
        x = x - 6;
        y = y - 4;

        Point q = Util.forcePlanetMapCoordinates(new Point(x, y));

        game.setMapOrigin(q);
    }

    public void clickOnPlanetWindow(MouseEvent e) {
        Point p = e.getPoint();
        if (SU.isOnStackDisplay(p)) {
            SU.clickOnStackDisplay(e);
        } else {
            int res = SU.isOnResourceIcon(p);
            if (res > -1) {
                SU.clickOnResourceIcon(res);
            }
        }

    }
}
