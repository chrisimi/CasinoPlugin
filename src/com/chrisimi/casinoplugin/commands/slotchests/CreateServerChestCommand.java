package com.chrisimi.casinoplugin.commands.slotchests;

import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.slotchest.SlotChestsManager;
import com.chrisimi.casinoplugin.utils.CommandUtils;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.PermissionType;
import com.chrisimi.commands.UsageType;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

/**
 * the command instance for /casino createserverchest
 */
public class CreateServerChestCommand extends Command
{
    public CreateServerChestCommand()
    {
        this.command = "createserverchest";
        this.description = "Create a server SlotChest. Make sure the inventory is empty";
        this.permissions = new String[] {"casino.create.serverslotchest"};
        this.permissionType = PermissionType.OR;
        this.usageType = UsageType.PLAYER;
    }

    @Override
    public void execute(Event event)
    {
        Player player = event.getPlayer();
        Block block = player.getTargetBlock(null, 10);
        if(block == null) return;

        Chest chest = null;
        try
        {
            chest = (Chest) block.getState();
        } catch(Exception e)
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-slotchest_chest_invalid"));
            return;
        }

        //check if chest is empty
        if(!(CommandUtils.isChestEmpty(chest)))
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-slotchest_chest_not_empty"));
            return;
        }

        //create the server slotchest
        SlotChestsManager.createSlotChest(chest.getLocation(), player, true);
    }
}
