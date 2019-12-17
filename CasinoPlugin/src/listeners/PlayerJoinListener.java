package listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.chrisimi.casino.main.Main;
import com.chrisimi.casino.main.VersionManager;

import scripts.CasinoManager;
import scripts.PlayerSignsManager;

public class PlayerJoinListener implements Listener {

	private Main main;
	public PlayerJoinListener(Main main) {
		this.main = main;
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(event.getPlayer().isOp() && Main.isConfigUpdated == false) {
		
			event.getPlayer().sendMessage(
					String.format("%s§4there is a new version for the config.yml of this plugin... please backup your config.yml and use §6/casino updateconfig", CasinoManager.getPrefix()));
			
		} if(event.getPlayer().isOp() && Main.isPluginUpdated == false) {
			event.getPlayer().sendMessage(String.format("%s" + 
					"Version %s is out! (you are running now version %s) Update now on: https://www.spigotmc.org/resources/casino-plugin.71898/ \n\n", CasinoManager.getPrefix(), VersionManager.newestVersion, Main.pluginVersion));
			
		}
		
		
		if(PlayerSignsManager.playerWonWhileOffline.containsKey(event.getPlayer())) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + String.format("While you were offline, your Casino-Signs brought you in: %s", Main.econ.format(PlayerSignsManager.playerWonWhileOffline.get(event.getPlayer()))));
			PlayerSignsManager.playerWonWhileOffline.remove(event.getPlayer());
		}
	}
}
