package com.chrisimi.casinoplugin.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;

import com.chrisimi.casinoplugin.main.Main;

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
			
			Writer oStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.configYml), Charset.forName("UTF-8")));
			oStream.write(new String(buffer));
			
			oStream.close();
			iStream.close();
			CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully created config.yml!");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateConfigYml(Main mainInstance) {
		//get configs from old config
		
		Map<String, Object> values = new HashMap<>();
		YamlConfiguration cofn = YamlConfiguration.loadConfiguration(Main.configYml);
		values = cofn.getValues(true);
		
		createConfigYml(mainInstance);
		
		
		changeCommandsToPoints();
		cofn = YamlConfiguration.loadConfiguration(Main.configYml);
		
		for(Entry<String, Object> entry : values.entrySet())
		{
			if(cofn.contains(entry.getKey()))
			{
				cofn.set(entry.getKey(), entry.getValue());
			}
		}
		
		try
		{
			cofn.save(Main.configYml);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		changePointsToCommand();
	}

	private static void changeCommandsToPoints()
	{
		int index = 0;
		BufferedReader reader = null;
		BufferedWriter writer = null;
		StringBuilder builder = new StringBuilder();
		try
		{
			reader = new BufferedReader(new FileReader(Main.configYml));
			
			String line = "";
			while((line = reader.readLine()) != null) 
			{
				if(index < 3) { index++; builder.append(line); builder.append("\n"); continue;} //ignore die 1. 3 Zeilen
				
				if(line.contains("#"))
				{
					if(line.length() > 75)
					{
						String a = line.substring(75);
						builder.append("COMMENT_" + index + ":" + a);
						index++;
					}
					
					line = line.replace("#", "COMMENT_" + index + ":");
					index++;
					
				} else if(line.contains(":"))
				{
					String[] splited = line.split(":");
					
					if(splited.length != 1)
					{
						char[] a = splited[1].toCharArray();
						
						String output = "";
						for(char ch : a)
						{
							output += " " + String.valueOf(Integer.valueOf(ch));
						}
						
						CasinoManager.Debug(UpdateManager.class, splited[0] + " + " + output);
						
						if(splited[1].equals(" ") || splited[1].equals(""))
						{
							splited[1] = "''";
						}
						line = splited[0] + ": " + splited[1];
					}
				}
				
				builder.append(line);
				builder.append("\n");
				
			}
			
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.configYml), Charset.forName("UTF-8")));
			writer.write(builder.toString());
			
			reader.close();
			writer.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void changePointsToCommand()
	{
		int index = 0;
		BufferedReader reader = null;
		BufferedWriter writer = null;
		StringBuilder builder = new StringBuilder();
		try
		{
			reader = new BufferedReader(new FileReader(Main.configYml));
			String line = "";
			while((line = reader.readLine()) != null)
			{
				builder.append(line);
				builder.append("\n");
				index++;
			}
			
			
			String configString = builder.toString();
			//System.out.println(configString);
			for(; index >= 0; index--)
			{
				configString = configString.replace("COMMENT_" + index+":", "# ");
				//System.out.println("COMMENT_" + index + ": ");
				//System.out.print(configString.contains("COMMENT_" + index + ":"));
			}
			//System.out.println(configString);
			//add space
			
			
			String[] lines = configString.split("\n");
			for(int i = 0; i < lines.length; i++)
			{
				if(lines[i].contains(":") && lines.length > i + 1 && lines[i + 1].startsWith("# "))
				{
					lines[i] += "\n\n";
				}
			}
			configString = String.join("\n", lines);
			
			
			
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.configYml), Charset.forName("UTF-8")));
			writer.write(configString);
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				reader.close();
				writer.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			
		}
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
			CasinoManager.LogWithColor(ChatColor.RED + path + " is not valid using default value! Try to update the config!");
			return obj;
		} else
			return o;
	}
	public static void changeSettings(File file, Map<String, Object> oldData, Map<String, Object> newData) {
		
	}
}
