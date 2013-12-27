/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import galaxyreader.Unit;
import game.Square;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import util.C;

/**
 * Unit info window unit dragged
 *
 * @author joulupunikki
 */
public class UIW2 extends State {

    private static UIW2 instance = new UIW2();

    public UIW2() {
    }

    public static State get() {
        return instance;
    }

    public void dragOnWindow(MouseEvent e) {
        Point p = e.getPoint();
        System.out.println("p = " + p);
        gui.setDragPoint(p);
        gui.getMainWindows().repaint();
    }

    public void releaseOnWindow(MouseEvent e) {
        System.out.println("e = " + e);
        Unit drag_unit = gui.getDragUnit();
        Point q = e.getPoint();
        int faction = game.getSelectedFaction();
        Point p = game.getSelectedPoint();

        List<Unit> stack = null;
        boolean[] terrain = null;
        int tile_set = -1;
        if (faction == -1) {
            stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y).getStack();
            terrain = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y).getTerrain();
            System.out.println("terrain = " + terrain[C.OCEAN]);
            tile_set = game.getPlanet(game.getCurrentPlanetNr()).tile_set_type;
        } else {
            Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
            stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[faction];
        }


        boolean is_cargo_listing = false;
        Iterator<Unit> iterator = stack.listIterator();
        Iterator<Unit> cargo_it = null;
        Unit u = iterator.next();

        for (int i = 0; i < C.STACK_WINDOW_UNITS_Y; i++) {
            for (int j = 0; j < C.STACK_WINDOW_UNITS_X; j++) {
                int dx = (int) (ws.unit_panel_x_offset + j * 3.5 * ws.unit_icon_size);
                int dy = (int) (ws.unit_panel_y_offset + i * 1.15 * ws.unit_icon_size);

                if (dx <= q.x && q.x <= dx + ws.unit_icon_size
                        && dy <= q.y && q.y <= dy + ws.unit_icon_size) {
                    if (!drag_unit.equals(u)) {
                        if (drag_unit.carrier != null) {
                            if ((faction != -1 && !(drag_unit.move_type == C.MoveType.JUMP
                                    || drag_unit.move_type == C.MoveType.LANDER
                                    || drag_unit.move_type == C.MoveType.SPACE))
                                    && (u.type_data.cargo == 0 || u.cargo_list.size() >= u.type_data.cargo)) {
                                JOptionPane.showMessageDialog(gui, "Cannot unload cargo in space.", null, JOptionPane.PLAIN_MESSAGE);
                            } else if (faction == -1 && terrain[C.OCEAN] == true && tile_set != 4) {
                                JOptionPane.showMessageDialog(gui, "Cannot unload cargo on the ocean.", null, JOptionPane.PLAIN_MESSAGE);

                            } else {
                                Unit carrier = drag_unit.carrier;
                                carrier.disembark(drag_unit);
                                stack.add(drag_unit);
//                            drag_unit.carrier = null;
                                System.out.println("carrier = " + carrier);

                            }
                        }
                        if (u.type_data.cargo > 0) {
                            if (drag_unit.type_data.can_b_cargo == 1) {
                                if (u.cargo_list.size() < u.type_data.cargo) {
                                    if (u.embark(drag_unit)) {
                                        stack.remove(drag_unit);
                                        boolean selected = false;
                                        for (Unit unit : stack) {
                                            if (unit.selected == true) {
                                                selected = true;
                                                break;
                                            }
                                        }
                                        if (!selected) {
                                            stack.get(0).selected = true;
                                        }
//                            drag_unit.carrier = u;
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(gui, "Transport is full.", null, JOptionPane.PLAIN_MESSAGE);
                                }
                            } else {
                                JOptionPane.showMessageDialog(gui, "That unit cannot be loaded onto a transport.", null, JOptionPane.PLAIN_MESSAGE);
                            }
                        }
                    }

                    gui.setDragUnit(null, null);
                    gui.setCurrentState(UIW1.get());
                    gui.getMainWindows().repaint();
                    return;
                }


//                if (iterator.hasNext()) {
//                    u = iterator.next();
//                } else {
//                    return;
//                }
                if (is_cargo_listing) {
                    u = cargo_it.next();
                    if (!cargo_it.hasNext()) {
                        cargo_it = null;
                        is_cargo_listing = false;
                    }
                } else if (u.cargo_list.isEmpty()) {
                    if (iterator.hasNext()) {
                        u = iterator.next();
                    } else {
                        if (drag_unit.carrier != null) {
                            if (faction != -1 && !(drag_unit.move_type == C.MoveType.JUMP
                                    || drag_unit.move_type == C.MoveType.LANDER
                                    || drag_unit.move_type == C.MoveType.SPACE)) {
                                JOptionPane.showMessageDialog(gui, "Cannot unload cargo in space.", null, JOptionPane.PLAIN_MESSAGE);
                            } else {
                                Unit carrier = drag_unit.carrier;
                                carrier.disembark(drag_unit);
                                stack.add(drag_unit);
//                            drag_unit.carrier = null;
                                System.out.println("carrier = " + carrier);

                            }
                        }
                        gui.setDragUnit(null, null);
                        gui.setCurrentState(UIW1.get());
                        gui.getMainWindows().repaint();
                        return;
                    }
                } else {
                    cargo_it = u.cargo_list.listIterator();
                    u = cargo_it.next();
                    if (cargo_it.hasNext()) {
                        is_cargo_listing = true;
                    }
                }

            }

        }


        gui.setDragUnit(null, null);
        gui.setCurrentState(UIW1.get());
        gui.getMainWindows().repaint();
    }
}
