package com.chrisimi.casinoplugin.commands.jackpot;

import com.chrisimi.casinoplugin.jackpot.JackpotManager;
import com.chrisimi.casinoplugin.jackpot.JackpotSystem;
import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.menues.JackpotCreationMenu;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.serializables.Jackpot;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.UsageType;

/**
 * the command instance for /casino editjackpot [name]
 */
public class EditJackpotCommand extends Command
{
    public EditJackpotCommand()
    {
        this.command = "editjackpot";
        this.description = "Edits an existing jackpot by using it's unique name";
        this.argumentsDescription = "[name]";
        this.permissions = new String[] {"casino.jackpot.create"};
        this.enableArguments = true;
        this.usageType = UsageType.PLAYER;
    }

    @Override
    public void execute(Event event)
    {
        if(CasinoManager.jackpotManager == null)
        {
            event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("jackpot-disabled"));
            return;
        }

        if(event.getArgs().length <= 0)
        {
            event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("jackpot-edit-error"));
            return;
        }

        Jackpot jackpot = JackpotManager.byName(event.getArgs()[0]);
        if(jackpot == null)
        {
            event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("jackpot-edit-error2"));
            return;
        }

        if(!jackpot.isServerOwner() && !event.getPlayer().getUniqueId().equals(jackpot.getOwner().getUniqueId()))
        {
            event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
            return;
        }

        if(jackpot.isServerOwner())
        {
            if(!(Main.perm.has(event.getPlayer(), "casino.admin") || Main.perm.has(event.getPlayer(), "casino.jackpot.server")))
            {
                event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
                return;
            }
        }

        new JackpotCreationMenu(event.getPlayer(), jackpot);
    }
}
