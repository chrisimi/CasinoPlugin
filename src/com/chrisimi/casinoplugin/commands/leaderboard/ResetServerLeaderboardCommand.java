package com.chrisimi.casinoplugin.commands.leaderboard;

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
 * the command instance for commmand /casino resetserverleaderboard
 */
public class ResetServerLeaderboardCommand extends Command
{
    public ResetServerLeaderboardCommand()
    {
        this.command = "resetserverleaderboard";
        this.description = "Resets all server leaderboards in range (blocks) or only with a specific mode [sumamount, highestamount, sumloss, highestloss, count]";
        this.argumentsDescription = "[range in blocks/all] [(optional) mode]";
        this.permissions = new String[] {"casino.create.serverleaderboard"};
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
        LeaderboardsignsManager.resetServerLeaderboard(player, rangeBlocks == -1, rangeBlocks, chosenMode == null, chosenMode);
    }
}

