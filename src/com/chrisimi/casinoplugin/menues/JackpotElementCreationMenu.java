package com.chrisimi.casinoplugin.menues;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.serializables.Jackpot;
import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    private final JackpotCreationMenu jackpotCreationMenu;

    private List<ItemStack> itemStacks = new ArrayList<>();
    private final ItemStack backButton = ItemAPI.createItem("§2back", Material.STONE_BUTTON);
    private final ItemStack fillMaterial = ItemAPI.createItem("", Material.PINK_STAINED_GLASS_PANE);

    public JackpotElementCreationMenu(Player player, JackpotCreationMenu jackpotCreationMenu)
    {
        super(player, 9*6, Main.getInstance(), "jackpot element creation menu");
        this.jackpotCreationMenu = jackpotCreationMenu;

        addEvents(this);
        updateInventory();

        getInventory().setItem(45, backButton);
    }

    //runnable which checks the 49th slot because of the input
    private Runnable checkInputSlot = new Runnable()
    {
        @Override
        public void run()
        {
            if(itemStacks.size() <= LIMIT && getInventory().getItem(49) != null && !containsMaterial(jackpotCreationMenu.elementList, getInventory().getItem(49).getType()))
            {
                //add item as element
                waitforChatInput(player);
                waitingFor = WaitingFor.WEIGHT;
                closeInventory();
                player.sendMessage("type in the weight, total weight " + String.format("#.##", totalWeight(jackpotCreationMenu.elementList)));
                newElementMaterial = getInventory().getItem(49).getType();
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
                    player.sendMessage("type in the win multiplicand for this block... if you want to trigger the jackpot type '§etrigger'");
                    return;
                } catch(Exception e)
                {
                    player.sendMessage("This is not a valid number. Try it again or exit with 'exit'");
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
                        player.sendMessage("This is not a valid number. Try it again or exit with §e'exit'");
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
        if(event.getClicked().equals(backButton))
        {
            closeInventory();
            jackpotCreationMenu.openInventory();
        }
    }

    private void addNewItem()
    {
        Jackpot.JackpotElement element = new Jackpot.JackpotElement();
        element.material = newElementMaterial;
        element.triggerJackpot = newElementIsJackpotTrigger;
        element.weight = newElementWeight;
        element.winMultiplicator = newElementWinMultiplicator;
        jackpotCreationMenu.elementList.add(element);

        updateInventory();
    }


    private void updateInventory()
    {
        updateItems();

        for(int i = 0; i < LIMIT; i++)
        {
            if(itemStacks.get(i) != null)
                getInventory().setItem(i, itemStacks.get(i));
            else
                getInventory().setItem(i, fillMaterial);
        }
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
            lore.add("chance: " + Math.round((element.weight / totalWeight(jackpotCreationMenu.elementList)) * 100) / 100);
            ItemAPI.setLore(itemStack, lore);

            itemStacks.add(itemStack);
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

    public double totalWeight(List<Jackpot.JackpotElement> elements)
    {
        double result = 0.0;
        for(Jackpot.JackpotElement element : elements)
            result += element.weight;

        return result;
    }
}
