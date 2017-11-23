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
public class Event_Lethal extends Event {
    private final ArrayList<Integer> winners;
    private final ArrayList<Integer> losers;
    
    Event_Lethal(GamePeriod.Phase phase, int id, int numTributes, String desc,
            ArrayList<Integer> winners, ArrayList<Integer> losers) 
    {
        super(phase, id, numTributes, desc);
        this.winners = winners;
        this.losers = losers;
    }
    
    public ArrayList<Integer> getWinners() { return winners; }
    public ArrayList<Integer> getLosers() { return losers; }
    
    @Override
    public String execute(ArrayList<Tribute> tributes) throws Exception
    {
        if (tributes.size() < numTributes) {
            throw new Exception("Not enough tributes passed to event: #" + id 
                    + ", Phase " + phase.toString() + ", Lethal.");
        }
        for (int loserNum : losers) {
            tributes.get(loserNum).setDead(true);
        }
        for (int winnerNum : winners) {
            for (int loserNum : losers) {
                Tribute loser = tributes.get(loserNum);
                tributes.get(winnerNum).giveKill(loser.name);
            }
        }
        return super.execute(tributes);
    }
    
    @Override
    public void dump()
    {
        System.out.println("Event " + id + ": (" + phase.toString() + ", lethal)");
        System.out.println("Desc: " + desc);
        System.out.println("Killers: " + winners);
        System.out.println("Killed: " + losers);
        System.out.println("Tributes: " + numTributes);
    }
}
