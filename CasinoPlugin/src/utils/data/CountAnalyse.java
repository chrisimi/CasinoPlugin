package utils.data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.OfflinePlayer;

import serializeableClass.PlayData;

public class CountAnalyse extends DataAnalyse
{
	
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
		return map;
	}

	@Override
	public LinkedHashMap<Integer, Query> getData(int from, int to)
	{
		LinkedHashMap<OfflinePlayer, Double> values = analyseData(comparator);
		
		LinkedHashMap<Integer, Query> result = new LinkedHashMap<>();
		
		int place = 1;
		
		for(Entry<OfflinePlayer, Double> entry : values.entrySet())
		{
			if(place >= from && place >= to)
			{
				Query query = new Query();
				query.player = entry.getKey();
				query.value = entry.getValue();
				
				final int currPos = place;
				result.put(currPos, query);
			}
			place++;
		}
		return result;
	}

}
