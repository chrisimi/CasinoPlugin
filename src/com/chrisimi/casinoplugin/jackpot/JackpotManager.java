package com.chrisimi.casinoplugin.jackpot;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.UpdateManager;
import com.chrisimi.casinoplugin.serializables.Jackpot;
import com.chrisimi.casinoplugin.utils.Validator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.ChatColor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JackpotManager
{
    private static final HashMap<String, Jackpot> jackpotHashMap = new HashMap<>();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();

    public static double maxBet = -1.0;
    public static int[] spins = new int[] {10, 20};

    public JackpotManager()
    {
        init();
    }

    private void init()
    {
        importJackpots();
        importValues();
    }

    private void importValues()
    {
        try
        {
            maxBet = Double.parseDouble(UpdateManager.getValue("jackpot-max-bet", "-1.0").toString());
        }
        catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.DARK_RED + "ERROR while trying to get jackpot-max-bet: " + e.getMessage()
                    + ". Set to default value: -1");
            maxBet = -1.0;
        }

        try
        {
            List<Integer> a = (List<Integer>)UpdateManager.getValue("jackpot-spins");
            if(a.size() != 2) throw new Exception("There are not 2 symbols");

            spins = new int[2];
            for(int i = 0; i < 2; i++)
            {
                spins[i] = a.get(i);
            }

        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.DARK_RED + "ERROR while trying to get jackpot-spins: " + e.getMessage()
                    + ". Set to default value: [10, 20]");
        }
    }

    private synchronized void importJackpots()
    {
        //clear old jackpots to overwrite them
        jackpotHashMap.clear();

        BufferedReader rb;
        try
        {
            //read in data
            rb = new BufferedReader(new FileReader(Main.jackpotJson));

            StringBuilder sb = new StringBuilder();
            String line = "";
            while((line = rb.readLine()) != null)
                sb.append(line);

            //extract data
            if(sb.toString().length() <= 24) return;

            Jackpot.JackpotContainer container = gson.fromJson(sb.toString(), Jackpot.JackpotContainer.class);
            if(container == null)
            {
                CasinoManager.LogWithColor(ChatColor.DARK_RED + "ERROR while importing jackpot data");
                return;
            }

            for(Jackpot jackpot : container.jackpots)
            {
                if(!Validator.validateJackpot(jackpot))
                {
                    CasinoManager.LogWithColor(ChatColor.RED + "Error while import jackpot data... one data is not valid: " + jackpot.name);
                }

                //when there is the same jackpot... just rename it
                if(jackpotHashMap.containsKey(jackpot.name))
                {
                    CasinoManager.LogWithColor(ChatColor.YELLOW + "Error while importing jackpot data... two jackpots do have the same name... add a 1 at the end of the name");
                    jackpot.name += "1";
                }

                jackpotHashMap.put(jackpot.name, jackpot);
                JackpotSystem.initJackpot(jackpot);
            }

            if(CasinoManager.configEnableConsoleMessages)
                CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully imported " + container.jackpots.size() + " jackpots");

            rb.close();
        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.RED + "ERROR while trying to import jackpot data: " + e.getMessage());
            e.printStackTrace(CasinoManager.getPrintWriterForDebug());
        }
    }

    private synchronized void exportJackpots()
    {
        BufferedWriter bw;

        try
        {
            bw = new BufferedWriter(new FileWriter(Main.jackpotJson));

            Jackpot.JackpotContainer container = new Jackpot.JackpotContainer();
            container.jackpots = new ArrayList<>(jackpotHashMap.values());

            String json = gson.toJson(container);

            //overwrite old
            bw.write("");

            //write new
            bw.write(json);

            bw.close();

            if(CasinoManager.configEnableConsoleMessages)
                CasinoManager.LogWithColor(ChatColor.GREEN + "Successully exported " + container.jackpots.size() + " jackpots");

        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.RED + "ERROR while trying to export jackpot data: " + e.getMessage());
        }
    }

    /**
     * add a created jackpot to the system
     * @param jackpot jackpot instance
     * @return true if it was successful, false if not
     */
    public static boolean addJackpot(Jackpot jackpot)
    {
        if(jackpotHashMap.containsKey(jackpot.name) || !Validator.validateJackpot(jackpot))
            return false;

        jackpotHashMap.put(jackpot.name, jackpot);

        JackpotSystem.initJackpot(jackpot);

        //save
        CasinoManager.jackpotManager.exportJackpots();

        return true;
    }

    public static boolean updateJackpot(Jackpot jackpot)
    {
        jackpotHashMap.put(jackpot.name, jackpot);

        JackpotSystem.initJackpot(jackpot);

        CasinoManager.jackpotManager.exportJackpots();

        return true;
    }

    public static boolean removeJackpot(Jackpot jackpot)
    {
        if(JackpotSystem.deleteJackpot(jackpot.name))
        {
            jackpotHashMap.remove(jackpot.name);

            CasinoManager.jackpotManager.exportJackpots();

            return true;
        }

        return false;
    }

    public static void save()
    {
        CasinoManager.jackpotManager.exportJackpots();
    }

    public static Jackpot byName(String name)
    {
        return jackpotHashMap.get(name);
    }

    public static boolean doesNameExists(String name)
    {
        return jackpotHashMap.get(name) != null;
    }

    public static boolean validAmount(double amount)
    {
        if(maxBet == -1.0)
            return true;

        return amount < maxBet;
    }
}
