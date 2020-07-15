package com.chrisimi.casinoplugin.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
}
