package com.chrisimi.casinoplugin.commands;

import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.PermissionType;

/**
 * the command instance for the command /casino sign
 */
public class SignCommand extends Command
{
    public SignCommand()
    {
        this.command = "sign";
        this.description = "display information about the sign you are looking at";
        this.permissions = new String[] {"casino.dice.use", "casino.blackjack.use", "casino.slots.use"};
        this.permissionType = PermissionType.OR;
    }

    @Override
    public void execute(Event event)
    {
        //TODO
    }
}
