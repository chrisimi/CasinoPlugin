package hologramsystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import com.chrisimi.casino.main.Main;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.placeholder.PlaceholderReplacer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import scripts.CasinoManager;
import scripts.PlayerSignsManager;

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
		
	}
	
	public void startSystem(Plugin plugin)
	{
		hologramsjson = new File(plugin.getDataFolder(), "holograms.json");
		createFiles();
		
		gson = new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		loadPlaceholders();
		importData();
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
	
	private class LBHologramContainer 
	{
		@Expose
		public List<LBHologram> container = new ArrayList<>();
	}

	public static void addHologram(LBHologram hologram)
	{
		// TODO Auto-generated method stub
		
	}
}
