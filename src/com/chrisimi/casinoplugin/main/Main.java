package com.chrisimi.casinoplugin.main;

import java.io.File;
import java.io.IOException;

import com.chrisimi.casinoplugin.scripts.*;
import com.chrisimi.numberformatter.Configuration;
import com.chrisimi.numberformatter.NumberFormatter;
import com.chrisimi.versionchecker.VersionChecker;
import com.chrisimi.versionchecker.VersionResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.chrisimi.casinoplugin.hologramsystem.HologramSystem;
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
	public static File jackpotJson;
	public static File notificationsYml;

	public static File slotChestsYml;
	
	private static String pathToFolderOfPlugin; //.../plugins/CasinoPlugin/
	
	private static Main instance;
	public static MessageManager msgManager;
	
	
	public static boolean hologramSystemUp;
	
	@Override
	public void onEnable() {
		
		instance = this;

		getPathToFolderOfPlugin();
		configYml = new File(pathToFolderOfPlugin, "config.yml");
		createFile(configYml);

		CasinoManager casinoManager = new CasinoManager();
		createFiles();



		UpdateManager.reloadConfig();

		msgManager = new MessageManager();

		casinoManager.prefixYml();
		casinoManager.initialize();

		checkVersion();

		activateEconomySystem();
		activatePermissionSystem();

		//configure bstats
		Metrics metric = new Metrics(this); //Stats plugin
		BStatsManager.configureMetrics(metric);
		
		//check if the hologram system can be started
		if(hologramSystemUp = checkCompatibility())
			HologramSystem.getInstance().startSystem(); //start hologram system
		
		InventoryAPI.initiate(this);

		setUpNumberFormatter();
	}

	private void setUpNumberFormatter()
	{
		boolean atTheBegin = false;
		try
		{
			atTheBegin = Boolean.parseBoolean(UpdateManager.getValue("currency-at-the-begin", false).toString());
		} catch(Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to parse currency-at-the-begin. Using default value: false");
		}

		String currency = " $";
		try
		{
			currency = UpdateManager.getValue("currency", currency).toString();
		} catch(Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "Error while trying to parse currency. Using default value: ' $'");
		}

		Configuration conf = new Configuration()
				.withCurrency(currency, atTheBegin)
				.withDiv(1000)
				.withSymbols(new String[] {"k", "M", "B", "T", "Q"});

		NumberFormatter.setConfiguration(conf);
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

	private void getPathToFolderOfPlugin()
	{
		File toCasinoPluginFolder = getDataFolder();

		if(!getDataFolder().exists() && getDataFolder().mkdirs())
			CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully created CasinoPlugin folder");

		pathToFolderOfPlugin = toCasinoPluginFolder.getAbsolutePath();
	}

	private void createFiles()
	{
		signsYml = new File(pathToFolderOfPlugin,"signs.json");
		createFile(signsYml);

		playerSignsYml = new File(pathToFolderOfPlugin, "playersigns.json");
		createFile(playerSignsYml);

		dataYml = new File(pathToFolderOfPlugin, "data.yml");
		createFile(dataYml);

		leaderboardSignsYml = new File(pathToFolderOfPlugin, "leaderboardsigns.yml");
		createFile(leaderboardSignsYml);

		slotChestsYml = new File(pathToFolderOfPlugin, "slotchests.json");
		createFile(slotChestsYml);

		jackpotJson = new File(pathToFolderOfPlugin, "jackpot.json");
		createFile(jackpotJson);

		notificationsYml = new File(pathToFolderOfPlugin, "notifications.yml");
		createFile(notificationsYml);
	}

	private void activateEconomySystem()
	{
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
				CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully hooked with " + esp.getPlugin().getName());
			}
		}
	}

	private void activatePermissionSystem()
	{
		RegisteredServiceProvider<Permission> permProvider = getServer().getServicesManager().getRegistration(Permission.class);
		if(permProvider != null) {
			perm = permProvider.getProvider();
			CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully hooked with " + permProvider.getPlugin().getName());
		} else {
			CasinoManager.LogWithColor(ChatColor.RED + "Unable to activate vault's permission system! Try restarting server and check vault!");
		}
	}

	/**
	 * check if the file exists and if not create it
	 * @param file {@linkplain File} instance to create
	 */
	private void createFile(File file)
	{
		try
		{
			if(file.exists())
			{
				CasinoManager.LogWithColor(ChatColor.GREEN + file.getName() + " exists!");
				return;
			}

			if(!file.exists() && file.createNewFile())
			{
				CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully created " + file.getName());
				UpdateManager.createConfigYml();
			}
		} catch (IOException e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to create" + file.getName());
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
