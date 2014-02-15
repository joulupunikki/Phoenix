/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import util.C;

/**
 * Contains per faction data: firebirds, messages, buildable units.
 *
 * @author joulupunikki
 */
public class Faction implements Serializable {

//    List<Unit> units;
//    List<Unit> unmoved_units;
//    List<Structure> cities;
    int firebirds = C.STARTING_FIREBIRDS;
    List<Message> messages = new LinkedList<>();

    public Faction() {
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

    public static Faction[] createFactions() {
        Faction[] factions = new Faction[C.NR_FACTIONS];
        for (int i = 0; i < factions.length; i++) {
            factions[i] = new Faction();

        }
        return factions;
    }
}
