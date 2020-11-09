package com.chrisimi.casinoplugin.scripts;

import com.chrisimi.casinoplugin.database.FileDataBase;
import com.chrisimi.casinoplugin.database.IDataBase;
import com.chrisimi.casinoplugin.database.MySQLDataBase;
import com.chrisimi.casinoplugin.main.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;

public class DataManager
{
    public enum DBMode
    {
        MYSQL,
        FILE
    }
    private static DataManager _instance;
    public static DBMode dbMode;
    public static IDataBase dataBase;

    public static DataManager getInstance()
    {
        if(_instance == null)
            _instance = new DataManager();

        return _instance;
    }

    private DataManager()
    {
        initialize();
    }

    //init database/file
    private void initialize()
    {
        try
        {
            String data = UpdateManager.getValue("connectiontype", "file").toString();
            if(data.equalsIgnoreCase("file"))
                dataBase = new FileDataBase();
            else if(data.equalsIgnoreCase("mysql"))
                dataBase = new MySQLDataBase();
            else
                throw new Exception("no valid connection type");

        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.DARK_RED + "ERROR while trying to get connectiontype: " + e.getMessage()
                    + ". Using file system now");
            dataBase = new FileDataBase();
        }

        dataBase.init();
    }

    public void resetData(Player player)
    {
        dataBase.reset();
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-admin_successfully_reset_data"));
    }
}
