package com.chrisimi.casinoplugin.scripts;

import java.util.Collection;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;

//initialized by CasinoManager
public class RollCommand {

	public static int rollAmount = 0;
	private static int playerRange;
	

	public RollCommand(Main main) {
		main;
		configureVariables();
	}
	
	public void reload() {
		configureVariables();
	}
	
	public static void roll(Player player, String[] args) {
		rollAmount++;
		int minimum = 0;
		int maximum = 0;
		try {
			minimum = Integer.parseInt(args[1]);
		} catch(NumberFormatException e) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("roll-min_value_invalid"));
			return;
		}
		try {
			maximum = Integer.parseInt(args[2]);
		} catch(NumberFormatException e) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("roll-max_value_invalid"));
			return;
		}
		if(minimum > maximum) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("roll-min_max_diff"));
			return;
		}
		Random rnd = new Random();
		int randomZahl = rnd.nextInt(maximum-minimum+1) + minimum;
		
		if(args.length == 3) { //er hat keinen Spieler angegeben!
			Collection<? extends Player> currentPlayer = Bukkit.getServer().getOnlinePlayers();
			for(Player p : currentPlayer) {
				if(p.getWorld().equals(player.getWorld())) {
					if(p.getLocation().distance(player.getLocation()) < (double)playerRange) {
						p.sendMessage(CasinoManager.getPrefix() + MessageManager.get("roll-rolled_message").replace("%playername%", player.getName()).replace("%min%", String.valueOf(minimum)).replace("%max%", String.valueOf(maximum)).replace("%result%", String.valueOf(randomZahl)));
					}
				}
			}
			
		} else { //er hat einen Spieler angegeben
			Player angegebenerSpieler = Bukkit.getPlayer(args[3]);
			if(angegebenerSpieler == null) {
				player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("roll-player_doesnt_exist"));
				return;
			} else {
				angegebenerSpieler.sendMessage(CasinoManager.getPrefix() + MessageManager.get("roll-rolled_message").replace("%playername%", player.getName()).replace("%min%", String.valueOf(minimum)).replace("%max%", String.valueOf(maximum)).replace("%result%", String.valueOf(randomZahl)));
			}
		}
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("roll-player_roll").replace("%result%", String.valueOf(randomZahl)));
	}
	
	
	private void configureVariables() {
		try {
			playerRange = Integer.parseInt(UpdateManager.getValue("rollcommand-range").toString());
		} catch(NumberFormatException e) {
			main.getLogger().warning("roll command range is not a valid value!!! it have to be a full number like 2 or 3!");
		} finally {
			playerRange = 30;
		}
		
	}
	
}
