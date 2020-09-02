package com.chrisimi.casinoplugin.slotchest;



import java.util.Map.Entry;

import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.ClickEvent;
import com.chrisimi.inventoryapi.EventMethodAnnotation;
import com.chrisimi.inventoryapi.IInventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;

public class OwnerInterfaceInventory extends com.chrisimi.inventoryapi.Inventory implements IInventoryAPI, Listener
{
	/*
	 * Interface for the Owner, to configure his chest!
	 */

	private Player owner;

	private SlotChest slotChest;

	private ItemStack warehouseItem = ItemAPI.createItem("§bWarehouse", Material.CHEST);
	private ItemStack winningsItem = ItemAPI.createItem("§awinnings", Material.DIAMOND);
	private ItemStack betItem = ItemAPI.createItem("§6bet", Material.GOLD_INGOT);
	private ItemStack settingsItem = ItemAPI.createItem("§fsettings", Material.IRON_NUGGET);
	
	private ItemStack disableItem = ItemAPI.createItem("§4disable", Material.RED_WOOL);
	private ItemStack enableItem = ItemAPI.createItem("§2enable", Material.GREEN_WOOL);

	public OwnerInterfaceInventory(Player owner, Main main, SlotChest chest) {
		super(owner, 9, Main.getInstance(), "Owner Interface");
		this.owner = owner;
		main;
		this.slotChest = chest;
		Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
		initialize();
	}
	private void initialize() {
		slotChest.maintenance = true;
		createInventory();
		this.addEvents(this);
		openInventory();
	}
	
	
	
	//region initialize
	private void createInventory()
	{
		//put the items into the inventory

		//warehouse is not needed for a server slot chest
		if(!slotChest.isServerOwner())
			bukkitInventory.setItem(0, warehouseItem);

		bukkitInventory.setItem(1, winningsItem);
		bukkitInventory.setItem(6, settingsItem);
		bukkitInventory.setItem(4, betItem);
		bukkitInventory.setItem(8, (slotChest.enabled) ? disableItem : enableItem);
	}
	//endregion

	//region EventHandlers
	
	@EventMethodAnnotation
	public void clickEvent(ClickEvent event) {

		if(event.getClicked().equals(warehouseItem)) openWarehouseMenu();
		else if(event.getClicked().equals(winningsItem)) openWinningsMenu();
		else if(event.getClicked().equals(disableItem)) disableChest();
		else if(event.getClicked().equals(enableItem)) enableChest();
		else if(event.getClicked().equals(betItem)) openBetMenu();
		else if(event.getClicked().equals(settingsItem)) openSettingsMenu();

	}
	
	@EventHandler
	public void onInventoryLeave(InventoryCloseEvent event) {
		if(!(event.getInventory().equals(bukkitInventory))) return;
		
		slotChest.maintenance = false;
	}
	//endregion

	//region click events
	private void openWarehouseMenu()
	{
		closeInventory();
		new WarehouseMenu(Main.getInstance(), slotChest, owner);
	}
	private void openWinningsMenu()
	{
		closeInventory();
		new WinningsMenu(Main.getInstance(), owner, slotChest);
	}
	private void openBetMenu()
	{
		closeInventory();
		new BetMenu(Main.getInstance(), owner, slotChest, this);
		
	}
	private void openSettingsMenu()
	{
		closeInventory();
		new SettingsMenu(Main.getInstance(), slotChest, owner);
	}
	
	private void disableChest()
	{
		this.slotChest.enabled = false;
		bukkitInventory.setItem(8, enableItem);
		CasinoManager.slotChestManager.save();
	}

	private void enableChest()
	{
		boolean hasForbiddenItem = false;
		for(Entry<ItemStack, Double> entry : slotChest.itemsToWin.entrySet())
		{
			if(slotChest.itemIsOnForbiddenList(entry.getKey().getType()))
			{
				owner.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-ownerinterface-cant_activate_forbidden_item").replace("%item%", entry.getKey().getType().toString()));
				hasForbiddenItem = true;
			}
		}
		
		if(hasForbiddenItem) return;
		
		this.slotChest.enabled = true;
		bukkitInventory.setItem(8, disableItem);
		CasinoManager.slotChestManager.save();
	}
	

}
