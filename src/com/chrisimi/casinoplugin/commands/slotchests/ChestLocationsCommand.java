package com.chrisimi.casinoplugin.commands.slotchests;

import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.slotchest.SlotChest;
import com.chrisimi.casinoplugin.slotchest.SlotChestsManager;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.PermissionType;
import com.chrisimi.commands.UsageType;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * the command instance for /casino chestlocations
 */
public class ChestLocationsCommand extends Command
{
    public ChestLocationsCommand()
    {
        this.command = "chestlocations";
        this.description = "Get the locations from your SlotChests";
        this.permissions = new String[] {"casino.admin", "casino.slotchest.create", "casino.slotchest.server"};
        this.permissionType = PermissionType.OR;
        this.usageType = UsageType.PLAYER_CONSOLE;
    }

    @Override
    public void execute(Event event)
    {
        Player player = event.getPlayer();
        ArrayList<SlotChest> list = SlotChestsManager.getSlotChestsFromPlayer(player);
        if (list.size() == 0)
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-slotchest_no_slotchests"));
            return;
        }
        player.sendMessage("\n\n");
        player.sendMessage(CasinoManager.getPrefix() + "§6§lYour SlotChests:");

        int index = 1;
        for (SlotChest chest : list)
        {
            player.sendMessage(String.format("§6%s: x: %s, y: %s, z: %s", index,
                    chest.getLocation().getBlockX(),
                    chest.getLocation().getBlockY(),
                    chest.getLocation().getBlockZ()));
            index++;
        }
    }
}
