package com.chrisimi.casinoplugin.menues;

import com.chrisimi.casinoplugin.jackpot.JackpotManager;
import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.serializables.Jackpot;
import com.chrisimi.casinoplugin.utils.CommandUtils;
import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class JackpotCreationMenu extends Inventory implements IInventoryAPI
{
    private enum WaitingFor
    {
        NONE,
        POS_1,
        POS_2,
        NAME
    }

    private final ItemStack setPos1 = ItemAPI.createItem("§6set position 1 of the jackpot area", Material.GOLD_INGOT);
    private final ItemStack setPos2 = ItemAPI.createItem("§6set position 2 of the jackpot area", Material.GOLD_INGOT);
    private final ItemStack setName = ItemAPI.createItem("§6set the name", Material.SIGN);
    private final ItemStack finishButton = ItemAPI.createItem("§6finish creation or update", Material.STONE_BUTTON);
    private final ItemStack setServerJackpot = ItemAPI.createItem("§6make jackpot server-managed", Material.GOLD_BLOCK);
    private final ItemStack setPlayerJackpot = ItemAPI.createItem("§6make jackpot player-managed", Material.COAL_BLOCK);

    private final ItemStack openElementInventory = ItemAPI.createItem("§6edit elements", Material.BOOK);

    private Location pos1 = null;
    private Location pos2 = null;
    private String name = null;
    private boolean isServerJackpot = false;
    public List<Jackpot.JackpotElement> elementList = new ArrayList<>();

    private WaitingFor waitingFor = WaitingFor.NONE;
    private boolean allValuesCorrect = false;


    private JackpotElementCreationMenu jackpotElementCreationMenu = null;

    /**
     * the create constructor
     * @param player the player for whom should the inventory be created
     */
    public JackpotCreationMenu(Player player)
    {
        super(player, 9, Main.getInstance(), "Jackpot creation menu");

        addEvents(this);

        getInventory().setItem(0, setPos1);
        getInventory().setItem(1, setPos2);
        getInventory().setItem(3, setName);
        getInventory().setItem(5, openElementInventory);
        getInventory().setItem(8, finishButton);
    }
    //TODO add constructor for edditing an existing jackpot

    @EventMethodAnnotation
    public void onClick(ClickEvent event)
    {
        if(event.getClicked().equals(openElementInventory))
        {
            if(jackpotElementCreationMenu == null)
                jackpotElementCreationMenu = new JackpotElementCreationMenu(player, this);

            closeInventory();
            jackpotElementCreationMenu.openInventory();
        }
        else if(event.getClicked().equals(setPos1)) setPos1();
        else if(event.getClicked().equals(setPos2)) setPos2();
        else if(event.getClicked().equals(setName)) setName();
        else if(event.getClicked().equals(finishButton)) finish();
    }

    private void finish()
    {
        if(!allValuesCorrect) return;

        //TODO add finish button
    }

    private void setName()
    {
        waitingFor = WaitingFor.NAME;
        closeInventory();
        waitforChatInput(player);
        player.sendMessage("Type in the name of the jackpt - it must be unique!");
    }

    private void setPos2()
    {
        waitingFor = WaitingFor.POS_2;
        closeInventory();
        waitforChatInput(player);
        player.sendMessage("Go to the bottom right block of your jackpot display. When you look at the bock type something in the chat.");
    }

    private void setPos1()
    {
        waitingFor = WaitingFor.POS_1;
        closeInventory();
        waitforChatInput(player);
        player.sendMessage("Go to the top left block of your jackpot display. When you look at the block type something in the chat.");
    }

    @EventMethodAnnotation
    public void onChat(ChatEvent event)
    {
        switch(waitingFor)
        {
            case POS_1:
                pos1 = getTargetLocation();
                break;
            case POS_2:
                pos2 = getTargetLocation();
                break;
            case NAME:
            {
                if(event.getMessage().equalsIgnoreCase("exit"))
                {
                    waitingFor = WaitingFor.NONE;
                    openInventory();
                }

                if(!JackpotManager.doesNameExists(event.getMessage()))
                {
                    name = event.getMessage();
                }
                else
                {
                    player.sendMessage("Name does exists! Try again or exit with 'exit'");
                    waitforChatInput(player);
                }
            }
        }
    }

    private void updateInventory()
    {
        if(Main.perm.has(player, "casino.jackpot.server") || Main.perm.has(player, "casino.admin"))
        {
            getInventory().setItem(8, (isServerJackpot) ? setPlayerJackpot : setServerJackpot);
        }
    }

    private Location getTargetLocation()
    {
        Block block = player.getTargetBlock(null, 10);
        if(block != null) return block.getLocation();

        return null;
    }
}
