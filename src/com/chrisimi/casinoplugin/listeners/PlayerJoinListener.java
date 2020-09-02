package com.chrisimi.casinoplugin.listeners;

import com.chrisimi.casinoplugin.scripts.UpdateManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.main.VersionManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.OfflineEarnManager;
import com.chrisimi.casinoplugin.scripts.PlayerSignsManager;

public class PlayerJoinListener implements Listener {


	public PlayerJoinListener(Main main) {
		main;
		Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
	}
	
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		if(event.getPlayer().isOp() || Main.perm.has(event.getPlayer(), "casino.admin"))
		{
			switch(Main.result.getStatus())
			{

				case OUTDATED:
					event.getPlayer().sendMessage(String.format(CasinoManager.getPrefix() + "Version %s is out! Update now on: https://www.spigotmc.org/resources/casino-plugin.71898/ \n\n", Main.result.getSpigotPluginVersion()));
					break;
				case UP_TO_DATE:
				case ERROR:
				default:
					break;
			}

			if(!Main.result.getSpigotPluginVersion().equals(UpdateManager.getValue("version", Main.result.getLocalPluginVersion())))
			{
				System.out.println(UpdateManager.getValue("version", Main.result.getLocalPluginVersion()));
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + "§4There is a new version for the config.yml... please backup your config.yml and use §6/casino updateconfig");
			}
		}
		
		if(PlayerSignsManager.playerWonWhileOffline.containsKey(event.getPlayer())) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("player-join-message").replace("%amount%", Main.econ.format(PlayerSignsManager.playerWonWhileOffline.get(event.getPlayer()))));
			PlayerSignsManager.playerWonWhileOffline.remove(event.getPlayer());
		}
		
		OfflineEarnManager.getInstance().showPlayerStats(event.getPlayer());
	}
}
