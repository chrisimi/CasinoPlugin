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
        this.description = "run jackpot";
        this.enableArguments = true;
        this.permissions = new String[] {"casino.jackpot.use", "casino.jackpot.server", "casino.admin", "casino.jackpot.*"};
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
