package com.chrisimi.casinoplugin.utils;

import com.chrisimi.casinoplugin.scripts.PlayerSignsManager;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;

public class Validator
{
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
