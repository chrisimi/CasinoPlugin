package com.chrisimi.casinoplugin.commands.playersigns;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.UsageType;
import org.bukkit.entity.Player;

/**
 * the command instance for /casino help blackjack
 */
public class BlackjackHelpCommand extends Command
{
    public BlackjackHelpCommand()
    {
        this.command = "blackjack";
        this.description = "show help for placing blackjack signs!";
        this.usageType = UsageType.PLAYER;
    }

    @Override
    public void execute(Event event)
    {
        Player player = event.getPlayer();

        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("§f§l§lBlackjack help");
        if (Main.perm.has(player, "casino.create.blackjack")) player.sendMessage("§2permissions: §4true");
        else player.sendMessage("§2permissions: §4false");

        player.sendMessage("");
        player.sendMessage("§4NEW! §6try using the blackjack creation menu by only writing:");
        player.sendMessage("     §6line 1: §ecasino");
        player.sendMessage("     §6line 2: §eblackjack\n");
    }
}
