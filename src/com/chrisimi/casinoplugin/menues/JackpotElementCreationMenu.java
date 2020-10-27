package com.chrisimi.casinoplugin.menues;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.serializables.Jackpot;
import com.chrisimi.inventoryapi.ChatEvent;
import com.chrisimi.inventoryapi.EventMethodAnnotation;
import com.chrisimi.inventoryapi.IInventoryAPI;
import com.chrisimi.inventoryapi.Inventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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

    public JackpotElementCreationMenu(Player player, JackpotCreationMenu jackpotCreationMenu)
    {
        super(player, 9*6, Main.getInstance(), "jackpot element creation menu");
        this.jackpotCreationMenu = jackpotCreationMenu;

        addEvents(this);
    }

    //runnable which checks the 49th slot because of the input
    private Runnable checkInputSlot = new Runnable()
    {
        @Override
        public void run()
        {
            if(getInventory().getItem(49) != null && !containsMaterial(jackpotCreationMenu.elementList, getInventory().getItem(49).getType()))
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

    private void addNewItem()
    {

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
