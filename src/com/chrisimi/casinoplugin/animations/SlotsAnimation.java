package com.chrisimi.casinoplugin.animations;

import java.util.Random;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.LeaderboardsignsManager;
import com.chrisimi.casinoplugin.scripts.OfflineEarnManager;
import com.chrisimi.casinoplugin.scripts.PlayerSignsManager;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;

public class SlotsAnimation implements Runnable
{
	private PlayerSignsConfiguration thisSign = new PlayerSignsConfiguration();
	private final Player player;
	private final OfflinePlayer owner;
	private final PlayerSignsManager manager;
	private Sign sign;

	private Random random = new Random();
	private int rollsLeft = 0;
	private int bukkitTaskId = 0;
	private double winAmount = 0;
	
	public SlotsAnimation(PlayerSignsConfiguration thisSign, Player player, PlayerSignsManager manager)
	{
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
		rollsLeft = random.nextInt(20) + 2;
		
		for(int i = 0; i < 4; i++)
		{
			sign.setLine(i, "");
		}
		sign.update(true);
		
		bukkitTaskId = Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), animation, 0, 5L);
	}

	//main animation runnable which will be called every update
	Runnable animation = new Runnable()
	{
		
		
		@Override
		public void run()
		{
			//move every line to the next one 1 -> 2 etc.
			//ignoring the first line because a new line will be generated there
			String[] lines = sign.getLines();
			
			for(int i = 2; i >= 0; i--)
			{
				lines[i+1] = lines[i];
			}

			//generate the new line
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
				Main.getInstance().getServer().getScheduler().cancelTask(bukkitTaskId);
				
				Main.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable()
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

		String[] symbols = thisSign.getSlotsSymbols();
		String[] colorCodes = thisSign.getColorCodesSlots();
		double[] weights = thisSign.getSlotsWeight();

		/*
		//for 3 elements on the display
		for(int i = 0; i < 3; i++)
		{

			int randomNumber = random.nextInt((int) (thisSign.getSlotsWeightSum())) + 1;

			//get the character from the weight sum
			double weightSum = 0.0;
			for(int j = 0; j < symbols.length; j++)
			{
				weightSum += weights[i];

				if(randomNumber < weightSum)
				{
					newCharacter = colorCodes[i] + symbols[i];
					break;
				}
			}
			
			if(i == 0)
				newLine = newCharacter;
			else if(i == 1)
				newLine += " " + newCharacter;
			else if(i == 2)
				newLine += " " + newCharacter;
		}
		*/

		//for 3 element on the sign
		for(int i = 0; i < 3; i++)
		{
			double randomNum = random.nextDouble() * thisSign.getSlotsWeightSum();

			//iterate through all symbols weight to find out which symbol has the correct weight
			double sumWeight = 0.0;
			for(int j = 0; j < symbols.length; j++)
			{
				sumWeight += weights[j];
				if(randomNum < sumWeight)
				{
					newCharacter = colorCodes[j] + symbols[j];
					break;
				}
			}

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
			for(int i = 0; i < thisSign.getSlotsSymbols().length; i++)
			{
				if(symbStrings[1].equals(thisSign.getColorCodesSlots()[i] + thisSign.getSlotsSymbols()[i]))
				{
					winAmount = thisSign.bet * thisSign.getSlotsMultiplicators()[i];
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
		if(!thisSign.isServerOwner() && !owner.isOnline())
		{
			OfflineEarnManager.getInstance().addEarning(owner, thisSign.bet);
		}
		
		
		finish();
	}
	private void playerWon()
	{
		LeaderboardsignsManager.addData(player, thisSign, thisSign.bet, winAmount);
		if(!thisSign.isServerOwner() && owner.isOnline())
			owner.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("playersigns-slots-player_won").replace("%playername%", player.getDisplayName()));
		Main.econ.depositPlayer(player, winAmount + thisSign.bet);
		thisSign.withdrawOwner(winAmount + thisSign.bet);
		
		
		
		if(!thisSign.isServerOwner() && !owner.isOnline())
		{
			OfflineEarnManager.getInstance().addLoss(owner, winAmount + thisSign.bet);
		}
		
		finish();
	}
	private void finish()
	{
		Main.getInstance().getServer().getScheduler().cancelTask(bukkitTaskId);
		Main.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable()
		{
			
			@Override
			public void run()
			{
				thisSign.isRunning = false;
			}
		}, 20*6L);
		
	}
}
