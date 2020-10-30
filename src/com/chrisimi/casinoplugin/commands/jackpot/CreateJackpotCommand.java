package com.chrisimi.casinoplugin.commands.jackpot;

import com.chrisimi.casinoplugin.commands.slotchests.CreateChestCommand;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.menues.JackpotCreationMenu;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;

/**
 * the command instance for /casino createjackpot
 */
public class CreateJackpotCommand extends Command
{
    public CreateJackpotCommand()
    {
        this.command = "createjackpot";
        this.description = "Open the creation menu to create a new Jackpot";
        this.permissions = new String[] {"casino.jackpot.create", "casino.jackpot.server", "casino.admin"};
    }

    @Override
    public void execute(Event event)
    {
        if(CasinoManager.jackpotManager == null)
        {
            event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("jackpot-disabled"));
            return;
        }

        new JackpotCreationMenu(event.getPlayer());
    }
}
