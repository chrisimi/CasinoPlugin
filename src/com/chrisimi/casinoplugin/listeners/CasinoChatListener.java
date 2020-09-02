package com.chrisimi.casinoplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import com.chrisimi.casinoplugin.animations.BlackjackAnimation;
import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.inventoryapi.InventoryAPI;

public class CasinoChatListener implements Listener {


	public CasinoChatListener() {
		Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		
		Player player = event.getPlayer();
		if(BlackjackAnimation.IsBlackJackAnimationWaitingForUserInput(player)) {
			Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(), new Runnable() {
				@Override
				public void run() {
					BlackjackAnimation.userInput(event.getMessage(), player);
					
				}
			});
			event.setCancelled(true);
		}
	}
}
