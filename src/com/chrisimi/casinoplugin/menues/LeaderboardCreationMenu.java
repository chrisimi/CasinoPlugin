package com.chrisimi.casinoplugin.menues;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.serializables.Leaderboardsign;
import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardCreationMenu extends Inventory implements IInventoryAPI
{
    private enum WaitingFor
    {
        NONE,
        RANGE,
        POSITION,
        LASTMANUALRESET,
        VALIDUNTIL
    }

    private final ItemStack setPosition = ItemAPI.createItem("§6Set the position", Material.LADDER);
    private final ItemStack setCycle = ItemAPI.createItem("§6Set the cycle", Material.CLOCK);
    private final ItemStack setMode = ItemAPI.createItem("§6Set the mode", Material.REDSTONE_TORCH);
    private final ItemStack setRange = ItemAPI.createItem("§6Set the range", Material.COMPASS);
    private final ItemStack setServerSign = ItemAPI.createItem("§6Change to a server sign", Material.GOLD_BLOCK);
    private final ItemStack disableSign = ItemAPI.createItem("§4Disable sign", Material.RED_WOOL);
    private final ItemStack enableSign = ItemAPI.createItem("§aEnable sign", Material.GREEN_WOOL);

    private final ItemStack finishButton = ItemAPI.createItem("§6Finish creation", Material.STONE_BUTTON);

    private boolean isServerSign = false;
    private boolean isDisabled = false;
    private boolean rangeAll = false;
    private int rangeValue = Integer.MIN_VALUE;
    private int position = Integer.MIN_VALUE;
    private Leaderboardsign.Cycle cycle = Leaderboardsign.Cycle.NaN;
    private Leaderboardsign.Mode mode = Leaderboardsign.Mode.HIGHESTAMOUNT;
    private long lastManualReset = Long.MIN_VALUE;
    private long validUntil = Long.MIN_VALUE;

    private boolean allValuesValid = false;

    private final Player player;
    private final Location lrc;

    public LeaderboardCreationMenu(Location lrc, Player player)
    {
        super(player, 9*2, Main.getInstance(), "leaderboard creation menu");
        this.player = player;
        this.lrc = lrc;

        this.addEvents(this);

        initialize();
        updateInventory();

        openInventory();
    }

    private void initialize()
    {
        bukkitInventory.setItem(0, setPosition);
        bukkitInventory.setItem(1, setCycle);
        bukkitInventory.setItem(2, setMode);
        bukkitInventory.setItem(3, setRange);
        bukkitInventory.setItem(8, setServerSign);
        bukkitInventory.setItem(15, disableSign);
        bukkitInventory.setItem(17, finishButton);
    }
    private void updateInventory()
    {
        bukkitInventory.setItem(15, (isDisabled) ? enableSign : disableSign);

        //change the name of setServerSign
        ItemAPI.changeName(setServerSign, (isServerSign) ? "§6to player sign" : "§6to server sign");
        bukkitInventory.setItem(8, setServerSign);

        //manage the other buttons
        manageModeButton();
        manageCycleButton();


        //manage the finish button lore etc.
        manageFinishButton();
    }


    @EventMethodAnnotation
    public void onClick(ClickEvent event)
    {

    }

    @EventMethodAnnotation
    public void onChat(ChatEvent event)
    {

    }

    private void manageModeButton()
    {
        List<String> lore = new ArrayList<>();

        //go through all modes and highlight the current one
        for(Leaderboardsign.Mode mode : Leaderboardsign.Mode.values())
        {
            if(mode == this.mode)
                lore.add("§6§l" + mode.toString());
            else
                lore.add("§6" + mode.toString());
        }

        ItemAPI.setLore(setMode, lore);
        bukkitInventory.setItem(2, setMode);
    }
    private void manageCycleButton()
    {
        List<String> lore = new ArrayList<>();

        //go through all cycles and highlight the current one
        for(Leaderboardsign.Cycle cycle : Leaderboardsign.Cycle.values())
        {
            if(cycle == this.cycle)
                lore.add("§6§l" + cycle.toString());
            else
                lore.add("§6" + cycle.toString());
        }

        ItemAPI.setLore(setCycle, lore);
        bukkitInventory.setItem(1, setCycle);
    }
    private void manageFinishButton()
    {
        boolean valuesValid = false;
    }
}
