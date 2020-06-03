package hologramsystem;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.google.gson.annotations.Expose;

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
	public String hologramName;
	
	@Expose
	public int minPosition;
	@Expose
	public int maxPosition;
	
	@Expose
	public boolean useAllMode;
	@Expose
	public int range;
	
	
	
	
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
}
