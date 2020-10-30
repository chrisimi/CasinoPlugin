package com.chrisimi.casinoplugin.commands;

import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import com.chrisimi.casinoplugin.utils.CommandUtils;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.PermissionType;
import com.chrisimi.numberformatter.NumberFormatter;
import org.bukkit.entity.Player;

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
        this.subCommands = new Command[] {new SignEnableCommand(), new SignDisableCommand()};
    }

    @Override
    public void execute(Event event)
    {
        PlayerSignsConfiguration psc = CommandUtils.getPlayerSignFromLookingOntoIt(event.getPlayer());
        if(psc == null)
        {
            event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-sign-no_sign"));
            return;
        }

        event.getPlayer().sendMessage(CasinoManager.getPrefix() + String.format("Information about sign at X: %s, Y: %s, Z: " + psc.getLocation().getBlockX(),
                psc.getLocation().getBlockY(), psc.getLocation().getBlockZ()));
        event.getPlayer().sendMessage(CasinoManager.getPrefix() + "§6type: " + psc.gamemode.toString());
        event.getPlayer().sendMessage(CasinoManager.getPrefix() + ((psc.isSignDisabled()) ? "§4disabled" : "§aenabled"));

        switch(psc.gamemode)
        {
            case DICE:
                showDiceInformation(psc, event.getPlayer());
        }
    }

    private void showDiceInformation(PlayerSignsConfiguration psc, Player player)
    {
        double winChance = (double)(Math.round((psc.getWinChancesDice()[1] - psc.getWinChancesDice()[0] + 1) * 100.0)) / 100.0;

        player.sendMessage(CasinoManager.getPrefix() + "§6bet: §e" + NumberFormatter.format(psc.bet, false));
        player.sendMessage(CasinoManager.getPrefix() + "possible win: " + NumberFormatter.format(psc.bet * psc.winMultiplicatorDice(), false));
        player.sendMessage(CasinoManager.getPrefix() + "win-range between " + psc.getWinChancesDice()[0] + " and " + psc.getWinChancesDice()[1]);
        player.sendMessage(CasinoManager.getPrefix() + "win chance: " + winChance + " %");
    }
}
