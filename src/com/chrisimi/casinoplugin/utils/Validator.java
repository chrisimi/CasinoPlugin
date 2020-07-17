package com.chrisimi.casinoplugin.utils;

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

        return (conf.bet != null && conf.plusinformations.contains("-") && conf.plusinformations.contains(";")
        && conf.plusinformations.split("-").length == 2 && conf.plusinformations.split(";").length == 2);
    }
    private static boolean validateBlackjack(PlayerSignsConfiguration conf)
    {
        try
        {
            conf.blackjackGetMaxBet();
            conf.blackjackGetMinBet();
            conf.blackjackMultiplicator();
        }
        catch(Exception e) {return false;}

        return (conf.bet != null && conf.plusinformations.contains(";") && conf.plusinformations.split(";").length == 2);
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

        return (conf.bet != null && conf.getSlotsWeight().length == 3 && conf.getSlotsSymbols().length == 3);
    }
}
