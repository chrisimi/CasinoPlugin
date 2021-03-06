package com.chrisimi.casinoplugin.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import com.chrisimi.casinoplugin.menues.LeaderboardCreationMenu;
import com.chrisimi.casinoplugin.utils.Validator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.chrisimi.casinoplugin.animations.LeaderboardsignAnimation;
import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.serializables.Leaderboardsign;
import com.chrisimi.casinoplugin.serializables.PlayData;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import com.chrisimi.casinoplugin.serializables.Leaderboardsign.Cycle;
import com.chrisimi.casinoplugin.serializables.Leaderboardsign.Mode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class LeaderboardsignsManager implements Listener
{


    private static HashMap<Location, Leaderboardsign> leaderboardsigns = new HashMap<>();
    public static HashMap<Leaderboardsign, Integer> leaderboardsignRunnableTaskID = new HashMap<>();

    private static Gson gson;

    private static int reloadTime = 0;
    private static Boolean signsenable = false;

    public LeaderboardsignsManager()
    {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();

        reload(); //get variables from config.yml
        initialize();

    }

    /**
     * reload the all config variables from config.yml
     */
    public void reload()
    {
        try
        {
            reloadTime = Integer.valueOf(UpdateManager.getValue("leaderboard-signs-reload-time").toString());
        } catch (Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get leaderboardsign reloadtime! You have to use a valid integer value! Set to default value: 12000 (10 Minutes)");
            reloadTime = 12000;
        }
        try
        {
            signsenable = Boolean.valueOf(UpdateManager.getValue("leaderboard-signs-enable").toString());
        } catch (Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get leaderboard-signs enable! You have to use a boolean value (true/false)! Set to default value: true");
            signsenable = true;
        }

    }

    private void initialize()
    {
        if (signsenable)
        {
            importLeaderboardsigns();
        } else
            CasinoManager.LogWithColor(ChatColor.DARK_RED + "Leaderboard signs are disabled!");
        Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable()
        {

            @Override
            public void run()
            {
                exportLeaderboardsigns();
            }
        }, 20 * 60 * 5, 20 * 60 * 15);
    }

    //
    // export / import
    //

    //leaderboard signs
    public class LeaderboardList
    {
        @Expose
        public ArrayList<Leaderboardsign> list = new ArrayList<>();
    }

    private synchronized void importLeaderboardsigns()
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(Main.leaderboardSignsYml));

            StringBuilder jsonString = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null)
            {
                jsonString.append(line);
            }

            if (jsonString.length() < 9) return;
            LeaderboardList leaderboardsigns = gson.fromJson(jsonString.toString(), LeaderboardList.class);
            if (leaderboardsigns == null || leaderboardsigns.list == null)
            {
                CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to import all leaderboardsigns!");
                throw new Exception("leaderboard signs is null!");
            }
            for (Leaderboardsign sign : leaderboardsigns.list)
            {
                if (sign.cycleMode == null)
                    sign.cycleMode = Cycle.NaN;
                LeaderboardsignsManager.leaderboardsigns.put(sign.getLocation(), sign);
                try
                {
                    addSignAnimation(sign);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            if (CasinoManager.configEnableConsoleMessages)
                CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully imported all leaderboard signs! (" + leaderboardsigns.list.size() + ")");

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {

            try
            {
                reader.close();
            } catch (IOException e)
            {
                //nothing
            }
        }
    }

    private synchronized void exportLeaderboardsigns()
    {
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new FileWriter(Main.leaderboardSignsYml));
            LeaderboardList list = new LeaderboardList();
            list.list.addAll(leaderboardsigns.values());

            writer.write(gson.toJson(list));

            if (CasinoManager.configEnableConsoleMessages)
                CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully exported leaderboardsigns!");
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                writer.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    //
//	public methods
//
    public Boolean deleteLeaderbordsign(Leaderboardsign sign)
    {
        if (sign == null)
            return true;
        synchronized (leaderboardsigns)
        {
            Main.getInstance().getServer().getScheduler().cancelTask(leaderboardsignRunnableTaskID.remove(sign));
            return leaderboardsigns.remove(sign.getLocation()) != null;
        }
    }

    public void createLeaderboardSign(Player player, Sign sign, Mode mode, Boolean all, int count, int position, Cycle cycle)
    {
        Leaderboardsign leaderboardsign = new Leaderboardsign();
        leaderboardsign.setLocation(sign.getLocation());
        leaderboardsign.setMode(mode);

        if (player == null) //if leaderboard sign should be server leaderboard sign
            leaderboardsign.ownerUUID = "server";
        else
            leaderboardsign.setPlayer(player);
        leaderboardsign.setRange(all);
        leaderboardsign.setRange(count);
        leaderboardsign.position = position;
        leaderboardsign.cycleMode = cycle;
        leaderboardsigns.put(leaderboardsign.getLocation(), leaderboardsign);
        addSignAnimation(leaderboardsign);

        exportLeaderboardsigns();
    }

    public void addSignAnimation(Leaderboardsign sign)
    {
        Sign signBlock = null;
        try
        {
            signBlock = (Sign) sign.getLocation().getBlock().getState();
        } catch (Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.RED + "Leaderboardsign is not valid! (Block is not a sign)!");
            leaderboardsigns.remove(sign.getLocation());
            return;
        }
        addSignAnimation(sign, signBlock);
    }

    public void addSignAnimation(Leaderboardsign LBsign, Sign sign)
    {
        Random rnd = new Random();

        int randomWaitTime = rnd.nextInt(200);
        int taskID = Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new LeaderboardsignAnimation(LBsign, sign),
                (long) randomWaitTime, (long) reloadTime);
        if (taskID == -1)
        {
            CasinoManager.LogWithColor(ChatColor.RED + "Error occured while trying to animate sign!");
            return;
        }
        leaderboardsignRunnableTaskID.put(LBsign, taskID);
    }

    //
//	EventHandler and methods
//
    @EventHandler
    public void onSignPlace(SignChangeEvent event)
    {
        //check both possibilities
        if(Validator.is("casino", event.getLine(0)) && Validator.is("leaderboard", event.getLine(1)) ||
                Validator.is("leaderboard", event.getLine(0)))
        {
            if(Main.perm.has(event.getPlayer(), "casino.create.leaderboard"))
            {
                new LeaderboardCreationMenu(event.getBlock().getLocation(), event.getPlayer());
            }
            else
            {
                event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event)
    {
        if (leaderboardsigns.containsKey(event.getBlock().getLocation()))
        {
            checkIfSignIsLeaderboardSign(event);
        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event)
    {
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (!(event.getClickedBlock().getType().toString().contains("SIGN"))) return;

        Leaderboardsign lb = getLeaderboardsign(event.getClickedBlock().getLocation());
        if (lb == null) return;

        if(lb.isServerSign() && Main.perm.has(event.getPlayer(), "casino.create.serverleaderboard"))
        {
            new LeaderboardCreationMenu(lb, event.getPlayer());
        } else if(!lb.isServerSign() && lb.getPlayer().getUniqueId().equals(event.getPlayer().getUniqueId()) && Main.perm.has(event.getPlayer(), "casino.create.leaderboard"))
        {
            new LeaderboardCreationMenu(lb, event.getPlayer());
        } else
        {
            event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
        }


    }

    private void checkIfSignIsLeaderboardSign(BlockBreakEvent event)
    {
        Leaderboardsign sign = leaderboardsigns.get(event.getBlock().getLocation());
        if (sign == null) return;

        if (!(Main.perm.has(event.getPlayer(), "casino.admin")))
        {
            if (sign.isServerSign() && !(Main.perm.has(event.getPlayer(), "casino.create.serverleaderboard")))
            {
                event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
                event.setCancelled(true);
                return;
            } else if (!sign.isServerSign() && !(sign.getPlayer().getUniqueId().equals(event.getPlayer().getUniqueId())))
            {
                event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
                event.setCancelled(true);
                return;
            }
        }


        if (deleteLeaderbordsign(sign))
        {
            event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("leaderboard-delete_successful"));
        } else
        {
            event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("leaderboard-error_when_break"));
        }

        exportLeaderboardsigns();
    }

    /**
     * save all leaderboard signs and export them
     */
    public static void save()
    {
        CasinoManager.leaderboardManager.exportLeaderboardsigns();
        CasinoManager.leaderboardManager.reload();
    }


    public static void addLeaderboardSign(Leaderboardsign lb)
    {
        CasinoManager.leaderboardManager.deleteLeaderbordsign(leaderboardsigns.get(lb.getLocation()));

        leaderboardsigns.put(lb.getLocation(), lb);
        CasinoManager.leaderboardManager.addSignAnimation(lb);
        CasinoManager.leaderboardManager.exportLeaderboardsigns();
    }

    public static void reloadData()
    {
        //1. stop all runnables
        //2. reload data
        //3. start all leaderboardsigns

        //1. stop all runnables
        for (int taskID : leaderboardsignRunnableTaskID.values())
        {
            Main.getInstance().getServer().getScheduler().cancelTask(taskID);
        }
        leaderboardsignRunnableTaskID.clear();

        //2. reload data
        leaderboardsigns.clear();
        CasinoManager.leaderboardManager.importLeaderboardsigns();
    }

    public static void resetLeaderboard(Player player, Boolean allSigns, int range, Boolean allModes, Mode mode)
    {
        synchronized (leaderboardsigns)
        {
            if (allSigns)
            {
                for (Leaderboardsign leaderboardsign : leaderboardsigns.values())
                {
                    if (leaderboardsign == null) continue;

                    if (!leaderboardsign.isServerSign() && leaderboardsign.getPlayer().equals(player))
                    {
                        if (allModes)
                            leaderboardsign.lastManualReset = System.currentTimeMillis();
                        else if (leaderboardsign.getMode() == mode)
                            leaderboardsign.lastManualReset = System.currentTimeMillis();
                    }
                }
            } else
            {
                for (Leaderboardsign leaderboardsign : leaderboardsigns.values())
                {
                    if (leaderboardsign == null) continue;

                    if (!leaderboardsign.isServerSign() &&
                            leaderboardsign.getPlayer().equals(player))
                    {
                        if (player.getWorld().equals(leaderboardsign.getLocation().getWorld()) && (double) range > player.getLocation().distance(leaderboardsign.getLocation()))
                        {
                            if (allModes)
                                leaderboardsign.lastManualReset = System.currentTimeMillis();
                            else if (leaderboardsign.getMode() == mode)
                                leaderboardsign.lastManualReset = System.currentTimeMillis();
                        }
                    }
                }
            }
        }
        //save changes
        CasinoManager.leaderboardManager.exportLeaderboardsigns();
    }

    /**
     * Reset all ServerSigns
     *
     * @param allSigns if all Signs should be reseted
     * @param range    range in Blocks
     * @param allModes if every mode
     * @param mode     Mode
     */
    public static void resetServerLeaderboard(Player player, Boolean allSigns, int range, Boolean allModes, Mode mode)
    {
        synchronized (leaderboardsigns)
        {
            if (allSigns)
            {
                for (Leaderboardsign leaderboardsign : leaderboardsigns.values())
                {
                    if (leaderboardsign == null) continue;

                    if (leaderboardsign.isServerSign())
                    {
                        if (allModes)
                            leaderboardsign.lastManualReset = System.currentTimeMillis();
                        else if (leaderboardsign.getMode() == mode)
                            leaderboardsign.lastManualReset = System.currentTimeMillis();
                    }
                }
            } else
            {
                for (Leaderboardsign leaderboardsign : leaderboardsigns.values())
                {
                    if (leaderboardsign == null) continue;

                    if (leaderboardsign.isServerSign())
                    {
                        if (player.getWorld().equals(leaderboardsign.getLocation().getWorld()) && (double) range > player.getLocation().distance(leaderboardsign.getLocation()))
                        {
                            if (allModes)
                                leaderboardsign.lastManualReset = System.currentTimeMillis();
                            else if (leaderboardsign.getMode() == mode)
                                leaderboardsign.lastManualReset = System.currentTimeMillis();
                        }
                    }
                }
            }
        }
        //save changes
        CasinoManager.leaderboardManager.exportLeaderboardsigns();
    }

    /**
     * get a leaderboardsign
     *
     * @param lrc {@link Location} of the sign
     * @return the sign instance or null
     */
    public static Leaderboardsign getLeaderboardsign(Location lrc)
    {
        return leaderboardsigns.get(lrc);
    }

    public static void clearAllTasks()
    {
        for (int taskID : leaderboardsignRunnableTaskID.values())
        {
            Main.getInstance().getServer().getScheduler().cancelTask(taskID);
        }
    }
}
