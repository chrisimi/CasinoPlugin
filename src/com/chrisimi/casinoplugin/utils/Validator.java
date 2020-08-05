package com.chrisimi.casinoplugin.utils;

import com.chrisimi.casinoplugin.scripts.PlayerSignsManager;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;

public class Validator
{
    /**
     * check two strings for their equality in many ways
     * @param check String to check
     * @param with String to check with
     * @return true if they are equal, false if not
     */
    public static boolean is(String check, String with)
    {
        return check.equals(with) || check.equalsIgnoreCase(with) || check.toLowerCase().equals(with.toLowerCase()) || check.contains(with) || with.contains(check);
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
        && conf.bet <= PlayerSignsManager.getMaxBetDice();
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
        && conf.bet <= PlayerSignsManager.getMaxBetBlackjack();
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

        return conf.getSlotsWeight().length == 3 && conf.getSlotsSymbols().length == 3
        && conf.bet <= PlayerSignsManager.getMaxBetSlots();
    }
}
