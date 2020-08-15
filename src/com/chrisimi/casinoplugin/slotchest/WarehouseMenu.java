package com.chrisimi.casinoplugin.slotchest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.ClickEvent;
import com.chrisimi.inventoryapi.EventMethodAnnotation;
import com.chrisimi.inventoryapi.IInventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;

public class WarehouseMenu extends com.chrisimi.inventoryapi.Inventory implements IInventoryAPI, Listener{

	private static Material placeHolder = Material.PINK_STAINED_GLASS_PANE;
	public static HashMap<Inventory, Integer> tasks = new HashMap<>();
	
	
	private Main main;
	private SlotChest slotChest;
	private Player owner;
	
	private ItemStack changeSortModeSign = ItemAPI.createItem("§asort by ID", Material.SIGN);
	private Boolean sortByID = true;
	private ItemStack sortButton = ItemAPI.createItem("§6SORT", Material.STONE_BUTTON);
	
	public WarehouseMenu(Main main, SlotChest slotChest, Player owner)
	{
		super(owner, 9*6, Main.getInstance(), "Warehouse");
		this.main = main;
		this.slotChest = slotChest;
		this.owner = owner;
		main.getServer().getPluginManager().registerEvents(this, main);
		initialize();
		addEvents(this);
		setCancelClickEvent(false);
		openInventory();

		initializeInventoryUpdateRunnable();
	}
	
	private void initialize()
	{
		ArrayList<String> loreList = new ArrayList<>();
		loreList.add("§6click to change!");
		ItemAPI.setLore(changeSortModeSign, loreList);
		
		for(int i = 45; i < 9*6; i++) {
			if(i == 49) continue;
			bukkitInventory.setItem(i, new ItemStack(placeHolder));
		}
		for(int i = 0; i < 9*5 ; i++)
		{
			if(i >= slotChest.lager.size())
				bukkitInventory.setItem(i, new ItemStack(placeHolder));
			else
				bukkitInventory.setItem(i, slotChest.lager.get(i));
		}

		bukkitInventory.setItem(52, changeSortModeSign);
		bukkitInventory.setItem(53, sortButton);
	}
	
	private void updateInventory()
	{
		bukkitInventory.setItem(52, changeSortModeSign);
		bukkitInventory.setItem(53, sortButton);
	}

	private void initializeInventoryUpdateRunnable()
	{
		int taskId = Main.getInstance().getServer()
				.getScheduler()
				.scheduleSyncRepeatingTask(Main.getInstance(), warehouseRunnable, 10L, 10L);
	}

	private Runnable warehouseRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			ItemStack itemStack = bukkitInventory.getItem(49);
			if(itemStack == null) return;

			//check if it's not a valid item
			if(itemStack.getType() == Material.AIR || itemStack.getType() == placeHolder) return;

			//check if the item is registered as a win
			if(!slotChest.itemstoWinContains(itemStack.getType()))
			{
				owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-warehouse_item_no_win"));
				bukkitInventory.setItem(49, null);
				ItemAPI.putItemInInventory(itemStack, player);
			}

			//check if slot chest has enough space
			if(slotChest.lager.size() >= 9*5)
			{
				owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-warehouse_is_full"));
				bukkitInventory.setItem(49, null);
				ItemAPI.putItemInInventory(itemStack, player);
			}

			for(int i = 0; i < 9*5; i++)
				if(bukkitInventory.getItem(i).getType() == placeHolder)
				{
					bukkitInventory.setItem(i, itemStack);
					break;
				}

			//add item and sort it
			slotChest.lager.add(itemStack);
			bukkitInventory.setItem(49, null);
			sortLager();
			updateLager();
			owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-warehouse_successful").replace("%item%", itemStack.getType().toString()));

			slotChest.save();
			CasinoManager.slotChestManager.save();
		}
	};

	//region EventHandlers

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
	{
		if(!(event.getInventory().equals(bukkitInventory))) return;
		
		if(tasks.containsKey(event.getInventory()))
		{
			Bukkit.getServer().getScheduler().cancelTask(tasks.get(event.getInventory()));
			tasks.remove(event.getInventory());
		}

		updateLager();
		CasinoManager.slotChestManager.save();
	}

	@EventHandler
	public void clickEvent(InventoryClickEvent event)
	{
		if(event.getCurrentItem() == null) return;
		if(!(event.getInventory().equals(bukkitInventory))) return;

		if(event.getCurrentItem().equals(new ItemStack(placeHolder))) {
			event.setCancelled(true);
			return;
		}

		if(event.getCurrentItem().equals(changeSortModeSign))
		{
			sortByID = !sortByID;

			ArrayList<String> loreList = new ArrayList<>();
			loreList.add("§6click to change!");

			ItemAPI.changeName(changeSortModeSign, (sortByID) ? "§asort by ID" : "§asort by name");
			ItemAPI.setLore(changeSortModeSign, loreList);

			event.setCancelled(true);
			updateInventory();
		} else if(event.getCurrentItem().equals(sortButton))
		{
			updateLager();
			sortLager();
			event.setCancelled(true);
		}

		updateInventory();
	}
	//endregion

	//region sort system
	public enum SortType
	{
		ID,
		NAME
	}
	private void sortLager() {
		updateLager();
		
		if(sortByID)
			sortLager(SortType.ID);
		else
			sortLager(SortType.NAME);

		updateLager();
	}

	private void sortLager(SortType type)
	{
		HashMap<Material, Integer> items = slotChest.getLagerWithNumbers();
		
//		for(Entry<Material, Integer> entry : items.entrySet()) 
//			System.out.println(entry.getKey().toString() + " " + entry.getValue());
//		System.out.println("---");

		TreeMap<Material, Integer> sortedList = new TreeMap<>(items);
		
//		for(Entry<Material, Integer> entry : sortedList.entrySet()) 
//			System.out.println(entry.getKey().toString() + " " + entry.getValue());
//		System.out.println("---");
		
		Comparator<Entry<Material, Integer>> valueComperator = new Comparator<Map.Entry<Material,Integer>>() {
			@Override
			public int compare(Entry<Material, Integer> e1, Entry<Material, Integer> e2) {
				switch (type)
				{
				case ID:
					return e2.getKey().compareTo(e1.getKey());
					
				case NAME:
					return e1.getKey().toString().compareTo(e2.getKey().toString());

				default:
					return 0;
				}
			}
		};
		
		ArrayList<Entry<Material, Integer>> listOfEntries = new ArrayList<Entry<Material, Integer>>(sortedList.entrySet());
		Collections.sort(listOfEntries, valueComperator);
		
		//for(Entry<Material, Integer> entry : listOfEntries) System.out.println(entry.getKey().toString() + " " + entry.getValue());
		
		
		LinkedHashMap<Material, Integer> sortedByList = new LinkedHashMap<Material, Integer>(listOfEntries.size());
		for(Entry<Material, Integer> entry : listOfEntries) sortedByList.put(entry.getKey(), entry.getValue());
		
		//clear inv
		for(int i = 0; i < bukkitInventory.getSize(); i++)
		{
			if(i == 49 || i == 52 || i == 53) continue; //ignore this 3 slots
			bukkitInventory.setItem(i, new ItemStack(placeHolder));
		}
		
		
		int slot = 0;
		
		for(Entry<Material, Integer> entry : sortedByList.entrySet())
		{
			if(entry.getValue() <= 0) continue;
			CasinoManager.Debug(this.getClass(), entry.getKey().toString() + " " + entry.getValue());
			
			
			int amountOfItems = entry.getValue();
			
			while(amountOfItems >= 1)
			{
				CasinoManager.Debug(this.getClass(), slot + " " + entry.getKey().toString() + " " + amountOfItems);

				if(amountOfItems > entry.getKey().getMaxStackSize())
				{
					bukkitInventory.setItem(slot, new ItemStack(entry.getKey(), entry.getKey().getMaxStackSize()));
					slot++;
					amountOfItems -= entry.getKey().getMaxStackSize();
				} else {
					bukkitInventory.setItem(slot, new ItemStack(entry.getKey(), amountOfItems));
					slot++;
					break;
				}
			}
		}
		ArrayList<ItemStack> stock = new ArrayList<>();
		for(ItemStack item : bukkitInventory.getContents())
			if(item != null)
				stock.add(item);
		
		slotChest.lager = stock;
		slotChest.save();
	}
	//endregion
	
	private void updateLager() {
		ArrayList<ItemStack> tempLager = new ArrayList<ItemStack>();
		for(int i = 0; i < 9*5; i++)
		{
			if(bukkitInventory.getItem(i) == null || bukkitInventory.getItem(i).getType() == Material.AIR || bukkitInventory.getItem(i).getType() == placeHolder) continue;
			tempLager.add(bukkitInventory.getItem(i));
		}

		slotChest.lager = tempLager;
		slotChest.save();
	}
}
