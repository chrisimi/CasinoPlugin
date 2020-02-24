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
		
		getData();
		analyseData();
		writeSign();
	}
	
	private void getData()
	{
		currentData = LeaderboardsignsManager.getPlayData(sign.getPlayer());
		Bukkit.getLogger().info("current size of data: " + currentData.size());
	}
	private void analyseData()
	{
		
		//cool sign animations
		signBlock.setLine(1, "§4updating...");
		signBlock.setLine(2, "§4updating...");
		signBlock.update(true);
		
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
		
		Bukkit.getLogger().info("sorted count list:");
		int index = 1;
		for(Entry<OfflinePlayer, Integer> entry : sortedMap.entrySet()) {
			Bukkit.getLogger().info(entry.getKey().getName() + "-" + entry.getValue());
			if(index == sign.position) 
			{
				currentPlayer = entry.getKey();
				currentValue = entry.getValue();
				
				Bukkit.getLogger().info("wichtig für sign");
				break;
				
			} else
			index++;
		}
	}
	private void analyseDataHighestAmount() 
	{
		
	}
	private void analyseDataSumAmount()
	{
		
	}
	private void writeSign() 
	{
		signBlock.setLine(0, String.valueOf(sign.position)+".");
		signBlock.setLine(1, (currentPlayer == null) ? "§4---" : currentPlayer.getName());
		signBlock.setLine(2, (currentValue == 0.0) ? "§4---" : String.valueOf(currentValue));
		signBlock.setLine(3, "----------------");
		signBlock.update(true);
	}
}
