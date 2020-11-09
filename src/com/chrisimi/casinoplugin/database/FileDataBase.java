package com.chrisimi.casinoplugin.database;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.LeaderboardsignsManager;
import com.chrisimi.casinoplugin.scripts.PlayerSignsManager;
import com.chrisimi.casinoplugin.serializables.PlayData;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FileDataBase implements IDataBase
{
    private final ArrayList<PlayData> playdatas = new ArrayList<>();

    @Override
    public void init()
    {
        importData();
    }

    private synchronized void importData()
    {

        BufferedReader reader = null;
        int row = 0;
        try
        {
            reader = new BufferedReader(new FileReader(Main.dataYml));
            String line = "";
            while ((line = reader.readLine()) != null)
            {
                row++;

                String[] splited = line.split(";");
                if (splited.length != 6)
                {
                    CasinoManager.LogWithColor(ChatColor.RED + "data value is invalid! length is not 6! line will be deleted! Row: " + row);
                    continue;
                } else if (splited[2].split(",").length != 3)
                {
                    CasinoManager.LogWithColor(ChatColor.RED + "location is invalid!");
                    continue;
                } else if (Bukkit.getWorld(splited[1]) == null)
                {
                    CasinoManager.LogWithColor(ChatColor.RED + "worldname is invalid!");
                    continue;
                }
                PlayData data = getPlayData(splited);
                if (data != null && !playdatas.contains(data))
                {
                    playdatas.add(data);
                }

            }

            if (CasinoManager.configEnableConsoleMessages)
                CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully imported " + playdatas.size() + " data-packets!");

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                reader.close();

            } catch (Exception e)
            {
                //nothing
            }
        }
    }

    private synchronized void exportData()
    {
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new FileWriter(Main.dataYml));
            writer.write("");
            for (PlayData data : playdatas)
            {
                writer.append(getStringFromPlayData(data) + "\n");
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                writer.close();
            } catch (Exception e)
            {
                //nothing
            }
        }
    }

    @Override
    public boolean isOnline()
    {
        return playdatas.size() > 0 || Main.dataYml.exists();
    }

    @Override
    public List<PlayData> getPlayData(OfflinePlayer player)
    {
        List<PlayData> dataList = new ArrayList<>();
        ArrayList<Location> locationOfSignsFromPlayer = PlayerSignsManager.getLocationsFromAllPlayerSigns(player);
        CasinoManager.Debug(LeaderboardsignsManager.class, "total datasets: " + playdatas.size());

        synchronized (playdatas)
        {
            dataList = playdatas.stream()
                    .filter(a -> locationOfSignsFromPlayer.contains(a.Location))
                    .collect(Collectors.toList());
        }
        return dataList;
    }

    @Override
    public List<PlayData> getPlayData(OfflinePlayer player, Calendar fromDate, Calendar toDate)
    {
        return getPlayData(player, fromDate.getTimeInMillis(), toDate.getTimeInMillis());
    }

    @Override
    public List<PlayData> getPlayData(OfflinePlayer player, long fromMillis, long toMillis)
    {
        List<PlayData> dataList = new ArrayList<>();
        ArrayList<Location> locationsOfSignsFromPlayer = PlayerSignsManager.getLocationsFromAllPlayerSigns(player);
        CasinoManager.Debug(LeaderboardsignsManager.class, "total datasets: " + playdatas.size());

        synchronized (playdatas)
        {
            dataList = playdatas.stream()
                    .filter(a -> locationsOfSignsFromPlayer.contains(a.Location) && a.Timestamp >= fromMillis && a.Timestamp <= toMillis)
                    .collect(Collectors.toList());
        }
        return dataList;
    }

    @Override
    public List<PlayData> getPlayData(Calendar fromDate, Calendar toDate)
    {
        return getPlayData(fromDate.getTimeInMillis(), toDate.getTimeInMillis());
    }

    @Override
    public List<PlayData> getPlayData(long fromMillis, long toMillis)
    {
        List<PlayData> dataList = new ArrayList<>();
        ArrayList<Location> locationsOfSignsFromPlayer = PlayerSignsManager.getLocationsFromAllServerSigns();
        CasinoManager.Debug(LeaderboardsignsManager.class, "total datasets: " + playdatas.size());

        synchronized (playdatas)
        {
            dataList = playdatas.stream()
                    .filter(a -> locationsOfSignsFromPlayer.contains(a.Location) && a.Timestamp >= fromMillis && a.Timestamp <= toMillis)
                    .collect(Collectors.toList());
        }
        return dataList;
    }

    @Override
    public void addData(Player player, PlayerSignsConfiguration psc, double playAmount, double winAmount)
    {
        PlayData data = new PlayData();
        data.Player = player;
        data.World = psc.getLocation().getWorld();
        data.Location = psc.getLocation();
        data.PlayAmount = playAmount;
        data.WonAmount = winAmount;
        data.Timestamp = System.currentTimeMillis();

        playdatas.add(data);
        exportData();
    }

    private PlayData getPlayData(String[] data)
    {
        PlayData playData = new PlayData();
        try
        {
            playData.Player = Bukkit.getOfflinePlayer(UUID.fromString(data[0]));
            playData.World = Bukkit.getWorld(data[1]);
            String[] locationSplit = data[2].split(",");
            playData.Location = new Location(playData.World, Integer.parseInt(locationSplit[0]), Integer.parseInt(locationSplit[1]), Integer.parseInt(locationSplit[2]));
            playData.PlayAmount = Double.parseDouble(data[3]);
            playData.WonAmount = Double.parseDouble(data[4]);
            playData.Timestamp = Long.parseLong(data[5]);
        } catch (NumberFormatException e)
        {
            CasinoManager.LogWithColor(ChatColor.RED + "error at converting data!");
            playData = null;
        }
        return playData;
    }

    private String getStringFromPlayData(PlayData data)
    {
        String[] splited = new String[6];
        splited[0] = data.Player.getUniqueId().toString();
        splited[1] = data.World.getName();
        String[] locationSplit = new String[3];
        locationSplit[0] = String.valueOf(data.Location.getBlockX());
        locationSplit[1] = String.valueOf(data.Location.getBlockY());
        locationSplit[2] = String.valueOf(data.Location.getBlockZ());
        splited[2] = String.join(",", locationSplit);
        splited[3] = String.valueOf(data.PlayAmount);
        splited[4] = String.valueOf(data.WonAmount);
        splited[5] = String.valueOf(data.Timestamp);
        return String.join(";", splited);
    }
}
