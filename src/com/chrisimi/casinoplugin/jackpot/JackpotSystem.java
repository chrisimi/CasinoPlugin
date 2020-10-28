package com.chrisimi.casinoplugin.jackpot;

import com.chrisimi.casinoplugin.serializables.Jackpot;
import com.gmail.filoghost.holographicdisplays.api.Hologram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JackpotSystem
{
    private static Map<String, Jackpot> activeJackpots = new HashMap<String, Jackpot>();

    /**
     * init the jackpot - create the hologram etc...
     * @param jackpot
     */
    public static void initJackpot(Jackpot jackpot)
    {

    }

    /**
     * remove the jackpot from the system - not delete it
     * @param name the name of the jackpot
     * @return true if it was successful, false if not
     */
    public static boolean deleteJackpot(String name)
    {
        if(!activeJackpots.containsKey(name)) return false;

        Jackpot jackpot = activeJackpots.get(name);

        //remove the jackpot hologram
        jackpot.hologramInstance.delete();

        return true;
    }

    private static Hologram createHologram(Jackpot jackpot)
    {
        return null;
    }
}
