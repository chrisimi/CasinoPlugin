package com.chrisimi.casinoplugin.utils.data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.OfflinePlayer;

import com.chrisimi.casinoplugin.serializables.PlayData;

public class HighestAmountAnalyse extends DataAnalyse
{
	

	public HighestAmountAnalyse(List<PlayData> data)
	{
		super(data);
	}

	@Override
	protected HashMap<OfflinePlayer, Double> prepareData()
	{
		HashMap<OfflinePlayer, Double> resultHashMap = new HashMap<>();
		
		Iterator<PlayData> iterator = data.iterator();
		
		while(iterator.hasNext())
		{
			PlayData playData = iterator.next();
			
			if(resultHashMap.containsKey(playData.Player))
			{
				if(resultHashMap.get(playData.Player) < playData.WonAmount)
					resultHashMap.put(playData.Player, playData.WonAmount);
			}
			else
			{
				resultHashMap.put(playData.Player, playData.WonAmount);
			}
		}
		return resultHashMap;
	}

	@Override
	public LinkedHashMap<Integer, Query> getData(int from, int to)
	{
		return this.getData(from, to, comparator);
	}

}
