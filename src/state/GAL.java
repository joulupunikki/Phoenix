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

import gui.Gui;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Map;
import util.C;
import util.G.CGW;

/**
 * Galaxy Window state.
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class GAL extends State {

    private static GAL instance = new GAL();

    private Map<Enum, Integer> c = Gui.getWindowSize().galaxy_window;
    private GAL() {
    }

    public static State get() {
        return instance;
    }

    @Override
    public void pressExitButton() {
        SU.restoreMainWindow();
        gui.setCurrentState(main_game_state);
        main_game_state = null;
    }

    /**
     *
     * @param e
     */
    @Override
    public void clickOnWindow(MouseEvent e) {
        Point p = e.getPoint();
        p.x -= c.get(CGW.MAP_X) + c.get(CGW.MAP_MARGIN);
        p.y -= c.get(CGW.MAP_Y);
        int map_width = c.get(CGW.MAP_W) - 2 * c.get(CGW.MAP_MARGIN);
        if (0 <= p.x && p.x <= map_width
                && 0 <= p.y && p.y <= c.get(CGW.MAP_H) + c.get(CGW.MAP_Y)) {
            SU.setSpaceMapOrigin(p.x * C.STAR_MAP_WIDTH / map_width, p.y * C.STAR_MAP_HEIGHT / c.get(CGW.MAP_H));
            pressExitButton();
        }
    }


}
