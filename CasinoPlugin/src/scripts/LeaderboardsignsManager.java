package scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.Listener;

import com.chrisimi.casino.main.Main;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import serializeableClass.Leaderboardsign;

public class LeaderboardsignsManager implements Listener {
		
	private static ArrayList<PlayData> playdatas = new ArrayList<>();
	
	private static HashMap<Location, Leaderboardsign> leaderboardsigns = new HashMap<>();
	
	private static Gson gson;
	
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
	public void reload() {
	
	}
	private void initialize() {
		
	}
	
	//
	// export / import
	//
	
	//format of data.yml:
	//<UUID>;<world>;<location splited by ','>; <playAmount>;<wonAmount>;<timestamp>
	//bsp:
	//1892-8172817-38738;world;12,13,14;25;50;154272772
	
	public class PlayData {
		public Player Player;
		public World World;
		public Location Location;
		public double PlayAmount;
		public double WonAmount;
		public long Timestamp;
	}
	
	private synchronized void importData() {
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(Main.dataYml));
			String line = "";
			while((line = reader.readLine()) != null) {
				String[] splited = line.split(";");
				if(splited.length != 6) {
					CasinoManager.LogWithColor(ChatColor.RED + "data value is invalid! length is not 6! line will be deleted!");
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
				writer.append(getStringFromPlayData(data));
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
			playData.Player = Bukkit.getPlayer(UUID.fromString(data[0]));
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
		public ArrayList<PlayData> list = new ArrayList<>();
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
			
			LeaderboardList leaderboardsigns = gson.fromJson(jsonString.toString(), LeaderboardList.class);
			if(leaderboardsigns == null || leaderboardsigns.list == null) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to import all leaderboardsigns!");
				return;
			}
			
			
			
		}
	}
	private synchronized void exportLeaderboardsigns() {
		
	}
	
}
