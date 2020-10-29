package com.chrisimi.casinoplugin.commands.jackpot;

import com.chrisimi.casinoplugin.commands.slotchests.CreateChestCommand;
import com.chrisimi.casinoplugin.menues.JackpotCreationMenu;
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
        this.description = "create a new jackpot";
    }

    @Override
    public void execute(Event event)
    {
        new JackpotCreationMenu(event.getPlayer());
    }
}
