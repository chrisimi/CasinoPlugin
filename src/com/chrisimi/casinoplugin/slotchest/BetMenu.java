package com.chrisimi.casinoplugin.slotchest;

import java.util.HashMap;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;

import javax.swing.text.GapContent;

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

public class BetMenu implements Listener{

	//TODO: 2 modis fia bet amoi prozentual und amoi mid fixen werten!
	public static HashMap<Player, BetMenu> waitingForChatInputTasks = new HashMap<>();
	
	public Inventory inventory;
	
	private ItemStack inputsign;
	private ItemStack backButton;
	private ItemStack currentBetsign;
	
	private ItemStack minusBet1;
	private ItemStack minusBet2;
	private ItemStack minusBet3;
	private ItemStack minusBet4;
	private ItemStack plusBet1;
	private ItemStack plusBet2;
	private ItemStack plusBet3;
	private ItemStack plusBet4;
	
	private double[] minusBetValues = new double[4];
	private double[] plusBetValues = new double[4];
	
	private Material minusBlockMaterial = Material.RED_WOOL;
	private Material plusBlockMaterial = Material.GREEN_WOOL;
	
	private final Main main;
	private final Player player;
	private final SlotChest slotChest;
	private final OwnerInterfaceInventory ownerInterfaceInventory;
	public BetMenu(Main main, Player owner, SlotChest slotchest, OwnerInterfaceInventory oii) {
		this.main = main;
		this.player = owner;
		this.slotChest = slotchest;
		this.ownerInterfaceInventory = oii;
		main.getServer().getPluginManager().registerEvents(this, main);
		initialize();
	}
	private void initialize() {
		minusBet1 = new ItemStack(minusBlockMaterial);
		minusBet2 = new ItemStack(minusBlockMaterial);
		minusBet3 = new ItemStack(minusBlockMaterial);
		minusBet4 = new ItemStack(minusBlockMaterial);
		plusBet1 = new ItemStack(plusBlockMaterial);
		plusBet2 = new ItemStack(plusBlockMaterial);
		plusBet3 = new ItemStack(plusBlockMaterial);
		plusBet4 = new ItemStack(plusBlockMaterial);
		
		inventory = Bukkit.createInventory(player, 9*3, "setup your bet!");
		
		inputsign = new ItemStack(Material.SIGN);
		ItemMeta meta = inputsign.getItemMeta();
		meta.setDisplayName("Write bet in chat");
		inputsign.setItemMeta(meta);
		inventory.setItem(22, inputsign);
		
		backButton = new ItemStack(Material.STONE_BUTTON);
		meta = backButton.getItemMeta();
		meta.setDisplayName("§bBack");
		backButton.setItemMeta(meta);
		inventory.setItem(18, backButton);
		
		currentBetsign = new ItemStack(Material.SIGN);
		meta = currentBetsign.getItemMeta();
		meta.setDisplayName("§6Current bet: " + Main.econ.format(slotChest.bet));
		currentBetsign.setItemMeta(meta);
		inventory.setItem(4, currentBetsign);
		//TODO
		
		player.openInventory(inventory);
		
		managePlusMinusBlocks();
	}
	public void updateInventory() {
		currentBetsign = new ItemStack(Material.SIGN);
		ItemMeta meta = currentBetsign.getItemMeta();
		meta.setDisplayName("§6current bet: " + Main.econ.format(slotChest.bet));
		currentBetsign.setItemMeta(meta);
		inventory.setItem(4, currentBetsign);
		managePlusMinusBlocks();
		
		CasinoManager.slotChestManager.save();
	}
	private void managePlusMinusBlocks() {
		double playerbalance = Main.econ.getBalance(player) - slotChest.bet;
		
		/* Slot: 1	2	3	4	5	6	7	8	9
		 * 		 m4	m3	m2	m1	sign p1	p2	p3	p4 
		 *  m4>m1 p1<p4
		 */
		for(int i = 0; i < 9; i++) {
			if(i == 4) continue;
			inventory.setItem(i, new ItemStack(Material.AIR));
		}
		
		if(slotChest.bet > 0) {
			createNewBlock(minusBet4, slotChest.bet);
			minusBetValues[0] = slotChest.bet;
			createNewBlock(minusBet3, slotChest.bet/2);
			minusBetValues[1] = slotChest.bet/2;
			createNewBlock(minusBet2, slotChest.bet/5);
			minusBetValues[2] = slotChest.bet/5;
			createNewBlock(minusBet1, slotChest.bet/20);
			minusBetValues[3] = slotChest.bet/20;
			
			inventory.setItem(0, minusBet4);
			inventory.setItem(1, minusBet3);
			inventory.setItem(2, minusBet2);
			inventory.setItem(3, minusBet1);
		}
		
		if(playerbalance >= slotChest.bet) {
			createNewBlock(plusBet4, playerbalance);
			plusBetValues[0] = playerbalance;
			createNewBlock(plusBet3, playerbalance/2);
			plusBetValues[1] = playerbalance/2.0;
			createNewBlock(plusBet2, playerbalance/5);
			plusBetValues[2] = playerbalance/5.0;
			createNewBlock(plusBet1, playerbalance/20);
			plusBetValues[3] = playerbalance/20.0;
			
			
			inventory.setItem(5, plusBet1);
			inventory.setItem(6, plusBet2);
			inventory.setItem(7, plusBet3);
			inventory.setItem(8, plusBet4);
		}
		//TODO bl§cke wieder verschwinden
	}
	private void createNewBlock(ItemStack item, double bet) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(bet>=1 ? String.format("§2Increase your bet by §6%s", Main.econ.format(bet)) : String.format("§4Decrease your bet by §6%s", Main.econ.format(bet)));
		item.setItemMeta(meta);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getInventory() == null) return;
		if(event.getCurrentItem() == null) return;
		
		if(!(event.getInventory().equals(inventory))) return;
		
		if(event.getCurrentItem().equals(backButton)) goBack();
		else if(event.getCurrentItem().equals(inputsign)) inputWithChat();
		else if(event.getCurrentItem().getType().equals(minusBlockMaterial)) clickedOnMinusBlock(event);
		else if(event.getCurrentItem().getType().equals(plusBlockMaterial)) clickedOnPlusBlock(event);
		
		event.setCancelled(true);
		updateInventory();
	}
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		if(!(waitingForChatInputTasks.containsKey(event.getPlayer()))) return;
		
		if(event.isAsynchronous()) {
			main.getServer().getScheduler().runTask(main, new Runnable() {
				
				@Override
				public void run() {
					input(event.getMessage());
				}
			});
		} else {
			input(event.getMessage());
		}
		event.setCancelled(true);
	}
	
	private void goBack() {
		ownerInterfaceInventory.openInventory();
	}
	private void inputWithChat() {
		waitingForChatInputTasks.put(player, this);
		player.sendMessage("\n\n"+CasinoManager.getPrefix() + MessageManager.get("slotchest-bet_message"));
		main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
			
			
			@Override
			public void run() {
				if(waitingForChatInputTasks.containsKey(player)) {
					player.sendMessage(CasinoManager.getPrefix() + "§4Time for writing the input expired! Try it again!");
					waitingForChatInputTasks.remove(player);
				}
				
			}
		}, 20*120);
		
		player.closeInventory();
	}
	
	private void clickedOnMinusBlock(InventoryClickEvent event) {
		ItemStack clickedOn = event.getCurrentItem();
		ItemStack[] blocks = new ItemStack[] {minusBet4, minusBet3, minusBet2, minusBet1};
		for(int i = 0; i < 4; i++) {
			if(clickedOn.equals(blocks[i])) {
				slotChest.bet -= minusBetValues[i];
//				player.sendMessage(CasinoManager.getPrefix() + "You decreased your bet by " + Main.econ.format(minusBetValues[i]));
				player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-bet_decrease").replace("%amount%", Main.econ.format(minusBetValues[i])));
				return;
			}
		}
	}
	private void clickedOnPlusBlock(InventoryClickEvent event) {
		ItemStack clickedOn = event.getCurrentItem();
		ItemStack[] blocks = new ItemStack[] {plusBet4, plusBet3, plusBet2, plusBet1};
		for(int i = 0; i < 4; i++) {
			if(clickedOn.equals(blocks[i])) {
				slotChest.bet += plusBetValues[i];
//				player.sendMessage(CasinoManager.getPrefix() + "You increased your bet by " + Main.econ.format(plusBetValues[i]));
				player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-bet_increase").replace("%amount%", Main.econ.format(plusBetValues[i])));
				CasinoManager.Debug(this.getClass(), "bet: " + plusBetValues[i]);
				return;
			}
		}
	}
	private void input(String message) {
		if(!(waitingForChatInputTasks.containsKey(player))) return;
		Double input = null;
		try {
			input = Double.parseDouble(message);
		} catch (NumberFormatException e) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-bet_input_invalid"));
			return;
		}
		if(input <= 0) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-bet_input_lower_than_0"));
			return;
		}
		
//		player.sendMessage(CasinoManager.getPrefix() + "§6" + Main.econ.format(input) + " is the new bet!");
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-bet_new_bet_message").replace("%amount%", Main.econ.format(input)));
		waitingForChatInputTasks.remove(player);
		slotChest.bet = input;
		slotChest.save();
		this.updateInventory();
		player.openInventory(inventory);
	}
}
