package scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.chrisimi.casino.main.MessageManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import animations.LeaderboardsignAnimation;
import serializeableClass.Leaderboardsign;
import serializeableClass.Leaderboardsign.Cycle;
import serializeableClass.Leaderboardsign.Mode;
import serializeableClass.PlayData;
import serializeableClass.PlayerSignsConfiguration;

public class LeaderboardsignsManager implements Listener {
		
	private static ArrayList<PlayData> playdatas = new ArrayList<>();
	
	private static HashMap<Location, Leaderboardsign> leaderboardsigns = new HashMap<>();
	public static HashMap<Leaderboardsign, Integer> leaderboardsignRunnableTaskID = new HashMap<>();
	
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
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get leaderboardsign reloadtime! You have to use a valid integer value! Set to default value: 12000 (10 Minutes)");
			reloadTime = 12000;
		}
		try 
		{
			signsenable = Boolean.valueOf(UpdateManager.getValue("playersigns.leaderboard-signs.enable").toString());
		} catch(Exception e) {
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get leaderboard-signs enable! You have to use a boolean value (true/false)! Set to default value: true");
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
		main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable()
		{
			
			@Override
			public void run()
			{
				exportData();
				exportLeaderboardsigns();
			}
		}, 20*60*5, 20*60*15);
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
			
			if(CasinoManager.configEnableConsoleMessages)
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
				if(sign.cycleMode == null)
					sign.cycleMode = Cycle.NaN;
				LeaderboardsignsManager.leaderboardsigns.put(sign.getLocation(), sign);
				try {
					addSignAnimation(sign);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			if(CasinoManager.configEnableConsoleMessages)
				CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully imported all leaderboard signs! (" + leaderboardsigns.list.size() + ")");
			
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
			
			if(CasinoManager.configEnableConsoleMessages)
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
	public void createLeaderboardSign(Player player, Sign sign, Mode mode, Boolean all, int count, int position, Cycle cycle)
	{
		Leaderboardsign leaderboardsign = new Leaderboardsign();
		leaderboardsign.setLocation(sign.getLocation());
		leaderboardsign.setMode(mode);
		
		if(player == null) //if leaderboard sign should be server leaderboard sign
			leaderboardsign.ownerUUID = "server";
		else
			leaderboardsign.setPlayer(player);
		leaderboardsign.setRange(all);
		leaderboardsign.setRange(count);
		leaderboardsign.position = position;
		leaderboardsign.cycleMode = cycle;
		leaderboardsigns.put(leaderboardsign.getLocation(), leaderboardsign);
		addSignAnimation(leaderboardsign);
		
		exportLeaderboardsigns();
	}
	public void addSignAnimation(Leaderboardsign sign) 
	{
		Sign signBlock = null;
		try 
		{
			signBlock = (Sign) sign.getLocation().getBlock().getState();
		} catch(Exception e) {
			CasinoManager.LogWithColor(ChatColor.RED + "Leaderboardsign is not valid! (Block is not a sign)!");
			leaderboardsigns.remove(sign.getLocation());
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
		Sign sign = null;
		Mode mode = null;
		Cycle cycle = null;
		int position = 0;
		Boolean all = false;
		int count = 0;
		try {
			sign = (Sign) event.getBlock().getState();
		} catch(Exception e) {
			 return;
		}
		String[] lines = event.getLines();
		if(!(lines[0].contains("leaderboard"))) {
			return;
		}
	
		if(lines[1].contains(";"))
		{
			String[] values = lines[1].split(";");
			try
			{
				position = Integer.valueOf(values[0]);
				
				for(Cycle cycle2 : Cycle.values())
				{
					if(cycle2.toString().equalsIgnoreCase(values[1]))
						cycle = cycle2;
				}
				
			} catch (Exception e)
			{
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("leaderboard-creation-cycle_invalid"));
				event.setCancelled(true);
				return;
			}
			
		}
		else
		{
			try {
				position = Integer.valueOf(lines[1]);
				cycle = Cycle.NaN;
			} catch(Exception e) {
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("leaderboard-creation-position_invalid"));
				event.setCancelled(true);
				return;
			}
		}
		for(Mode modeV : Mode.values()) {
			if(lines[2].equalsIgnoreCase(modeV.toString())) mode = modeV;
		}
		if(mode == null) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("leaderboard-creation-mode_invalid"));
			event.setCancelled(true);
			return;
		}
		if(lines[3].equalsIgnoreCase("all")) all = true;
		else {
			try {
				count = Integer.valueOf(lines[3]);
			} catch(Exception e) {
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("leaderboard-creation-range_invalid"));
				event.setCancelled(true);
				return;
			}
		}
		if(!(Main.perm.has(event.getPlayer(), "casino.leaderboard.create") || Main.perm.has(event.getPlayer(), "casino.admin")))
		{
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-creating-leaderboard"));
			event.setCancelled(true);
			return;
		}
		
		if(event.getLine(0).contains(";s"))
		{
			if(Main.perm.has(event.getPlayer(), "casino.serversigns") || Main.perm.has(event.getPlayer(), "casino.admin"))
			{
				createLeaderboardSign(null, sign, mode, all, count, position, cycle);
			}
			else 
			{
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("no-permissions-creating-leaderboard"));
			}
		}
		else
			createLeaderboardSign(event.getPlayer(), sign, mode, all, count, position, cycle);
		event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("leaderboard-creation_successful"));
	}
	
	private void checkIfSignIsLeaderboardSign(BlockBreakEvent event) 
	{
		Leaderboardsign sign = leaderboardsigns.get(event.getBlock().getLocation());
		if(sign == null) return;
		
		if(!(event.getPlayer().getUniqueId().equals(sign.getPlayer().getUniqueId()))) {
			
			//check if player is NOT admin
			if(!(Main.perm.has(event.getPlayer(), "casino.admin") || event.getPlayer().isOp())) {
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
				event.setCancelled(true);
				return;
			}
		}
		
		if(deleteLeaderbordsign(sign)) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("leaderboard-delete_successful"));
		} else {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("leaaderboard-error_when_break"));
		}
	}
	public static void save()
	{
		CasinoManager.leaderboardManager.exportLeaderboardsigns();
		CasinoManager.leaderboardManager.exportData();
		CasinoManager.leaderboardManager.reload();
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
		CasinoManager.Debug(LeaderboardsignsManager.class, "total datasets: " + playdatas.size());
		
		synchronized (playdatas)
		{
			dataList = playdatas.stream()
					.filter(a -> locationOfSignsFromPlayer.contains(a.Location))
					.collect(Collectors.toList());
		}
		return dataList;
	}
	public static List<PlayData> getPlayData(OfflinePlayer player, Calendar fromDate, Calendar toDate)
	{
		List<PlayData> dataList = new ArrayList<>();
		ArrayList<Location> locationsOfSignsFromPlayer = PlayerSignsManager.getLocationsFromAllPlayerSigns(player);
		CasinoManager.Debug(LeaderboardsignsManager.class, "total datasets: " + playdatas.size());
		
		synchronized (playdatas)
		{
			dataList = playdatas.stream()
					.filter(a -> locationsOfSignsFromPlayer.contains(a.Location) && a.Timestamp > fromDate.getTimeInMillis() && a.Timestamp < toDate.getTimeInMillis())
					.collect(Collectors.toList());
		}
		return dataList;
	}
	public static List<PlayData> getPlayData(OfflinePlayer player, long fromMilis, long toMilis)
	{
		List<PlayData> dataList = new ArrayList<>();
		ArrayList<Location> locationsOfSignsFromPlayer = PlayerSignsManager.getLocationsFromAllPlayerSigns(player);
		CasinoManager.Debug(LeaderboardsignsManager.class, "total datasets: " + playdatas.size());
		
		synchronized (playdatas)
		{
			dataList = playdatas.stream()
					.filter(a -> locationsOfSignsFromPlayer.contains(a.Location) && a.Timestamp > fromMilis && a.Timestamp < toMilis)
					.collect(Collectors.toList());
		}
		return dataList;
	}
	
	//--server side
	public static List<PlayData> getPlayData() {
		List<PlayData> dataList = new ArrayList<>();
		ArrayList<Location> locationOfSignsFromPlayer = PlayerSignsManager.getLocationsFromAllServerSigns();
		CasinoManager.Debug(LeaderboardsignsManager.class, "total datasets: " + playdatas.size());
		
		synchronized (playdatas)
		{
			dataList = playdatas.stream()
					.filter(a -> locationOfSignsFromPlayer.contains(a.Location))
					.collect(Collectors.toList());
		}
		return dataList;
	}
	public static List<PlayData> getPlayData(Calendar fromDate, Calendar toDate)
	{
		List<PlayData> dataList = new ArrayList<>();
		ArrayList<Location> locationsOfSignsFromPlayer = PlayerSignsManager.getLocationsFromAllServerSigns();
		CasinoManager.Debug(LeaderboardsignsManager.class, "total datasets: " + playdatas.size());
		synchronized (playdatas)
		{
			dataList = playdatas.stream()
					.filter(a -> locationsOfSignsFromPlayer.contains(a.Location) && a.Timestamp > fromDate.getTimeInMillis() && a.Timestamp < toDate.getTimeInMillis())
					.collect(Collectors.toList());
		}
		return dataList;
	}
	public static List<PlayData> getPlayData(long fromMilis, long toMilis)
	{
		List<PlayData> dataList = new ArrayList<>();
		ArrayList<Location> locationsOfSignsFromPlayer = PlayerSignsManager.getLocationsFromAllServerSigns();
		CasinoManager.Debug(LeaderboardsignsManager.class, "total datasets: " + playdatas.size());
		
		synchronized (playdatas)
		{
			dataList = playdatas.stream()
					.filter(a -> locationsOfSignsFromPlayer.contains(a.Location) && a.Timestamp > fromMilis && a.Timestamp < toMilis)
					.collect(Collectors.toList());
		}
		return dataList;
	}
	
	
	public static void resetData() 
	{
		LeaderboardsignsManager.playdatas = new ArrayList<>();
		CasinoManager.leaderboardManager.exportData();
		CasinoManager.LogWithColor(ChatColor.GREEN + "You successfully reset data.yml!");
	}
	public static void reloadData(Main main)
	{
		//1. stop all runnables
		//2. reload data
		//3. start all leaderboardsigns
	
		//1. stop all runnables
		for(int taskID : leaderboardsignRunnableTaskID.values()) 
		{
			main.getServer().getScheduler().cancelTask(taskID);
		}
		leaderboardsignRunnableTaskID.clear();
		
		//2. reload data
		playdatas.clear();
		CasinoManager.leaderboardManager.importData();
		
		//3. start all leaderboardsigns
		for(Leaderboardsign sign : leaderboardsigns.values()) 
		{
			CasinoManager.leaderboardManager.addSignAnimation(sign);
		}
	}
	public static void resetLeaderboard(Player player, Boolean allSigns, int range, Boolean allModes, Mode mode)
	{
		synchronized (leaderboardsigns)
		{
			if(allSigns)
			{
				for(Leaderboardsign leaderboardsign : leaderboardsigns.values())
				{
					if(leaderboardsign == null) continue;
					
					if(!leaderboardsign.isServerSign() && leaderboardsign.getPlayer().equals(player))
					{
						if(allModes)
							leaderboardsign.lastManualReset = System.currentTimeMillis();
						else if(leaderboardsign.getMode() == mode)
							leaderboardsign.lastManualReset = System.currentTimeMillis();
					}
				}
			} 
			else
			{
				for(Leaderboardsign leaderboardsign : leaderboardsigns.values())
				{
					if(leaderboardsign == null) continue;
					
					if(!leaderboardsign.isServerSign() && 
							leaderboardsign.getPlayer().equals(player))
					{
						if(player.getWorld().equals(leaderboardsign.getLocation().getWorld()) && (double)range > player.getLocation().distance(leaderboardsign.getLocation()))
						{
							if(allModes)
								leaderboardsign.lastManualReset = System.currentTimeMillis();
							else if(leaderboardsign.getMode() == mode)
								leaderboardsign.lastManualReset = System.currentTimeMillis();
						}
					}
				}
			}
		}
		//save changes
		CasinoManager.leaderboardManager.exportLeaderboardsigns();	
	}
	/**
	 * Reset all ServerSigns
	 * @param allSigns if all Signs should be reseted
	 * @param range range in Blocks
	 * @param allModes if every mode 
	 * @param mode Mode
	 */
	public static void resetServerLeaderboard(Player player, Boolean allSigns, int range, Boolean allModes, Mode mode)
	{
		synchronized (leaderboardsigns)
		{
			if(allSigns)
			{
				for(Leaderboardsign leaderboardsign : leaderboardsigns.values())
				{
					if(leaderboardsign == null) continue;
					
					if(leaderboardsign.isServerSign())
					{
						if(allModes)
							leaderboardsign.lastManualReset = System.currentTimeMillis();
						else if(leaderboardsign.getMode() == mode)
							leaderboardsign.lastManualReset = System.currentTimeMillis();
					}
				}
			} 
			else
			{
				for(Leaderboardsign leaderboardsign : leaderboardsigns.values())
				{
					if(leaderboardsign == null) continue;
					
					if(leaderboardsign.isServerSign())
					{
						if(player.getWorld().equals(leaderboardsign.getLocation().getWorld()) && (double)range > player.getLocation().distance(leaderboardsign.getLocation()))
						{
							if(allModes)
								leaderboardsign.lastManualReset = System.currentTimeMillis();
							else if(leaderboardsign.getMode() == mode)
								leaderboardsign.lastManualReset = System.currentTimeMillis();
						}
					}
				}
			}
		}
		//save changes
		CasinoManager.leaderboardManager.exportLeaderboardsigns();	
	}
	public static void clearAllTasks()
	{
		for(int taskID : leaderboardsignRunnableTaskID.values())
		{
			Main.getInstance().getServer().getScheduler().cancelTask(taskID);
		}
	}
}
