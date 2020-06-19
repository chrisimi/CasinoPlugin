package com.chrisimi.casinoplugin.listeners;

import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.chrisimi.casinoplugin.animations.BlackjackAnimation;
import com.chrisimi.casinoplugin.hologramsystem.HologramMenu;
import com.chrisimi.casinoplugin.hologramsystem.HologramSystem;
import com.chrisimi.casinoplugin.hologramsystem.LBHologram;
import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoGUI;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.LeaderboardsignsManager;
import com.chrisimi.casinoplugin.scripts.PlayerSignsManager;
import com.chrisimi.casinoplugin.scripts.RollCommand;
import com.chrisimi.casinoplugin.scripts.UpdateManager;
import com.chrisimi.casinoplugin.serializables.Leaderboardsign;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import com.chrisimi.casinoplugin.serializables.Leaderboardsign.Mode;
import com.chrisimi.casinoplugin.slotchest.SlotChest;
import com.chrisimi.casinoplugin.slotchest.SlotChestsManager;

public class CasinoCommandsListener implements Listener, CommandExecutor {

	/* casino.hologram.create - to create a hologram (normal)
	 * casino.hologram.server - create and manage server holograms
	 * 
	 */
	
	
	private Main main;
	
	public CasinoCommandsListener(Main main) {
		this.main = main;
		Bukkit.getServer().getPluginCommand("casino").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-sender-not_player"));
			return false;
		}
		
		Player player = (Player)sender;
		
		if(args.length == 0) {
			showHelpToPlayer(player);
		} else if(args.length == 1) {
			if(args[0].equalsIgnoreCase("updateconfig")) {
				if(Main.perm.has(player, "casino.admin") || player.isOp()) {
					UpdateManager.updateConfigYml(main);
					player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-admin_successfully_updated_config"));
				} else {
					player.sendMessage(CasinoManager.getPrefix()+ MessageManager.get("commands-player_no_permission"));
				} 
		
			} else if(args[0].equalsIgnoreCase("reloadconfig")) {
				if(Main.perm.has(player, "casino.admin") || player.isOp()) {
					
					UpdateManager.reloadConfig();
					CasinoManager.reload();
					player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-admin_successfully_reloaded_config"));
				} else {
					player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
				}
				
			} else if(args[0].equalsIgnoreCase("help")) {
				showHelpToPlayer(player);
			}
			else if(args[0].equalsIgnoreCase("admin") && Main.perm.has(sender, "casino.admin")) {
				showHelpToAdmin(player);
			} else if(args[0].equalsIgnoreCase("createchest")) {
				if(!Main.perm.has(sender, "casino.slotchest.create")) {
					player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
					
				} else
				createSlotChest(player);
			} else if(args[0].equalsIgnoreCase("save")) {
				CasinoManager.slotChestManager.save();
			} else if(args[0].equalsIgnoreCase("chestlocations")) {
				showChestLocations(player);
			} else if(args[0].equalsIgnoreCase("resetdata")) {
				resetData(player);
			} else if(args[0].equalsIgnoreCase("reloaddata")) {
				reloadData(player);
			} else if(args[0].equalsIgnoreCase("test"))
			{
				player.sendMessage(MessageManager.get("test-message"));
			} else if(args[0].equalsIgnoreCase("reloadmessages"))
			{
				reloadMessages(player);
			} else if(args[0].equalsIgnoreCase("gui"))
			{
				openCasinoGui((Player) sender);
			} else if(args[0].equalsIgnoreCase("resetsign"))
			{
				resetSign((Player)player);
			} else if(args[0].equalsIgnoreCase("deletereset"))
			{
				deletereset((Player)player);
			} else if(args[0].equalsIgnoreCase("createhologram"))
			{
				createHologram((Player)player);
			} else if(args[0].equalsIgnoreCase("holograms"))
			{
				showHolograms((Player)player);
			}
		} else if(args.length == 2) {
			if(args[0].equalsIgnoreCase("help") && args[1].equalsIgnoreCase("dice")) {

				showDiceHelpToPlayer(player);
			} else if(args[0].equalsIgnoreCase("help") && args[1].equalsIgnoreCase("blackjack")) {
				showBlackjackHelpToPlayer(player);
			} else if(args[0].equalsIgnoreCase("help") && args[1].equalsIgnoreCase("leaderboard")) {
				showLeaderboardSignHelpToPlayer(player);
			} else if(args[0].equalsIgnoreCase("help") && args[1].equalsIgnoreCase("slots")) {
				showSlotsSignHelpToPlayer(player);
			}
			
			else if(args[0].equalsIgnoreCase("sign") && args[1].equalsIgnoreCase("enable")) {
				enablePlayerManagedSign(player);
			} else if(args[0].equalsIgnoreCase("sign") && args[1].equalsIgnoreCase("disable")) {
				disablePlayerManagedSign(player);
			} else if(args[0].equalsIgnoreCase("resetleaderboard"))
			{
				resetLeaderboard(player, args[1], "", false);
			} else if(args[0].equalsIgnoreCase("resetserverleaderboard"))
			{
				resetLeaderboard(player, args[1], "", true);
			} else if(args[0].equalsIgnoreCase("setdate"))
			{
				setdate((Player) player, args[1]);
			} else if(args[0].equalsIgnoreCase("edithologram"))
			{
				editHologram((Player) player, args[1]);
			}
		} else if(args.length == 3) {
			if(args[0].equalsIgnoreCase("roll")) {
				if(Main.perm.has(player, "casino.roll")) {
					rollCommand(player, args);
				} else {
					player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
				}
			} else if(args[0].equalsIgnoreCase("resetleaderboard"))
			{
				resetLeaderboard(player, args[1], args[2], false);
			} else if(args[0].equalsIgnoreCase("resetserverleaderboard"))
			{
				resetLeaderboard(player, args[1], args[2], true);
			} else if(args[0].equalsIgnoreCase("setdate"))
			{
				setdate((Player) player, args[1] + " " + args[2]);
			}
		} else if(args.length == 4) {
			if(args[0].equalsIgnoreCase("roll")) {
				if(Main.perm.has(player, "casino.roll")) {
					rollCommand(player, args);
				} else {
					player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
				}
			}
		}
		return true;
	}
	
	
	



	private void showHolograms(Player player)
	{
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-holograms-overview"));
		ArrayList<LBHologram> holograms = HologramSystem.getHologramsFromPlayer(player);
		for(LBHologram holo : holograms)
		{
			player.sendMessage("§a " + holo.hologramName + " | " + holo.getLocation().getWorld().getName() + " | " + holo.getLocation().getBlockX() + " | " + holo.getLocation().getBlockY() + " | " + holo.getLocation().getBlockZ());
		}
	}

	private void editHologram(Player player, String string)
	{
		if(!(Main.perm.has(player, "casino.admin") || Main.perm.has(player, "casino.hologram.server") || Main.perm.has(player,  "casino.hologram.create")))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
			return;
		}
		
		
		LBHologram holo = HologramSystem.getHologramByName(string);
		if(holo == null)
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-invalid_hologramname"));
			return;
		}
		
		if(holo.isServerHologram() && !(Main.perm.has(player, "casino.admin") || Main.perm.has(player, "casino.hologram.server")))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
			return;
		}
		
		if(!holo.isServerHologram() && !(holo.getOwner().getUniqueId().equals(player.getUniqueId())) && !(Main.perm.has(player, "casino.admin")))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
			return;
		}
		
		new HologramMenu(player, holo);
	}

	private void createHologram(Player player)
	{
		if(!(Main.perm.has(player, "casino.admin") || Main.perm.has(player, "casino.hologram.server") || Main.perm.has(player,  "casino.hologram.create")))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
			return;
		}
		new HologramMenu(player);
	}

	private void setdate(Player player, String string)
	{
		DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm");
		DateFormat adf = new SimpleDateFormat("MM-dd-yyyy h:mm a");
		Date date = null;
		try
		{
			date = sdf.parse(string);
		} catch (ParseException e)
		{
			try
			{
				date = adf.parse(string);
			} catch (Exception e2)
			{
				player.sendMessage(MessageManager.get("commands-setdate_invalid_format"));
				return;
			}
		}
		Leaderboardsign sign = getLeaderboardsign(player);
		if(sign == null) return;
		
		if(sign.isServerSign() && !(Main.perm.has(player, "casino.admin") || Main.perm.has(player, "casino.serversigns")))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
			return;
		}
		else if(!sign.isServerSign() && !(sign.getPlayer().getUniqueId().equals(player.getUniqueId())))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
			return;
		}
		
		sign.validUntil = date.getTime();
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-setdate_successful"));
		LeaderboardsignsManager.save();
	}

	//muss noch gestestet werden
	private void deletereset(Player player)
	{
		Leaderboardsign sign = getLeaderboardsign(player);
		if(sign == null) return;
		
		if(sign.isServerSign() && (!Main.perm.has(player, "casino.admin") || !Main.perm.has(player, "casino.serversigns")))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
			return;
		}
		if(!sign.isServerSign() && !sign.getPlayer().getUniqueId().equals(player.getUniqueId()))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
			return;
		}
		
		sign.lastManualReset = 0;
		sign.validUntil = 0;
		LeaderboardsignsManager.save();
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-leaderboard_delete_successful"));
	}
	
	
	
	private void resetSign(Player player)
	{
		if(!(Main.perm.has(player, "casino.admin")))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
			return;
		}
		PlayerSignsConfiguration cnf = checkForSign(player);
		if(cnf == null) return;
		
		BlackjackAnimation.resetForSign(cnf.getLocation());
		cnf.isRunning = false;
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-sign_reset_successful"));
	}

	private void createSlotChest(Player player) {
		if(!(Main.perm.has(player, "casino.slotchest.create") || Main.perm.has(player, "casino.admin"))) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
			return;
		}
//		Block block = player.getTargetBlockExact(10);
		Block block = player.getTargetBlock(null, 10);
		if(block == null)
			return;
		
		Chest chest = null;
		try {
			chest = (Chest) block.getState();
		} catch(ClassCastException e) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-slotchest_chest_invalid"));
		}
		
		if(chest == null)
			return;
	
		if(!(chestEmpty(chest))) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-slotchest_chest_not_empty"));
			return;
		}
		SlotChestsManager.createSlotChest(chest.getLocation(), player);
		
	}
	private Boolean chestEmpty(Chest chest) {
		for(int i = 0; i < chest.getInventory().getSize(); i++) {
			if(chest.getInventory().getItem(i) != null)
				return false;
		}
		return true;
	}

	private void rollCommand(Player player, String[] args) {
		RollCommand.roll(player, args);	
	}
	
	private void enablePlayerManagedSign(Player player) {
		if(!(Main.perm.has(player, "casino.dice.create") || Main.perm.has(player, "casino.admin"))) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
			return;
		}
		
		// TODO Auto-generated method stub
		PlayerSignsConfiguration cnf = checkForSign(player);
		if(cnf == null) return; //feedback wird vorher schon ausgegeben
		
		if(cnf.isSignDisabled()) {
			cnf.enableSign();
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_enable").replace("%sign%", cnf.gamemode.toString()));
		} else {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_is_enabled"));
		}
	}

	/**
	 * check if the player is looking onto one of his player signs
	 * @param player
	 * @return {@link PlayerSignsConfiguration} of looked sign can be null
	 */
	private PlayerSignsConfiguration checkForSign(Player player) {
//		Block block = player.getTargetBlockExact(10);
		Block block = player.getTargetBlock(null, 10);
		if(block == null) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_invalid_target"));
			return null;
		}
		if(!(block.getType().toString().contains("SIGN"))) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_not_a_sign"));
			return null;
		}
		Sign signLookingAt = (Sign) block.getState();
		PlayerSignsConfiguration cnf = PlayerSignsManager.getPlayerSign(signLookingAt.getLocation());
		if(cnf == null) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_not_a_playersign"));
			return null;
		}
		if(Main.perm.has(player, "casino.admin")) {
			
		} else if(cnf.isServerOwner() || (!(cnf.getOwner().getPlayer().equals(player)))) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_not_owner"));
			return null;
		}
		
		return cnf;
		
	}

	private void disablePlayerManagedSign(Player player) {
		if(!(Main.perm.has(player, "casino.dice.create") || Main.perm.has(player, "casino.admin"))) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
			return;
		}
		PlayerSignsConfiguration cnf = checkForSign(player);
		if(cnf == null) return;
		
		if(cnf.isSignDisabled()) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_is_disabled"));
		} else {
			cnf.disableSign();
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_disable").replace("%sign%", cnf.gamemode.toString()));
		}
		
	}

	private void showDiceHelpToPlayer(Player player) {
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("§f§l§nDice help");
		if(Main.perm.has(player, "casino.dice.create")) player.sendMessage("§2permissions: §4true");
		else player.sendMessage("§2permissions: §4false");
		
		player.sendMessage("§6§n§lFormat of a dice sign:");
		player.sendMessage("");
		player.sendMessage("     §6line 1: §ecasino §6(§ecasino;server §6for using it as a server dice sign)");
		player.sendMessage("     §6line 2: §edice");
		player.sendMessage("     §6line 3: §ebet §6like 30 or 20.5");
		player.sendMessage("     §6line 4: §ewin chance §6and §emultiplicator §6like 1-40;3 (the player wins if he draws between 1-40 and get bet*3)");
	}
	private void showBlackjackHelpToPlayer(Player player) {
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("§f§l§lBlackjack help");
		if(Main.perm.has(player, "casino.blackjack.create")) player.sendMessage("§2permissions: §4true");
		else player.sendMessage("§2permissions: §4false");
		
		player.sendMessage("§6§n§lFormat of a blackjack sign:");
		player.sendMessage("");
		player.sendMessage("     §6line 1: §ecasino §6(§ecasino;server §6for using it as a server blackjack sign)");
		player.sendMessage("     §6line 2: §eblackjack");
		player.sendMessage("     §6line 3: §eminbet§6;§emaxbet §6like 20;30");
		player.sendMessage("     §6line 4: §emultiplicator §6if players draws a blackjack (21) in §eto writing §6like 3 to 2");
	}
	private void showLeaderboardSignHelpToPlayer(Player player)
	{
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("§f§lLeaderboardsign help");
		if(Main.perm.has(player, "casino.leaderboardsign.create")) player.sendMessage("§2permissions: §4true");
		else player.sendMessage("§2permissions: §4false");
		
		player.sendMessage("");
		player.sendMessage("§6§n§lFormat of a leaderboardsign:");
		player.sendMessage("");
		player.sendMessage("     §6line 1: §eleaderboard §6(§eleaderboard;s §6for using it as a server leaderboardsign§6) ");
		player.sendMessage("     §6line 2: §eposition§6;§ecycle §eposition §6like 1 for first place, §ecycle §6is optional like month data will be only taken from this month, (§eyear, month, week, day, hour§6)");
		player.sendMessage("     §6line 3: §emode §6(§ecount, sumamount, highestamount, highestloss, sumloss§6)");
		player.sendMessage("     §6line 4: §erange §6(§eall §6for all your signs, §enumber of blocks §6(3 as example) for using signs in this block range");
	}
	private void showSlotsSignHelpToPlayer(Player player)
	{
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("§f§lCasino-Slots sign help");

		
		player.sendMessage("");
		player.sendMessage("§6§n§lFormat of a Casino-Slots sign:");
		player.sendMessage("");
		player.sendMessage("    §6line 1: §eslots §6or §eslots;server §6for server sign");
		player.sendMessage("    §6line 2: §ebet §6in decimal like §e10.0");
		player.sendMessage("    §6line 3: 3 symbols splited by ';' a semicolon like §eA;B;C");
		player.sendMessage("    §6line 4: §echances and multiplicators §6in that format: ");
		player.sendMessage("    §6        §echance1-chance2-chance3;multiplicator1-multiplicator2-multiplicator3 §6(1 is for A, 2 is for B and 3 is for C in that example)");
		player.sendMessage("    §6example:   50-30-20;2-3-5");
		
	}
	
	private void showHelpToAdmin(Player player) 
	{
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("§4Admin page");
		player.sendMessage("§6/casino reloadconfig §8- reloads the config.yml");
		player.sendMessage("§6/casino resetdata §8- deletes all roll-data from playermanagedsigns (data.yml)");
		player.sendMessage("§6/casino reloaddata §8- reload all leaderboard signs and data.yml. Could lag a bit!");
		player.sendMessage("§6/casino resetsign §8- reset the sign your are look onto it. Use it when the sign is bugging");
	}

	private void showHelpToPlayer(Player player) {
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage(CasinoManager.getPrefix());
		player.sendMessage("§2CasinoPlugin Version " + Main.pluginVersion + " by chrisimi");
		player.sendMessage("§6/casino gui§8- open the casino GUI");
		player.sendMessage("§6/casino admin §8- admin help command");
		player.sendMessage("");
		player.sendMessage("§6/casino help slots §8- show help for placing slots signs!");
		player.sendMessage("§6/casino help dice §8- show help for placing a dice signs!");
		player.sendMessage("§6/casino help blackjack §8- show help for placing blackjack signs!");
		player.sendMessage("§6/casino help leaderboard §8- show help for placing leaderboard signs!");
		player.sendMessage("");
		player.sendMessage("§6/casino sign disable §8- disable your own player sign while looking onto it!");
		player.sendMessage("§6/casino sign enable §8- enable your own player sign while looking onto it!");
		player.sendMessage("");
		player.sendMessage("§6/casino roll [minimum] [maximum] [player (not needed)] §8- roll a random number which will be sent to nearby players or mentioned player!");
		player.sendMessage("§6/casino createchest §8- create your own slotchest while looking on a normal chest!!! clear it's inventory before!");
		player.sendMessage("§6/casino chestlocations §8- get the locations from your SlotChests!");
		player.sendMessage("");
		player.sendMessage("§6/casino createhologram §8- open the GUI to create a hologram");
		player.sendMessage("§6/casino edithologram [name] §8- edit hologram [name]");
		player.sendMessage("§6/casino holograms §8- get the name of all your holograms and their position");
		player.sendMessage("");
		player.sendMessage("§6/casino resetleaderboard [range/all] [mode (optional)] §6- reset the leaderboard in range (blocks). (mode: sumamount, count, highestamount)");
		player.sendMessage("§6/casino resetserverleaderboard [range/all] [mode (optional)] §6- same as resetleaderboard but for serversigns!");
		player.sendMessage("§6/casino deletereset §8- delete the manual reset from a sign you are looking onto it");
		player.sendMessage("§6/casino setdate [date] §8- set a date until where the leaderboard will count data (valid date format in numeric and without pm or am: day-month-year hour:minute");
	}
	
	private void showChestLocations(Player player) {
		ArrayList<SlotChest> list = SlotChestsManager.getSlotChestsFromPlayer(player);
		if(list.size() == 0) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-slotchest_no_slotchests"));
			return;
		}
		player.sendMessage("\n\n");
		player.sendMessage(CasinoManager.getPrefix() + "§6§lYour SlotChests:");
		
		int index = 1;
		for(SlotChest chest : list) {
			player.sendMessage(String.format("§6%s: x: %s, y: %s, z: %s", index, (int)chest.getLocation().getX(), (int)chest.getLocation().getY(), (int)chest.getLocation().getZ()));
			index++;
		}
	}
	
	private void openCasinoGui(Player sender) {
		if(Main.perm.has(sender, "casino.gui") || Main.perm.has(sender, "casino.admin")) {
			new CasinoGUI(main, sender);
		} else {
			sender.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
		}

	}
	private void resetData(Player player)
	{
		if(Main.perm.has(player, "casino.admin"))
		{
			LeaderboardsignsManager.resetData();
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-admin_successfully_reset_data"));
		} else
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
		}
	}
	private void reloadData(Player player)
	{
		if(Main.perm.has(player, "casino.admin")) 
		{
			LeaderboardsignsManager.reloadData(main);
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-admin_successfully_reload_data"));
		} else 
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
		}
	}
	
	private void resetLeaderboard(Player player, String range, String mode, Boolean serverSigns)
	{
		int rangeBlocks = 0;
		Boolean allModes = false;
		Mode chosenMode = null;
		if(range.contains("all"))
			rangeBlocks = 0;
		else
		{
			try
			{
				rangeBlocks = Integer.valueOf(range);
			} catch (Exception e)
			{
				player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_reset_leaderboard_invalid"));
				return;
			}
		}
		
		if(mode == "")
			allModes = true;
		else
		{
			for(Mode mode2 : Mode.values())
			{
				if(mode2.toString().equalsIgnoreCase(mode))
					chosenMode = mode2;
			}
		}
		
		if(serverSigns && (!(Main.perm.has(player, "casino.serversigns")) || !(Main.perm.has(player, "casino.admin"))))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
			return;
		} 
		if(serverSigns)
			LeaderboardsignsManager.resetServerLeaderboard(player, rangeBlocks == 0, rangeBlocks, allModes, chosenMode);
		else
			LeaderboardsignsManager.resetLeaderboard(player, rangeBlocks == 0, rangeBlocks, allModes, chosenMode);
	}
	private void reloadMessages(Player player)
	{
		if(!(Main.perm.has(player, "casino.admin")))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
			return;
		}
		
		MessageManager.ReloadMessages();
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-reload_messages_successful"));
	}
	
	private Leaderboardsign getLeaderboardsign(Player player)
	{
//		Block block = player.getTargetBlockExact(10);
		Block block = player.getTargetBlock(null, 10);
		if(block == null) return null;
		
		if(!(block.getType().toString().contains("SIGN"))) 
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player-playersigns_not_a_sign"));
			return null;
		}
		
		Leaderboardsign leaderboardsign = LeaderboardsignsManager.getLeaderboardsign(block.getLocation());
		if(leaderboardsign == null)
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-leaderboard_not_a_lbsign"));
			return null;
		}
		return leaderboardsign;
	}
}
