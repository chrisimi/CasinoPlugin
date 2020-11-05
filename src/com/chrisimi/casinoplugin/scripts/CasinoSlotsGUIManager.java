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
        updateInventory();
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
}
