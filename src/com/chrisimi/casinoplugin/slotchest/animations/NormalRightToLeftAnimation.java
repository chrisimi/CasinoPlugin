package com.chrisimi.casinoplugin.slotchest.animations;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.slotchest.SlotChest;

/**
 * implementation of IRollAnimation to animate the roll from right to left
 */
public class NormalRightToLeftAnimation implements IRollAnimation
{
	private final Inventory inventory;
	private final SlotChest slotChest;
	private final Player player;
	private final int inventorysize = 9*3;
	
	private ItemStack placeholder;
	
	private int rollsToSkip = 0;
	private int rollSkipMaximum = 0;
	private ItemStack[] currentItems = new ItemStack[5];

	public NormalRightToLeftAnimation(Main main, SlotChest slotChest, Player player)
	{
		main;
		this.slotChest = slotChest;
		this.player = player;

		inventory = Bukkit.createInventory(player, inventorysize, "roll animation");
		placeholder = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
	}
	
	@Override
	public Inventory getInventory()
	{
		return inventory;
	}
	
	@Override
	public void initialize()
	{
		if(inventory == null)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "Inventory is null!");
			return;
		}

		//fill the whole inventory with the placeholder
		for(int i = 0; i < 9*3; i++)
			inventory.setItem(i, placeholder);

		//remove placeholder in the middle because the place is needed for the animation
		for(int i = 11; i < 16; i++)
			inventory.setItem(i, null);
	}

	@Override
	public Boolean nextAnimation(int rollsLeft)
	{
		rollsToSkip++;
		
		if(rollsToSkip >= rollSkipMaximum && rollSkipMaximum != 0)
		{
			rollsToSkip = 0;
			return false;
		} else
		{
			//get items
			for(int i = 11; i < 16; i++)
				currentItems[i-11] = (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) ? null : inventory.getItem(i);

			for(int i = 0; i < 4; i++) //move items one further
				currentItems[i] = currentItems[i+1];

			//get new item
			currentItems[4] = slotChest.getRandomItem();

			//set new items
			for(int i = 0; i < 5; i++)
				inventory.setItem(i+11, currentItems[i]);

			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, org.bukkit.SoundCategory.AMBIENT, 4, 3);
			
			if(rollsLeft <= 10)
				rollSkipMaximum = 2;
			else if(rollsLeft <= 15)
				rollSkipMaximum = 3;
			
			return true;
		}
	}

	@Override
	public ItemStack finish()
	{
		for(int i = 11; i < 16; i++)
		{
			if(i == 13) continue;
			inventory.setItem(i, placeholder);
		}
		
		return currentItems[2];
	}

	@Override
	public void simulateEnding(int rollsLeft)
	{
		for(; rollsLeft > 0; rollsLeft--)
		{
			for(int i = 0; i < 4; i++)
				currentItems[i] = currentItems[i+1];

			currentItems[4] = slotChest.getRandomItem();
		}
	}

	@Override
	public int getAnimationID()
	{
		return 1;
	}

	 @Override
	 public int getInventorySize()
	 {
		 return inventorysize;
	 }
}
