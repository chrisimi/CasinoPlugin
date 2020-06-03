package animations;

import java.text.DateFormat;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;

import com.chrisimi.casino.main.Main;

import scripts.CasinoManager;
import scripts.LeaderboardsignsManager;
import serializeableClass.Leaderboardsign;
import serializeableClass.PlayData;
import serializeableClass.Leaderboardsign.Cycle;
import serializeableClass.Leaderboardsign.Mode;
import utils.CycleHelper;


public class LeaderboardsignAnimation implements Runnable
{
	public final Main main;
	public final Sign signBlock;
	public final Leaderboardsign sign;
	
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
		
		signBlock.setLine(1, "§4updating...");
		signBlock.setLine(2, "§4updating...");
		signBlock.update(true);
		CasinoManager.Debug(this.getClass(), sign.getLocation().toString() + " run ");
		getData();
		analyseData();
		writeSign();
	}
	
	private void getData()
	{
		if(this.sign.cycleMode == Cycle.NaN && this.sign.lastManualReset != 0)
		{
//			Calendar calendar = new GregorianCalendar();
//			calendar.set(Calendar.MILLISECOND, (int) this.sign.lastManualReset);
			if(sign.isServerSign())
			{
				currentData = LeaderboardsignsManager.getPlayData(this.sign.lastManualReset, System.currentTimeMillis());
			}
			else
			{
				currentData = LeaderboardsignsManager.getPlayData(sign.getPlayer(), this.sign.lastManualReset, System.currentTimeMillis());				
			}
			//System.out.println("get a" + calendar.getTime().toString());
			
		} else if(this.sign.cycleMode == Cycle.NaN && this.sign.validUntil != 0)
		{
			if(sign.isServerSign())
			{
				currentData = LeaderboardsignsManager.getPlayData(0, this.sign.validUntil);
			}
			else
			{
				currentData = LeaderboardsignsManager.getPlayData(this.sign.getPlayer(), 0, this.sign.validUntil);
			}
		}
		else
		{
			if(sign.isServerSign())
			{
				currentData = LeaderboardsignsManager.getPlayData(CycleHelper.getStartDateOfSign(this.sign.cycleMode), CycleHelper.getEndDateOfSign(this.sign.cycleMode));
			}
			else
			{
				currentData = LeaderboardsignsManager.getPlayData(sign.getPlayer(), CycleHelper.getStartDateOfSign(this.sign.cycleMode), CycleHelper.getEndDateOfSign(this.sign.cycleMode));				
			}
		}
			
		CasinoManager.Debug(this.getClass(), "found datasets: " +  currentData.size());
		CasinoManager.Debug(this.getClass(), "from: " + CycleHelper.getStartDateOfSign(this.sign.cycleMode).getTime().toString() + " to: " + CycleHelper.getEndDateOfSign(this.sign.cycleMode).getTime().toString() + " - " + this.sign.cycleMode.toString());
		
		if(!(sign.modeIsAll())) 
		{
			int range = sign.getRange();
			ArrayList<PlayData> dataToRemove = new ArrayList<>();
			for(PlayData data : currentData) {
				if(data.Location.distance(signBlock.getLocation()) > range) {
					dataToRemove.add(data);
				}
			}
			currentData.removeAll(dataToRemove);
		}
		CasinoManager.Debug(this.getClass(), "after sorting out, datasets: " + currentData.size());
	}
	private void analyseData()
	{
		
		//cool sign animations
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
		case HIGHESTLOSS:
			analyseHighestLoss();
			break;
		case SUMLOSS:
			analyseSumLoss();
		}
	}
	private void analyseHighestLoss()
	{
		HashMap<OfflinePlayer, Double> map = new HashMap<>();
		for(PlayData data : currentData)
		{
			if(data.WonAmount != 0) continue;
			
			if(map.containsKey(data.Player) && map.get(data.Player) < data.PlayAmount)
				map.put(data.Player, data.PlayAmount);
			else if(!(map.containsKey(data.Player)))
				map.put(data.Player, data.WonAmount);
		}
		
		Comparator<Entry<OfflinePlayer, Double>> valueComparator = new Comparator<Map.Entry<OfflinePlayer,Double>>()
		{

			@Override
			public int compare(Entry<OfflinePlayer, Double> o1, Entry<OfflinePlayer, Double> o2)
			{
				return o2.getValue().compareTo(o1.getValue());
			}
		};
		List<Entry<OfflinePlayer, Double>> listOfEntries = new ArrayList<>(map.entrySet());
		Collections.sort(listOfEntries, valueComparator);
		
		LinkedHashMap<OfflinePlayer, Double> sortedMap = new LinkedHashMap<OfflinePlayer, Double>(listOfEntries.size());
		for(Entry<OfflinePlayer, Double> entry : listOfEntries)
		{
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		
		int index = 1;
		CasinoManager.Debug(this.getClass(), "Result of analyse highest loss");
		for(Entry<OfflinePlayer, Double> entry : sortedMap.entrySet())
		{
			CasinoManager.Debug(this.getClass(), entry.getKey().getName() + " - " + entry.getValue());
			if(index == sign.position)
			{
				currentPlayer = entry.getKey();
				currentValue = entry.getValue();
				break;
			}
			else
				index++;
		}
		if(sortedMap.size() == 0)
		{
			currentPlayer = null;
			currentValue = 0.0;
		}
		
	}


	private void analyseSumLoss()
	{
		HashMap<OfflinePlayer, Double> map = new HashMap<>();
		for(PlayData data : currentData)
		{
			if(data.WonAmount != 0) continue;
			
			if(map.containsKey(data.Player))
				map.compute(data.Player, (a, b) -> b + data.PlayAmount);
			else
				map.put(data.Player, data.PlayAmount);
		}
		
		Comparator<Entry<OfflinePlayer, Double>> valueComperator = new Comparator<Map.Entry<OfflinePlayer,Double>>()
		{

			@Override
			public int compare(Entry<OfflinePlayer, Double> o1, Entry<OfflinePlayer, Double> o2)
			{
				return o2.getValue().compareTo(o1.getValue());
			}
		};
		
		List<Entry<OfflinePlayer, Double>> listOfEntries = new ArrayList<>(map.entrySet());
		Collections.sort(listOfEntries, valueComperator);
		
		LinkedHashMap<OfflinePlayer, Double> sortedMap = new LinkedHashMap<OfflinePlayer, Double>(listOfEntries.size());
		for(Entry<OfflinePlayer, Double> entry : listOfEntries)
		{
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		
		int index = 1;
		CasinoManager.Debug(this.getClass(), "Result of analyse sum loss");
		for(Entry<OfflinePlayer, Double> entry : sortedMap.entrySet())
		{
			CasinoManager.Debug(this.getClass(), entry.getKey().getName() + " - " + entry.getValue());
			if(index == sign.position)
			{
				currentPlayer = entry.getKey();
				currentValue = entry.getValue();
				break;
			}
			else
				index++;
		}
		if(sortedMap.size() == 0)
		{
			currentPlayer = null;
			currentValue = 0.0;
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
		
		int index = 1;
		CasinoManager.Debug(this.getClass(), "Result of analyse count: ");
		for(Entry<OfflinePlayer, Integer> entry : sortedMap.entrySet()) {
			CasinoManager.Debug(this.getClass(), entry.getKey().getName() + " - " + entry.getValue());
			if(index == sign.position) 
			{
				currentPlayer = entry.getKey();
				currentValue = entry.getValue();
				
				break;
				
			} else
			index++;
		}
		if(sortedMap.size() == 0) 
		{
			currentPlayer = null;
			currentValue = 0.0;
		}
	}
	private void analyseDataHighestAmount() 
	{
		HashMap<OfflinePlayer, Double> map = new HashMap<>();
		for(PlayData data : currentData) {
			if(data.WonAmount <= 0) continue;
			
			if(map.containsKey(data.Player)) {
				
				//check if the current wonAmount is higher than the one in the list!
				if(map.get(data.Player) < data.WonAmount)
					map.put(data.Player, data.WonAmount);

			} else {
				map.put(data.Player, data.WonAmount);
			}
		}
		
		Comparator<Entry<OfflinePlayer, Double>> valueComperator = new Comparator<Entry<OfflinePlayer, Double>>()
		{
			@Override
			public int compare(Entry<OfflinePlayer, Double> v1, Entry<OfflinePlayer, Double> v2)
			{
				return (int) (v2.getValue() - v1.getValue());
			}
		};
		List<Entry<OfflinePlayer, Double>> listOfEntries = new ArrayList<>(map.entrySet());
		Collections.sort(listOfEntries, valueComperator);
		
		LinkedHashMap<OfflinePlayer, Double> sortedMap = new LinkedHashMap<OfflinePlayer, Double>(listOfEntries.size());
		
		for(Entry<OfflinePlayer, Double> entry : listOfEntries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		
		int index = 1;
		CasinoManager.Debug(this.getClass(), "result of analyse highestamount: ");
		for(Entry<OfflinePlayer, Double> entry : sortedMap.entrySet()) {
			CasinoManager.Debug(this.getClass(), entry.getKey().getName() + " - " + entry.getValue());
			if(index == sign.position) 
			{
				currentPlayer = entry.getKey();
				currentValue = entry.getValue();
				
				break;
				
			} else
			index++;
		}
		if(sortedMap.size() == 0) 
		{
			currentPlayer = null;
			currentValue = 0.0;
		}
	}
	private void analyseDataSumAmount()
	{
		HashMap<OfflinePlayer, Double> map = new HashMap<>();
		for(PlayData data : currentData) {
			if(data.WonAmount <= 0) continue;
			
			if(map.containsKey(data.Player)) {
				map.compute(data.Player, (a, b) -> b + data.WonAmount);
			} else {
				map.put(data.Player, data.WonAmount);
			}
		}
		
		
		
		
		Comparator<Entry<OfflinePlayer, Double>> valueComperator = new Comparator<Entry<OfflinePlayer, Double>>()
		{
			@Override
			public int compare(Entry<OfflinePlayer, Double> v1, Entry<OfflinePlayer, Double> v2)
			{
				return (int) (v2.getValue() - v1.getValue());
			}
		};
		List<Entry<OfflinePlayer, Double>> listOfEntries = new ArrayList<>(map.entrySet());
		Collections.sort(listOfEntries, valueComperator);
		
		LinkedHashMap<OfflinePlayer, Double> sortedMap = new LinkedHashMap<OfflinePlayer, Double>(listOfEntries.size());
		
		for(Entry<OfflinePlayer, Double> entry : listOfEntries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		
		int index = 1;
		CasinoManager.Debug(this.getClass(), "result of analyse sumamount");
		for(Entry<OfflinePlayer, Double> entry : sortedMap.entrySet()) {
			CasinoManager.Debug(this.getClass(), entry.getKey().getName() + " - " + entry.getValue());
			if(index == sign.position) 
			{
				currentPlayer = entry.getKey();
				currentValue = entry.getValue();
				
				break;
				
			} else
			index++;
		}
		if(sortedMap.size() == 0) 
		{
			currentPlayer = null;
			currentValue = 0.0;
		}
	}
	private void writeSign() 
	{
		signBlock.setLine(0, "§6§l"+String.valueOf(sign.position)+". Place");
		signBlock.setLine(1, (currentPlayer == null) ? "§4---" : currentPlayer.getName());
		if(sign.getMode() != Mode.COUNT)
			signBlock.setLine(2, (currentValue == 0.0) ? "§4---" : Main.econ.format(currentValue));
		else
			signBlock.setLine(2, (currentValue == 0.0) ? "§4---" : String.valueOf(currentValue));
		
		if(sign.animationCount == 0)
		{
//			signBlock.setLine(3, getFromDateSignString());
			signBlock.setLine(3, CycleHelper.getDateStringFromCycle(this.sign.cycleMode, CycleHelper.getStartDateOfSign(this.sign.cycleMode)));
			sign.animationCount = 1;
		}
		else
		{
//			signBlock.setLine(3, getTodateSignString());
			signBlock.setLine(3, CycleHelper.getDateStringFromCycle(this.sign.cycleMode, CycleHelper.getEndDateOfSign(this.sign.cycleMode)));
			sign.animationCount = 0;
		}
		if(this.sign.cycleMode == Cycle.NaN && this.sign.lastManualReset != 0)
		{
			DateFormat dfa = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			signBlock.setLine(3, "§ar: " + dfa.format(new Date(this.sign.lastManualReset)));
		} else if(this.sign.cycleMode == Cycle.NaN && this.sign.validUntil != 0)
		{
			DateFormat dfa = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			signBlock.setLine(3, "§av: " + dfa.format(new Date(this.sign.validUntil)));
		}
		
		signBlock.update(true);
	}
}
