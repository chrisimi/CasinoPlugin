package com.chrisimi.casinoplugin.commands;

import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import com.chrisimi.casinoplugin.utils.CommandUtils;
import com.chrisimi.casinoplugin.utils.Validator;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.PermissionType;
import org.bukkit.entity.Player;

/**
 * the command instance for the command /casino sign enable
 */
public class SignEnableCommand extends Command
{
    public SignEnableCommand()
    {
        this.command = "enable";
        this.description = "enable your player sign while looking onto it";
        this.permissions = new String[] {"casino.dice.create", "casino.admin", "casino.serversigns"};
        this.permissionType = PermissionType.OR;
    }

    @Override
    public void execute(Event event)
    {
        Player player = event.getPlayer();

        PlayerSignsConfiguration cnf = CommandUtils.getPlayerSignFromLookingOntoIt(player);
        if (cnf == null) return;

        if (!(Validator.validate(cnf)))
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_enable_error"));
            return;
        }

        if (!cnf.isSignDisabled())
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_is_enabled"));
        } else
        {
            cnf.enableSign();
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_enable").replace("%sign%", cnf.gamemode.toString()));
        }
    }
}
