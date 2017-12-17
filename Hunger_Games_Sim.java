/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hunger_games_sim;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 *
 * @author ConnorReilly
 */
public class Hunger_Games_Sim {
    // Names of input data files
    public static final String FILENAME_EVENTS_START_NONLETHAL = "Events_start_nonlethal.txt";
    public static final String FILENAME_EVENTS_START_LETHAL = "Events_start_lethal.txt";
    public static final String FILENAME_EVENTS_DAY_NONLETHAL = "Events_day_nonlethal.txt";
    public static final String FILENAME_EVENTS_DAY_LETHAL = "Events_day_lethal.txt";
    public static final String FILENAME_EVENTS_NIGHT_NONLETHAL = "Events_night_nonlethal.txt";
    public static final String FILENAME_EVENTS_NIGHT_LETHAL = "Events_night_lethal.txt";
    public static final String FILENAME_TRIBUTES_LIVING = "Tributes_living.txt";
    
    public static final int NUM_DISTRICTS = 12;
    public static final int TRIBUTES_PER_DISTRICT = 3;
    public static final int TIME_INTERVAL = 3;   // The number of hours that pass between updates
    public static final double LETHAL_EVENT_PROB = 0.25;   // Probablility of lethal event'
    
    private static final TreeMap<GamePeriod.Phase, ArrayList<Event>> eventMap = new TreeMap<>();
    private static final TreeMap<GamePeriod.Phase, ArrayList<Event>> lethalEventMap = new TreeMap<>();
    
    private static final ArrayList<Tribute> allTributes = new ArrayList<>();   // All tribute data from parsed input file
    private static final ArrayList<Tribute> selectedTributes = new ArrayList<>();   // All tributes participating in the current HG
    private static final ArrayList<Tribute> livingTributes = new ArrayList<>();   // All living tributes in the current HG
    private static final ArrayList<Tribute> deadTributes = new ArrayList<>();   // All tributes that died in the current HG
    
    private static GamePeriod gp = new GamePeriod();   // Day/night number and time
    
    /**
     * @param args the command line arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        buildEventMaps();
        fetchAllTributes();
        selectTributes();
        
        while (livingTributes.size() > 1) {
            System.out.println(gp.toString());
            ArrayList<Tribute> tributesToAct = new ArrayList<>(livingTributes);
            while (!tributesToAct.isEmpty()) {
                tributesToAct = executeRandomEvent(tributesToAct);
            }
            System.out.println();
            printTributeStats();
            gp.update(TIME_INTERVAL);
            System.out.println();
        }
        if (livingTributes.size() == 1) {
            Tribute survivor = livingTributes.get(0);
            System.out.println("The winner of this Hunger Games is " + survivor.name
                            + " of District " + survivor.getDistrict());
        } else {
            System.out.println("There are no winners. :(");
        }
    }
    
    /**
     * Select random event and fill with random tributes
     * @param tributesToAct the list of tributes to choose from
     * @return the list of remaining tributes (chosen tributes are removed)
     * @throws Exception for print failure
     */
    public static ArrayList<Tribute> executeRandomEvent(ArrayList<Tribute> tributesToAct) throws Exception
    {
        Random rand = new Random();
        Event event = null;
        ArrayList<Event> candidateEvents;
        ArrayList<Tribute> tributesInvolved = new ArrayList<>();
        if (rand.nextDouble() < LETHAL_EVENT_PROB) {
            candidateEvents = lethalEventMap.get(gp.getPhase());
        } else {
            candidateEvents = eventMap.get(gp.getPhase());
        }
        do { 
            event = candidateEvents.get(rand.nextInt(candidateEvents.size()));
        } while (event.numTributes > tributesToAct.size());
        for (int j = 0; j < event.numTributes; ++j) {
            int tribIdx = rand.nextInt(tributesToAct.size());
            tributesInvolved.add(tributesToAct.remove(tribIdx));
        }
        System.out.println(event.execute(tributesInvolved, gp));
        if (event instanceof Event_Lethal) {
            Event_Lethal lethalEvent = (Event_Lethal) event;
            ArrayList<Integer> losers = lethalEvent.getLosers();
            ArrayList<Integer> winners = lethalEvent.getWinners();
            for (int j = 0; j < losers.size(); ++j) {
                Tribute killed = tributesInvolved.get(losers.get(j)-1);
                int i = 0;
                int size = livingTributes.size();
                while (!livingTributes.get(i).name.equals(killed.name) && i < size) ++i;
                assert (i < size);
                livingTributes.remove(i);
                deadTributes.add(killed);
                i = 0;
                size = selectedTributes.size();
                while (!selectedTributes.get(i).name.equals(killed.name) && i < size) ++i;
                assert (i < size);
                selectedTributes.remove(i);
                selectedTributes.add(i, killed);
            }
        }
        return tributesToAct;
    }
    
    /**
     * Parse event data and initialize event maps
     * @throws Exception on parse failure
     */
    public static void buildEventMaps() throws Exception
    {   
        GamePeriod.Phase phase = GamePeriod.Phase.START;
        eventMap.put(phase, parseEventData(FILENAME_EVENTS_START_NONLETHAL, phase, false));
        lethalEventMap.put(phase, parseEventData(FILENAME_EVENTS_START_LETHAL, phase, true));
        phase = GamePeriod.Phase.DAY;
        eventMap.put(phase, parseEventData(FILENAME_EVENTS_DAY_NONLETHAL, phase, false));
        lethalEventMap.put(phase, parseEventData(FILENAME_EVENTS_DAY_LETHAL, phase, true));
        phase = GamePeriod.Phase.NIGHT;
        eventMap.put(phase, parseEventData(FILENAME_EVENTS_NIGHT_NONLETHAL, phase, false));
        lethalEventMap.put(phase, parseEventData(FILENAME_EVENTS_NIGHT_LETHAL, phase, true));
    }
    
    /**
     * Parse all event data in a text file
     * @param eventFileName file to read event data from
     * @param phase game phase during which this event can occur
     * @param lethal whether this event is lethal
     * @return the list of parsed data
     * @throws Exception on file read/parse error
     */
    public static ArrayList<Event> parseEventData(String eventFileName, 
            GamePeriod.Phase phase, boolean lethal) throws Exception
    {
        ArrayList<Event> eventList = new ArrayList<>();
        InputStream inStream = new FileInputStream(new File(eventFileName));
        Scanner sc = new Scanner(inStream);
        Pattern eventIdPattern = Pattern.compile("#[0-9]++.");
        while (sc.hasNext() && !sc.hasNext(eventIdPattern)) sc.nextLine();
        while (sc.hasNext()) {
            // Fetch event id
            String str = sc.next();
            int idx1 = 0;
            char c = str.charAt(idx1);
            while (c > '9' || c < '0') c = str.charAt(++idx1);
            int idx2 = idx1 + 1;
            c = str.charAt(idx2);
            while (c <= '9' && c >= '0') c = str.charAt(++idx2);
            int id = Integer.parseUnsignedInt(str.substring(idx1, idx2));
            
            // Fetch event description
            str = sc.nextLine();
            str = str.trim();
            while (!sc.hasNext("Tributes:")) str += sc.nextLine();
            String desc = str;
            
            // Fetch number of tributes
            sc.next();
            int numTributes = Integer.parseUnsignedInt(sc.next());
            
            if (lethal) {
                sc.next();
                ArrayList<Integer> winners = new ArrayList<>();
                if (sc.hasNext("None")) sc.next();
                else {
                    while (sc.hasNext(Pattern.compile("Player[0-9]++,?"))) {
                        str = sc.next();
                        str = str.substring("player".length());
                        if (str.indexOf(',') != -1) str = str.substring(0,str.length()-1);
                        winners.add(Integer.parseInt(str));
                    }
                }
                sc.next();
                ArrayList<Integer> losers = new ArrayList<>();
                while (sc.hasNext(Pattern.compile("Player[0-9]++,?"))) {
                    str = sc.next();
                    str = str.substring("player".length());
                    if (str.indexOf(',') != -1) str = str.substring(0,str.length()-1);
                    losers.add(Integer.parseInt(str));
                }
                eventList.add(new Event_Lethal(phase, id, numTributes, desc, winners, losers));
            } else {
                eventList.add(new Event(phase, id, numTributes, desc));
            }
            
            while (sc.hasNext() && !sc.hasNext(eventIdPattern)) sc.nextLine();
        }
        return eventList;
    }
    
    /**
     * Parse living tribute data
     * @throws Exception on file read/parse error
     */
    public static void fetchAllTributes() throws Exception
    {
        InputStream inStream = new FileInputStream(new File(FILENAME_TRIBUTES_LIVING));
        Scanner sc = new Scanner(inStream);
        while (sc.hasNext() && !sc.hasNext("Type:")) sc.nextLine();
        sc.nextLine();
        while (sc.hasNext()) {
            String name = sc.nextLine();
            sc.next(); sc.next();
            String sex = sc.next();
            boolean isMale = sex.equals("male");
            allTributes.add(new Tribute(name, isMale));
            while (sc.hasNext() && !sc.hasNext("Type:")) sc.nextLine();
            if (sc.hasNext()) sc.nextLine();
        }
    }
    
    /**
     * Randomly select tributes for this hunger games, divided into districts
     */
    public static void selectTributes()
    {
        Random rand = new Random();
        for (int dist = 1; dist <= NUM_DISTRICTS; ++dist) {
            System.out.println("District " + dist + ":");
            for (int i = 0; i < TRIBUTES_PER_DISTRICT; ++i) {
                Tribute tribute = allTributes.remove(rand.nextInt(allTributes.size()));
                System.out.println("- " + tribute.name);
                tribute.setDistrict(dist);
                selectedTributes.add(tribute);
                livingTributes.add(tribute);
            }
            System.out.println();
        }
    }
    
    /**
     * Print current stats for all tributes
     */
    public static void printTributeStats()
    {
        for (int i = 0; i < selectedTributes.size(); ++i) {
            if (i % TRIBUTES_PER_DISTRICT == 0) {
                if (i != 0) System.out.println();
                System.out.println("DISTRICT " + (i/TRIBUTES_PER_DISTRICT + 1));
            }
            System.out.println("- " + selectedTributes.get(i));
        }
    }
    
    /**
     * Dump all event map contents (for debugging)
     */
    public static void dumpMaps()
    {
        ArrayList<Event> eventList;
        GamePeriod.Phase[] keyList = new GamePeriod.Phase[eventMap.keySet().size()];
        eventMap.keySet().toArray(keyList);
        for (int i = 0; i < keyList.length; ++i) {
            eventList = eventMap.get(keyList[i]);
            for (int j = 0; j < eventList.size(); ++j) {
                eventList.get(j).dump();
                System.out.println();
            }
        }
        keyList = new GamePeriod.Phase[lethalEventMap.keySet().size()];
        lethalEventMap.keySet().toArray(keyList);
        for (int i = 0; i < keyList.length; ++i) {
            eventList = lethalEventMap.get(keyList[i]);
            for (int j = 0; j < eventList.size(); ++j) {
                eventList.get(j).dump();
                System.out.println();
            }
        }
    }
    
    /**
     * Dump tribute data
     * @param tribList list of tribute data to dump
     */
    public static void dumpTributes(ArrayList<Tribute> tribList)
    {
        for (int i = 0; i < tribList.size(); ++i) {
            System.out.println(tribList.get(i));
            System.out.println();
        }
    }
    
}
