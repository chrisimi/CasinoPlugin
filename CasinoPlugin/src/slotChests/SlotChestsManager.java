package slotChests;

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

import com.chrisimi.casino.main.Main;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import scripts.CasinoManager;
import scripts.UpdateManager;
import slotChests.Animations.RollAnimationManager;

public class SlotChestsManager implements Listener{
	/* when owner clicks on slotchest, a interface will pop up where he can configure the chest
	 * 9 slots with items to win and to bet
	 * 
	 * upgrades to get more space in warehouse and winningstab
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
		main.getServer().getPluginManager().registerEvents(this, main);
		builder = new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting();
		gson = builder.create();
		try {
			importChests();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateConfigValues();
	}
	
	private void updateConfigValues() {
		try {
			configMaxAmount = Integer.valueOf(UpdateManager.getValue("slotchest.max-amount").toString());
		} catch(NumberFormatException e) {
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get max-amount for SlotChest: value is not a valid number!");
			//main.getLogger().info("CONFIG-ERROR: While trying to configure );
			configMaxAmount = 5;
		}
		try {
			configOpUnlimited = Boolean.valueOf(UpdateManager.getValue("slotchest.op-unlimited").toString());
		} catch(NumberFormatException e) {
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get op-unlimited for SlotChest: value is not a valid boolean!");
			configOpUnlimited = false;
		}
		
	}
	private void importChests() throws IOException {
		slotChests.clear();
		String json = "";
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
			json += line;
		}
		reader.close();
		if(json.length() < 24)
			return;
		
		//TODO: implement function to check if chest is valid!
		SlotChestsJson slotChestsJson = gson.fromJson(json, SlotChestsJson.class);
		if(slotChestsJson.slotChests.size() == 0) {
			return;
		}
		for(SlotChest chest : slotChestsJson.slotChests) {
			slotChests.put(chest.getLocation(), chest);
			chest.initialize(); //- initialize the virtual warehouse
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
		CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully saved SlotChests!");
		
	}
	public void reload() {
		try {
			exportChests();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
	//-----------------------------------------------------------------------------
	//EventHandlers
	
	@EventHandler
	public void onChestClick(PlayerInteractEvent event) {
		if(event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR) return;
		if(!(event.getClickedBlock().getType().toString().contains("CHEST"))) return;
		
		Chest chest = (Chest) event.getClickedBlock().getState(); 
		if(chest == null) {
			return;
		}
		if(!(slotChests.containsKey(chest.getLocation()))) {
			return;
		}
		if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if(event.getPlayer().equals(slotChests.get(chest.getLocation()).getOwner().getPlayer()) && event.getPlayer().isSneaking()) {
				event.setCancelled(false);
				return;
			}
			
			clickOnChest(event);
			return;
		}
		
		if(!(event.getPlayer().equals(slotChests.get(chest.getLocation()).getOwner().getPlayer()))) return;
		
		if(event.getPlayer().isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			openOwnerInterface(event);
			
			event.setCancelled(true);
		} else {
			openWarehouseDirectly(event);
			event.setCancelled(true);
		}
		
	}
	
	@EventHandler
	public void onChestBreak(BlockBreakEvent event) {
		if(!(slotChests.containsKey(event.getBlock().getLocation()))) return;
		
		SlotChest slotChest = slotChests.get(event.getBlock().getLocation());
		if(!(event.getPlayer().equals(slotChest.getOwner().getPlayer()))) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + "§4This is not your Slotchest, so you can't break it!");
			event.setCancelled(true);
			return;
		}
		if(slotChest.lager.size() >= 1) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + "§4Before you can break your Slotchest, you have to clear it's inventory!");
			event.setCancelled(true);
			return;
		}
		
		slotChests.remove(slotChest.getLocation());
		event.getPlayer().sendMessage(CasinoManager.getPrefix() + "You successfully removed your Slotchest!");
		reload();
		
	}
	//-------------------------------------------------------------------------------
	//EventActions
	
	private void clickOnChest(PlayerInteractEvent event) {
		SlotChest slotChest = slotChests.get(event.getClickedBlock().getLocation());
		if(!(slotChest.enabled)) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + "§4This Slotchest is currently disabled!");
			event.setCancelled(true);
			return;
		}
		if(slotChest.maintenance) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + "§4The owner is currently editing this Slotchest!");
			event.setCancelled(true);
			return;
		}
	
		//event.getPlayer().sendMessage("opening animation menu!");
		startAnimation(event.getPlayer(), slotChests.get(event.getClickedBlock().getLocation()));
		event.setCancelled(true);
		return;
	}
	
	private void startAnimation(Player player, SlotChest slotchest) {
		main.getServer().getScheduler().scheduleSyncDelayedTask(main, new RollAnimationManager(player, slotchest, main), 0L);
	}
	
	private void openWarehouseDirectly(PlayerInteractEvent event) {
		SlotChest chest = slotChests.get(event.getClickedBlock().getLocation());
		new WarehouseMenu(main, chest, event.getPlayer());
	}
	private void openOwnerInterface(PlayerInteractEvent event) {
		SlotChest chest = slotChests.get(event.getClickedBlock().getLocation());
		new OwnerInterfaceInventory(event.getPlayer(), main, chest);
	}
	
	private static int getAmountForPlayer(Player player) {
		int returnValue = 0;
		for(Entry<Location, SlotChest> entry : slotChests.entrySet()) {
			if(entry.getValue().getOwner().equals(player))
				returnValue++;
		}
		return returnValue;
	}
	public static void createSlotChest(Location lrc, Player owner) {
		if(slotChests.containsKey(lrc)) {
			owner.sendMessage(CasinoManager.getPrefix() + "§4This is a SlotChest");
			return;
		}
		if(getAmountForPlayer(owner) >= configMaxAmount) {
			
			if(!(owner.isOp() && configOpUnlimited)) {
				owner.sendMessage(CasinoManager.getPrefix() + "§4You exceed the limit of " + configMaxAmount + " SlotChests per Player!");
				return;
			}
		}
		slotChests.put(lrc, new SlotChest(owner, lrc));
		try {
			CasinoManager.slotChestManager.exportChests();
			owner.sendMessage(CasinoManager.getPrefix() + "You successfully created your own Slotchest!");
			for(int i = 0; i < 50; i++)
				lrc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, lrc, 5, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			owner.sendMessage(CasinoManager.getPrefix() + "§4An error occured while trying to create your Slotchest! Please contact the server administrator!");
		}
		
	}
}
