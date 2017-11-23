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
public class Tribute {
    public final String name;
    public final boolean isMale;
    private boolean isDead;
    private final ArrayList<String> kills;
    private final ArrayList<String> killedBy;
    private GamePeriod diedOn;
    
    Tribute(String name, boolean isMale)
    {
        this.name = name.trim();
        this.isMale = isMale;
        isDead = false;
        kills = new ArrayList<>();
        killedBy = new ArrayList<>();
        diedOn = null;
    }
    
    public boolean isDead() { return isDead; }
    public ArrayList<String> getKills() { return kills; }
    public ArrayList<String> getKilledBy() { return killedBy; }
    public GamePeriod getDiedOn() { return diedOn; }
    
    public void setDead(boolean isDead) { this.isDead = isDead; }
    public void addKill(String name) { kills.add(name); }
    public void addKiller(String name) { killedBy.add(name); }
    public void setDiedOn(GamePeriod period)
    {
        if (period.getPhase() == GamePeriod.Phase.START) {
            diedOn = new GamePeriod(GamePeriod.Phase.DAY, period.getDayNum());
        } else {
            diedOn = new GamePeriod(period);
        }
    }
    
    public void dump()
    {
        System.out.println(name);
        System.out.println("Sex: " + ((isMale) ? "Male":"Female"));
        System.out.println("Dead: " + ((isDead) ? "Yes":"No"));
    }
}
