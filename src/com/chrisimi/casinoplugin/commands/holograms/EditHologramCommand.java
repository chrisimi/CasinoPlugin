package com.chrisimi.casinoplugin.commands.holograms;

import com.chrisimi.casinoplugin.hologramsystem.HologramMenu;
import com.chrisimi.casinoplugin.hologramsystem.HologramSystem;
import com.chrisimi.casinoplugin.hologramsystem.LBHologram;
import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.PermissionType;
import org.bukkit.entity.Player;

/**
 * command instance for /casino edithologram [name]
 */
public class EditHologramCommand extends Command
{
    public EditHologramCommand()
    {
        this.command = "edithologram";
        this.description = "Edits a existing hologram with the help of the hologram creation menu.";
        this.parametersDescription = "[name]";
        this.permissions = new String[] {"casino.admin", "casino.hologram.server", "casino.hologram.create"};
        this.permissionType = PermissionType.OR;
        this.enableArguments = true;
    }

    @Override
    public void execute(Event event)
    {
        if(event.getArgs().length == 0)
            return;

        Player player = event.getPlayer();

        LBHologram holo = HologramSystem.getHologramByName(event.getArgs()[0]);
        if(holo == null)
        {
            event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands_invalid_hologramname"));
            return;
        }

        //check if it's a server hologram and the player has sufficient permission
        if (holo.isServerHologram() && !(Main.perm.has(player, "casino.admin") || Main.perm.has(player, "casino.hologram.server")))
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
            return;
        }

        //check if it's a player hologram and afterwards if the player is admin or the owner of the hologram
        if (!holo.isServerHologram() && !(holo.getOwner().getUniqueId().equals(player.getUniqueId())) && !(Main.perm.has(player, "casino.admin")))
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
            return;
        }

        new HologramMenu(player, holo);
    }
}
