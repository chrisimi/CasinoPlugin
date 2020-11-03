package com.chrisimi.casinoplugin.commands.admin;

import com.chrisimi.casinoplugin.scripts.UpdateManager;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;

/**
 * the command instance for /casino updateconfig
 */
public class UpdateConfigCommand extends Command
{
    public UpdateConfigCommand()
    {
        this.command = "updateconfig";
        this.permissions = new String[] {"casino.admin"};
        this.description = "Updates the config. Save your config.yml before using this command to prevent lose of your config.";
    }

    @Override
    public void execute(Event event)
    {
        UpdateManager.updateConfigYml();
    }
}
