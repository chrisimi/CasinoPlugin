package slotChests.Animations;

import slotChests.SlotChest;


import com.chrisimi.casino.main.Main;
import scripts.CasinoManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class NormalRightToLeftAnimation implements IRollAnimation {

	private final Inventory inventory;
	private final Main main;
	private final SlotChest slotChest;
	private final Player player;
	private final int inventorysize = 9*3;
	
	private ItemStack fillmaterial;
	
	private int rollsToSkip = 0;
	private int rollSkipMaximum = 0;
	private ItemStack[] currentItems = new ItemStack[5];
	public NormalRightToLeftAnimation(Main main, SlotChest slotChest, Player player) {
		this.main = main;
		this.slotChest = slotChest;
		this.player = player;
		
		
		inventory = Bukkit.createInventory(player, inventorysize, "roll animation");
		fillmaterial = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
	}
	
	@Override
	public Inventory getInventory() {
		// TODO Auto-generated method stub
		return inventory;
	}
	
	@Override
	public void initialize() {
		
		
		if(inventory == null) {
			CasinoManager.LogWithColor(ChatColor.RED + "Inventory is null!");
			return;
		}
		for(int i = 0; i < 9*3; i++)
			inventory.setItem(i, fillmaterial);
		
		for(int i = 11; i < 16; i++)
			inventory.setItem(i,  new ItemStack(Material.AIR));
		
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
			for(int i = 0; i < 4; i++) //move items one further
				currentItems[i] = currentItems[i+1];
			currentItems[4] = slotChest.getRandomItem();
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
			
			for(int i = 0; i < 4; i++)
				currentItems[i] = currentItems[i+1];
			currentItems[4] = slotChest.getRandomItem();
			
		}
	
		
	}

	@Override
	public int getAnimationID() {
		// TODO Auto-generated method stub
		return 1;
	}

	 @Override
	 public int getInventorySize() {
		 return inventorysize;
	 }
	
}
