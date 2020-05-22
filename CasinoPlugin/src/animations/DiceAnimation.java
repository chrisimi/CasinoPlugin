package animations;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.chrisimi.casino.main.Main;
import com.chrisimi.casino.main.MessageManager;

import scripts.CasinoManager;
import scripts.LeaderboardsignsManager;
import scripts.OfflineEarnManager;
import scripts.PlayerSignsManager;
import serializeableClass.PlayerSignsConfiguration;

public class DiceAnimation implements Runnable {

	private Main main;
	private PlayerSignsConfiguration thisSign;
	private Player player;
	private OfflinePlayer owner;
	private Sign sign;
	private PlayerSignsManager signsManager;
	
	private int tasknumber;
	public DiceAnimation(Main main, PlayerSignsConfiguration thisSign, Player player, PlayerSignsManager manager) {
		this.main = main;
		this.thisSign = thisSign;
		this.player = player;
		this.owner = thisSign.getOwner();
		this.sign =  thisSign.getSign();
		this.signsManager = manager;
	}
	
	@Override
	public void run() {
		Main.econ.withdrawPlayer(player, thisSign.bet);
		//Main.econ.depositPlayer(owner, thisSign.bet);
		thisSign.depositOwner(thisSign.bet);
		if(sign == null) {
			main.getLogger().info("Error while trying to start diceanimation! (Sign is null)");
			return;
		}
		
		prepareSign();
			try {
				tasknumber = main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
					int animationCount = 0;
					@Override
					public void run() {
						animate();
						
						if(animationCount >= 15)  {
							endAnimation();
							
							return;
						}
						sign.update(true);
		
						animationCount++;
					}
				}, 5, 8);
			} catch(Exception e) {
				e.printStackTrace();
				CasinoManager.LogWithColor(ChatColor.RED + "An error occured, try to restart the server! If the problems stays, contact the owner of the plugin!");
			}
	} //main run
	
	
	private void endAnimation() {
		double wonamount = thisSign.winMultiplicatorDice() * thisSign.bet;
		main.getServer().getScheduler().cancelTask(tasknumber);
		
		double[] values = thisSign.getWinChancesDice();
		double ergebnis = Double.parseDouble(sign.getLine(1));
		if(ergebnis >= values[0] && ergebnis <= values[1]) {
			
			sign.setLine(2, "§aYOU WON!");
			//player.sendMessage(CasinoManager.getPrefix() + "§aYou won " + Main.econ.format(wonamount));
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("dice-player_won").replace("%amount%", Main.econ.format(wonamount)));
			
			LeaderboardsignsManager.addData(player, thisSign, thisSign.bet, wonamount);
			Main.econ.depositPlayer(player, wonamount);
			//Main.econ.withdrawPlayer(owner, wonamount);
			thisSign.withdrawOwner(wonamount);
			if(!thisSign.isServerOwner() && owner.isOnline()) {
				//owner.getPlayer().sendMessage(CasinoManager.getPrefix() + String.format("§4%s won %s at your Dice sign.", player.getName(), Main.econ.format(wonamount)));
				owner.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("dice-owner-player_won").replace("%playername%", player.getPlayerListName()).replace("%amount%", Main.econ.format(wonamount)));
				
			} else if(!thisSign.isServerOwner()) {
				signsManager.addOfflinePlayerWinOrLose(wonamount * -1, owner);
			}
			
			
			//TEST
			OfflineEarnManager.getInstance().addLoss(owner, wonamount);
			
			
		} else {
			sign.setLine(2, "§4YOU LOST!");
			//player.sendMessage(CasinoManager.getPrefix() + "§4You lost " + Main.econ.format(thisSign.bet));
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("dice-player_lost").replace("%amount%", Main.econ.format(thisSign.bet)));
			
			LeaderboardsignsManager.addData(player, thisSign, thisSign.bet, 0);
			if(!thisSign.isServerOwner() && owner.isOnline()) {
				//owner.getPlayer().sendMessage(CasinoManager.getPrefix() + String.format("§a%s lost %s at your Dice sign.", player.getName(), Main.econ.format(thisSign.bet)));
				owner.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("dice-owner-player_lost").replace("%playername%", player.getPlayerListName()).replace("%amount%", Main.econ.format(thisSign.bet)));
			} else if(!thisSign.isServerOwner()){
				signsManager.addOfflinePlayerWinOrLose(thisSign.bet, owner);
			}
			
			
			
			//TEST
			OfflineEarnManager.getInstance().addEarning(owner, thisSign.bet);
			
		}
		sign.update(true);
		main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
			
			@Override
			public void run() {
				
				thisSign.isRunning = false;
			}
		}, 40);
		
		
	}
	private void prepareSign() {
		sign.setLine(0, "");
		sign.setLine(1, "");
		sign.setLine(2, "");
		sign.setLine(3, "§awin: " + thisSign.plusinformations.split(";")[0]);

		
	}

	private void animate() {
		Random rnd = new Random();
		int rndNumber = rnd.nextInt(100) + 1; //because 0 could come out so that 0 -> 1 99->100 and that 100 is exlusive in method
		sign.setLine(1, String.valueOf(rndNumber));
	}

}
