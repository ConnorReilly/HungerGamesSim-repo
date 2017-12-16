/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hunger_games_sim;

/**
 *
 * @author ConnorReilly
 */
public final class GamePeriod {
    public enum Phase { START, DAY, NIGHT };
    public static final int NUM_PHASES = 3;
    private int time;
    public int getTime() { return time; }
    public void setTime(int time) { this.time = time; }
    private Phase phase;
    private int dayNum;
    
    GamePeriod() { reset(); }
    GamePeriod(GamePeriod period)
    {
        phase = period.getPhase(); dayNum = period.getDayNum(); time = period.getTime();
    }
    GamePeriod(Phase phase, int dayNum) {
        this(); this.phase = phase; this.dayNum = dayNum;
    }
    GamePeriod(Phase phase, int dayNum, int time)
    {
        this(phase, dayNum); this.time = time;
    }
    
    public void update(int addHours)
    {
        int numPhaseChanges = 0;
        int tmp = addHours;
        int hoursUntilPhaseChange;
        if (phase == Phase.START) {
            phase = (time % 24 < 19 && time % 24 >= 7) ? Phase.DAY:Phase.NIGHT;
        }
        if (phase == Phase.DAY) hoursUntilPhaseChange = 19-time;
        else hoursUntilPhaseChange = (time >= 19) ? 31 - time : 7 - time;
        tmp -= hoursUntilPhaseChange;
        while (tmp >= 0) { ++numPhaseChanges; tmp -= 12; }
        tmp = numPhaseChanges;
        if (phase == Phase.NIGHT && tmp > 0) {
            ++dayNum; --tmp;
        }
        while (tmp > 0) {
            if (--tmp > 0) {
                ++dayNum; --tmp;
            }
        }
        if (numPhaseChanges % 2 == 1) {
            phase = (phase == Phase.DAY) ? Phase.NIGHT:Phase.DAY;
        }
        time += addHours;
        time = time % 24;
    }
    
    public void reset() { phase = Phase.START; dayNum = 1; time = 7; }
    
    public Phase getPhase() { return phase; }
    public int getDayNum() { return dayNum; }
    
    @Override
    public String toString()
    {
        int hour24 = time;
        int hour12;
        GamePeriod.Phase phaseTmp;
        String gameStart;
        if (hour24 == 0) hour12 = 12;
        else if (hour24 > 12) hour12 = hour24 - 12;
        else hour12 = hour24;
        if (phase == Phase.START) {
            phaseTmp = (time < 19 && time >= 7) ? Phase.DAY:Phase.NIGHT;
            gameStart = " (GAME START)";
        } else { 
            phaseTmp = phase;
            gameStart = "";
        }
        
        return hour12 + ":00" 
                    + ((hour24 < 12) ? "am":"pm") + " "
                    + phaseTmp + " " + dayNum + gameStart;
    }
}
