package com.chrisimi.casinoplugin.menues;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.swing.*;

public class SlotsCreationMenu extends Inventory implements IInventoryAPI
{
    private enum WaitingFor
    {
        NONE,
        ADDOPTION1,
        ADDOPTION2,
        ADDOPTION3
    }

    private class Element
    {
        public String symbol;
        public double weight;
        public double winMultiplicand;

        public ItemStack changeOptionItemStack;
        public ItemStack changeChanceItemStack;
        public ItemStack changeWinMultiplicandItemStack;
        public ItemStack removeOptionItemStack;
    }


    private final ItemStack addOption = ItemAPI.createItem("§6add option", Material.STONE_BUTTON);
    private final ItemStack removeOption = ItemAPI.createItem("§aremove option", Material.RED_BANNER); //when click you can remove this element
    private final ItemStack changeChanceOfOption = ItemAPI.createItem("§6change option", Material.YELLOW_BANNER); //when click you can change the chance to get it
    private final ItemStack showOption = ItemAPI.createItem("§6{option numr} {option}", Material.BLUE_BANNER); //when click you can change the item like 'A' to 'B'
    private final ItemStack changeWinMultiplicandOfOption = ItemAPI.createItem("§6{win multiplicand}", Material.BROWN_BANNER); //when click you can change the win multiplicand if you get 3 in a row

    private final ItemStack setBet = ItemAPI.createItem("§6set bet", Material.GOLD_INGOT);
    private final ItemStack setServerSign = ItemAPI.createItem("§6to server sign", Material.GOLD_BLOCK);
    private final ItemStack disableSign = ItemAPI.createItem("§4disable sign", Material.RED_WOOL);
    private final ItemStack enableSign = ItemAPI.createItem("§aenable sign", Material.GREEN_WOOL);
    private final ItemStack finishButton = ItemAPI.createItem("§afinish creation or update of slots", Material.STONE_BUTTON);

    private final ItemStack barrier = ItemAPI.createItem("", Material.PINK_STAINED_GLASS_PANE);

    private Element[] elements = new Element[9];
    private int currAddPos = 0;
    private WaitingFor waitingFor = WaitingFor.NONE;

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

    }

    private void updateInventory()
    {
        sortOptions();
        //symbols things
        bukkitInventory.setItem(currAddPos, addOption);
        updateSymbolsInventory();

    }

    /**
     * sort the options in elem array so that there are no empty spaces
     */
    private void sortOptions()
    {
        for(int i = 0; i < 9; i++)
        {
            for(int j = i + 1; j < 9; j++)
            {
                //move one back
                if(elements[i] == null && elements[j] != null)
                {
                    elements[i] = elements[j];
                    elements[j] = null;
                }
            }
        }
    }

    private void updateSymbolsInventory()
    {
        for(int i = 0; i < elements.length; i++)
        {
            if(elements[i] == null) continue;

            ItemStack changeOption = new ItemStack(showOption.getType());
            ItemStack changeChance = new ItemStack(changeChanceOfOption.getType());
            ItemStack changeWinMultiplicand = new ItemStack(changeWinMultiplicandOfOption.getType());
            ItemStack removeOptionIS = new ItemStack(removeOption.getType());

            ItemAPI.changeName(changeOption, String.format("§6%s. %s §6- change symbol", i, elements[i].symbol));
            ItemAPI.changeName(changeChance, String.format("§6%s of %s (%.2f) - change weight", elements[i].weight, getWeightSum(), elements[i].weight / getWeightSum()));
            ItemAPI.changeName(changeWinMultiplicand, String.format("§6%.1f x - change win multiplicand", elements[i].winMultiplicand));
            ItemAPI.changeName(removeOptionIS, "§4remove option");

            elements[i].changeOptionItemStack = changeOption;
            elements[i].changeChanceItemStack = changeChance;
            elements[i].changeWinMultiplicandItemStack = changeWinMultiplicand;
            elements[i].removeOptionItemStack = removeOptionIS;

            bukkitInventory.setItem(i, changeOption);
            bukkitInventory.setItem(i + 9, changeChance);
            bukkitInventory.setItem(i + 18, changeWinMultiplicand);
            bukkitInventory.setItem(i + 27, removeOptionIS);
        }
    }

    private double getWeightSum()
    {
        double sum = 0.0;
        for(int i = 0; i < elements.length; i++)
            if(elements[i] != null)
                sum += elements[i].weight;

        return sum;
    }

    @EventMethodAnnotation
    public void onClick(ClickEvent event)
    {
        if(event.getClicked().equals(addOption)) addOption();

        for(int i = 0; i < elements.length; i++)
            if(elements[i] != null)
            {
                if(event.getClicked().equals(elements[i].changeOptionItemStack)) changeOption(i);
                else if(event.getClicked().equals(elements[i].changeChanceItemStack)) changeChance(i);
                else if(event.getClicked().equals(elements[i].changeWinMultiplicandItemStack)) changeWinMultiplicand(i);
                else if(event.getClicked().equals(elements[i].removeOptionItemStack)) removeOption(i);
            }

        updateInventory();

    }

    private void changeOption(int index)
    {

    }

    private void changeChance(int index)
    {

    }

    private void changeWinMultiplicand(int index)
    {

    }

    private void removeOption(int index)
    {
        elements[index] = null;
    }

    private void addOption()
    {
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-slots-set_elem_symbol"));
        closeInventory();
        waitforChatInput(player);
        waitingFor = WaitingFor.ADDOPTION1;
    }

    @EventMethodAnnotation
    public void onChat(ChatEvent event)
    {
        switch(waitingFor)
        {
            case ADDOPTION1:
                //create new element at current pos
                elements[currAddPos] = new Element();
                elements[currAddPos].symbol = event.getMessage();

                //prepare for next chat input
                player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-slots-set_elem_weight").replace("{total_weight}", String.format("%.2f", getWeightSum())));
                waitforChatInput(player);
                waitingFor = WaitingFor.ADDOPTION2;
                return;
            case ADDOPTION2:
            {
                try
                {
                    //set value
                    elements[currAddPos].weight = Double.parseDouble(event.getMessage());

                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-slots-set_elem_win_multiplicand"));
                    waitforChatInput(player);
                    waitingFor = WaitingFor.ADDOPTION3;
                    return;
                } catch(Exception e)
                {
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-double_invalid"));
                    elements[currAddPos] = null;
                }
            }
            case ADDOPTION3:
            {
                try
                {
                    //set value
                    elements[currAddPos].winMultiplicand = Double.parseDouble(event.getMessage());

                    //proceed with value

                } catch(Exception e)
                {
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-double_invalid"));
                    elements[currAddPos] = null;
                }
            }
        }

        updateInventory();
        openInventory();
        waitingFor = WaitingFor.NONE;
    }
}
