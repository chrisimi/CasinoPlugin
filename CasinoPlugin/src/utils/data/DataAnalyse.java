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
/**
 * abstract class for analyse data for holograms and leaderboard signs
 * @author chris
 *
 */
public abstract class DataAnalyse
{
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
	 * get the analysed data in a {@link LinkedHashMap}
	 * @param from start Position 
	 * @param to end position. Have to be higher than @param from
	 * @return {@link LinkedHashMap} containing the number of the position as key and the query for the position as value
	 */
	public abstract LinkedHashMap<Integer, Query> getData(int from, int to);
}
