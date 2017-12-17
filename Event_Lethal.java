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
    
    /**
     * Execute lethal event; update tribute stats accordingly
     * @param tributes tributes involved
     * @param period current game period
     * @return the event description, after tribute data placeholders have been replaced with actual data
     * @throws Exception if not enough tributes were passed
     */
    @Override
    public String execute(ArrayList<Tribute> tributes, GamePeriod period) throws Exception
    {
        if (tributes.size() < numTributes) {
            throw new Exception("Not enough tributes passed to event: #" + id 
                    + ", Phase " + phase.toString() + ", Lethal.");
        }
        for (int loserNum : losers) {
            Tribute loser = tributes.get(loserNum-1);
            loser.setDead(true);
            loser.setDiedOn(period);
            for (int winnerNum : winners) {
                Tribute winner = tributes.get(winnerNum-1);
                loser.addKiller(winner.name);
                winner.addKill(loser.name);
                tributes.remove(winnerNum-1);
                tributes.add(winnerNum-1, winner);
            }
            tributes.remove(loserNum-1);
            tributes.add(loserNum-1, loser);
        }
        return super.execute(tributes, period);
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
