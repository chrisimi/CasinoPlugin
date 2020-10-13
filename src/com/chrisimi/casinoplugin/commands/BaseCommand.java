package com.chrisimi.casinoplugin.commands;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.UsageType;
import org.bukkit.entity.Player;

public class BaseCommand extends Command
{
    public BaseCommand()
    {
        this.command = "casino";
        this.usageType = UsageType.PLAYER;
    }

    @Override
    public void execute(Event event)
    {
        Player player = event.getPlayer();

        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("§2CasinoPlugin Version " + Main.result.getLocalPluginVersion() + " by chrisimi");
        player.sendMessage("§6use §e/casino help §6to find information about the commands");
    }
}
