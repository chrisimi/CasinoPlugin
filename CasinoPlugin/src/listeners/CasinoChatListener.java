package listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import com.chrisimi.casino.main.Main;

import animations.BlackjackAnimation;

public class CasinoChatListener implements Listener {

	private Main main;
	public CasinoChatListener(Main main) {
		this.main = main;
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if(BlackjackAnimation.IsBlackJackAnimationWaitingForUserInput(player)) {
			main.getServer().getScheduler().runTask(main, new Runnable() {
				@Override
				public void run() {
					BlackjackAnimation.userInput(event.getMessage(), player);
					
				}
			});
			event.setCancelled(true);
		}
	}
}
