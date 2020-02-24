package scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.chrisimi.casino.main.Main;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import animations.LeaderboardsignAnimation;
import serializeableClass.Leaderboardsign;
import serializeableClass.Leaderboardsign.Mode;
import serializeableClass.PlayData;
import serializeableClass.PlayerSignsConfiguration;

public class LeaderboardsignsManager implements Listener {
		
	private static ArrayList<PlayData> playdatas = new ArrayList<>();
	
	private static HashMap<Location, Leaderboardsign> leaderboardsigns = new HashMap<>();
	private static HashMap<Leaderboardsign, Integer> leaderboardsignRunnableTaskID = new HashMap<>();
	
	private static Gson gson;
	
	private static int reloadTime = 0;
	private static Boolean signsenable = false;
	
	private final Main main;
	public LeaderboardsignsManager(Main main) {
		this.main = main;
		main.getServer().getPluginManager().registerEvents(this, main);
		gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();
		
		reload(); //get variables from config.yml
		initialize();
		
	}
	/**
	 * reload the all config variables from config.yml
	 */
	public void reload() 
	{
		try {
			reloadTime = Integer.valueOf(UpdateManager.getValue("playersigns.leaderboard-signs.reload-time").toString());
		} catch(Exception e) {
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get leaderboardsign reloadtime! You have to use a valid integer value! Set to default value: 1200 (1 Minute)");
			reloadTime = 1200;
		}
		try 
		{
			signsenable = Boolean.valueOf(UpdateManager.getValue("playersigns.leaderboard-signs.enable").toString());
		} catch(Exception e) {
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get leaderboard-sgisn enable! You have to use a boolean value (true/false)! Set to default value: true");
			signsenable = true;
		}
	}
	private void initialize() {
		if(signsenable)
		{
			importLeaderboardsigns();
			importData();
		} else 
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "Leaderboard signs are disabled!");
	}
	
	//
	// export / import
	//
	
	
	
	private synchronized void importData() {
		
		BufferedReader reader = null;
		int row = 0;
		try {
			reader = new BufferedReader(new FileReader(Main.dataYml));
			String line = "";
			while((line = reader.readLine()) != null) {
				row++;
				
				String[] splited = line.split(";");
				if(splited.length != 6) {
					CasinoManager.LogWithColor(ChatColor.RED + "data value is invalid! length is not 6! line will be deleted! Row: " + row);
					continue;
				} else if(splited[2].split(",").length != 3) {
					CasinoManager.LogWithColor(ChatColor.RED + "location is invalid!");
					continue;
				} else if(Bukkit.getWorld(splited[1]) == null) {
					CasinoManager.LogWithColor(ChatColor.RED + "worldname is invalid!");
					continue;
				}
				PlayData data = getPlayData(splited);
				if(data != null && !playdatas.contains(data)) {
					playdatas.add(data);
				}
				
			}
			CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully imported " + playdatas.size() + " data-packets!");
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
		
			} catch(Exception e) {
				//nothing
			}
		}
	}
	private synchronized void exportData() {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(Main.dataYml));
			writer.write("");
			for(PlayData data : playdatas) {
				writer.append(getStringFromPlayData(data) + "\n");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch(Exception e) {
				//nothing
			}
		}
	}
	
	private PlayData getPlayData(String[] data) {
		
		PlayData playData = new PlayData();
		try {
			playData.Player = Bukkit.getOfflinePlayer(UUID.fromString(data[0]));
			playData.World = Bukkit.getWorld(data[1]);
			String[] locationSplit = data[2].split(",");
			playData.Location = new Location(playData.World, Integer.valueOf(locationSplit[0]), Integer.valueOf(locationSplit[1]), Integer.valueOf(locationSplit[2]));
			playData.PlayAmount = Double.valueOf(data[3]);
			playData.WonAmount = Double.valueOf(data[4]);
			playData.Timestamp = Long.valueOf(data[5]);
		} catch(NumberFormatException e) {
			CasinoManager.LogWithColor(ChatColor.RED + "error at converting data!");
			playData = null;
		} 
		return playData;
	}
	private String getStringFromPlayData(PlayData data) {
		String[] splited = new String[6];
		splited[0] = data.Player.getUniqueId().toString();
		splited[1] = data.World.getName();
		String[] locationSplit = new String[3];
		locationSplit[0] = String.valueOf(data.Location.getBlockX());
		locationSplit[1] = String.valueOf(data.Location.getBlockY());
		locationSplit[2] = String.valueOf(data.Location.getBlockZ());
		splited[2] = String.join(",", locationSplit);
		splited[3] = String.valueOf(data.PlayAmount);
		splited[4] = String.valueOf(data.WonAmount);
		splited[5] = String.valueOf(data.Timestamp);
		return String.join(";", splited);
	}
	
	//leaderboard signs
	public class LeaderboardList {
		@Expose
		public ArrayList<Leaderboardsign> list = new ArrayList<>();
	}
	
	private synchronized void importLeaderboardsigns() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(Main.leaderboardSignsYml));
			
			StringBuilder jsonString = new StringBuilder();
			String line = "";
			while((line = reader.readLine()) != null) {
				jsonString.append(line);
			}
			
			if(jsonString.length() < 9) return;
			LeaderboardList leaderboardsigns = gson.fromJson(jsonString.toString(), LeaderboardList.class);
			if(leaderboardsigns == null || leaderboardsigns.list == null) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to import all leaderboardsigns!");
				throw new Exception("leaderboard signs is null!");
			}
			for(Leaderboardsign sign : leaderboardsigns.list) {
				LeaderboardsignsManager.leaderboardsigns.put(sign.getLocation(), sign);
				try {
					addSignAnimation(sign);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully exported all leaderboard signs!");
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			
			try
			{
				reader.close();
			} catch (IOException e)
			{
				//nothing
			}
		}
	}
	private synchronized void exportLeaderboardsigns()
	{
		BufferedWriter writer = null;
		try 
		{
			writer = new BufferedWriter(new FileWriter(Main.leaderboardSignsYml));
			LeaderboardList list = new LeaderboardList();
			list.list.addAll(leaderboardsigns.values());
			
			writer.write(gson.toJson(list));
			CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully exported leaderboardsigns!");
		} catch(Exception e)
		{
			e.printStackTrace();
		} finally 
		{
			try
			{
				writer.close();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
//
//	public methods
//
	public Boolean deleteLeaderbordsign(Leaderboardsign sign) 
	{
		synchronized (leaderboardsigns)
		{
			main.getServer().getScheduler().cancelTask(leaderboardsignRunnableTaskID.remove(sign));
			return leaderboardsigns.remove(sign.getLocation()) != null;
		}
	}
	public void createLeaderboardSign(Player player, Sign sign, Mode mode, Boolean all, int count, int position)
	{
		Leaderboardsign leaderboardsign = new Leaderboardsign();
		leaderboardsign.setLocation(sign.getLocation());
		leaderboardsign.setMode(mode);
		leaderboardsign.setPlayer(player);
		leaderboardsign.setRange(all);
		leaderboardsign.setRange(count);
		leaderboardsign.position = position;
		leaderboardsigns.put(leaderboardsign.getLocation(), leaderboardsign);
		addSignAnimation(leaderboardsign);
		player.sendMessage(CasinoManager.getPrefix() + "You successfully created a leaderboard sign!");
		exportLeaderboardsigns();
	}
	public void addSignAnimation(Leaderboardsign sign) 
	{
		Sign signBlock = null;
		try 
		{
			signBlock = (Sign) sign.getLocation().getBlock().getState();
		} catch(Exception e) {
			CasinoManager.LogWithColor(ChatColor.RED + "Leaderboardsign is not valid! (Block is not a sign!");
			return;
		}
		addSignAnimation(sign, signBlock);
	}
	public void addSignAnimation(Leaderboardsign LBsign, Sign sign)
	{
		Random rnd = new Random();
		
		int randomWaitTime = rnd.nextInt(200);
		int taskID = main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new LeaderboardsignAnimation(main, LBsign, sign), 
				(long)randomWaitTime, (long)reloadTime);
		if(taskID == -1) {
			CasinoManager.LogWithColor(ChatColor.RED + "Error occured while trying to animate sign!");
			return;
		}
		leaderboardsignRunnableTaskID.put(LBsign, taskID);
	}
	
//
//	EventHandler and methods
//
	@EventHandler
	public void onSignPlace(SignChangeEvent event) 
	{
		Bukkit.getLogger().info("Block place event");
		checkIfSignIsLeaderboardSign(event);
	}
	@EventHandler
	public void onSignBreak(BlockBreakEvent event)
	{
		if(leaderboardsigns.containsKey(event.getBlock().getLocation())) {
			checkIfSignIsLeaderboardSign(event);
		}
	}
	
	private void checkIfSignIsLeaderboardSign(SignChangeEvent event) 
	{
		Bukkit.getLogger().info("checkIfSignIsLeaderboard");
		Sign sign = null;
		Mode mode = null;
		int position = 0;
		Boolean all = false;
		int count = 0;
		try {
			sign = (Sign) event.getBlock().getState();
		} catch(Exception e) {
			 return;
		}
		String[] lines = event.getLines();
		if(!(lines[0].equalsIgnoreCase("leaderboard"))) {
			return;
		}
	
		try {
			position = Integer.valueOf(lines[1]);
		} catch(Exception e) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + "�4Position must be an Integer value!");
			event.setCancelled(true);
			return;
		}
		
		for(Mode modeV : Mode.values()) {
			if(lines[2].equalsIgnoreCase(modeV.toString())) mode = modeV;
		}
		if(mode == null) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + "�4You have to use a valid mode! (highestamount, count, sumamount");
			event.setCancelled(true);
			return;
		}
		if(lines[3].equalsIgnoreCase("all")) all = true;
		else {
			try {
				count = Integer.valueOf(lines[3]);
			} catch(Exception e) {
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + "�4You have to use a valid range! (all or range in blocks)!");
				event.setCancelled(true);
				return;
			}
		}
		createLeaderboardSign(event.getPlayer(), sign, mode, all, count, position);
	}
	
	private void checkIfSignIsLeaderboardSign(BlockBreakEvent event) 
	{
		Leaderboardsign sign = leaderboardsigns.get(event.getBlock().getLocation());
		if(sign == null) return;
		
		if(!(event.getPlayer().getUniqueId().equals(sign.getPlayer().getUniqueId()))) {
			
			//check if player is NOT admin
			if(!(Main.perm.has(event.getPlayer(), "casino.admin") || event.getPlayer().isOp())) {
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + "�4You don't have enough permission to break this leaderboardsign!");
				event.setCancelled(true);
				return;
			}
		}
		
		if(deleteLeaderbordsign(sign)) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + "You successfully deleted this leaderboardsign!");
		} else {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + "�4An error occured while trying to break the leaderboardsign! Retry it in a moment or relog!");
		}
	}
	public static void save()
	{
		CasinoManager.leaderboardManager.exportLeaderboardsigns();
		CasinoManager.leaderboardManager.exportData();
	}
	public static void addData(Player player, PlayerSignsConfiguration manager, double playAmount, double winAmount)
	{
		PlayData data = new PlayData();
		data.Player = player;
		data.World = manager.getLocation().getWorld();
		data.Location = manager.getLocation();
		data.PlayAmount = playAmount;
		data.WonAmount = winAmount;
		data.Timestamp = System.currentTimeMillis();
		
		playdatas.add(data);
		CasinoManager.leaderboardManager.exportData();
	}
	/**
	 * Get all PlayData with 
	 * @param location of sign
	 * @return ArrayList containing all playdata where uuid Player is owner
	 */
	public static List<PlayData> getPlayData(OfflinePlayer player) {
		List<PlayData> dataList = new ArrayList<>();
		ArrayList<Location> locationOfSignsFromPlayer = PlayerSignsManager.getLocationsFromAllPlayerSigns(player);
		
		synchronized (playdatas)
		{
			dataList = playdatas.stream()
					.filter(a -> locationOfSignsFromPlayer.contains(a.Location))
					.collect(Collectors.toList());
		}
		return dataList;
	}
}