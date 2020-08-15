package com.chrisimi.casinoplugin.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemAPI {

    /**
     * create a item stack with a customized display name
     * @param name
     * @param material
     * @return
     */
    public static ItemStack createItem(String name, Material material)
    {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    /**
     * change the display name of the item stack
     * @param itemStack instance of the item stack
     * @param newName the new name of the item stack
     */
    public static void changeName(ItemStack itemStack, String newName)
    {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(newName);

        itemStack.setItemMeta(meta);
    }
    public static void setLore(ItemStack itemStack, List<String> elements)
    {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(elements);
        itemStack.setItemMeta(meta);
    }

    /**
     * tries to put a item into the inventory of the player, if the inventory is full, the item will be dropped above him
     * @param itemStack {@linkplain ItemStack} instance to drop
     * @param player {@linkplain Player} player who should get the item
     */
    public static void putItemInInventory(ItemStack itemStack, Player player)
    {
        int slot = player.getInventory().first(Material.AIR);
        if(slot == -1)
            player.getWorld().dropItem(player.getLocation().add(0, 3, 0), itemStack);
        else
            player.getInventory().setItem(slot, itemStack);
    }

}
