package com.chrisimi.casinoplugin.serializables;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.gson.annotations.Expose;

public class Leaderboardsign {

	public enum Mode {
		HIGHESTAMOUNT,
		COUNT,
		SUMAMOUNT,
		HIGHESTLOSS,
		SUMLOSS
	}
	public enum Cycle
	{
		NaN,
		HOUR,
		DAY,
		WEEK,
		MONTH,
		YEAR
	}
	
	@Expose
	public String ownerUUID;
	@Expose
	public String range; //range like how many blocks (all/30)
	@Expose
	public String mode; //mode like highestAmount, count, sumAmount, 
	@Expose
	public int position; //position like 1 for first, 2 for second
	
	@Expose
	public Cycle cycleMode;
	@Expose
	public long lastManualReset; //last manual reset from player
	@Expose
	public long validUntil; //how long the duration of the sign should work like for a event to sunday etc.
	
	
	//for location
	@Expose
	public double x;
	@Expose
	public double y;
	@Expose
	public double z;
	@Expose
	public String world;
	
	public int animationCount = 0;
	
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
	public void setRange(int count) {
		if(count <= 0) return;
		this.range = String.valueOf(count);
	}
	public void setRange(Boolean all) {
		
		if(all)
			this.range = "all";
	}
	
	public Location getLocation() {
		return new Location(Bukkit.getWorld(this.world), x, y, z);
	}
	public Mode getMode() {
		return Enum.valueOf(Mode.class, this.mode);
	}
	public OfflinePlayer getPlayer() {
		if(isServerSign()) return null;
		return Bukkit.getOfflinePlayer(UUID.fromString(this.ownerUUID));
	}
	public String getPlayerName()
	{
		if(isServerSign())
			return "§6Server";
		return getPlayer().getName();
	}
	
	public Boolean modeIsAll() {
		try {
			int a = Integer.valueOf(this.range);
			return false;
		} catch(Exception e) {
			return true;
		}
	}
	public Integer getRange() {
		try {
			
		
		return Integer.valueOf(this.range);
		} catch(Exception e) {
			return null;
		}
	}
	public Boolean isServerSign()
	{
		return ownerUUID.equalsIgnoreCase("server");
	}
}
