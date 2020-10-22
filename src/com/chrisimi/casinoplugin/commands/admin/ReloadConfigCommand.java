package com.chrisimi.casinoplugin.commands.admin;

import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.UpdateManager;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.UsageType;

/**
 * the command instance for /casino reloadconfig
 */
public class ReloadConfigCommand extends Command
{
    public ReloadConfigCommand()
    {
        this.command = "reloadconfig";
        this.description = "reload the config.yml";
        this.permissions = new String[] {"casino.admin"};
    }

    @Override
    public void execute(Event event)
    {
        UpdateManager.reloadConfig();
        CasinoManager.reload();
        event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-admin_successfully_reloaded_config"));
    }
}
