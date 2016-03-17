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
package gui;

import game.Contract;
import game.Game;
import game.Message;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import util.C;
import util.FN;
import util.G;
import util.G.CDW;
import util.Util;
import util.UtilG;
import util.WindowSize;

/**
 * Window for viewing, and responding to, received contracts.
 *
 * @author joulupunikki
 */
public class ResolveContract extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;
    private JButton exit;
    private JButton reject;
    private JButton accept;

    // pointer to map holding gui element coordinates
    private Map<Enum, Integer> c;
    private Map<Enum, Integer> c2;
    private JDialog dialog;
    // the message with the contract as attachment
    private Message message;
    private Contract contract;
    private ResolveContract() {
    }

    private ResolveContract(Gui gui, JDialog dialog) {
        this.dialog = dialog;
        this.gui = gui;
        ws = Gui.getWindowSize();
        c = ws.diplomacy_window;
        c2 = ws.house;
        game = gui.getGame();
        setUpWindow();
        dialog.setUndecorated(true);
        dialog.add(this);
    }

    public static ResolveContract getWindow(Gui gui) {
        JDialog dialog = new JDialog(gui);
        Map<Enum, Integer> c = Gui.getWindowSize().diplomacy_window;
        dialog.setBounds(gui.getX(), c.get(CDW.R_WIN_Y) + gui.getY(), Gui.getWindowSize().main_window_width, c.get(CDW.R_WIN_H));
        dialog.setModal(true);
        ResolveContract w = new ResolveContract(gui, dialog);
        w.setLayout(null);
        return w;
    }

    public void enterDialog(Message msg) {
        message = msg;
        contract = message.getContract();
        accept.setEnabled(true);
        reject.setEnabled(true);
        if (contract.isResolved()) {
            accept.setEnabled(false); // fix #68
            reject.setEnabled(false);
        }
        dialog.setVisible(true);
    }

    public void setWindowVisiblity(boolean visible) {
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

    private void setUpWindow() {
        setUpButtons();
    }

    private void setUpButtons() {
        exit = new JButton("Exit");
        exit.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
        exit.setBackground(Color.BLACK);
        exit.setForeground(C.COLOR_GOLD);
        exit.setBounds(c.get(G.CDW.R_ACCEPT_X) + 2 * c.get(G.CDW.R_BUTTON_W), c.get(G.CDW.R_ACCEPT_Y), c.get(G.CDW.R_ACCEPT_W), c.get(G.CDW.R_ACCEPT_H));
        exit.setEnabled(true);
        final ResolveContract self = this;
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                self.setWindowVisiblity(false);
            }
        });
        this.add(exit);

        accept = new JButton("Accept");
        accept.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
        accept.setBackground(Color.BLACK);
        accept.setForeground(C.COLOR_GOLD);
        accept.setBounds(c.get(G.CDW.R_ACCEPT_X), c.get(G.CDW.R_ACCEPT_Y), c.get(G.CDW.R_ACCEPT_W), c.get(G.CDW.R_ACCEPT_H));
        accept.setEnabled(true);
        accept.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tryToAccept(gui, contract, game);
                self.setWindowVisiblity(false);
            }

        });
        this.add(accept);

        reject = new JButton("Reject");
        reject.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
        reject.setBackground(Color.BLACK);
        reject.setForeground(C.COLOR_GOLD);
        reject.setBounds(c.get(G.CDW.R_ACCEPT_X) + c.get(G.CDW.R_BUTTON_W), c.get(G.CDW.R_ACCEPT_Y), c.get(G.CDW.R_ACCEPT_W), c.get(G.CDW.R_ACCEPT_H));
        reject.setEnabled(true);
        reject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reject(contract, game);
                self.setWindowVisiblity(false);
            }

        });
        this.add(reject);
    }

    public static boolean tryToAccept(Gui gui, Contract contract, Game game) {
        if (contract.acceptCheck(game)) {
            contract.acceptDo(game);
        } else {
            if (gui != null) {
                gui.showInfoWindow("My Lord, we cannot satisfy all the terms of the Contract!");
            }
            return false;
        }
        Message response_message = new Message(""
                + Util.factionNameDisplay(game.getTurn())
                + " sent Us the following message: \"We accept the offer.\"", C.Msg.RESPONSE, game.getYear(), null);
        response_message.setContract(contract);
        game.getFaction(contract.getSender()).addMessage(response_message);
        return true;
    }

    public static void reject(Contract contract, Game game) {
        contract.reject();
        Message message = new Message(""
                + Util.factionNameDisplay(game.getTurn())
                + " sent Us the following message: \"We reject the offer.\"", C.Msg.RESPONSE, game.getYear(), null);
        message.setContract(contract);
        game.getFaction(contract.getSender()).addMessage(message);
    }

    private void renderWindow(Graphics g) {
        drawBackground(g);
        drawDetails(g);

    }

    private void drawDetails(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
        drawContractHeaders(g);
        DiplomacyWindow.drawContract(game, g, message.getContract(), ws, game.getTurn());
        drawLeader(g);
        if (message.getType() == C.Msg.RESPONSE) {
            drawRespose(g);
        }
    }

    private void drawBackground(Graphics g) {
        byte[][] pallette = gui.getPallette();
        BufferedImage bi = Util.loadImage(FN.S_CONTRACT_PCX, ws.is_double, pallette, 640, 480);
        Graphics2D g2d = (Graphics2D) g;
//TODO draw leader portrait
//        WritableRaster wr = bi.getRaster();
//        int[] pixel_data = new int[1];

        g2d.drawImage(bi, null, 0, 0);
        UtilG.drawFrameRectIn(g, 0, 0, ws.main_window_width, c.get(CDW.R_WIN_H));
    }

    private void drawContractHeaders(Graphics2D g) {
        String s = "If You Will ...";
        int x = UtilG.center(g, c.get(CDW.GIVE_X), c.get(CDW.GIVE_W), ws.font_large, s);
        UtilG.drawStringGrad(g, s, ws.font_large, x, +c.get(CDW.GIVE_H_Y));
        s = "Then We Will ...";
        x = UtilG.center(g, c.get(CDW.GIVE_X), c.get(CDW.GIVE_W), ws.font_large, s);
        UtilG.drawStringGrad(g, s, ws.font_large, x, +c.get(CDW.GIVE_H_Y) - c.get(CDW.GIVE_Y) + c.get(CDW.TAKE_Y));
    }

    private void drawLeader(Graphics2D g) {
        int message_sender = contract.getSender();
        if (message.getType() == C.Msg.RESPONSE) {
            message_sender = contract.getReceiver();
        }
        String s = Util.factionNameDisplay(message_sender);
        int x = UtilG.center(g, c2.get(G.CH.LEADER_H_X), c2.get(G.CH.LEADER_H_W), ws.font_large, s);
        UtilG.drawStringGrad(g, s, ws.font_large, x, c2.get(G.CH.LEADER_H_Y));
    }

    private void drawRespose(Graphics2D g) {
        UtilG.drawStringGrad(g, message.getMsgTxt(), ws.font_large, c.get(CDW.R_RESPONCE_X), c.get(CDW.R_RESPONCE_Y));
    }
}
