package scripts;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import com.chrisimi.casino.main.Main;

/**
 * 
 * config manager
 * @author chris
 *
 */
public class UpdateManager {

	public static Map<String, Object> configValues = new HashMap<String, Object>();
	
	
	
	public static void createConfigYml(Main main) {
		
		try {
			Main.configYml.createNewFile();
			
			InputStream iStream = main.getResource("config.yml");
			if(iStream == null) {
				CasinoManager.LogWithColor(ChatColor.RED + "can't read config.yml from jar");
				return;
			}
			byte[] buffer = new byte[iStream.available()];
			iStream.read(buffer);
			
			OutputStream oStream = new FileOutputStream(Main.configYml);
			oStream.write(buffer);
			
			oStream.close();
			iStream.close();
			CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully created config.yml!");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void updateConfigYml(Main mainInstance) {
		//get configs from old config
		
		Map<String, Object> values = YamlConfiguration.loadConfiguration(Main.configYml).getValues(true);
		Main.configYml.delete();
		createConfigYml(mainInstance);
		YamlConfiguration configYml = YamlConfiguration.loadConfiguration(Main.configYml);
		
		ArrayList<String> valuesToNotChange = new ArrayList<String>();
		valuesToNotChange.add("version");
		
		for(Entry<String, Object> entry : values.entrySet()) {
			
			if(!(valuesToNotChange.contains(entry.getKey()))) {
				configYml.set(entry.getKey(), entry.getValue());
			}
		}
		
		reloadConfig();
	}

	public static void reloadConfig() {
		YamlConfiguration configYml = YamlConfiguration.loadConfiguration(Main.configYml);
		configValues = configYml.getValues(true);
		CasinoManager.LogWithColor(ChatColor.GREEN + "config.yml successfully imported!");
	}
	
	public static Object getValue(String path) {
		
		if(configValues == null || configValues.size() <= 1) {
			
			
			
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get values from config.yml... recreating config.yml!");
			createConfigYml(CasinoManager.main);
		}
		
		Object o = configValues.get(path);
		if(o == null) return "0.0";
		else
			return o;
	}
	public static Object getValue(String path, Object obj) {
		Object o = configValues.get(path);
		if(o == null) {
			Main.getInstance().getLogger().info(ChatColor.RED + path + " is not valid using default value! Try to update the config!");
			return obj;
		} else
			return o;
	}
	
}
