package com.chrisimi.casinoplugin.serializables;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.NotificationManager;
import com.chrisimi.casinoplugin.scripts.OfflineEarnManager;
import com.chrisimi.numberformatter.NumberFormatter;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.google.gson.annotations.Expose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Jackpot
{
    @Expose
    public String ownerUUID;

    /**
     * must be unique
     */
    @Expose
    public String name;

    //location of the first point
    @Expose
    public int x1;
    @Expose
    public int y1;
    @Expose
    public int z1;

    //location of the second point
    @Expose
    public int x2;
    @Expose
    public int y2;
    @Expose
    public int z2;

    //location of the jackpot hologram
    @Expose
    public int x3;
    @Expose
    public int y3;
    @Expose
    public int z3;

    @Expose
    public String world;

    @Expose
    public double bet;
    @Expose
    public double jackpotValue;

    @Expose
    public List<JackpotElement> elements;

    public Hologram hologramInstance;
    public boolean isRunning = false;

    public Jackpot(Location lrc1, Location lrc2, boolean isServerOwner, Player owner)
    {
        setLocation1(lrc1);
        setLocation2(lrc2);
        if(isServerOwner) setServerOwner();
        else setOwner(owner);
    }

    public void setLocation1(Location lrc)
    {
        this.x1 = lrc.getBlockX();
        this.y1 = lrc.getBlockY();
        this.z1 = lrc.getBlockZ();
        this.world = lrc.getWorld().getName();
    }

    public void setLocation2(Location lrc)
    {
        this.x2 = lrc.getBlockX();
        this.y2 = lrc.getBlockY();
        this.z2 = lrc.getBlockZ();
        this.world = lrc.getWorld().getName();
    }

    public void setLocationHologram(Location lrc)
    {
        this.x3 = lrc.getBlockX();
        this.y3 = lrc.getBlockY();
        this.z3 = lrc.getBlockZ();
        this.world = lrc.getWorld().getName();
    }

    public void setServerOwner()
    {
        this.ownerUUID = "server";
    }

    public void setOwner(Player player)
    {
        this.ownerUUID = player.getUniqueId().toString();
    }

    public boolean isServerOwner()
    {
        return this.ownerUUID.equalsIgnoreCase("server");
    }

    public OfflinePlayer getOwner()
    {
        return Bukkit.getOfflinePlayer(UUID.fromString(this.ownerUUID));
    }

    public Location getLocation1()
    {
        return new Location(Bukkit.getWorld(world), x1, y1, z1);
    }

    public Location getLocation2()
    {
        return new Location(Bukkit.getWorld(world), x2, y2, z2);
    }

    public Location getLocationHologram()
    {
        return new Location(Bukkit.getWorld(world), x3, y3, z3);
    }

    public JackpotElement getJackpotElement(Material material)
    {
        for(JackpotElement element : elements)
        {
            if(element.material.equals(material))
                return element;
        }

        return null;
    }

    public static class JackpotElement
    {
        @Expose
        public boolean triggerJackpot;
        @Expose
        public double winMultiplicator;
        @Expose
        public double weight;
        @Expose
        public Material material;
    }

    public static class JackpotContainer
    {
        @Expose
        public List<Jackpot> jackpots = new ArrayList<>();
    }

    public void payOwner(double amount, Player player)
    {
        if(!isServerOwner())
        {
            Main.econ.depositPlayer(getOwner(), amount);

            OfflineEarnManager.getInstance().addEarning(getOwner(), amount);
            if(getOwner().isOnline() && !NotificationManager.hasNotificationsDisabled(getOwner()))
                getOwner().getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("jackpot-owner_earn")
                        .replaceAll("%amount%", NumberFormatter.format(amount, false)));
        }
        Main.econ.withdrawPlayer(player, amount);
    }

    public void payPlayer(double amount, Player player)
    {
        if(!isServerOwner())
        {
            Main.econ.withdrawPlayer(getOwner(), amount);

            OfflineEarnManager.getInstance().addLoss(getOwner(), amount);
            if(getOwner().isOnline() && !NotificationManager.hasNotificationsDisabled(getOwner()))
                getOwner().getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("jackpot-owner_lost")
                        .replaceAll("%amount%", NumberFormatter.format(amount, false)));
        }
        Main.econ.depositPlayer(player, amount);
    }
}
