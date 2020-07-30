package com.chrisimi.casinoplugin.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.Map.Entry;

import com.chrisimi.casinoplugin.menues.BlackjackCreationMenu;
import com.chrisimi.casinoplugin.menues.DiceCreationMenu;
import com.chrisimi.casinoplugin.menues.SlotsCreationMenu;
import com.chrisimi.casinoplugin.utils.Validator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.chrisimi.casinoplugin.animations.BlackjackAnimation;
import com.chrisimi.casinoplugin.animations.DiceAnimation;
import com.chrisimi.casinoplugin.animations.SlotsAnimation;
import com.chrisimi.casinoplugin.animations.signanimation.Blackjack;
import com.chrisimi.casinoplugin.animations.signanimation.Dice;
import com.chrisimi.casinoplugin.animations.signanimation.Slots;
import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.serializables.PlayerSigns;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration.GameMode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


public class PlayerSignsManager implements Listener {

	/*
	 * rewrited system:
	 * one central thread (runnable) which manages the signs and clicks...
	 * click: manager looks if sign exists and then creates the new animation
	 * when finished... manager clears everything like garbage collector
	 * only animate signs if a player is in the near (16 blocks)
	 * 
	 * TODO:_
	 * system to check if sign is running
	 */
	public static int rollCount = 0;
	/**
	 * map which contains all player signs keyd with their location
	 */
	private static HashMap<Location, PlayerSignsConfiguration> playerSigns = new HashMap<Location, PlayerSignsConfiguration>();
	
	public static HashMap<OfflinePlayer, Double> playerWonWhileOffline = new HashMap<OfflinePlayer, Double>(); 
	
	private GsonBuilder builder;
	private Gson gson;
	private Main main;
	private static Double maxBetDice = 200.0;
	private static Double maxBetBlackjack = 200.0;
	private static Double maxBetSlots = 200.0;
	
	private static int maxSignsDice = -1;
	private static int maxSignsBlackjack = -1;
	private static int maxSignsSlots = -1;
	
	private int managerUpdateCycle = 120;
	private int managerDistance = 16;
	
	private int updateTask = 0;
	public PlayerSignsManager(Main main) {
		this.main = main;
		main.getServer().getPluginManager().registerEvents(this, main);
		
		builder = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().serializeNulls();
		gson = builder.create();
		
		configureVariables();
		importSigns();
		updateSignsJson();
		
		Manager.start(this);
	}
	private void updateSignsJson() {
		main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
			@Override
			public void run() {
				exportSigns();
			}
		}, 12000, 12000);
	}
	
	public void serverClose() {
		exportSigns();
		Manager.stop();
	}
	public void reload() {
		configureVariables();
	}
	public void addOfflinePlayerWinOrLose(double amount, OfflinePlayer player) {
		
		if(playerWonWhileOffline.containsKey(player)) {
			playerWonWhileOffline.compute(player, (p, m) ->  m + amount );
		} else {
			playerWonWhileOffline.put(player, amount);
		}
	}
	
	private void configureVariables() {
		try {
			maxBetDice = Double.parseDouble(UpdateManager.getValue("dice-max-bet").toString());
		} catch(NumberFormatException nfe) {
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get dice max-bet: dice max-bet is not a valid number!");
		} finally {
			if(maxBetDice == null)
				maxBetDice = 200.0;
		}
		try {
			maxBetBlackjack = Double.parseDouble(UpdateManager.getValue("blackjack-max-bet").toString());
		} catch(NumberFormatException e) {
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get blackjack max-bet: blackjack max-bet is not a valid number!");
		} finally {
			if(maxBetBlackjack == null)
				maxBetBlackjack = 200.0;
		}
		try
		{
			maxBetSlots = Double.parseDouble(UpdateManager.getValue("slots-max-bet").toString());
		} catch (NumberFormatException e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get slots max-bet: slots max-bet is not a valid number!");
			
		} finally {
			if(maxBetSlots == null)
				maxBetSlots = 200.0;
		}
		
		try
		{
			maxSignsBlackjack = Integer.valueOf(UpdateManager.getValue("blackjack-max-signs", -1).toString());
		} catch (Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get blackjack max signs: blackjack max signs is not a valid number! Set to default value infinite");
			maxSignsBlackjack = -1;
		}
		
		try
		{
			maxSignsDice = Integer.valueOf(UpdateManager.getValue("dice-max-signs", -1).toString());
			
		} catch (Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get dice max signs: dice max signs is not a valid number! Set to default value infinite");
			maxSignsDice = -1;
		}
		
		try
		{
			maxSignsSlots = Integer.valueOf(UpdateManager.getValue("slots-max-signs", -1).toString());
		} catch (Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get slots max signs: slots max signs is not a valid number! Set to default value infinite!");
			maxSignsSlots = -1;
		}
		
		try
		{
			managerUpdateCycle = Integer.valueOf(UpdateManager.getValue("playersigns-update-cycle", 120).toString());
		} catch (Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get playersigns update cycle: is not a valid number! Set to default value: 6 seconds!");
			managerUpdateCycle = 120;
		}
		
		try
		{
			managerDistance = Integer.valueOf(UpdateManager.getValue("playersigns-distance", 16).toString());
		} catch (Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get playersigns distance: is not a valid number! Set to default value: 16 blocks");
			managerDistance = 16;
		}
	}
	
	private void importSigns() {
		String line = "";
		String jsonString = "";
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(Main.playerSignsYml));
			while((line = reader.readLine()) != null) {
				jsonString += line;
			}
			reader.close();
		} catch(IOException e) {
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to import signs: Can't get playersigns from playerSigns.json!");
			e.printStackTrace();
		}
		if(jsonString.length() < 25) { //if there are not 25 symbols, a sign can't be saved and it can be ignored
			
			if(CasinoManager.configEnableConsoleMessages)
				CasinoManager.LogWithColor(ChatColor.YELLOW + "No playersigns to import!");
			return;
		}
		ArrayList<PlayerSignsConfiguration> signs = null;
		try {
			signs = gson.fromJson(jsonString, PlayerSigns.class).playerSigns;
		} catch(JsonSyntaxException jse) {
			CasinoManager.LogWithColor(ChatColor.RED + "An Error occured while trying to import PlayerSigns from json: Invalid Json file!");
			CasinoManager.LogWithColor(ChatColor.BLUE + "2 things you can do:\n1. check the json file on your own after errors or use https://jsonlint.com \n2. SAVE! the json file with an other name and let the plugin create a new json file!");
			
			CasinoManager.LogWithColor(ChatColor.RED + "Closing Server because of an fatal error!");
			Bukkit.shutdown();
			return;
		}
		
		if(signs == null) {
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get playersigns from json file: sign is null?");
			return;
		}
		for(PlayerSignsConfiguration cnf : signs) {
			try {
				if(cnf == null) throw new NullPointerException();
				
				cnf.changeEnum();

				//check if player sign has valid values
				if(!Validator.validate(cnf))
				{
					CasinoManager.LogWithColor(ChatColor.RED + "A player sign with invalid values have been found. The sign is now disabled and can be enabled again when all values are valid");
					cnf.disabled = true;
				}
				playerSigns.put(cnf.getLocation(), cnf);

				if(cnf.plusinformations.contains("disabled")) {
					String[] values = cnf.plusinformations.split(";");
					cnf.plusinformations = values[0] + ";" + values[1];
					cnf.disabled = true;
				} else if(cnf.disabled == null) cnf.disabled = false;
				
			} catch(NullPointerException npe) {
				CasinoManager.LogWithColor(ChatColor.RED + "Found a damaged PlayerSign in json file! Data will be deleted! Code: NullPointerException");
			} catch(Exception e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Found a damaged PlayerSign in json file! Data will be deleted! Code: Unknown");
				
			}
		}
		
		//go through all signs and check if a sign exists on the location
		//if not add it to the map and don't import it = remove on the next export
		
		Map<Location, PlayerSignsConfiguration> signsToDelete = new HashMap<Location, PlayerSignsConfiguration>();
		for(Entry<Location, PlayerSignsConfiguration> entry : playerSigns.entrySet()) {
			if(!(Bukkit.getWorld(entry.getValue().worldname).getBlockAt(entry.getKey()).getState() instanceof Sign)) 
			{
				CasinoManager.LogWithColor(ChatColor.RED + "1 Sign is not valid: " + entry.getKey().toString());
				signsToDelete.put(entry.getKey(), entry.getValue());
			}	
		}
		
		
		//remove the listed maps from the main list
		for(Entry<Location, PlayerSignsConfiguration> entry : signsToDelete.entrySet()) {
			playerSigns.remove(entry.getKey());
		}
		if(signsToDelete.size() > 1)
			exportSigns();
		if(CasinoManager.configEnableConsoleMessages)
			CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully imported " + playerSigns.size() + " playersigns from playersigns.json");
	}
	public void exportSigns() {
		BufferedWriter writer;
		PlayerSigns signs = new PlayerSigns();
		
		for(Entry<Location, PlayerSignsConfiguration> entry : playerSigns.entrySet()) {
			signs.playerSigns.add(entry.getValue());
		}
		
		try {
			writer = new BufferedWriter(new FileWriter(Main.playerSignsYml));
			writer.write("");
			writer.write(gson.toJson(signs, PlayerSigns.class));
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		if(CasinoManager.configEnableConsoleMessages)
			CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully exported " + signs.playerSigns.size() + " playersigns to playersigns.json");
	}
	
	@EventHandler
	public void onSignsPlace(SignChangeEvent event) {
		
		/*
		 * 0: casino prefix
		 * 1: dice
		 * 2: bet
		 * 3: win chances: 1-100 or 60-100
		 */
		
		String[] lines = event.getLines();
		if(!(lines[0].contains("casino") || lines[0].equalsIgnoreCase("casino") || lines[0].equalsIgnoreCase("slots") || lines[0].contains("slots"))) return;

		if(lines[0].contains("casino") && lines[1].contains("dice"))
		{
			new DiceCreationMenu(event.getBlock().getLocation(), event.getPlayer());
			return;
		} else if(lines[0].contains("casino") && lines[1].contains("blackjack"))
		{
			new BlackjackCreationMenu(event.getBlock().getLocation(), event.getPlayer());
			return;
		} else if(lines[0].contains("casino") && lines[1].contains("slots"))
		{
			new SlotsCreationMenu(event.getBlock().getLocation(), event.getPlayer());
			return;
		}


		if(lines[1].length() == 0) return;
		if(lines[2].length() == 0) return;
		if(lines[3].length() == 0) return;
		
		if(lines[1].contains("dice") || lines[1].equalsIgnoreCase("dice")) {
			if(!(Main.perm.has(event.getPlayer(), "casino.dice.create") || Main.perm.has(event.getPlayer(), "casino.admin") || Main.perm.has(event.getPlayer(), "casino.serversigns"))) {
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-creating-dicesign"));
				return;
			}
			createDiceSign(event);
		} else if(lines[1].contains("blackjack") || lines[1].equalsIgnoreCase("blackjack")) {
			if(!(Main.perm.has(event.getPlayer(), "casino.blackjack.create") || Main.perm.has(event.getPlayer(), "casino.admin") || Main.perm.has(event.getPlayer(), "casino.serversigns"))) {
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-creating-blackjacksign"));
				return;
			}
			createBlackjackSign(event);
		} else if(lines[0].contains("slots") || lines[0].equalsIgnoreCase("slots"))
		{
			if(!(Main.perm.has(event.getPlayer(), "casino.slots.create") || Main.perm.has(event.getPlayer(), "casino.admin") || Main.perm.has(event.getPlayer(), "casino.serversigns")))
			{
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-creating-slotssign"));
				return;
			}
			createSlotsSign(event);
		}
		
	}

	@EventHandler
	public void onSignsBreak(BlockBreakEvent event) {
		if(!(event.getBlock().getType().toString().contains("SIGN"))) return;
		Sign sign = (Sign) event.getBlock().getState();
		
		if(sign.getLine(3).length() > 2) {
			
			PlayerSignsConfiguration thisSign = playerSigns.get(sign.getLocation());
			if(thisSign == null) return;
			
			if(thisSign.isRunning) {
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-sign_is_running"));
				event.setCancelled(true);
				return;
			}
			
			
			
			if(!(Main.perm.has(event.getPlayer(), "casino.admin")))
			{
				if(thisSign.isServerOwner() && !(Main.perm.has(event.getPlayer(), "casino.serversigns")))
				{
					event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-player-is_not_owner"));
					event.setCancelled(true);
					return;
				} else if(!thisSign.isServerOwner() && !(thisSign.getOwner().getUniqueId().equals(event.getPlayer().getUniqueId())))
				{
					//it a player sign and the breaker is not the owner of the sign
					event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-player-is_not_owner"));
					event.setCancelled(true);
					return;
				}
			}


			playerSigns.remove(sign.getLocation());
			exportSigns();
			
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-owner_destroyed_sign").replace("%gamemode%", thisSign.gamemode.toString()));
		}
	}
	
	@EventHandler
	public void onSignClick(PlayerInteractEvent event) {
		if(!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
		if(!(event.getClickedBlock().getType().toString().contains("SIGN"))) return;
		
		if(!(playerSigns.containsKey(event.getClickedBlock().getLocation()))) return;
		
		Player player = event.getPlayer();
		PlayerSignsConfiguration thisSign = playerSigns.get(event.getClickedBlock().getLocation());
		if(thisSign == null) return;
		
		Sign sign = (Sign) event.getClickedBlock().getState();
		if(sign == null) return;
		
		if(sign.getLine(0).contains("Dice"))
		{
			if(event.getPlayer().isSneaking() && ((!thisSign.isServerOwner() && thisSign.getOwner().getUniqueId().equals(event.getPlayer().getUniqueId())) ||
					(thisSign.isServerOwner() && (Main.perm.has(player, "casino.admin") || Main.perm.has(player,"casino.serversigns")))))
				new DiceCreationMenu(thisSign, event.getPlayer());
			else
				onDiceSignClick(sign, thisSign, player);

		}
		else if(sign.getLine(0).contains("Blackjack"))
		{
			if(event.getPlayer().isSneaking() && ((!thisSign.isServerOwner() && thisSign.getOwner().getUniqueId().equals(event.getPlayer().getUniqueId())) ||
					(thisSign.isServerOwner() && (Main.perm.has(player, "casino.admin") || Main.perm.has(player, "casino.serversigns")))))
				new BlackjackCreationMenu(thisSign, event.getPlayer());
			else
				onBlackjackSignClick(sign, thisSign, player);
		}
		else if(sign.getLine(0).contains("Slots"))
		{
			if(event.getPlayer().isSneaking() && ((!thisSign.isServerOwner() && thisSign.getOwner().getUniqueId().equals(event.getPlayer().getUniqueId())) ||
					(thisSign.isServerOwner() && (Main.perm.has(player, "casino.admin") || Main.perm.has(player, "casino.serversigns")))))
				new SlotsCreationMenu(thisSign, player);
			else
				onSlotsSignClick(sign, thisSign, player);
		}
		else
		{
			return;
		}
		
		
		rollCount++;
	}
	private void onDiceSignClick(Sign sign, PlayerSignsConfiguration thisSign, Player player) { // continue log/error methode
		if(!(Main.perm.has(player, "casino.dice.use") || Main.perm.has(player, "casino.admin"))) {
			
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-using-dicesigns"));
			return;
		}
		
		if(thisSign.isRunning) {
			
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-dice-sign_is_ingame"));
			return;
		}
		if(thisSign.isSignDisabled()) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-sign_is_disabled"));
			return;
		}
		
		if(thisSign.hasOwnerEnoughMoney() == false) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-owner_lacks_money"));
			return;
		}
		if(!(Main.econ.has(player, thisSign.bet))) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-dice-player_lacks_money"));
			return;
		}
		
		//when more gamemodes think about different sigens if(gamemode = dice)
		
		Manager.onSignClick(sign.getLocation(), player);
	}
	private void onBlackjackSignClick(Sign sign, PlayerSignsConfiguration thisSign, Player player) {
		if(!(Main.perm.has(player, "casino.blackjack.use") || Main.perm.has(player, "casino.admin"))) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-using-blackjacksigns"));
			return;
		}
		
		if(thisSign.isRunning) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-blackjack-sign_is_ingame"));
			return;
		}
		
		if(thisSign.isSignDisabled()) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-sign_is_disabled"));
			return;
		}
		if(!(thisSign.hasOwnerEnoughMoney(thisSign.blackjackGetMaxBet()*thisSign.blackjackGetMultiplicand()))) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-owner_lacks_money"));
			return;
		}
		if(!(Main.econ.has(player, thisSign.bet))) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-player_lacks_money"));
			return;
		}
		
		//main.getServer().getScheduler().cancelTask(updateTasks.get(thisSign));
		//updateTasks.remove(thisSign);
		
		//int taskNumber = main.getServer().getScheduler().runTask(main, new BlackjackAnimation(main, thisSign, player, this)).getTaskId();
		
		
		
		//signTasks.put(thisSign, taskNumber); ignored
		
		Manager.onSignClick(thisSign.getLocation(), player);
	}
	private void onSlotsSignClick(Sign sign, PlayerSignsConfiguration thisSign, Player player)
	{
		if(!(Main.perm.has(player, "casino.slots.use") || Main.perm.has(player, "casino.admin")))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-using-slotssigns"));
			return;
		}
		
		if(thisSign.isRunning)
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-slots-sign_is_ingame"));
			return;
		}
		
		if(thisSign.isSignDisabled()) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-sign_is_disabled"));
			return;
		}
		if(!(thisSign.hasOwnerEnoughMoney(thisSign.getSlotsHighestPayout()))) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-owner_lacks_money"));
			return;
		}
		if(!(Main.econ.has(player, thisSign.bet))) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-player_lacks_money"));
			return;
		}
		
		Manager.onSignClick(sign.getLocation(), player);
	}

	
	private void createDiceSign(SignChangeEvent event) {
		//bet validation
		Double bet = null;
		try {
			 bet = Double.parseDouble(event.getLine(2));
		} catch(NumberFormatException nfe) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-dice-bet_invalid"));
			event.setCancelled(true);
			return;
		}
		
		if(bet > maxBetDice) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-dice-bet_is_higher_than_max_bet").replace("%max_bet%", Main.econ.format(maxBetDice)));
			event.setCancelled(true);
			return;
		}
		
		//winmultiplicator
		if(!(event.getLine(3).contains(";") && event.getLine(3).contains("-"))) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-dice-win_chance_invalid"));
			event.setCancelled(true);
			return;
		}
		
		
		String[] informations = event.getLine(3).split(";");
		Double winmultiplicator = null;
		try {
			winmultiplicator = Double.parseDouble(informations[1]);
		} catch(NumberFormatException nfe) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-dice-winmultiplicator_invalid"));
			event.setCancelled(true);
			return;
		}
		
		//win chanes validation
		String[] values = informations[0].split("-");
		if(values.length != 2) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-dice-win_chance_format_invalid"));
			event.setCancelled(true);
			return;
		}
		Double winChanceBegin = null;
		Double winChanceEnd = null;
		try {
			winChanceBegin = Double.parseDouble(values[0]);
			winChanceEnd = Double.parseDouble(values[1]);
		} catch(NumberFormatException nfe) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-dice-win_chance_number_invalid"));
			event.setCancelled(true);
			return;
		}
		if(winChanceEnd < winChanceBegin) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-dice-win_chance_first_higher"));
			event.setCancelled(true);
			return;
		}
		if(!(winChanceBegin > 0 && winChanceBegin <= 100 || winChanceEnd > 0 && winChanceEnd <= 100)) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-dice-win_chance_not_in100"));
			event.setCancelled(true);
			return;
		}
		
		if(getAmountOfPlayerSigns(event.getPlayer(), GameMode.DICE) >= maxSignsDice && maxSignsDice != -1)
		{
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-dice-reached_max_amount"));
			event.setCancelled(true);
			return;
		}
		
		
		if(event.getLine(0).contains(";server") || event.getLine(0).contains(";s"))
		{
			
			//Server signs
			if(Main.perm.has(event.getPlayer(), "casino.serversigns") || Main.perm.has(event.getPlayer(), "casino.admin"))
			{
				PlayerSignsConfiguration newSign = new PlayerSignsConfiguration(event.getBlock().getLocation(), GameMode.Dice, bet, event.getLine(3));
				playerSigns.put(newSign.getLocation(), newSign);
				
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-dice-server-successful"));
				exportSigns();
			}
			else
			{
				System.out.println("a");
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-creating-dicesign"));
				event.setCancelled(true);
				return;
			}
		}
		else
		{
			//Player signs
			if(Main.perm.has(event.getPlayer(), "casino.dice.create") || Main.perm.has(event.getPlayer(), "casino.admin")) {
				
				PlayerSignsConfiguration newSign = new PlayerSignsConfiguration(event.getBlock().getLocation(), GameMode.Dice, event.getPlayer(), bet, event.getLine(3));
				playerSigns.put(newSign.getLocation(), newSign);
				
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-dice-successful"));
				exportSigns();
			}
			else
			{
			
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-creating-dicesign"));
				event.setCancelled(true);
				return;
			}
		}
	}
	
	private void createBlackjackSign(SignChangeEvent event) {
		
		//casino
		//blackjack
		//minEinsatz;maxEinsatz ->maxEinsatz zu plusinformations
		//gewinnMultiplicator
		Double minBet = null;
		Double maxBet = null;
		String plusinf = "";
		String plusInformations = "";
		if(event.getLine(2).contains(";")) {
			String[] values = event.getLine(2).split(";");
			if(values.length == 2) {
				try {
					minBet = Double.parseDouble(values[0]);
					maxBet = Double.parseDouble(values[1]);
				} catch(NumberFormatException e) {
					event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-blackjack-bet_invalid"));
					event.setCancelled(true);
					return;
				}
				if(maxBet < minBet) {
					event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-blackjack-max_bet_invalid"));
					event.setCancelled(true);
					return;
				}
				if(maxBet > maxBetBlackjack) {
					event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-blackjack-over_max_server_bet").replace("%max_amount_server%", Main.econ.format(maxBetBlackjack)));
					event.setCancelled(true);
					return;
				}
			} else {
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-blackjack-max_bet_missing"));
				event.setCancelled(true);
				return;
			}
		} else if(event.getLine(2).equalsIgnoreCase("-1"))
		{
			minBet = -1.0;
			maxBet = -1.0;
		} else 
		{
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-blackjack-bet_invalid_format"));
			event.setCancelled(true);
			return;
		}
		
		if(event.getLine(3).contains("to"))
		{
			String[] values = event.getLine(3).trim().split("to");
			try 
			{
				if(values.length != 2) throw new Exception("invalid winning (3 to 2)!");
				Double left = Double.parseDouble(values[0]);
				Double right = Double.parseDouble(values[1]);
				plusinf = event.getLine(3);
			} catch(Exception e)
			{
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-blackjack-invalid_winning").replace("%error%", e.getMessage()));
				event.setCancelled(true);
				return;
			}
		} else
		{
			try {
				Double winMultiplicator = Double.parseDouble(event.getLine(3));
				plusinf = winMultiplicator.toString();
			} catch(NumberFormatException e) {
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-blackjack-invalid_win_multiplicator"));
				event.setCancelled(true);
				return;
			}
		}
		
		if(getAmountOfPlayerSigns(event.getPlayer(), GameMode.BLACKJACK) >= maxSignsBlackjack && maxSignsBlackjack != -1)
		{
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-blackjack-reached_max_amount"));
			event.setCancelled(true);
			return;
		}
		
		
		if(event.getLine(0).contains(";server") || event.getLine(0).contains(";s"))
		{
			//server signs
			if(Main.perm.has(event.getPlayer(), "casino.serversigns") || Main.perm.has(event.getPlayer(), "casino.admin"))
			{
				plusInformations = maxBet.toString()+";"+plusinf;
				PlayerSignsConfiguration newSign = new PlayerSignsConfiguration(event.getBlock().getLocation(), GameMode.Blackjack, minBet, plusInformations);
				playerSigns.put(newSign.getLocation(), newSign);
				
				
				
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-blackjack-server-successful"));
			
				
				exportSigns();
			}
			else
			{
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-creating-blackjacksign"));
				event.setCancelled(true);
				return;
			}
		}
		else
		{
			//player signs
			if(Main.perm.has(event.getPlayer(), "casino.blackjack.create") || Main.perm.has(event.getPlayer(), "casino.admin"))
			{
				plusInformations = maxBet.toString()+";"+plusinf;
				PlayerSignsConfiguration newSign = new PlayerSignsConfiguration(event.getBlock().getLocation(), GameMode.Blackjack, event.getPlayer(), minBet, plusInformations);
				playerSigns.put(newSign.getLocation(), newSign);
				
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-blackjack-successful"));
				
				exportSigns();
			}
			else 
			{
				//System.out.println(Main.perm.has(event.getPlayer(), "casino.blackjack.create") + " " + Main.perm.has(event.getPlayer(), "casino.admin"));
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-creating-blackjacksign"));
				event.setCancelled(true);
				return;
			
			}
			
		}
	}
	private void createSlotsSign(SignChangeEvent event)
	{
		Double bet = 0.0;
		String[] symbols = new String[3];
		double[] multiplicators = new double[3];
		double[] chance = new double[3];

		try
		{
			bet = Double.parseDouble(event.getLine(1));
		} catch (Exception e)
		{
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-slots-bet_invalid"));
			event.setCancelled(true);
			return;
		}
		if(bet > maxBetSlots)
		{
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-slots-bet_higher_than_max_bet").replace("%max_bet%", Main.econ.format(maxBetSlots)));
			event.setCancelled(true);
			return;
		}
		
		symbols = event.getLine(2).split(";");
		if(symbols.length != 3)
		{
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-slots-not-enough-symbols"));
			event.setCancelled(true);
			return;
		}
		
		String[] values = event.getLine(3).split(";");
		if(values.length != 2)
		{
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-slots-forgot_chance_or_multiplicator"));
			event.setCancelled(true);
			return;
		}
		for(int i = 0; i < 2; i++)
		{
			String[] valueElement = values[i].split("-");
			if(valueElement.length != 3)
			{
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-slots-not_3_elements"));
				event.setCancelled(true);
				return;
			}
			try
			{
				if(i == 0)
				{
					for(int j = 0; j < 3; j++)
					{
						chance[j] = Double.parseDouble(valueElement[j]);
					}
				} else
				{
					for(int j = 0; j < 3; j++)
					{
						multiplicators[j] = Double.parseDouble(valueElement[j]);
					}
				}
			} catch (NumberFormatException e)
			{
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-slots-value_is_not_valid"));
				event.setCancelled(true);
				return;
			}
		}
		
		if(getAmountOfPlayerSigns(event.getPlayer(), GameMode.SLOTS) >= maxSignsSlots && maxSignsSlots != -1)
		{
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-slots-reached_max_amount"));
			event.setCancelled(true);
			return;
		}
		
		
		
		String plusinformations = String.format(
				"%s-%s-%s;%s-%s-%s;%s-%s-%s", symbols[0], multiplicators[0], chance[0], symbols[1], multiplicators[1], chance[1], symbols[2], multiplicators[2], chance[2]);
	
		if(event.getLine(0).contains(";server") || event.getLine(0).contains(";s"))
		{
			
			//server signs
			if(Main.perm.has(event.getPlayer(), "casino.serversigns") || Main.perm.has(event.getPlayer(), "casino.admin"))
			{
				
				PlayerSignsConfiguration newSign = new PlayerSignsConfiguration(event.getBlock().getLocation(), GameMode.Slots, bet, plusinformations);
				playerSigns.put(newSign.getLocation(), newSign);
				
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-slots-server-successful"));
				exportSigns();
			}
			else
			{
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-creating-slotssign"));
				event.setCancelled(true);
				return;
			}
		}
		else
		{
			//server signs
			if(Main.perm.has(event.getPlayer(), "casino.slots.create") || Main.perm.has(event.getPlayer(), "casino.admin"))
			{
				
				PlayerSignsConfiguration newSign = new PlayerSignsConfiguration(event.getBlock().getLocation(), GameMode.Slots, event.getPlayer(), bet, plusinformations);
				playerSigns.put(newSign.getLocation(), newSign);
				
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-creation-slots-successful"));
				
				exportSigns();	
			}
			
			else
			{
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-creating-slotssign"));
				event.setCancelled(true);
				return;
			}
		}
	}
	/**
	 * get amount of signs the player currently has
	 * @param player {@link Player} instance of player
	 * @param gameMode {@link GameMode} instance of gamemode
	 * @return count as int
	 */
	private static int getAmountOfPlayerSigns(OfflinePlayer player, GameMode gameMode)
	{
		return playerSigns.values().stream()
				.filter(a -> !a.isServerOwner() && a.getOwner().getUniqueId().equals(player.getUniqueId()) && a.gamemode == gameMode)
				.collect(Collectors.toList()).size();
	}
	
	/**
	 * get the {@link PlayerSignsConfiguration} from the sign 
	 * @param location {@link Location} location of the sign
	 * @return {@link PlayerSignsConfiguration} if location is valid otherwise null
	 */
	public static PlayerSignsConfiguration getPlayerSign(Location location) {
		if(!(playerSigns.containsKey(location))) return null;
		
		return playerSigns.get(location);
	}
	/**
	 * Get a list from locations from all signs the player owns
	 * @param player {@link OfflinePlayer} instance of player
	 * @return {@link ArrayList} which contains {@link Location} for every sign. Can't be null
	 */
	public static ArrayList<Location> getLocationsFromAllPlayerSigns(OfflinePlayer player)
	{
		ArrayList<Location> locations = new ArrayList<>();
		synchronized (playerSigns)
		{
			for(Map.Entry<Location, PlayerSignsConfiguration> entry : playerSigns.entrySet()) 
			{
				if(entry.getValue().isServerOwner()) continue;
				if(entry.getValue().getOwner().equals(player)) locations.add(entry.getKey());
			}
		}
		return locations;
	}
	/**
	 * Get a list from all locations of serversigns of the server
	 * @return {@link ArrayList} with {@link Location} elements
	 */
	public static ArrayList<Location> getLocationsFromAllServerSigns()
	{
		ArrayList<Location> locations = new ArrayList<>();
		synchronized (playerSigns)
		{
			for(Map.Entry<Location, PlayerSignsConfiguration> entry : playerSigns.entrySet())
			{
				if(entry.getValue().ownerUUID.equalsIgnoreCase("server")) locations.add(entry.getKey());
			}
		}
		return locations;
	}
	public static ArrayList<PlayerSignsConfiguration> getPlayerSigns()
	{
		return new ArrayList<>(playerSigns.values());
	}

	public static void addPlayerSign(PlayerSignsConfiguration conf)
	{
		playerSigns.put(conf.getLocation(), conf);
		CasinoManager.playerSignsManager.exportSigns();
	}

	public static double getMaxBetDice() {return maxBetDice;}
	public static double getMaxBetSlots() {return maxBetSlots;}
	public static double getMaxBetBlackjack() {return maxBetBlackjack;}
	public static boolean playerCanCreateSign(OfflinePlayer player, GameMode gamemode)
	{
		switch (gamemode)
		{
			case BLACKJACK:
				if(maxSignsBlackjack == -1) return true;
				return getAmountOfPlayerSigns(player, gamemode) <= maxSignsBlackjack;
			case DICE:
				if(maxSignsDice == -1) return true;
				return getAmountOfPlayerSigns(player, gamemode) <= maxSignsDice;
			case SLOTS:
				if(maxSignsSlots == -1) return true;
				return getAmountOfPlayerSigns(player, gamemode) <= maxSignsSlots;
		}
		return false;
	}

	private static class Manager 
	{
		private static int currentID = 0;
		
		/**
		 * click on a playersign
		 * @param lrc location of the sign
		 * @param player player who clicked on the sign
		 */
		public static void onSignClick(Location lrc, Player player)
		{
			PlayerSignsConfiguration cnf = PlayerSignsManager.getPlayerSign(lrc);
			if(cnf == null) return;
			
			cnf.isRunning = true;
			
			switch (cnf.gamemode)
			{
			case BLACKJACK:
				
					Main.getInstance().getServer().getScheduler().
					runTask(Main.getInstance(), 
							new BlackjackAnimation(Main.getInstance(), cnf, player, CasinoManager.playerSignsManager));
					
				break;
			case SLOTS:
				Main.getInstance().getServer().getScheduler()
				.runTask(Main.getInstance(), 
						new SlotsAnimation(Main.getInstance(), cnf, player, CasinoManager.playerSignsManager));
				break;
			case DICE:
				Main.getInstance().getServer().getScheduler()
				.runTask(Main.getInstance(), 
						new DiceAnimation(Main.getInstance(), cnf, player, CasinoManager.playerSignsManager));
				break;
			default:
				cnf.isRunning = false;
				break;
			}
		}
		/**
		 * start the Manager
		 */
		public static void start(PlayerSignsManager manager)
		{
			currentID = Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), task, 0, manager.managerUpdateCycle);
		}
		/**
		 * stop the Manager
		 */
		public static void stop()
		{
			Main.getInstance().getServer().getScheduler().cancelTask(currentID);
		}
		private static Runnable task = new Runnable()
		{
			
			@Override
			public void run()
			{
				for(PlayerSignsConfiguration sign : PlayerSignsManager.getPlayerSigns())
				{
					if(sign.isRunning) 
					{
						CasinoManager.Debug(this.getClass(), sign.gamemode + " is running");
						continue;
					}
					
					if(isPlayerInRange(sign.getLocation()))
					{
						switch (sign.gamemode)
						{
						case BLACKJACK:
							Main.getInstance().getServer().getScheduler()
							.runTask(Main.getInstance(), 
									new Blackjack(sign.getSign(), sign));
							break;
						case SLOTS:
							Main.getInstance().getServer().getScheduler()
							.runTask(Main.getInstance(), 
									new Slots(sign.getSign(), sign));
							break;
						case DICE:
							Main.getInstance().getServer().getScheduler()
							.runTask(Main.getInstance(), 
									new Dice(sign.getSign(), sign));
							break;
						default:
							break;
						}
					}
				}
			}
		};
		/**
		 * 
		 * @param lrc of the sign
		 * @return true if a player is in the range of 16 blocks, false if not
		 */
		private static boolean isPlayerInRange(Location lrc)
		{
			if(CasinoManager.playerSignsManager.managerDistance == -1) return true;
			
			@SuppressWarnings("unchecked")
			ArrayList<Player> players = (ArrayList<Player>) Bukkit.getOnlinePlayers().stream()
					.filter(a -> a.getWorld().equals(lrc.getWorld()))
					.collect(Collectors.toList());
			for(Player player : players)
			{
				if(player.getLocation().distance(lrc) < CasinoManager.playerSignsManager.managerDistance)
				{
					CasinoManager.Debug(PlayerSignsConfiguration.class, "player is in range! " + player.getName() + " - " + player.getLocation().distance(lrc));
					return true;
				}
			}
			return false;
		}
	}
}
