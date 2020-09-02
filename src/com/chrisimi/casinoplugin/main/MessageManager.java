package com.chrisimi.casinoplugin.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.UpdateManager;

public class MessageManager
{
	public static HashMap<String, String> defaultLanguageFilePack = new HashMap<>();
	public static HashMap<String, String> chosenLanguageFilePack = new HashMap<>();
	
	
	public File messagesFolder = null;
	public static File defaultLanguageFile = null;
	public static File chosenLanguageFile = null;

	public MessageManager(Main main)
	{
		main;
		
		
		
		initializeFiles();
		getLanguageFiles();
		checkForDefaultUpdate();
	}
	
	public static void ReloadMessages()
	{
		Main.msgManager.initializeFiles();
		Main.msgManager.getLanguageFiles();
	}
	
	//create the messages folder and EN_default.yml
	private void initializeFiles()
	{
		messagesFolder = new File(Main.getInstance().getDataFolder(), "messages");
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
				OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(defaultLanguageFile), Charset.forName("UTF-8"));
				outputStream.write(new String(data));
				
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
		 CasinoManager.Debug(this.getClass(), "using: " + configString + ".yml");
		 
		 Boolean exists = false;
		 try
		 {
			File[] files = messagesFolder.listFiles();
			if(!(configString.equals("default")))
				for(File file : files)
				{
					if(file.getName().equalsIgnoreCase(configString + ".yml"))
					{
						exists = true;
						chosenLanguageFile = file;
						CasinoManager.Debug(this.getClass(), "found file: " + configString);
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
	private void checkForDefaultUpdate()
	{
		File tempFile = new File(messagesFolder, "temp.yml");
		try
		{
			InputStream inputStream = main.getResource("EN_default.yml");
			
			if(inputStream == null)
			{
				CasinoManager.LogWithColor(ChatColor.RED + "Can't read EN_default.yml from jar");
				return;
			}
			byte[] data = new byte[inputStream.available()];
			inputStream.read(data);
			OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(tempFile), Charset.forName("UTF-8"));
			
			CasinoManager.Debug(this.getClass(), new String(data));
			
			outputStream.write(new String(data));
			
			inputStream.close();
			outputStream.close();
			
			YamlConfiguration jarConfiguration = YamlConfiguration.loadConfiguration(tempFile);
			YamlConfiguration serverConfiguration = YamlConfiguration.loadConfiguration(defaultLanguageFile);
			
			Map<String, Object> jarMap = jarConfiguration.getValues(true);
			Map<String, Object> serverMap = serverConfiguration.getValues(true);
			Map<String, Object> difference = new HashMap<>();
			
			for(Entry<String, Object> entry : jarMap.entrySet())
			{
				if(!serverMap.containsKey(entry.getKey()))
				{
					difference.put(entry.getKey(), entry.getValue());
				}
			}
			if(difference.size() >= 1)
			{
				//overwrite language file and replace things xd
				
				if(defaultLanguageFile.delete())
				{
					tempFile.renameTo(defaultLanguageFile);
					serverConfiguration = YamlConfiguration.loadConfiguration(defaultLanguageFile);
					for(Entry<String, Object> entry : serverConfiguration.getValues(true).entrySet())
					{
						//System.out.println(entry.getKey() + " - " + entry.getValue());
					}
					
					
					for(Entry<String, Object> entry : serverMap.entrySet())
					{
						serverConfiguration.set(entry.getKey(), entry.getValue());
						
						//System.out.println(entry.getKey() + " - " + entry.getValue().toString());
					}
					
					//serverConfiguration.save(defaultLanguageFile);
					
					
					CasinoManager.LogWithColor(ChatColor.GREEN + "Successfully upgraded default language file!");
					loadLanguageFile(defaultLanguageFile, true);
					
				}	
			}
			
		} catch (Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to update messages: " + e.getMessage());
			e.printStackTrace(CasinoManager.getPrintWriterForDebug());
		}
		//tempFile.delete();
	}
	public static String get(String messageName)
	{
		String early = messageName;
		messageName = "messages." + messageName;
		
		if(chosenLanguageFilePack.containsKey(messageName))
		{
			return chosenLanguageFilePack.get(messageName).replaceAll("&", "§");
		}
		else if(defaultLanguageFilePack.containsKey(messageName))
		{
			if(chosenLanguageFile != null) //only show messages if owner set a language file
				CasinoManager.LogWithColor(ChatColor.YELLOW + "Language file does not have message: " + early + "!");
			
			return defaultLanguageFilePack.get(messageName).replaceAll("&", "§");
		}
		else 
		{
			CasinoManager.LogWithColor(ChatColor.RED + "MESSAGE: " + early + " does not exists!");
			return "§4There is an error with messages! Tell it your administrator!";
			
		}
	}
}
