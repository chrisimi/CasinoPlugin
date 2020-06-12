package com.chrisimi.casinoplugin.utils.data;

import org.bukkit.OfflinePlayer;

public class Query 
{
	public double value;
	public OfflinePlayer player;
	
	@Override
	public String toString()
	{
		return player.getName() + " " + value;
	}
}
