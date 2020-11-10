package com.chrisimi.casinoplugin.commands;

import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import com.chrisimi.casinoplugin.utils.CommandUtils;
import com.chrisimi.casinoplugin.utils.Validator;
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
        this.permissions = new String[] {"casino.command.sign"};
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

        if(!Validator.validate(psc))
        {
            event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-sign_not_valid"));
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
                break;
            case SLOTS:
                showSlotsInformation(psc, event.getPlayer());
                break;
            case BLACKJACK:
                showBlackjackInformation(psc, event.getPlayer());
                break;
        }
    }

    private void showBlackjackInformation(PlayerSignsConfiguration psc, Player player)
    {
        player.sendMessage(CasinoManager.getPrefix() + "§6bet between §e" + NumberFormatter.format(psc.blackjackGetMinBet(), false)
                + " §6and §e" + NumberFormatter.format(psc.blackjackGetMaxBet(), false));

        player.sendMessage(CasinoManager.getPrefix() + "payout:");
        player.sendMessage(CasinoManager.getPrefix() + "     Blackjack (21): x" + psc.blackjackGetMultiplicand());
        player.sendMessage(CasinoManager.getPrefix() + "     win: 1x");
    }

    private void showSlotsInformation(PlayerSignsConfiguration psc, Player player)
    {
        player.sendMessage(CasinoManager.getPrefix() + "§6bet: §e" + NumberFormatter.format(psc.bet, false));
        player.sendMessage(CasinoManager.getPrefix() + "elements:");

        for(int i = 0; i < 3; i++)
        {
            double chance = (double)(Math.round((psc.getSlotsWeight()[i] / psc.getSlotsWeightSum() ) * 10000.0)) / 100.0;
            player.sendMessage(CasinoManager.getPrefix() + String.format("     %s: chance: %s %% payout: %s",
                    psc.getSlotsSymbols()[i],
                    chance,
                    NumberFormatter.format(psc.getSlotsMultiplicators()[i] * psc.bet, false)));
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
