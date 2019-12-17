package scripts;

import java.util.Collection;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.chrisimi.casino.main.Main;

//initialized by CasinoManager
public class RollCommand {

	public static int rollAmount = 0;
	private static int playerRange;
	
	private Main main;
	public RollCommand(Main main) {
		this.main = main;
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
			player.sendMessage(CasinoManager.getPrefix() + "§4The minimum value is not valid!");
			return;
		}
		try {
			maximum = Integer.parseInt(args[2]);
		} catch(NumberFormatException e) {
			player.sendMessage(CasinoManager.getPrefix() + "§4The maximum value is not valid!");
			return;
		}
		if(minimum > maximum) {
			player.sendMessage(CasinoManager.getPrefix() + "§4The minium value have to be higher than the maximum value!");
			return;
		}
		Random rnd = new Random();
		int randomZahl = rnd.nextInt(maximum-minimum+1) + minimum;
		
		if(args.length == 3) { //er hat keinen Spieler angegeben!
			Collection<? extends Player> currentPlayer = Bukkit.getServer().getOnlinePlayers();
			for(Player p : currentPlayer) {
				if(p.getWorld().equals(player.getWorld())) {
					if(p.getLocation().distance(player.getLocation()) < (double)playerRange) {
						p.sendMessage(CasinoManager.getPrefix() + String.format("%s rolled from %s to %s and got: §0%s", player.getName(), minimum, maximum, randomZahl));
					}
				}
			}
			
		} else { //er hat einen Spieler angegeben
			Player angegebenerSpieler = Bukkit.getPlayer(args[3]);
			if(angegebenerSpieler == null) {
				player.sendMessage(CasinoManager.getPrefix() + "§4This player does not exist on this server or he is not online!");
				return;
			} else {
				angegebenerSpieler.sendMessage(CasinoManager.getPrefix() + String.format("%s rolled from %s to %s and got: §e§l%s", player.getName(), minimum, maximum, randomZahl));
			}
		}
		player.sendMessage(CasinoManager.getPrefix() + "You rolled §e§l" + randomZahl);
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
