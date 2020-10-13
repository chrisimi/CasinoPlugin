package com.chrisimi.casinoplugin.commands;

import com.chrisimi.casinoplugin.commands.playersigns.BlackjackHelpCommand;
import com.chrisimi.casinoplugin.commands.playersigns.DiceHelpCommand;
import com.chrisimi.casinoplugin.commands.playersigns.LeaderboardHelpCommand;
import com.chrisimi.casinoplugin.commands.playersigns.SlotsHelpCommand;
import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Commands;
import com.chrisimi.commands.CommandsAPI;
import com.chrisimi.commands.domain.MessageType;
import com.chrisimi.commands.domain.PermSystem;
import com.sun.istack.internal.NotNull;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandsControl
{
    public static CommandsAPI api;
    //init and manage the commands classes
    public static void init(@NotNull JavaPlugin plugin)
    {
        api = new Commands(plugin)
                .setBaseCommand(new BaseCommand())
                .generateHelpCommand(
                        new BlackjackHelpCommand(),
                        new DiceHelpCommand(),
                        new LeaderboardHelpCommand(),
                        new SlotsHelpCommand())
                .setPermissionSystem(PermSystem.VAULT)
                .setCustomMessage(MessageType.PREFIX, CasinoManager.getPrefix())
                .setCustomMessage(MessageType.NOT_ENOUGH_PERMISSION, "§4You don't have enough permission to use this command")
                .build(plugin, "casino");
    }
}
