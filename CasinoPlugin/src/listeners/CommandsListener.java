package listeners;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.chrisimi.casino.main.Main;
import com.chrisimi.casino.main.MessageManager;

import scripts.CasinoGUI;
import scripts.CasinoManager;
import scripts.LeaderboardsignsManager;
import scripts.PlayerSignsManager;
import scripts.RollCommand;
import scripts.UpdateManager;
import serializeableClass.PlayerSignsConfiguration;
import slotChests.SlotChest;
import slotChests.SlotChestsManager;

public class CommandsListener implements Listener, CommandExecutor {

	private Main main;
	
	public CommandsListener(Main main) {
		this.main = main;
		Bukkit.getServer().getPluginCommand("casino").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(CasinoManager.getPrefix() + "You need to be a player!");
			return false;
		}
		
		Player player = (Player)sender;
		
		if(args.length == 0) {
			openCasinoGui((Player) sender);
		} else if(args.length == 1) {
			if(args[0].equalsIgnoreCase("updateconfig")) {
				if(Main.perm.has(player, "casino.admin") || player.isOp()) {
					UpdateManager.updateConfigYml(main);
					player.sendMessage(CasinoManager.getPrefix() + "You successfully updated the config!");
				} else {
					player.sendMessage(CasinoManager.getPrefix()+ "§4You don't have permission for that action!");
				} 
		
			} else if(args[0].equalsIgnoreCase("reloadconfig")) {
				if(Main.perm.has(player, "casino.admin") || player.isOp()) {
					
					UpdateManager.reloadConfig();
					CasinoManager.reload();
					player.sendMessage(CasinoManager.getPrefix() + "Config.yml reloaded!");
				} else {
					player.sendMessage(CasinoManager.getPrefix() + "§4You don't have permission for that action!");
				}
				
			} else if(args[0].equalsIgnoreCase("help")) {
				showHelpToPlayer(player);
			}
			else if(args[0].equalsIgnoreCase("admin") && Main.perm.has(sender, "casino.admin")) {
				showHelpToAdmin(player);
			} else if(args[0].equalsIgnoreCase("createchest")) {
				if(!Main.perm.has(sender, "casino.slotchest.create")) {
					player.sendMessage(CasinoManager.getPrefix() + "§4You don't have permissions for that action!");
					
				} else
				createSlotChest(player);
			} else if(args[0].equalsIgnoreCase("save")) {
				CasinoManager.slotChestManager.save();
			} else if(args[0].equalsIgnoreCase("chestlocations")) {
				showChestLocations(player);
			} else if(args[0].equalsIgnoreCase("resetdata")) {
				resetData(player);
			} else if(args[0].equalsIgnoreCase("reloaddata")) {
				reloadData(player);
			} else if(args[0].equalsIgnoreCase("test"))
			{
				player.sendMessage(MessageManager.get("test-message"));
			}
		} else if(args.length == 2) {
			if(args[0].equalsIgnoreCase("help") && args[1].equalsIgnoreCase("dice")) {

				showDiceHelpToPlayer(player);
			} else if(args[0].equalsIgnoreCase("help") && args[1].equalsIgnoreCase("blackjack")) {
				showBlackjackHelpToPlayer(player);
			} else if(args[0].equalsIgnoreCase("help") && args[1].equalsIgnoreCase("leaderboard")) {
				showLeaderboardSignHelpToPlayer(player);
			} else if(args[0].equalsIgnoreCase("help") && args[1].equalsIgnoreCase("slot")) {
				showSlotsSignHelpToPlayer(player);
			}
			
			else if(args[0].equalsIgnoreCase("sign") && args[1].equalsIgnoreCase("enable")) {
				enablePlayerManagedSign(player);
			} else if(args[0].equalsIgnoreCase("sign") && args[1].equalsIgnoreCase("disable")) {
				disablePlayerManagedSign(player);
			} 
		} else if(args.length == 3) {
			if(args[0].equalsIgnoreCase("roll")) {
				if(Main.perm.has(player, "casino.roll")) {
					rollCommand(player, args);
				} else {
					player.sendMessage(CasinoManager.getPrefix() + "§4You don't have permissions to do that!");
				}
			}
		} else if(args.length == 4) {
			if(args[0].equalsIgnoreCase("roll")) {
				if(Main.perm.has(player, "casino.roll")) {
					rollCommand(player, args);
				} else {
					player.sendMessage(CasinoManager.getPrefix() + "§4You don't have permissions to do that!");
				}
			}
		}
		return true;
	}
	
	
	




	private void createSlotChest(Player player) {
		if(!(Main.perm.has(player, "casino.slotchest.create") || Main.perm.has(player, "casino.admin"))) {
			player.sendMessage(CasinoManager.getPrefix() + "§4You don't have permissions to create a SlotChest!");
			return;
		}
		Block block = player.getTargetBlockExact(10);
		if(block == null)
			return;
		
		Chest chest = null;
		try {
			chest = (Chest) block.getState();
		} catch(ClassCastException e) {
			player.sendMessage(CasinoManager.getPrefix() + "§4This is not a valid Chest!");
		}
		
		if(chest == null)
			return;
	
		if(!(chestEmpty(chest))) {
			player.sendMessage(CasinoManager.getPrefix() + "§4This chest contains items! You have to remove them first, before you can create a SlotChest out of it!");
			return;
		}
		SlotChestsManager.createSlotChest(chest.getLocation(), player);
		
	}
	private Boolean chestEmpty(Chest chest) {
		for(int i = 0; i < chest.getInventory().getSize(); i++) {
			if(chest.getInventory().getItem(i) != null)
				return false;
		}
		return true;
	}

	private void rollCommand(Player player, String[] args) {
		RollCommand.roll(player, args);	
	}
	
	private void enablePlayerManagedSign(Player player) {
		if(!(Main.perm.has(player, "casino.dice.create") || Main.perm.has(player, "casino.admin"))) {
			player.sendMessage(CasinoManager.getPrefix() + "§4You don't have permissions to do that!");
			return;
		}
		
		// TODO Auto-generated method stub
		PlayerSignsConfiguration cnf = checkForSign(player);
		if(cnf == null) return; //feedback wird vorher schon ausgegeben
		
		if(cnf.isSignDisabled()) {
			cnf.enableSign();
			player.sendMessage(CasinoManager.getPrefix() + "Successfully enabled your sign!");
		} else {
			player.sendMessage(CasinoManager.getPrefix() + "§4Your sign is currently enabled!");
		}
	}

	private PlayerSignsConfiguration checkForSign(Player player) {
		Block block = player.getTargetBlockExact(10);
		if(block == null) {
			player.sendMessage(CasinoManager.getPrefix() + "§4Invalid target!");
			return null;
		}
		if(!(block.getType().toString().contains("SIGN"))) {
			player.sendMessage(CasinoManager.getPrefix() + "§4That is not a sign!");
			return null;
		}
		Sign signLookingAt = (Sign) block.getState();
		PlayerSignsConfiguration cnf = PlayerSignsManager.getPlayerSign(signLookingAt.getLocation());
		if(cnf == null) {
			player.sendMessage(CasinoManager.getPrefix() + "§4That is not a valid player sign!");
			return null;
		}
		if(Main.perm.has(player, "casino.admin")) {
			
		} else if(!(cnf.getOwner().getPlayer().equals(player))) {
			player.sendMessage(CasinoManager.getPrefix() + "§4You are not the owner of this sign!");
			return null;
		}
		
		return cnf;
		
	}

	private void disablePlayerManagedSign(Player player) {
		if(!(Main.perm.has(player, "casino.dice.create") || Main.perm.has(player, "casino.admin"))) {
			player.sendMessage(CasinoManager.getPrefix() + "§4You don't have permissions to do that!");
			return;
		}
		PlayerSignsConfiguration cnf = checkForSign(player);
		if(cnf == null) return;
		
		if(cnf.isSignDisabled()) {
			player.sendMessage(CasinoManager.getPrefix() + "§4Your sign is currently disabled!");
		} else {
			cnf.disableSign();
			player.sendMessage(CasinoManager.getPrefix() + "Successfully disabled your sign!");
		}
		
	}

	private void showDiceHelpToPlayer(Player player) {
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("§f§l§nDice help");
		if(Main.perm.has(player, "casino.dice.create")) player.sendMessage("§2permissions: §4true");
		else player.sendMessage("§2permissions: §4false");
		
		player.sendMessage("§6§n§lFormat of a dice sign:");
		player.sendMessage("");
		player.sendMessage("     §6line 1: casino");
		player.sendMessage("     §6line 2: dice");
		player.sendMessage("     §6line 3: the bet like 30 or 20.5");
		player.sendMessage("     §6line 4: the win chance and the multiplicator like 1-40;3 (the player wins if he draws between 1-40 and get bet*3)");
	}
	private void showBlackjackHelpToPlayer(Player player) {
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("§f§l§lBlackjack help");
		if(Main.perm.has(player, "casino.blackjack.create")) player.sendMessage("§2permissions: §4true");
		else player.sendMessage("§2permissions: §4false");
		
		player.sendMessage("§6§n§lFormat of a blackjack sign:");
		player.sendMessage("");
		player.sendMessage("     §6line 1: casino");
		player.sendMessage("     §6line 2: blackjack");
		player.sendMessage("     §6line 3: minbet;maxbet like 20;30");
		player.sendMessage("     §6line 4: multiplicator if players draws a blackjack (21)");
	}
	private void showLeaderboardSignHelpToPlayer(Player player)
	{
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("§f§lLeaderboardsign help");
		if(Main.perm.has(player, "casino.leaderboardsign.create")) player.sendMessage("§2permissions: §4true");
		else player.sendMessage("§2permissions: §4false");
		
		player.sendMessage("");
		player.sendMessage("§6§n§lFormat of a leaderboardsign:");
		player.sendMessage("");
		player.sendMessage("     §6line 1: leaderboard");
		player.sendMessage("     §6line 2: position for the sign like 1 for first place");
		player.sendMessage("     §6line 3: mode (count, sumamount, highestamount)");
		player.sendMessage("     §6line 4: range (all for all your signs, number of blocks (3 as example) for using signs in this block range");
	}
	private void showSlotsSignHelpToPlayer(Player player)
	{
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("§f§lCasino-Slots sign help");
		if(Main.perm.has(player, "casino.sign.create")) player.sendMessage("§2permissions: §4true");
		else player.sendMessage("§2permissions: §4false");
		
		player.sendMessage("");
		player.sendMessage("§6§n§lFormat of a Casino-Slots sign:");
		player.sendMessage("");
		player.sendMessage("    §6line 1: casino");
		player.sendMessage("    §6line 2: bet amount in decimal like 10.0");
		
	}
	
	private void showHelpToAdmin(Player player) {
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("§4Admin page");
		player.sendMessage("§6/casino reloadconfig §8- reloads the config.yml");
		player.sendMessage("§6/casino resetdata §8- deletes all roll-data from playermanagedsigns (data.yml)");
		player.sendMessage("§6/casino reloaddata §8- reload all leaderboard signs and data.yml. Could lag a bit!");
	}

	private void showHelpToPlayer(Player player) {
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage(CasinoManager.getPrefix());
		player.sendMessage("§2CasinoPlugin Version " + Main.pluginVersion + " by chrisimi");
		player.sendMessage("§6/casino §8- open the casino GUI");
		player.sendMessage("§6/casino admin §8- admin help command"); 
		player.sendMessage("§6/casino help slot §8- show help for placing Casino-Slots signs!");
		player.sendMessage("§6/casino help dice §8- show help for placing a dice signs!");
		player.sendMessage("§6/casino help blackjack §8- show help for placing blackjack signs!");
		player.sendMessage("§6/casino help leaderboard §8- show help for placing leaderboard signs!");
		player.sendMessage("§6/casino sign disable §8- disable your own player sign while looking onto it!");
		player.sendMessage("§6/casino sign enable §8- enable your own player sign while looking onto it!");
		player.sendMessage("§6/casino roll [minimum] [maximum] [player (not needed)] §8 - roll a random number which will be sent to nearby players or mentioned player!");
		player.sendMessage("§6/casino createchest §8 - create your own slotchest while looking on a normal chest!!! clear it's inventory before!");
		player.sendMessage("§6/casino chestlocations §8 - get the locations from your SlotChests!");
	}

	private void showChestLocations(Player player) {
		ArrayList<SlotChest> list = SlotChestsManager.getSlotChestsFromPlayer(player);
		if(list.size() == 0) {
			player.sendMessage(CasinoManager.getPrefix() + "You don't have any SlotChests!");
			return;
		}
		player.sendMessage("\n\n");
		player.sendMessage(CasinoManager.getPrefix() + "§6§lYour SlotChests:");
		
		int index = 1;
		for(SlotChest chest : list) {
			player.sendMessage(String.format("§6%s: x: %s, y: %s, z: %s", index, (int)chest.getLocation().getX(), (int)chest.getLocation().getY(), (int)chest.getLocation().getZ()));
			index++;
		}
	}
	
	private void openCasinoGui(Player sender) {
		if(Main.perm.has(sender, "casino.gui") || Main.perm.has(sender, "casino.admin")) {
			sender.sendMessage(CasinoManager.getPrefix() + "open Casino GUI");
			new CasinoGUI(main, sender);
		} else {
			sender.sendMessage(CasinoManager.getPrefix() + "§4You don't have permissions to play slots!");
		}

	}
	private void resetData(Player player)
	{
		if(Main.perm.has(player, "casino.admin"))
		{
			LeaderboardsignsManager.resetData();
			player.sendMessage(CasinoManager.getPrefix() + "You successfully reset the data!");
		} else
		{
			player.sendMessage(CasinoManager.getPrefix() + "§4You don't have permission to do that!");
		}
	}
	private void reloadData(Player player)
	{
		if(Main.perm.has(player, "casino.admin")) 
		{
			LeaderboardsignsManager.reloadData(main);
			player.sendMessage(CasinoManager.getPrefix() + "You successfully reloaded the data!");
		} else 
		{
			player.sendMessage(CasinoManager.getPrefix() + "§4You don't have permission to do that!");
		}
	}
}
