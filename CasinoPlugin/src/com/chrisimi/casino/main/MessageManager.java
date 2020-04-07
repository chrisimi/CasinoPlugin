package com.chrisimi.casino.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import scripts.CasinoManager;
import scripts.UpdateManager;

public class MessageManager
{
	public static HashMap<String, String> defaultLanguageFilePack = new HashMap<>();
	public static HashMap<String, String> chosenLanguageFilePack = new HashMap<>();
	
	
	public File messagesFolder = null;
	public File defaultLanguageFile = null;
	public File chosenLanguageFile = null;
	private Main main;
	public MessageManager(Main main)
	{
		this.main = main;
		
		
		
		initializeFiles();
		getLanguageFiles();
		
	}
	
	//create the messages folder and en_US.yml
	private void initializeFiles()
	{
		messagesFolder = new File(main.getDataFolder(), "messages");
		try 
		{
			if(!(messagesFolder.exists()))
				messagesFolder.mkdirs();
		} catch(Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "ERROR while trying to create messages folder: " + e.getMessage());
			e.printStackTrace(CasinoManager.getPrintWriterForDebug());
		}
		
		defaultLanguageFile = new File(messagesFolder, "EN_default.yml");
		try
		{
			if(!(defaultLanguageFile.exists()))
			{
				InputStream inputStream = main.getResource("EN_default.yml");
				if(inputStream == null)
				{
					CasinoManager.LogWithColor(ChatColor.RED + "Can't read EN_default.yml from jar");
					return;
				}
				byte[] data = new byte[inputStream.available()];
				inputStream.read(data);
				OutputStream outputStream = new FileOutputStream(defaultLanguageFile);
				outputStream.write(data);
				
				inputStream.close();
				outputStream.close();
				
				CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully created EN_default.yml!");
			}
		} catch (Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "ERROR while trying to create default language file: " + e.getMessage());
			e.printStackTrace(CasinoManager.getPrintWriterForDebug());
		}
	}
	private void getLanguageFiles()
	{
		 String configString = UpdateManager.getValue("language").toString();
		 
		 Boolean exists = false;
		 try
		 {
			File[] files = messagesFolder.listFiles();
			if(configString.equals("default"))
				for(File file : files)
				{
					if(file.getName().equalsIgnoreCase(configString))
					{
						exists = true;
						chosenLanguageFile = file;
					}
				}
			loadLanguageFile(defaultLanguageFile, true);
			if(exists)
				loadLanguageFile(chosenLanguageFile, false);
			
			
		} catch (Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "ERRROR while trying to get language file: " + e.getMessage());
			e.printStackTrace(CasinoManager.getPrintWriterForDebug());
		}
	}
	private void loadLanguageFile(File file, Boolean defaultLanguage)
	{
		if(file == null) return;
		try
		{
			YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
			for(Entry<String, Object> entry : configuration.getValues(true).entrySet())
			{
				if(defaultLanguage)
					defaultLanguageFilePack.put(entry.getKey(), entry.getValue().toString());
				else {
					chosenLanguageFilePack.put(entry.getKey(), entry.getValue().toString());
				}
			}
		} catch (Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "ERROR while trying to parse messages: " + e.getMessage());
			e.printStackTrace(CasinoManager.getPrintWriterForDebug());
		}
	}
	public static String get(String messageName)
	{
		String early = messageName;
		messageName = "messages." + messageName;
		
		if(chosenLanguageFilePack.containsKey(messageName))
		{
			return chosenLanguageFilePack.get(messageName);
		}
		else if(defaultLanguageFilePack.containsKey(messageName))
		{
			if(chosenLanguageFilePack != null) //only show messages if owner set a language file
				CasinoManager.LogWithColor(ChatColor.YELLOW + "Language file does not have message: " + early + "!");
			
			return defaultLanguageFilePack.get(messageName);
		}
		else {
			CasinoManager.LogWithColor(ChatColor.RED + "MESSAGE: " + early + " does not exists!");
			return "§4There is an error with messages! Tell it your administrator!";
			
		}
	}
}
