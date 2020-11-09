package com.chrisimi.casinoplugin.animations;

import java.util.Random;

import com.chrisimi.casinoplugin.scripts.*;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;

/**
 * animation for dice signs
 */
public class DiceAnimation implements Runnable
{
    private PlayerSignsConfiguration thisSign;
    private Player player;
    private OfflinePlayer owner;
    private Sign sign;
    private PlayerSignsManager signsManager;

    private static int updateCycle = 8;

    private int tasknumber;

    public DiceAnimation(PlayerSignsConfiguration thisSign, Player player, PlayerSignsManager manager)
    {
        this.thisSign = thisSign;
        this.player = player;
        this.owner = thisSign.getOwner();
        this.sign = thisSign.getSign();
        this.signsManager = manager;

        getConfigValues();
    }

    private void getConfigValues()
    {
        try
        {
            updateCycle = Integer.valueOf(UpdateManager.getValue("dice-animation-speed", 8).toString());
        } catch (Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get Dice animation speed: invalid number! Set to default value: 8 Ticks");
            updateCycle = 8;
        }
    }

    @Override
    public void run()
    {
        Main.econ.withdrawPlayer(player, thisSign.bet);
        //Main.econ.depositPlayer(owner, thisSign.bet);
        thisSign.depositOwner(thisSign.bet);
        if (sign == null)
        {
            CasinoManager.Debug(this.getClass(), "Error while trying to start diceanimation! (Sign is null)");
            return;
        }

        prepareSign();
        try
        {
            tasknumber = Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable()
            {
                int animationCount = 0;

                @Override
                public void run()
                {
                    animate();

                    if (animationCount >= 15)
                    {
                        endAnimation();

                        return;
                    }
                    sign.update(true);

                    animationCount++;
                }
            }, 5, updateCycle);
        } catch (Exception e)
        {
            e.printStackTrace();
            CasinoManager.LogWithColor(ChatColor.RED + "An error occured, try to restart the server! If the problems stays, contact the owner of the plugin!");
        }
    } //main run

    private void endAnimation()
    {
        double wonamount = thisSign.winMultiplicatorDice() * thisSign.bet;
        Main.getInstance().getServer().getScheduler().cancelTask(tasknumber);

        double[] values = thisSign.getWinChancesDiceOld();
        double ergebnis = Double.parseDouble(sign.getLine(1));
        if (ergebnis >= values[0] && ergebnis <= values[1])
        {
            sign.setLine(2, "§aYOU WON!");
            //player.sendMessage(CasinoManager.getPrefix() + "§aYou won " + Main.econ.format(wonamount));
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("dice-player_won").replace("%amount%", Main.econ.format(wonamount)));

            DataManager.dataBase.addData(player, thisSign, thisSign.bet, wonamount);
            Main.econ.depositPlayer(player, wonamount);
            //Main.econ.withdrawPlayer(owner, wonamount);
            thisSign.withdrawOwner(wonamount);
            if (!thisSign.isServerOwner() && owner.isOnline() && !NotificationManager.hasNotificationsDisabled(owner))
            {
                //owner.getPlayer().sendMessage(CasinoManager.getPrefix() + String.format("§4%s won %s at your Dice sign.", player.getName(), Main.econ.format(wonamount)));
                owner.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("dice-owner-player_won").replace("%playername%", player.getPlayerListName()).replace("%amount%", Main.econ.format(wonamount)));
            } else if (!thisSign.isServerOwner())
            {
                OfflineEarnManager.getInstance().addLoss(owner, wonamount);
            }
        } else
        {
            sign.setLine(2, "§4YOU LOST!");
            //player.sendMessage(CasinoManager.getPrefix() + "§4You lost " + Main.econ.format(thisSign.bet));
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("dice-player_lost").replace("%amount%", Main.econ.format(thisSign.bet)));

            DataManager.dataBase.addData(player, thisSign, thisSign.bet, 0);
            if (!thisSign.isServerOwner() && owner.isOnline() && !NotificationManager.hasNotificationsDisabled(owner))
            {
                //owner.getPlayer().sendMessage(CasinoManager.getPrefix() + String.format("§a%s lost %s at your Dice sign.", player.getName(), Main.econ.format(thisSign.bet)));
                owner.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("dice-owner-player_lost").replace("%playername%", player.getPlayerListName()).replace("%amount%", Main.econ.format(thisSign.bet)));
            } else if (!thisSign.isServerOwner())
            {
                OfflineEarnManager.getInstance().addEarning(owner, thisSign.bet);
            }
        }
        sign.update(true);
        Main.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable()
        {
            @Override
            public void run()
            {

                thisSign.isRunning = false;
            }
        }, 40);
    }

    private void prepareSign()
    {
        sign.setLine(0, "");
        sign.setLine(1, "");
        sign.setLine(2, "");
        sign.setLine(3, "§awin: " + thisSign.plusinformations.split(";")[0]);
    }

    private void animate()
    {
        Random rnd = new Random();
        int rndNumber = rnd.nextInt(100) + 1; //because 0 could come out so that 0 -> 1 99->100 and that 100 is exlusive in method
        sign.setLine(1, String.valueOf(rndNumber));
    }
}
