/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import galaxyreader.Planet;
import galaxyreader.Structure;
import galaxyreader.Unit;
import game.CombatReport;
import game.Message;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import util.C;

/**
 * MsgsW (Messages Window)
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class MsgsW extends State {

    private static MsgsW instance = new MsgsW();

    public MsgsW() {
    }

    public static State get() {
        return instance;
    }

    public void pressExitButton() {
        SU.restoreMainWindow();
        gui.setCurrentState(main_game_state);
        main_game_state = null;
    }

    public void clickOnWindow(MouseEvent e) {
        JTable message_table = (JTable) e.getSource();
        Point p = e.getPoint();
        int row = message_table.rowAtPoint(p);
        int col = message_table.columnAtPoint(p);
        if (row == -1) {
            return;
        }
        if (e.getClickCount() == 1) {
            System.out.println("Single clicked row " + row);
        }
        if (e.getClickCount() == 2) {
//                    message_table.clearSelection();

            System.out.println("Double clicked row " + row);
            C.Msg msg_type = (C.Msg) message_table.getValueAt(row, 0);
            Message msg = (Message) message_table.getValueAt(row, 1);
            if (col == 0) {
                switch (msg_type) {
                    case COMBAT_REPORT:

                        CombatReport report = (CombatReport) msg.getSource();
                        game.getBattle().setCombatStacks(report.attacker, report.defender);
                        System.out.println("combat stacks set");
                        gui.getMessages().setCombatReport(report);
//                        current_cmbt_report = report;
                        SU.showCombatReplay();
                        break;
                    default:
                        break;
//                            throw new AssertionError();
                }
            } else if (col == 1) {
                Object source = msg.getSource();
                int p_idx = -1;
                int p_x = -1;
                int p_y = -1;
                if (source instanceof Planet) {

                    p_idx = ((Planet) source).index;
                } else if (source instanceof Structure) {
                    Structure city = (Structure) source;
                    p_idx = city.p_idx;
                    p_x = city.x;
                    p_y = city.y;
                } else if (source instanceof CombatReport) {
                    CombatReport report = (CombatReport) source;
                    Unit unit = report.defender.get(0);
                    p_idx = unit.p_idx;
                    if (!unit.in_space) {
                        p_x = unit.x;
                        p_y = unit.y;
                    }
                } else {
                    throw new AssertionError();
                }
                game.setSelectedPoint(null, -1);
                game.setSelectedFaction(-1);
                game.setPath(null);
                game.setJumpPath(null);
                game.setCurrentPlanetNr(p_idx);
                main_game_state = null;
                if (p_x < 0) {
                    Planet planet = game.getPlanet(p_idx);
                    SU.setSpaceMapOrigin(planet.x, planet.y);

                    SU.setWindow(C.S_STAR_MAP);
                    gui.setCurrentState(SW1.get());
                } else {
                    SU.setPlanetMapOrigin(p_x, p_y);
                    SU.setWindow(C.S_PLANET_MAP);
                    gui.setCurrentState(PW1.get());
                }
            }
        }
    }
}
