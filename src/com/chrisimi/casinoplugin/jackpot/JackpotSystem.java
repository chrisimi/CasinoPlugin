package com.chrisimi.casinoplugin.jackpot;

import com.chrisimi.casinoplugin.animations.jackpot.SimpleJackpotAnimation;
import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.serializables.Jackpot;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JackpotSystem
{
    private static Map<String, Jackpot> activeJackpots = new HashMap<String, Jackpot>();

    private static int bukkitTaskID = 0;

    public static void initSystem()
    {
        if(bukkitTaskID == 0)
            bukkitTaskID = Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), systemRunnable, 20L, 20L * 60 * 2);
        else
        {
            stopSystem();
            initSystem();
        }
    }

    public static void stopSystem()
    {
        Main.getInstance().getServer().getScheduler().cancelTask(bukkitTaskID);
        bukkitTaskID = 0;
    }

    private static Runnable systemRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            for(Jackpot jackpot : activeJackpots.values())
            {
                updateJackpot(jackpot);
            }
        }
    };

    private static void updateJackpot(Jackpot jackpot)
    {
        if(jackpot.hologramInstance != null)
            jackpot.hologramInstance.delete();

        jackpot.hologramInstance = createHologram(jackpot);
    }

    /**
     * init the jackpot - create the hologram etc...
     * @param jackpot
     */
    public static void initJackpot(Jackpot jackpot)
    {
        if(!activeJackpots.containsKey(jackpot.name))
        {
            jackpot.hologramInstance = createHologram(jackpot);
            activeJackpots.put(jackpot.name, jackpot);
        }
    }

    /**
     * remove the jackpot from the system - not delete it
     * @param name the name of the jackpot
     * @return true if it was successful, false if not
     */
    public static boolean deleteJackpot(String name)
    {
        if(!activeJackpots.containsKey(name)) return false;

        Jackpot jackpot = activeJackpots.remove(name);

        //remove the jackpot hologram
        jackpot.hologramInstance.delete();

        return true;
    }

    private static Hologram createHologram(Jackpot jackpot)
    {
        Hologram hologram = HologramsAPI.createHologram(Main.getInstance(), jackpot.getLocationHologram());

        hologram.appendTextLine("§l§nJACKPOT: " + Main.econ.format(jackpot.jackpotValue));
        hologram.appendTextLine("");
        hologram.appendTextLine("Try it now with the bet of " + Main.econ.format(jackpot.bet));

        return hologram;
    }

    public static void runJackpot(String name, Player player)
    {
        Jackpot jackpot = activeJackpots.get(name);
        if(jackpot == null) return;

        if(jackpot.isRunning)
        {
            player.sendMessage("jackpot is running");
            return;
        }

        new SimpleJackpotAnimation(jackpot, player).run();
    }
}
