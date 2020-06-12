package com.chrisimi.casinoplugin.utils.data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.OfflinePlayer;

import com.chrisimi.casinoplugin.serializables.PlayData;

public class HighestlossAnalyse extends DataAnalyse
{
	
	public HighestlossAnalyse(List<PlayData> data)
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
			PlayData data = iterator.next();
			
			if(data.WonAmount != 0.0) continue;
			
			if(resultHashMap.containsKey(data.Player))
			{
				if(resultHashMap.get(data.Player) < data.PlayAmount) resultHashMap.put(data.Player, data.PlayAmount);
			}
			else
			{
				resultHashMap.put(data.Player, data.PlayAmount);
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
