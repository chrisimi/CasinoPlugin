package com.chrisimi.casinoplugin.commands.jackpot;

import com.chrisimi.casinoplugin.jackpot.JackpotSystem;
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
    }

    @Override
    public void execute(Event event)
    {
        JackpotSystem.runJackpot(event.getArgs()[0], event.getPlayer());
    }
}
