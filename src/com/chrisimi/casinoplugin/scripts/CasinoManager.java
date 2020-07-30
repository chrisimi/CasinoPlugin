package com.chrisimi.casinoplugin.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import com.chrisimi.casinoplugin.listeners.CasinoChatListener;
import com.chrisimi.casinoplugin.listeners.CasinoCommandsListener;
import com.chrisimi.casinoplugin.listeners.InventoryClickListener;
import com.chrisimi.casinoplugin.listeners.PlayerJoinListener;
import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.slotchest.SlotChestsManager;

public class CasinoManager {

	public static Main main;
	private static String prefix = "§9[§6Casino§9] §a";
	
	private static SignsManager signsManager;
	public static PlayerSignsManager playerSignsManager;
	private static RollCommand rollCommand;
	public static SlotChestsManager slotChestManager;
	public static LeaderboardsignsManager leaderboardManager;
	
	public static Boolean configEnableConsoleMessages = true;
	
	public static File debugfile;
	
	public CasinoManager(Main main) {
		debugfile = new File(main.getDataFolder(), "debug.log");
		this.main = main;
	}
	public void prefixYml() {
		try {
			String prefixFromYml = YamlConfiguration.loadConfiguration(Main.configYml).getString("prefix").replaceAll("&", "§");
			if(prefixFromYml == "" || prefixFromYml.equalsIgnoreCase("default")) {
				LogWithColor(org.bukkit.ChatColor.YELLOW + "no prefix in config.yml... using default one!");
			} else {
				main.getLogger().info(String.format("Found prefix %s in config.yml... changed to new prefix!", prefixFromYml));
				prefix = prefixFromYml;
			}
		} catch(Exception e)
		{
			LogWithColor(ChatColor.RED + "An error occured while trying to get prefix. That's because you are using a old config."
					+ " Try to rename or delete your config.yml and restart/reload the server OR go into the config.yml and go to prefix (line 27) and write 'default'.");
			prefix = "§9[§6Casino§9] §a";
		}
	}
	public void initialize() {
		
		new CasinoCommandsListener(main);
		new InventoryClickListener(main);
		new PlayerJoinListener(main);
		new CasinoChatListener(main);
		signsManager = new SignsManager(main);
		playerSignsManager = new PlayerSignsManager(main);
		rollCommand = new RollCommand(main);
		slotChestManager = new SlotChestsManager(main);
		leaderboardManager = new LeaderboardsignsManager(main);
		
		try 
		{
			configEnableConsoleMessages = Boolean.valueOf(UpdateManager.getValue("enable-console-messages").toString());
		} catch(Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get enable-console-messages! You have to use a valid boolean value (true/false)! Set to default value: false");
			configEnableConsoleMessages = false;
		}
	}
	
	public static void reload() {
		signsManager.reload();
		playerSignsManager.reload();
		rollCommand.reload();
		slotChestManager.reload();
	}
	public static String getPrefix() {return prefix;}
	
	public static void LogWithColor(String message) {
		try {
			main.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('�', prefix) + message);
			
		} catch(Exception e) {
			if(message == null) return;
			e.printStackTrace();
			Bukkit.getLogger().info(message);
			return;
		}
		try
		{
			Debug(Main.class, message);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public static <T> void Debug(Class<T> className, String debugMessage)
	{
		if(UpdateManager.configValues.size() == 0) return;
		
		try {
			
			if(!(UpdateManager.getValue("enable-debug").toString().equalsIgnoreCase("true"))) return;
		} catch(Exception e)
		{
			
		}
		String message = String.format("[%s] %s: %s" , new Date().toString(), className.toString(), debugMessage);
		
		
		if(Main.development)
			Main.getInstance().getLogger().info(message);
		
		
		synchronized (debugfile)
		{
			PrintWriter writer = null;
			try
			{
				if(!(debugfile.exists()))
					debugfile.createNewFile();
				
				writer = new PrintWriter(new BufferedWriter(new FileWriter(debugfile, true)));
				writer.println(message);
			} catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				writer.close();
			}
		}
	}
	public static PrintWriter getPrintWriterForDebug()
	{
		try
		{
			return new PrintWriter(new BufferedWriter(new FileWriter(debugfile, true)));
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
