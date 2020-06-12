package com.chrisimi.casinoplugin.serializables;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class PlayerSigns {

	@Expose
	public ArrayList<PlayerSignsConfiguration> playerSigns = new ArrayList<PlayerSignsConfiguration>();
}
