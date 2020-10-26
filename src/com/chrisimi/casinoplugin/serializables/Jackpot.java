package com.chrisimi.casinoplugin.serializables;

import com.google.gson.annotations.Expose;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

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
