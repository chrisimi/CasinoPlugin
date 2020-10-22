package com.chrisimi.casinoplugin.commands.admin;

import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.PermissionType;
import com.chrisimi.commands.UsageType;
import org.bukkit.entity.Player;

/**
 * the command instance for /casino admin
 */
public class AdminHelpCommand extends Command
{
    public AdminHelpCommand()
    {
        this.command = "admin";
        this.description = "show commands for admins";
        this.permissions = new String[] {"casino.admin"};
        this.permissionType = PermissionType.OR;
        this.usageType = UsageType.PLAYER;
    }

    @Override
    public void execute(Event event)
    {
        Player player = event.getPlayer();

        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("§4Admin page");
        player.sendMessage("§6/casino reloadconfig §8- reloads the config.yml");
        player.sendMessage("§6/casino resetdata §8- deletes all roll-data from player-managed-signs (data.yml)");
        player.sendMessage("§6/casino reloaddata §8- reload all leaderboard signs and data.yml. Could lag a bit!");
        player.sendMessage("§6/casino resetsign §8- reset the sign your are look onto it. Use it when the sign is bugging");
    }
}
