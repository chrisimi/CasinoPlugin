package com.chrisimi.casinoplugin.commands.playersigns;

import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.UsageType;
import org.bukkit.entity.Player;

public class SlotsHelpCommand extends Command
{
    public SlotsHelpCommand()
    {
        this.command = "slots";
        this.description = "show help for placing slots signs!";
        this.usageType = UsageType.PLAYER;
    }
    @Override
    public void execute(Event event)
    {
        Player player = event.getPlayer();

        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("§f§lCasino-Slots sign help");

        player.sendMessage("");
        player.sendMessage("§6§n§lFormat of a Slots sign:");
        player.sendMessage("");
        player.sendMessage("    §6line 1: §eslots §6or §eslots;server §6for server sign");
        player.sendMessage("    §6line 2: §ebet §6in decimal like §e10.0");
        player.sendMessage("    §6line 3: 3 symbols splited by ';' a semicolon like §eA;B;C");
        player.sendMessage("    §6line 4: §echances and multiplicators §6in that format: ");
        player.sendMessage("    §6        §echance1-chance2-chance3;multiplicator1-multiplicator2-multiplicator3 §6(1 is for A, 2 is for B and 3 is for C in that example)");
        player.sendMessage("    §6example:   50-30-20;2-3-5");
    }
}
