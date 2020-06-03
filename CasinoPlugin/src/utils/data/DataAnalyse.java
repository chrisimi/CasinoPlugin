package utils.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.OfflinePlayer;

import serializeableClass.PlayData;

public abstract class DataAnalyse
{
	protected final List<PlayData> data;
	public DataAnalyse(List<PlayData> data)
	{
		this.data = data;
	}
	
	protected abstract HashMap<OfflinePlayer, Double> prepareData();
	
	protected LinkedHashMap<OfflinePlayer, Double> analyseData(Comparator<Entry<OfflinePlayer, Double>> comp)
	{
		List<Entry<OfflinePlayer, Double>> listOfEntries = new ArrayList<>(prepareData().entrySet());
		Collections.sort(listOfEntries, comp);
		
		LinkedHashMap<OfflinePlayer, Double> sortedMap = new LinkedHashMap<OfflinePlayer, Double>(listOfEntries.size());
		for(Entry<OfflinePlayer, Double> entry : listOfEntries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	public abstract LinkedHashMap<Integer, Query> getData(int from, int to);
}
