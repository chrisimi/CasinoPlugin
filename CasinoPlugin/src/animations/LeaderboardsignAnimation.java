package animations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;

import com.chrisimi.casino.main.Main;

import scripts.LeaderboardsignsManager;
import serializeableClass.Leaderboardsign;
import serializeableClass.PlayData;
import serializeableClass.Leaderboardsign.Mode;


public class LeaderboardsignAnimation implements Runnable
{
	private final Main main;
	private final Sign signBlock;
	private final Leaderboardsign sign;
	
	List<PlayData> currentData = new ArrayList<>();
	
	private double currentValue = 0.0;
	private OfflinePlayer currentPlayer = null;
	
	public LeaderboardsignAnimation(Main main, Leaderboardsign sign, Sign signBlock)
	{
		this.main = main;
		this.sign = sign;
		this.signBlock = signBlock;
	}
	
	
	@Override
	public void run() 
	{
		//will be called every frame
		if(!(signBlock.isPlaced())) return;
		
		signBlock.setLine(1, "§4updating...");
		signBlock.setLine(2, "§4updating...");
		signBlock.update(true);
		
		getData();
		analyseData();
		writeSign();
	}
	
	private void getData()
	{
		currentData = LeaderboardsignsManager.getPlayData(sign.getPlayer());
		
		if(!(sign.modeIsAll())) 
		{
			int range = sign.getRange();
			ArrayList<PlayData> dataToRemove = new ArrayList<>();
			for(PlayData data : currentData) {
				if(data.Location.distance(signBlock.getLocation()) > range) {
					dataToRemove.add(data);
				}
			}
			currentData.removeAll(dataToRemove);
		}
	}
	private void analyseData()
	{
		
		//cool sign animations
		
		switch(sign.getMode()) {
		case COUNT:
			analyseDataCount();
			break;
		case HIGHESTAMOUNT:
			analyseDataHighestAmount();
			break;
		case SUMAMOUNT:
			analyseDataSumAmount();
			break;
		}
	}
	private void analyseDataCount() 
	{
	
		//count data
		HashMap<OfflinePlayer, Integer> map = new HashMap<>();
		for(PlayData data : currentData) {
			if(map.containsKey(data.Player)) {
				map.compute(data.Player, (a, b) -> b + 1);
			} else {
				map.put(data.Player, 1);
			}
		}
		Comparator<Entry<OfflinePlayer, Integer>> valueComperator = new Comparator<Entry<OfflinePlayer, Integer>>()
		{
			@Override
			public int compare(Entry<OfflinePlayer, Integer> v1, Entry<OfflinePlayer, Integer> v2)
			{
				return v2.getValue() - v1.getValue();
			}
		};
		List<Entry<OfflinePlayer, Integer>> listOfEntries = new ArrayList<>(map.entrySet());
		Collections.sort(listOfEntries, valueComperator);
		
		LinkedHashMap<OfflinePlayer, Integer> sortedMap = new LinkedHashMap<OfflinePlayer, Integer>(listOfEntries.size());
		for(Entry<OfflinePlayer, Integer> entry : listOfEntries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		
		int index = 1;
		for(Entry<OfflinePlayer, Integer> entry : sortedMap.entrySet()) {
			if(index == sign.position) 
			{
				currentPlayer = entry.getKey();
				currentValue = entry.getValue();
				
				break;
				
			} else
			index++;
		}
		if(sortedMap.size() == 0) 
		{
			currentPlayer = null;
			currentValue = 0.0;
		}
	}
	private void analyseDataHighestAmount() 
	{
		HashMap<OfflinePlayer, Double> map = new HashMap<>();
		for(PlayData data : currentData) {
			if(data.WonAmount <= 0) continue;
			
			if(map.containsKey(data.Player)) {
				
				//check if the current wonAmount is higher than the one in the list!
				if(map.get(data.Player) < data.WonAmount)
					map.put(data.Player, data.WonAmount);

			} else {
				map.put(data.Player, data.WonAmount);
			}
		}
		
		Comparator<Entry<OfflinePlayer, Double>> valueComperator = new Comparator<Entry<OfflinePlayer, Double>>()
		{
			@Override
			public int compare(Entry<OfflinePlayer, Double> v1, Entry<OfflinePlayer, Double> v2)
			{
				return (int) (v2.getValue() - v1.getValue());
			}
		};
		List<Entry<OfflinePlayer, Double>> listOfEntries = new ArrayList<>(map.entrySet());
		Collections.sort(listOfEntries, valueComperator);
		
		LinkedHashMap<OfflinePlayer, Double> sortedMap = new LinkedHashMap<OfflinePlayer, Double>(listOfEntries.size());
		
		for(Entry<OfflinePlayer, Double> entry : listOfEntries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		
		int index = 1;
		for(Entry<OfflinePlayer, Double> entry : sortedMap.entrySet()) {
			if(index == sign.position) 
			{
				currentPlayer = entry.getKey();
				currentValue = entry.getValue();
				
				break;
				
			} else
			index++;
		}
		if(sortedMap.size() == 0) 
		{
			currentPlayer = null;
			currentValue = 0.0;
		}
	}
	private void analyseDataSumAmount()
	{
		HashMap<OfflinePlayer, Double> map = new HashMap<>();
		for(PlayData data : currentData) {
			if(data.WonAmount <= 0) continue;
			
			if(map.containsKey(data.Player)) {
				map.compute(data.Player, (a, b) -> b + data.WonAmount);
			} else {
				map.put(data.Player, data.WonAmount);
			}
		}
		
		Comparator<Entry<OfflinePlayer, Double>> valueComperator = new Comparator<Entry<OfflinePlayer, Double>>()
		{
			@Override
			public int compare(Entry<OfflinePlayer, Double> v1, Entry<OfflinePlayer, Double> v2)
			{
				return (int) (v2.getValue() - v1.getValue());
			}
		};
		List<Entry<OfflinePlayer, Double>> listOfEntries = new ArrayList<>(map.entrySet());
		Collections.sort(listOfEntries, valueComperator);
		
		LinkedHashMap<OfflinePlayer, Double> sortedMap = new LinkedHashMap<OfflinePlayer, Double>(listOfEntries.size());
		
		for(Entry<OfflinePlayer, Double> entry : listOfEntries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		
		int index = 1;
		for(Entry<OfflinePlayer, Double> entry : sortedMap.entrySet()) {
			if(index == sign.position) 
			{
				currentPlayer = entry.getKey();
				currentValue = entry.getValue();
				
				break;
				
			} else
			index++;
		}
		if(sortedMap.size() == 0) 
		{
			currentPlayer = null;
			currentValue = 0.0;
		}
	}
	private void writeSign() 
	{
		signBlock.setLine(0, "§6§l"+String.valueOf(sign.position)+". Place");
		signBlock.setLine(1, (currentPlayer == null) ? "§4---" : currentPlayer.getName());
		if(sign.getMode() != Mode.COUNT)
			signBlock.setLine(2, (currentValue == 0.0) ? "§4---" : Main.econ.format(currentValue));
		else
			signBlock.setLine(2, (currentValue == 0.0) ? "§4---" : String.valueOf(currentValue));
		signBlock.setLine(3, "----------------");
		signBlock.update(true);
	}
}
