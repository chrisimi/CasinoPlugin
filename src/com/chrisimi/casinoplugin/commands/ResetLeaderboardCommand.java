package com.chrisimi.casinoplugin.commands;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.LeaderboardsignsManager;
import com.chrisimi.casinoplugin.serializables.Leaderboardsign;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.PermissionType;
import com.chrisimi.commands.UsageType;
import org.bukkit.entity.Player;

/**
 * the command instance for commmand /casino resetleaderboard
 */
public class ResetLeaderboardCommand extends Command
{
    public ResetLeaderboardCommand()
    {
        this.command = "resetleaderboard";
        this.description = "reset all leaderboards in range (blocks) or only with a specific mode [sumamount, highestamount, sumloss, highestloss, count]";
        this.permissions = new String[] {"casino.admin", "casino.serversigns", "casino.leaderboard.create"};
        this.permissionType = PermissionType.OR;
        this.enableArguments = true;
        this.usageType = UsageType.PLAYER;
    }
    @Override
    public void execute(Event event)
    {
        if(event.getArgs().length == 0)
            return; //no arguments

        Player player = event.getPlayer();
        int rangeBlocks = 0; //-1 is for unlimited
        Leaderboardsign.Mode chosenMode = null; //null for no filtering

        //check the range
        if(event.getArgs().length >= 1 && !event.getArgs()[0].contains("all"))
        {
            try
            {
                rangeBlocks = Integer.parseInt(event.getArgs()[0]);
            } catch(Exception e)
            {
                player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_reset_leaderboard_invalid"));
                return;
            }
        }
        else if(event.getArgs().length >= 1 && event.getArgs()[0].contains("all"))
        {
            rangeBlocks = -1; //set to unlimited
        }

        //check the mode
        if(event.getArgs().length >= 2)
        {
            for(Leaderboardsign.Mode mode : Leaderboardsign.Mode.values())
            {
                if(mode.toString().equalsIgnoreCase(event.getArgs()[1]))
                    chosenMode = mode;
            }
        }

        //finish command
        LeaderboardsignsManager.resetLeaderboard(player, rangeBlocks == -1, rangeBlocks, chosenMode == null, chosenMode);
    }
}
