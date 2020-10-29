package com.chrisimi.casinoplugin.commands.jackpot;

import com.chrisimi.casinoplugin.jackpot.JackpotManager;
import com.chrisimi.casinoplugin.jackpot.JackpotSystem;
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
        this.description = "Edit an existing jackpot by using it's unique name";
        this.permissions = new String[] {"casino.jackpot.create", "casino.jackpot.server", "casino.admin"};
        this.enableArguments = true;
        this.usageType = UsageType.PLAYER;
    }

    @Override
    public void execute(Event event)
    {
        if(event.getArgs().length <= 0)
        {
            event.getPlayer().sendMessage("You need to write the name of the jackpot");
            return;
        }

        Jackpot jackpot = JackpotManager.byName(event.getArgs()[0]);
        if(jackpot == null)
        {
            event.getPlayer().sendMessage("This is not an existing jackpot");
            return;
        }

        if(!jackpot.isServerOwner() && !event.getPlayer().getUniqueId().equals(jackpot.getOwner().getUniqueId()))
        {
            event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
            return;
        }

        new JackpotCreationMenu(event.getPlayer(), jackpot);
    }
}
