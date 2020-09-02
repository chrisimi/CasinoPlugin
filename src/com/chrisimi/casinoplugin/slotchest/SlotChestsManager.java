package com.chrisimi.casinoplugin.slotchest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.UpdateManager;
import com.chrisimi.casinoplugin.slotchest.animations.RollAnimationManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class SlotChestsManager implements Listener{
	/* when owner clicks on slotchest, a interface will pop up where he can configure the chest
	 * 9 slots with items to win and to bet
	 * 
	 * TODO upgrades to get more space in warehouse and winnings menu
	 * 
	 */
	private static HashMap<Location, SlotChest> slotChests = new HashMap<Location, SlotChest>();
	
	
	private static int configMaxAmount;
	private static Boolean configOpUnlimited;

	private Main main;
	private GsonBuilder builder;
	private Gson gson;
	public SlotChestsManager(Main main) {
		this.main = main;
		Main.getInstance().getServer().getPluginManager().registerEvents(this, main);
		builder = new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting();
		gson = builder.create();
		
		updateConfigValues();
		
		try {
			importChests();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void updateConfigValues()
	{
		configMaxAmount = Integer.parseInt(UpdateManager.getValue("slotchest-max-amount", 5).toString());
		configOpUnlimited = Boolean.valueOf(UpdateManager.getValue("slotchest-op-unlimited", true).toString());
	}
	private void importChests() throws IOException {
		slotChests.clear();
		StringBuilder json = new StringBuilder();
		String line = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(Main.slotChestsYml));
		} catch (FileNotFoundException e) {
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to find slotchests.json for importing!");
			
			e.printStackTrace();
			return;
		}
		
		while((line = reader.readLine()) != null) {
			json.append(line);
		}
		reader.close();
		if(json.length() < 24)
			return;

		SlotChestsJson slotChestsJson = null;
		try {
			slotChestsJson = gson.fromJson(json.toString(), SlotChestsJson.class);
		} catch(JsonSyntaxException jse) {
			CasinoManager.LogWithColor(ChatColor.RED + "An Error occured while trying to import SlotChests from json: Invalid Json file!");
			CasinoManager.LogWithColor(ChatColor.BLUE + "2 things you can do:\n1. check the json file on your own after errors or use https://jsonlint.com \n2. SAVE! the json file with an other name and let the plugin create a new json file!");
			CasinoManager.LogWithColor(ChatColor.RED + "Closing Server because of an fatal error!");
			Bukkit.shutdown();
			
			return;
		}
		if(slotChestsJson.slotChests.size() == 0) {
			
			if(CasinoManager.configEnableConsoleMessages)
				CasinoManager.LogWithColor(ChatColor.YELLOW + "No SlotChests to import!");
			return;
		}
		for(SlotChest chest : slotChestsJson.slotChests) {
			if(chest == null) {
				CasinoManager.LogWithColor(ChatColor.RED + "Found a damaged SlotChest in json data... Data will be deleted!");
				continue;
			} else if(chest.getLocation() == null) {
				CasinoManager.LogWithColor(ChatColor.RED + "Found a damaged SlotChest in json data...  Data will be deleted!");
				continue;
			}
			slotChests.put(chest.getLocation(), chest);
			chest.initialize(); //- initialize the virtual warehouse
			
		}
		if(slotChestsJson.slotChests.size() >= 1) {
			
			if(CasinoManager.configEnableConsoleMessages)
				CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully imported " + slotChests.size() + " SlotChests from slotchests.json!");
		}
	}
	private synchronized void exportChests()  throws IOException {
		slotChests.forEach((a, b) -> b.save());
		
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(Main.slotChestsYml));
		writer.write("");
		
		ArrayList<SlotChest> array = new ArrayList<SlotChest>(slotChests.size());
		for(Entry<Location, SlotChest> entry : slotChests.entrySet()) {
			array.add(entry.getValue());
		}
		SlotChestsJson jsonObject = new SlotChestsJson();
		jsonObject.slotChests = array;
		
		String json = gson.toJson(jsonObject, SlotChestsJson.class);
		writer.write(json);
		writer.close();

		if(CasinoManager.configEnableConsoleMessages)
			CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully saved SlotChests!");
		
	}
	public void reload() {
		try {
			exportChests();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		updateConfigValues();
		
	}
	public void save() {
		try {
			exportChests();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	//region event handlers
	
	@EventHandler
	public void onChestClick(PlayerInteractEvent event)
	{
		if(event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR) return;
		if(!(event.getClickedBlock().getType().toString().contains("CHEST"))) return;
		
		Chest chest = null;
		try 
		{
			chest = (Chest) event.getClickedBlock().getState(); 
		} catch(Exception e) {
			return;
		}
		
		if(chest == null) {
			return;
		}
		if(!(slotChests.containsKey(chest.getLocation())))
			return;

		SlotChest slotChest = slotChests.get(chest.getLocation());

		if(event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			if(isPermitted(event.getPlayer(), slotChest) && event.getPlayer().isSneaking()) {
				event.setCancelled(false);
				return;
			}
			
			clickOnChest(event);
			return;
		}
		
		if(!isPermitted(event.getPlayer(), slotChest))
		{
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-creation-not_your_chest"));
			event.setCancelled(true);
			return;
		}
		
		if(event.getPlayer().isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			openOwnerInterface(event);

		} else {
			openWarehouseDirectly(event);
		}
		event.setCancelled(true);

	}

	private boolean isPermitted(Player player, SlotChest slotChest)
	{
		return (slotChest.isServerOwner() && (Main.perm.has(player, "casino.admin") || Main.perm.has(player, "casino.slotchest.server"))) ||
				(!slotChest.isServerOwner() && player.getUniqueId().equals(slotChest.getOwner().getUniqueId()));
	}

	@EventHandler
	public void onChestBreak(BlockBreakEvent event) {
		if(!(slotChests.containsKey(event.getBlock().getLocation()))) return;
		
		SlotChest slotChest = slotChests.get(event.getBlock().getLocation());
		if(!isPermitted(event.getPlayer(), slotChest)) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-creation_not_your_chest"));
			event.setCancelled(true);
			return;
		}
		if(slotChest.lager.size() >= 1 && !slotChest.isServerOwner()) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-break_inventory_not_empty"));
			event.setCancelled(true);
			return;
		}
		
		slotChests.remove(slotChest.getLocation());
		event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-break_successful"));
		reload();
		
	}
	//endregion
	//region event methods
	
	private void clickOnChest(PlayerInteractEvent event)
	{
		SlotChest slotChest = slotChests.get(event.getClickedBlock().getLocation());
		if(!(slotChest.enabled)) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest_is_disabled"));
			event.setCancelled(true);
			return;
		}
		if(slotChest.maintenance) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest_owner_editing"));
			event.setCancelled(true);
			return;
		}
	
		//event.getPlayer().sendMessage("opening animation menu!");
		startAnimation(event.getPlayer(), slotChests.get(event.getClickedBlock().getLocation()));
		event.setCancelled(true);
		return;
	}

	/**
	 * start the {@link RollAnimationManager} when a player want's to play on the slot chest
	 * @param player {@linkplain Player} instance of whom want's to play
	 * @param slotchest {@linkplain SlotChest} instance of the used slot chest
	 */
	private void startAnimation(Player player, SlotChest slotchest)
	{
		Main.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new RollAnimationManager(player, slotchest, main), 0L);
	}

	/**
	 * open the warehouse directly
	 * @param event {@linkplain PlayerInteractEvent} instance
	 */
	private void openWarehouseDirectly(PlayerInteractEvent event)
	{
		SlotChest chest = slotChests.get(event.getClickedBlock().getLocation());
		new WarehouseMenu(Main.getInstance(), chest, event.getPlayer());
	}

	/**
	 * open the owner interface
	 * @param event {@linkplain PlayerInteractEvent} instance
	 */
	private void openOwnerInterface(PlayerInteractEvent event)
	{
		SlotChest chest = slotChests.get(event.getClickedBlock().getLocation());
		new OwnerInterfaceInventory(event.getPlayer(), main, chest);
	}
	//endregion

	/**
	 * get the amount of slot chests a player owns
	 * @param player {@linkplain Player} instance which should be the owner
	 * @return the amount of slot chests
	 */
	private static int getAmountForPlayer(Player player)
	{
		return getSlotChestsFromPlayer(player).size();
	}

	/**
	 * create a new slot chest
	 * @param lrc {@linkplain Location} location of the new slot chest
	 * @param creator {@linkplain Player} the new owner of the slotchest or if it's a server sign the player who is currently working on it
	 * @param serverSlotChest if it's a server slot chest or not
	 */
	public static void createSlotChest(Location lrc, Player creator, boolean serverSlotChest)
	{
		if(slotChests.containsKey(lrc))
		{
			creator.sendMessage(CasinoManager.getPrefix() + "§4This is a SlotChest");
			return;
		}

		//so that player cannot exceed limit of slot chests
		if(!serverSlotChest && getAmountForPlayer(creator) >= configMaxAmount)
		{
			if(!(creator.isOp() && configOpUnlimited))
			{
				creator.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-creation_exceed_limit").replace("%limit%", String.valueOf(configMaxAmount)));
				return;
			}
		}

		if(serverSlotChest)
			slotChests.put(lrc, new SlotChest(lrc));
		else
			slotChests.put(lrc, new SlotChest(creator, lrc));

		try
		{
			CasinoManager.slotChestManager.exportChests();
			creator.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-creation-successful"));

			for(int i = 0; i < 50; i++)
				lrc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, lrc, 5, null);
		} catch (IOException e)
		{
			e.printStackTrace();
			creator.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-creation_error"));
		}
		
	}

	/**
	 * get all slot chest a player owns
	 * @param player {@linkplain Player} instance which should be the owner of the slot chests
	 * @return a {@linkplain ArrayList} containing {@linkplain SlotChest} instances which belongs to the player, empty if the player does not owns a slot chest
	 */
	public static ArrayList<SlotChest> getSlotChestsFromPlayer(Player player){
		ArrayList<SlotChest> chestList = new ArrayList<SlotChest>();
		for(Entry<Location, SlotChest> entry : slotChests.entrySet()) {
			if(entry.getValue().getOwner().equals(player))
				chestList.add(entry.getValue());
		}
		return chestList;
	}
}
