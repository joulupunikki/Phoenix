package game;

import dat.EfsIni;
import java.io.File;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import util.C;
import util.Util;

/**
 * Will handle faction data, particularly money
 *
 * @author RSW
 */
public class Faction implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Game game;
    private EfsIni efs_ini;
    private int turn;

    private int number;
    private int firebirds;
    private int tax_rate;
    private int tithe_rate;
    private int pay_rate;
    List<Message> messages = new LinkedList<>();
//    private boolean[] techs;
//    private int[] tech_costs;

    private Research research;

    public Faction(Game game, int number) {

        this.game = game;
        this.efs_ini = game.getEfs_ini();

        this.number = number;
        firebirds = efs_ini.starting_credits;
        tax_rate = efs_ini.default_tax_rate;
        tithe_rate = efs_ini.default_tithe_rate;
        pay_rate = 100;

//        initTechs();
        initResearch();

    }

    public void addMessage(Message m) {
        messages.add(m);
    }

    public void initResearch() {
        research = new Research(game);
    }

    public Research getResearch() {
        return research;
    }

//    public void setResearch(int tech) {
//        research.researched = tech;
//    }
//
//    /**
//     * Calculate research points at beginning of turn.
//     */
//    public void initResearchPts() {
//        research.points_left = 0;
//        List<Structure> cities = game.getStructures();
//        for (Structure structure : cities) {
//            if (structure.owner == game.getTurn() && structure.type == C.LAB) {
//                research.points_left += game.getEfs_ini().lab_points;
//            }
//        }
//
//        for (int i = 0; i < research.techs.length; i++) {
//            if (research.techs[i]) {
//                research.points_left -= game.getResources().getTech()[i].stats[C.TECH_COST] / C.TECH_MAINT;
//            }
//
//        }
//
//    }
//
//    public void doResearch() {
//        if (research.researched == 0) {
//            return;
//        }
//        research.points[research.researched] += research.points_left;
//        int cost = game.getResources().getTech()[research.researched].stats[C.TECH_COST];
//        research.points_left = research.points[research.researched] - cost;
//        if (research.points_left >= 0) {
//            research.techs[research.researched] = true;
//            research.researched = 0;
//        } else {
//            research.points_left = 0;
//        }
//    }
//    public void initTechs() {
//        int len = game.getResources().getTech().length;
//        techs = new boolean[len];
//        tech_costs = new int[len];
//        for (int i = 0; i < techs.length; i++) {
//            techs[i] = false;
//            tech_costs[i] = 0;
//
//        }
//        techs[0] = true;
//    }
//    public boolean[] getTechs() {
//        return techs;
//    }
//
//    public int[] getTechCosts() {
//        return tech_costs;
//    }
    public List<Message> getMessages() {
        return messages;
    }

    public void deleteOldMessages() {
        messages.clear();
    }

//    public void deleteOldMessages(int year) {
//        ListIterator<Message> iter = messages.listIterator();
//        Message m = null;
//        while (iter.hasNext()) {
//            m = iter.next();
//            if (year > m.getYear()) {
//                iter.remove();
//            }
//        }
//
//    }

    public static Faction[] createFactions(Game game) {
        Faction[] factions = new Faction[C.NR_FACTIONS];
        for (int i = 0; i < factions.length; i++) {
            factions[i] = new Faction(game, i);

        }
        return factions;
    }

    /**
     * Game state printout method, prints out faction information.
     *
     */
    public void record(File file) {
        Util.printString(file, " " + Util.getFactionName(number) + "," + number);
        research.record(file);
    }
}
