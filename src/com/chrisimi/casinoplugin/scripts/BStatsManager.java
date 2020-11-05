package com.chrisimi.casinoplugin.scripts;

import com.chrisimi.casinoplugin.main.Metrics;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import com.chrisimi.casinoplugin.slotchest.animations.RollAnimationManager;

import java.util.concurrent.Callable;

public class BStatsManager
{
    public static void configureMetrics(Metrics metric)
    {
        //use of the casino gui slots (deprecated)
        metric.addCustomChart(new Metrics.SingleLineChart("use_of_slots", new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                int amount = CasinoAnimation.rollCount;
                CasinoAnimation.rollCount = 0;
                CasinoManager.Debug(this.getClass(), "sent use_of_slots with value " + amount);
                return amount;

            }

        }));

        //use of player signs
        metric.addCustomChart(new Metrics.SingleLineChart("use_of_playersigns", new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                int amount = PlayerSignsManager.rollCount;
                PlayerSignsManager.rollCount = 0;
                CasinoManager.Debug(this.getClass(), "sent use_of_playersigns with value " + amount);
                return amount;
            }

        }));

        //use of the roll command
        metric.addCustomChart(new Metrics.SingleLineChart("use_of_roll_command", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int amount = RollCommand.rollAmount;
                RollCommand.rollAmount = 0;
                CasinoManager.Debug(this.getClass(), "sent use_of_roll_command with value " + amount);
                return amount;
            }
        }));

        //spins in SlotChest
        metric.addCustomChart(new Metrics.SingleLineChart("use_of_slotchest", new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                int amount = RollAnimationManager.rollsGlobal;
                RollAnimationManager.rollsGlobal = 0;
                CasinoManager.Debug(this.getClass(), "sent use_of_slotchest with value " + amount);
                return amount;
            }

        }));

        //currently running leaderboard signs
        metric.addCustomChart(new Metrics.SingleLineChart("currently_running_leaderboardsigns", new Callable<Integer>()
        {

            @Override
            public Integer call() throws Exception
            {
                CasinoManager.Debug(this.getClass(), "sent currently_running_leaderboardsigns with value " + LeaderboardsignsManager.leaderboardsignRunnableTaskID.size());
                return LeaderboardsignsManager.leaderboardsignRunnableTaskID.size();
            }
        }));

        //currently running dice signs
        metric.addCustomChart(new Metrics.SingleLineChart("currently_running_dice_signs", new Callable<Integer>()
        {
            @Override
            public Integer call() throws Exception
            {
                int amount = PlayerSignsManager.getTotalAmountSigns(PlayerSignsConfiguration.GameMode.DICE);
                CasinoManager.Debug(this.getClass(), "sent currently_running_dice_signs with value " + amount);
                return amount;
            }
        }));

        //currently running blackjack signs
        metric.addCustomChart(new Metrics.SingleLineChart("currently_running_blackjack_signs", new Callable<Integer>()
        {
            @Override
            public Integer call() throws Exception
            {
                int amount = PlayerSignsManager.getTotalAmountSigns(PlayerSignsConfiguration.GameMode.BLACKJACK);
                CasinoManager.Debug(this.getClass(), "sent currently_running_blackjack_signs with value " + amount);
                return amount;
            }
        }));

        //currently running slots signs
        metric.addCustomChart(new Metrics.SingleLineChart("currently_running_slots_signs", new Callable<Integer>()
        {
            @Override
            public Integer call() throws Exception
            {
                int amount = PlayerSignsManager.getTotalAmountSigns(PlayerSignsConfiguration.GameMode.SLOTS);
                CasinoManager.Debug(this.getClass(), "sent currently_running_slots_signs with value " + amount);
                return amount;
            }
        }));
    }
}
