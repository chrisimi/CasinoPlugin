package com.chrisimi.casinoplugin.menues;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.serializables.Jackpot;
import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.*;
import com.chrisimi.numberformatter.NumberFormatter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * the inventory to manage the elements of a jackpot
 *
 * perhaps copy function?
 */
public class JackpotElementCreationMenu extends Inventory implements IInventoryAPI
{
    private enum WaitingFor
    {
        NONE,
        WEIGHT,
        WIN_MULTIPLICATOR
    }

    private final int LIMIT =  9*5;
    private WaitingFor waitingFor = WaitingFor.NONE;

    private double newElementWeight = 0.0;
    private double newElementWinMultiplicator = 0.0;
    private Material newElementMaterial = null;
    private boolean newElementIsJackpotTrigger = false;
    private Jackpot.JackpotElement elementToEdit = null;


    private final JackpotCreationMenu jackpotCreationMenu;

    private Map<Jackpot.JackpotElement, ItemStack> itemStacks = new HashMap<>();
    private final ItemStack backButton = ItemAPI.createItem("§2back", Material.STONE_BUTTON);
    private final ItemStack fillMaterial = ItemAPI.createItem("", Material.PINK_STAINED_GLASS_PANE);
    private static final ItemStack informationSign = ItemAPI.createItem("§6INFORMATION", Material.SIGN);

    public JackpotElementCreationMenu(Player player, JackpotCreationMenu jackpotCreationMenu)
    {
        super(player, 9*6, Main.getInstance(), "jackpot element creation menu");
        this.jackpotCreationMenu = jackpotCreationMenu;

        addEvents(this);
        updateInventory();

        getInventory().setItem(45, backButton);

        List<String> lore = new ArrayList<>();
        lore.add("click on an element in your inventory - must be a solid block to add it to the jackpot");
        lore.add("click on an element in this inventory to remove it from the jackpot");
        ItemAPI.setLore(informationSign, lore);
        getInventory().setItem(53, informationSign);
    }

    //runnable which checks the 49th slot because of the input
    private Runnable checkInputSlot = new Runnable()
    {
        @Override
        public void run()
        {
            if(itemStacks.size() <= LIMIT && getInventory().getItem(49) != null && !containsMaterial(jackpotCreationMenu.elementList, getInventory().getItem(49).getType()))
            {
                if(getInventory().getItem(49).getType().isBlock())
                {
                    //add item as element
                    startNewItemEvent();
                    newElementMaterial = getInventory().getItem(49).getType();
                }
                else
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("jackpot-creation-element-not_block"));
            }
        }
    };

    @EventMethodAnnotation
    public void onChatInput(ChatEvent event)
    {
        if(event.getMessage().equalsIgnoreCase("exit"))
        {
            openInventory();
            newElementWeight = 0.0;
            newElementMaterial = null;
            newElementWinMultiplicator = 0.0;
            newElementIsJackpotTrigger = false;
            waitingFor = WaitingFor.NONE;
            return;
        }

        switch(waitingFor)
        {
            case WEIGHT:
            {
                try
                {
                    double a = Double.parseDouble(event.getMessage());
                    newElementWeight = a;

                    waitingFor = WaitingFor.WIN_MULTIPLICATOR;
                    waitforChatInput(player);
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("jackpot-creation-element-win_multiplicand"));
                    return;
                } catch(Exception e)
                {
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("jackpot-creation-error_invalid_number"));
                    waitforChatInput(player);
                    return;
                }
            }
            case WIN_MULTIPLICATOR:
            {
                if(event.getMessage().equalsIgnoreCase("trigger") || event.getMessage().contains("trigger"))
                {
                    newElementIsJackpotTrigger = true;
                    waitingFor = WaitingFor.NONE;
                    openInventory();
                    addNewItem();
                    return;
                }
                else
                {
                    try
                    {
                         double a = Double.parseDouble(event.getMessage());
                         newElementIsJackpotTrigger = false;
                         newElementWinMultiplicator = a;

                         waitingFor = WaitingFor.NONE;
                         openInventory();
                         addNewItem();
                    } catch(Exception e)
                    {
                        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("jackpot-creation-error_invalid_number"));
                        waitforChatInput(player);
                        return;
                    }
                }
            }
        }
    }

    @EventMethodAnnotation
    public void onClick(ClickEvent event)
    {
        for(Map.Entry<Jackpot.JackpotElement, ItemStack> elementItemStackEntry : itemStacks.entrySet())
        {
            if(event.getClicked().equals(elementItemStackEntry.getValue()))
            {
                //delete element
                jackpotCreationMenu.elementList.remove(elementItemStackEntry.getKey());
                updateInventory();
                return;
            }
        }

        if(event.getClicked().equals(backButton))
        {
            closeInventory();
            jackpotCreationMenu.updateInventory();
            jackpotCreationMenu.openInventory();
            return;
        }

        //add item if not in the same inventory
        if(!event.getClicked().equals(fillMaterial))
        {
            if(itemStacks.size() <= LIMIT && !containsMaterial(jackpotCreationMenu.elementList, event.getClicked().getType()))
            {
                if(event.getClicked().getType().isBlock())
                {
                    startNewItemEvent();
                    newElementMaterial = event.getClicked().getType();
                }
                else
                {
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("jackpot-creation-element-not_block"));
                }

            }
        }
    }

    private void startNewItemEvent()
    {
        waitforChatInput(player);
        waitingFor = WaitingFor.WEIGHT;
        closeInventory();
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("jackpot-creation-element-weight")
                .replace("%total_weight%", String.valueOf(totalWeight(jackpotCreationMenu.elementList))));
    }

    private void addNewItem()
    {
        if(elementToEdit != null)
        {
            elementToEdit.material = newElementMaterial;
            elementToEdit.triggerJackpot = newElementIsJackpotTrigger;
            elementToEdit.weight = newElementWeight;
            elementToEdit.winMultiplicator = newElementWinMultiplicator;
        }
        else
        {
            Jackpot.JackpotElement element = new Jackpot.JackpotElement();
            element.material = newElementMaterial;
            element.triggerJackpot = newElementIsJackpotTrigger;
            element.weight = newElementWeight;
            element.winMultiplicator = newElementWinMultiplicator;
            jackpotCreationMenu.elementList.add(element);
        }

        updateInventory();

        elementToEdit = null;
    }


    private void updateInventory()
    {
        updateItems();

        int index = 0;

        for(Map.Entry<Jackpot.JackpotElement, ItemStack> entry : itemStacks.entrySet())
        {
            if(index >= LIMIT) continue;

            getInventory().setItem(index, entry.getValue());
            index++;
        }

        for(int i = index; i < LIMIT; i++)
            getInventory().setItem(i, fillMaterial);
    }

    private void updateItems()
    {
        itemStacks.clear();
        //sort
        jackpotCreationMenu.elementList.sort(new Comparator<Jackpot.JackpotElement>()
        {
            @Override
            public int compare(Jackpot.JackpotElement o1, Jackpot.JackpotElement o2)
            {
                if(o1.triggerJackpot && !o2.triggerJackpot) return 1;
                else if (o2.triggerJackpot && !o1.triggerJackpot) return -1;
                else
                    return 0;
            }
        });

        for(Jackpot.JackpotElement element : jackpotCreationMenu.elementList)
        {
            ItemStack itemStack = null;
            if(element.triggerJackpot)
                itemStack = ItemAPI.createItem("§6jackpot-trigger", element.material);
            else
                itemStack = ItemAPI.createItem("§6" + element.winMultiplicator + "x the bet", element.material);

            List<String> lore = new ArrayList<>();
            lore.add("chance: " + Math.round((element.weight / totalWeight(jackpotCreationMenu.elementList)) * 10000.0) / 100.0 + " %");
            ItemAPI.setLore(itemStack, lore);

            itemStacks.put(element, itemStack);
        }
    }

    private boolean containsMaterial(List<Jackpot.JackpotElement> elementList, Material material)
    {
        for(Jackpot.JackpotElement element : elementList)
        {
            if(material.equals(element.material))
            {
                return true;
            }
        }

        return false;
    }

    public static double totalWeight(List<Jackpot.JackpotElement> elements)
    {
        double result = 0.0;
        for(Jackpot.JackpotElement element : elements)
            result += element.weight;

        return result;
    }
}
