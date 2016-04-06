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

import java.awt.Point;
import java.awt.event.MouseEvent;
import util.C;
import util.Util;

/**
 * Space To Planet, superclass of states where spaceships in orbit interact with
 * planetary hexes.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class STP extends State {

    public void clickOnGlobeMap(MouseEvent e) {
        if (!game.getEfs_ini().STP_minimap_navigation) {
            return;
        }
// TODO making this work for STP may require a bit of State tweaking
//        if (e.getButton() == MouseEvent.BUTTON3) {
//            setWindow(C.S_GLOBE_WINDOW);
//            saveMainGameState();
//            gui.getGlobeWindow().initWindow();
//            gui.setCurrentState(GLO.get());
//            return;
//        }
        Point p = e.getPoint();
        int x = p.x / (ws.globe_map_width / C.PLANET_MAP_WIDTH);
        int y = p.y / (ws.globe_map_height / (C.PLANET_MAP_COLUMNS - 1));
        x = x - 6;
        y = y - 4;

        Point q = Util.forcePlanetMapCoordinates(new Point(x, y));

        game.setMapOrigin(q);
    }
}
