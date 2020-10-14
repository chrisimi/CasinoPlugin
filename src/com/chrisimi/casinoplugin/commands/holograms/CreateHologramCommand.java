package com.chrisimi.casinoplugin.commands.holograms;

import com.chrisimi.casinoplugin.hologramsystem.HologramMenu;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.PermissionType;
import com.chrisimi.commands.UsageType;

/**
 * the command instance for /casino createhologram
 */
public class CreateHologramCommand extends Command
{
    public CreateHologramCommand()
    {
        this.command = "createhologram";
        this.description = "open the GUI for creating a hologram";
        this.permissions = new String[] {"casino.admin", "casino.hologram.server", "casino.hologram.create"};
        this.permissionType = PermissionType.OR;
        this.usageType = UsageType.PLAYER;
    }

    @Override
    public void execute(Event event)
    {
        if(event.getPlayer() != null)
            new HologramMenu(event.getPlayer());
    }
}
