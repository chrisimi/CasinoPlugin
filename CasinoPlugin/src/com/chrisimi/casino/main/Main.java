package com.chrisimi.casino.main;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import scripts.CasinoAnimation;
import scripts.CasinoManager;
import scripts.PlayerSignsManager;
import scripts.RollCommand;
import scripts.SignsManager;
import scripts.UpdateManager;

public class Main extends JavaPlugin {

	public static Economy econ = null;
	public static Permission perm = null;

	/**
	 * config version in jar
	 */
	public static String configVersion = "3.0"; //version in jar
	public static Boolean isConfigUpdated = true;
	
	public static String pluginVersion = "3.0";
	public static Boolean isPluginUpdated = true;
	public static File configYml;
	
	public static File signsYml;
	public static File playerSignsYml;
	
	public static File slotChestsYml;
	@Override
	public void onEnable() {
		configYmlActivate();
		signsYmlActivate();
		playerSignsYmlActivate();
		slotChestsYmlActivate();
		activateEconomySystem();
		activatePermissionSystem();
		UpdateManager.reloadConfig(); //equals .getConfigData
		
		new CasinoManager(this);
		//new ConfigurationManager(this);
		versionManager();
		VersionManager.CheckForNewVersion(pluginVersion, this);
		
		//getLogger().info("Minecraft Server version: " + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);
		Metrics metric = new Metrics(this); //Stats plugin
		configurateMetrics(metric);
		
		
	}

	private void configurateMetrics(Metrics metric) {
		
		metric.addCustomChart(new Metrics.SingleLineChart("use_of_slots", new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				int amount = CasinoAnimation.rollCount;
				CasinoAnimation.rollCount = 0;
				//Bukkit.getLogger().info("Send use_of_slots to bstats: " + amount);
				return amount;
				
			}
			
		}));
		metric.addCustomChart(new Metrics.SingleLineChart("use_of_casinosigns", new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				int amount = SignsManager.playCount;
				SignsManager.playCount = 0;
				//Bukkit.getLogger().info("Send use_of_casinoslots to bstats: " + amount);
				return amount;
				
			}
			
		}));
		metric.addCustomChart(new Metrics.SingleLineChart("use_of_playersigns", new Callable<Integer>() {
			
			@Override
			public Integer call() throws Exception {
				int amount = PlayerSignsManager.rollCount;
				PlayerSignsManager.rollCount = 0;
				//Bukkit.getLogger().info("Send use_of_playersigns to bstats: " + amount);
				return amount;
			}
			
		}));
		metric.addCustomChart(new Metrics.SingleLineChart("use_of_roll_command", new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				int amount = RollCommand.rollAmount;
				RollCommand.rollAmount = 0;
				return amount;
			}
		}));
	}
	



	private void versionManager() {
		String versionLocal = UpdateManager.getValue("version").toString();
		
		getLogger().info(String.format("config version installed: %s - config version in jar: %s", versionLocal, configVersion));
		if(!(configVersion.equals(versionLocal))) {
			getLogger().info(String.format("There is a new version for the CasinoPlugin config.yml. Please follow ingame instructions! /casino updateconfig"));
			isConfigUpdated = false;
		}
	}

	private void configYmlActivate() {
		
		
		configYml = new File(getDataFolder().getAbsolutePath() + "\\config.yml");
		//getLogger().info(getDataFolder().getAbsolutePath()+ "\\config.yml");
		if(!(configYml.exists())) {
			new File(getDataFolder().getAbsolutePath()).mkdir();
			UpdateManager.createConfigYml(this);
		}
	}
	private void signsYmlActivate() {
		signsYml = new File(getDataFolder().getAbsolutePath() + "\\signs.json");
		if(!(signsYml.exists())) {
			try {
				signsYml.createNewFile();
			} catch (IOException e) {
				getLogger().info("Error in creating signs.json!");
				e.printStackTrace();
			}
		}
	}
	private void playerSignsYmlActivate() {
		playerSignsYml = new File(getDataFolder().getAbsolutePath() + "\\playersigns.json");
		if(!(playerSignsYml.exists())) {
			try {
				playerSignsYml.createNewFile();
			} catch(IOException e) {
				getLogger().info("Error in creating playersigns.json!");
				e.printStackTrace();
			}
		}
	}
	private void slotChestsYmlActivate() {
		slotChestsYml = new File(getDataFolder().getAbsolutePath() + "\\slotchests.json");
		if(!(slotChestsYml.exists())) {
			try {
				slotChestsYml.createNewFile();
			} catch(IOException e) {
				getLogger().info("Error while trying to create slotchests.json!");
				e.printStackTrace();
			}
		}
	}
	

	private void activateEconomySystem() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			getLogger().info("cannot find Vault");
			Bukkit.shutdown();
		} else {
			RegisteredServiceProvider<Economy> esp = getServer().getServicesManager().getRegistration(Economy.class);
			if (esp == null) {
				getLogger().info("Error while finding Service");
			}
			else {
				econ = esp.getProvider();
				getLogger().info("found Vault");
			}
		}
	}
	private void activatePermissionSystem() {
		RegisteredServiceProvider<Permission> permProvider = getServer().getServicesManager().getRegistration(Permission.class);
		if(permProvider != null) {
			perm = permProvider.getProvider();
		} else {
			getLogger().info("cannot activate Permission system!");
		}
	}
	

}
