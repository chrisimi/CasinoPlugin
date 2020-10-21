package com.chrisimi.casinoplugin.commands.slotchests;

import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.slotchest.SlotChestsManager;
import com.chrisimi.casinoplugin.utils.CommandUtils;
import com.chrisimi.commands.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

/**
 * the command instance for /casino createchest
 */
public class CreateChestCommand extends Command
{
    public CreateChestCommand()
    {
        this.command = "createchest";
        this.description = "create your own slotchest while looking onto a normal chest. Make sure the inventory is empty";
        this.permissions = new String[] {"casino.admin", "casino.slotchest.create"};
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

        //create the slotchest
        SlotChestsManager.createSlotChest(chest.getLocation(), player, false);
    }
}
