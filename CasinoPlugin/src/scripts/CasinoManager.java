package scripts;

import org.bukkit.configuration.file.YamlConfiguration;

import com.chrisimi.casino.main.Main;

import listeners.ChatListener;
import listeners.CommandsListener;
import listeners.InventoryClickListener;
import listeners.PlayerJoinListener;
import slotChests.SlotChestsManager;

public class CasinoManager {

	private static Main main;
	private static String prefix = "§9[§6Casino§9] §a";
	
	private static SignsManager signsManager;
	public static PlayerSignsManager playerSignsManager;
	private static RollCommand rollCommand;
	public static SlotChestsManager slotChestManager;
	public CasinoManager(Main main) {
		this.main = main;
		prefixYml();
		initialize();
	}
	private void prefixYml() {
		String prefixFromYml = YamlConfiguration.loadConfiguration(Main.configYml).getString("prefix");
		if(prefixFromYml == "" || prefixFromYml == null) {
			main.getLogger().info("no prefix in config.yml... using default one!");
		} else {
			main.getLogger().info(String.format("Found prefix %s in config.yml... changed to new prefix!", prefixFromYml));
			prefix = prefixFromYml;
		}
		
	}
	private void initialize() {
		new CommandsListener(main);
		new InventoryClickListener(main);
		new PlayerJoinListener(main);
		new ChatListener(main);
		signsManager = new SignsManager(main);
		playerSignsManager = new PlayerSignsManager(main);
		rollCommand = new RollCommand(main);
		slotChestManager = new SlotChestsManager(main);
	}
	
	public static void reload() {
		signsManager.reload();
		playerSignsManager.reload();
		rollCommand.reload();
		slotChestManager.reload();
	}
	public static String getPrefix() {return prefix;}
	
}
