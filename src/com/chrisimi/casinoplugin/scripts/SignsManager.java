package com.chrisimi.casinoplugin.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.serializables.SignConfiguration;
import com.chrisimi.casinoplugin.serializables.Signs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import net.milkbowl.vault.economy.EconomyResponse;


public class SignsManager implements Listener {

	public static int playCount = 0;
	
	private static HashMap<Sign, Integer> signTasks = new HashMap<Sign, Integer>(); //values with task
	
	private static HashMap<Location, Double> signValues = new HashMap<Location, Double>(); //values with bet werte serialisieren
	
	private Material[] signs = new Material[] {Material.SIGN, Material.SIGN};
	

	
	private GsonBuilder builder;
	private Gson gson;
	
	private static Integer minDuration;
	private static Integer maxDuration;
	private static ArrayList<String> possibilities;
	private static ArrayList<Double> multiplicators;
	private static ArrayList<Double> chances;
	private static double maxBet;

	public SignsManager() {
		main;
		
		builder = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().serializeNulls();
		gson = builder.create();
		
		updateVariables();
		
		Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
		
		importSignsYml();
	}
	
	@SuppressWarnings("unchecked")
	private void updateVariables() {
		try {
			minDuration = Integer.parseInt(UpdateManager.getValue("sign-min-duration").toString());
		} catch(Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get min-duration for Casino-Signs! Value have to be a valid integer! Set to default value: 30");
			minDuration = 30;
		}
		
		try {
			maxDuration = Integer.parseInt(UpdateManager.getValue("sign-max-duration").toString());
		} catch(Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get max-duration for Casino-Signs! Value have to be a valid integer! Set to default value: 50");
			maxDuration = 50;
		}
		
		try {
			possibilities = (ArrayList<String>) UpdateManager.getValue("sign-possibilities", new ArrayList<String>());
			
			if(possibilities.size() != 3) throw new Exception("The size of the list isn't 3!");
			
		} catch(Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get possibilities for Casino-Signs (" + e.getMessage() + ")! Value have to be a valid list of 3 elements like [R, D, E]! Set to default value: [R, D, E]");
			ArrayList<String> list = new ArrayList<>();
			list.add("R");
			list.add("D");
			list.add("E");
			possibilities = list;
		}
			
		try {
			multiplicators = (ArrayList<Double>) UpdateManager.getValue("sign-multiplicator", new ArrayList<Double>());
			
			if(multiplicators.size() != 3) throw new Exception("The size of the list isn't 3!");
			
		} catch(Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get multiplicator for Casino-Signs (" + e.getMessage() + ") ! Value have to be a list with 3 valid decimal values! Set to default value: [3.0, 5.0, 7.0]!");
			ArrayList<Double> list = new ArrayList<>();
			list.add(3.0);
			list.add(5.0);
			list.add(7.0);
			multiplicators = list;
		}
		
		try 
		{
			chances = (ArrayList<Double>) UpdateManager.getValue("sign-chance", new ArrayList<Double>());
			if(chances.size() == 0) throw new Exception("You probably forgot to update config.yml! Try /casino updateconfig to fix this problem!");
			
			if(chances.size() != 3) throw new Exception("The size of the list isn't 3!");
			
			if(chances.get(0) + chances.get(1) + chances.get(2) != 100) throw new Exception("Sum of all 3 values isn't 3!");
			
		} catch(Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get chance for Casino-signs (" + e.getMessage() + ") ! Value have to be a list with 3 valid decimal values and should be together 100! Set to default value: [50.0, 30.0, 20.0]!");
			ArrayList<Double> list = new ArrayList<>();
			list.add(50.0);
			list.add(30.0);
			list.add(20.0);
			chances = list;
		}
		
		
		try {
			maxBet = Double.parseDouble(UpdateManager.getValue("sign-max-bet").toString());
		} catch(Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get max-bet for Casino-Signs! Value have to be a valid decimal value! Set to default value: 1000.0!");
			maxBet = 1000.0;
		}
		
		
	}
	
	private Boolean registerNewSign(Sign sign, Double value) {
		signValues.put(sign.getLocation(), value);
		updateSignsYml();
		return true;
	}
	
	private void updateSignsYml() { //export all sign configurations to signs yml file to save them
		BufferedWriter writer;
		Signs signs = new Signs();
		
		for(Entry<Location, Double> entry : signValues.entrySet()) {
			SignConfiguration sc = new SignConfiguration(entry.getKey(), entry.getValue());
			signs.signs.add(sc);
		}
		
		try {
			writer = new BufferedWriter(new FileWriter(Main.signsYml));
			writer.write("");
			writer.write(gson.toJson(signs, Signs.class));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(CasinoManager.configEnableConsoleMessages)
			CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully exported " + signs.signs.size() + " signs to signs.json");
		
		
		
	}
	
	private void importSignsYml() { //import all sign configurations from signs json file
		
		//input string from file
		String line = "";
		String jsonString = "";
		try {
			FileReader fileReader = new FileReader(Main.signsYml);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) != null) {
				jsonString += line;
			}
			bufferedReader.close();
			fileReader.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		if(jsonString.length() < 25) {
			
			if(CasinoManager.configEnableConsoleMessages)
				CasinoManager.LogWithColor(ChatColor.YELLOW + "No CasinoSigns to import!");
			return;
		}
		ArrayList<SignConfiguration> signs = null;
		try {
			 signs = gson.fromJson(jsonString, Signs.class).signs;
		} catch(JsonSyntaxException jse) {
			CasinoManager.LogWithColor(ChatColor.RED + "An Error occured while trying to import CasinoSigns from json: Invalid Json file!");
			CasinoManager.LogWithColor(ChatColor.BLUE + "2 things you can do:\n1. check the json file on your own after errors or use https://jsonlint.com \n2. SAVE! the json file with an other name and let the plugin create a new json file!");
			CasinoManager.LogWithColor(ChatColor.RED + "Closing Server because of an fatal error!");
			Bukkit.shutdown();
			return;
		}
		if(signs == null) {
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to import signs from yml: sign is null?");
		} else {
				
			for(SignConfiguration cnf : signs) {
				try {
					if(cnf == null) throw new NullPointerException();
					
					signValues.put(cnf.getLocation(), cnf.bet);
					
				} catch(NullPointerException npe) {
					CasinoManager.LogWithColor(ChatColor.RED + "Damaged CasinoSign found! Data will be deleted! Code: NullPointerException!");
					signs.remove(cnf);
				}
				catch(Exception e) {
					CasinoManager.LogWithColor(ChatColor.RED + "Damaged CasinoSign found! Data will be deleted! Code: Unknown Exception!");
					e.printStackTrace();
					signs.remove(cnf);
				}
			}
			
			if(CasinoManager.configEnableConsoleMessages)
				CasinoManager.LogWithColor(ChatColor.GREEN + "imported " + signs.size() + " signs from signs.json");
			
		}
	}
	
	@EventHandler
	public void onSignPlace(SignChangeEvent event) {
		if(event.getLine(0) == null) {
			return;
		}
		if(event.getLine(1).equalsIgnoreCase("dice") || event.getLine(1).equalsIgnoreCase("blackjack")) {
			return;
		}
		
		
		if(!(event.getLine(0).contains("casino"))) {
			return;
		}
		if(!(Main.perm.has(event.getPlayer(), "casino.sign.create") || event.getPlayer().isOp() || Main.perm.has(event.getPlayer(), "casino.admin"))) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("commands-player_no_permission"));
			return;
		}
		
		
		if(event.getLine(1) == null || event.getLine(1).equals("")) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("casinosigns-creation_no_bet"));
			return;
		}
		Double bet;
		try {
			 bet = Double.parseDouble(event.getLine(1).trim());
		} catch(NumberFormatException e) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("casinosigns-creation_bet_not_valid"));
			return;
		}
		
		if(bet > maxBet) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("casinosigns-creation_bet_higher_than_max_bet"));
			return;
		}
		
		Boolean success = registerNewSign((Sign) event.getBlock().getState(), bet);
		if(success) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("casinosigns-creation-success"));
			Sign sign = (Sign) event.getBlock().getState();
			signToNormal(sign, bet);
		} 
	}
	@EventHandler
	public void onSignBreak(BlockBreakEvent event) {
		
		String blockname = event.getBlock().getType().toString();
		if(blockname.contains("SIGN")) {
			Sign sign = (Sign) event.getBlock().getState();
			if(sign == null) {
				return;
			}
		} else {
			return;
		}
		
		
		
		
		
		if(signValues.containsKey(event.getBlock().getLocation())) {
			if(!(Main.perm.has(event.getPlayer(), "casino.sign.break") || event.getPlayer().isOp() || Main.perm.has(event.getPlayer(), "casino.admin"))) {
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("casinosigns-no_permission-break"));
				event.setCancelled(true);
				return;
			}
			//is a sign from the list
			signValues.remove(event.getBlock().getLocation());
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("casinosigns-break-success"));
			this.updateSignsYml();
		}
		
	}
	
	@EventHandler
	public void onSignClick(PlayerInteractEvent event) {
		if(!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
		
		Sign sign = null;
		if(event.getClickedBlock().getType().toString().contains("SIGN")) {
			sign = (Sign) event.getClickedBlock().getState();
		} else {
			return;
		}
		
		//zuerest schauen ob es ein valid sign is und dann sofort in der Liste schauen ob das Sign eingetragen gibt!
		
		
		
		if(signTasks.containsKey(sign)) {
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("casinosigns-sign_ingame"));
			return;
		}
		
		
		if(signValues.containsKey(sign.getLocation())) {
			if(!(Main.perm.has(event.getPlayer(), "casino.sign.use") || event.getPlayer().isOp() || Main.perm.has(event.getPlayer(), "casino.admin"))) {
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + "§4You don't have permissions to use a Casino-Sign!");
				return;
			}
			
			if(Main.econ.has(event.getPlayer(), signValues.get(sign.getLocation()))) {
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("casinosigns-playing").replace("%amount%", Main.econ.format(signValues.get(sign.getLocation()))));
				Main.econ.withdrawPlayer(event.getPlayer(), signValues.get(sign.getLocation()));
				playAnimationServerSigns(sign, signValues.get(sign.getLocation()), event.getPlayer());
				
			} else {
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("casinosigns-not_enough_money"));
			}
		} 
			
			
	
	}

	private void playAnimationServerSigns(Sign sign, Double bet, Player player) {
		playCount++;
		
		int taskint = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {
			String wonSymbol = "";
			int wonSymbolIndex = 0;
			
			Random rnd = new Random();
			int rolls = (int) (rnd.nextInt((int) Math.round(maxDuration - minDuration)) + minDuration);
			boolean finished = false;
			
			@Override
			public void run() {
				if(finished) {
					return;
				}
				HashMap<Integer, String> lines = new HashMap<Integer, String>();
				if(sign.getLine(0).length() > 10) {
					
					for(int i = 0; i < 4; i++) { //prepareSign
						
						String lineText = "";
						for(int r = 0; r < 3; r++) {
							
							int randomZahl = rnd.nextInt(99) + 1;
							
							if(randomZahl < chances.get(0)) randomZahl = 0;
							else if(randomZahl < chances.get(0) + chances.get(1)) randomZahl = 1;
							else randomZahl = 2;
							
							
							lineText += possibilities.get(randomZahl) + " ";
						}
						
						sign.setLine(i, lineText);
					}
					
					
				} else {
					
					for(int i = 0; i < 4; i++) { //readSign
						lines.put(i, sign.getLine(i));
					}
					
					for(int line = 0; line < 4; line++) {
						
						for(Entry<Integer, String> entry : lines.entrySet()) {
							int currentline = entry.getKey() + 1;
							if(currentline < 4) {
								sign.setLine(currentline, entry.getValue());
							} else {
								sign.setLine(0, entry.getValue());
							}
							
						}
					}
				}
				rolls--;
				if(rolls <= 0) {
					finished = true;
					int wonrow = rnd.nextInt(4);
					
					
					String wonrowString = sign.getLine(wonrow);
					String[] wonrowFields = sign.getLine(wonrow).split(" ");
					
					sign.setLine(wonrow, ">   " + wonrowString + "   <");
					
					
					for(int i = 0; i < 3; i++) {
						if(wonrowFields[0].equals(possibilities.get(i)) && wonrowFields[1].equals(possibilities.get(i)) && wonrowFields[2].equals(possibilities.get(i))) {
							wonSymbol = possibilities.get(i);
							wonSymbolIndex = i;
						}
					}
					sign.update(true);
					Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {

						@Override
						public void run() {
							sign.setLine(0, CasinoManager.getPrefix());
							sign.setLine(1, player.getDisplayName());
							sign.setLine(2, wonrowString);
							if(wonSymbol == "") {
								sign.setLine(3, "§4You won nothing!");
								
							} else {
								double wonamount = bet;
								switch(wonSymbolIndex) {
								case 0:
									wonamount *= multiplicators.get(0);
									break;
								case 1:
									wonamount *= multiplicators.get(1);
									break;
								case 2:
									wonamount *= multiplicators.get(2);
									break;
								}
								sign.setLine(3, "§3You won: " + wonamount);
								EconomyResponse r = Main.econ.depositPlayer(player, wonamount);
								if(r.transactionSuccess()) {
									player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("casinosigns-player_won").replace("%amount%", Main.econ.format(wonamount)).replace("%balance%",Main.econ.format(Main.econ.getBalance(player))));
								} else {
									player.sendMessage(String.format(CasinoManager.getPrefix() + "An error occured: %s", r.errorMessage));
								}
							}
							
							sign.update(true);
							Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
								@Override
								public void run() {
									signToNormal(sign, bet);
									SignsManager.animationFinished(sign, bet);
								}
							}, 60);
							
						}
						
					}, 40);
					
					
					
					
				
				}
				sign.update();
			}
			
			
			
		}, 0, 3);
		signTasks.put(sign, taskint);
		
		
		
		
	}
	
	private void signToNormal(Sign sign, Double bet) { //normal sign layout
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {

			@Override
			public void run() {
				sign.setLine(0, CasinoManager.getPrefix());
				sign.setLine(1, "§5bet: " + Main.econ.format(bet));
				sign.setLine(2, "§aright click");
				sign.setLine(3, "§ato begin");
				
				Boolean a = sign.update(true);
				sign.update();
				
				
			}
			
		}, 20);
		
	}
	
	
	public static void animationFinished(Sign sign, Double bet) {
		if(signTasks.containsKey(sign)) {
			Bukkit.getScheduler().cancelTask(signTasks.get(sign));
			signTasks.remove(sign);
		}
	}


	public void reload() {
		updateVariables();
		
	}

}
