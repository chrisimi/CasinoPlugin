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
        yamlConfiguration = YamlConfiguration.loadConfiguration(Main.notificationsYml);
        importData();
    }

    private static synchronized void importData()
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
    }

    private static synchronized void export()
    {
        List<String> toExport = new ArrayList<>();

        for(UUID uuid : disabledNotifications)
            toExport.add(uuid.toString());

        yamlConfiguration.set("notification-disabled", toExport);
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

    public static void disableNotifications(OfflinePlayer player)
    {
        if(!hasNotificationsDisabled(player))
        {
            disabledNotifications.remove(player.getUniqueId());
            export();
        }
    }
}
