package slotChests;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chrisimi.casino.main.Main;
import scripts.CasinoManager;
import slotChests.Animations.RollAnimationFactory;

public class SettingsMenu implements Listener{

	private final Main main;
	private final SlotChest slotChest;
	private final Player owner;
	private final Inventory inventory;
	
	private ItemStack changeRollAnimationBlock;
	
	public SettingsMenu(Main main, SlotChest slotChest, Player owner) {
		this.main = main;
		this.slotChest = slotChest;
		this.owner = owner;
		
		main.getServer().getPluginManager().registerEvents(this, main);
		inventory = Bukkit.createInventory(owner, 9*1, "Settings");
		owner.openInventory(inventory);
		
		initialize();
	}
	
	
	private void initialize() {
		
		updateInventory();
		
		
	}
	private void updateInventory() {
		changeRollAnimationBlock = new ItemStack(Material.STONE_BUTTON);
		ItemMeta meta = changeRollAnimationBlock.getItemMeta();
		meta.setDisplayName("Change the roll animation of this chest");
		meta.setLore(getLoreForAnimations());
		changeRollAnimationBlock.setItemMeta(meta);
		inventory.setItem(0, changeRollAnimationBlock);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getCurrentItem() == null) return;
		if(!(event.getInventory().equals(inventory))) return;
		
		if(event.getCurrentItem().equals(changeRollAnimationBlock)) {
			
			slotChest.animationID++;
			if(slotChest.animationID> RollAnimationFactory.getNameOfAllAnimations().length)
				slotChest.animationID = 1;
			updateInventory();
		}
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onInventoryLeave(InventoryCloseEvent event) {
		if(!(event.getInventory().equals(inventory))) return;
		
		CasinoManager.slotChestManager.save();
	}
	
	
	
	
	
	
	
	private ArrayList<String> getLoreForAnimations() {
		String[] lores = RollAnimationFactory.getNameOfAllAnimations();
		for(int i = 0; i < lores.length; i++) {
			if(i == slotChest.animationID-1) {
				lores[i] = "§6" + lores[i];
			} else {
				lores[i] = "§8" + lores[i]; 
			}
		}
		ArrayList<String> loresList = new ArrayList<String>(lores.length);
		for(String a : lores)
			loresList.add(a);
		return loresList;
	}
}
