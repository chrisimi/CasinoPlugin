package listeners;

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

import scripts.CasinoGUI;
import scripts.CasinoManager;
import scripts.PlayerSignsManager;
import scripts.RollCommand;
import scripts.UpdateManager;
import serializeableClass.PlayerSignsConfiguration;
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
			sender.sendMessage(CasinoManager.getPrefix() + "you need to be a player!");
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
					player.sendMessage(CasinoManager.getPrefix()+ "�4you don't have permission for that action!");
				}
		
			} else if(args[0].equalsIgnoreCase("reloadconfig")) {
				if(Main.perm.has(player, "casino.admin") || player.isOp()) {
					
					UpdateManager.reloadConfig();
					CasinoManager.reload();
					player.sendMessage(CasinoManager.getPrefix()+ "config reloaded!");
				} else {
					player.sendMessage(CasinoManager.getPrefix()+"�4you don't have permission for that action!");
				}
				
			} else if(args[0].equalsIgnoreCase("help")) {
				showHelpToPlayer(player);
			}
			else if(args[0].equalsIgnoreCase("admin") && Main.perm.has(sender, "casino.admin")) {
				showHelpToAdmin(player);
			} else if(args[0].equalsIgnoreCase("createchest")) {
				createSlotChest(player);
			} else if(args[0].equalsIgnoreCase("save")) {
				CasinoManager.slotChestManager.save();
			}
		} else if(args.length == 2) {
			if(args[0].equalsIgnoreCase("help") && args[1].equalsIgnoreCase("dice")) {

				showDiceHelpToPlayer(player);
			} else if(args[0].equalsIgnoreCase("help") && args[1].equalsIgnoreCase("blackjack")) {
				showBlackjackHelpToPlayer(player);
			} else if(args[0].equalsIgnoreCase("sign") && args[1].equalsIgnoreCase("enable")) {
				enablePlayerManagedSign(player);
			} else if(args[0].equalsIgnoreCase("sign") && args[1].equalsIgnoreCase("disable")) {
				disablePlayerManagedSign(player);
			} 
		} else if(args.length == 3) {
			if(args[0].equalsIgnoreCase("roll")) {
				if(Main.perm.has(player, "casino.roll")) {
					rollCommand(player, args);
				} else {
					player.sendMessage(CasinoManager.getPrefix() + "�4You don't have permissions to do that!");
				}
			}
		} else if(args.length == 4) {
			if(args[0].equalsIgnoreCase("roll")) {
				if(Main.perm.has(player, "casino.roll")) {
					rollCommand(player, args);
				} else {
					player.sendMessage(CasinoManager.getPrefix() + "�4You don't have permissions to do that!");
				}
			}
		}
		return true;
	}
	
	
	
	private void createSlotChest(Player player) {
		if(!(Main.perm.has(player, "casino.slotchest.create") || Main.perm.has(player, "casino.admin"))) {
			player.sendMessage(CasinoManager.getPrefix() + "�4You don't have permissions to create a SlotChest!");
			return;
		}
		Block block = player.getTargetBlockExact(10);
		if(block == null)
			return;
		Chest chest = (Chest) block.getState();
		if(chest == null)
			return;
		SlotChestsManager.createSlotChest(chest.getLocation(), player);
		
	}

	private void rollCommand(Player player, String[] args) {
		RollCommand.roll(player, args);	
	}
	
	private void enablePlayerManagedSign(Player player) {
		if(!(Main.perm.has(player, "casino.dice.create") || Main.perm.has(player, "casino.admin"))) {
			player.sendMessage(CasinoManager.getPrefix() + "�4You don't have permissions to do that!");
			return;
		}
		
		// TODO Auto-generated method stub
		PlayerSignsConfiguration cnf = checkForSign(player);
		if(cnf == null) return; //feedback wird vorher schon ausgegeben
		
		if(cnf.isSignDisabled()) {
			cnf.enableSign();
			player.sendMessage(CasinoManager.getPrefix() + "Successfully enabled your sign!");
		} else {
			player.sendMessage(CasinoManager.getPrefix() + "�4Your sign is currently enabled!");
		}
	}

	private PlayerSignsConfiguration checkForSign(Player player) {
		Block block = player.getTargetBlockExact(10);
		if(block == null) {
			player.sendMessage(CasinoManager.getPrefix() + "�4Invalid target!");
			return null;
		}
		if(!(block.getType().toString().contains("SIGN"))) {
			player.sendMessage(CasinoManager.getPrefix() + "�4That is not a sign!");
			return null;
		}
		Sign signLookingAt = (Sign) block.getState();
		PlayerSignsConfiguration cnf = PlayerSignsManager.getPlayerSign(signLookingAt.getLocation());
		if(cnf == null) {
			player.sendMessage(CasinoManager.getPrefix() + "�4That is not a valid player sign!");
			return null;
		}
		if(Main.perm.has(player, "casino.admin")) {
			
		} else if(!(cnf.getOwner().getPlayer().equals(player))) {
			player.sendMessage(CasinoManager.getPrefix() + "�4You are not the owner of this sign!");
			return null;
		}
		
		return cnf;
		
	}

	private void disablePlayerManagedSign(Player player) {
		if(!(Main.perm.has(player, "casino.dice.create") || Main.perm.has(player, "casino.admin"))) {
			player.sendMessage(CasinoManager.getPrefix() + "�4You don't have permissions to do that!");
			return;
		}
		PlayerSignsConfiguration cnf = checkForSign(player);
		if(cnf == null) return;
		
		if(cnf.isSignDisabled()) {
			player.sendMessage(CasinoManager.getPrefix() + "�4Your sign is currently disabled!");
		} else {
			cnf.disableSign();
			player.sendMessage(CasinoManager.getPrefix() + "Successfully disabled your sign!");
		}
		
	}

	private void showDiceHelpToPlayer(Player player) {
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("�f�l�nDice help");
		if(Main.perm.has(player, "casino.dice.create")) player.sendMessage("�2permissions: �4true");
		else player.sendMessage("�2permissions: �4false");
		
		player.sendMessage("�6�n�lFormat of a dice sign:");
		player.sendMessage("");
		player.sendMessage("     �6line 1: casino");
		player.sendMessage("     �6line 2: dice");
		player.sendMessage("     �6line 3: the bet like 30 or 20.5");
		player.sendMessage("     �6line 4: the win chance and the multiplicator like 1-40;3 (the player wins if he draws between 1-40 and get bet*3)");
	}
	private void showBlackjackHelpToPlayer(Player player) {
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("�f�l�lBlackjack help");
		if(Main.perm.has(player, "casino.blackjack.create")) player.sendMessage("�2permissions: �4true");
		else player.sendMessage("�2permissions: �4false");
		
		player.sendMessage("�6�n�lFormat of a blackjack sign:");
		player.sendMessage("");
		player.sendMessage("     �6line 1: casino");
		player.sendMessage("     �6line 2: blackjack");
		player.sendMessage("     �6line 3: minbet;maxbet like 20;30");
		player.sendMessage("     �6line 4: multiplicator if players draws a blackjack (21)");
	}
	private void showHelpToAdmin(Player player) {
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("�4Admin page");
		player.sendMessage("�6/casino reloadconfig �8- reloads the config.yml");
		
	}

	private void showHelpToPlayer(Player player) {
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage(CasinoManager.getPrefix());
		player.sendMessage("�2CasinoPlugin Version " + Main.pluginVersion + " by chrisimi");
		player.sendMessage("�6/casino �8- open the casino GUI");
		player.sendMessage("�6/casino admin �8- admin help command"); 
		player.sendMessage("�6/casino help dice �8- show help for placing a dice signs!");
		player.sendMessage("�6/casino help blackjack �8- show help for placing blackjack signs!");
		player.sendMessage("�6/casino sign disable �8- disable your own player sign while looking onto it!");
		player.sendMessage("�6/casino sign enable �8- enable your own player sign while looking onto it!");
		player.sendMessage("�6/casino roll [minimum] [maximum] [player (not needed)] �8 - roll a random number which will be sent to nearby players or mentioned player!");
	}

	private void openCasinoGui(Player sender) {
		if(Main.perm.has(sender, "casino.gui") || Main.perm.has(sender, "casino.admin")) {
			sender.sendMessage(CasinoManager.getPrefix()+"open Casino GUI");
			new CasinoGUI(main, sender);
		} else {
			sender.sendMessage(CasinoManager.getPrefix() + "�4You don't have permissions to play slots!");
		}
		
		
		
		
	}

}
