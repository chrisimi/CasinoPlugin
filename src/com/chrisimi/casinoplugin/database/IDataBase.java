package com.chrisimi.casinoplugin.database;

import com.chrisimi.casinoplugin.serializables.PlayData;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.List;

public interface IDataBase
{
    /**
     * will be called when the database should be initiated
     */
    void init();

    /**
     * check if the database is online
     * @return true if there is a valid connection to the database, false if not
     */
    boolean isOnline();

    /**
     * reset all data!!!
     */
    void reset();

    //region player managed signs
    List<PlayData> getPlayData(OfflinePlayer player);

    List<PlayData> getPlayData(OfflinePlayer player, Calendar fromDate, Calendar toDate);

    //the main getPlaymethod
    List<PlayData> getPlayData(OfflinePlayer player, long fromMillis, long toMillis);

    //endregion

    //region server managed signs
    List<PlayData> getPlayData(Calendar fromDate, Calendar toDate);

    List<PlayData> getPlayData(long fromMillis, long toMillis);

    //endregion
    List<PlayData> getPlayData(Location signLrc);


    void addData(Player player, PlayerSignsConfiguration psc, double playAmount, double winAmount);
}
