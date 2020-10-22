package com.chrisimi.casinoplugin.commands;

import com.chrisimi.casinoplugin.commands.admin.*;
import com.chrisimi.casinoplugin.commands.holograms.CreateHologramCommand;
import com.chrisimi.casinoplugin.commands.holograms.EditHologramCommand;
import com.chrisimi.casinoplugin.commands.holograms.MyHologramsCommand;
import com.chrisimi.casinoplugin.commands.leaderboard.DeleteResetCommand;
import com.chrisimi.casinoplugin.commands.leaderboard.ResetLeaderboardCommand;
import com.chrisimi.casinoplugin.commands.leaderboard.ResetServerLeaderboardCommand;
import com.chrisimi.casinoplugin.commands.leaderboard.SetDateCommand;
import com.chrisimi.casinoplugin.commands.playersigns.BlackjackHelpCommand;
import com.chrisimi.casinoplugin.commands.playersigns.DiceHelpCommand;
import com.chrisimi.casinoplugin.commands.playersigns.LeaderboardHelpCommand;
import com.chrisimi.casinoplugin.commands.playersigns.SlotsHelpCommand;
import com.chrisimi.casinoplugin.commands.slotchests.ChestLocationsCommand;
import com.chrisimi.casinoplugin.commands.slotchests.CreateChestCommand;
import com.chrisimi.casinoplugin.commands.slotchests.CreateServerChestCommand;
import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Commands;
import com.chrisimi.commands.CommandsAPI;
import com.chrisimi.commands.domain.MessageType;
import com.chrisimi.commands.domain.PermSystem;
import com.sun.istack.internal.NotNull;
import com.sun.org.glassfish.external.statistics.annotations.Reset;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandsControl
{
    public static CommandsAPI api;
    //init and manage the commands classes
    public static void init(@NotNull JavaPlugin plugin)
    {
        api = new Commands(plugin)
                .setBaseCommand(new BaseCommand())

                //admin package
                .addCommand(new AdminHelpCommand())
                .addCommand(new ReloadConfigCommand())
                .addCommand(new ReloadDataCommand())
                .addCommand(new ResetDataCommand())
                .addCommand(new ResetSignCommand())

                //holograms package
                .addCommand(new CreateHologramCommand())
                .addCommand(new EditHologramCommand())
                .addCommand(new MyHologramsCommand())

                //leaderboard package
                .addCommand(new DeleteResetCommand())
                .addCommand(new ResetLeaderboardCommand())
                .addCommand(new ResetServerLeaderboardCommand())
                .addCommand(new SetDateCommand())

                //playersigns package
                .addCommand(new BlackjackHelpCommand())
                .addCommand(new DiceHelpCommand())
                .addCommand(new LeaderboardHelpCommand())
                .addCommand(new SlotsHelpCommand())

                //slotchests package
                .addCommand(new ChestLocationsCommand())
                .addCommand(new CreateChestCommand())
                .addCommand(new CreateServerChestCommand())

                //rest
                .addCommand(new RollCommand())
                .addCommand(new SignCommand())
                .addCommand(new SlotsCommand())
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
