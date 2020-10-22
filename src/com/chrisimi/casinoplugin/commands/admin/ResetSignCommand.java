package com.chrisimi.casinoplugin.commands.admin;

import com.chrisimi.casinoplugin.animations.BlackjackAnimation;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.UpdateManager;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import com.chrisimi.casinoplugin.utils.CommandUtils;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;

/**
 * the command instance for /casino resetsign
 */
public class ResetSignCommand extends Command
{
    public ResetSignCommand()
    {
        this.command = "resetsign";
        this.description = "reset/restart the sign your are currently looking onto. Good for bugging signs.";
        this.permissions = new String[] {"casino.admin"};
    }

    @Override
    public void execute(Event event)
    {
        PlayerSignsConfiguration cnf = CommandUtils.getPlayerSignFromLookingOntoIt(event.getPlayer());
        if (cnf == null) return;

        BlackjackAnimation.resetForSign(cnf.getLocation());
        cnf.isRunning = false;
        event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-sign_reset_successful"));
    }
}
