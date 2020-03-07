package scripts;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import com.chrisimi.casino.main.Main;

import listeners.ChatListener;
import listeners.CommandsListener;
import listeners.InventoryClickListener;
import listeners.PlayerJoinListener;
import net.md_5.bungee.api.ChatColor;
import slotChests.SlotChestsManager;

public class CasinoManager {

	public static Main main;
	private static String prefix = "§9[§6Casino§9] §a";
	
	private static SignsManager signsManager;
	public static PlayerSignsManager playerSignsManager;
	private static RollCommand rollCommand;
	public static SlotChestsManager slotChestManager;
	public static LeaderboardsignsManager leaderboardManager;
	
	public static Boolean configEnableConsoleMessages = true;
	public CasinoManager(Main main) {
		this.main = main;
	}
	public void prefixYml() {
		String prefixFromYml = YamlConfiguration.loadConfiguration(Main.configYml).getString("prefix");
		if(prefixFromYml == "" || prefixFromYml == null) {
			LogWithColor(org.bukkit.ChatColor.YELLOW + "no prefix in config.yml... using default one!");
		} else {
			main.getLogger().info(String.format("Found prefix %s in config.yml... changed to new prefix!", prefixFromYml));
			prefix = prefixFromYml;
		}
		
	}
	public void initialize() {
		new CommandsListener(main);
		new InventoryClickListener(main);
		new PlayerJoinListener(main);
		new ChatListener(main);
		signsManager = new SignsManager(main);
		playerSignsManager = new PlayerSignsManager(main);
		rollCommand = new RollCommand(main);
		slotChestManager = new SlotChestsManager(main);
		leaderboardManager = new LeaderboardsignsManager(main);
		
		try 
		{
			configEnableConsoleMessages = Boolean.valueOf(UpdateManager.getValue("enable-console-messages").toString());
		} catch(Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "CONFIG_ERROR: Error while trying to get enable-console-messages! You have to use a valid boolean value (true/false)! Set to default value: false");
			configEnableConsoleMessages = false;
		}
	}
	
	public static void reload() {
		signsManager.reload();
		playerSignsManager.reload();
		rollCommand.reload();
		slotChestManager.reload();
	}
	public static String getPrefix() {return prefix;}
	
	public static void LogWithColor(String message) {
		try {
			main.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('§', prefix) + message);
		} catch(NullPointerException e) {
			if(message == null) return;
			Bukkit.getLogger().info(message);
		}
		
	}
}
