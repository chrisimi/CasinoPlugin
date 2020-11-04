package com.chrisimi.casinoplugin.scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.ChatEvent;
import com.chrisimi.inventoryapi.ClickEvent;
import com.chrisimi.inventoryapi.EventMethodAnnotation;
import com.chrisimi.inventoryapi.IInventoryAPI;
import com.chrisimi.numberformatter.NumberFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chrisimi.casinoplugin.main.Main;


/**
 * //TODO rename to CasinoSlotsGUIManager
 *
 * @author chris
 */
public class CasinoSlotsGUIManager extends com.chrisimi.inventoryapi.Inventory implements IInventoryAPI
{

    private static List<CasinoAnimation.SlotsGUIElement> elements = new ArrayList<>();
    private static Double[] bets = new Double[6];
    private Map<ItemStack, Double> blocks = new HashMap<>();

    private static ItemStack fillMaterial = ItemAPI.createItem("", Material.PINK_STAINED_GLASS_PANE);
    private static ItemStack rollButton = ItemAPI.createItem("§6roll", Material.STONE_BUTTON);
    private static ItemStack betSign = ItemAPI.createItem("§6bet: 0.0", Material.SIGN);
    private static ItemStack informationBlock = ItemAPI.createItem("§6change your bets by clicking on the blocks", Material.DIAMOND_BLOCK);

    private static Material plusBlock = Material.GREEN_WOOL;
    private static Material minusBlock = Material.RED_WOOL;

    private double playerBalance = 0.0;
    protected double currentBet = 0.0;

    private CasinoAnimation casinoAnimation = null;

    public CasinoSlotsGUIManager(Player player)
    {
        super(player, 9*6, Main.getInstance(), "Casino Slots GUI");
        addEvents(this);
        openInventory();

        playerBalance = Main.econ.getBalance(player);
        updateVariables();
        initialize();

        updateInventory();
    }

    private void initialize()
    {
        //init bet sign lore
        List<String> lore = new ArrayList<String>();
        lore.add("click to manually set the bet");
        ItemAPI.setLore(betSign, lore);
    }

    private void updateInventory()
    {
        updateBlocks();

        getInventory().setItem(31, rollButton);

        ItemAPI.changeName(betSign, "§6bet: " + NumberFormatter.format(this.currentBet, false));

        getInventory().setItem(30, betSign);
        getInventory().setItem(32, betSign);
        getInventory().setItem(39, betSign);
        getInventory().setItem(41, betSign);

    }

    private void updateBlocks()
    {
        blocks.clear();

        for(int i = 0; i < getInventory().getSize(); i++)
            getInventory().setItem(i, fillMaterial);

        for(int i = 0; i < bets.length; i++)
        {
            ItemStack plusBlockItem = ItemAPI.createItem("§a+ " + NumberFormatter.format(bets[i]), plusBlock);

            if(playerBalance >= bets[i])
            {
                getInventory().setItem(((i > 2) ? i + 2 : i + 1), plusBlockItem);
                blocks.put(plusBlockItem, bets[i]);
            }
        }

        for(int i = 0; i < bets.length; i++)
        {
            ItemStack minusBlockItem = ItemAPI.createItem("§4- " + NumberFormatter.format(bets[i]), minusBlock);

            if(currentBet >= bets[i])
            {
                getInventory().setItem(((i > 2) ? i + 11 : i + 10), minusBlockItem);
                blocks.put(minusBlockItem, -bets[i]);
            }
        }
    }

    private void updateVariables()
    {
        try
        {
            fillMaterial = new ItemStack(Enum.valueOf(Material.class, UpdateManager.getValue("gui-inventoryMaterial", Material.PINK_STAINED_GLASS_PANE).toString()));
        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to parse fill material: " + e.getMessage() + ". Set to default value: PINK_STAINED_GLASS_PANE");
        }

        try
        {
            plusBlock = Enum.valueOf(Material.class, UpdateManager.getValue("gui-plusBlock", Material.GREEN_WOOL).toString());
        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to parse plus block material: " + e.getMessage() + ". Set to default value: GREEN_WOOL");
        }

        try
        {
            minusBlock = Enum.valueOf(Material.class, UpdateManager.getValue("gui-minusBlock", Material.RED_WOOL).toString());
        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to parse minus block material: " + e.getMessage() + ". Set to default value: RED_WOOL");
        }

        try
        {
            List<Object> objects = (List<Object>) UpdateManager.getValue("gui-elements");

            if(objects.size() < 3) throw new Exception("not enough elements");


            //string(material); double(winMutliplicand); double(weight)
            //TODO rewrite and make it more typesafe
            for(Object element : objects)
            {
                List<Object> list = (List<Object>) element;

                CasinoAnimation.SlotsGUIElement guiElement = new CasinoAnimation.SlotsGUIElement();
                guiElement.material = Enum.valueOf(Material.class, list.get(0).toString());
                guiElement.winMultiplicand = Double.parseDouble(list.get(1).toString());
                guiElement.weight = Double.parseDouble(list.get(2).toString());

                elements.add(guiElement);
            }

        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to parse gui elements: " + e.getMessage()
                    + ". Set to default value.");
            elements.add(new CasinoAnimation.SlotsGUIElement(Material.REDSTONE_BLOCK, 3.0, 2.0));
            elements.add(new CasinoAnimation.SlotsGUIElement(Material.DIAMOND_BLOCK, 5.0, 3.0));
            elements.add(new CasinoAnimation.SlotsGUIElement(Material.EMERALD_BLOCK, 7.0, 5.0));
        }

        try
        {
            List<Double> elements = (List<Double>)UpdateManager.getValue("gui-list");

            if(elements.size() != 6) throw new Exception("there are not 6 elements!");

            for(int i = 0; i < 6; i++)
            {
                bets[i] = elements.get(i);
            }

        } catch(Exception e)
        {
            CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to parse bet list: " + e.getMessage()
                    + ". Set to default value");
            bets = new Double[] {1.0, 5.0, 10.0, 50.0, 100.0, 500.0};
        }

        //TODO add try catch for informationBlock
    }

    @EventMethodAnnotation
    public void onClick(ClickEvent event)
    {
        if(event.getClicked().equals(betSign)) setBet();
        else if(event.getClicked().equals(rollButton)) rollButton();

        for(Map.Entry<ItemStack, Double> entry : blocks.entrySet())
        {
            if(event.getClicked().equals(entry.getKey()))
                currentBet += entry.getValue();
        }

        updateInventory();
    }

    @EventMethodAnnotation
    public void onChat(ChatEvent event)
    {
        try
        {
            currentBet = Double.parseDouble(event.getMessage());
        } catch(Exception e)
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("&4This is not a valid number!"));
        }
        openInventory();
    }

    private void rollButton()
    {
        if(casinoAnimation == null)
            casinoAnimation = new CasinoAnimation(player, elements, this);

        closeInventory();
        casinoAnimation.openInventory();
    }

    private void setBet()
    {
        closeInventory();
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-bet_message"));
        waitforChatInput(player);
    }
    /*
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
     */
}
