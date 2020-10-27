package com.chrisimi.casinoplugin.menues;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.serializables.Jackpot;
import com.chrisimi.inventoryapi.IInventoryAPI;
import com.chrisimi.inventoryapi.Inventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

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
            }
        }
    };

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
}
