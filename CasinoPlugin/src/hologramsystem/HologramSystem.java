package hologramsystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.chrisimi.casino.main.Main;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.placeholder.PlaceholderReplacer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import scripts.CasinoManager;
import scripts.DataQueryManager;
import scripts.PlayerSignsManager;
import scripts.UpdateManager;
import utils.data.Query;

/**
 * Singelton class which is maintaining the holograms
 * @author chris
 *
 */
public class HologramSystem
{
	/*
	 * 1 interface inventory wo dann das Hologram erstellt werden kann
	 * 
	 * 
	 * 
	 * total_signs
	 */
	
	private static Map<Location, LBHologram> datas = new HashMap<>();
	private static Map<Location, Hologram> holograms = new HashMap<Location, Hologram>();
	
	private static boolean configValueEnabled;
	private static int updateCycleTime;
	
	private static boolean holographicsEnabled;
	
	private static File hologramsjson;
	private static Gson gson;
	
	
	public static HologramSystem _instance;
	
	public static HologramSystem getInstance()
	{
		if(_instance == null)
			_instance = new HologramSystem();
		return _instance;
	}
	
	private Main main;
	private HologramSystem()
	{
		this.main = Main.getInstance();
		initializeConfigValues();
		
		
	}
	
	private void initializeConfigValues()
	{
		try 
		{
			configValueEnabled = Boolean.parseBoolean(UpdateManager.getValue("holograms-enabled", true).toString());
		} catch(Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "ERROR while trying to parse value for enabling holograms. Set to default value: true");
			configValueEnabled = true;
		}
		try
		{
			updateCycleTime = Integer.parseInt(UpdateManager.getValue("holograms-updatecycle", 1200).toString());
		} catch(Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.DARK_RED + "ERROR while trying to parse value for holograms update cycle. Set to default value 1200 (2 minutes)");
			updateCycleTime = 1200;
		}
	}
	
	public void startSystem(Plugin plugin)
	{
		if(!(checkCompatibility(plugin))) return; //if there is an error with HolographicDisplays or the user disabled the holograms
		
		
		hologramsjson = new File(plugin.getDataFolder(), "holograms.json");
		createFiles();
		
		gson = new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		loadPlaceholders();
		importData();
		
		Manager.start();
	}
	
	

	public void stopSystem()
	{
		Manager.stop();
	}
	private boolean checkCompatibility(Plugin plugin)
	{
		holographicsEnabled = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
		
		if(!configValueEnabled)
		{
			CasinoManager.LogWithColor(ChatColor.YELLOW + "You've disabled holograms! No holograms will be loaded.");
			return false;
		}
		else if(!holographicsEnabled)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "Can't find HolographicDisplays! Make sure that you are using the newest version and it's working!");
			return false;
		}
		else
		{
			CasinoManager.LogWithColor(ChatColor.GREEN + "Holograms are enabled on the server.");
			return true;
		}
		
	}
	
	private void createFiles()
	{
		if(!hologramsjson.exists())
			hologramsjson.mkdirs();
	}
	
	private synchronized void importData()
	{
		BufferedReader reader = null;
		StringBuilder sb = new StringBuilder();
		try
		{
			reader = new BufferedReader(new FileReader(hologramsjson));
			
			String line = "";
			
			while((line = reader.readLine()) != null)
				sb.append(line);
			
			if(sb.length() <= 24) 
			{
				reader.close();
				return;
			}
			
			LBHologramContainer container = gson.fromJson(sb.toString(), LBHologramContainer.class);
			
			if(container == null || container.container.size() == 0) throw new Exception("Error while trying to get holograms");
			
			
			
		} catch (Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "Error while trying to get holograms: " + e.getMessage());
			e.printStackTrace(CasinoManager.getPrintWriterForDebug());
		}
		finally
		{
			try
			{
				reader.close();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private synchronized void exportData()
	{
		LBHologramContainer container = new LBHologramContainer();
		container.container = new ArrayList<>(datas.values());
		
		String json = gson.toJson(container);
		
		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter(new FileWriter(hologramsjson));
			
			writer.write(json);
		} catch (Exception e)
		{
			CasinoManager.LogWithColor(ChatColor.RED + "ERROR while trying to export holograms: " + e.getMessage());
			e.printStackTrace(CasinoManager.getPrintWriterForDebug());
		}
		finally 
		{
			try
			{
				writer.close();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	private void loadPlaceholders()
	{
		HologramsAPI.registerPlaceholder(main, "%total_signs%", 10.0, new PlaceholderReplacer()
		{
			
			@Override
			public String update()
			{
				return String.valueOf(PlayerSignsManager.getPlayerSigns().size());
			}
		});
	}
	
	private Hologram createHologram(LBHologram lbHologram)
	{
		Hologram holo = HologramsAPI.createHologram(Main.getInstance(), lbHologram.getLocation());
		
		LinkedHashMap<Integer, Query> datas = DataQueryManager.getQuery(lbHologram);
		
		holo.appendTextLine(lbHologram.description);
		
		//get max length of name
		int highestLengthName = 0;
		int highestLengthValue = 0;
		for(Query query : datas.values())
		{
			highestLengthName = (query.player.getName().length() > highestLengthName) ? query.player.getName().length() : highestLengthName;
			highestLengthValue = (int) (((query.value / 10.0) > highestLengthValue) ? query.value / 10.0 : highestLengthValue);
		}
		
		
		
		for(Map.Entry<Integer, Query> entry : datas.entrySet())
		{
			int pos = entry.getKey();
			
			if(pos <= 3 && lbHologram.highlightTop3)
			{
				switch (pos)
				{
				case 1:
					holo.appendTextLine("§3" + getLine(entry.getValue(), highestLengthName, highestLengthValue));
					holo.appendItemLine(new ItemStack(Material.DIAMOND_BLOCK));
					break;
				case 2:
					holo.appendTextLine("§6" + getLine(entry.getValue(), highestLengthName, highestLengthValue));
					holo.appendItemLine(new ItemStack(Material.GOLD_BLOCK));
					break;
				case 3:
					holo.appendTextLine("§7" + getLine(entry.getValue(), highestLengthName, highestLengthValue));
					holo.appendItemLine(new ItemStack(Material.IRON_BLOCK));
				default:
					break;
				}
			}
			if(!lbHologram.highlightTop3)
			{
				holo.appendTextLine("§4" + entry.getKey() + " | " + getLine(entry.getValue(), highestLengthName, highestLengthValue));
			}
		}
		
		return holo;
	}
	private String getLine(Query query, int maxLengthName, int maxLengthValue)
	{
		if(query == null || query.player == null ) return "";
		
		return String.format("%-" + maxLengthName + "s | %" +  maxLengthValue + "d", query.player.getName(), query.value);
	}
	
	private static class Manager 
	{
		private static int currentID = 0;
		
		public static void start()
		{
			if(currentID != 0)
				stop();
			
			currentID = Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), task, 0, updateCycleTime);
		}
		public static void stop()
		{
			Main.getInstance().getServer().getScheduler().cancelTask(currentID);
		}
		private static Runnable task = new Runnable()
		{
			
			@Override
			public void run()
			{
				for(Map.Entry<Location, LBHologram> entry : datas.entrySet())
				{
					//delete old hologram
					if(holograms.containsKey(entry.getKey()))
					{
						holograms.remove(entry.getKey());
					}
					
					Hologram holo = HologramSystem.getInstance().createHologram(entry.getValue());
					holograms.put(entry.getKey(), holo);
				}
			}
		};
	}
	
	private class LBHologramContainer 
	{
		@Expose
		public List<LBHologram> container = new ArrayList<>();
	}

	public static void addHologram(LBHologram hologram)
	{
		datas.put(hologram.getLocation(), hologram);
		HologramSystem.getInstance().createHologram(hologram);
		HologramSystem.getInstance().exportData();
	}
}
