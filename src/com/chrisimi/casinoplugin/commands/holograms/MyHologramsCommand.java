package com.chrisimi.casinoplugin.commands.holograms;

import com.chrisimi.casinoplugin.hologramsystem.HologramSystem;
import com.chrisimi.casinoplugin.hologramsystem.LBHologram;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.utils.Validator;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.PermissionType;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * the command instance for /casino myholograms
 */
public class MyHologramsCommand extends Command
{
    public MyHologramsCommand()
    {
        this.command = "myholograms";
        this.description = "Displays the coordinates and names of all your holograms.";
        this.aliases = new String[] {"holograms"};
        this.permissions = new String[] {"casino.admin", "casino.hologram.server", "casino.hologram.create"};
        this.permissionType = PermissionType.OR;
    }

    @Override
    public void execute(Event event)
    {
        Player player = event.getPlayer();
        if (!Validator.isHologramSystemUp(player)) return;

        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-holograms-overview"));
        ArrayList<LBHologram> holograms = HologramSystem.getHologramsFromPlayer(player);
        for (LBHologram holo : holograms)
        {
            player.sendMessage("§a " + holo.hologramName + " | " + holo.getLocation().getWorld().getName() + " | " + holo.getLocation().getBlockX() + " | " + holo.getLocation().getBlockY() + " | " + holo.getLocation().getBlockZ());
        }
    }
}
