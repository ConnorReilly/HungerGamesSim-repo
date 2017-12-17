/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hunger_games_sim;

import java.util.ArrayList;

/**
 *
 * @author ConnorReilly
 */
public class Event {
    // Events are uniquely identified by their phase, id, and lethality
    // This class models non-lethal events; lethal events are a subclass of this one
    public final GamePeriod.Phase phase;   // Phase during which this event can occur
    public final int id;   // event ID
    public final int numTributes;   // number of tributes that this event requires
    public final String desc;   // event description
    
    Event(GamePeriod.Phase phase, int id, int numTributes, String desc)
    {
        this.phase = phase;
        this.id = id;
        this.desc = desc.trim();
        this.numTributes = numTributes;
    }
    
    /**
     * Execute this event
     * @param tributes the tributes involved
     * @param period current game period
     * @return the event description, after tribute data placeholders have been replaced with actual data
     * @throws Exception if not enough tributes were passed
     */
    public String execute(ArrayList<Tribute> tributes, GamePeriod period) throws Exception {
        if (tributes.size() < numTributes) {
            throw new Exception("Not enough tributes passed to event: #" + id 
                    + ", Phase " + phase.toString() + ", Nonlethal.");
        }
        int idx = 0;
        StringBuilder strBuilder = new StringBuilder();
        while (idx < desc.length()) {
            if (desc.charAt(idx) == '(') {
                if (desc.substring(idx+1, idx+7).equals("Player")) {
                    idx += 7;
                    int EndTribNumIdx = desc.indexOf(')', idx);
                    String tributeNumStr = desc.substring(idx, EndTribNumIdx);
                    int tributeNum = Integer.parseInt(tributeNumStr);
                    Tribute tribute = tributes.get(tributeNum-1);
                    strBuilder.append(tribute.name);
                    idx = EndTribNumIdx + 1;
                } else {
                    int idxEnd = desc.indexOf(')', ++idx);
                    int idxSlash = idx + desc.substring(idx, idxEnd).indexOf('/');
                    String tempStr = desc.substring(idxSlash+1, idxEnd);
                    int i = 0;
                    while (i < tempStr.length()) {
                        char c = tempStr.charAt(i);
                        if (c >= '0' && c <= '9') break;
                        ++i;
                    }
                    assert(i < tempStr.length());
                    int j = i+1;
                    while (j < tempStr.length()) {
                        char c = tempStr.charAt(j);
                        if (c < '0' || c > '9') break;
                        ++j;
                    }
                    i += idxSlash+1;
                    j += idxSlash+1;
                    
                    int tribNum = Integer.parseInt(desc.substring(i, j));
                    if (tributes.get(tribNum-1).isMale) {
                        strBuilder.append(desc.substring(idx, idxSlash));
                    } else {
                        strBuilder.append(desc.substring(idxSlash+1, idxEnd-(j-i)));
                    }
                    idx = idxEnd+1;
                }
            } else {
                strBuilder.append(desc.charAt(idx++));
            }
        }
        return new String(strBuilder);
    }
    
    /**
     * Dump event data
     */
    public void dump() 
    {
        System.out.println("Event " + id + ": (" + phase.toString() + ", non-lethal)");
        System.out.println("Desc: " + desc);
        System.out.println("Tributes: " + numTributes);
    }
}
