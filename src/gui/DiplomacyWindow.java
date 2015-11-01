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
import game.Contract.Term;
import game.Game;
import game.Message;
import game.Regency;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import util.C;
import util.FN;
import util.G;
import util.G.CDW;
import util.G.CH;
import util.Util;
import util.UtilG;
import util.UtilG.DarkSliderUI;
import util.WindowSize;

/**
 * Diplomacy Window Gui, where you can formulate and send a diplomatic agreement
 * to a previously selected faction.
 *
 * @author joulupunikki
 */
public class DiplomacyWindow extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int MAX_MONEY_DEMAND = 50000;


    public enum IfYouWill {

        PEACE("Sign A Peace Treaty With Us", null, null),
        MONEY("Compensate Us With ", "Firebirds", null),
        VOTES("Give Us all Your votes for the next election", null, null),
        MINISTRY("Give Us ", "If You are Elected", "Ministry");
//        RESOURCES("Compensate Us With ", "Resources"),
//        UNITS("Compensate Us With ", "Units"),
//        CITIES("Compensate Us With ", "Cities");

        private final String text;
        private final String text2;
        private final String text3;

        private IfYouWill(String text, String text2, String text3) {
            this.text = text;
            this.text2 = text2;
            this.text3 = text3;
        }
    }

    public enum ThenWeWill {

        PEACE("Sign A Peace Treaty With You", null, null),
        MONEY("Compensate You With ", "Firebirds", null),
        VOTES("Give You all Our votes for the next election", null, null),
        MINISTRY("Give You ", "If We are Elected", "Ministry");
//        RESOURCES("Compensate You With ", "Resources"),
//        UNITS("Compensate You With ", "Units"),
//        CITIES("Compensate You With ", "Cities");

        private final String text;
        private final String text2;
        private final String text3;

        private ThenWeWill(String text, String text2, String text3) {
            this.text = text;
            this.text2 = text2;
            this.text3 = text3;
        }

    }

    // pointer to GUI
    private Gui gui;
    private Game game;
    private WindowSize ws;
    private JButton exit;
    private JButton cancel;
    private JPopupMenu if_you_menu;
    private JPopupMenu then_we_menu;
    private DetailDialog detail_dialog;

    private JMenuItem[] if_you_items;
    private JMenuItem[] then_we_items;

    private int faction;
    private List<Integer> non_promised_ministries;
    private final List<Integer> all_ministries;
    private Map<Enum, Integer> c;
    private Map<Enum, Integer> c2;

    private Contract contract;
    private DiplomacyWindow() {
        all_ministries = null; // needed because final
    }

    private DiplomacyWindow(Gui gui) {
        this.gui = gui;
        ws = Gui.getWindowSize();
        c = ws.diplomacy_window;
        c2 = ws.house;
        game = gui.getGame();
        setUpWindow();
        detail_dialog = new DetailDialog(gui);
        non_promised_ministries = new LinkedList<>();
        all_ministries = new LinkedList<>();
        all_ministries.add(C.FLEET);
        all_ministries.add(C.THE_SPY);
        all_ministries.add(C.STIGMATA);

    }

    public static DiplomacyWindow getWindow(Gui gui) {
        DiplomacyWindow w = new DiplomacyWindow(gui);
        w.setLayout(null);
        w.setPreferredSize(new Dimension(Gui.getWindowSize().main_window_width,
                Gui.getWindowSize().main_window_height));
        return w;
    }

    public void enterWindow(int faction) {
        //System.out.println("enterWindow() start : " + contract);
        if (contract == null) {
            contract = new Contract();
        }
        clear();

        //System.out.println("enterWindow() end : " + contract);
        this.faction = faction;
    }

    public void showIfYouMenu(Point p) {        
        if_you_menu.show(this, c.get(G.CDW.GIVE_X), c.get(G.CDW.GIVE_Y));
    }

    public void showThenWeMenu(Point p) {
        then_we_menu.show(this, c.get(G.CDW.GIVE_X), c.get(G.CDW.TAKE_Y));
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
//        give_menu.setBounds(c.get(G.CDW.GIVE_X), c.get(G.CDW.GIVE_Y), c.get(G.CDW.GIVE_W), c.get(G.CDW.GIVE_H));
        setUpIfYouMenu();
        setUpThenWeMenu();
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                gui.getCurrentState().clickOnWindow(e);
            }

        });
    }

    public void doClick(MouseEvent e) {
        Point p = e.getPoint();
        List<Contract.Term> terms = contract.getTerms();
        if (c.get(G.CDW.GIVE_X) <= p.x && p.x <= c.get(G.CDW.GIVE_X) + c.get(G.CDW.GIVE_W)
                && c.get(G.CDW.GIVE_Y) <= p.y && p.y <= c.get(G.CDW.GIVE_Y) + c.get(G.CDW.GIVE_H)) {
            int term_count = 0;
            for (Contract.Term term : terms) {
                if_you_items[term.getType().ordinal()].setEnabled(false);
                term_count++;
            }
            if (term_count < 3) {
                showIfYouMenu(p);
            }
        } else if (c.get(G.CDW.GIVE_X) <= p.x && p.x <= c.get(G.CDW.GIVE_X) + c.get(G.CDW.GIVE_W)
                && c.get(G.CDW.TAKE_Y) <= p.y && p.y <= c.get(G.CDW.TAKE_Y) + c.get(G.CDW.GIVE_H)) {
            int term_count = 0;
            for (Contract.Term term : terms) {
                then_we_items[term.getType().ordinal()].setEnabled(false);
                term_count++;
            }
            if (term_count < 3) {
                showThenWeMenu(p);
            }
        }
    }

    public void clear() {
        contract.clear();
        for (int i = 0; i < if_you_items.length; i++) {
            if_you_items[i].setEnabled(true);
        }
        for (int i = 0; i < then_we_items.length; i++) {
            then_we_items[i].setEnabled(true);
        }
        if (game.getDiplomacy().getDiplomaticState(game.getTurn(), faction) != C.DS_WAR) {
            if_you_items[IfYouWill.PEACE.ordinal()].setEnabled(false);
            then_we_items[ThenWeWill.PEACE.ordinal()].setEnabled(false);
        }
        non_promised_ministries.clear();
        non_promised_ministries.addAll(all_ministries);

        for (Contract con : game.getDiplomacy().getSentContracts()) {
            for (Term term : con.getTerms()) {
                if (term.getDonor() == game.getTurn()) {
                    switch (term.getType()) {
                        case VOTES:
                            System.out.println("DBG no votes");
                            then_we_items[ThenWeWill.VOTES.ordinal()].setEnabled(false);
                            break;
                        case MINISTRY:
                            non_promised_ministries.remove(new Integer(term.getAmount()));
                            break;

                        default:
                            throw new AssertionError();
                    }
                }
            }
        }
        if (game.getRegency().getVotes()[game.getTurn()][Regency.CANDIDATE_IDX] > -1) {
            then_we_items[ThenWeWill.VOTES.ordinal()].setEnabled(false);
        }

        int[] promises = game.getDiplomacy().getMinistryPromises(game.getTurn());
        for (int i = 0; i < promises.length; i++) {
            if (promises[i] > -1) {
                non_promised_ministries.remove(new Integer(promises[i]));
            }
        }
        if (non_promised_ministries.isEmpty()) {
            then_we_items[ThenWeWill.MINISTRY.ordinal()].setEnabled(false);
        }
    }

//    public void done() {
//        //System.out.println("contract in done() " + contract);
//        if (contract.getTerms().isEmpty()) {
//            return;
//        }
//        Message msg = new Message(null, C.Msg.CONTRACT, game.getYear(), null);
//        game.getDiplomacy().addSentContract(contract);
//        msg.setContract(contract);
//        contract = null;
//    }

    private void setUpButtons() {
        setUpDone();
        setUpClear();
    }

    private void setUpClear() {
        cancel = new JButton("Clear");
        cancel.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
        cancel.setBackground(Color.BLACK);
        cancel.setForeground(C.COLOR_GOLD);
        cancel.setBounds(ws.fw_eb_x - (int) (ws.fw_eb_w * 1.2), ws.fw_eb_y, ws.fw_eb_w, ws.fw_eb_h);
        cancel.setEnabled(true);
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.getCurrentState().pressCancelButton();
            }
        });
        this.add(cancel);
    }

    private void setUpDone() {
        exit = new JButton("Done");
        exit.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
        exit.setBackground(Color.BLACK);
        exit.setForeground(C.COLOR_GOLD);
        exit.setBounds(ws.fw_eb_x, ws.fw_eb_y, ws.fw_eb_w, ws.fw_eb_h);
        exit.setEnabled(true);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Term> terms = contract.getTerms();
                if (!terms.isEmpty()) {
                    int receiver = terms.get(0).getDonor();
                    if (receiver == game.getTurn()) {
                        receiver = terms.get(0).getRecipient();
                                
                    }
                    contract.setSender(game.getTurn());
                    Message msg = new Message(null, C.Msg.CONTRACT, game.getYear(), null);
                    msg.setContract(contract);
                    game.getDiplomacy().addSentContract(contract);
                    contract = null;
                    game.getFaction(receiver).addMessage(msg);
                }
                gui.getCurrentState().pressExitButton();
            }
        });
        this.add(exit);
    }

    private void renderWindow(Graphics g) {
        drawBackground(g);
        drawDetails(g);
    }
    
    private void drawBackground(Graphics g) {
        byte[][] pallette = gui.getPallette();
        BufferedImage bi = Util.loadImage(FN.S_DIPLOMA_PCX, ws.is_double, pallette, 640, 480);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(bi, null, 0, 0);
    }

    private void drawDetails(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
        drawLeader(g);
        drawContract(g, contract, ws, faction);
        drawContractHeaders(g);

    }

    private void drawContractHeaders(Graphics2D g) {
        String s = "If You Will ...";
        int x = UtilG.center(g, c.get(CDW.GIVE_X), c.get(CDW.GIVE_W), ws.font_large, s);
        UtilG.drawStringGrad(g, s, ws.font_large, x, +c.get(CDW.GIVE_H_Y));
        s = "Then We Will ...";
        x = UtilG.center(g, c.get(CDW.GIVE_X), c.get(CDW.GIVE_W), ws.font_large, s);
        UtilG.drawStringGrad(g, s, ws.font_large, x, +c.get(CDW.GIVE_H_Y) - c.get(CDW.GIVE_Y) + c.get(CDW.TAKE_Y));
    }

    static void drawContract(Graphics2D g, Contract contract, WindowSize ws, int other_party) throws AssertionError {
        Map<Enum, Integer> c = ws.diplomacy_window;
        List<Term> terms = contract.getTerms();
        int count_a = 0;
        int count_b = 0;
        for (Term term : terms) {
            String s;
            if (term.getDonor() == other_party) {
                switch (term.getType()) {
                    case STATE:
                        s = IfYouWill.PEACE.text;
                        break;
                    case MONEY:
                        s = IfYouWill.MONEY.text + term.getAmount() + " " + IfYouWill.MONEY.text2;
                        break;
                    case VOTES:
                        s = IfYouWill.VOTES.text;
                        break;
                    case MINISTRY:
                        s = IfYouWill.MINISTRY.text + Util.getFactionName(term.getAmount()) + " " + IfYouWill.MINISTRY.text2;
                        break;

                    default:
                        throw new AssertionError();
                }
                UtilG.drawStringGrad(g, s, ws.font_large, c.get(CDW.GIVE_X),
                        UtilG.centerY(g, c.get(CDW.GIVE_Y) + c.get(CDW.GIVE_H),
                                c.get(CDW.GIVE_H), ws.font_large) + count_a * c.get(CDW.ROW_H));
                count_a++;
            } else {
                switch (term.getType()) {
                    case STATE:
                        s = ThenWeWill.PEACE.text;
                        break;
                    case MONEY:
                        s = ThenWeWill.MONEY.text + term.getAmount() + " " + ThenWeWill.MONEY.text2;
                        break;
                    case VOTES:
                        s = ThenWeWill.VOTES.text;
                        break;
                    case MINISTRY:
                        s = ThenWeWill.MINISTRY.text + Util.getFactionName(term.getAmount()) + " " + ThenWeWill.MINISTRY.text2;
                        break;
                    default:
                        throw new AssertionError();
                }
                UtilG.drawStringGrad(g, s, ws.font_large, c.get(CDW.GIVE_X),
                        UtilG.centerY(g, c.get(CDW.TAKE_Y) + c.get(CDW.GIVE_H),
                                c.get(CDW.GIVE_H), ws.font_large) + count_b * c.get(CDW.ROW_H));
                count_b++;
            }
        }
    }

    private void drawLeader(Graphics2D g) {
        String s = Util.factionNameDisplay(faction);
        int x = UtilG.center(g, c2.get(CH.LEADER_H_X), c2.get(CH.LEADER_H_W), ws.font_large, s);
        UtilG.drawStringGrad(g, s, ws.font_large, x, c2.get(CH.LEADER_H_Y));
    }

    private void setUpIfYouMenu() {
        if_you_menu = new JPopupMenu("Give Me ...");
        IfYouWill[] if_you_options = IfYouWill.values();
        if_you_items = new JMenuItem[if_you_options.length];
        for (int i = 0; i < if_you_options.length; i++) {
            String s = if_you_options[i].text;
            if (if_you_options[i].text3 != null) {
                s = s + if_you_options[i].text3 + " ";
            }
            if (if_you_options[i].text2 != null) {
                s = s + if_you_options[i].text2;
            }
            if_you_items[i] = new JMenuItem(s);
            if_you_menu.add(if_you_items[i]);
            final int final_i = i;
            if_you_items[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectIfYou(final_i);
                }
            });
        }
    }

    private void setUpThenWeMenu() {
        then_we_menu = new JPopupMenu("We Will ...");
        ThenWeWill[] then_we_options = ThenWeWill.values();
        then_we_items = new JMenuItem[then_we_options.length];
        for (int i = 0; i < then_we_options.length; i++) {
            String s = then_we_options[i].text;
            if (then_we_options[i].text3 != null) {
                s = s + then_we_options[i].text3 + " ";
            }
            if (then_we_options[i].text2 != null) {
                s = s + then_we_options[i].text2;
            }
            then_we_items[i] = new JMenuItem(s);
            then_we_menu.add(then_we_items[i]);
            final int final_i = i;
            then_we_items[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectThenWe(final_i);
                }
            });
        }
    }

    private void selectIfYou(int selection) {
        Contract.Term term = null;
        switch (IfYouWill.values()[selection]) {
            case PEACE:
                term = new Contract.DiplomaticState(C.DS_PEACE);
                break;
            case MONEY:
                detail_dialog.showDialog(MAX_MONEY_DEMAND);
                int value = detail_dialog.getValue();
                if (value > 0) {
                    term = new Contract.Money(value);
                }
                //System.out.println("I'll forward you to alcoholics anonymous ...");
                break;
            case VOTES:
                term = new Contract.Votes();
                break;
            case MINISTRY:
                term = new Contract.Ministry(selectMinistry(false));
                break;
//            case RESOURCES:
//                System.out.println("A Hawkwood, a Hazat and a Decados went to a bar ...");
//                break;
//            case UNITS:
//                System.out.println("A Hawkwood, a Hazat and a Decados went to a bar ...");
//                break;
//            case CITIES:
//                System.out.println("A Hawkwood, a Hazat and a Decados went to a bar ...");
//                break;
            default:
                throw new AssertionError();
        }
        if (term != null) {
            term.setFactions(faction, game.getTurn());
            contract.addTerm(term);
        }
    }

    private int selectMinistry(boolean donor) {
        int promised_ministry = -1;
        List<Integer> ministry_list = new LinkedList<>();
        if (donor) {
            ministry_list.addAll(non_promised_ministries);
        } else {
            ministry_list.addAll(all_ministries);
        }
//        ministry_list.add(C.FLEET);
//        ministry_list.add(C.THE_SPY);
//        ministry_list.add(C.STIGMATA);
//        if (donor > -1) {
//            int[] promises = game.getDiplomacy().getMinistryPromises(donor);
//            for (int i = 0; i < promises.length; i++) {
//                if (promises[i] > -1) {
//                    ministry_list.remove(promises[i]);
//                }
//            }
//
//        }
        int size = ministry_list.size();
        if (size == 0) {
            return -1;
        } else if (size == 1) {
            promised_ministry = ministry_list.get(0).intValue();
        } else {

            String[] options = new String[size];
            int[] faction_nrs = new int[size];
            for (int i = 0; i < size; i++) {
                int tmp = ministry_list.get(i).intValue();
                options[i] = Util.getFactionName(tmp);
                faction_nrs[i] = tmp;
            }

            int j_options = -1;

            if (size == 2) {
                j_options = JOptionPane.YES_NO_OPTION;

            } else {
                j_options = JOptionPane.YES_NO_CANCEL_OPTION;
            }

            JOptionPane pane = new UtilG.PhoenixJOptionPane("Which ministry?",
                    JOptionPane.PLAIN_MESSAGE, j_options,
                    null, options, options[0]);
            JDialog dialog = pane.createDialog(gui, null);
            dialog.setVisible(true);
            String n = (String) pane.getValue();
            int selected_ministry = -1;
            if (n == null || n.equals(options[0])) {
                selected_ministry = faction_nrs[0];
            } else if (n.equals(options[1])) {
                selected_ministry = faction_nrs[1];
            } else if (size == 3 && n.equals(options[2])) {
                selected_ministry = faction_nrs[2];
            } else {
                selected_ministry = faction_nrs[0];
            }

            promised_ministry = selected_ministry;

        }
        return promised_ministry;
    }

    private void selectThenWe(int selection) {
        Contract.Term term = null;
        switch (ThenWeWill.values()[selection]) {
            case PEACE:
                term = new Contract.DiplomaticState(C.DS_PEACE);
                //System.out.println("\"Peace in our time ...\"");
                break;
            case MONEY:
                int max = game.getFaction(game.getTurn()).balanceBudget(false);
                if (max <= 0) {
                    gui.showInfoWindow("My Lord, our budget is not balanced, we"
                            + " cannot pledge any firebirds !");
                    return;
                }
                detail_dialog.showDialog(game.getFaction(game.getTurn()).balanceBudget(false));
                int value = detail_dialog.getValue();
                if (value > 0) {
                    term = new Contract.Money(value);
                }
                //System.out.println("I'll forward you to alcoholics anonymous ...");
                break;
            case VOTES:
                term = new Contract.Votes();
                break;
            case MINISTRY:
                term = new Contract.Ministry(selectMinistry(true));
                break;
//            case RESOURCES:
//                System.out.println("A Hawkwood, a Hazat and a Decados went to a bar ...");
//                break;
//            case UNITS:
//                System.out.println("A Hawkwood, a Hazat and a Decados went to a bar ...");
//                break;
//            case CITIES:
//                System.out.println("A Hawkwood, a Hazat and a Decados went to a bar ...");
//                break;
            default:
                throw new AssertionError();
        }
        if (term != null) {
            term.setFactions(game.getTurn(), faction);
            contract.addTerm(term);
        }
    }

    private class DetailDialog extends JPanel {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        // pointer to GUI
        private int w;
        private int h;
        private Gui gui;
        private WindowSize ws;
        private JButton cancel;
        private JButton done;
        private JSlider slider;
//        private JTextField amount;
        // pointer to map holding gui element coordinates
        private Map<Enum, Integer> c;

        private JDialog dialog;

        private int value;

        private DetailDialog() {
        }

        private DetailDialog(Gui gui) {
            dialog = new JDialog(gui);
            ws = Gui.getWindowSize();
            w = ws.main_window_width / 2;
            h = ws.main_window_height / 4;
            c = ws.diplomacy_selector;
            //dialog.setBounds(c.get(G.CD.WIN_X) + gui.getX(), c.get(G.CD.WIN_Y) + gui.getY(), c.get(G.CD.WIN_W), c.get(G.CD.WIN_H));
            dialog.setModal(true);
            this.gui = gui;
            game = gui.getGame();
            this.setLayout(null);
            setUpWindow();
            dialog.setUndecorated(true);
            dialog.setBounds(w / 2 + gui.getX(), (ws.main_window_height - h) / 2 + gui.getY(), w, h);
            dialog.add(this);
        }

        public void showDialog(int max) {
            //max = 500000;
            //System.out.println("DGBslider");
            value = -1;
            slider.setMaximum(max);
            int major = (max - 1) / 2;
            slider.setMajorTickSpacing(major);
            slider.setMinorTickSpacing(major / 5);
            slider.setValue(slider.getMinimum());
            slider.setLabelTable(slider.createStandardLabels(major));
            dialog.setVisible(true);
        }

        public void setWindowVisiblity(boolean visible) {
            dialog.setVisible(visible);
        }


        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            renderWindow(g);
        }

        private void setUpWindow() {
            setUpButtons();
            setUpSlider();
//            setUpAmount();
        }

//        private void setUpAmount() {
//            amount = new JTextField();
//            amount.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
//            amount.setBackground(Color.BLACK);
//            amount.setForeground(C.COLOR_GOLD);
//            amount.setBounds((ws.fw_eb_x - 4 * ws.fw_eb_w) / 2, (ws.fw_eb_y - ws.fw_eb_h) / 2, ws.fw_eb_w, ws.font_large_size * 2);
//            this.add(amount);
//        }

        private void setUpButtons() {
            cancel = new JButton("Cancel");
            cancel.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
            cancel.setBackground(Color.BLACK);
            cancel.setForeground(C.COLOR_GOLD);
            cancel.setBounds(ws.fw_eb_w, h - ws.fw_eb_h * 2, ws.fw_eb_w, ws.fw_eb_h);
            cancel.setEnabled(true);
            final DetailDialog self = this;
            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    value = -1;
                    self.setWindowVisiblity(false);
                }
            });
            this.add(cancel);
            done = new JButton("O.K.");
            done.setBorder((BorderFactory.createLineBorder(C.COLOR_GOLD)));
            done.setBackground(Color.BLACK);
            done.setForeground(C.COLOR_GOLD);
            done.setBounds(w - 2 * ws.fw_eb_w, h - ws.fw_eb_h * 2, ws.fw_eb_w, ws.fw_eb_h);
            done.setEnabled(true);
            done.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    value = slider.getValue();
                    self.setWindowVisiblity(false);
                }
            });
            this.add(done);
        }

        private void renderWindow(Graphics g) {
            drawBackground(g);
            drawDetails(g);
        }

        private void drawBackground(Graphics g) {
            byte[][] pallette = gui.getPallette();
            BufferedImage bi = Util.loadImage(FN.S_BG0_PCX, ws.is_double, pallette, 640, 480);
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(bi, null, 0, 0);
            UtilG.drawFrameRectIn(g, 0, 0, w, h);
        }

        private void drawDetails(Graphics gg) {
            Graphics2D g = (Graphics2D) gg;
            drawTexts(g);
        }

        private void drawTexts(Graphics2D g) {
            String s = "Firebirds " + slider.getValue();
            int x = UtilG.center(g, 0, w, ws.font_large, s);
            UtilG.drawStringGrad(g, s, ws.font_large, x, h / 50 + ws.font_large_size);
        }

        private void setUpSlider() {

            slider = new JSlider(JSlider.HORIZONTAL, 1, 100, 1);
            slider.setPaintLabels(true);
            slider.setPaintTicks(true);
            MouseWheelListener mwl = new MouseAdapter() {

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    int old = slider.getValue();
                    slider.setValue(old - e.getWheelRotation() * 100);
                }

            };
            ChangeListener cl = (ChangeEvent e) -> {
                repaint();
            };
            slider.setUI(new DarkSliderUI());
            slider.setBackground(Color.BLACK);
            slider.setForeground(C.COLOR_GOLD);
            slider.addChangeListener(cl);
            slider.addMouseWheelListener(mwl);
            int w = this.w * 4 / 5;
            slider.setBounds((this.w - w) / 2, h / 4, w, 39);
            this.add(slider);
        }

        /**
         * Return slider value and set it to -1.
         *
         * @return the value
         */
        public int getValue() {
            int tmp = value;
            value = -1;
            return tmp;
        }
    }
}
