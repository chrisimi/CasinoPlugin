package scripts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import animations.LeaderboardsignAnimation;
import hologramsystem.LBHologram;
import serializeableClass.Leaderboardsign.Cycle;
import serializeableClass.PlayData;

public class DataQueryManager
{
	public class Query 
	{
		double value;
		OfflinePlayer player;
	}
	private class QueryPost 
	{
		public boolean isServerSign;
		public OfflinePlayer player;
		public Cycle cycleMode;
		public long lastManualReset;
		public long validUntil;
		public Calendar startDate;
		public Calendar endDate;
		
		public QueryPost(LeaderboardsignAnimation animation)
		{
			this.isServerSign = animation.sign.isServerSign();
			this.player = animation.sign.getPlayer();
			this.cycleMode = animation.sign.cycleMode;
			this.lastManualReset = animation.sign.lastManualReset;
			this.validUntil = animation.sign.validUntil;
			this.startDate = animation.getStartDateOfSign();
			this.endDate = animation.getEndDateOfSign();
		}
		public QueryPost(LBHologram hologram)
		{
			this.isServerSign = hologram.isServerHologram();
			this.player = hologram.getOwner();
			this.cycleMode = hologram.cycleMode;
			this.lastManualReset = hologram.lastManualReset;
			this.validUntil = hologram.validuntil;
			
		}
	}
	
	
	public static Query getQuery(LeaderboardsignAnimation animation)
	{
		
	}
	
	private List<PlayData> getData(QueryPost post)
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
