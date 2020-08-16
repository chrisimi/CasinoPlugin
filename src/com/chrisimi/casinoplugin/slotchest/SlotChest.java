package com.chrisimi.casinoplugin.slotchest;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import com.chrisimi.casinoplugin.main.Main;
import org.apache.commons.lang.math.DoubleRange;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.UpdateManager;
import com.google.gson.annotations.Expose;


public class SlotChest {
	
	@Expose
	public Boolean enabled;
	@Expose
	public String ownerUUID;
	@Expose
	/**
	 * List with items a player can win; string: name of item&amount, weight
	 */
	public HashMap<String, Double> _itemsToWin;
	@Expose
	/**
	 * Shows, how many items there are in the chest for players to win (name of item, number of items)
	 */
	public HashMap<String, Integer> _lager;
	
	@Expose
	public Integer warehouseLevel;
	
	@Expose
	public Integer winningsLevel;
	
	@Expose
	public double bet;
	
	@Expose
	public double x;
	@Expose
	public double y;
	@Expose
	public double z;
	@Expose
	public String worldname;
	@Expose
	public int animationID;
	
	/**
	 * when owner is in the OwnerInterface, this SlotChest will be true
	 */
	public Boolean maintenance = false;
	
	public Boolean running = false;
	
	public ArrayList<ItemStack> lager = new ArrayList<>();
	public HashMap<ItemStack, Double> itemsToWin = new HashMap<>();
	
	public SlotChest() {}

	/**
	 * use this constructor to create a server slot chest
	 * @param lrc {@linkplain Location} of the slot chest
	 */
	public SlotChest(Location lrc)
	{
		enabled = false;

		_itemsToWin = new HashMap<String, Double>();
		_lager = new HashMap<String, Integer>();
		warehouseLevel = 0;
		winningsLevel = 0;
		x = lrc.getX();
		y = lrc.getY();
		z = lrc.getZ();
		worldname = lrc.getWorld().getName();
		animationID = 1;

		ownerUUID = "server";

		initialize();
	}

	/**
	 * use this constructor to create a player slot chest
	 * @param owner Owner of the slot chest
	 * @param lrc {@linkplain Location} of the slot chest
	 */
	public SlotChest(Player owner, Location lrc)
	{
		this(lrc);
		ownerUUID = owner.getUniqueId().toString();
	}
	
	public void initialize() {
		if(_lager != null && _lager.size() != 0)
			lager = getLagerFromData();
		if(!(_itemsToWin == null || _itemsToWin.size() == 0))
			itemsToWin = getItemsToWinFromData();
		
	}
	public void save()
	{
		if(itemsToWin != null && itemsToWin.size() != 0) {
			_itemsToWin = new HashMap<String, Double>();
			for(Entry<ItemStack, Double> entry : itemsToWin.entrySet()) {
				String inputString = entry.getKey().getType().toString() + "&" + entry.getKey().getAmount();
				_itemsToWin.put(inputString, entry.getValue());
			}
		}
		if(lager != null && lager.size() != 0)
		{
			_lager = new HashMap<String, Integer>();
			for(Entry<Material, Integer> entry : getLagerWithNumbers().entrySet()) {
				_lager.put(entry.getKey().toString(), entry.getValue());
			}
		}
	}
	public Boolean itemstoWinContains(Material material) {
		for(Entry<ItemStack, Double> entry : itemsToWin.entrySet()) {
			if(entry.getKey().getType().equals(material)) return true;
			
		}
		return false;
	}

	public boolean isServerOwner()
	{
		return this.ownerUUID.equalsIgnoreCase("server");
	}

	/**
	 * deposit money to the owner
	 * does also work if it's a server slot chest
	 * @param amount to deposit
	 */
	public void giveOwnerMoney(double amount)
	{
		if(!isServerOwner())
		{
			Main.econ.depositPlayer(getOwner(), amount);
		}
	}

	/**
	 * Get the Weight of all elements
	 * @return gesamtGewicht
	 */
	public Double getGesamtGewicht() {
		Double value = 0.0;
		for(Entry<ItemStack, Double> entry : itemsToWin.entrySet()) {
			value += entry.getValue();
		}
		return value;
	}
	
	public Location getLocation() {
		World world = Bukkit.getWorld(worldname);
		if(world == null) {
			try {
				throw new Exception("World is not valid!");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		
			
		}
		return new Location(world, x, y, z);
	}
	public OfflinePlayer getOwner()
	{
		return (isServerOwner()) ? null : Bukkit.getOfflinePlayer(UUID.fromString(this.ownerUUID));
	}
	public HashMap<Material, Integer> getLagerWithNumbers() {
		HashMap<Material, Integer> returnValue = new HashMap<>();
		
		for(ItemStack item : lager) {
			if(item == null) continue;
			
			if(returnValue.containsKey(item.getType())) {
				returnValue.compute(item.getType(), (key, val) -> val + item.getAmount() );
				
			} else {
				returnValue.put(item.getType(), item.getAmount());
			}
		}
		return returnValue;
	}
	public Boolean hasChestEnough() {
		HashMap<Material, Integer> warehouseValues = getLagerWithNumbers();
		
		for(Entry<ItemStack, Double> entry : itemsToWin.entrySet()) {
			
			if(entry.getKey() == null) continue;
			if(entry.getKey().getType() == null) {
				continue;
			}
			if(!warehouseValues.containsKey(entry.getKey().getType())) return false;
			
			if(warehouseValues.get(entry.getKey().getType()) < entry.getKey().getAmount()) {
				return false;
			}
		}
		return true;
	}
	
	private ArrayList<ItemStack> getLagerFromData() {
		ArrayList<ItemStack> returnValue = new ArrayList<ItemStack>();
		for(Entry<String, Integer> entries : _lager.entrySet())
		{
			Material material = Enum.valueOf(Material.class, entries.getKey());
			int amount = entries.getValue();
			while(amount > 0)
			{
				ItemStack itemStack = new ItemStack(material, (amount > material.getMaxStackSize()) ?  material.getMaxStackSize() : amount);
				amount -= material.getMaxStackSize();
				returnValue.add(itemStack);
			}
		}

		return returnValue;
	}
	private HashMap<ItemStack, Double> getItemsToWinFromData() {
		HashMap<ItemStack, Double> returnValue = new HashMap<ItemStack, Double>();
		
		for(Entry<String, Double> entry : _itemsToWin.entrySet()) {
			
			String[] splited = entry.getKey().split("&");
			if(splited.length != 2) Bukkit.getLogger().info("Error: WinningsItem format not correct!");
			
			Material material = Enum.valueOf(Material.class, splited[0]);
			int amount = Integer.parseInt(splited[1]);
			
			returnValue.put(new ItemStack(material, amount), entry.getValue());
		}
		return returnValue;
	}
	public ItemStack getRandomItem() {
		Random random = new Random();
		
		double gesamtGewicht = getGesamtGewicht();
		Comparator<Entry<ItemStack, Double>> comparator = new Comparator<Entry<ItemStack, Double>>() {

			@Override
			public int compare(Entry<ItemStack, Double> o1, Entry<ItemStack, Double> o2) {
				
				return o1.getValue().compareTo(o2.getValue());
			}
		};
		ArrayList<Entry<ItemStack, Double>> sortedList = new ArrayList<Entry<ItemStack, Double>>(itemsToWin.entrySet());
		sortedList.sort(comparator);
		LinkedHashMap<ItemStack, Double> linkedList = new LinkedHashMap<>();
		for(Entry<ItemStack, Double> entry : sortedList) {
			linkedList.put(entry.getKey(), entry.getValue());
		}
		
		double randomValue = random.nextDouble()*gesamtGewicht;
		Map.Entry<ItemStack, Double> latest = null; 
		for(Entry<ItemStack, Double> entry : linkedList.entrySet()) {
			if(latest == null) {
				latest = entry;
				continue;
			}
			
			if(latest == entry) continue; 
			
			
			if(latest.getValue() < randomValue && entry.getValue() > randomValue)
				return entry.getKey();
			if(latest.getValue() > randomValue)
				return latest.getKey();
			latest = entry;
		}
		
		//remove displayname
		ItemMeta meta = latest.getKey().getItemMeta();
		meta.setDisplayName(latest.getKey().getItemMeta().getDisplayName());
		
		return latest.getKey();
	}
	public void RemoveItemsFromWarehouse(ItemStack itemStack) {
		HashMap<Material, Integer> lagerBestand = getLagerWithNumbers();
		
		if(!lagerBestand.containsKey(itemStack.getType())) {
			CasinoManager.LogWithColor(ChatColor.RED + "SlotChest does not contain item!");
			return;
		}
		lagerBestand.compute(itemStack.getType(), (a, b) -> b - itemStack.getAmount());
		
		ArrayList<ItemStack> newLager = new ArrayList<>();
		for(Entry<Material, Integer> entry : lagerBestand.entrySet()) {
			int amount = entry.getValue();
			
			while(amount != 0) {
				if(amount > 64) {
					newLager.add(new ItemStack(entry.getKey(), 64));
					amount-= 64;
				} else {
					newLager.add(new ItemStack(entry.getKey(), amount));
					amount = 0;
				}
			}
			
			
		}
		lager = newLager;
		CasinoManager.slotChestManager.save();
	}
	
	
	@SuppressWarnings("unchecked") 
	public boolean itemIsOnForbiddenList(Material itemStack) {
		ArrayList<String> inputList = new ArrayList<>();
		inputList = (ArrayList<String>) UpdateManager.getValue("slotchest-list-of-banned-items", new ArrayList<String>());
		
		ArrayList<Material> bannedList = new ArrayList<>();
		for(String string : inputList) {
			try {
				bannedList.add(Enum.valueOf(Material.class, string));
			} catch (IllegalArgumentException e) {
				CasinoManager.LogWithColor(org.bukkit.ChatColor.RED + string + " is not a valid Minecraft-Block!");
				continue;
			}
			
		}
		return bannedList.contains(itemStack);
		
	}
	
}
