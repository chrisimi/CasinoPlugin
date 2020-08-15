package com.chrisimi.casinoplugin.slotchest;

import java.util.ArrayList;

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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.slotchest.animations.RollAnimationFactory;

public class SettingsMenu extends com.chrisimi.inventoryapi.Inventory implements Listener, IInventoryAPI
{

	private final Main main;
	private final SlotChest slotChest;
	private final Player owner;

	private ItemStack changeRollAnimationBlock = ItemAPI.createItem("Change the roll animation of this SlotChest", Material.STONE_BUTTON);
	
	public SettingsMenu(Main main, SlotChest slotChest, Player owner)
	{
		super(owner, 9, Main.getInstance(), "Settings");
		this.main = main;
		this.slotChest = slotChest;
		this.owner = owner;
		
		main.getServer().getPluginManager().registerEvents(this, main);
		openInventory();
		addEvents(this);

		initialize();
	}
	
	
	private void initialize()
	{
		updateInventory();
	}
	private void updateInventory()
	{
		ItemAPI.setLore(changeRollAnimationBlock, getLoreForAnimations());
		bukkitInventory.setItem(0, changeRollAnimationBlock);
	}
	
	@EventMethodAnnotation
	public void clickEvent(ClickEvent event)
	{
		if(event.getClicked().equals(changeRollAnimationBlock))
		{
			//move to the next animation
			slotChest.animationID = (slotChest.animationID > RollAnimationFactory.getNameOfAllAnimations().length - 1) ? 1 : slotChest.animationID + 1;
			updateInventory();
		}
	}
	
	@EventHandler
	public void onInventoryLeave(InventoryCloseEvent event)
	{
		if(!(event.getInventory().equals(bukkitInventory))) return;
		
		CasinoManager.slotChestManager.save();
	}

	private ArrayList<String> getLoreForAnimations()
	{
		String[] lores = RollAnimationFactory.getNameOfAllAnimations();
		for(int i = 0; i < lores.length; i++)
		{
			if(i == slotChest.animationID-1)
				lores[i] = "§6" + lores[i];
			else
				lores[i] = "§8" + lores[i];
		}

		ArrayList<String> loresList = new ArrayList<String>(lores.length);
		for(String a : lores)
			loresList.add(a);

		return loresList;
	}
}
