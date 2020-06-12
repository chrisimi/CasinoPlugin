package com.chrisimi.casinoplugin.utils.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.OfflinePlayer;

import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.serializables.PlayData;
/**
 * abstract class for analyse data for holograms and leaderboard signs
 * @author chris
 *
 */
public abstract class DataAnalyse
{ 
	
	public Comparator<Entry<OfflinePlayer, Double>> comparator = new Comparator<Map.Entry<OfflinePlayer,Double>>()
	{

		@Override
		public int compare(Entry<OfflinePlayer, Double> o1, Entry<OfflinePlayer, Double> o2)
		{
			return o2.getValue().compareTo(o1.getValue());
		}
	};
	
	/**
	 * list of data which the analyse should have
	 */
	protected final List<PlayData> data;
	/**
	 * 
	 * @param data list of {@link PlayData} containing the data for the analyse
	 */
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
	/**
	 * create a linkedhashmap containing the the sorted elements for the leaderboard or hologram and analyse the data from the list
	 * @param from start position
	 * @param to end position
	 * @param comp {@link Comparator} for the analyse of data
	 * @return {@link LinkedHashMap} keyd with the position and valued with the Query
	 */
	protected LinkedHashMap<Integer, Query> getData(int from, int to, Comparator<Entry<OfflinePlayer, Double>> comp)
	{
		
		LinkedHashMap<OfflinePlayer, Double> values = analyseData(comp);
		
		CasinoManager.Debug(this.getClass(), "getData size " + values.size());
		
		LinkedHashMap<Integer, Query> result = new LinkedHashMap<>();
		
		int place = 1;
		
		for(Entry<OfflinePlayer, Double> entry : values.entrySet())
		{
			CasinoManager.Debug(this.getClass(), entry.getKey().getName() + " " + entry.getValue());
			if(place >= from && place <= to)
			{
				Query query = new Query();
				query.player = entry.getKey();
				query.value = entry.getValue();
				
				final int currPos = place;
				result.put(currPos, query);
				CasinoManager.Debug(this.getClass(), entry.getKey().getName() + " " + entry.getValue() + " set for place " + currPos);
			}
			place++;
		}
		return result;
	}
	
	/**
	 * get the analysed data in a {@link LinkedHashMap}
	 * @param from start Position 
	 * @param to end position. Have to be higher than @param from
	 * @return {@link LinkedHashMap} containing the number of the position as key and the query for the position as value
	 */
	public abstract LinkedHashMap<Integer, Query> getData(int from, int to);
}
