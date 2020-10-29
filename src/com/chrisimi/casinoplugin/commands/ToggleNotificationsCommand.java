package com.chrisimi.casinoplugin.commands;

import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.NotificationManager;
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
        if(NotificationManager.hasNotificationsDisabled(event.getPlayer()))
        {
            NotificationManager.enableNotifications(event.getPlayer());

            event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("notifications-enable"));
        }
        else
        {
            NotificationManager.disableNotifications(event.getPlayer());

            event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("notifications-disable"));
        }
    }
}
