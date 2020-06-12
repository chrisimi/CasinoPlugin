package com.chrisimi.casinoplugin.serializables;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.google.gson.annotations.Expose;

public class SignConfiguration {

	@Expose
	public String worldname;
	@Expose
	public double x;
	@Expose
	public double y;
	@Expose
	public double z;
	@Expose
	public Double bet;
	
	
	
	public SignConfiguration() {}
	
	public SignConfiguration(Location lrc, Double bet) {
		this.worldname = lrc.getWorld().getName();
		this.x = lrc.getX();
		this.y = lrc.getY();
		this.z = lrc.getZ();
		this.bet = bet;
	}
	
	public Location getLocation() {
		return new Location(Bukkit.getWorld(worldname), this.x, this.y, this.z);
	}
}
