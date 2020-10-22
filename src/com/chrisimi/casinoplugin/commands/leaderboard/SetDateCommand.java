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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * the command instance for the command /casino setdate
 */
public class SetDateCommand extends Command
{
    public SetDateCommand()
    {
        this.command = "setdate";
        this.description = "set a date until the leaderboard will count data (valid date format in numeric and without pm or am: day-month-year hour:minute) while looking onto it";
        this.permissions = new String[] {"casino.admin", "casino.serversigns", "casino.leaderboard.create"};
        this.permissionType = PermissionType.OR;
        this.enableArguments = true;
        this.usageType = UsageType.PLAYER;
    }

    @Override
    public void execute(Event event)
    {
        if(event.getArgs().length < 1)
            return;

        Player player = event.getPlayer();
        String dateString = event.getArgs()[0];

        //set the date formats
        DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        DateFormat adf = new SimpleDateFormat("MM-dd-yyyy h:mm a");

        //try to parse dates
        Date date = null;
        try
        {
            date = sdf.parse(dateString);
        } catch (ParseException e)
        {
            try
            {
                date = adf.parse(dateString);
            } catch (Exception e2)
            {
                player.sendMessage(MessageManager.get("commands-setdate_invalid_format"));
                return;
            }
        }
        Leaderboardsign sign = CommandUtils.getLeaderboardSignFromLookingOntoIt(player);
        if (sign == null) return;

        //check permission for different cases
        if (sign.isServerSign() && !(Main.perm.has(player, "casino.admin") || Main.perm.has(player, "casino.serversigns")))
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
            return;
        } else if (!sign.isServerSign() && !(sign.getPlayer().getUniqueId().equals(player.getUniqueId())))
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
            return;
        }

        //set the values
        sign.validUntil = date.getTime();
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-setdate_successful"));
        LeaderboardsignsManager.save();
    }
}
