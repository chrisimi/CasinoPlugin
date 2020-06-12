package com.chrisimi.casinoplugin.serializables;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class Signs {

	@Expose
	public ArrayList<SignConfiguration> signs = new ArrayList<SignConfiguration>();
}
