package com.chrisimi.casinoplugin.commands.playersigns;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.UsageType;
import org.bukkit.entity.Player;

/**
 * the command instance for /casino help dice
 */
public class DiceHelpCommand extends Command
{
    public DiceHelpCommand()
    {
        this.command = "dice";
        this.description = "show help for placing dice signs!";
        this.usageType = UsageType.PLAYER;
    }

    @Override
    public void execute(Event event)
    {
        Player player = event.getPlayer();

        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("§f§l§nDice help");
        if (Main.perm.has(player, "casino.dice.create")) player.sendMessage("§2permissions: §4true");
        else player.sendMessage("§2permissions: §4false");

        player.sendMessage("§6§n§lFormat of a dice sign:");
        player.sendMessage("");
        player.sendMessage("     §6line 1: §ecasino §6(§ecasino;server §6for creating a server dice sign)");
        player.sendMessage("     §6line 2: §edice");
        player.sendMessage("     §6line 3: §ebet §6like 30 or 20.5");
        player.sendMessage("     §6line 4: §ewin chance §6and §emultiplicator §6like 1-40;3 (the player wins if he draws between 1-40 and get bet*3)");
    }
}
