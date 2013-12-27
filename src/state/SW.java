/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import java.awt.Point;
import java.awt.event.MouseEvent;
import util.C;
import util.Util;

/**
 *
 * @author joulupunikki
 */
public class SW extends State {

    public void pressNextStackButton() {
        SU.pressNextStackButtonSU();
    }

    public void pressEndTurnButton() {
        game.endTurn();
        game.setJumpPath(null);
        SU.selectNextUnmovedUnit();
    }

    public void clickOnGalacticMap(MouseEvent e) {
        
        Point p = e.getPoint();
        int x = p.x / (ws.galactic_map_width / C.STAR_MAP_WIDTH);
        int y = p.y / (ws.galactic_map_height / C.STAR_MAP_HEIGHT);
        x = x - 6;
        y = y - 5;
        
        Point q = Util.forceSpaceMapCoordinates(new Point(x, y));
        
        game.setSpaceMapOrigin(q);
    }

}
