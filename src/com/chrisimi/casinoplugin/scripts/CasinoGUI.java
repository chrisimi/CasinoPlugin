package com.chrisimi.casinoplugin.scripts;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chrisimi.casinoplugin.main.Main;


/**
 * MENU 1
 * <p>
 * BLABLA
 *
 * @author chris
 */
public class CasinoGUI
{

    private static Inventory inv;
    @SuppressWarnings("unused")
    private Player player;
    private double playerBalance;

    private static HashMap<Player, CasinoGUI> guis = new HashMap<Player, CasinoGUI>();

    private int einsatz = 0;


    private int plus1;
    private int plus2;
    private int plus3;
    private int plus4;
    private int plus5;
    private int plus6;
    private Material plusBlock;
    private int minus1;
    private int minus2;
    private int minus3;
    private int minus4;
    private int minus5;
    private int minus6;
    private Material minusBlock;
    private Material informationBlock;
    private Material gapBlock;

    public CasinoGUI(Player player)
    {
        this.player = player;
        playerBalance = Main.econ.getBalance(player);

        inv = Bukkit.createInventory(player, 9 * 6, "Casino GUI");

        /*
        plus1 = (int) UpdateManager.getValue("bet-plus1");
        plus2 = (int) UpdateManager.getValue("bet-plus2");
        plus3 = (int) UpdateManager.getValue("bet-plus3");
        plus4 = (int) UpdateManager.getValue("bet-plus4");
        plus5 = (int) UpdateManager.getValue("bet-plus5");
        plus6 = (int) UpdateManager.getValue("bet-plus6");
        plusBlock = Enum.valueOf(Material.class, (String) UpdateManager.getValue("bet-plusBlock"));
        minus1 = (int) UpdateManager.getValue("bet-minus1");
        minus2 = (int) UpdateManager.getValue("bet-minus2");
        minus3 = (int) UpdateManager.getValue("bet-minus3");
        minus4 = (int) UpdateManager.getValue("bet-minus4");
        minus5 = (int) UpdateManager.getValue("bet-minus5");
        minus6 = (int) UpdateManager.getValue("bet-minus6");
        minusBlock = Enum.valueOf(Material.class, (String) UpdateManager.getValue("bet-minusBlock"));
        informationBlock = Enum.valueOf(Material.class, (String) UpdateManager.getValue("bet-informationBlock"));
        gapBlock = Enum.valueOf(Material.class, (String) UpdateManager.getValue("bet-inventoryMaterial"));
         */

        List<Object> objects = (List<Object>) UpdateManager.getValue("gui-elements");

        List<Object> a = (List<Object>) objects.get(0);


        addItemsToInv();

        guis.put(player, this);
        openCasinoGUI(player);
    }

    public static void openCasinoGUI(Player player)
    {
        player.openInventory(inv);

    }

    public void updateInv()
    { //aktualisierungs Methode um Inventar zu ver§ndern
        addItemsToInv();
    }

    private void addItemsToInv()
    {
        ItemStack sideMaterial = new ItemStack(gapBlock, 1);

        for (int i = 0; i < 6; i++)
        {
            inv.setItem(i * 9, sideMaterial);
            inv.setItem(i * 9 + 8, sideMaterial);
        }
        for (int i = 0; i < 3; i++)
        {
            inv.setItem(46 + i, sideMaterial);
            inv.setItem(52 - i, sideMaterial);
        }
        addPlusItems();
        addMinusItems();
        addInformationBlocks();
        addSignsAndButton();
    }

    private void addSignsAndButton()
    {
        int[] positions = {30, 32, 39, 41};
        for (int i = 0; i < 4; i++)
        {
            ItemStack material = new ItemStack(Material.SIGN);

            ItemMeta meta = material.getItemMeta();
            meta.setDisplayName("§fbet: " + einsatz);
            material.setItemMeta(meta);
            inv.setItem(positions[i], material);
        }
        ItemStack material = new ItemStack(Material.STONE_BUTTON);
        ItemMeta meta = material.getItemMeta();

        meta.setDisplayName("§f§bROLL");
        material.setItemMeta(meta);
        inv.setItem(49, material);

    }

    private void addInformationBlocks()
    { //informationBlocks
        ItemStack material = new ItemStack(informationBlock, 1);

        ItemStack block1 = new ItemStack(material.getType());
        ItemMeta block1Meta = block1.getItemMeta();
        block1Meta.setDisplayName("§4higher your bet!");
        block1.setItemMeta(block1Meta);
        inv.setItem(4, block1);

        ItemStack block2 = new ItemStack(material.getType());
        ItemMeta block2Meta = block2.getItemMeta();
        block2Meta.setDisplayName("§4lower your bet!");
        block2.setItemMeta(block2Meta);
        inv.setItem(13, block2);

    }

    private void addMinusItems()
    {
        ItemStack material = new ItemStack(minusBlock, 1);

        if (einsatz >= this.minus1)
        {
            ItemStack minus1 = new ItemStack(material.getType());
            ItemMeta minus1Meta = minus1.getItemMeta();
            minus1Meta.setDisplayName("§4- " + this.minus1);
            minus1.setItemMeta(minus1Meta);
            inv.setItem(10, minus1);
        } else inv.setItem(10, null);
        if (einsatz >= this.minus2)
        {
            ItemStack minus5 = new ItemStack(material.getType());
            ItemMeta minus5Meta = minus5.getItemMeta();
            minus5Meta.setDisplayName("§4 - " + this.minus2);
            minus5.setItemMeta(minus5Meta);
            inv.setItem(11, minus5);
        } else inv.setItem(11, null);
        if (einsatz >= this.minus3)
        {

            ItemStack minus10 = new ItemStack(material.getType());
            ItemMeta minus10Meta = minus10.getItemMeta();
            minus10Meta.setDisplayName("§4 - " + this.minus3);
            minus10.setItemMeta(minus10Meta);
            inv.setItem(12, minus10);
        } else inv.setItem(12, null);
        if (einsatz >= this.minus4)
        {

            ItemStack minus50 = new ItemStack(material.getType());
            ItemMeta minus50Meta = minus50.getItemMeta();
            minus50Meta.setDisplayName("§4 - " + this.minus4);
            minus50.setItemMeta(minus50Meta);
            inv.setItem(14, minus50);
        } else inv.setItem(14, null);
        if (einsatz >= this.minus5)
        {

            ItemStack minus100 = new ItemStack(material.getType());
            ItemMeta minus100Meta = minus100.getItemMeta();
            minus100Meta.setDisplayName("§4 - " + this.minus5);
            minus100.setItemMeta(minus100Meta);
            inv.setItem(15, minus100);
        } else inv.setItem(15, null);
        if (einsatz >= this.minus6)
        {
            ItemStack minus500 = new ItemStack(material.getType());
            ItemMeta minus500Meta = minus500.getItemMeta();
            minus500Meta.setDisplayName("§4 - " + this.minus6);
            minus500.setItemMeta(minus500Meta);
            inv.setItem(16, minus500);
        } else inv.setItem(16, null);

    }

    private void addPlusItems()
    {
        ItemStack material = new ItemStack(plusBlock, 1);

        if (einsatz + plus1 <= playerBalance)
        {
            ItemStack plus1 = new ItemStack(material.getType());
            ItemMeta plus1Meta = material.getItemMeta();
            plus1Meta.setDisplayName("§2+ " + this.plus1);
            plus1.setItemMeta(plus1Meta);
            inv.setItem(1, plus1);
        } else inv.setItem(1, null);

        if (einsatz + plus2 <= playerBalance)
        {
            ItemStack plus5 = new ItemStack(material.getType());
            ItemMeta plus5Meta = plus5.getItemMeta();
            plus5Meta.setDisplayName("§2+ " + this.plus2);
            plus5.setItemMeta(plus5Meta);
            inv.setItem(2, plus5);
        } else inv.setItem(2, null);

        if (einsatz + plus3 <= playerBalance)
        {
            ItemStack plus10 = new ItemStack(material.getType());
            ItemMeta plus10Meta = plus10.getItemMeta();
            plus10Meta.setDisplayName("§2+ " + this.plus3);
            plus10.setItemMeta(plus10Meta);
            inv.setItem(3, plus10);
        } else inv.setItem(3, null);
        //slot 4 diamondblock

        if (einsatz + plus4 <= playerBalance)
        {
            ItemStack plus50 = new ItemStack(material.getType());
            ItemMeta plus50Meta = plus50.getItemMeta();
            plus50Meta.setDisplayName("§2+ " + this.plus4);
            plus50.setItemMeta(plus50Meta);
            inv.setItem(5, plus50);
        } else inv.setItem(5, null);

        if (einsatz + plus5 <= playerBalance)
        {
            ItemStack plus100 = new ItemStack(material.getType());
            ItemMeta plus100Meta = plus100.getItemMeta();
            plus100Meta.setDisplayName("§2+ " + this.plus5);
            plus100.setItemMeta(plus100Meta);
            inv.setItem(6, plus100);
        } else inv.setItem(6, null);

        if (einsatz + plus6 <= playerBalance)
        {
            ItemStack plus500 = new ItemStack(material.getType());
            ItemMeta plus500Meta = plus500.getItemMeta();
            plus500Meta.setDisplayName("§2+ " + this.plus6);
            plus500.setItemMeta(plus500Meta);
            inv.setItem(7, plus500);

        } else inv.setItem(7, null);
    }


    public static Inventory getInv()
    {
        return inv;
    }

    public static void addEinsatz(int amount, Player player)
    {
        CasinoGUI gui = guis.get(player);
        if (gui == null) return;


        if (amount <= 0)
        {
            CasinoManager.LogWithColor(ChatColor.RED + "not allowed operation: addEinsatz: " + amount);
        } else
            gui.einsatz += amount;

        gui.updateInv();
    }

    public static void startAnimation(Player player)
    {
        int einsatz = guis.get(player).einsatz;
        player.closeInventory();
        new CasinoAnimation(player, einsatz);
    }

    public static void subEinsatz(int amount, Player player)
    {
        CasinoGUI gui = guis.get(player);
        if (gui == null) return;


        if (amount > gui.einsatz)
        {
            gui.einsatz = 0;
        } else
        {
            gui.einsatz -= amount;
        }
        gui.updateInv();
    }

    public static void removeInventory(Player player)
    {
        //Bukkit.getLogger().info("GUI count: " + guis.size());
        guis.remove(player);
        //Bukkit.getLogger().info("deleted GUI of player: " + player.getName());
        //Bukkit.getLogger().info("GUI count: " + guis.size());
    }

    public static void onDisable()
    {
        for (Entry<Player, CasinoGUI> entry : guis.entrySet())
        {
            entry.getKey().closeInventory();
            guis.remove(entry.getKey());
        }
    }

}
