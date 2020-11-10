package com.chrisimi.casinoplugin.menues;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.PlayerSignsManager;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BlackjackCreationMenu extends Inventory implements IInventoryAPI
{
    private enum WaitingFor
    {
        MINBET,
        MAXBET,
        WINMULTIPLICAND,
        NONE
    }

    private final ItemStack setMinBet = ItemAPI.createItem("§6set min bet", Material.GOLD_NUGGET);
    private final ItemStack setMaxBet = ItemAPI.createItem("§6set max bet", Material.GOLD_INGOT);
    private final ItemStack setWinMultiplicand = ItemAPI.createItem("§6set win multiplicand", Material.JUKEBOX);
    private final ItemStack setServerSign = ItemAPI.createItem("§6set server sign", Material.GOLD_BLOCK);
    private final ItemStack disableSign = ItemAPI.createItem("§4disable sign", Material.RED_WOOL);
    private final ItemStack enableSign = ItemAPI.createItem("§aenable sign", Material.GREEN_WOOL);

    private final ItemStack finishButton = ItemAPI.createItem("§6finish creation or update", Material.STONE_BUTTON);

    private boolean allValuesValid = false;
    private boolean isSignDisabled = false;
    private boolean isServerSign = false;
    private double minBet = Double.MIN_VALUE;
    private double maxBet = Double.MIN_VALUE;
    private double winMultiplicand1 = Double.MIN_VALUE;
    private int[] winMultiplicand2 = new int[0];
    private WaitingFor waitingFor = WaitingFor.NONE;

    private final Location lrc;

    public BlackjackCreationMenu(Location lrc, Player player)
    {
        super(player, 9, Main.getInstance(), "Blackjack creation menu");

        this.lrc = lrc;

        this.addEvents(this);

        initialize();

        updateInventory();
        openInventory();
    }

    public BlackjackCreationMenu(PlayerSignsConfiguration conf, Player player)
    {
        this(conf.getLocation(), player);

        initializeValues(conf);

        updateInventory();
    }
    private void initializeValues(PlayerSignsConfiguration conf)
    {
        this.isSignDisabled = conf.disabled;
        this.isServerSign = conf.isServerOwner();
        this.minBet = conf.bet;
        this.maxBet = conf.blackjackGetMaxBet();
        if(conf.blackjackIsToWriting())
            this.winMultiplicand2 = conf.blackjackGetToWriting();
        else
            this.winMultiplicand1 = conf.blackjackGetMaxBet();
    }
    
    private void initialize()
    {
        bukkitInventory.setItem(0, setMinBet);
        bukkitInventory.setItem(1, setMaxBet);
        bukkitInventory.setItem(2, setWinMultiplicand);

    }

    private void updateInventory()
    {
        //manage server sign
        if(Main.perm.has(player, "casino.create.serversign"))
        {
            ItemAPI.changeName(setServerSign, (isServerSign) ? "§6change to player sign" : "§6change to server sign");
            bukkitInventory.setItem(4, setServerSign);
        }

        //manage disable/enable button
        bukkitInventory.setItem(7, (isSignDisabled) ? enableSign : disableSign);

        //manage finish button
        manageFinishButton();
    }


    //region Click methods
    @EventMethodAnnotation
    public void onClick(ClickEvent event)
    {
        if(event.getClicked().equals(setMinBet)) setMinBet();
        else if(event.getClicked().equals(setMaxBet)) setMaxBet();
        else if(event.getClicked().equals(setWinMultiplicand)) setWinMultiplicand();
        else if(event.getClicked().equals(setServerSign)) isServerSign = !isServerSign;
        else if(event.getClicked().equals(enableSign)) isSignDisabled = false;
        else if(event.getClicked().equals(disableSign)) isSignDisabled = true;
        else if(event.getClicked().equals(finishButton) && allValuesValid) finishButton();

        updateInventory();
    }

    private void setMinBet()
    {
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-blackjack-set_min_bet"));
        closeInventory();
        waitforChatInput(player);
        waitingFor = WaitingFor.MINBET;
    }

    private void setMaxBet()
    {
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-blackjack-set_max_bet"));
        closeInventory();
        waitforChatInput(player);
        waitingFor = WaitingFor.MAXBET;
    }

    private void setWinMultiplicand()
    {
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-blackjack-set_win_multiplicand"));
        closeInventory();
        waitforChatInput(player);
        waitingFor = WaitingFor.WINMULTIPLICAND;
    }

    private void finishButton()
    {
        String plusInformation = (winMultiplicand1 == Double.MIN_VALUE) ? String.format("%s to %s", winMultiplicand2[0], winMultiplicand2[1]) : String.valueOf(winMultiplicand1);

        PlayerSignsConfiguration cnf = new PlayerSignsConfiguration
                (this.lrc, PlayerSignsConfiguration.GameMode.BLACKJACK, player, minBet, String.format("%s;%s", maxBet, plusInformation));

        if(isServerSign)
            cnf.ownerUUID = "server";
        cnf.disabled = isSignDisabled;

        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-creation-blackjack_successful"));
        closeInventory();

        PlayerSignsManager.addPlayerSign(cnf);
    }
    //endregion

    @EventMethodAnnotation
    public void onChat(ChatEvent event)
    {
        switch(waitingFor)
        {

            case MINBET:
            {
                try
                {
                    this.minBet = Double.parseDouble(event.getMessage());
                } catch(Exception e)
                {
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-double_invalid"));
                }
            }
                break;
            case MAXBET:
            {
                if(event.getMessage().equalsIgnoreCase("-1"))
                    this.maxBet = -1.0;
                else
                {
                    try
                    {
                        this.maxBet = Double.parseDouble(event.getMessage());
                    } catch(Exception e)
                    {
                        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-double_invalid"));
                    }
                }
            }
                break;
            case WINMULTIPLICAND:
            {
                if(event.getMessage().contains("to"))
                {
                    try
                    {
                        String[] values = event.getMessage().split("to");
                        int a = Integer.parseInt(values[0].trim());
                        int b = Integer.parseInt(values[1].trim());
                        this.winMultiplicand2 = new int[] {a, b};
                        this.winMultiplicand1 = Double.MIN_VALUE;
                    } catch(Exception e)
                    {
                        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-integer_invalid"));
                    }
                }
                else
                {
                    try
                    {
                        this.winMultiplicand1 = Double.parseDouble(event.getMessage());
                        this.winMultiplicand2 = new int[0];
                    } catch(Exception e)
                    {
                        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-double_invalid"));
                    }
                }
            }
                break;
            default:
                break;
        }
        waitingFor = WaitingFor.NONE;
        openInventory();
        updateInventory();
    }

    private void manageFinishButton()
    {
        boolean valuesValid = true;
        List<String> lore = new ArrayList<>();

        if(minBet == Double.MIN_VALUE)
        {
            lore.add("-§4 min bet not set");
            valuesValid = false;
        } else
            lore.add("-§a min bet set to §6" + Main.econ.format(minBet));

        if(maxBet == Double.MIN_VALUE || maxBet < minBet)
        {
            lore.add("-§4 max bet not set");
            valuesValid = false;
        } else
            lore.add("-§a max bet set to §6" + Main.econ.format(maxBet));

        if(winMultiplicand1 == Double.MIN_VALUE && winMultiplicand2.length == 0)
        {
            lore.add("-§4 win multiplicand not set");
            valuesValid = false;
        } else
            lore.add("-§a win multiplicand set to " + ((winMultiplicand1 == Double.MIN_VALUE) ? String.format("%s to %s", winMultiplicand2[0], winMultiplicand2[1]) : winMultiplicand1));

        lore.add((isServerSign) ? "-§6 sign is a server sign" : "-§6 sign is a player sign");

        lore.add((isSignDisabled) ? "-§6 sign is disabled" : "-§a sign is enabled");

        if(!PlayerSignsManager.isBetAllowed(maxBet, PlayerSignsConfiguration.GameMode.BLACKJACK))
        {
            lore.add("- §4bet is too high for this server");
            valuesValid = false;
        }

        if(valuesValid)
        {
            ItemAPI.changeName(finishButton, "§afinish creation or update");
            allValuesValid = true;
        } else
            ItemAPI.changeName(finishButton, "§4you can't finish creation or update");

        ItemAPI.setLore(finishButton, lore);

        bukkitInventory.setItem(8, finishButton);
    }
}
