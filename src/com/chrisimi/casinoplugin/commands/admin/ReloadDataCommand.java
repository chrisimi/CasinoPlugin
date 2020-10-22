package com.chrisimi.casinoplugin.commands.admin;

import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.LeaderboardsignsManager;
import com.chrisimi.casinoplugin.scripts.UpdateManager;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;

/**
 * the command instance for /casino reloaddata
 */
public class ReloadDataCommand extends Command
{
    public ReloadDataCommand()
    {
        this.command = "reloaddata";
        this.description = "reload all leaderboard signs and the data.yml file";
        this.permissions = new String[] {"casino.admin"};
    }

    @Override
    public void execute(Event event)
    {
        LeaderboardsignsManager.reloadData();
        event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-admin_successfully_reload_data"));
    }
}
