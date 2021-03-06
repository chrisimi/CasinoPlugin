package com.chrisimi.casinoplugin.animations.signanimation;

import com.chrisimi.numberformatter.NumberFormatter;
import org.bukkit.block.Sign;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;

/**
 * the sign animation for Dice signs, animation should be called every x seconds to update things like payout etc.
 */
public class Dice implements Runnable
{

    private final Sign sign;
    private final PlayerSignsConfiguration thisSign;

    public Dice(Sign sign, PlayerSignsConfiguration thisSign)
    {
        this.sign = sign;
        this.thisSign = thisSign;
    }

    @Override
    public void run()
    {
        double[] values = thisSign.getWinChancesDiceOld();
        double chance = values[1] - values[0] + 1;

        sign.setLine(0, "§fDice");
        sign.setLine(1, thisSign.getOwnerName());

        if (thisSign.isSignDisabled())
        {
            sign.setLine(2, "§4DISABLED!");
            sign.setLine(3, "§4DISABLED!");
        } else
        {
            if (thisSign.currentSignAnimation == 1)
            {
                sign.setLine(2, "§6bet: " + NumberFormatter.format(thisSign.bet));
            } else
            {
                sign.setLine(2, "§awin: " + NumberFormatter.format(thisSign.winMultiplicatorDice() * thisSign.bet));
            }
            //playersign is enabled
            if (!(thisSign.hasOwnerEnoughMoney()))
            {
                //owner of the sign doesn't have enough money
                if (thisSign.currentSignAnimation == 1)
                {
                    sign.setLine(2, "§4ERROR!");
                    sign.setLine(3, "§4ERROR!");
                } else
                {
                    sign.setLine(2, "§4doesn't have");
                    sign.setLine(3, "§4enough money!");
                }
            } else
            {
                //owner of the sign has enough money
                sign.setLine(3, String.format("§b§nChance: %.2f %%", chance));
            }
        }

        sign.update(true);

        thisSign.currentSignAnimation = (thisSign.currentSignAnimation == 1) ? 0 : 1; //if value 1 set it to 0  range: 0-1
    }

}
