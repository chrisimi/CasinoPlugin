package com.chrisimi.casinoplugin.scripts;

import com.chrisimi.casinoplugin.main.Main;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotificationManager
{
    /**
     * which player has disabled notifications
     */
    private static List<UUID> disabledNotifications = new ArrayList<>();

    private static YamlConfiguration yamlConfiguration = null;

    public NotificationManager()
    {
        try
        {
            yamlConfiguration = YamlConfiguration.loadConfiguration(Main.notificationsYml);
        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.RED + "ERROR while trying to open notifcation.yml: " + e.getMessage());
            e.printStackTrace(CasinoManager.getPrintWriterForDebug());
        }

        importData();
    }

    private static synchronized void importData()
    {
        try
        {
            List<String> list = (List<String>) yamlConfiguration.getList("notification-disabled");

            for(String string : list)
            {
                try
                {
                    UUID uuid = UUID.fromString(string);
                    disabledNotifications.add(uuid);
                } catch(Exception e)
                {
                    CasinoManager.LogWithColor(ChatColor.RED + "One player UUID is not valid!");
                }
            }
        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.RED + "ERROR while trying to import notification settings: " + e.getMessage());
            e.printStackTrace(CasinoManager.getPrintWriterForDebug());
        }

    }

    private static synchronized void export()
    {
        try
        {
            List<String> toExport = new ArrayList<>();

            for(UUID uuid : disabledNotifications)
                toExport.add(uuid.toString());

            yamlConfiguration.set("notification-disabled", toExport);
        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to export the notification settings: " + e.getMessage());
            e.printStackTrace(CasinoManager.getPrintWriterForDebug());
        }

    }

    /**
     * check if the player has notifications disabled
     * @param player the player instance for whom to check
     * @return true if the player has disabled notifications
     */
    public static boolean hasNotificationsDisabled(OfflinePlayer player)
    {
        return disabledNotifications.contains(player.getUniqueId());
    }

    /**
     * Enable notifications for the player
     * @param player the player for whom to activate the notifications
     */
    public static void enableNotifications(OfflinePlayer player)
    {
        if(hasNotificationsDisabled(player))
        {
            disabledNotifications.add(player.getUniqueId());
            export();
        }
    }

    /**
     * disable notifications for the player
     * @param player the player for whom to deactivate the notifications
     */
    public static void disableNotifications(OfflinePlayer player)
    {
        if(!hasNotificationsDisabled(player))
        {
            disabledNotifications.remove(player.getUniqueId());
            export();
        }
    }
}
