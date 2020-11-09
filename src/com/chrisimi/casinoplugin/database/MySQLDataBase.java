package com.chrisimi.casinoplugin.database;

import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.PlayerSignsManager;
import com.chrisimi.casinoplugin.scripts.UpdateManager;
import com.chrisimi.casinoplugin.serializables.PlayData;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class MySQLDataBase implements IDataBase
{
    private Connection connection = null;

    @Override
    public void init()
    {
        try
        {
            String ip = UpdateManager.getValue("mysql-ip", "localhost").toString();
            String port = UpdateManager.getValue("mysql-port", "3306").toString();
            String database = UpdateManager.getValue("mysql-database", "test").toString();
            String user = UpdateManager.getValue("mysql-user", "root").toString();
            String password = UpdateManager.getValue("mysql-password", "").toString();


            Class.forName("com.mysql.jdbc.Driver");

            connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s", ip, port, database), user, password);

            String createTableIfNotExists = "CREATE TABLE IF NOT EXISTS playdatas (" +
                    "player char(36), " +
                    "world text, " +
                    "location text, " +
                    "playamount int, " +
                    "wonamount int, " +
                    "timestamp SIGNED BIGINT);";

            ExecuteNonQuery(createTableIfNotExists, null);

            CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully connected to the mysql database");

        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.DARK_RED + "ERROR while trying to establish connection to mysql database: " + e.getMessage());
            e.printStackTrace(CasinoManager.getPrintWriterForDebug());
        }
    }

    @Override
    public boolean isOnline()
    {
        try
        {
            return connection.isValid(3000);
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }

        return false;
    }

    @Override
    public List<PlayData> getPlayData(OfflinePlayer player)
    {
        return getPlayData(player, 0, new GregorianCalendar(2100, 1, 1).getTimeInMillis());
    }

    @Override
    public List<PlayData> getPlayData(OfflinePlayer player, Calendar fromDate, Calendar toDate)
    {
        return getPlayData(player, fromDate.getTimeInMillis(), toDate.getTimeInMillis());
    }

    @Override
    public List<PlayData> getPlayData(OfflinePlayer player, long fromMillis, long toMillis)
    {
        List<Map.Entry<String, String>> signs = getLocationsFromPlayerSigns(player);

        List<PlayData> result = new ArrayList<>();

        for(Map.Entry<String, String> entry : signs)
        {
            String sql = "SELECT * FROM playdatas " +
                    "WHERE world = ? AND location = ? AND timestamp >= ? AND timestamp <= ?";
            List<Object> mapping = new ArrayList<>();
            mapping.add(entry.getKey());
            mapping.add(entry.getValue());
            mapping.add(fromMillis);
            mapping.add(toMillis);

            result.addAll(ExecuteQuery(sql, mapping, PlayData.class));
        }

        return result;
    }

    @Override
    public List<PlayData> getPlayData(Calendar fromDate, Calendar toDate)
    {
        return getPlayData(fromDate.getTimeInMillis(), toDate.getTimeInMillis());
    }

    @Override
    public List<PlayData> getPlayData(long fromMillis, long toMillis)
    {
        List<PlayData> result = new ArrayList<>();
        List<Map.Entry<String, String>> locations = getLocationsFromServerSigns();

        for(Map.Entry<String, String> entry : locations)
        {
            String sql = "SELECT * FROM playdatas " +
                    "WHERE world = ? AND location = ? AND timestamp >= ? AND timestamp <= ?";
            List<Object> mapping = new ArrayList<>();
            mapping.add(entry.getKey());
            mapping.add(entry.getValue());
            mapping.add(fromMillis);
            mapping.add(toMillis);

            result.addAll(ExecuteQuery(sql, mapping, PlayData.class));
        }

        return result;
    }

    @Override
    public void addData(Player player, PlayerSignsConfiguration psc, double playAmount, double winAmount)
    {
        String sql = "INSERT INTO playdatas(player, world, location, playamount, wonamount, timestamp) " +
                "VALUES(?, ?, ?, ?, ?, ?)";
        List<Object> mapping = new ArrayList<>();
        mapping.add(player.getUniqueId().toString());
        mapping.add(psc.getLocation().getWorld().getName());
        mapping.add(String.format("%s,%s,%s", psc.getLocation().getBlockX(), psc.getLocation().getBlockY(), psc.getLocation().getBlockZ()));
        mapping.add(playAmount);
        mapping.add(winAmount);
        mapping.add(new GregorianCalendar().getTimeInMillis());

        ExecuteNonQuery(sql, mapping);
    }

    public synchronized int ExecuteNonQuery(String sql, List<Object> mapping) {

        if(!(isOnline())) return 0;


        try {
            PreparedStatement statement = connection.prepareStatement(sql);

            if(mapping != null) {
                int index = 1;
                for(Object object : mapping) {
                    statement.setObject(index, object);
                    index++;
                }
            }
            //BankManager.Debug(this.getClass(), statement.toString());
            //log(statement.toString());


            return statement.executeUpdate();

        } catch (SQLException e) {
            //BankManager.Debug(this.getClass(), e.getMessage());
            return 0;
        }
    }
    @SuppressWarnings("unchecked")
    public synchronized <E extends Object> ArrayList<E> ExecuteQuery(String sql, List<Object> mapping, Class<E> clas) {



        ArrayList<E> returnValue = new ArrayList<>();
        if(!(isOnline())) return returnValue;

        try {
            PreparedStatement statement = connection.prepareStatement(sql);

            if(mapping != null) {
                int index = 1;
                for(Object object : mapping) {
                    statement.setString(index, object.toString());
                }
            }

            ResultSet resultSet = statement.executeQuery();

            //BankManager.Debug(this.getClass(), statement.toString());
            //log(statement.toString());


            Constructor<E> constructor = (Constructor<E>) clas.getConstructor();


            while(resultSet.next())
            {
                E object = constructor.newInstance();

                Field[] fields = object.getClass().getFields();
                for(int i = 0; i < fields.length; i++)
                {
                    if(fields[i].getType().isEnum())
                    {
                        fields[i].set(object, Enum.valueOf((Class<Enum>) fields[i].getType(), resultSet.getString(i+1)));

                        //fields[i].set(object, resultSet.getObject(i+1));

                    }
                    else if(fields[i].getName().equals("PlayAmount") || fields[i].getName().equals("WinAmount"))
                    {
                        fields[i].set(object, (double) Integer.parseInt(resultSet.getObject(i + 1).toString()) / 100.0);
                    }
                    else
                    {
                        fields[i].set(object, resultSet.getObject(i+1));
                    }
                }

                returnValue.add(object);
            }


            return returnValue;

        } catch(Exception e)
        {
            //BankManager.Debug(this.getClass(), e.getMessage());
            return returnValue;
        }
    }

    /**
     *
     * @param player
     * @return a list of all locations where the key is the worldname and the value is the location splited by ',' x,y,z
     */
    private List<Map.Entry<String, String>> getLocationsFromPlayerSigns(OfflinePlayer player)
    {
        ArrayList<Location> locations = PlayerSignsManager.getLocationsFromAllPlayerSigns(player);

        return serialize(locations);
    }

    private List<Map.Entry<String, String>> getLocationsFromServerSigns()
    {
        ArrayList<Location> locations = PlayerSignsManager.getLocationsFromAllServerSigns();

        return serialize(locations);
    }

    private List<Map.Entry<String, String>> serialize(ArrayList<Location> locations)
    {
        List<Map.Entry<String, String>> result = new ArrayList<>();
        for(Location location : locations)
        {
            result.add(new AbstractMap.SimpleEntry<String, String>(location.getWorld().getName(), String.format("%s,%s,%s", location.getBlockX(), location.getBlockY(), location.getBlockZ())));
        }

        return result;
    }
}
