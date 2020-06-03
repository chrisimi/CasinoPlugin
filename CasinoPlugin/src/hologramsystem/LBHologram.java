package hologramsystem;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import com.google.gson.annotations.Expose;

import serializeableClass.Leaderboardsign.Cycle;
import serializeableClass.Leaderboardsign.Mode;

public class LBHologram
{
	@Expose
	public String world;
	@Expose
	public double x;
	@Expose
	public double y;
	@Expose
	public double z;
	
	@Expose
	public String ownerUUID;
	
	@Expose
	public Mode mode;
	
	@Expose
	public Cycle cycleMode;
	
	@Expose
	public String hologramName;
	
	@Expose
	public int minPosition;
	@Expose
	public int maxPosition;
	
	@Expose
	public boolean useAllMode;
	@Expose
	public int range;
	
	@Expose
	public long lastManualReset;
	@Expose
	public long validuntil;
	
	
	
	
	public boolean isServerHologram()
	{
		return ownerUUID.equals("server");
	}
	public Location getLocation()
	{
		return new Location(Bukkit.getWorld(world), x, y, z);
	}
	public void setLocation(Location lrc)
	{
		this.x = lrc.getX();
		this.y = lrc.getY();
		this.z = lrc.getZ();
		this.world = lrc.getWorld().getName();
	}
	public OfflinePlayer getOwner()
	{
		if(isServerHologram()) return null;
		
		return Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID));
	}
}
