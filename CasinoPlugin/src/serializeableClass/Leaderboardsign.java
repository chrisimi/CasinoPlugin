package serializeableClass;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.google.gson.annotations.Expose;

public class Leaderboardsign {

	public enum Mode {
		HIGHESTAMOUNT,
		COUNT,
		SUMAMOUNT
	}
	
	
	@Expose
	public String ownerUUID;
	@Expose
	public String range; //range like how many blocks (all/30)
	@Expose
	public String mode; //mode like highestAmount, count, sumAmount, 
	
	//for location
	@Expose
	public double x;
	@Expose
	public double y;
	@Expose
	public double z;
	@Expose
	public String world;
	
	
	public void setLocation(Location lrc) {
		this.x = lrc.getX();
		this.y = lrc.getY();
		this.z = lrc.getZ();
		this.world = lrc.getWorld().getName();
	}
	public void setMode(Mode mode) {
		this.mode = mode.toString();
	}
	public void setPlayer(Player player) {
		this.ownerUUID = player.getUniqueId().toString();
	}
	
	public Location getLocation() {
		return new Location(Bukkit.getWorld(this.world), x, y, z);
	}
	public Mode getMode() {
		return Enum.valueOf(Mode.class, this.mode);
	}
}
