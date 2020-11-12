package com.chrisimi.casinoplugin.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

	public static int rollCount = 0;
	/**
	 * map which contains all player signs keyed with their location
	 */
	private static final HashMap<Location, PlayerSignsConfiguration> playerSigns = new HashMap<>();
	
	public static final HashMap<OfflinePlayer, Double> playerWonWhileOffline = new HashMap<>();

	private final Gson gson;
	private static Double maxBetDice = 200.0;
	private static Double maxBetBlackjack = 200.0;
	private static Double maxBetSlots = 200.0;
	
	private static int maxSignsDice = -1;
	private static int maxSignsBlackjack = -1;
	private static int maxSignsSlots = -1;
	
	private int managerUpdateCycle = 120;
	private int managerDistance = 16;

	public PlayerSignsManager()
	{
		Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());

		gson = new GsonBuilder()
				.setPrettyPrinting()
				.excludeFieldsWithoutExposeAnnotation()
				.serializeNulls()
				.create();
		
		configureVariables();
		importSigns();
		updateSignsJson();
		
		Manager.start(this);
	}
	private void updateSignsJson()
	{
		Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this::exportSigns, 12000, 12000);
	}
	
	public void serverClose() {
		exportSigns();
		Manager.stop();
	}
	public void reload() {
		configureVariables();
	}

	private void configureVariables()
	{
		maxBetDice = Double.parseDouble(UpdateManager.getValue("dice-max-bet", 200.0).toString());
		maxBetBlackjack = Double.parseDouble(UpdateManager.getValue("blackjack-max-bet", 200.0).toString());
		maxBetSlots = Double.parseDouble(UpdateManager.getValue("slots-max-bet", 200.0).toString());

		maxSignsBlackjack = Integer.parseInt(UpdateManager.getValue("blackjack-max-signs", -1).toString());
		maxSignsDice = Integer.parseInt(UpdateManager.getValue("dice-max-signs", -1).toString());
		maxSignsSlots = Integer.parseInt(UpdateManager.getValue("slots-max-signs", -1).toString());

		managerUpdateCycle = Integer.parseInt(UpdateManager.getValue("playersigns-update-cycle", 120).toString());
		managerDistance = Integer.parseInt(UpdateManager.getValue("playersigns-distance", 16).toString());
	}
	
	private void importSigns()
	{
		String line;
		StringBuilder sb = new StringBuilder();

		//read all characters from the file
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(Main.playerSignsYml));
			while((line = reader.readLine()) != null)
			{
				sb.append(line);
			}
			reader.close();
		} catch(IOException e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to import signs: can't get player signs from playerSigns.json!");
			e.printStackTrace();
		}

		//check if there are some characters, if not stop the import process
		if(sb.toString().length() < 25)
		{
			if(CasinoManager.configEnableConsoleMessages)
				CasinoManager.LogWithColor(ChatColor.YELLOW + "No playersigns to import!");

			return;
		}
		ArrayList<PlayerSignsConfiguration> signs;
		try
		{
			signs = gson.fromJson(sb.toString(), PlayerSigns.class).playerSigns;
		} catch(JsonSyntaxException jse)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "An error occurred while trying to import PlayerSigns from json: Invalid Json file!");
			CasinoManager.LogWithColor(ChatColor.BLUE + "2 things you can do:\n1. check the json file on your own after errors or use https://jsonlint.com \n2. SAVE! the json file with an other name and let the plugin create a new json file!");

			CasinoManager.LogWithColor(ChatColor.RED + "Closing Server because of an fatal error!");
			Bukkit.shutdown();
			return;
		}
		
		if(signs == null)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get player signs from json file: sign is null?");
			return;
		}

		//go through all signs and validate them
		for(PlayerSignsConfiguration cnf : signs)
		{
			try
			{
				if(cnf == null) throw new NullPointerException();

				//check if the sign has an old enum and change it to the new ones
				cnf.changeEnum();

				//check if player sign has valid values
				if(!Validator.validate(cnf))
				{
					CasinoManager.LogWithColor(ChatColor.RED + "A player sign with invalid values have been found. The sign is now disabled and can be enabled again when all values are valid");
					cnf.disabled = true;
				}
				playerSigns.put(cnf.getLocation(), cnf);

				if(cnf.plusinformations.contains("disabled"))
				{
					cnf.disabled = true;
				} else if(cnf.disabled == null)
					cnf.disabled = false;
				
			} catch(NullPointerException npe) {
				CasinoManager.LogWithColor(ChatColor.RED + "Found a damaged player sign in json file! Data will be deleted! Code: NullPointerException");
			} catch(Exception e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Found a damaged player sign in json file! Data will be deleted! Code: Unknown");
				
			}
		}
		
		//go through all signs and check if a sign exists on the location
		//if not add it to the map and don't import it = remove on the next export
		
		Map<Location, PlayerSignsConfiguration> signsToDelete = new HashMap<>();
		for(Entry<Location, PlayerSignsConfiguration> entry : playerSigns.entrySet())
		{
			if(!(Bukkit.getWorld(entry.getValue().worldname).getBlockAt(entry.getKey()).getState() instanceof Sign)) 
			{
				CasinoManager.LogWithColor(ChatColor.RED + "1 sign does not exist in the world: " + entry.getKey().toString());
				signsToDelete.put(entry.getKey(), entry.getValue());
			}	
		}
		
		
		//remove the listed maps from the main list
		for(Entry<Location, PlayerSignsConfiguration> entry : signsToDelete.entrySet())
		{
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
	public void onSignsPlace(SignChangeEvent event)
	{
		String[] lines = event.getLines();

		//check if the first line contains casino
		if(!Validator.is(lines[0], "casino")) return;

		if(Validator.is(lines[1], "dice"))
		{
			if(!PlayerSignsManager.playerCanCreateSign(event.getPlayer(), GameMode.DICE))
			{
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("reached-limit"));
				event.setCancelled(true);
				return;
			}

			new DiceCreationMenu(event.getBlock().getLocation(), event.getPlayer());
		} else if(Validator.is(lines[1], "blackjack"))
		{
			if(!PlayerSignsManager.playerCanCreateSign(event.getPlayer(), GameMode.BLACKJACK))
			{
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("reached-limit"));
				event.setCancelled(true);
				return;
			}

			new BlackjackCreationMenu(event.getBlock().getLocation(), event.getPlayer());
		} else if(Validator.is(lines[1], "slots"))
		{
			if(!PlayerSignsManager.playerCanCreateSign(event.getPlayer(), PlayerSignsConfiguration.GameMode.SLOTS))
			{
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("reached-limit"));
				event.setCancelled(true);
				return;
			}

			new SlotsCreationMenu(event.getBlock().getLocation(), event.getPlayer());
		}
	}

	@EventHandler
	public void onSignsBreak(BlockBreakEvent event)
	{
		if (!(event.getBlock().getType().toString().contains("SIGN"))) return;
		Sign sign = (Sign) event.getBlock().getState();

		PlayerSignsConfiguration thisSign = playerSigns.get(sign.getLocation());
		if (thisSign == null) return;

		//cancel break because sign is currently running
		if (thisSign.isRunning)
		{
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-sign_is_running"));
			event.setCancelled(true);
			return;
		}

		//check if player has permission to break the sign
		if (!(Main.perm.has(event.getPlayer(), "casino.admin")))
		{
			if (thisSign.isServerOwner() && !(Main.perm.has(event.getPlayer(), "casino.create.serversign")))
			{
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-player-is_not_owner"));
				event.setCancelled(true);
				return;
			} else if (!thisSign.isServerOwner() && !(thisSign.getOwner().getUniqueId().equals(event.getPlayer().getUniqueId())))
			{
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-player-is_not_owner"));
				event.setCancelled(true);
				return;
			}
		}

		//remove sign from the list
		playerSigns.remove(sign.getLocation());
		exportSigns();

		event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-owner_destroyed_sign").replace("%gamemode%", thisSign.gamemode.toString()));
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

		//check for some various options
		if(thisSign.isRunning)
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-dice-sign_is_ingame"));
			return;
		} else if(thisSign.isSignDisabled())
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-sign_is_disabled"));
			return;
		} else if(!thisSign.hasOwnerEnoughMoney())
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-owner_lacks_money"));
			return;
		} else if(!(Main.econ.has(player, thisSign.bet)))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-dice-player_lacks_money"));
			return;
		}


		//check if player is sneaking and has permission for this sign (check if it's a server sign and he has server sign permission or if it's his sign)
		if(event.getPlayer().isSneaking() &&
				((!thisSign.isServerOwner() && thisSign.getOwner().getUniqueId().equals(event.getPlayer().getUniqueId())) ||
				(thisSign.isServerOwner() && Main.perm.has(player,"casino.create.serversign"))))
		{
			switch(thisSign.gamemode)
			{
				case DICE:
				case Dice:
					new DiceCreationMenu(thisSign, event.getPlayer());
					break;
				case BLACKJACK:
				case Blackjack:
					new BlackjackCreationMenu(thisSign, event.getPlayer());
					break;
				case SLOTS:
				case Slots:
					new SlotsCreationMenu(thisSign, event.getPlayer());
			}
		}
		else
		{
			switch(thisSign.gamemode)
			{
				case DICE:
				case Dice:
					if(onDiceSignClick(sign, thisSign, player))
						Manager.onSignClick(sign.getLocation(), player);
					break;
				case BLACKJACK:
				case Blackjack:
					if(onBlackjackSignClick(sign, thisSign, player))
						Manager.onSignClick(sign.getLocation(), player);
					break;
				case SLOTS:
				case Slots:
					if(onSlotsSignClick(sign, thisSign, player))
						Manager.onSignClick(sign.getLocation(), player);
					break;
			}
		}
		rollCount++;
	}

	//check some cases for dice signs
	private boolean onDiceSignClick(Sign sign, PlayerSignsConfiguration thisSign, Player player)
	{
		if(!Main.perm.has(player, "casino.use.dice"))
		{
			
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-using-dicesigns"));
			return false;
		}

		return true;
	}

	//check some cases for blackjack signs
	private boolean onBlackjackSignClick(Sign sign, PlayerSignsConfiguration thisSign, Player player)
	{
		if(!Main.perm.has(player, "casino.use.blackjack"))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-using-blackjacksigns"));
			return false;
		}

		return true;
	}

	//check some cases for slots signs
	private boolean onSlotsSignClick(Sign sign, PlayerSignsConfiguration thisSign, Player player)
	{
		if(!Main.perm.has(player, "casino.use.slots"))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-using-slotssigns"));
			return false;
		}

		return true;
	}


	/**
	 * get amount of signs the player currently has
	 * @param player {@link Player} instance of player
	 * @param gameMode {@link GameMode} instance of gamemode
	 * @return count as int
	 */
	public static int getAmountOfPlayerSigns(OfflinePlayer player, GameMode gameMode)
	{
		return (int) playerSigns.values().stream()
				.filter(a -> !a.isServerOwner() && a.getOwner().getUniqueId().equals(player.getUniqueId()) && a.gamemode == gameMode)
				.count();
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

	/**
	 * add a new {@link PlayerSignsConfiguration} in the system
	 * @param conf instance of the player sign
	 */
	public static void addPlayerSign(PlayerSignsConfiguration conf)
	{
		if(Validator.validate(conf))
		{
			playerSigns.put(conf.getLocation(), conf);
			CasinoManager.playerSignsManager.exportSigns();
		}

	}

	/**
	 *
	 * @return the max bet set for dice signs
	 */
	public static double getMaxBetDice()
	{
		return maxBetDice;
	}

	/**
	 *
	 * @return the max bet set for slots signs
	 */
	public static double getMaxBetSlots()
	{
		return maxBetSlots;
	}

	/**
	 *
	 * @return the max bet set for blackjack signs
	 */
	public static double getMaxBetBlackjack()
	{
		return maxBetBlackjack;
	}

	/**
	 * check if the player can create a sign from game mode without getting over the limit
	 * @param player instance of the player
	 * @param gamemode which mode should be checked
	 * @return true if the player can create a new sign of the game mode without getting over the limit
	 */
	public static boolean playerCanCreateSign(OfflinePlayer player, GameMode gamemode)
	{
		if(player.isOnline() && Main.perm.has(player.getPlayer(), "casino.unlimited")) return true;

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

	/**
	 * check if the bet is allowed on this server
	 * @param amount the amount to check
	 * @param gameMode which mode should be checked
	 * @return true if it is allowed, false if not
	 */
	public static boolean isBetAllowed(double amount, GameMode gameMode)
	{
		switch(gameMode)
		{
			case BLACKJACK:
				if(maxBetBlackjack == -1.0) return true;
				return amount < maxBetBlackjack;

			case DICE:
				if(maxBetDice == -1.0) return true;
				return amount < maxBetDice;

			case SLOTS:
				if(maxBetSlots == -1.0) return true;
				return amount < maxBetSlots;
		}

		return false;
	}

	/**
	 * get the total amount of signs for gamemode x
	 * @param gameMode game mode
	 * @return {@link Integer} value representing the total amount of signs with {@link GameMode} game mode
	 */
	public static int getTotalAmountSigns(GameMode gameMode)
	{
		return (int) playerSigns.values().stream()
				.filter(a -> a.gamemode == gameMode)
				.count();
	}

	public static List<Location> getLocationsOfAllSigns()
	{
		return new ArrayList<Location>(playerSigns.keySet());
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
					Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(),
							new BlackjackAnimation(cnf, player, CasinoManager.playerSignsManager));
					
					break;
				case SLOTS:
					Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(),
							new SlotsAnimation(cnf, player, CasinoManager.playerSignsManager));
					break;
				case DICE:
					Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(),
							new DiceAnimation(cnf, player, CasinoManager.playerSignsManager));
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

		/**
		 * the runnable which contains the functionality of the manager
		 * the runnable will be called every x ticks (configurable in the config) to do his work
		 */
		private final static Runnable task = new Runnable()
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

					//check if a player is in the range of a sign
					//if yes, execute the idle animation
					//to ensure that only loaded signs has a idle animation = better performance
					if(isPlayerInRange(sign.getLocation()))
					{
						switch (sign.gamemode)
						{
						case BLACKJACK:
							Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(),
									new Blackjack(sign.getSign(), sign));
							break;
						case SLOTS:
							Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(),
									new Slots(sign.getSign(), sign));
							break;
						case DICE:
							Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(),
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
