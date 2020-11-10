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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Inventory to create a dice sign
 */
public class DiceCreationMenu extends Inventory implements IInventoryAPI
{
    private enum InputType
    {
        NONE,
        BET,
        RANGE,
        WINMULTIPLICAND
    }

    private boolean isServerSign = false;
    private double bet = 0.0;
    private int rangeMin = -1;
    private int rangeMax = -1;
    private double winMultiplicand = 2.0;
    private boolean isDisabled = false;

    private InputType currentInputType = InputType.NONE;

    private boolean allValuesValid = false;

    private final ItemStack setBet = ItemAPI.createItem("§6Set the bet", Material.GOLD_INGOT);
    private final ItemStack setWinRange = ItemAPI.createItem("§6Set the win range", Material.COMPASS);
    private final ItemStack setWinMultiplicand = ItemAPI.createItem("§6Set the win multiplicand", Material.JUKEBOX);
    private final ItemStack serverSign = ItemAPI.createItem("§6change it to a server sign", Material.GOLD_BLOCK);
    private final ItemStack disableSign = ItemAPI.createItem("§4Disable this sign", Material.RED_WOOL);
    private final ItemStack enableSign = ItemAPI.createItem("§aEnable this sign", Material.GREEN_WOOL);
    private final ItemStack finishButton = ItemAPI.createItem("§a finish", Material.STONE_BUTTON);

    private final Location lrc;

    /**
     * Create a new inventory to create a dice sign
     * @param lrc Location of the sign
     * @param player player, owner of the sign
     */
    public DiceCreationMenu(Location lrc, Player player)
    {
        super(player, 9, Main.getInstance(), "Dice creation menu");
        this.lrc = lrc;

        initialize();
        addEvents(this);

        openInventory();
        updateInventory();
    }

    public DiceCreationMenu(PlayerSignsConfiguration conf, Player player)
    {
        this(conf.getLocation(), player);

        //set the values in local variables
        initializeValues(conf);
    }

    private void initializeValues(PlayerSignsConfiguration conf)
    {
        isServerSign = conf.isServerOwner();
        bet = conf.bet;
        rangeMin = conf.getWinChancesDice()[0];
        rangeMax = conf.getWinChancesDice()[1];
        winMultiplicand = conf.winMultiplicatorDice();
        isDisabled = conf.disabled;

        updateInventory();
    }

    private void initialize()
    {
        bukkitInventory.setItem(0, setBet);
        bukkitInventory.setItem(1, setWinRange);
        bukkitInventory.setItem(2, setWinMultiplicand);
        if(playerIsAllowedForServerSigns())
            bukkitInventory.setItem(5, serverSign);
        bukkitInventory.setItem(7, disableSign);

        bukkitInventory.setItem(8, finishButton);
    }

    private void updateInventory()
    {
        bukkitInventory.setItem(7, (isDisabled) ? enableSign : disableSign);

        if(playerIsAllowedForServerSigns())
        {
            ItemAPI.changeName(serverSign, (isServerSign) ? "§6to player sign" : "§6to server sign");
            bukkitInventory.setItem(5, serverSign);
        }

        updateLoreButton();
    }

    @EventMethodAnnotation
    public void onClick(ClickEvent event)
    {
        if(event.getClicked().equals(disableSign)) isDisabled = true;
        else if(event.getClicked().equals(enableSign)) isDisabled = false;
        else if(event.getClicked().equals(finishButton) && allValuesValid) finishButton();
        else if(event.getClicked().equals(setBet)) setBet();
        else if(event.getClicked().equals(setWinRange)) setWinRange();
        else if(event.getClicked().equals(setWinMultiplicand)) setWinMultiplicand();
        else if(event.getClicked().equals(serverSign)) isServerSign = !isServerSign;
        updateInventory();
    }
    @EventMethodAnnotation
    public void onChat(ChatEvent event)
    {
        switch(currentInputType)
        {
            case BET:
            {
                try
                {
                    this.bet = Double.parseDouble(event.getMessage());
                } catch(Exception e)
                {
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-double_invalid"));
                }
            }
            break;
            case RANGE:
            {
                if(!(event.getMessage().contains("-")))
                {
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-string_invalid"));
                }
                else
                    try
                    {
                        String[] splited = event.getMessage().split("-");
                        int chatMinRange = Integer.parseInt(splited[0]);
                        int chatMaxRange = Integer.parseInt(splited[1]);

                        this.rangeMin = chatMinRange;
                        this.rangeMax = chatMaxRange;
                    } catch(Exception e)
                    {
                        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-integer_invalid"));
                    }
            }
            break;
            case WINMULTIPLICAND:
            {
                try
                {
                    this.winMultiplicand = Double.parseDouble(event.getMessage());
                } catch(Exception e)
                {
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-doubel_invalid"));
                }
            }
            break;
        }
        openInventory();
        currentInputType = InputType.NONE;
        updateInventory();
    }
    private void setWinRange()
    {
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-dice-set_win_range"));
        closeInventory();
        waitforChatInput(player);
        currentInputType = InputType.RANGE;
    }

    private void setWinMultiplicand()
    {
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-dice-set_win_multiplicand"));
        closeInventory();
        waitforChatInput(player);
        currentInputType = InputType.WINMULTIPLICAND;
    }

    private void setBet()
    {
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-dice-set_bet"));
        closeInventory();
        waitforChatInput(player);
        currentInputType = InputType.BET;
    }

    private void finishButton()
    {
        PlayerSignsConfiguration conf = new PlayerSignsConfiguration
                (this.lrc, PlayerSignsConfiguration.GameMode.DICE, player, this.bet, String.format("%s-%s;%s", rangeMin, rangeMax, winMultiplicand));

        //make player sign conf server sign too
        if(this.isServerSign)
            conf.ownerUUID = "server";

        //disable the sign if it have to be
        conf.disabled = isDisabled;

        PlayerSignsManager.addPlayerSign(conf);

        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-creation-dice_successful"));
        closeInventory();
    }
    private void updateLoreButton()
    {
        List<String> lore = new ArrayList<>();
        allValuesValid = true;

        if(bet != 0.0)
        {
            if(bet > PlayerSignsManager.getMaxBetDice())
            {
                lore.add("-§4 the bet limit is " + Main.econ.format(PlayerSignsManager.getMaxBetDice()));
                allValuesValid = false;
            }
            else
                lore.add("-§a bet is " + Main.econ.format(bet));
        }
        else
        {
            lore.add("-§4 bet is not set");
            allValuesValid = false;
        }

        if(rangeMin != -1 && rangeMax != -1 && rangeMin < rangeMax && rangeMax < 100 && rangeMin > 0)
            lore.add("-§a range is set to " + String.format("§e%s§6-§e%s", rangeMin, rangeMax));
        else
        {
            allValuesValid = false;
            lore.add("-§4 the range is invalid!");
        }

        if(winMultiplicand != 0.0)
            lore.add("-§a win multiplicand is set to " + winMultiplicand);
        else
        {
            lore.add("-§4 win multiplicand is not set");
            allValuesValid = false;
        }
        if(!PlayerSignsManager.playerCanCreateSign(player, PlayerSignsConfiguration.GameMode.DICE))
        {
            lore.add("-§4 you can't create a new dice sign because you are at the limit");
            allValuesValid = false;
        }

        if(!PlayerSignsManager.betIsAllowed(bet, PlayerSignsConfiguration.GameMode.BLACKJACK))
        {
            lore.add("- §4bet is too high for this server");
            allValuesValid = false;
        }


        ItemMeta meta = finishButton.getItemMeta();

        if(allValuesValid)
            meta.setDisplayName("§afinish the creation or update of the sign");
        else
            meta.setDisplayName("§4you can't finish the creation or update of the sign");

        meta.setLore(lore);
        finishButton.setItemMeta(meta);
        bukkitInventory.setItem(8, finishButton);
    }
    private boolean playerIsAllowedForServerSigns()
    {
        return Main.perm.has(player, "casino.create.serversign");
    }
}
