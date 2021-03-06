package com.chrisimi.casinoplugin.animations.signanimation;

import com.chrisimi.numberformatter.NumberFormatter;
import org.bukkit.block.Sign;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;

/**
 * the sign animation for slots signs should be called every x seconds to update the sign
 */
public class Slots implements Runnable
{

    private final Sign sign;
    private final PlayerSignsConfiguration thisSign;

    public Slots(Sign sign, PlayerSignsConfiguration thisSign)
    {
        this.sign = sign;
        this.thisSign = thisSign;
    }

    @Override
    public void run()
    {
        sign.setLine(0, "§fSlots");
        sign.setLine(1, thisSign.getOwnerName());

        if (thisSign.isSignDisabled())
        {
            sign.setLine(2, "§4DISABLED!");
            sign.setLine(3, "§4DISABLED!");
        } else
        {
            if (thisSign.hasOwnerEnoughMoney(thisSign.getSlotsHighestPayout()))
            {
                //owner has enough money
                sign.setLine(2, "§6" + NumberFormatter.format(thisSign.bet));
                sign.setLine(3, "3x " + thisSign.getColorCodesSlots()[thisSign.currentSignAnimation] + thisSign.getSlotsSymbols()[thisSign.currentSignAnimation] + " : " + NumberFormatter.format(thisSign.bet * thisSign.getSlotsMultiplicators()[thisSign.currentSignAnimation]));
            } else
            {
                if ((thisSign.currentSignAnimation % 2) == 1)
                {
                    sign.setLine(2, "§4ERROR!");
                    sign.setLine(3, "§4ERROR!");
                } else
                {
                    sign.setLine(2, "§4doesn't have");
                    sign.setLine(3, "§4enough money!");
                }
            }
        }
        sign.update(true);
        thisSign.currentSignAnimation = (thisSign.currentSignAnimation == thisSign.getSlotsSymbols().length - 1) ? 0 : ++thisSign.currentSignAnimation; // add 1 and set it to 0 if it's 2.     range: 0-2
    }
}
