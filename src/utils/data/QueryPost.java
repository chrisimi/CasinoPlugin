package utils.data;

import java.util.Calendar;

import org.bukkit.OfflinePlayer;

import animations.LeaderboardsignAnimation;
import hologramsystem.LBHologram;
import serializeableClass.Leaderboardsign.Cycle;
import utils.CycleHelper;

public class QueryPost 
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
		this.startDate = CycleHelper.getStartDateOfSign(animation.sign.cycleMode);
		this.endDate = CycleHelper.getEndDateOfSign(animation.sign.cycleMode);
	}
	public QueryPost(LBHologram hologram)
	{
		this.isServerSign = hologram.isServerHologram();
		this.player = hologram.getOwner();
		this.cycleMode = hologram.cycleMode;
		this.lastManualReset = hologram.lastManualReset;
		this.validUntil = hologram.validuntil;
		this.startDate = CycleHelper.getStartDateOfSign(hologram.cycleMode);
		this.endDate = CycleHelper.getEndDateOfSign(hologram.cycleMode);
	}
}
