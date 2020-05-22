package scripts;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chrisimi.casino.main.Main;
import com.chrisimi.casino.main.MessageManager;


public class CasinoAnimation {

	public static int rollCount = 0;
	
	
	private Main main;
	public int einsatz;
	private Inventory inv;
	private Player player;
	private int period;
	
	private static HashMap<Player, Integer> tasksList = new HashMap<Player, Integer>();
	private static HashMap<Player, CasinoAnimation> guiList = new HashMap<Player, CasinoAnimation>();
	
	private Material block1;
	private double block1Multiplicator;
	private double block1Chance;
	
	private Material block2;
	private double block2Multiplicator;
	private double block2Chance;
	
	private Material block3;
	private double block3Multiplicator;
	private double block3Chance;
	
	private Material inventoryMaterial;
	public CasinoAnimation(Main main, Player player, int einsatz) {
		
		this.player = player;
		this.main = main;
		this.einsatz = einsatz;
		inv = Bukkit.createInventory(player, 5*9);
		
		
		period = (int) UpdateManager.getValue("animation-animation-cooldown");
		
		block1 = Enum.valueOf(Material.class, (String) UpdateManager.getValue("animation-block1-Type"));
		block1Multiplicator = Double.parseDouble(UpdateManager.getValue("animation-block1-Multiplicator").toString());
		block1Chance = Double.parseDouble(UpdateManager.getValue("animation-block1-Chance").toString());
		
		block2 = Enum.valueOf(Material.class, (String) UpdateManager.getValue("animation-block2-Type"));
		block2Multiplicator = Double.parseDouble(UpdateManager.getValue("animation-block2-Multiplicator").toString());
		block2Chance = Double.parseDouble(UpdateManager.getValue("animation-block2-Chance").toString());
		
		block3 = Enum.valueOf(Material.class, (String) UpdateManager.getValue("animation-block3-Type"));
		block3Multiplicator = Double.parseDouble(UpdateManager.getValue("animation-block3-Multiplicator").toString());
		block3Chance = Double.parseDouble(UpdateManager.getValue("animation-block3-Chance").toString());
		
		if(!(block1Chance+block2Chance+block3Chance == 100)) {
			CasinoManager.LogWithColor(ChatColor.RED + "blockchanceexception: the value of all 3 values isn't 100! (" + (block1Chance+block2Chance+block3Chance) + ")");
		}
		
		inventoryMaterial = Enum.valueOf(Material.class, (String) UpdateManager.getValue("animation-inventoryMaterial"));
		
		createInventory();
		guiList.put(player, this);
	}
	private void createInventory() {
		for(int i = 0; i < 45; i++) {
				ItemStack material = new ItemStack(inventoryMaterial, 1);
				inv.setItem(i, material);
		}
		//23
		ItemStack material = new ItemStack(Material.STONE_BUTTON, 1);
		ItemMeta meta = material.getItemMeta();
		meta.setDisplayName("§2Press Button to start!");
		
		material.setItemMeta(meta);
		inv.setItem(22, material);
		player.openInventory(inv);
		
		meta = material.getItemMeta();
		meta.setDisplayName("§2Back To Menu");
		material.setItemMeta(meta);
		inv.setItem(36, material);
		
	}
	public void startRoll() { //get called when the slot start
		rollCount++;
		
		Main.econ.withdrawPlayer(player, einsatz);
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("casinogui-player_bet_message").replace("%amount%", Main.econ.format(einsatz)));
		
		
		placeRandomizeBlocks();
		inv.setItem(20, new ItemStack(Material.BIRCH_SIGN));
		inv.setItem(24, new ItemStack(Material.BIRCH_SIGN));
		inv.setItem(44, new ItemStack(inventoryMaterial));
		
		
		final int minDuration = (int) UpdateManager.getValue("animation-min-duration");
		if(minDuration <= 0) {
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get min-duration: min-duration is lower or equal 0 !!!");
			Bukkit.getPluginManager().disablePlugin(main);
		}
		final int maxDuration = (int) UpdateManager.getValue("animation-max-duration");
		if(maxDuration < minDuration) {
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get max-duration: max-duration is lower than min-duration!!!");
			Bukkit.getPluginManager().disablePlugin(main);
		}
		
		int value = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
			
			
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
	
}
