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

import scripts.LeaderboardsignsManager;
import serializeableClass.Leaderboardsign;
import serializeableClass.PlayData;
import serializeableClass.Leaderboardsign.Cycle;
import serializeableClass.Leaderboardsign.Mode;


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
		System.out.println("run");
		//will be called every frame
		if(!(signBlock.isPlaced())) return;
		
		signBlock.setLine(1, "§4updating...");
		signBlock.setLine(2, "§4updating...");
		signBlock.update(true);
		
		getData();
		analyseData();
		writeSign();
	}
	
	private void getData()
	{
		if(this.sign.cycleMode == Cycle.NaN && this.sign.lastManualReset != 0)
		{
			Calendar calendar = new GregorianCalendar();
			calendar.set(Calendar.MILLISECOND, (int) this.sign.lastManualReset);
			if(sign.isServerSign())
			{
				currentData = LeaderboardsignsManager.getPlayData(this.sign.lastManualReset, System.currentTimeMillis());
			}
			else
			{
				currentData = LeaderboardsignsManager.getPlayData(sign.getPlayer(), this.sign.lastManualReset, System.currentTimeMillis());				
			}
			System.out.println("get a" + calendar.getTime().toString());
			
		}
		else
		{
			if(sign.isServerSign())
			{
				currentData = LeaderboardsignsManager.getPlayData(getStartDateOfSign(), getEndDateOfSign());
			}
			else
			{
				currentData = LeaderboardsignsManager.getPlayData(sign.getPlayer(), getStartDateOfSign(), getEndDateOfSign());				
			}
		}
			
		
		System.out.println("from: " + getStartDateOfSign().getTime().toString() + " to: " + getEndDateOfSign().getTime().toString() + " - " + this.sign.cycleMode.toString());
		
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
		for(Entry<OfflinePlayer, Integer> entry : sortedMap.entrySet()) {
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
		for(Entry<OfflinePlayer, Double> entry : sortedMap.entrySet()) {
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
		for(Entry<OfflinePlayer, Double> entry : sortedMap.entrySet()) {
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
			signBlock.setLine(3, getFromDateSignString());
			sign.animationCount = 1;
		}
		else
		{
			signBlock.setLine(3, getTodateSignString());
			sign.animationCount = 0;
		}
		if(this.sign.cycleMode == Cycle.NaN && this.sign.lastManualReset != 0)
		{
			DateFormat dfa = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			signBlock.setLine(3, "§ar: " + dfa.format(new Date(this.sign.lastManualReset)));
		}
		
		signBlock.update(true);
	}
	//get calendar for data filter
	private Calendar getStartDateOfSign()
	{
		Calendar now = new GregorianCalendar();
		switch (this.sign.cycleMode)
		{
		case YEAR:
			
			return new GregorianCalendar(now.get(Calendar.YEAR), 0, 1, 0, 0, 1);
		case MONTH:
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 0);
		case WEEK:
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH) - now.get(Calendar.DAY_OF_WEEK_IN_MONTH), 0, 0, 1);
		case DAY:
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		case HOUR:
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY), 0, 0);
		default:
			return new GregorianCalendar(2000, 1, 0);
		}
	}
	//string for the sign
	private String getFromDateSignString()
	{
		switch (this.sign.cycleMode)
		{
		case YEAR:
		case MONTH:
		case WEEK:
		case DAY:
			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
			return "§a" + df.format(getStartDateOfSign().getTime());
			
		case HOUR:
			DateFormat dfa = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			
			return "§a" + dfa.format(getStartDateOfSign().getTime());

		default:
			return "§6------";
		}
		
	}
	
	//get calendar for data filter
	private Calendar getEndDateOfSign()
	{
		Calendar now = new GregorianCalendar();
		switch (this.sign.cycleMode)
		{
		case YEAR:
			
			return new GregorianCalendar(now.get(Calendar.YEAR), 11, 30, 23, 59, 59);
		case MONTH:
			Calendar nowMonth = new GregorianCalendar();
			nowMonth.set(Calendar.MONTH, nowMonth.get(Calendar.MONTH) + 1);
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, nowMonth.getActualMinimum(Calendar.DAY_OF_MONTH), 23, 59, 59);
		case WEEK:
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH) + (7 - now.get(Calendar.DAY_OF_WEEK_IN_MONTH)) - 1, 23, 59, 59);
		case DAY:
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
		case HOUR:
			return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY), 59, 59);
		default:
			return new GregorianCalendar(2100, 1, 0);
		}
	}
	//string for the sign
	private String getTodateSignString()
	{
		switch (this.sign.cycleMode)
		{
		case YEAR:
		case MONTH:
		case WEEK:
		case DAY:
			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
			return "§c" + df.format(getEndDateOfSign().getTime());
			
		case HOUR:
			DateFormat dfa = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			return "§c" + dfa.format(getEndDateOfSign().getTime());

		default:
			return "§6------";
		}
	}
}
