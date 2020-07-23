package com.chrisimi.casinoplugin.menues;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.IInventoryAPI;
import com.chrisimi.inventoryapi.Inventory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SlotsCreationMenu extends Inventory implements IInventoryAPI
{
    private final ItemStack addOption = ItemAPI.createItem("§6add option", Material.STONE_BUTTON);
    private final ItemStack removeOption = ItemAPI.createItem("§aremove option", Material.RED_BANNER);
    private final ItemStack changeChanceOfOption = ItemAPI.createItem("§6change option", Material.YELLOW_BANNER); //when click you can change the chance to get it
    private final ItemStack showOption = ItemAPI.createItem("§6{option numr} {option}", Material.BLUE_BANNER); //when click you can change the item like 'A' to 'B'
    private final ItemStack changeWinMultiplicandOfOption = ItemAPI.createItem("§6{win multiplicand}", Material.BROWN_BANNER); //when click you can change the win multiplicand if you get 3 in a row

    private final ItemStack setBet = ItemAPI.createItem("§6set bet", Material.GOLD_INGOT);
    private final ItemStack setServerSign = ItemAPI.createItem("§6to server sign", Material.GOLD_BLOCK);
    private final ItemStack disableSign = ItemAPI.createItem("§4disable sign", Material.RED_WOOL);
    private final ItemStack enableSign = ItemAPI.createItem("§aenable sign", Material.GREEN_WOOL);
    private final ItemStack finishButton = ItemAPI.createItem("§afinish creation or update of slots", Material.STONE_BUTTON);

    private final ItemStack barrier = ItemAPI.createItem("", Material.PINK_STAINED_GLASS_PANE);

    private final Location lrc;
    public SlotsCreationMenu(Location lrc, Player player)
    {
        super(player, 9*3, Main.getInstance(), "Slots creation menu");

        this.lrc = lrc;

        this.addEvents(this);
        openInventory();

        initialize();
        updateInventory();
    }


    public SlotsCreationMenu(PlayerSignsConfiguration conf, Player player)
    {
        this(conf.getLocation(), player);

        initializeValues();
    }

    private void initializeValues()
    {

    }

    private void initialize()
    {

        for(int i = 36; i <= 44; i++) bukkitInventory.setItem(i, barrier);
    }

    private void updateInventory()
    {
    }
}
