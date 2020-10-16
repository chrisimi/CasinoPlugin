package com.chrisimi.casinoplugin.utils;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.LeaderboardsignsManager;
import com.chrisimi.casinoplugin.scripts.PlayerSignsManager;
import com.chrisimi.casinoplugin.serializables.Leaderboardsign;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class CommandUtils
{
    /**
     * check if the player is looking onto a player sign
     * @param player the player to use for it
     * @return {@link PlayerSignsConfiguration} if it is a valid player sign
     */
    public static PlayerSignsConfiguration getPlayerSignFromLookingOntoIt(Player player)
    {
        Block block = player.getTargetBlock(null, 10);
        if (block == null)
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_invalid_target"));
            return null;
        }
        if (!(block.getType().toString().contains("SIGN")))
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_not_a_sign"));
            return null;
        }
        Sign signLookingAt = (Sign) block.getState();
        PlayerSignsConfiguration cnf = PlayerSignsManager.getPlayerSign(signLookingAt.getLocation());
        if (cnf == null)
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_not_a_playersign"));
            return null;
        }
        if (Main.perm.has(player, "casino.admin"))
        {

        } else if (cnf.isServerOwner() || (!(cnf.getOwner().getPlayer().equals(player))))
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_not_owner"));
            return null;
        }

        return cnf;
    }

    /**
     * check if the player is looking onto a leaderboard sign
     * @param player the player to use for it
     * @return {@link Leaderboardsign} if it is a valid leaderboard sign
     */
    public static Leaderboardsign getLeaderboardSignFromLookingOntoIt(Player player)
    {
        //get the block the player is currently looking at
        Block block = player.getTargetBlock(null, 10);
        if(block == null)
            return null;

        //check if it's a sign
        if(!(block.getType().toString().contains("SIGN")))
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_not_a_sign"));
            return null;
        }

        //check if the sign is a leaderboard sign
        Leaderboardsign leaderboardsign = LeaderboardsignsManager.getLeaderboardsign(block.getLocation());
        if (leaderboardsign == null)
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-leaderboard_not_a_lbsign"));
            return null;
        }
        return leaderboardsign;
    }
}
