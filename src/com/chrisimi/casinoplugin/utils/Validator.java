package com.chrisimi.casinoplugin.utils;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.PlayerSignsManager;
import com.chrisimi.casinoplugin.serializables.Jackpot;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import org.bukkit.entity.Player;

public class Validator
{
    public static boolean isHologramSystemUp(Player player)
    {
        if (!Main.hologramSystemUp)
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-holograms-disabled"));
            return false;
        }
        return true;
    }

    /**
     * check two strings for their equality in many ways
     * @param check String to check
     * @param with String to check with
     * @return true if they are equal, false if not
     */
    public static boolean is(String check, String with)
    {
        return check.equals(with) || check.equalsIgnoreCase(with) || check.toLowerCase().equals(with.toLowerCase());
    }

    public static boolean validate(PlayerSignsConfiguration conf)
    {
        switch(conf.gamemode)
        {
            case DICE:
                return validateDice(conf);
            case BLACKJACK:
                return validateBlackjack(conf);
            case SLOTS:
                return validateSlots(conf);
            default:
                return false;
        }
    }

    private static boolean validateDice(PlayerSignsConfiguration conf)
    {
        try
        {
            conf.getWinChancesDice();
            conf.isServerOwner();

        }
        catch(Exception e) { return false;}


        if(!conf.isServerOwner())
            if(!PlayerSignsManager.playerCanCreateSign(conf.getOwner(), PlayerSignsConfiguration.GameMode.DICE))
                return false;

        return conf.plusinformations.contains("-") && conf.plusinformations.contains(";")
        && conf.plusinformations.split("-").length == 2 && conf.plusinformations.split(";").length == 2
        && PlayerSignsManager.isBetAllowed(conf.bet, PlayerSignsConfiguration.GameMode.DICE);
    }
    private static boolean validateBlackjack(PlayerSignsConfiguration conf)
    {
        try
        {
            conf.blackjackGetMaxBet();
            conf.blackjackGetMinBet();
            conf.blackjackGetMultiplicand();
        }
        catch(Exception e) {return false;}

        if(!conf.isServerOwner())
            if(!PlayerSignsManager.playerCanCreateSign(conf.getOwner(), PlayerSignsConfiguration.GameMode.BLACKJACK))
                return false;

        return conf.plusinformations.contains(";") && conf.plusinformations.split(";").length == 2
        && PlayerSignsManager.isBetAllowed(conf.blackjackGetMaxBet(), PlayerSignsConfiguration.GameMode.BLACKJACK);
    }
    private static boolean validateSlots(PlayerSignsConfiguration conf)
    {
        try
        {
            conf.getSlotsHighestPayout();
            conf.getSlotsMultiplicators();
            conf.getSlotsSymbols();
            conf.getSlotsWeight();
        }
        catch(Exception e) {return false;}

        if(!conf.isServerOwner())
            if(!PlayerSignsManager.playerCanCreateSign(conf.getOwner(), PlayerSignsConfiguration.GameMode.DICE))
                return false;

        return conf.getSlotsWeight().length >= 3 && conf.getSlotsSymbols().length >= 3
        && PlayerSignsManager.isBetAllowed(conf.bet, PlayerSignsConfiguration.GameMode.SLOTS);
    }

    /**
     * validate if a jackpot has valid values
     * @param jackpot the jackpot to check
     * @return true if the jackpot is valid, false if not
     */
    public static boolean validateJackpot(Jackpot jackpot)
    {
        try
        {
            if(jackpot.getLocation1() == null || jackpot.getLocation2() == null)
                throw new Exception();

            if(jackpot.elements.size() < 3)
                throw new Exception();

            if(jackpot.name.isEmpty())
                throw new Exception();
        } catch(Exception e)
        {
            return false;
        }

        return true;
    }
}
