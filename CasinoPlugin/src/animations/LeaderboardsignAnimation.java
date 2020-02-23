package animations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;

import com.chrisimi.casino.main.Main;

import scripts.LeaderboardsignsManager;
import scripts.LeaderboardsignsManager.PlayData;
import serializeableClass.Leaderboardsign;


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
	}
	private void analyseData()
	{
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
				return v1.getValue() - v2.getValue();
			}
		};
		List<Entry<OfflinePlayer, Integer>> listOfEntries = new ArrayList<>(map.entrySet());
		Collections.sort(listOfEntries, valueComperator);
		
		LinkedHashMap<OfflinePlayer, Integer> sortedMap = new LinkedHashMap<OfflinePlayer, Integer>(listOfEntries.size());
		
		int index = 1;
		for(Entry<OfflinePlayer, Integer> entry : sortedMap.entrySet()) {
			if(index == sign.position) 
			{
				currentPlayer = entry.getKey();
				currentValue = entry.getValue();
				
				
				
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
		signBlock.setLine(1, currentPlayer.getName());
		signBlock.setLine(2, String.valueOf(currentValue));
		signBlock.setLine(3, "----------------");
		signBlock.update(true);
	}
}
