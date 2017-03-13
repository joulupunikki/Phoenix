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
package gui;

import dat.ResType;
import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Game;
import game.Hex;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import util.C;
import util.FN;
import util.G;
import util.Util;
import util.UtilG;
import util.WindowSize;

/**
 * Provides opportunity to auto buy missing resources from League during unit
 * building.
 *
 * @author joulupunikki joulupunikki@gmail.communist.invalid
 */
public class AgoraAutobuyPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;
    private JButton yes;
    private JButton no_ok;
    // pointer to map holding gui element coordinates
    private Map<Enum, Integer> c;

    private JDialog dialog;
    private int height;
    private int res_rows;
    private int type;
    private int t_lvl;
    private int fb_required;
    private int[] res_list;
    private boolean[] response;
    private STATUS status;
    private BufferedImage bi;
    private List<Unit> agora_stack;
    private AgoraAutobuyPanel() {
    }

    private AgoraAutobuyPanel(Gui gui, JDialog dialog) {
        this.dialog = dialog;
        this.gui = gui;
        ws = Gui.getWindowSize();
        c = ws.agora_auto_buy;
        game = gui.getGame();
        setUpWindow();
        dialog.setUndecorated(true);
        dialog.add(this);
    }

    public static AgoraAutobuyPanel getWindow(Gui gui) {
        JDialog dialog = new JDialog(gui, true);
        dialog.setModal(true);
        AgoraAutobuyPanel w = new AgoraAutobuyPanel(gui, dialog);
        w.setLayout(null);
        return w;
    }

    public void setWindowVisiblity(boolean visible) {
        dialog.setBounds(c.get(G.SS.WIN_X) + gui.getX(), c.get(G.SS.WIN_Y) + gui.getY(), c.get(G.SS.WIN_W), c.get(G.SS.WIN_H));
        dialog.setVisible(visible);
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderWindow(g);
    }

    private boolean checkResAvail() {
        for (int i = 0; i < res_list.length; i++) {
            if (res_list[i] <= 0) {
                continue;
            }
            if (!checkSingleRes(i)) {
                return false;
            }

        }
        return true;
    }

    private boolean checkSingleRes(int res_nr) {
        for (Unit unit : agora_stack) {
            if (unit.type == C.CARGO_UNIT_TYPE && unit.res_relic == res_nr && unit.amount >= res_list[res_nr]) {
                return true;
            }
        }
        return false;
    }

    private boolean tryToBuy() {
        int p_idx = agora_stack.get(0).p_idx;
        int[] sells = game.getDiplomacy().getAgora_prices();
        Hex h = game.getPlanetGrid(p_idx).getHex(agora_stack.get(0).x, agora_stack.get(0).y);
        Set<Hex> hexes = Util.getHexesWithinRadiusOf(h, 1, null);
        hexes.remove(h);
        h = null;
        List<Integer> manifest = new LinkedList<>();
        for (int i = 0; i < res_list.length; i++) {
            if (res_list[i] > 0) {
                manifest.add(i);
            }
        }
        for (Hex hex : hexes) {
            Structure struct = hex.getStructure();
            List<Unit> stack = hex.getStack();
            if ((struct == null || struct.owner == game.getTurn())
                    && (stack.isEmpty() || (stack.get(0).owner == game.getTurn())
                    && stack.size() + manifest.size() <= C.STACK_SIZE)) {
                h = hex;
                break;
            }
        }
        if (h == null) {
            gui.showInfoWindow("We cannot sell resources to you because there is nowhere to put it. ("
                    + "At least one hex next to the Agora must have enough room for as many new pods "
                    + "as the number of different resources you bought.)");
            return false;
        }
        for (int i = 0; i < res_list.length; i++) {
            if (res_list[i] > 0) {
                for (Unit u : agora_stack) {
                    if (u.type == C.CARGO_UNIT_TYPE && u.res_relic == i) {
                        //u.amount -= amounts[i];
                        game.getResources().adjustPodResources(u, -res_list[i]);
                        game.getFaction(game.getTurn()).addFirebirds(res_list[i] * sells[i] * (-1));
                        game.getResources().addOneResourceTypeToHex(p_idx, h.getX(), h.getY(), game.getTurn(), game.getTurn(), i, res_list[i]);
                    }

                }
            }
        }
        for (Iterator<Unit> iterator = agora_stack.iterator(); iterator.hasNext();) {
            Unit next = iterator.next();

            if (next.type == C.CARGO_UNIT_TYPE && next.amount < 1) {
                iterator.remove();
                game.deleteUnitNotInCombat(next);
            }
        }
        return true;
    }

    private void setButtons() {
        yes.setBounds(c.get(G.AAB.BUTTON_X), c.get(G.AAB.BUTTON_Y) + (res_rows - 1) * c.get(G.AAB.ROW_H), c.get(G.AAB.BUTTON_W), c.get(G.AAB.BUTTON_H));
        no_ok.setBounds(c.get(G.AAB.BUTTON_X2), c.get(G.AAB.BUTTON_Y) + (res_rows - 1) * c.get(G.AAB.ROW_H), c.get(G.AAB.BUTTON_W), c.get(G.AAB.BUTTON_H));
        if (status.equals(STATUS.CAN_BUY)) {
            yes.setVisible(true);
            no_ok.setText("No");
        } else {
            yes.setVisible(false);
            no_ok.setText("OK");
        }
    }

    private enum STATUS {
        CAN_BUY,
        NOT_ENUF_FB,
        NOT_ENUF_RES
    }

    public void enterDialog(int[] res_list, int planet, int type, int t_lvl, boolean[] response) {
        // try to find a League agora and see if they are willing to sell
        Util.HexIter hex_iter = Util.getHexIter(game, planet);
        agora_stack = null;
        for(Hex hex = hex_iter.next(); hex != null; hex = hex_iter.next()) {
            Structure s = hex.getStructure();
            if (hex.isSpotted(game.getTurn()) && s != null && s.owner == C.LEAGUE && s.type == C.AGORA) {
                agora_stack = hex.getStack();
                break;
            }
        }
        if (agora_stack == null) {
            response[0] = false;
            gui.showInfoWindow("You do not have the required resources to start producing "
                    + game.getUnitTypes()[type][t_lvl].name
                    + ". Normally you could purchase those resources from the League, "
                    + "but you have either not found the Leaguehall or there isn't one on this planet.");
            return;
        } else if (game.getDiplomacy().getDiplomaticState(game.getTurn(), C.LEAGUE) == C.DS_WAR) {
            response[0] = false;
            gui.showInfoWindow("You do not have the required resources to start producing "
                    + game.getUnitTypes()[type][t_lvl].name
                    + ". Normally you could purchase those resources from the League, but they have "
                    + "imposed a trade embargo on you.");
            return;
        }
        // do you have enough money, do they have enough goods
        bi = Util.loadImage(FN.S_BG0_PCX, ws.is_double, gui.getPallette(), 640, 480);
        this.response = response;
        this.type = type;
        this.t_lvl = t_lvl;
        this.res_list = res_list;
        int res_count = 0;
        fb_required = 0;
        int[] prices = game.getDiplomacy().getAgora_prices();
        for (int i = 0; i < res_list.length; i++) {
            if (res_list[i] > 0) {
                res_count++;
                fb_required += res_list[i] * prices[i];
            }
        }
        if (fb_required > game.getFaction(game.getTurn()).getFirebirds()) {
            status = STATUS.NOT_ENUF_FB;
        } else if (checkResAvail()) {
            status = STATUS.CAN_BUY;
        } else {
            status = STATUS.NOT_ENUF_RES;
        }
        res_rows = (res_count - 1) / 2 + 1;
        height = c.get(G.AAB.WIN_H) + (res_rows - 1) * c.get(G.AAB.ROW_H);
        System.out.println(res_rows + " " + height);
        int win_y = (ws.main_window_height - c.get(G.AAB.WIN_H) - res_rows * c.get(G.AAB.ROW_H)) / 2 + gui.getY();
        setButtons();
        dialog.setBounds(c.get(G.AAB.WIN_X) + gui.getX(), win_y, c.get(G.AAB.WIN_W), height);
        dialog.setVisible(true);
    }

    private void setUpWindow() {
        yes = setUpButton("Yes", G.AAB.BUTTON_X);
        no_ok = setUpButton("No", G.AAB.BUTTON_X2);
    }

    private JButton setUpButton(String s, G.AAB button) {
        JButton b = new JButton();
        b.setText(s);
        b.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
        b.setBackground(Color.BLACK);
        b.setForeground(C.COLOR_GOLD);

        b.setEnabled(true);
        final AgoraAutobuyPanel self = this;
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                self.selection(button);
            }
        });
        this.add(b);
        return b;
    }

    private void renderWindow(Graphics g) {
        drawBackground(g);
        drawDetails(g);
    }

    private void drawBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        drawResListImages(g2d);
        g2d.drawImage(bi, null, 0, 0);
        UtilG.drawFrameRectIn(g, 0, 0, c.get(G.AAB.WIN_W), height);
    }

    private void drawDetails(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
        drawTexts(g);
        drawResListTexts(g);
    }

    private void drawTexts(Graphics2D g) {
        String[] s = { "You do not have the required resources to start",
            "producing " + game.getUnitTypes()[type][t_lvl].name + ".",
            "You have " + game.getFaction(game.getTurn()).getFirebirds() + " Firebirds and you need:",
            "For a total of " + fb_required + " Firebirds."
        };
        int[] row_y = {
            c.get(G.AAB.HEADER_Y),
            c.get(G.AAB.HEADER_Y2),
            c.get(G.AAB.HEADER_Y3),
            c.get(G.AAB.HEADER_Y4) + (res_rows - 1) * c.get(G.AAB.ROW_H),};
        for (int i = 0; i < s.length; i++) {
            int x = UtilG.center(g, 0, c.get(G.AAB.WIN_W), ws.font_large, s[i]);
            UtilG.drawStringGrad(g, s[i], ws.font_large, x, row_y[i]);
            
        }
        String s2 = "";
        switch (status) {
            case CAN_BUY:
                s2 = "Do you wish to purchase these resources and start producing?";
                break;
            case NOT_ENUF_FB:
                s2 = "You don't have enough Firebirds to purchase the resources.";
                break;
            case NOT_ENUF_RES:
                s2 = "The League doesn't have enough of the resources to sell to you.";
                break;
            default:
                throw new AssertionError();
        }
        UtilG.drawStringGrad(g, s2, ws.font_large, UtilG.center(g, 0, c.get(G.AAB.WIN_W), ws.font_large, s2), c.get(G.AAB.HEADER_Y5) + (res_rows - 1) * c.get(G.AAB.ROW_H));
    }

    private void selection(G.AAB button) {
        switch (button) {
            case BUTTON_X:
                if (tryToBuy()) {
                    response[0] = true;
                } else {
                    response[0] = false;
                }
                
                break;
            case BUTTON_X2:
                response[0] = false;
                break;
            default:
                throw new AssertionError();
        }
        dialog.setVisible(false);

    }

    private void drawResListTexts(Graphics2D g) {
        ResType[] res_types = game.getResTypes();
        int[] res_prices = game.getDiplomacy().getAgora_prices();
        int count = 0;
        for (int i = 0; i < res_list.length; i++) {
            if (res_list[i] <= 0) {
                continue;
            }

            int row = count++ / 2;
            String[] s = {
                res_list[i] + " " + res_types[i].name + " at",
                (res_list[i] * res_prices[i]) + " Firebirds"
            };
            int x_text = c.get(G.AAB.COL_TEXT_X);
            if (count % 2 == 0) {
                x_text = c.get(G.AAB.COL_TEXT_X2);
            }
            UtilG.drawStringGradRes(g, s[0], ws.font_large, x_text, c.get(G.AAB.ROW_TEXT_Y) + row * c.get(G.AAB.ROW_H));
            UtilG.drawStringGradRes(g, s[1], ws.font_large, x_text, c.get(G.AAB.ROW_TEXT_Y2) + row * c.get(G.AAB.ROW_H));
        }
    }

    private void drawResListImages(Graphics2D g) {
        int count = 0;
        for (int i = 0; i < res_list.length; i++) {
            if (res_list[i] <= 0) {
                continue;
            }

            int row = count++ / 2;
            
            int x_img = c.get(G.AAB.COL_IMG_X);
            if (count % 2 == 0) {
                x_img = c.get(G.AAB.COL_IMG_X2);
            }
            drawResourceIcon(bi.getRaster(), i, x_img, c.get(G.AAB.ROW_IMG_Y) + row * c.get(G.AAB.ROW_H));
        }
    }

    private void drawResourceIcon(WritableRaster wr, int res_nr, int x, int y) {
        int[][] res_icons = gui.getResources().getResIcons();
        int[] pixel_data = new int[1];
        int w = C.CARGO_WIDTH;
        int h = C.CARGO_HEIGHT;
        Util.writeImage(pixel_data, res_nr, res_icons,
                wr, ws, w, h, x, y);

    }
}
