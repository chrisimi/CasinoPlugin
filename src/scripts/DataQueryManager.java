
package scripts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import animations.LeaderboardsignAnimation;
import hologramsystem.LBHologram;
import serializeableClass.Leaderboardsign.Cycle;
import serializeableClass.Leaderboardsign.Mode;
import utils.CycleHelper;
import utils.data.CountAnalyse;
import utils.data.DataAnalyse;
import utils.data.HighestamountAnalyse;
import utils.data.HighestlossAnalyse;
import utils.data.Query;
import utils.data.QueryPost;
import utils.data.SumamountAnalyse;
import utils.data.SumlossAnalyse;
import serializeableClass.PlayData;

public class DataQueryManager
{

	public static Query getQuery(LeaderboardsignAnimation animation)
	{
		return null;
//		TODO
	}
	public static LinkedHashMap<Integer, Query> getQuery(LBHologram hologram)
	{
		List<PlayData> data = getData(new QueryPost(hologram));
		
		DataAnalyse analyse = getAnalyse(hologram.mode, data);
		return analyse.getData(hologram.minPosition, hologram.maxPosition);
	}
	
	private static DataAnalyse getAnalyse(Mode mode, List<PlayData> data)
	{
		switch (mode)
		{
		case COUNT:
			return new CountAnalyse(data);
		case SUMAMOUNT:
			return new SumamountAnalyse(data);
		case HIGHESTAMOUNT:
			return new HighestamountAnalyse(data);
		case SUMLOSS:
			return new SumlossAnalyse(data);
		case HIGHESTLOSS:
			return new HighestlossAnalyse(data);
		default:
			return null;
		}
	}
	
	private static List<PlayData> getData(QueryPost post)
	{
		List<PlayData> currentData = new ArrayList<>();
		if(post.cycleMode == Cycle.NaN && post.lastManualReset != 0)
		{
//			Calendar calendar = new GregorianCalendar();
//			calendar.set(Calendar.MILLISECOND, (int) this.sign.lastManualReset);
			if(post.isServerSign)
			{
				currentData = LeaderboardsignsManager.getPlayData(post.lastManualReset, System.currentTimeMillis());
			}
			else
			{
				currentData = LeaderboardsignsManager.getPlayData(post.player, post.lastManualReset, System.currentTimeMillis());				
			}
			//System.out.println("get a" + calendar.getTime().toString());
			
		} else if(post.cycleMode == Cycle.NaN && post.validUntil != 0)
		{
			if(post.isServerSign)
			{
				currentData = LeaderboardsignsManager.getPlayData(0, post.validUntil);
			}
			else
			{
				currentData = LeaderboardsignsManager.getPlayData(post.player, 0, post.validUntil);
			}
		}
		else
		{
			if(post.isServerSign)
			{
				currentData = LeaderboardsignsManager.getPlayData(post.startDate, post.endDate);
			}
			else
			{
				currentData = LeaderboardsignsManager.getPlayData(post.player, post.startDate, post.endDate);				
			}
		}
		return currentData;
	}
}
