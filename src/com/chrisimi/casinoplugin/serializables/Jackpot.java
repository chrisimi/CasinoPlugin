package com.chrisimi.casinoplugin.serializables;

import com.google.gson.annotations.Expose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Jackpot
{
    @Expose
    public String ownerUUID;

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

    @Expose
    public String world;

    @Expose
    public double bet;
    @Expose
    public double jackpotValue;
    /**
     * the elements to use
     * double: the multiplicator if the win condition with this element is correct
     * material: the material of the element
     */
    @Expose
    public List<JackpotElement> elements;

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
    }

    public void setLocation2(Location lrc)
    {
        this.x2 = lrc.getBlockX();
        this.y2 = lrc.getBlockY();
        this.z2 = lrc.getBlockZ();
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

    public class JackpotElement
    {
        @Expose
        public boolean triggerJackpot;
        @Expose
        public double winMultiplicator;
        @Expose
        public Material material;
    }
}
