package com.chrisimi.casinoplugin.animations.jackpot;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.menues.JackpotElementCreationMenu;
import com.chrisimi.casinoplugin.serializables.Jackpot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * the simple slot animation for jackpot where the blocks will be animated to go from the top to the bottom
 */
public class SimpleJackpotAnimation implements Runnable
{
    private final Jackpot jackpot;
    private final Player player;

    private int bukkitTaskID = 0;
    private int rolls = 100;

    private int xDirection = 0;
    private int zDirection = 0;
    private int yDifference = 0;

    public SimpleJackpotAnimation(Jackpot jackpot, Player player)
    {
        this.jackpot = jackpot;
        this.player = player;
    }
    @Override
    public void run()
    {
        bukkitTaskID = Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), animation, 5L, 5L);
    }

    private Runnable animation = new Runnable()
    {
        @Override
        public void run()
        {
            if(rolls <= 0)
            {
                Main.getInstance().getServer().getScheduler().cancelTask(bukkitTaskID);
                finish();
                return;
            }
            rolls--;

            Map<Location, List<Location>> columns = getArea();

            for(Map.Entry<Location, List<Location>> entry : columns.entrySet())
            {
                animateColumn(entry.getValue());
            }
        }

    };

    private void animateColumn(List<Location> value)
    {
        for(int i = value.size() - 2; i >= 0; i--)
        {
            Location currLrc = value.get(i);
            Location nextLrc = value.get(i + 1);

            //set random block to the top most block
            if(i == 0)
                currLrc.getBlock().setType(getRandomBlock());

            //overwrite the block under with the current block
            nextLrc.getBlock().setType(currLrc.getBlock().getType());
        }
    }
    //TODO overwork randomness?? looks sometimes weird?


    private Material getRandomBlock()
    {
        Random rnd = new Random();
        List<Jackpot.JackpotElement> elements = new ArrayList<>(jackpot.elements);
        double totalWeight = JackpotElementCreationMenu.totalWeight(elements);

        //sort after weight
        elements.sort(new Comparator<Jackpot.JackpotElement>()
        {
            @Override
            public int compare(Jackpot.JackpotElement o1, Jackpot.JackpotElement o2)
            {
                if(o1.weight == o2.weight) return 0;
                return (o1.weight > o2.weight) ? 1 : -1;
            }
        });

        //select the material where the material weight and the current weight are higher than the randomvalue
        double randomValue = rnd.nextDouble() * totalWeight;
        double currentValue = 0.0;

        for(Jackpot.JackpotElement element : elements)
        {
            if(currentValue < randomValue && currentValue + element.weight >= randomValue)
            {
                //this element
                return element.material;
            }
            else
                currentValue += element.weight;
        }

        return elements.get(elements.size() - 1).material;
    }

    private void finish()
    {
        //TODO add money payout/jackpot payout
        int zHeight = (int) ((yDifference % 2 == 1) ? Math.floor((yDifference / 2) + 1) : yDifference / 2);

        if(hasPlayerWon(zHeight).getKey())
        {
            player.sendMessage("you have won");
        }
        else
        {
            player.sendMessage("you lost");
        }

        jackpot.isRunning = false;
    }

    private Map.Entry<Boolean, Material> hasPlayerWon(int zheight)
    {
        Map<Location, List<Location>> a = getArea();
        Material winMaterial = null;

        for(Map.Entry<Location, List<Location>> entry : a.entrySet())
        {
            if(winMaterial == null)
                winMaterial = entry.getValue().get(zheight - 1).getBlock().getType();

            if(winMaterial != entry.getValue().get(zheight - 1).getBlock().getType())
                return new AbstractMap.SimpleEntry<Boolean, Material>(false, null);
        }

        return new AbstractMap.SimpleEntry<Boolean, Material>(true, winMaterial);
    }

    //get the location of the field with the top most location and the list of locations from the same column
    private java.util.Map<Location, List<Location>> getArea()
    {
        Map<Location, List<Location>> result = new HashMap<>();

        xDirection = 0;
        zDirection = 0;
        yDifference = jackpot.getLocation1().getBlockY() - jackpot.getLocation2().getBlockY();

        if(jackpot.getLocation1().getBlockX() != jackpot.getLocation2().getBlockX())
        {
            xDirection = (jackpot.getLocation1().getBlockX() > jackpot.getLocation2().getBlockX()) ? -1 : 1;

            for(int x = jackpot.getLocation1().getBlockX(); x <= jackpot.getLocation2().getBlockX(); x += xDirection)
            {
                List<Location> blocks = new ArrayList<>();
                for(int i = 0; i <= yDifference; i++)
                {
                    blocks.add(new Location(jackpot.getLocation1().getWorld(), x, jackpot.getLocation1().getBlockY() - i, jackpot.getLocation1().getBlockZ()));
                }

                result.put(blocks.get(0), blocks);
            }
        }
        else
        {
            zDirection = (jackpot.getLocation1().getBlockZ() > jackpot.getLocation2().getBlockZ()) ? -1 : 1;

            for(int z = jackpot.getLocation1().getBlockZ(); z <= jackpot.getLocation2().getBlockZ(); z += zDirection)
            {
                List<Location> blocks = new ArrayList<>();
                for(int i = 0; i <= yDifference; i++)
                {
                    blocks.add(new Location(jackpot.getLocation1().getWorld(), jackpot.getLocation1().getBlockX(), jackpot.getLocation1().getBlockY() - i, z));
                }

                result.put(blocks.get(0), blocks);
            }
        }

        return result;
    }
}
