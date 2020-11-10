package com.chrisimi.casinoplugin.commands;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import com.chrisimi.casinoplugin.utils.CommandUtils;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.PermissionType;
import org.bukkit.entity.Player;

/**
 * the command instance for the command /casino sign disable
 */
public class SignDisableCommand extends Command
{
    public SignDisableCommand()
    {
        this.command = "disable";
        this.description = "disable your player sign while looking onto it";
        this.permissions = new String[] {"casino.command.sign"};
        this.permissionType = PermissionType.OR;
    }

    @Override
    public void execute(Event event)
    {
        Player player = event.getPlayer();

        PlayerSignsConfiguration cnf = CommandUtils.getPlayerSignFromLookingOntoIt(player);
        if (cnf == null) return;

        if (cnf.isSignDisabled())
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_is_disabled"));
        } else
        {
            cnf.disableSign();
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_disable").replace("%sign%", cnf.gamemode.toString()));
        }
    }
}
