package com.chrisimi.casinoplugin.commands.jackpot;

import com.chrisimi.casinoplugin.jackpot.JackpotSystem;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;

/**
 * the command instance for /casino runjackpot [name]
 */
public class RunJackpotCommand extends Command
{
    public RunJackpotCommand()
    {
        this.command = "runjackpot";
        this.description = "Runs the jackpot with the unique name";
        this.argumentsDescription = "[name]";
        this.enableArguments = true;
        this.permissions = new String[] {"casino.command.jackpot"};
    }

    @Override
    public void execute(Event event)
    {
        if(CasinoManager.jackpotManager == null)
        {
            event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("jackpot-disabled"));
            return;
        }

        if(event.getArgs().length >= 1)
            JackpotSystem.runJackpot(event.getArgs()[0], event.getPlayer());
    }
}
