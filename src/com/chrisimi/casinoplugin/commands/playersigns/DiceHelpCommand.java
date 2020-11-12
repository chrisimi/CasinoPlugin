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
        if (Main.perm.has(player, "casino.create.dice")) player.sendMessage("§2permissions: §4true");
        else player.sendMessage("§2permissions: §4false");

        player.sendMessage("");
        player.sendMessage("§4NEW! §6try using the dice creation menu by only writing:");
        player.sendMessage("     §6line 1: §ecasino");
        player.sendMessage("     §6line 2: §edice\n");
    }
}
