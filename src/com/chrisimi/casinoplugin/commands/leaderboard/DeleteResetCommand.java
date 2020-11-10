package com.chrisimi.casinoplugin.commands.leaderboard;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.LeaderboardsignsManager;
import com.chrisimi.casinoplugin.serializables.Leaderboardsign;
import com.chrisimi.casinoplugin.utils.CommandUtils;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.PermissionType;
import com.chrisimi.commands.UsageType;
import org.bukkit.entity.Player;

/**
 * the command instance for command /casino deletereset
 */
public class DeleteResetCommand extends Command
{
    public DeleteResetCommand()
    {
        this.command = "deletereset";
        this.description = "delete the manual reset form a sign you are looking onto it";
        this.permissions = new String[] {"casino.create.leaderboard"};
        this.permissionType = PermissionType.OR;
        this.usageType = UsageType.PLAYER;
    }

    @Override
    public void execute(Event event)
    {
        Player player = event.getPlayer();
        Leaderboardsign sign = CommandUtils.getLeaderboardSignFromLookingOntoIt(event.getPlayer());
        if(sign == null)
            return;

        //check for enough permission
        if (sign.isServerSign() && (!Main.perm.has(player, "casino.admin") || !Main.perm.has(player, "casino.serversigns")))
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
            return;
        }

        if (!sign.isServerSign() && !sign.getPlayer().getUniqueId().equals(player.getUniqueId()))
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
            return;
        }

        //change values
        sign.lastManualReset = 0;
        sign.validUntil = 0;
        LeaderboardsignsManager.save();
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-leaderboard_delete_successful"));
    }
}
