package com.chrisimi.casinoplugin.commands;

import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.PermissionType;
import com.chrisimi.commands.UsageType;

/**
 * the command instance for /casino roll
 */
public class RollCommand extends Command
{
    public RollCommand()
    {
        this.command = "roll";
        this.description = "[minimum] [maximum] [player (not needed)] roll a random number which will be sent to nearby players or to the mentioned player";
        this.permissions = new String[] {"casino.admin", "casino.roll"};
        this.permissionType = PermissionType.OR;
        this.usageType = UsageType.PLAYER;
        this.enableArguments = true;
    }

    @Override
    public void execute(Event event)
    {
        com.chrisimi.casinoplugin.scripts.RollCommand.roll(event.getPlayer(), event.getArgs());
    }
}
