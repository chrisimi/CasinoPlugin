package utils.data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.OfflinePlayer;

import scripts.CasinoManager;
import serializeableClass.PlayData;

/**
 * class for analysing datas 
 * type: Count
 * @author chris
 *
 */
public class CountAnalyse extends DataAnalyse
{
	//default comparator for count
	
	public static Comparator<Entry<OfflinePlayer, Double>> comparator = new Comparator<Entry<OfflinePlayer, Double>>()
	{

		@Override
		public int compare(Entry<OfflinePlayer, Double> o1, Entry<OfflinePlayer, Double> o2)
		{
			return o2.getValue().compareTo(o1.getValue());
		}
	};
	
	public CountAnalyse(List<PlayData> data) { super(data); }

	@Override
	protected HashMap<OfflinePlayer, Double> prepareData()
	{
		HashMap<OfflinePlayer, Double> map = new HashMap<>();
		for(PlayData data : data) {
			if(map.containsKey(data.Player)) {
				map.compute(data.Player, (a, b) -> b + 1.0);
			} else {
				map.put(data.Player, 1.0);
			}
		}
		
		CasinoManager.Debug(this.getClass(), "prepareData: ");
		for(Entry<OfflinePlayer, Double> entry : map.entrySet())
			CasinoManager.Debug(this.getClass(), entry.getKey().getName() + " " + entry.getValue());
		
		return map;
	}

	@Override
	public LinkedHashMap<Integer, Query> getData(int from, int to)
	{
		return this.getData(from, to, comparator);
	}

}
