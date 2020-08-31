package com.chrisimi.casinoplugin.main;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import com.chrisimi.casinoplugin.scripts.*;
import com.chrisimi.versionchecker.VersionChecker;
import com.chrisimi.versionchecker.VersionResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.chrisimi.casinoplugin.hologramsystem.HologramSystem;
import com.chrisimi.casinoplugin.slotchest.animations.RollAnimationManager;
import com.chrisimi.inventoryapi.InventoryAPI;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class Main extends JavaPlugin {

	public static Economy econ = null;
	public static Permission perm = null;

	public static VersionResult result = null;
	public static boolean development = false;


	public static File configYml;
	
	public static File signsYml;
	public static File playerSignsYml;
	public static File dataYml;
	public static File leaderboardSignsYml;
	
	public static File slotChestsYml;
	
	private static String pathToFolderOfPlugin; //.../plugins/CasinoPlugin/
	
	public static String fileSeparator;
	
	private static Main instance;
	public static MessageManager msgManager;
	
	
	public static boolean hologramSystemUp;
	
	@Override
	public void onEnable() {
		
		instance = this;
		
		//configYml = new File(getDataFolder(), "config.yml");
		fileSeparator = System.getProperty("file.separator");
		
		CasinoManager casinoManager = new CasinoManager(this);
		
		
		
		
		getPathToFolderOfPlugin();
		configYmlActivate();
		signsYmlActivate();
		playerSignsYmlActivate();
		slotChestsYmlActivate();
		
		UpdateManager.reloadConfig();
		
		
		 //equals .getConfigData
		msgManager = new MessageManager(this);
		
		//new ConfigurationManager(this);
		casinoManager.prefixYml();
		casinoManager.initialize();



		checkVersion();

		//getLogger().info("Minecraft Server version: " + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);
		
		activateEconomySystem();
		activatePermissionSystem();
		
		Metrics metric = new Metrics(this); //Stats plugin
		BStatsManager.configureMetrics(metric);
		
		//CasinoManager.LogWithColor("Test: " + MessageManager.get("test"));
		
		
		//check if the hologram system can be started
		if(hologramSystemUp = checkCompatibility())
			HologramSystem.getInstance().startSystem(this); //start hologram system
		
		InventoryAPI.initiate(this);
	}

	private void checkVersion()
	{
		result = VersionChecker.getStatus(this, "71898");

		switch(result.getStatus())
		{
			case OUTDATED:
				CasinoManager.LogWithColor(String.format(ChatColor.YELLOW + "Your plugin is not up to date. Your current version is %s and the newest version is %s", result.getLocalPluginVersion(), result.getSpigotPluginVersion()));
				break;
			case UP_TO_DATE:
				CasinoManager.LogWithColor(ChatColor.GREEN + "Plugin is up to date.");
				break;
			case ERROR:
			default:
				CasinoManager.LogWithColor(ChatColor.YELLOW + "An error occured while trying to fetch version from spigot. Error can be ignored if it's happening for the first or second time. If this error happens more often make sure to manually check the spigot site for new updates");
				break;
		}
	}

	@Override
	public void onDisable()
	{
		LeaderboardsignsManager.clearAllTasks();
		CasinoManager.playerSignsManager.serverClose();
		HologramSystem.getInstance().stopSystem();
	}

	


	/*
	private void versionManager() {
		String versionLocal = UpdateManager.getValue("version").toString();
		
		CasinoManager.LogWithColor(ChatColor.YELLOW + String.format("config version installed: %s - config version in jar: %s", versionLocal, configVersion));
		if(!(configVersion.equals(versionLocal))) {
			CasinoManager.LogWithColor(ChatColor.YELLOW + "There is a new version for the CasinoPlugin config.yml. Please follow ingame instructions! /casino updateconfig");
			isConfigUpdated = false;
		}
	}
	*/
	
	
	private void getPathToFolderOfPlugin() {
		File toCasinoPluginFolder = getDataFolder();
		if(!toCasinoPluginFolder.exists()) {
			toCasinoPluginFolder.mkdir();
		} else {
		
		}
		pathToFolderOfPlugin = toCasinoPluginFolder.getAbsolutePath();
	}
	private void configYmlActivate() {
		
		/*
		configYml = new File(getDataFolder().getAbsolutePath() + "\\config.yml");
		//getLogger().info(getDataFolder().getAbsolutePath()+ "\\config.yml");
		if(!(configYml.exists())) {
			new File(getDataFolder().getAbsolutePath()).mkdir();
			UpdateManager.createConfigYml(this);
		}
		*/
		getLogger().info(pathToFolderOfPlugin);
		configYml = new File(pathToFolderOfPlugin, "config.yml");
		if(!configYml.exists()) {
			try {
				configYml.createNewFile();
				UpdateManager.createConfigYml(this);
			} catch (IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to create config.yml:");
				e.printStackTrace();
			}
		} else CasinoManager.LogWithColor(ChatColor.GREEN + "config.yml exists!");
		
	}
	private void signsYmlActivate() {
		/*
		signsYml = new File(getDataFolder().getAbsolutePath() + "\\signs.json");
		if(!(signsYml.exists())) {
			try {
				signsYml.createNewFile();
			} catch (IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while creating signs.json!");
				e.printStackTrace();
			}
		}
		*/
		signsYml = new File(pathToFolderOfPlugin + fileSeparator + "signs.json");
		if(!signsYml.exists()) {
			try {
				signsYml.createNewFile();
			} catch (IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to create signs.json:");
				e.printStackTrace();
			}
		} else CasinoManager.LogWithColor(ChatColor.GREEN + "signs.json exists!");
	}
	private void playerSignsYmlActivate() {
		/*
		playerSignsYml = new File(getDataFolder().getAbsolutePath() + "\\playersigns.json");
		if(!(playerSignsYml.exists())) {
			try {
				playerSignsYml.createNewFile();
			} catch(IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while creating playersigns.json!");
				e.printStackTrace();
			}
		}
		*/
		playerSignsYml = new File(pathToFolderOfPlugin + fileSeparator + "playersigns.json");
		if(!playerSignsYml.exists()) {
			try {
				playerSignsYml.createNewFile();
			} catch (IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to create playersigns.json:");
				e.printStackTrace();
			}
		} else CasinoManager.LogWithColor(ChatColor.GREEN + "playersigns.json exists!");
		
		dataYml = new File(pathToFolderOfPlugin, "data.yml");
		if(!dataYml.exists()) {
			try {
				dataYml.createNewFile();
			} catch(IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to create data.yml:");
				e.printStackTrace();
			}
		} else CasinoManager.LogWithColor(ChatColor.GREEN + "data.yml exists!");
		
		
		leaderboardSignsYml = new File(pathToFolderOfPlugin, "leaderboardsigns.yml");
		if(!leaderboardSignsYml.exists()) {
			try {
				leaderboardSignsYml.createNewFile();
			} catch(IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to create leaderboardsigns.yml:");
				e.printStackTrace();
			}
		} else CasinoManager.LogWithColor(ChatColor.GREEN + "leaderboardsigns.yml exists!");
	}
	private void slotChestsYmlActivate() {
		/*
		slotChestsYml = new File(getDataFolder().getAbsolutePath() + "\\slotchests.json");
		if(!(slotChestsYml.exists())) {
			try {
				slotChestsYml.createNewFile();
			} catch(IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while creating slotchests.json!");
				e.printStackTrace();
			}
		}
		*/
		slotChestsYml = new File(pathToFolderOfPlugin + fileSeparator + "slotchests.json");
		if(!slotChestsYml.exists()) {
			try {
				slotChestsYml.createNewFile();
			} catch (IOException e) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to create slotchests.json:");
				e.printStackTrace();
			}
		} else CasinoManager.LogWithColor(ChatColor.GREEN + "slotchests.json exists!");
	}
	

	private void activateEconomySystem() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			CasinoManager.LogWithColor(ChatColor.RED + "Can't find vault!");
			Bukkit.shutdown();
		} else {
			RegisteredServiceProvider<Economy> esp = getServer().getServicesManager().getRegistration(Economy.class);
			if (esp == null) {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to connect to vault, try restarting server and check vault!");
			}
			else {
				econ = esp.getProvider();
				CasinoManager.LogWithColor(ChatColor.GREEN + "Connected successfully to vault!");
			}
		}
	}
	private void activatePermissionSystem() {
		RegisteredServiceProvider<Permission> permProvider = getServer().getServicesManager().getRegistration(Permission.class);
		if(permProvider != null) {
			perm = permProvider.getProvider();
			CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully hooked with " + permProvider.getPlugin().getName());
		} else {
			CasinoManager.LogWithColor(ChatColor.RED + "Unable to activate vault's permission system! Try restarting server and check vault!");
		}
	}

	/**
	 * get the compatibility
	 * @return false if HolographicDisplays is not available or invalid
	 */
	private boolean checkCompatibility()
	{
		boolean holographicsEnabled = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
		boolean configValueEnabled = Boolean.parseBoolean(UpdateManager.getValue("holograms-enabled", true).toString());
		
		if(!configValueEnabled)
		{
			CasinoManager.LogWithColor(ChatColor.YELLOW + "You've disabled holograms! No holograms will be loaded.");
			return false;
		}
		else if(!holographicsEnabled)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "Can't find HolographicDisplays! Make sure that you are using the newest version and it's working! https://dev.bukkit.org/projects/holographic-displays. \nYou can also deactivate the hologram system in the config.yml to remove this message.");
			return false;
		}
		else
		{
			CasinoManager.LogWithColor(ChatColor.GREEN + "Holograms are enabled on the server.");
			return true;
		}
		
	}
	
	public static Main getInstance()
	{
		return instance;
	}
}
