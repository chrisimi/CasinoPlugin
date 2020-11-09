
package com.chrisimi.casinoplugin.scripts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.chrisimi.casinoplugin.animations.LeaderboardsignAnimation;
import com.chrisimi.casinoplugin.hologramsystem.LBHologram;
import com.chrisimi.casinoplugin.serializables.PlayData;
import com.chrisimi.casinoplugin.serializables.Leaderboardsign.Cycle;
import com.chrisimi.casinoplugin.serializables.Leaderboardsign.Mode;
import com.chrisimi.casinoplugin.utils.data.CountAnalyse;
import com.chrisimi.casinoplugin.utils.data.DataAnalyse;
import com.chrisimi.casinoplugin.utils.data.HighestAmountAnalyse;
import com.chrisimi.casinoplugin.utils.data.HighestlossAnalyse;
import com.chrisimi.casinoplugin.utils.data.Query;
import com.chrisimi.casinoplugin.utils.data.QueryPost;
import com.chrisimi.casinoplugin.utils.data.SumamountAnalyse;
import com.chrisimi.casinoplugin.utils.data.SumlossAnalyse;

public class DataQueryManager
{
	@Deprecated
	public static Query getQuery(LeaderboardsignAnimation animation)
	{
		return null;
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
			return new HighestAmountAnalyse(data);
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
				currentData = DataManager.dataBase.getPlayData(post.lastManualReset, System.currentTimeMillis());
			}
			else
			{
				currentData = DataManager.dataBase.getPlayData(post.player, post.lastManualReset, System.currentTimeMillis());
			}
			//System.out.println("get a" + calendar.getTime().toString());
			
		} else if(post.cycleMode == Cycle.NaN && post.validUntil != 0)
		{
			if(post.isServerSign)
			{
				currentData = DataManager.dataBase.getPlayData(0, post.validUntil);
			}
			else
			{
				currentData = DataManager.dataBase.getPlayData(post.player, 0, post.validUntil);
			}
		}
		else
		{
			if(post.isServerSign)
			{
				currentData = DataManager.dataBase.getPlayData(post.startDate, post.endDate);
			}
			else
			{
				currentData = DataManager.dataBase.getPlayData(post.player, post.startDate, post.endDate);
			}
		}
		return currentData;
	}
}
