
package game;

 
import dat.EfsIni;
import dat.StrBuild;
import dat.UnitType;
import galaxyreader.Planet;
import galaxyreader.Structure;
import galaxyreader.Unit;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Arrays;    // DEBUG
import util.C;
import util.StackIterator;
import util.Util;

/**
 * Will handle faction data, particularly money
 * @author RSW
 */
public class Faction implements Serializable {
    
    private Game game;
    private EfsIni efs_ini;
    private int turn;
    
    private int firebirds;
    private int tax_rate;
    private int tithe_rate;
    private int pay_rate;
    List<Message> messages = new LinkedList<>();

    public Faction(Game game) {
        
        this.game = game;
        this.efs_ini = game.getEfs_ini();
        
        firebirds = efs_ini.starting_credits;
        tax_rate = efs_ini.default_tax_rate;
        tithe_rate = efs_ini.default_tithe_rate;
        pay_rate = 100;
        
    }
    
        public void addMessage(Message m) {
        messages.add(m);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void deleteOldMessages(int year) {
        ListIterator<Message> iter = messages.listIterator();
        Message m = null;
        while (iter.hasNext()) {
            m = iter.next();
            if (year > m.year) {
                iter.remove();
            }
        }

    }

    public static Faction[] createFactions(Game game) {
        Faction[] factions = new Faction[C.NR_FACTIONS];
        for (int i = 0; i < factions.length; i++) {
            factions[i] = new Faction(game);

        }
        return factions;
    }

}
 
