package com.chrisimi.casinoplugin.serializables;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.google.gson.annotations.Expose;

public class PlayerSignsConfiguration {
	
	/**
	 * all different gamemodes a player sign can be
	 * @author chris
	 *
	 */
	public enum GameMode {
		BLACKJACK,
		DICE,
		SLOTS,
		Blackjack,
		Dice,
		Slots
	}
	
	@Expose
	public GameMode gamemode;
	
	@Expose
	public String ownerUUID;
	
	@Expose
	public Double bet;
	
	@Expose
	public String plusinformations;
	
	@Expose
	public Boolean disabled;
	
	@Expose
	public String worldname;
	@Expose
	public double x;
	@Expose
	public double y;
	@Expose
	public double z;
	
	public int currentSignAnimation = 0;
	public boolean isRunning = false;
	
	
	public PlayerSignsConfiguration() {}
	
	public PlayerSignsConfiguration(Location lrc, GameMode gamemode, Player player, Double bet, String plusInformations) {
		this.gamemode = gamemode;
		this.ownerUUID = player.getUniqueId().toString();
		this.bet = bet;
		this.plusinformations = plusInformations;
		this.worldname = lrc.getWorld().getName();
		this.x = lrc.getX();
		this.y = lrc.getY();
		this.z = lrc.getZ();
		this.disabled = false;
		
		changeEnum();
	}
	public PlayerSignsConfiguration(Location lrc, GameMode gamemode, Double bet, String plusInformations) {
		this.gamemode = gamemode;
		this.ownerUUID = "server";
		this.bet = bet;
		this.plusinformations = plusInformations;
		this.worldname = lrc.getWorld().getName();
		this.x = lrc.getX();
		this.y = lrc.getY();
		this.z = lrc.getZ();
		this.disabled = false;
		
		changeEnum();
	}
	public void changeEnum()
	{
		switch (this.gamemode)
		{
		case Blackjack:
			gamemode = GameMode.BLACKJACK;
			break;
		case Dice:
			gamemode = GameMode.DICE;
			break;
		case Slots:
			gamemode = GameMode.SLOTS;
			break;
		default:
			break;
		}
	}
	
	public boolean unlimitedBet()
	{
		return this.bet == -1.0 || this.plusinformations.contains("-1");
	}
	
	public Location getLocation() {
		return new Location(Bukkit.getWorld(worldname), x, y, z);
	}
	
	public double[] getWinChancesDiceOld() {
		double[] values = new double[2];
		String[] informations = plusinformations.split(";");
		String[] numbers = informations[0].split("-");
		values[0] = Double.parseDouble(numbers[0]);
		values[1] = Double.parseDouble(numbers[1]);
		return values;
	}

	public int[] getWinChancesDice()
	{
		//TODO add check for false values
		int[] values = new int[2];
		String[] informations = plusinformations.split(";");
		String[] numbers = informations[0].split("-");
		values[0] = Integer.parseInt(numbers[0]);
		values[1] = Integer.parseInt(numbers[1]);

		return values;
	}


	public double winMultiplicatorDice() {
		String[] informations = plusinformations.split(";");
		Double value = Double.parseDouble(informations[1].trim());
		return value;
	}
	/**
	 * Get the owner of the sign
	 * @return offlineplayer instance of player, null is owner is server
	 */
	public OfflinePlayer getOwner() {
		if(isServerOwner()) return null;
		return Bukkit.getOfflinePlayer(UUID.fromString(this.ownerUUID));
	}
	public String getOwnerName()
	{
		if(isServerOwner()) return "§6Server";
		else {
			return getOwner().getName();
		}
	}

	public Sign getSign() {
		try 
		{
			Sign sign =  (Sign) Bukkit.getWorld(worldname).getBlockAt(this.getLocation()).getState();
			return sign;
		} catch(ClassCastException e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "There is not a sign for a CasinoSign at: " + this.getLocation().toString());
		}
		return null;
	}
	
	public Boolean isSignDisabled() {
		return disabled;
	}
	public Boolean isSignEnabled() {
		return !(disabled);
	}
	public void enableSign() {
		this.disabled = false;
	}
	public void disableSign() {
		this.disabled = true;
	}


	//region Blackjack

	/**
	 * gets the maximum bet which is valid
	 * if {@link PlayerSignsConfiguration#unlimitedBet} true then it will return the maximum account which the owner can payout
	 * @return {@link Double} value of maxValue
	 */
	public Double blackjackGetMaxBet() 
	{
		if(unlimitedBet())
		{
			double value = (isServerOwner()) ? Double.MAX_VALUE : Main.econ.getBalance(getOwner()) / blackjackGetMultiplicand();
			int rounded = (int) (value * 100.0);
			return (double)rounded / 100.0;
		}
		
		String[] values = this.plusinformations.split(";");
		return Double.valueOf(values[0]);
	}

	public boolean blackjackIsToWriting()
	{
		return this.plusinformations.contains("to");
	}

	public int[] blackjackGetToWriting()
	{
		String[] splits = plusinformations.split(";");
		String[] values = splits[1].split("to");
		int a = Integer.parseInt(values[0]);
		int b = Integer.parseInt(values[1]);
		return new int[] {a, b};
	}

	public Double blackjackGetMinBet() 
	{
		return (unlimitedBet()) ? 1.0 : this.bet;
	}
	
	/**
	 * get the multiplicand from the to writing
	 * @return double value which is the factor
	 */
	public double blackjackGetMultiplicand() {
		if(this.plusinformations.contains("to"))
		{
			String[] values = this.plusinformations.split(";");
			String[] blackjackValues = values[1].split("to");
			Double left = Double.parseDouble(blackjackValues[0]);
			Double right = Double.parseDouble(blackjackValues[1]);
			return (double)left/(double)right;
		}
		String[] values = this.plusinformations.split(";");
		return Double.valueOf(values[1]);
	}
	public double blackjackGetWin(Double bet) {
		return blackjackGetMultiplicand()*bet;
	}

	//endregion

	public Boolean isServerOwner()
	{
		return this.ownerUUID.equalsIgnoreCase("server");
	}

	//region money related methods
	/**
	 * Take money from owner
	 * also work if the owner is not a player
	 * @param amount amount
	 */
	public void withdrawOwner(double amount)
	{
		if(!isServerOwner())
		{
			Main.econ.withdrawPlayer(getOwner(), amount);
		}
	}
	/**
	 * Give owner money
	 * also work if the owner is not a player
	 * @param amount amount
	 */
	public void depositOwner(double amount) 
	{
		if(!isServerOwner())
		{
			Main.econ.depositPlayer(getOwner(), amount);
		}
	}

	/**
	 * check if the owner has enough money, using the highest values
	 * work for every mode
	 * also work for server signs, no need to check
	 * @return true if the owner has enough money, false if not
	 */
	public Boolean hasOwnerEnoughMoney()
	{
		if(isServerOwner()) return true;
		double amountToCheck = 0.0;
		switch(gamemode)
		{
			case DICE:
				amountToCheck = bet * winMultiplicatorDice();
				break;
			case BLACKJACK:
				amountToCheck = blackjackGetMaxBet() * blackjackGetMultiplicand();
				break;
			case SLOTS:
				amountToCheck = getSlotsHighestPayout() * bet;
				break;
		}
		return Main.econ.has(getOwner(), amountToCheck);
	}

	/**
	 * check manually if owner has ... amount of money
	 * also work for server signs, no need to check
	 * @param amount {@link Double} amount to check
	 * @return true fi the owner has enough money, false if not
	 */
	public Boolean hasOwnerEnoughMoney(double amount)
	{
		if(isServerOwner()) return true;
		return Main.econ.has(getOwner(), amount);
	}

	//endregion

	//region Slots

	//A-10-2.5;B-50-3.5;C-40-3.8
	/**
	 * get the symbols from the slot
	 * length is equal to the amount of symbols
	 * @return {@link String} array containing the symbols as string with color codes
	 */
	public String[] getSlotsSymbols()
	{
		String[] symbols = this.plusinformations.split(";");
		String[] values = new String[symbols.length];
		for(int i = 0; i < values.length; i++)
		{
			String[] splited = symbols[i].split("-");
			values[i] = splited[0];
		}
		return values;
	}

	/**
	 * get the multiplicands for all symbols
	 * length is equal to the amount of symbols
	 * @return {@link Double} array containing the weight for every symbol
	 */

	public double[] getSlotsMultiplicators()
	{
		String[] symbols = this.plusinformations.split(";");
		double[] values = new double[symbols.length];
		for(int i = 0; i < values.length; i++)
		{
			String[] splited = symbols[i].split("-");
			values[i] = Double.parseDouble(splited[1]);
		}
		return values;
	}

	/**
	 * get the weight for all symbols
	 * length is equal to the amount of symbols
	 * @return {@link Double} array containing the weight for every symbol
	 */
	public double[] getSlotsWeight()
	{
		String[] symbols = this.plusinformations.split(";");
		double[] values = new double[symbols.length];
		for(int i = 0; i < values.length; i++)
		{
			String[] splited = symbols[i].split("-");
			values[i] = Double.parseDouble(splited[2]);
		}
		return values;
	}

	public double getSlotsWeightSum()
	{
		double[] weights = getSlotsWeight();
		double sumWeight = 0.0;
		for(int i = 0; i < weights.length; i++)
			sumWeight += weights[i];

		return sumWeight;
	}

	/**
	 * get the highest payout for the slot
	 * @return {@link Double} number which is the highest win for the slot
	 */
	public double getSlotsHighestPayout()
	{
		double[] multiplicators = getSlotsMultiplicators();
		double highestMulti = 0.0;
		for(int i = 0; i < multiplicators.length; i++)
		{
			if(highestMulti < multiplicators[0])
				highestMulti = multiplicators[0];
		}
		return this.bet * highestMulti + bet;
	}

	/**
	 * get the color codes which could be in the symbols for valid equals check
	 * length is equal to the amount of symbols
	 * @return String array containing the color code with paragraph '§' or empty if there isn't a color code
	 */
	public String[] getColorCodesSlots()
	{
		String[] symbols = getSlotsSymbols();
		String[] colors = new String[symbols.length];

		for(int i = 0; i < symbols.length; i++)
		{
			if(symbols[i].contains("§"))
			{
				int index = symbols[i].indexOf("§");

				char a;
				//check if there are two paragraph symbols to see if it is a color code or not
				//if yes add color code WITH paragraph symbol to the string array
				if((a = symbols[i].charAt(index + 1)) != '§')
					colors[i] = "§" + a;
				else
					symbols[i] = "";
			}
		}
		return colors;
	}
	//endregion
}
