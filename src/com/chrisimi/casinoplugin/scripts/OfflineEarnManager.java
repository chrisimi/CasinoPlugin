package com.chrisimi.casinoplugin.scripts;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;

/**
 * start priority: last
 * manages the offline earnings of players
 * @author chris
 *
 */
public class OfflineEarnManager
{
	
	private static File offlineDataYml = new File(Main.getInstance().getDataFolder(), "offlinedata.yml");
	
	private static OfflineEarnManager _instance;
	
	public static OfflineEarnManager getInstance()
	{
		if(_instance == null)
			_instance = new OfflineEarnManager();
		return _instance;
	}
	
	public OfflineEarnManager()
	{
		try
		{
			if(!offlineDataYml.exists())
				offlineDataYml.createNewFile();
		} catch (Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "An error occured while trying to create offlinedata.yml");
			e.printStackTrace(CasinoManager.getPrintWriterForDebug());
		}
	}
	/**
	 * add offline earnings 
	 * @param player {@link OfflinePlayer} instance of player
	 * @param amount amount of earning
	 *
	 */
	public void addEarning(OfflinePlayer player, double amount)
	{
		if(player.isOnline()) return;

		synchronized (offlineDataYml)
		{
			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(offlineDataYml);
			String pathToEarnings = player.getUniqueId().toString() + ".earnings";
			if(yaml.isSet(player.getUniqueId().toString()))
			{
				
				if(yaml.isSet(pathToEarnings))
				{
					yaml.set(pathToEarnings, yaml.getDouble(pathToEarnings) + amount);
				}
				else
				{
					yaml.set(pathToEarnings, amount);
				}
			}
			else
			{
				yaml.set(pathToEarnings, amount);
				yaml.set(player.getUniqueId().toString() + ".starttime", System.currentTimeMillis());
			}
			try
			{
				yaml.save(offlineDataYml);
			} catch (IOException e)
			{
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to save offlinedata.yml! " + e.getMessage());
				e.printStackTrace(CasinoManager.getPrintWriterForDebug());
			}
		}	
	}
	
	/**
	 * add offfline losses
	 * @param player {@link OfflinePlayer} instance of player 
	 * @param amount amount of loss
	 */
	public void addLoss(OfflinePlayer player, double amount)
	{
		if(player.isOnline()) return;

		synchronized (offlineDataYml)
		{
			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(offlineDataYml);
			String pathToLosses = player.getUniqueId().toString() + ".losses";
			if(yaml.isSet(player.getUniqueId().toString()))
			{
				if(yaml.isSet(pathToLosses))
				{
					yaml.set(pathToLosses, yaml.getDouble(pathToLosses) + amount);
				}
				else
				{
					yaml.set(pathToLosses, amount);
				}
			}
			else
			{
				yaml.set(pathToLosses, amount);
				yaml.set(player.getUniqueId().toString() + ".starttime", System.currentTimeMillis());
			}
			
			try
			{
				yaml.save(offlineDataYml);
			} catch (IOException e)
			{
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to save offlinedata.yml! " + e.getMessage());
				e.printStackTrace(CasinoManager.getPrintWriterForDebug());
			}
		}
		
	}
	/**
	 * show player his stats while he was offline
	 * @param player
	 */
	public void showPlayerStats(Player player)
	{
		synchronized (offlineDataYml)
		{
			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(offlineDataYml);
			if(!yaml.isSet(player.getUniqueId().toString()))
			{
				//no datas
				return;
			}
			Double earnings = 0.0;
			Double losses = 0.0;
			
			
			String earningspath = player.getUniqueId().toString() + ".earnings";
			String lossespath = player.getUniqueId().toString() + ".losses";
			if(yaml.isSet(earningspath))
			{
				earnings = yaml.getDouble(earningspath);
			}
			if(yaml.isSet(lossespath))
			{
				losses = yaml.getDouble(lossespath);
			}
			
			yaml.set(player.getUniqueId().toString(), null); //delete node to reset
			
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("offlineearnmanager-join-message")
			.replace("%earnings%", Main.econ.format(earnings))
			.replace("%losses%", Main.econ.format(losses)));
			
			if(earnings > losses)
			{
				player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("offlineearnmanager-join-plus")
				.replace("%win%", Main.econ.format(earnings - losses)));
			}
			else if(earnings < losses)
			{
				player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("offlineearnmanager-join-minus")
				.replace("%loss%", Main.econ.format(losses - earnings)));
			} else
			{
				player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("offlineearnmanager-join-equal"));
			}
			try
			{
				yaml.save(offlineDataYml);
			} catch (IOException e)
			{
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to save offlinedata.yml! " + e.getMessage());
				e.printStackTrace(CasinoManager.getPrintWriterForDebug());
			}
		}
	}
	
}
