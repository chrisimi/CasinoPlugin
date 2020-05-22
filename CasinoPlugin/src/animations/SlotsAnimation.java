package animations;

import java.util.Random;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.chrisimi.casino.main.Main;
import com.chrisimi.casino.main.MessageManager;

import scripts.CasinoManager;
import scripts.LeaderboardsignsManager;
import scripts.PlayerSignsManager;
import serializeableClass.PlayerSignsConfiguration;

public class SlotsAnimation implements Runnable
{
	private final Main main;
	private PlayerSignsConfiguration thisSign = new PlayerSignsConfiguration();
	private final Player player;
	private final OfflinePlayer owner;
	private final PlayerSignsManager manager;
	private Sign sign;
	
	private String[] symbols;
	private double[] multiplicators;
	private double[] weights;
	private Random random = new Random();
	private int rollsLeft = 0;
	private int bukkitTaskId = 0;
	private double winAmount = 0;
	
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
		startAnimation();
	}
	private void startAnimation()
	{
		symbols = thisSign.getSlotsSymbols();
		multiplicators = thisSign.getSlotsMultiplicators();
		weights = thisSign.getSlotsWeight();
		rollsLeft = random.nextInt(20) + 2;
		
		for(int i = 0; i < 4; i++)
		{
			sign.setLine(i, "");
		}
		sign.update(true);
		
		bukkitTaskId = main.getServer().getScheduler().scheduleSyncRepeatingTask(main, animation, 0, 5L);
	}
	Runnable animation = new Runnable()
	{
		
		
		@Override
		public void run()
		{
			String[] lines = sign.getLines();
			
			for(int i = 2; i >= 0; i--)
			{
				lines[i+1] = lines[i];
			}
			
			String newLine = generateNewLine();
			lines[0] = newLine;
			
			for(int i = 0; i < 4; i++)
			{
				sign.setLine(i, lines[i]);
			}
			
			sign.update(true);
			//
			if(rollsLeft < 0)
			{
				int line = random.nextInt(4);
				String editedLine = "> " + lines[line] + " §0<";
				sign.setLine(line, editedLine);
				sign.update(true);
				main.getServer().getScheduler().cancelTask(bukkitTaskId);
				
				main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable()
				{
					
					@Override
					public void run()
					{
						if(checkIfPlayerWon(lines[line]))
						{
							
							sign.setLine(0, editedLine);
							sign.setLine(1, "");
							sign.setLine(2, "§2You won");
							sign.setLine(3, Main.econ.format(winAmount));
							sign.update(true);
							playerWon();
						} else
						{
							sign.setLine(0, editedLine);
							sign.setLine(1, "");
							sign.setLine(2, "§4You lost");
							sign.setLine(3, "");
							sign.update(true);
							playerLost();
						}
					}
				}, 60L);
				
				
				
				
			}
			rollsLeft--;
		}
	};
	private String generateNewLine()
	{
		String newLine = "";
		String newCharacter = "";
		for(int i = 0; i < 3; i++)
		{
			int randomNumber = random.nextInt(100) + 1;
			
			if(randomNumber < weights[0])
			{
				newCharacter = "§b" + symbols[0];
			} else if(randomNumber < weights[0] + weights[1])
			{
				newCharacter = "§a" + symbols[1];
			} else if(randomNumber <= weights[0] + weights[1] + weights[2])
			{
				newCharacter = "§c" + symbols[2];
			}
			else {
				newCharacter = "Error";
			}
			
			if(i == 0)
				newLine = newCharacter;
			else if(i == 1)
				newLine += " " + newCharacter;
			else if(i == 2)
				newLine += " " + newCharacter;
		}
		
		
		return newLine;
	}
	private Boolean checkIfPlayerWon(String line)
	{
		
		String[] symbStrings = line.split(" ");
		CasinoManager.Debug(this.getClass(), line);
		CasinoManager.Debug(this.getClass(), String.format("(%s) (%s) (%s)", symbStrings[1], symbStrings[2], symbStrings[3]));
		CasinoManager.Debug(this.getClass(), String.valueOf(symbStrings[1].equals(symbStrings[2])));
		CasinoManager.Debug(this.getClass(), String.valueOf(symbStrings[2].equals(symbStrings[3])));
		CasinoManager.Debug(this.getClass(), String.valueOf(symbStrings[1].equals(symbStrings[3])));
		if(symbStrings[1].equals(symbStrings[2]) && symbStrings[1].equals(symbStrings[3]))
		{
			for(int i = 0; i < 3; i++)
			{
				if(symbStrings[1].equals(thisSign.getColorMultiplicators()[i] + symbols[i]))
				{
					winAmount = thisSign.bet * multiplicators[i];
					break;
				}
			}
		}
		return winAmount != 0;
	}
	
	private void playerLost()
	{
		LeaderboardsignsManager.addData(player, thisSign, thisSign.bet, 0.0);
		if(!thisSign.isServerOwner() && owner.isOnline())
			owner.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-slots-player_lost").replace("%playername%", player.getDisplayName()));
		finish();
	}
	private void playerWon()
	{
		LeaderboardsignsManager.addData(player, thisSign, thisSign.bet, winAmount);
		if(!thisSign.isServerOwner() && owner.isOnline())
			owner.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-slots-player_won").replace("%playername%", player.getDisplayName()));
		Main.econ.depositPlayer(player, winAmount + thisSign.bet);
		thisSign.withdrawOwner(winAmount + thisSign.bet);
		finish();
	}
	private void finish()
	{
		main.getServer().getScheduler().cancelTask(bukkitTaskId);
		main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable()
		{
			
			@Override
			public void run()
			{
				thisSign.isRunning = false;
			}
		}, 20*6L);
		
	}
}
