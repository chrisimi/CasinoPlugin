package utils.data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.OfflinePlayer;

import serializeableClass.PlayData;

/**
 * class for analysing data 
 * current mode: Sumamount
 * {@link Mode}
 * 
 * @author chris
 *
 */
public class SumamountAnalyse extends DataAnalyse
{
	private static Comparator<Entry<OfflinePlayer, Double>> comparator = new Comparator<Map.Entry<OfflinePlayer,Double>>()
	{
		@Override
		public int compare(Entry<OfflinePlayer, Double> o1, Entry<OfflinePlayer, Double> o2)
		{
			return o2.getValue().compareTo(o1.getValue());
		}
	}; 
	
	
	public SumamountAnalyse(List<PlayData> data)
	{
		super(data);
	}

	@Override
	protected HashMap<OfflinePlayer, Double> prepareData()
	{
		HashMap<OfflinePlayer, Double> resultHashMap = new HashMap<OfflinePlayer, Double>();
		
		Iterator<PlayData> iterator = data.iterator();
		
		while(iterator.hasNext())
		{
			PlayData data = iterator.next();
			if(resultHashMap.containsKey(data.Player))
			{
				resultHashMap.compute(data.Player, (a, b) -> b + data.PlayAmount);
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
