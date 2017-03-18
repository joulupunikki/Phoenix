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

import galaxyreader.Structure;
import galaxyreader.Unit;
import game.Message;
import game.Regency;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;
import util.C;
import util.Util;

/**
 * Byzantium II Window base state.
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class ByzII extends State {

    private static ByzII instance = new ByzII();

    private ByzII() {
    }

    public static State get() {
        return instance;
    }

    @Override
    public void pressExitButton() {
        if (game.getRegency().needToAssignOffices(game)) {
            gui.showInfoWindow("You must assign all the offices !");
            return;
        }
        if (game.getRegency().getRegent() == game.getTurn()) {
            if (game.getRegency().isMay_set_offices()) {
                game.initVisibilitySpot(false); // re-spot with ministry assets
            }
            game.getRegency().setMay_set_offices(false);
        }
        
        SU.restoreMainWindow();
        gui.setCurrentState(main_game_state);
        main_game_state = null;
    }

    @Override
    public void pressVoteButton() {
        if (!game.getRegency().allowedToVote(game.getTurn())) {
            gui.showInfoWindow("House " + Util.getFactionName(game.getTurn()) + " is not eligible to vote in these elections!");
            return;
        }
        if (game.getRegency().needToVote(game.getTurn(), game.getEfs_ini(), game.getYear())) {
            gui.setCurrentState(ByzII2.get());
            String s = "This is";
            int claim = game.getRegency().getElectionLevel();
            if (claim == C.ELECTION_LEVEL.REGENT) {
                s += " a vote for the regency. ";
            } else {
                String claimant = Util.factionNameDisplay(game.getRegency().getRegent());
                if (claim == C.ELECTION_LEVEL.FIRST_EMPEROR) {
                    s += " the first vote for ";
                } else {
                    s += " the final vote for ";
                }
                s += claimant
                        + "'s claim to the emperor's crown.";
                switch (claim) {
                    case C.ELECTION_LEVEL.FIRST_EMPEROR:
                        break;
                    case C.ELECTION_LEVEL.FINAL_EMPEROR:
                        s += "\nIf " + claimant
                                + " wins this vote then they will be declared the Emperor!";
                        break;
                    default:
                        throw new AssertionError();
                }
            }
            gui.showInfoWindow(s + " Cast your votes, Lord " + Util.getFactionName(game.getTurn()) + ".");
            gui.getByzantium_ii_window().enableAbstainButton(true);
            gui.getByzantium_ii_window().enableVoteButton(false);
        }
    }

    @Override
    public void pressDeclareEmperorButton() {
        if (!gui.showConfirmWindow("Are you sure you want to lay a claim to the emperor's crown?")) {
            return;
        }
        game.getRegency().makeThroneClaim();
        String s = "" + Util.factionNameDisplay(game.getTurn()) + " has laid claim "
                + "to the emperor's crown in the year " + game.getYear() + ". New "
                + "elections must take place in the year " + (game.getYear() + 1)
                + " to decide the legitimacy of this claim.";
        for (int i = 0; i < C.NR_HOUSES; i++) {
            game.getFaction(i).addMessage(new Message(s, C.Msg.THRONE_CLAIM, i, null));
        }
        gui.getByzantium_ii_window().hideDeclareEmperorButton();
    }

    @Override
    public void clickOnByzantiumIIWindow(MouseEvent e) {
        Point p = e.getPoint();
        int pos = getPosition(p);
        Regency r = game.getRegency();

        switch (pos) {
            case C.STIGMATA:
            case C.THE_SPY:
            case C.FLEET:
                if (game.getTurn() != game.getRegency().getRegent()) {
                    gui.showInfoWindow("Only the Regent may assign offices!");
                    return;
                }
                if (!game.getRegency().isMay_set_offices()) {
                    gui.showInfoWindow("You may only assign offices once on your first turn as Regent.");
                    return;
                }
                if (game.getRegency().isPromisedMinistry(pos)) {
                    gui.showInfoWindow("You promised that office already!");
                    return;
                }
                break;
//            case C.IMPERIAL: // regent banner
//                break;
            default:
                return;
        }

        switch (pos) {
            case C.STIGMATA:
                r.setGarrison(r.cycleMinistry(r.getGarrison(), Regency.GARRISON, game));
                setAssets(pos, r.getGarrison());
                break;
            case C.THE_SPY:
                r.setEye(r.cycleMinistry(r.getEye(), Regency.EYE, game));
                setAssets(pos, r.getEye());
                break;
            case C.FLEET:
                r.setFleet(r.cycleMinistry(r.getFleet(), Regency.FLEET, game));
                setAssets(pos, r.getFleet());
                break;
//            case C.IMPERIAL: // this is used during debugging
//                r.setRegent(r.cycleMinistry(r.getRegent(), Regency.GARRISON, game));
//                break;
            default:
                throw new AssertionError();
        }
    }


    public static void setAssets(int ministry, int faction) {
        List<Unit> u = game.getUnits();
        List<Structure> s = game.getStructures();
        Point p = new Point(faction, ministry);
        if (faction == -1) {
            p.x = ministry;
        }
        
        for (Unit u1 : u) {
            if (u1.prev_owner == ministry) {
                game.changeOwnerOfUnit(p, u1);
            }
        }
        for (Structure s1 : s) {
            if (s1.prev_owner == ministry) {
                game.getEconomy().updateProdConsForCity(s1, false);
                s1.owner = p.x;
                game.getEconomy().updateProdConsForCity(s1, true);
            }
        }
    }
    
    static protected int getPosition(Point p) {
        int pos = -1;
        if (ws.bz2_ministry_y1 <= p.y && p.y <= ws.bz2_ministry_y2) {
            if (ws.bz2_stigmata_x1 <= p.x && p.x <= ws.bz2_stigmata_x2) {
                pos = C.STIGMATA;
            } else if (ws.bz2_eye_x1 <= p.x && p.x <= ws.bz2_eye_x2) {
                pos = C.THE_SPY;
            } else if (ws.bz2_fleet_x1 <= p.x && p.x <= ws.bz2_fleet_x2) {
                pos = C.FLEET;
            }
        } else if (ws.bz2_regent_x1 <= p.x && p.x <= ws.bz2_regent_x2
                && ws.bz2_regent_y1 <= p.y && p.y <= ws.bz2_regent_y2) {
            pos = C.IMPERIAL; // using faction ID of IMPERIAL for regent
        } else if (ws.bz2_house_banner_y1 <= p.y && p.y <= ws.bz2_house_banner_y2) {
            if (ws.bz2_house_names_x11 <= p.x && p.x <= ws.bz2_house_names_x11 + ws.bz2_house_names_w) {
                pos = C.HOUSE1;
            } else if (ws.bz2_house_names_x12 <= p.x && p.x <= ws.bz2_house_names_x12 + ws.bz2_house_names_w) {
                pos = C.HOUSE2;
            } else if (ws.bz2_house_names_x13 <= p.x && p.x <= ws.bz2_house_names_x13 + ws.bz2_house_names_w) {
                pos = C.HOUSE3;
            } else if (ws.bz2_house_names_x14 <= p.x && p.x <= ws.bz2_house_names_x14 + ws.bz2_house_names_w) {
                pos = C.HOUSE4;
            } else if (ws.bz2_house_names_x15 <= p.x && p.x <= ws.bz2_house_names_x15 + ws.bz2_house_names_w) {
                pos = C.HOUSE5;
            }
            System.out.println(pos);
        }
        return pos;
    }

}
