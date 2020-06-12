package com.chrisimi.casinoplugin.slotchest;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;

/**
 * represent the winnings menu for the slotchest
 * not singelton
 * @author chris
 *
 */
public class WinningsMenu implements Listener {

	public static HashMap<Player, WinningsMenu> waitingForEingabe = new HashMap<>();
	
	public static HashMap<WinningsMenu, Integer> inventoryReadingTasks = new HashMap<>(); //TODO: on inventory close stop task
	
	
	private Main main;
	private Player owner;
	private SlotChest slotChest;
	
	private ItemStack fillMaterial;
	public Inventory inventory;
	
	
	//input
	public Boolean waitingForAmount = false;
	public Boolean waitingForWeight = false;
	public Boolean eingabeFinished = false;
	
	private int newItemAmount = 0;
	private double newItemWeight = 0;
	private Material newItemMaterial = null;
	
	public WinningsMenu instance = this;
	public WinningsMenu(Main main, Player owner, SlotChest slotChest) {
		this.main = main;
		this.owner = owner;
		this.slotChest = slotChest;
		main.getServer().getPluginManager().registerEvents(this, main);
		initialize();
	}
	
	private void initialize() {
		inventory = Bukkit.createInventory(owner, 9*5, "WinningsMenu");
		owner.openInventory(inventory);
		fillMaterial = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
		for(int i = 0; i < 9*5; i++) {
			if(i == 40) continue;
			inventory.setItem(i, fillMaterial);
		}
		
		
		//inventoryReadTask
		initializeInventoryReadingTask();
		updateInventory();
	}
	/**
	 * start the background task which tracks the input from the player
	 */
	private void initializeInventoryReadingTask() {
		int taskNumber = main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
			
			//@SuppressWarnings("unlikely-arg-type")
			@Override
			public void run() {			
				ItemStack check = inventory.getItem(40);
				if(check != null) {
					Material itemStack = check.getType(); 
					if(itemStack != Material.AIR && itemStack != fillMaterial.getType()) {
						
						if(slotChest.lager.size() >= 9*5) {
							owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings_reached_limit"));
							inventory.setItem(40, new ItemStack(Material.AIR));
							int slot = owner.getInventory().first(Material.AIR);
							if(slot == -1)
								owner.getWorld().dropItem(owner.getLocation(), check);
							else
								owner.getInventory().setItem(slot, check);
						}
						
						if(slotChest.itemstoWinContains(itemStack)) {
							owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings_exists"));
							inventory.setItem(40, new ItemStack(Material.AIR));
							int slot = owner.getInventory().first(Material.AIR);
							if(slot == -1) {
								owner.getWorld().dropItem(owner.getLocation(), check);
							} else {
								owner.getInventory().setItem(slot, check);
							}
							
						} else if(slotChest.itemIsOnForbiddenList(itemStack)) {
							owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings_forbidden_item"));
							inventory.setItem(40, new ItemStack(Material.AIR));
							int slot = owner.getInventory().first(Material.AIR);
							if(slot == -1)
								owner.getWorld().dropItem(owner.getLocation(), check);
							else
								owner.getInventory().setItem(slot, check);
							
							
						} else {
							//ein Spieler hat ein Item reingelegt
							main.getServer().getScheduler().cancelTask(inventoryReadingTasks.get(instance));
							inventoryReadingTasks.remove(instance);
							
							waitingForEingabe.put(owner, instance);
							waitingForAmount = true;
							waitingForWeight = true;
							
//							owner.sendMessage(String.format(CasinoManager.getPrefix() + "Adding Informations to your winning: \nType in the amount of %s the player should win", itemStack.toString()));
							owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings_amount_message").replace("%item%", itemStack.toString()));
							newItemMaterial = itemStack;
							owner.closeInventory();
						}
						
						
					}
				}
				
			}

			
		}, 10, 10);
		inventoryReadingTasks.put(this, taskNumber);
	}

	
	//
	//EventHandler
	@EventHandler
	public void onPlayerWrite(AsyncPlayerChatEvent event) {
		if(!(waitingForEingabe.containsKey(event.getPlayer()))) return;
		
		if(event.isAsynchronous()) {
			main.getServer().getScheduler().runTask(main, new Runnable() {
				
				@Override
				public void run() {
					playerEingabe(event.getMessage());
					
				}
			});
		} else 
			playerEingabe(event.getMessage());
		
		event.setCancelled(true);
	}
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getCurrentItem() == null) return;
		if(!(event.getInventory().equals(inventory))) return;
		
		if(event.getCurrentItem().equals(new ItemStack(Material.PINK_STAINED_GLASS_PANE))) {
			event.setCancelled(true);
			return;
		}
		
		HashMap<ItemStack, Double> newHashMap = new HashMap<>(); 
		
		for(Entry<ItemStack, Double> entry : slotChest.itemsToWin.entrySet()) {
			if(entry.getKey().equals(event.getCurrentItem())) {
				removeItemToWin(entry.getKey());
				inventory.setItem(event.getSlot(), new ItemStack(Material.AIR));
				event.setCancelled(true);
			} else
				newHashMap.put(entry.getKey(), entry.getValue());
			
		}
		
		/*
		for(Entry<ItemStack, Double> entry : slotChest.itemsToWin.entrySet()) {
			System.out.println(entry.getKey().toString() + " " + entry.getKey().hashCode());
			if(entry.getKey().getType().equals(event.getCurrentItem().getType()) && entry.getKey().getAmount() == event.getCurrentItem().getAmount()) {
				
				
				
				System.out.println("toremove: " + entry.getKey().toString() + " " + entry.getKey().hashCode());
				System.out.println(slotChest.itemsToWin.remove(entry.getKey()));
				break;
			}
		}
		*/
		slotChest.itemsToWin = newHashMap;
		updateInventory();
		
	}
	
	/**
	 * remove a item from the list of items to win (winnable items)
	 * @param item item to remove 
	 */
	private void removeItemToWin(ItemStack item) {
		int itemsInWarehouse = 0;
		ArrayList<ItemStack> itemLagerToRemove = new ArrayList<>(); 
		for(ItemStack itemStack : slotChest.lager) {
			if(itemStack.getType().equals(item.getType())) {
				itemsInWarehouse += itemStack.getAmount();
				itemLagerToRemove.add(itemStack);
				
			}
		}
		slotChest.lager.removeAll(itemLagerToRemove);
		while(itemsInWarehouse > 0) {
			if(itemsInWarehouse >= 64) {
				int slot = owner.getInventory().first(Material.AIR);
				if(slot == -1)
					owner.getWorld().dropItem(owner.getLocation().add(0, 2, 0), new ItemStack(item.getType(), 64));
				else
					owner.getInventory().setItem(slot, new ItemStack(item.getType(), 64));
				itemsInWarehouse -= 64;
			} else {
				int slot = owner.getInventory().first(Material.AIR);
				if(slot == -1)
					owner.getWorld().dropItem(owner.getLocation().add(0,2,0), new ItemStack(item.getType(), itemsInWarehouse));
				else
					owner.getInventory().setItem(slot, new ItemStack(item.getType(), itemsInWarehouse));
				itemsInWarehouse = 0;
			}
			
		}
		
		slotChest.save();
		updateInventory();
//		owner.sendMessage(CasinoManager.getPrefix() + "You successfully removed " + item.getType().toString() + " from your winnings list");
		owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings_successful_remove").replace("%item%", item.getType().toString()));
	}
	/**
	 * manage a player input 
	 * @param message raw message from player
	 */
	private void playerEingabe(String message) {
		if(waitingForAmount) {
			int amount = 0;
			try {
				amount = Integer.parseInt(message);
			} catch (NumberFormatException e) {
				owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings-amount_not_number"));
				return;
			}
			if(amount <= 0) {
				owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings-amount_lower_0"));
				return;
			}
			waitingForAmount = false;
			newItemAmount = amount;
//			owner.sendMessage(String.format(CasinoManager.getPrefix() + "Type in the weight of this win! Total weight: %.1f",slotChest.getGesamtGewicht()));
			owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings_weight_message").replace("%total_weight%", String.format("%.1f", slotChest.getGesamtGewicht())));
		} else if(waitingForWeight) {
			double amount = 0.0;
			try {
				amount = Double.parseDouble(message);
			} catch (NumberFormatException e) {
				owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings-weight_not_number"));
				return;
			}
			if(amount == 0.0) {
				owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings-weight_lower_0"));
				return;
			}
			waitingForWeight = false;
			newItemWeight = amount;
			eingabeFinished = true;
			newItem();
			owner.openInventory(inventory);
			
			waitingForEingabe.remove(owner);
		}
	}
	
	
	
	private void newItem() {
		ItemStack newItem = new ItemStack(newItemMaterial, newItemAmount);
		slotChest.itemsToWin.put(newItem, newItemWeight);
		
		if(inventoryReadingTasks.containsKey(this)) {
			main.getServer().getScheduler().cancelTask(inventoryReadingTasks.get(this));
			inventoryReadingTasks.remove(this);
		}
		//move item player wanted to input in his inventory
		slotChest.lager.add(inventory.getItem(40));
		inventory.setItem(40, new ItemStack(Material.AIR));
		
		
		
		initializeInventoryReadingTask();
		slotChest.save();
		updateInventory();
	}
	
	private void updateInventory() {
		for(int i = 0; i < 9*4; i++) inventory.setItem(i, new ItemStack(Material.AIR));
		
		int index = 0;
		if(slotChest.itemsToWin.size() == 0) {
			for(int i = 0; i < 4*9; i++) inventory.setItem(i, fillMaterial);
			
			return;
		}
		for(Entry<ItemStack, Double> entry : slotChest.itemsToWin.entrySet()) {
			
			if(index >= 9*4) return;
			
			ItemStack item = entry.getKey();
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(String.format("§5weight: " + entry.getValue() + " ( %.2f %% )", (entry.getValue()/slotChest.getGesamtGewicht())*100));
			item.setItemMeta(meta);
			entry.getKey().setItemMeta(meta);
			inventory.setItem(index, item);
			
			index++;
		}
		for(int i = index; i < 9*4; i++) inventory.setItem(i, new ItemStack(Material.PINK_STAINED_GLASS_PANE));
		CasinoManager.slotChestManager.save();
	}
}

