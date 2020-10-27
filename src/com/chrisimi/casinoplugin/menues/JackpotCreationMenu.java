package com.chrisimi.casinoplugin.menues;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.serializables.Jackpot;
import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.IInventoryAPI;
import com.chrisimi.inventoryapi.Inventory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class JackpotCreationMenu extends Inventory implements IInventoryAPI
{
    private enum WaitingFor
    {
        NONE,
        POS_1,
        POS_2,
        NAME,
        WEIGHT, //when editing -> when player writes delete instead of the new weight, delete this jackpot element
    }

    private final ItemStack setPos1 = ItemAPI.createItem("§6set position 1 of the jackpot area", Material.GOLD_INGOT);
    private final ItemStack setPos2 = ItemAPI.createItem("§6set position 2 of the jackpot area", Material.GOLD_INGOT);
    private final ItemStack setName = ItemAPI.createItem("§6set the name", Material.SIGN);
    private final ItemStack finishButton = ItemAPI.createItem("§6finish creation or update", Material.STONE_BUTTON);
    private final ItemStack openElementInventory = ItemAPI.createItem("§6edit elements", Material.BOOK);

    private Location pos1 = null;
    private Location pos2 = null;
    private String name = null;
    public List<Jackpot.JackpotElement> elementList = new ArrayList<>();

    private WaitingFor waitingFor = WaitingFor.NONE;

    private JackpotElementCreationMenu jackpotElementCreationMenu = null;

    /**
     * the create constructor
     * @param player the player for whom should the inventory be created
     */
    public JackpotCreationMenu(Player player)
    {
        super(player, 9, Main.getInstance(), "Jackpot creation menu");

        addEvents(this);
    }
}
