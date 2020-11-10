package com.chrisimi.casinoplugin.commands.playersigns;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.UsageType;
import org.bukkit.entity.Player;

/**
 * the command instance for /casino help leaderboard
 */
public class LeaderboardHelpCommand extends Command
{
    public LeaderboardHelpCommand()
    {
        this.command = "leaderboard";
        this.description = "show help for placing leaderboard signs!";
        this.usageType = UsageType.PLAYER;
    }

    @Override
    public void execute(Event event)
    {
        Player player = event.getPlayer();

        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("§f§lLeaderboardsign help");
        if (Main.perm.has(player, "casino.create.leaderboard")) player.sendMessage("§2permissions: §4true");
        else player.sendMessage("§2permissions: §4false");

        player.sendMessage("");
        player.sendMessage("§6§n§lFormat of a leaderboard sign:");
        player.sendMessage("");
        player.sendMessage("     §6line 1: §eleaderboard §6(§eleaderboard;s §6for using it as a server leaderboardsign§6) ");
        player.sendMessage("     §6line 2: §eposition§6;§ecycle §eposition §6like 1 for first place, §ecycle §6is optional like month data will be only taken from this month, (§eyear, month, week, day, hour§6)");
        player.sendMessage("     §6line 3: §emode §6(§ecount, sumamount, highestamount, highestloss, sumloss§6)");
        player.sendMessage("     §6line 4: §erange §6(§eall §6for all your signs, §enumber of blocks §6(3 as example) for using signs in this block range");
    }
}
