package com.chrisimi.casinoplugin.commands;

import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;

/**
 * the command instance for /casino togglenotifications
 */
public class ToggleNotificationsCommand extends Command
{
    public ToggleNotificationsCommand()
    {
        this.command = "togglenotifications";
        this.description = "Enable or disable the notficiations of your player-managed signs, jackpots and SlotChests";
        this.permissions = new String[] {"casino.blackjack.create",
                "casino.slots.create",
                "casino.dice.create",
                "casino.jackpot.create",
                "casino.slotchest.create",
                "casino.admin"};
    }

    @Override
    public void execute(Event event)
    {
        //TODO implement logic
    }
}
