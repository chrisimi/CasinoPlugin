package com.chrisimi.casinoplugin.menues;

import com.chrisimi.casinoplugin.jackpot.JackpotManager;
import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.serializables.Jackpot;
import com.chrisimi.casinoplugin.utils.CommandUtils;
import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.*;
import com.chrisimi.numberformatter.NumberFormatter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class JackpotCreationMenu extends Inventory implements IInventoryAPI
{
    //TODO add message.yml integration

    private enum WaitingFor
    {
        NONE,
        POS_1,
        POS_2,
        NAME,
        HOLOGRAM_POS, BET
    }

    private final ItemStack setPos1 = ItemAPI.createItem("§6set position 1 of the jackpot area", Material.GOLD_INGOT);
    private final ItemStack setPos2 = ItemAPI.createItem("§6set position 2 of the jackpot area", Material.GOLD_INGOT);
    private final ItemStack setHologramPos = ItemAPI.createItem("§6set position of the hologram", Material.SIGN);
    private final ItemStack setName = ItemAPI.createItem("§6set the name", Material.SIGN);
    private final ItemStack setBet = ItemAPI.createItem("§6set bet", Material.GOLD_NUGGET);
    private final ItemStack finishButton = ItemAPI.createItem("§6finish creation or update", Material.STONE_BUTTON);
    private final ItemStack setServerJackpot = ItemAPI.createItem("§6make jackpot server-managed", Material.GOLD_BLOCK);
    private final ItemStack setPlayerJackpot = ItemAPI.createItem("§6make jackpot player-managed", Material.COAL_BLOCK);

    private final ItemStack openElementInventory = ItemAPI.createItem("§6edit elements", Material.BOOK);

    private Location pos1 = null;
    private Location pos2 = null;
    private Location hologramPos = null;
    private String name = null;
    private boolean isServerJackpot = false;
    private double bet = 0.0;

    private Jackpot editingJackpot = null;

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
        getInventory().setItem(2, setBet);
        getInventory().setItem(3, setName);
        getInventory().setItem(4, setHologramPos);
        getInventory().setItem(5, openElementInventory);

        updateInventory();
    }

    /**
     * the constructor for updating this jackpot through the creation menu
     * @param player the player who want to edit the jackpot
     * @param jackpot the jackpot instance
     */
    public JackpotCreationMenu(Player player, Jackpot jackpot)
    {
        this(player);

        pos1 = jackpot.getLocation1();
        pos2 = jackpot.getLocation2();
        hologramPos = jackpot.getLocationHologram();
        name = jackpot.name;
        bet = jackpot.bet;
        isServerJackpot = jackpot.getOwner() == null;
        elementList = jackpot.elements;
        editingJackpot = jackpot;

        //the player is not allowed to change the name
        getInventory().setItem(3, null);
    }

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
        else if(event.getClicked().equals(setHologramPos)) setHologramPos();
        else if(event.getClicked().equals(setBet)) setBet();
        else if(event.getClicked().equals(setName)) setName();
        else if(event.getClicked().equals(finishButton)) finish();
    }

    private void setHologramPos()
    {
        waitingFor = WaitingFor.HOLOGRAM_POS;
        closeInventory();
        waitforChatInput(player);
        player.sendMessage("Go to the position where you want to have your hologram");
    }

    private void setBet()
    {
        waitingFor = WaitingFor.BET;
        closeInventory();
        waitforChatInput(player);
        player.sendMessage("Type in the bet to roll for the jackpot!");
    }

    private void finish()
    {
        if(!allValuesCorrect) return;

        if(editingJackpot == null)
        {
            Jackpot jackpot = new Jackpot(pos1, pos2, isServerJackpot, player);
            jackpot.name = name;
            jackpot.bet = bet;
            jackpot.elements = elementList;
            jackpot.setLocationHologram(hologramPos);

            if(JackpotManager.addJackpot(jackpot))
            {
                closeInventory();
                player.sendMessage("You successfully added a new jackpot named " + jackpot.name);
            }
            else
            {
                player.sendMessage("§4An error occured while trying to add the new jackpot to the system");
            }
        }
        else
        {
            editingJackpot.setLocation1(pos1);
            editingJackpot.setLocation2(pos2);
            editingJackpot.bet = bet;
            editingJackpot.elements = elementList;
            editingJackpot.setLocationHologram(hologramPos);

            if(isServerJackpot) editingJackpot.setServerOwner();
            else
                editingJackpot.setOwner(player);

            if(JackpotManager.updateJackpot(editingJackpot))
            {
                closeInventory();
                player.sendMessage("You successfully added a new jackpot named " + editingJackpot.name);
            }
            else
            {
                player.sendMessage("§4An error occured while trying to add the new jackpot to the system");
            }
        }
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
                    return;
                }
            }
            case BET:
            {
                try
                {

                    this.bet = Double.parseDouble(event.getMessage());
                } catch(Exception e)
                {
                    player.sendMessage("this is not a valid number. Try again or exit with 'exit'");
                    waitforChatInput(player);
                    return;
                }
            }
            case HOLOGRAM_POS:
                hologramPos = player.getLocation();
                break;
        }

        openInventory();
        waitingFor = WaitingFor.NONE;
    }

    private void setFinishButtonLore()
    {
        boolean allValuesValid = false;

        List<String> lore = new ArrayList<>();

        lore.add((pos1 != null) ? "- §aposition 1 set" : "- §4position 1 not set");
        lore.add((pos2 != null) ? "- §aposition 2 set" : "- §4position 2 not set");
        lore.add((hologramPos != null) ? "- §ahologram position set" : "- §4hologram position not set");
        lore.add((bet != 0.0) ? "- §abet set to " + NumberFormatter.format(bet) : "- §4bet not set");
        lore.add((elementList.size() >= 3) ? "- §a" + elementList.size() + " elements set" : "- §4not enough elements set");
        lore.add((name != null) ? "- §aname set to §l" + name : "- §4no name set");
        lore.add((isServerJackpot) ? "- §eis a server-managed jackpot" : "- §eis a player-managed jackpot");

        allValuesValid = pos1 != null && pos2 != null && hologramPos != null && bet != 0.0 && elementList.size() >= 3 && name != null;

        ItemAPI.changeName(finishButton, (allValuesValid) ? "§acreate or update jackpot" : "§4jackpot not correctly set up");
        ItemAPI.setLore(finishButton, lore);

        allValuesCorrect = allValuesValid;
    }

    private void updateInventory()
    {
        if(Main.perm.has(player, "casino.jackpot.server") || Main.perm.has(player, "casino.admin"))
        {
            getInventory().setItem(8, (isServerJackpot) ? setPlayerJackpot : setServerJackpot);
        }

        //update finish button and set it in the inventory
        setFinishButtonLore();
        getInventory().setItem(8, finishButton);
    }

    private Location getTargetLocation()
    {
        Block block = player.getTargetBlock(null, 10);
        if(block != null) return block.getLocation();

        return null;
    }
}
