package com.chrisimi.casinoplugin.scripts;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.ClickEvent;
import com.chrisimi.inventoryapi.EventMethodAnnotation;
import com.chrisimi.inventoryapi.IInventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;


public class CasinoAnimation extends com.chrisimi.inventoryapi.Inventory implements IInventoryAPI
{

	public static class SlotsGUIElement
	{
		public SlotsGUIElement() {}
		public SlotsGUIElement(Material material, double winMultiplicand, double weight)
		{
			this.material = material;
			this.winMultiplicand = winMultiplicand;
			this.weight = weight;
		}

		public Material material = Material.STONE;
		public double winMultiplicand = 0.0;
		public double weight = 0.0;

		public static double getTotalWeight(List<SlotsGUIElement> elementList)
		{
			double result = 0.0;
			for(SlotsGUIElement element : elementList)
				result += element.weight;

			return result;
		}
	}

	private static ItemStack backButton = ItemAPI.createItem("§9back", Material.STONE_BUTTON);
	private static ItemStack retryButton = ItemAPI.createItem("§2retry", Material.STONE_BUTTON);
	private static ItemStack rollButton = ItemAPI.createItem("§2Press button to start!", Material.STONE_BUTTON);
	private static ItemStack winRowSigns = ItemAPI.createItem("", Material.SIGN);

	private static ItemStack fillItemStack = ItemAPI.createItem("", Material.BLUE_STAINED_GLASS_PANE);

	private static int[] rolls = new int[] {50, 120};

	//when created by a CasinoSlotsGUIManager... save the instance to go back later on
	private CasinoSlotsGUIManager casinoSlotsGUIManager;
	//when created by a PlayerSignsConfiguration (slots sign)... save the instance to get the owner etc.
	private PlayerSignsConfiguration playerSignsConfiguration;
	public CasinoAnimation(Player player, List<SlotsGUIElement> elements, CasinoSlotsGUIManager casinoSlotsGUIManager)
	{
		super(player, 5*9, Main.getInstance(), "Casino Slots GUI");
		addEvents(this);

		this.casinoSlotsGUIManager = casinoSlotsGUIManager;
		openInventory();
		updateVariables();
		
		initialize();
	}

	public CasinoAnimation(Player player, List<SlotsGUIElement> elements, PlayerSignsConfiguration playerSignsConfiguration)
	{
		this(player, elements, (CasinoSlotsGUIManager) null);
		this.playerSignsConfiguration = playerSignsConfiguration;
	}

	private void updateVariables()
	{
		try
		{
			fillItemStack = new ItemStack(Enum.valueOf(Material.class, UpdateManager.getValue("gui-fillMaterial").toString()));
		} catch(Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get inventory material: " + e.getMessage()
					+ ". Set to default value: BLUE_STAINED_GLASS_PANE");
		}

		try
		{
			List<String> values = (List<String>) UpdateManager.getValue("gui-animation");
			if(values.size() != 2) throw new Exception("There are not 2 valid values!");

			for(int i = 0; i < 2; i++)
				rolls[i] = Integer.parseInt(values.get(i));
		} catch(Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get animation rolls: " + e.getMessage()
					+ ". Set to default value: [50, 120]");
		}
	}

	private void initialize()
	{
		//only display button when the animation was called from a CasinoSlotsGUIManager instance
		if(casinoSlotsGUIManager != null)
			getInventory().setItem(36, backButton);

		getInventory().setItem(44, retryButton);
		getInventory().setItem(20, winRowSigns);
		getInventory().setItem(24, winRowSigns);
	}

	@EventMethodAnnotation
	public void onClick(ClickEvent event)
	{
		if(event.getClicked().equals(backButton)) backButton();
		else if(event.getClicked().equals(retryButton)) retryButton();
		else if(event.getClicked().equals(rollButton)) rollButton();
	}

	private void rollButton()
	{
	}

	private void retryButton()
	{
	}

	private void backButton()
	{
	}
	/*
	public void startRoll() { //get called when the slot start
		rollCount++;
		
		Main.econ.withdrawPlayer(player, einsatz);
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("casinogui-player_bet_message").replace("%amount%", Main.econ.format(einsatz)));
		
		
		placeRandomizeBlocks();
		inv.setItem(20, new ItemStack(Material.SIGN));
		inv.setItem(24, new ItemStack(Material.SIGN));
		inv.setItem(44, new ItemStack(inventoryMaterial));
		
		
		final int minDuration = (int) UpdateManager.getValue("animation-min-duration");
		if(minDuration <= 0) {
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get min-duration: min-duration is lower or equal 0 !!!");
			Bukkit.getPluginManager().disablePlugin(Main.getInstance());
		}
		final int maxDuration = (int) UpdateManager.getValue("animation-max-duration");
		if(maxDuration < minDuration) {
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get max-duration: max-duration is lower than min-duration!!!");
			Bukkit.getPluginManager().disablePlugin(Main.getInstance());
		}
		
		int value = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {
			
			
			int round = 0;
			int maxRound = (new Random().nextInt(maxDuration-minDuration)+minDuration)/period;
			
			@Override
			public void run() {
				if(round >= maxRound) {
					Bukkit.getScheduler().cancelTask(tasksList.get(player));
					ItemStack retryButton = new ItemStack(Material.OAK_BUTTON);
					ItemMeta meta = retryButton.getItemMeta();
					meta.setDisplayName("§4Retry?");
					retryButton.setItemMeta(meta);
					inv.setItem(44, retryButton);
					
					getWin();
				}
				
				
				
				HashMap<Integer, Material> inhalt = new HashMap<Integer, Material>();
				
				//fill Inhalt
				for(int zeile = 0; zeile < 3; zeile++) {
					for(int spalte = 0; spalte < 5; spalte++) {
						int slot = 3+zeile+spalte*9;
						inhalt.put(slot, inv.getItem(slot).getType());
						
					}
				}
				
				for(Entry<Integer, Material> entry : inhalt.entrySet()) {
					int slot = entry.getKey()+9;
					Material material = entry.getValue();
					
					if(slot > 42) {
						slot-=45;
						
					}
					inv.setItem(slot, new ItemStack(material));
				}
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 50, 15);
				round++;
			}
			
		}, period, period);
		tasksList.put(player, value);
		
	}
	
		

	private void getWin() {
		//SLOT 30-33
		Material[] possibilites = new Material[] {this.block1, this.block2, this.block3};
		int amountofWinning = 0;
		for(int i = 0; i < 3; i++) {
			//Bukkit.getLogger().info(inv.getItem(12).getType().toString() + " " +
				//					inv.getItem(13).getType().toString() + " " + 
					//				inv.getItem(14).getType().toString());
			if(inv.getItem(12).getType().equals(possibilites[i]) && inv.getItem(13).getType().equals(possibilites[i]) && inv.getItem(14).getType().equals(possibilites[i])) {
				
				switch(i) {
					case 0: //block type 1 won
						amountofWinning = (int) Math.round(einsatz * this.block1Multiplicator);
						player.sendMessage(CasinoManager.getPrefix()+"You have 3 " + this.block1.toString() + ", you have won: " + Main.econ.format(amountofWinning));
						Main.econ.depositPlayer(player, amountofWinning);
						return;
					case 1:
						amountofWinning = (int) Math.round(einsatz * this.block2Multiplicator);
						player.sendMessage(CasinoManager.getPrefix()+"You have 3 " + this.block2.toString() + ", you have won: " + Main.econ.format(amountofWinning));
						Main.econ.depositPlayer(player, amountofWinning);
						return;
					case 2:
						amountofWinning = (int) Math.round(einsatz * this.block3Multiplicator);
						player.sendMessage(CasinoManager.getPrefix()+"You have 3 " + this.block3.toString() + ", you have won: " + Main.econ.format(amountofWinning));
						Main.econ.depositPlayer(player, amountofWinning);
						return;
					default:
						
						break;
				}
			}
		}
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("casinogui-player_won_nothing").replace("%balance%", Main.econ.format(Main.econ.getBalance(player))));
		
	}
	private void placeRandomizeBlocks() {
		Random rnd = new Random();
		for(int j = 0; j < 5; j++) {
			for(int i = 0; i < 3; i++) {
				int chanceValue = rnd.nextInt(100);
				ItemStack material;
				
				if(chanceValue > block1Chance+block2Chance) {
					material = new ItemStack(this.block3);
				} else if(chanceValue > block1Chance) {
					material = new ItemStack(this.block2);
				} else {
					material = new ItemStack(this.block1);
				}
				
				inv.setItem(3+i+j*9, material);
			}
		}
		
	}
	
	
	
	
	
	public static void playerExit(Player player) {
		if(tasksList.containsKey(player)) {
			Bukkit.getScheduler().cancelTask(tasksList.get(player));
			//Bukkit.getLogger().info("deleted GUIAnimation of Player :" + player.getDisplayName());
		}
	}
	
	public static Inventory getInventory(Player player) {
		if(guiList.containsKey(player)) {
			return guiList.get(player).inv;
		} else {
			return null;
		}
	}
	public static CasinoAnimation getAnimationClass(Player player) {
		if(guiList.containsKey(player)) {
			return guiList.get(player);
		} else {
			return null;
		}
	}
	*/
}
