package com.chrisimi.casinoplugin.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class VersionManager {

	private static final String URL = "https://api.spigotmc.org/legacy/update.php?resource=71898";
	/**
	 * 
	 * @param currentVersion String with the current version of the plugin
	 * @return 
	 */
	public static String newestVersion = "";
	public static void CheckForNewVersion(String currentVersion, ) {
		if(currentVersion.contains("b")) {
			main.getLogger().info("This is a Development build, so no version check!");
			return;
		}
		
		try {
			HttpsURLConnection connection = (HttpsURLConnection) new URL(URL).openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			
			connection.connect();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String data = "";
			String a;
			while((a = br.readLine()) != null) {
				data += a;
			}
			if(data == "") {
				CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get plugin-version!");
				return;
			}
			newestVersion = data;
			
			Boolean isPluginUpdated = currentVersion.equals(data);
			//Main.isPluginUpdated = isPluginUpdated;
			
			if(isPluginUpdated) {
				CasinoManager.LogWithColor(ChatColor.GREEN + "Newest Version: " + data + ", current version: " + currentVersion + " - Plugin is updated!");
			} else {
				CasinoManager.LogWithColor(ChatColor.YELLOW + "Newest Version: " + data + ", current version: " + currentVersion);
				CasinoManager.LogWithColor(ChatColor.YELLOW + "There is a newer version for this plugin!");
				CasinoManager.LogWithColor(ChatColor.YELLOW + "https://www.spigotmc.org/resources/casino-plugin.71898/");
			}
			
		} catch (IOException e) {
			CasinoManager.LogWithColor(ChatColor.RED + "ERROR while trying to get version!");
			e.printStackTrace();
		}
		
	}
}
