package com.chrisimi.casinoplugin.menues;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.LeaderboardsignsManager;
import com.chrisimi.casinoplugin.serializables.Leaderboardsign;
import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LeaderboardCreationMenu extends Inventory implements IInventoryAPI
{
    private enum WaitingFor
    {
        NONE,
        RANGE,
        POSITION,
        VALIDUNTIL
    }

    private final ItemStack setPosition = ItemAPI.createItem("§6Set the position", Material.LADDER);
    private final ItemStack setCycle = ItemAPI.createItem("§6Set the cycle", Material.CLOCK);
    private final ItemStack setMode = ItemAPI.createItem("§6Set the mode", Material.REDSTONE_TORCH);
    private final ItemStack setRange = ItemAPI.createItem("§6Set the range", Material.COMPASS);
    private final ItemStack setServerSign = ItemAPI.createItem("§6Change to a server sign", Material.GOLD_BLOCK);
    private final ItemStack resetSign = ItemAPI.createItem("§6reset sign", Material.REDSTONE);
    private final ItemStack setValidDate = ItemAPI.createItem("§6set time until sign use data", Material.CLOCK);

    private final ItemStack finishButton = ItemAPI.createItem("§6Finish creation", Material.STONE_BUTTON);

    private WaitingFor waitingFor = WaitingFor.NONE;
    private boolean isServerSign = false;
    private boolean rangeAll = false;
    private int rangeValue = Integer.MIN_VALUE;
    private int position = Integer.MIN_VALUE;
    private Leaderboardsign.Cycle cycle = Leaderboardsign.Cycle.NaN;
    private Leaderboardsign.Mode mode = Leaderboardsign.Mode.HIGHESTAMOUNT;
    private boolean shouldSignReset = false;
    private long oldValue = 0L;
    private long validUntil = 0L;

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
    public LeaderboardCreationMenu(Leaderboardsign lb, Player player)
    {
        this(lb.getLocation(), player);

        initializeValues(lb);

        updateInventory();
    }

    private void initializeValues(Leaderboardsign cnf)
    {
        this.isServerSign = cnf.isServerSign();
        this.rangeAll = cnf.modeIsAll();
        if(!rangeAll)
            this.rangeValue = cnf.getRange();
        this.position = cnf.position;
        this.cycle = cnf.cycleMode;
        this.mode = cnf.getMode();
        this.shouldSignReset = cnf.lastManualReset != 0;
        this.validUntil = cnf.validUntil;
        this.oldValue = cnf.lastManualReset;
    }

    private void initialize()
    {
        bukkitInventory.setItem(0, setPosition);
        bukkitInventory.setItem(1, setCycle);
        bukkitInventory.setItem(2, setMode);
        bukkitInventory.setItem(3, setRange);
        if(playerIsAllowedForServersign(player))
            bukkitInventory.setItem(8, setServerSign);
        bukkitInventory.setItem(9, resetSign);
        bukkitInventory.setItem(10, setValidDate);
        bukkitInventory.setItem(17, finishButton);
    }
    private void updateInventory()
    {

        //change the name of setServerSign
        if(playerIsAllowedForServersign(player))
        {
            ItemAPI.changeName(setServerSign, (isServerSign) ? "§6to player sign" : "§6to server sign");
            bukkitInventory.setItem(8, setServerSign);
        }

        ItemAPI.changeName(resetSign, (shouldSignReset) ? "§6remove reset" : "§6reset sign");
        bukkitInventory.setItem(9, resetSign);

        //manage the other buttons
        manageModeButton();
        manageCycleButton();


        //manage the finish button lore etc.
        manageFinishButton();
    }



    @EventMethodAnnotation
    public void onChat(ChatEvent event)
    {
        switch(waitingFor)
        {
            case RANGE:
            {
                if(event.getMessage().equalsIgnoreCase("all"))
                {
                    this.rangeAll = true;
                    this.rangeValue = Integer.MIN_VALUE;
                }
                else
                {
                    try
                    {
                        this.rangeValue = Integer.parseInt(event.getMessage());
                        this.rangeAll = false;
                    } catch(Exception e)
                    {
                        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-integer_invalid"));
                    }
                }
            }
                break;
            case POSITION:
            {
                try
                {
                    this.position = Integer.parseInt(event.getMessage());
                } catch(Exception e)
                {
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-integer_invalid"));
                }
            }
                break;
            case VALIDUNTIL:
            {
                if(event.getMessage().equalsIgnoreCase("remove"))
                    validUntil = 0L;
                else
                {
                    DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm");
                    DateFormat adf = new SimpleDateFormat("MM-dd-yyyy h:mm");
                    Date date = null;
                    try
                    {
                        date = sdf.parse(event.getMessage());
                    } catch (ParseException e)
                    {
                        try
                        {
                            date = adf.parse(event.getMessage());
                        } catch (Exception e2)
                        {
                            player.sendMessage(MessageManager.get("creationmenu-input-date_invalid"));
                        }
                    }
                    if(date != null)
                        validUntil = date.getTime();
                }
            }
                break;
        }

        openInventory();
        waitingFor = WaitingFor.NONE;
        updateInventory();
    }
    @EventMethodAnnotation
    public void onClick(ClickEvent event)
    {
        if(event.getClicked().equals(setPosition)) setPosition();
        else if(event.getClicked().equals(setCycle)) setCycle();
        else if(event.getClicked().equals(setMode)) setMode();
        else if(event.getClicked().equals(setRange)) setRange();
        else if(event.getClicked().equals(setServerSign)) isServerSign = !isServerSign;
        else if(event.getClicked().equals(finishButton) && allValuesValid) finishButton();
        else if(event.getClicked().equals(resetSign)) shouldSignReset = !shouldSignReset;
        else if(event.getClicked().equals(setValidDate)) setValidDate();
        updateInventory();
    }

    private void setValidDate()
    {
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-leaderboard-set_valid_date"));
        closeInventory();
        waitforChatInput(player);
        waitingFor = WaitingFor.VALIDUNTIL;
    }

    private void finishButton()
    {
        Leaderboardsign lb = new Leaderboardsign();
        lb.ownerUUID = (isServerSign) ? "server" : player.getUniqueId().toString();
        if(rangeAll)
            lb.setRange(true);
        else
            lb.setRange(rangeValue);
        lb.position = position;
        lb.cycleMode = cycle;
        lb.setMode(mode);
        if(shouldSignReset && oldValue != 0L)
            lb.lastManualReset = (shouldSignReset) ? 0L : oldValue;
        else
            lb.lastManualReset = (shouldSignReset) ? System.currentTimeMillis() : 0L;

        lb.validUntil = validUntil;
        lb.setLocation(this.lrc);

        LeaderboardsignsManager.addLeaderboardSign(lb);
        closeInventory();
    }

    private void setRange()
    {
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-leaderboard-set_range"));
        closeInventory();
        waitforChatInput(player);
        waitingFor = WaitingFor.RANGE;
    }

    private void setMode()
    {
        mode = (mode.ordinal() == Leaderboardsign.Mode.values().length - 1) ? Leaderboardsign.Mode.values()[0] : Leaderboardsign.Mode.values()[mode.ordinal() + 1];
    }

    private void setCycle()
    {
        cycle = (cycle.ordinal() == Leaderboardsign.Cycle.values().length - 1) ? Leaderboardsign.Cycle.values()[0] : Leaderboardsign.Cycle.values()[cycle.ordinal() + 1];
    }

    private void setPosition()
    {
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-leaderboard-set_position"));
        player.closeInventory();
        waitforChatInput(player);
        waitingFor = WaitingFor.POSITION;
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
        boolean valuesValid = true;
        List<String> lores = new ArrayList<>();

        if(position == Integer.MIN_VALUE)
        {
            lores.add("- §4position not set");
            valuesValid = false;
        }
        else
            lores.add("- §aposition set to " + position);

        if(cycle == Leaderboardsign.Cycle.NaN)
            lores.add("- §6cycle set to none");
        else
            lores.add("- §acycle set to " + cycle.toString());

        lores.add("- §amode set to " + mode.toString());

        if(!rangeAll && rangeValue == Integer.MIN_VALUE)
        {
            lores.add("- §4range not set");
            valuesValid = false;
        }
        else if(rangeAll)
            lores.add("- §arange set to §lall");
        else if(rangeValue != Integer.MIN_VALUE)
            lores.add("- §arange set to " + rangeValue);

        if(isServerSign)
            lores.add("- §asign is a server sign");
        else
            lores.add("- §asign is a player sign");

        ItemAPI.setLore(finishButton, lores);
        ItemAPI.changeName(finishButton, (valuesValid) ? "§acreate or update your leaderboard sign" : "§4you can't create or update your leaderboard sign!");

        this.allValuesValid = valuesValid;
        bukkitInventory.setItem(17, finishButton);
    }
    private boolean playerIsAllowedForServersign(Player player)
    {
        return Main.perm.has(player, "casino.create.serverleaderboard");
    }
}
