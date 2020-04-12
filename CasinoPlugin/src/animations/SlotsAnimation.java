package animations;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.chrisimi.casino.main.Main;
import com.chrisimi.casino.main.MessageManager;

import scripts.CasinoManager;
import scripts.PlayerSignsManager;
import serializeableClass.PlayerSignsConfiguration;

public class SlotsAnimation implements Runnable
{
	private final Main main;
	private final PlayerSignsConfiguration thisSign;
	private final Player player;
	private final OfflinePlayer owner;
	private final PlayerSignsManager manager;
	private Sign sign;
	
	
	
	public SlotsAnimation(Main main, PlayerSignsConfiguration thisSign, Player player, PlayerSignsManager manager)
	{
		this.main = main;
		this.thisSign = thisSign;
		this.player = player;
		this.owner = thisSign.getOwner();
		this.manager = manager;
		this.sign = thisSign.getSign();
		
	}




	@Override
	public void run()
	{
		start();
		
	}
	private void start()
	{
		double bet = thisSign.bet;
		
		Main.econ.withdrawPlayer(player, bet);
		thisSign.depositOwner(bet);
		
		if(!thisSign.isServerOwner() && owner.isOnline())
			owner.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-slots-owner_message_player_is_playing").replace("%playername%", player.getName()).replace("%money%", Main.econ.format(bet)));
	}
}
