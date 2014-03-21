/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package state;

import dat.UnitType;
import galaxyreader.Planet;
import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Hex;
import game.PlanetGrid;
import game.Square;
import gui.BuildPanel;
import java.awt.CardLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import util.C;
import util.PathFind;
import util.StackIterator;
import util.Util;

/**
 * Utility class for states. Contains common functions.
 *
 * @author joulupunikki
 */
public class SU extends State {
//    public static void click

    public static Point getPlanetMapClickPoint(MouseEvent e) { //, Game game, WindowSize ws) {

        Point p = e.getPoint();
        int x = p.x;
        int y = p.y;

        int d_x = ws.planet_map_hex_center_x_gap;
        int d_y = ws.planet_map_hex_center_y_gap;

        int y_dip = d_y / 2;

        if (game.getMapOrigin().x % 2 == 1) {
            if ((0 <= (x - d_x) % (2 * d_x) && (x - d_x) % (2 * d_x) <= d_x)) {
                y = (y - y_dip) / d_y;
            } else {
                y = y / d_y;
            }
        } else {
            if ((0 <= x % (2 * d_x) && x % (2 * d_x) <= d_x)) {
                y = (y - y_dip) / d_y;
            } else {
                y = y / d_y;
            }
        }

        x = x / d_x;

        Point planet_map_origin = game.getMapOrigin();

        int map_point_x = planet_map_origin.x + x;
        int map_point_y = planet_map_origin.y + y;

        if (map_point_y < 0) {
            map_point_y = 0;
        }

        if (map_point_y > 31) {
            map_point_y = 31;
        }

        if (map_point_x % 2 == 0 && map_point_y > 30) {
            map_point_y = 30;
        }

        // roll-over x at x = 44
        if (map_point_x < 0) {
            // this is likely wrong by -1 but is never reached
            map_point_x = C.PLANET_MAP_WIDTH - 1 + map_point_x;
        } else if (map_point_x > 43) {
            map_point_x = map_point_x - C.PLANET_MAP_WIDTH;
        }
        System.out.println("x, y: " + x + ", " + y);
        return new Point(map_point_x, map_point_y);

    }

    public static boolean isSpotted(List<Unit> stack) {
        boolean visible = false;
        if (stack.get(0).owner == game.getTurn()) {
            visible = true;
        } else {
            for (Unit unit : stack) {
                if (unit.spotted[game.getTurn()]) {
                    visible = true;
                    break;
                }
            }
        }
        return visible;
    }

    public static void clickOnPlanetMapButton3(Point p) {
        int map_point_x = p.x;
        int map_point_y = p.y;

        Hex hex = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(map_point_x, map_point_y);
        List<Unit> stack = hex.getStack();

        if (!stack.isEmpty()) {

            if (isSpotted(stack)) {
                game.setSelectedPointFaction(new Point(map_point_x, map_point_y), -1, null, null);
                stack.get(0).selected = true;
                gui.setCurrentState(PW2.get());
            }
        }

        //if destination selected gui.setCurrentState(PW3.get());
        int map_origin_x = map_point_x - C.PLANET_MAP_ORIGIN_X_OFFSET;
        int map_origin_y = map_point_y - C.PLANET_MAP_ORIGIN_Y_OFFSET;

        // roll-over x at x = 44
        if (map_origin_x < 0) {
            map_origin_x = C.PLANET_MAP_WIDTH + map_origin_x;
        } else if (map_origin_x > 43) {
            map_origin_x = map_origin_x - C.PLANET_MAP_WIDTH;
        }

        // limit y to between 0 and (32 - 10)
        if (map_origin_y < 0) {
            map_origin_y = 0;
        } else if (map_origin_y > 32 - 10) {
            map_origin_y = 32 - 10;
        }

        game.setMapOrigin(new Point(map_origin_x, map_origin_y));

        //draw new map location
        gui.getPlanetWindow().repaint();

        System.out.println("map_X, map_y: " + map_point_x + ", " + map_point_y);

        Structure city = hex.getStructure();
        if (stack.isEmpty() && city != null && city.owner == game.getTurn()) {
            gui.showCityDialog(game.getCurrentPlanetNr(), city);
        }

    }

    public static Point getSpaceMapClickPoint(MouseEvent e) {

        Point p = e.getPoint();

        int x = p.x / ws.space_map_square_width;
        int y = p.y / ws.space_map_square_height;

        Point space_map_origin = game.getSpaceMapOrigin();

        x = space_map_origin.x + x;
        y = space_map_origin.y + y;

//        System.out.println("x, y: " + x + ", " + y);
        if (x < 0) {
            x = 0;
        } else if (C.STAR_MAP_WIDTH - 1 < x) {
            x = C.STAR_MAP_WIDTH - 1;
        }

        if (y < 0) {
            y = 0;
        } else if (C.STAR_MAP_HEIGHT - 1 < y) {
            y = C.STAR_MAP_HEIGHT - 1;
        }

        return new Point(x, y);
    }

    public static void clickOnSpaceMapButton3(Point p) {
        int x = p.x;
        int y = p.y;

        Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
        int owner = galaxy_grid[x][y].stack_owner;
        if (owner > -1) {
            if (checkForSpaceStacks(x, y, owner, galaxy_grid)) {
//                    game.setSelectedPoint(new Point(x, y));
                clickOnSpaceStack(x, y, owner, galaxy_grid);
//                gui.setCurrentState(SW2.get());
            }
        } else if (galaxy_grid[x][y].planet != null) {
            if (!galaxy_grid[x][y].planet.spotted[game.getTurn()]) {
                JOptionPane.showMessageDialog(gui, "We lost our map of this planet long ago. We must send a ship there or buy the information from another party.", null, JOptionPane.PLAIN_MESSAGE);
                return;
            }
            JPanel main_windows = gui.getMainWindows();
            game.setSelectedPointFaction(null, -1, null, null);
            game.setCurrentPlanetNr(galaxy_grid[x][y].planet.index);
            CardLayout cl = (CardLayout) main_windows.getLayout();
            cl.show(main_windows, C.S_PLANET_MAP);
//            System.out.println(galaxy_grid[x][y].name + " " + galaxy_grid[x][y].index);
            gui.setCurrentState(PW1.get());
            return;
        }

        System.out.println("(x, y): " + x + ", " + y);

        int space_map_x_origin_offset = ws.space_map_width / (2 * ws.space_map_square_width);
        int space_map_y_origin_offset = ws.space_map_height / (2 * ws.space_map_square_height);

        x -= space_map_x_origin_offset;
        y -= space_map_y_origin_offset;

        if (x < 0) {
            x = 0;
        } else if (C.STAR_MAP_WIDTH - 15 < x) {
            x = C.STAR_MAP_WIDTH - 15;
        }

        if (y < 0) {
            y = 0;
        } else if (C.STAR_MAP_HEIGHT - 13 < y) {
            y = C.STAR_MAP_HEIGHT - 13;
        }

        game.setSpaceMapOrigin(new Point(x, y));

        gui.getSpaceWindow().repaint();

    }

    public static void clickOnSpaceStack(int x, int y, int owner, Square[][] galaxy_grid) {
        if (owner < C.LEAGUE) {
//            game.setSelectedPoint(new Point(x, y));
//            game.setSelectedFaction(owner);
            List<Unit> stack = galaxy_grid[x][y].parent_planet.space_stacks[owner];
            if (isSpotted(stack)) {
                game.setSelectedPointFaction(new Point(x, y), owner, null, null);
                stack.get(0).selected = true;
                gui.setCurrentState(SW2.get());
            }
            return;
        }

        List<Integer> stack_list = new LinkedList<>();
        int factions = 3;
        for (int i = 0; i < factions; i++) {
            List<Unit> stack = galaxy_grid[x][y].parent_planet.space_stacks[owner + i];
            if (!stack.isEmpty()) {
                if (isSpotted(stack)) {
                    stack_list.add(new Integer(owner + i));
                }
            }
        }

        int size = stack_list.size();

        if (size == 0) {
            return;
        } else if (size == 1) {
//            game.setSelectedPoint(new Point(x, y));
//            game.setSelectedFaction(stack_list.get(0).intValue());
            game.setSelectedPointFaction(new Point(x, y), stack_list.get(0).intValue(), null, null);
        } else {

            Object[] options = new Object[size];
            int[] faction_nrs = new int[size];
            for (int i = 0; i < size; i++) {
                int tmp = stack_list.get(i).intValue();
                options[i] = Util.getFactionName(tmp);
                faction_nrs[i] = tmp;
            }

            int j_options = -1;

            if (size == 2) {
                j_options = JOptionPane.YES_NO_OPTION;

            } else {
                j_options = JOptionPane.YES_NO_CANCEL_OPTION;
            }

            int n = JOptionPane.showOptionDialog(gui,
                    "Who do you want to look at?",
                    "",
                    j_options,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);

//            UIManager uim = new UIManager();
//            uim.put("OptionPane.background", Color.BLACK);
//            uim.put("OptionPane.foreground", Color.YELLOW);
//            uim.put("Panel.background", Color.BLACK);
//            uim.put("Panel.foreground", Color.YELLOW);
//            uim.put("OptionPane.messageForeground", Color.YELLOW);
//
//            JOptionPane option_pane = new JOptionPane("Choose faction.",
//                    JOptionPane.QUESTION_MESSAGE,
//                    j_options,
//                    null,
//                    options);
//            JDialog dialog = new JDialog(this, true);
//            dialog.setContentPane(option_pane);
//            dialog.setDefaultCloseOperation(
//                    JDialog.DO_NOTHING_ON_CLOSE);
//            dialog.addWindowListener(new WindowAdapter() {
//                public void windowClosing(WindowEvent we) {
//                    ;
//                }
//            });
//            option_pane.setBackground(Color.BLACK);
//            option_pane.setForeground(Color.YELLOW);
//            dialog.setBackground(Color.BLACK);
//            dialog.setForeground(Color.YELLOW);
//            dialog.pack();
//            dialog.setVisible(true);
            int selected_faction = -1;

//            int n = ((Integer) option_pane.getValue()).intValue();
            switch (n) {
                case JOptionPane.YES_OPTION:
                    selected_faction = faction_nrs[0];
                    break;
                case JOptionPane.NO_OPTION:
                    selected_faction = faction_nrs[1];
                    break;
                case JOptionPane.CANCEL_OPTION:
                    selected_faction = faction_nrs[2];
                    break;
                default:
                    selected_faction = faction_nrs[0];
                    break;
            }

//            game.setSelectedPoint(new Point(x, y));
//            game.setSelectedFaction(selected_faction);
            game.setSelectedPointFaction(new Point(x, y), selected_faction, null, null);
        }
        galaxy_grid[x][y].parent_planet.space_stacks[game.getSelectedFaction()].get(0).selected = true;

        gui.setCurrentState(SW2.get());

    }

    public static void spaceToPlanet(Planet planet) {
        Point p = game.getSelectedPoint();
        int faction = game.getSelectedFaction();
        Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
        List<Unit> stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[faction];
        boolean stp_capable = false;
        for (Unit unit : stack) {
            if (unit.selected) {
                if (unit.move_points > 0) {
                    stp_capable = true;
                } else {
                    stp_capable = false;
                    break;
                }
            }
        }
        if (!stp_capable) {
            return;
        }
        Object[] options = {"Land", "Cancel"};
        int choice = JOptionPane.showOptionDialog(gui,
                "Do you want to attack or land?",
                "",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[1]);
        if (choice == JOptionPane.NO_OPTION) {
            return;
        }
        game.setCurrentPlanetNr(planet.index);
        setWindow(C.S_PLANET_MAP);
        gui.setMenus(false);
        gui.setCurrentState(LAND1.get());

    }

    public static boolean checkForSpaceStacks(int x, int y, int owner, Square[][] galaxy_grid) {
        boolean is_stacks = false;
        int factions = 1;
        if (owner >= C.LEAGUE) {
            factions = 3;
        }
        for (int i = 0; i < factions; i++) {
            if (!galaxy_grid[x][y].parent_planet.space_stacks[owner + i].isEmpty()) {
                is_stacks = true;
            }

        }

        return is_stacks;
    }

    public static void wheelOnSpaceMap(MouseWheelEvent e) {

        if (e.getWheelRotation() < 0) {
//            gui.setMenus(true);
            game.setSelectedPointFaction(null, -1, null, null);
//            JPanel main_windows = gui.getMainWindows();
//            CardLayout cl = (CardLayout) main_windows.getLayout();
//            cl.show(main_windows, C.S_PLANET_MAP);
            setWindow(C.S_PLANET_MAP);
            gui.setCurrentState(PW1.get());
        }
    }

    public static void wheelOnPlanetMap(MouseWheelEvent e) {

        if (e.getWheelRotation() > 0) {
            pressSpaceButtonSU();
        }

    }

    public static void pressSpaceButtonSU() {
//        gui.setMenus(false);
        game.setSelectedPointFaction(null, -1, null, null);
//            JPanel main_windows = gui.getMainWindows();
//            CardLayout cl = (CardLayout) main_windows.getLayout();
//            cl.show(main_windows, C.S_STAR_MAP);
        centerOnCurrentPlanet();
        setWindow(C.S_STAR_MAP);
        gui.setCurrentState(SW1.get());
    }

    public static void centerOnCurrentPlanet() {
        Planet planet = game.getPlanet(game.getCurrentPlanetNr());
        Point smo = Util.resolveSpaceMapOrigin(new Point(planet.x, planet.y), ws);
        game.setSpaceMapOrigin(smo);
    }

    public static void showCombatWindow() {
        setWindow(C.S_COMBAT_WINDOW);
        saveMainGameState();
        gui.setCurrentState(CW1.get());
    }

    public static void showUnitInfoWindow() {
        setWindow(C.S_UNIT_INFO);
        saveMainGameState();
        gui.setCurrentState(UIW1.get());
    }

    public static void restoreMainWindow() {
        if (main_game_state instanceof PW) {
            setWindow(C.S_PLANET_MAP);
        } else if (main_game_state instanceof SW) {
            setWindow(C.S_STAR_MAP);
        }
        gui.setMenus(true);
    }

    public static void setWindow(String window) {
        switch (window) {
            case C.S_PLANET_MAP:
            case C.S_STAR_MAP:
                gui.setMenus(true);
                break;
            default:
                gui.setMenus(false);
                break;
        }
        JPanel main_windows = gui.getMainWindows();
        CardLayout cl = (CardLayout) main_windows.getLayout();
        cl.show(main_windows, window);
    }

    public static boolean isOnStackDisplay(Point p) {

        boolean ret_val = false;

        int x = p.x;
        int y = p.y;

        if ((x >= ws.stack_display_x_offset && x <= ws.stack_display_x2
                && y >= ws.stack_display_y_offset && y <= ws.stack_display_y2)
                || (x >= ws.stack_display_x3 && x <= ws.stack_display_x4
                && y >= ws.stack_display_y3 && y <= ws.stack_display_y4)) {
            ret_val = true;
        }

        return ret_val;

    }

    /**
     * If on planet window a resource icon was clicked return resource type,
     * otherwise return -1.
     *
     * @param p
     * @return
     */
    public static int isOnResourceIcon(Point p) {
        int ret_val = -1;
        int x = p.x;
        int y = p.y;
        for (int i = 0; i < C.RES_TYPES; i++) {
            if (x >= ws.ri_x + i * ws.ri_x_gap && x <= ws.ri_x + ws.ri_w + i * ws.ri_x_gap
                    && y >= ws.ri_y && y <= ws.ri_y + ws.ri_h) {
                ret_val = i;
                break;
            }
        }
        return ret_val;
    }

    public static void clickOnResourceIcon(int resource) {
        gui.showResourceWindow(resource);
    }

    public static void clickOnStackDisplay(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            SU.clickOnStackDisplayButton1(e);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            // display stack window
//            gui.showStackWindow();
            SU.showUnitInfoWindow();

        } else if (e.getButton() == MouseEvent.BUTTON2) {
            gui.showStackMenu(e);
        }
    }

    public static void selectUnit(Unit unit, boolean select) {
        unit.selected = select;
        for (Unit unit1 : unit.cargo_list) {
            unit1.selected = select;
        }
    }

    public static void clickOnStackDisplayButton1(MouseEvent e) {
        Point p = game.getSelectedPoint();
        Point q = e.getPoint();
        int x = q.x;
        int y = q.y;

        int faction = game.getSelectedFaction();
        List<Unit> stack = null;
        if (faction == -1) {
            stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y).getStack();
        } else {
            Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
            stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[faction];
        }

        boolean is_cargo_listing = false;
        Iterator<Unit> iterator = stack.listIterator();
        Iterator<Unit> cargo_it = null;
        Unit u = iterator.next();
        for (int i = 0; i < 7; i++) {
            int cols = i == 6 ? 2 : 3;
            for (int j = 0; j < cols; j++) {

                int dx1 = ws.unit_icon_size * j + ws.stack_display_x_offset;
                int dy1 = ws.unit_icon_size * i + ws.stack_display_y_offset;
                int dx2 = ws.unit_icon_size * (j + 1) + ws.stack_display_x_offset;
                int dy2 = ws.unit_icon_size * (i + 1) + ws.stack_display_y_offset;

                if (x >= dx1 && x <= dx2 && y >= dy1 && y <= dy2) {
                    if (u.carrier == null) {
                        if (e.getClickCount() == 2) {
                            for (Unit unit : stack) {
//                                unit.selected = false;
                                selectUnit(unit, false);
                            }
//                            u.selected = true;
                            selectUnit(u, true);
                        } else {
//                            u.selected = !u.selected;
                            selectUnit(u, !u.selected);
                            System.out.println("u.selected = " + u.selected);

                            boolean is_selected = false;
                            for (Unit unit : stack) {
                                if (unit.selected) {
                                    is_selected = true;
                                }
                            }
                            if (!is_selected) {
//                                u.selected = true;
                                selectUnit(u, true);
                            }
                        }
                    }
                    LinkedList<Hex> path = game.getPath();
                    if (path != null) {
                        Point path_end = new Point(path.getLast().getX(), path.getLast().getY());
                        SU.findPath(path_end);
                        path = game.getPath();
                        if (path == null) {
                            gui.setCurrentState(PW2.get());
                        }
                    } else {
                        gui.getMainWindows().repaint();
                    }
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
    }

    public static void findPath(Point p) {
        Point sel = game.getSelectedPoint();

        PlanetGrid planet_grid = game.getPlanetGrid(game.getCurrentPlanetNr());

        Hex source = planet_grid.getHex(sel.x, sel.y);
        Hex destination = planet_grid.getHex(p.x, p.y);

        if (source.equals(destination)) {
            game.setPath(null);
            gui.getPlanetWindow().repaint();
            return;
        }

        int current_planet = game.getCurrentPlanetNr();
        double[][][] terr_cost = game.getTerrCost();
        int tile_set = game.getPlanet(current_planet).tile_set_type;
        List<Unit> stack = game.getPlanetGrid(current_planet).getHex(sel.x, sel.y).getStack();
        List<Unit> selected = new LinkedList<>();

        for (Unit unit : stack) {
            if (unit.selected) {
                selected.add(unit);
            }

        }

        long time = System.currentTimeMillis();
        PathFind.setMoveCosts(planet_grid, terr_cost, tile_set, Util.stackSize(selected), selected.get(0).owner, destination);
        System.out.println("time = " + (System.currentTimeMillis() - time));

        UnitType[][] unit_types = game.getUnitTypes();
        LinkedList<Hex> path = null;

        double min_max_path_cost = Double.POSITIVE_INFINITY;

        for (ListIterator<Unit> it = selected.listIterator(); it.hasNext();) {
            Unit e = it.next();

//        }
//        for (Unit e : selected) {
            int unit_type = e.type;
            int t_lvl = e.t_lvl;

            C.MoveType move_type = unit_types[unit_type][t_lvl].move_type;

            LinkedList<Hex> tmp_path = PathFind.shortestPath(planet_grid, source, destination, move_type.ordinal());
            if (tmp_path == null) {
                continue;
            }
            double max_path_cost = Double.NEGATIVE_INFINITY;
            int path_cost = 0;
            for (ListIterator<Unit> it2 = selected.listIterator(); it2.hasNext();) {
                Unit u = it2.next();
//            for (Unit u : selected) {

                int move_points_u = unit_types[u.type][u.t_lvl].move_pts;

                path_cost = PathFind.pathCost(tmp_path, u, game);
                if (path_cost == Integer.MAX_VALUE) {
                    break;
                }

                double tmp_cost = 1.0 * path_cost / move_points_u;

                if (tmp_cost > max_path_cost) {
                    max_path_cost = tmp_cost;
                }

            }

            if (path_cost != Integer.MAX_VALUE && min_max_path_cost > max_path_cost) {
                min_max_path_cost = max_path_cost;
                path = tmp_path;
            }

        }

        PathFind.printPath(path);

        game.setPath(path);

        gui.getPlanetWindow().repaint();
    }

    public static void pressBuildButtonSU() {
        BuildPanel bp = gui.getBuildPanel();
        Point p = game.getSelectedPoint();
        int planet = game.getCurrentPlanetNr();
        Structure s = game.getPlanetGrid(planet).getHex(p.x, p.y).getStructure();
        gui.showBuildWindow(null, planet, s);
//        bp.planetSelected(null, planet);
//        bp.citySelected(null, s);
    }

    public static void pressLaunchButtonSU() {
        if (game.launchStack()) {
            setWindow(C.S_STAR_MAP);
            Planet planet = game.getPlanet(game.getCurrentPlanetNr());
            Point smo = Util.resolveSpaceMapOrigin(new Point(planet.x, planet.y), ws);
            game.setSpaceMapOrigin(smo);
            gui.setCurrentState(SW2.get());
            System.out.println("gui = " + gui);
        } else {

            JOptionPane.showMessageDialog(gui, "Too many units in target area.", null, JOptionPane.PLAIN_MESSAGE);

        }

    }

    public static void pressEndTurnButtonSU() {
    }

    public static void pressSkipStackButtonSU() {
        LinkedList<Unit> pods = (LinkedList) game.getCargoPods();
        if (pods.isEmpty()) {
            JOptionPane.showMessageDialog(gui, "You have moved all of your units.", null, JOptionPane.PLAIN_MESSAGE);
            return;
        }
        Unit pod = pods.pop();
        Point p = new Point(pod.x, pod.y);
        System.out.println("p = " + p);
        System.out.println("pod x y " + pod.x + " " + pod.y);
        List<Unit> stack = null;
        int faction = -1;
        Point q = null;
        if (!pod.in_space) {
            stack = game.getPlanetGrid(pod.p_idx).getHex(p.x, p.y).getStack();
        } else {
            Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
            faction = pod.owner;
            q = game.resolveSpaceStack(p, faction);
            stack = galaxy_grid[q.x][q.y].parent_planet.space_stacks[faction];
            System.out.println("q = " + q);
            System.out.println("stack = " + stack);
        }
        for (Unit unit : stack) {
            unit.selected = false;
        }
        pod.selected = true;
        if (pod.in_space) {
            pod.carrier.selected = true;
//            p = q;
        }
        System.out.println("p = " + p);
        game.setSelectedPoint(p, faction);
        System.out.println(game.getSelectedPoint());
        game.setSelectedFaction(faction);
        game.setCurrentPlanetNr(pod.p_idx);
        centerMapOnUnit(pod);
    }

    public static void pressNextStackButtonSU() {
        Point p = game.getSelectedPoint();
        int faction = game.getSelectedFaction();
        int current_planet = game.getCurrentPlanetNr();

        List<Unit> unmoved_units = game.getUnmovedUnits();
        if (unmoved_units.isEmpty()) {
            JOptionPane.showMessageDialog(gui, "You have moved all of your units.", null, JOptionPane.PLAIN_MESSAGE);
            return;
        }
        if (p != null) {
            List<Unit> stack = null;
            if (faction == -1) {
                stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y).getStack();
            } else {
                Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
                stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[faction];
            }
            List<Unit> selected = new LinkedList<>();
            for (Unit unit : stack) {
                if (unit.selected) {
                    selected.add(unit);
                }
            }

            boolean is_cargo_listing = false;
            Iterator<Unit> iterator = selected.listIterator();
            Iterator<Unit> cargo_it = null;

            Unit e = iterator.next();
            for (int i = 0; i < selected.size(); i++) {

                unmoved_units.remove(e);
                if (is_cargo_listing) {
                    e = cargo_it.next();
                    if (!cargo_it.hasNext()) {
                        cargo_it = null;
                        is_cargo_listing = false;
                    }
                } else if (e.cargo_list.isEmpty()) {
                    if (iterator.hasNext()) {
                        e = iterator.next();
                    } else {
                        break;
                    }
                } else {
                    cargo_it = e.cargo_list.listIterator();
                    e = cargo_it.next();
                    if (cargo_it.hasNext()) {
                        is_cargo_listing = true;
                    }
                }

            }

        }

        if (unmoved_units.isEmpty()) {
            JOptionPane.showMessageDialog(gui, "You have moved all of your units.", null, JOptionPane.PLAIN_MESSAGE);
            return;
        }

        Unit unit = unmoved_units.get(0);
        System.out.println("unit = " + unit);
        int x = unit.x;
        System.out.println("x = " + x);
        int y = unit.y;
        System.out.println("y = " + y);
        Point point = new Point(x, y);
        faction = -1;
        if (unit.in_space) {
            System.out.println("unit.in_space = " + unit.in_space);
//            point = game.resolveSpaceStack(new Point(x, y), unit.prev_owner);
            faction = unit.owner;
            System.out.println("faction = " + faction);
        }
        game.setCurrentPlanetNr(unit.p_idx);
        System.out.println("unit.p_idx = " + unit.p_idx);
        game.setSelectedPointFaction(point, faction, null, null);
        game.setSelectedPoint(point, faction);
        game.setSelectedFaction(faction);
        String name = game.getPlanet(unit.p_idx).name;
        System.out.println("name = " + name);

        p = game.getSelectedPoint();
        System.out.println("p = " + p);
        List<Unit> stack = null;
        if (faction == -1) {
            stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y).getStack();
        } else {
            Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
            stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[faction];
            System.out.println("stack = " + stack);
        }

        boolean is_cargo_listing = false;
        Iterator<Unit> iterator = stack.listIterator();
        Iterator<Unit> cargo_it = null;

        Unit e = iterator.next();
        for (int i = 0; i < stack.size(); i++) {
            System.out.println("i = " + i);
            e.selected = true;
            System.out.println("e.owner = " + e.owner);
            if (is_cargo_listing) {
                e = cargo_it.next();
                if (!cargo_it.hasNext()) {
                    cargo_it = null;
                    is_cargo_listing = false;
                }
            } else if (e.cargo_list.isEmpty()) {
                if (iterator.hasNext()) {
                    e = iterator.next();
                } else {
                    break;
                }
            } else {
                cargo_it = e.cargo_list.listIterator();
                e = cargo_it.next();
                if (cargo_it.hasNext()) {
                    is_cargo_listing = true;
                }
            }

        }

//        if (unit.in_space) {
//            Point smo = Util.resolveSpaceMapOrigin(new Point(unit.x, unit.y), ws);
//            game.setSpaceMapOrigin(smo);
//            System.out.println(" Star map");
//            SU.setWindow(C.S_STAR_MAP);
//            gui.setCurrentState(SW2.get());
//        } else {
//            game.setMapOrigin(Util.resolvePlanetMapOrigin(new Point(unit.x - C.PLANET_MAP_ORIGIN_X_OFFSET,
//                    unit.y - C.PLANET_MAP_ORIGIN_Y_OFFSET)));
//            SU.setWindow(C.S_PLANET_MAP);
//            gui.setCurrentState(PW2.get());
//        }
        centerMapOnUnit(unit);
    }

    public static void selectNextUnmovedUnit() {

        List<Unit> unmoved_units = game.getUnmovedUnits();
        if (unmoved_units.isEmpty()) {
            JOptionPane.showMessageDialog(gui, "You have moved all of your units.", null, JOptionPane.PLAIN_MESSAGE);
            return;
        }

        Unit unit = unmoved_units.get(0);
        int x = unit.x;
        System.out.println("x = " + x);
        int y = unit.y;
        System.out.println("y = " + y);
        Point point = new Point(x, y);
        int faction = -1;
        if (unit.in_space) {
            System.out.println("unit.in_space = " + unit.in_space);
//            point = game.resolveSpaceStack(new Point(x, y), unit.prev_owner);
            faction = unit.owner;
            System.out.println("faction = " + faction);
        }
        game.setCurrentPlanetNr(unit.p_idx);
        System.out.println("unit.p_idx = " + unit.p_idx);
        game.setSelectedPointFaction(point, faction, null, null);
        game.setSelectedPoint(point, faction);
        game.setSelectedFaction(faction);
        String name = game.getPlanet(unit.p_idx).name;
        System.out.println("name = " + name);

        Point p = game.getSelectedPoint();
        System.out.println("p = " + p);
        List<Unit> stack = null;
        if (faction == -1) {
            stack = game.getPlanetGrid(game.getCurrentPlanetNr()).getHex(p.x, p.y).getStack();
        } else {
            Square[][] galaxy_grid = game.getGalaxyMap().getGalaxyGrid();
            stack = galaxy_grid[p.x][p.y].parent_planet.space_stacks[faction];
            System.out.println("stack = " + stack);
        }

        StackIterator iterator = new StackIterator(stack);
        Unit e = iterator.next();

        while (e != null) {
            e.selected = true;
            e = iterator.next();
        }
//        boolean is_cargo_listing = false;
//        Iterator<Unit> iterator = stack.listIterator();
//        Iterator<Unit> cargo_it = null;
//
//        Unit e = iterator.next();
//        for (int i = 0; i < C.STACK_SIZE; i++) {
//            System.out.println("i = " + i);
//            e.selected = true;
//            if (is_cargo_listing) {
//                e = cargo_it.next();
//                if (!cargo_it.hasNext()) {
//                    cargo_it = null;
//                    is_cargo_listing = false;
//                }
//            } else if (e.cargo_list.isEmpty()) {
//                if (iterator.hasNext()) {
//                    e = iterator.next();
//                } else {
//                    break;
//                }
//            } else {
//                cargo_it = e.cargo_list.listIterator();
//                e = cargo_it.next();
//                if (cargo_it.hasNext()) {
//                    is_cargo_listing = true;
//                }
//            }
//
//        }

//        if (unit.in_space) {
//            Point smo = Util.resolveSpaceMapOrigin(new Point(unit.x, unit.y), ws);
//            game.setSpaceMapOrigin(smo);
//            System.out.println(" Star map");
//            SU.setWindow(C.S_STAR_MAP);
//            gui.setCurrentState(SW2.get());
//        } else {
//            game.setMapOrigin(Util.resolvePlanetMapOrigin(new Point(unit.x - C.PLANET_MAP_ORIGIN_X_OFFSET,
//                    unit.y - C.PLANET_MAP_ORIGIN_Y_OFFSET)));
//            SU.setWindow(C.S_PLANET_MAP);
//            gui.setCurrentState(PW2.get());
//        }
        centerMapOnUnit(unit);
    }

    public static void centerMapOnUnit(Unit unit) {
        if (unit.in_space) {
            Point smo = Util.resolveSpaceMapOrigin(new Point(unit.x, unit.y), ws);
            game.setSpaceMapOrigin(smo);
            System.out.println(" Star map");
            SU.setWindow(C.S_STAR_MAP);
            gui.setCurrentState(SW2.get());
        } else {
            game.setMapOrigin(Util.resolvePlanetMapOrigin(new Point(unit.x - C.PLANET_MAP_ORIGIN_X_OFFSET,
                    unit.y - C.PLANET_MAP_ORIGIN_Y_OFFSET)));
            SU.setWindow(C.S_PLANET_MAP);
            gui.setCurrentState(PW2.get());
        }
    }

}
