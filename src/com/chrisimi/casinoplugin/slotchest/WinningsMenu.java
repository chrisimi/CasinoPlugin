package com.chrisimi.casinoplugin.slotchest;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.ChatEvent;
import com.chrisimi.inventoryapi.ClickEvent;
import com.chrisimi.inventoryapi.EventMethodAnnotation;
import com.chrisimi.inventoryapi.IInventoryAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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
public class WinningsMenu extends com.chrisimi.inventoryapi.Inventory implements IInventoryAPI, Listener {

	private enum WaitingFor
	{
		NONE,
		AMOUNT,
		WEIGHT
	}
	
	public static HashMap<WinningsMenu, Integer> inventoryReadingTasks = new HashMap<>();
	
	
	private Main main;
	private Player owner;
	private SlotChest slotChest;
	
	private ItemStack placeHolder = new ItemStack(Material.PINK_STAINED_GLASS_PANE);

	private int newItemAmount = 0;
	private double newItemWeight = 0;
	private Material newItemMaterial = null;
	private WaitingFor waitingFor = WaitingFor.NONE;

	public WinningsMenu instance = this;
	public WinningsMenu(Main main, Player owner, SlotChest slotChest) {
		super(owner, 9*5, Main.getInstance(), "Winnings Menu");
		this.main = main;
		this.owner = owner;
		this.slotChest = slotChest;

		Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
		this.addEvents(this);
		cancelEventWhenClickEvent(false);
		openInventory();

		initialize();
	}
	
	private void initialize()
	{
		for(int i = 0; i < 9*5; i++)
		{
			//ignore slot 40
			if(i == 40) continue;
			bukkitInventory.setItem(i, placeHolder);
		}
		
		
		//inventoryReadTask
		initializeInventoryReadingTask();
		updateInventory();
	}

	/**
	 * start the background task which tracks the input from the player
	 */

	private Runnable inventoryReadingTask = new Runnable()
	{
		@Override
		public void run()
		{
			ItemStack check = bukkitInventory.getItem(40);

			//validate if it's a valid item and can be used further
			if(check == null || check == placeHolder || check.getType() == Material.AIR) return;

			//check so that there are no duplicates
			if(slotChest.itemstoWinContains(check.getType()))
			{
				owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings_exists"));
				ItemAPI.putItemInInventory(check, owner);
				bukkitInventory.setItem(40, null);
				return;
			}

			//check if there is enough space to prevent overflow
			if(slotChest.itemsToWin.size() >= 9*5)
			{
				owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings_reached_limit"));
				ItemAPI.putItemInInventory(check, owner);
				bukkitInventory.setItem(40, null);
				return;
			}

			//check if item is on the forbidden list to prevent player adding forbidden item in the slot chest
			if(slotChest.itemIsOnForbiddenList(check.getType()))
			{
				owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings_forbidden_item"));
				ItemAPI.putItemInInventory(check, owner);
				bukkitInventory.setItem(40, null);
				return;
			}

			Main.getInstance().getServer().getScheduler().cancelTask(inventoryReadingTasks.get(instance));
			inventoryReadingTasks.remove(instance);

			owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings_amount_message").replace("%item%", check.getType().toString()));
			newItemMaterial = check.getType();
			owner.closeInventory();

			instance.waitforChatInput(player);
			waitingFor = WaitingFor.AMOUNT;
		}
	};

	private void initializeInventoryReadingTask() {
		int taskNumber = main.getServer().getScheduler().scheduleSyncRepeatingTask(main, inventoryReadingTask, 10L, 10L);
		inventoryReadingTasks.put(this, taskNumber);
	}

	
	//
	//EventHandler

	@EventMethodAnnotation
	public void onChat(ChatEvent event)
	{
		switch(waitingFor)
		{
			case AMOUNT:
			{
				try
				{
					newItemAmount = Integer.parseInt(event.getMessage());
					if(newItemAmount <= 0)
					{
						owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings-amount_lower_0"));
						stop();
						return;
					}

					owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings_weight_message").replace("%total_weight%", String.format("%.1f", slotChest.getGesamtGewicht())));
					waitingFor = WaitingFor.WEIGHT;
					waitforChatInput(owner);
				} catch(Exception e)
				{
					owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-double_invalid"));
					stop();
				}

				return;
			}
			case WEIGHT:
			{
				try
				{
					newItemWeight = Double.parseDouble(event.getMessage());
					if(newItemWeight <= 0.0)
					{
						owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings-weight_lower_0"));
						stop();
						return;
					}

					newItem();
					openInventory();
				} catch(Exception e)
				{
					owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings-weight_not_number"));
					stop();
				}

				return;
			}
		}
	}

	private void stop()
	{
		newItemWeight = 0.0;
		newItemAmount = 0;
		newItemMaterial = Material.AIR;
		waitingFor = WaitingFor.NONE;

		openInventory();
	}

	@EventMethodAnnotation
	public void clickEvent(ClickEvent event)
	{

	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if(event.getCurrentItem() == null) return;
		if(!(event.getInventory().equals(bukkitInventory))) return;
		
		if(event.getCurrentItem().equals(placeHolder))
		{
			event.setCancelled(true);
			return;
		}
		
		HashMap<ItemStack, Double> newHashMap = new HashMap<>(); 
		
		for(Entry<ItemStack, Double> entry : slotChest.itemsToWin.entrySet())
		{
			if(entry.getKey().equals(event.getCurrentItem()))
			{
				removeItemToWin(entry.getKey());
				bukkitInventory.setItem(event.getSlot(), null);
				System.out.println("cancel");
				event.setCancelled(true);
			} else
				newHashMap.put(entry.getKey(), entry.getValue());
			
		}

		slotChest.itemsToWin = newHashMap;
		updateInventory();
		
	}
	
	/**
	 * remove a item from the list of items to win (winnable items)
	 * @param item item to remove 
	 */
	private void removeItemToWin(ItemStack item)
	{
		int itemsInWarehouse = 0;
		ArrayList<ItemStack> itemLagerToRemove = new ArrayList<>(); 
		for(ItemStack itemStack : slotChest.lager)
		{
			//iterate through the sorted lager and make a list from all itemstacks which are the same type as the one to delete
			if(itemStack.getType().equals(item.getType()))
			{
				itemsInWarehouse += itemStack.getAmount();
				itemLagerToRemove.add(itemStack);

				//drop the owner the items
				ItemAPI.putItemInInventory(itemStack, owner);
			}
		}

		slotChest.lager.removeAll(itemLagerToRemove);
		
		slotChest.save();
		updateInventory();
		owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-winnings_successful_remove").replace("%item%", item.getType().toString()));
	}
	
	private void newItem()
	{
		ItemStack newItem = new ItemStack(newItemMaterial, newItemAmount);
		slotChest.itemsToWin.put(newItem, newItemWeight);

		if(inventoryReadingTasks.containsKey(this))
		{
			main.getServer().getScheduler().cancelTask(inventoryReadingTasks.get(this));
			inventoryReadingTasks.remove(this);
		}

		//move item input into the warehouse of the slot chest
		slotChest.lager.add(bukkitInventory.getItem(40));
		bukkitInventory.setItem(40, null);
		
		
		
		initializeInventoryReadingTask();
		slotChest.save();
		updateInventory();
	}
	
	private void updateInventory()
	{
		for(int i = 0; i < 9*4; i++) bukkitInventory.setItem(i, null);
		
		int index = 0;
		if(slotChest.itemsToWin.size() == 0)
		{
			for(int i = 0; i < 4*9; i++) bukkitInventory.setItem(i, placeHolder);

			return;
		}

		for(Entry<ItemStack, Double> entry : slotChest.itemsToWin.entrySet())
		{
			
			if(index >= 9*4) return;
			
			ItemStack item = entry.getKey();
			ItemAPI.changeName(item, String.format("§5weight: " + entry.getValue() + " ( %.2f %% )", (entry.getValue()/slotChest.getGesamtGewicht())*100));
			bukkitInventory.setItem(index, item);
			
			index++;
		}
		for(int i = index; i < 9*4; i++) bukkitInventory.setItem(i, placeHolder);

		CasinoManager.slotChestManager.save();
	}
}

