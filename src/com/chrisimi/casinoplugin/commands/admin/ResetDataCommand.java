package com.chrisimi.casinoplugin.commands.admin;

import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.DataManager;
import com.chrisimi.casinoplugin.scripts.LeaderboardsignsManager;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;

/**
 * the command instance for /casino resetdata
 */
public class ResetDataCommand extends Command
{
    public ResetDataCommand()
    {
        this.command = "resetdata";
        this.description = "delete all roll-data from player-managed-signs (data.yml)";
        this.permissions = new String[] {"casino.admin"};
    }

    @Override
    public void execute(Event event)
    {
        DataManager.getInstance().resetData();
        event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-admin_successfully_reset_data"));
    }
}
