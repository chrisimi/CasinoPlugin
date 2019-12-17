package com.chrisimi.casino.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.Bukkit;

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
	public static void CheckForNewVersion(String currentVersion, Main main) {
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
				main.getLogger().info("Error while trying to get version!");
				return;
			}
			newestVersion = data;
			
			Boolean isPluginUpdated = currentVersion.equals(data);
			Main.isPluginUpdated = isPluginUpdated;
			
			if(isPluginUpdated) {
				main.getLogger().info("Newest Version: " + data + ", current version: " + currentVersion + " - Plugin is updated!");
			} else {
				main.getLogger().info("Newest Version: " + data + ", current version: " + currentVersion);
				main.getLogger().info("There is a newer version for this plugin ! ");
				main.getLogger().info("https://www.spigotmc.org/resources/casino-plugin.71898/");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			main.getLogger().info("Error while trying to get version from spigot:");
			e.printStackTrace();
		}
		
	}
}
