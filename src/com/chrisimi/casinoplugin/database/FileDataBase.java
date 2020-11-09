package com.chrisimi.casinoplugin.database;

import com.chrisimi.casinoplugin.serializables.PlayData;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.List;

public class FileDataBase implements IDataBase
{
    @Override
    public void init()
    {

    }

    @Override
    public boolean isOnline()
    {
        return false;
    }

    @Override
    public List<PlayData> getPlayData(OfflinePlayer player)
    {
        return null;
    }

    @Override
    public List<PlayData> getPlayData(OfflinePlayer player, Calendar fromDate, Calendar toDate)
    {
        return null;
    }

    @Override
    public List<PlayData> getPlayData(OfflinePlayer player, long fromMillis, long toMillis)
    {
        return null;
    }

    @Override
    public List<PlayData> getPlayData(Calendar fromDate, Calendar toDate)
    {
        return null;
    }

    @Override
    public List<PlayData> getPlayData(long fromMillis, long toMillis)
    {
        return null;
    }

    @Override
    public void addData(Player player, PlayerSignsConfiguration psc, double playAmount, double winAmount)
    {

    }
}
