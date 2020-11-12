package com.chrisimi.casinoplugin.scripts;

import com.chrisimi.casinoplugin.database.FileDataBase;
import com.chrisimi.casinoplugin.database.IDataBase;
import com.chrisimi.casinoplugin.database.MySQLDataBase;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.List;

public class DataManager
{

    //TODO Add method /casino exportdata <location(mysql, file> moves all the data to the given database type and overwrite it

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
            {
                dataBase = new FileDataBase();
                dbMode = DBMode.FILE;
            }
            else if(data.equalsIgnoreCase("mysql"))
            {
                dataBase = new MySQLDataBase();
                dbMode = DBMode.MYSQL;
            }
            else
                throw new Exception("no valid connection type");

        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.DARK_RED + "ERROR while trying to get connectiontype: " + e.getMessage()
                    + ". Using file system now");
            dataBase = new FileDataBase();
            dbMode = DBMode.FILE;
        }

        dataBase.init();
    }

    /**
     * export data from a database to another database
     * @param fromDatabase fromdatabase
     * @param toDatabase to the database
     * @param overwrite overwrite data
     */
    public boolean exportData(DBMode fromDatabase, DBMode toDatabase, boolean overwrite)
    {
        //check if both databases are online and correct setup
        IDataBase fromDB = null;
        IDataBase toDB = null;

        if(fromDatabase != dbMode && fromDatabase == DBMode.MYSQL)
            fromDB = new MySQLDataBase();
        else if(fromDatabase != dbMode && fromDatabase == DBMode.FILE)
            fromDB = new FileDataBase();
        else if(fromDatabase == dbMode)
            fromDB = dataBase;

        if(toDatabase != dbMode && toDatabase == DBMode.MYSQL)
            toDB = new MySQLDataBase();
        else if(toDatabase != dbMode && toDatabase == DBMode.FILE)
            toDB = new FileDataBase();
        else if(toDatabase == dbMode)
            toDB = dataBase;

        fromDB.init();
        toDB.init();

        if(fromDB.isOnline() && toDB.isOnline())
            return exportData(fromDB, toDB, overwrite);
        else
            return false;
    }

    private boolean exportData(IDataBase fromDB, IDataBase toDB, boolean overwrite)
    {
        if(overwrite)
            toDB.reset();

        List<Location> locationList = PlayerSignsManager.getLocationsOfAllSigns();

        CasinoManager.LogWithColor(ChatColor.BLUE + "Initialize datatransfer of " + locationList + " signs");

        return false;
    }

    public void resetData()
    {
        dataBase.reset();
    }
}
