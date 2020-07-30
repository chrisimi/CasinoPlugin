package com.chrisimi.casinoplugin.slotchest.animations;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.slotchest.SlotChest;

public class NormalLeftToRightAnimation implements IRollAnimation{

	private final Inventory inventory;
	private final Main main;
	private final Player player;
	private final SlotChest slotChest;
	
	private ItemStack fillmaterial;
	
	private final int inventorySize = 3*9;
	
	private int rollsToSkip = 0;
	private int rollSkipMaximum = 0;
	private ItemStack[] currentItems = new ItemStack[5];
	public NormalLeftToRightAnimation(Main main, SlotChest slotChest, Player player) {
		this.main = main;
		this.slotChest = slotChest;
		this.player = player;
		this.inventory = Bukkit.createInventory(player, inventorySize, "roll animation");
		
		fillmaterial = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
	}
	@Override
	public void initialize() {
		for(int i = 0; i < 9*3; i++)
			inventory.setItem(i, fillmaterial);
		
		for(int i = 11; i < 16; i++)
			inventory.setItem(i,  new ItemStack(Material.AIR));
		
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	@Override
	public Boolean nextAnimation(int rollsLeft) {
		rollsToSkip++;
		
		if(rollsToSkip >= rollSkipMaximum && rollSkipMaximum != 0) {
			rollsToSkip = 0;
			
			return false;
			
		} else {
			//get items
			for(int i = 11; i < 16; i++)
				currentItems[i-11] = inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR ? new ItemStack(Material.AIR) : inventory.getItem(i);
			for(int i = 4; i > 0; i--) //move items one back
				currentItems[i] = currentItems[i-1];
			currentItems[0] = slotChest.getRandomItem();
			for(int i = 0; i < 5; i++)
				inventory.setItem(i+11, currentItems[i]);
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, org.bukkit.SoundCategory.AMBIENT, 4, 3);
			
			if(rollsLeft <= 10) {
				rollSkipMaximum = 2;
			}
			else if(rollsLeft <= 15) {
				rollSkipMaximum = 3;
			
			}
			
			return true;
		}
		
	}

	@Override
	public ItemStack finish() {
		
		for(int i = 11; i < 16; i++) {
			if(i == 13) continue;
			inventory.setItem(i, fillmaterial);
		}
		return currentItems[2];
	}

	@Override
	public void simulateEnding(int rollsLeft) {
			for(; rollsLeft > 0; rollsLeft--) {
			
			for(int i = 4; i > 0; i--)
				currentItems[i] = currentItems[i-1];
			currentItems[0] = slotChest.getRandomItem();
			
		}
		
	}

	@Override
	public int getAnimationID() {
		return 2;
	}

	@Override
	public int getInventorySize() {
		return inventorySize;
	}

}
