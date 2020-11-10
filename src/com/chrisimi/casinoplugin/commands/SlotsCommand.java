package com.chrisimi.casinoplugin.commands;

import com.chrisimi.casinoplugin.scripts.CasinoSlotsGUIManager;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.PermissionType;
import com.chrisimi.commands.UsageType;

/**
 * the command instance for /casino slots
 */
public class SlotsCommand extends Command
{
    public SlotsCommand()
    {
        this.command = "slots";
        this.description = "open the Slots GUI to play slots";
        this.permissions = new String[] {"casino.gui"};
        this.permissionType = PermissionType.OR;
        this.usageType = UsageType.PLAYER;
    }

    @Override
    public void execute(Event event)
    {
        new CasinoSlotsGUIManager(event.getPlayer());
    }
}
