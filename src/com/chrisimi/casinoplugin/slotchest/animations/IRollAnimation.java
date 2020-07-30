package com.chrisimi.casinoplugin.slotchest.animations;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface IRollAnimation {

	//TODO translate to english

	//wird beim Initialisieren vom RollAnimationManager ausgef§hrt!
	void initialize();
	
	Inventory getInventory();
	
	//wird im rollThread jede x Ticks aufgerufen
	//return wird true zur§ckgeben, wenn man einen roll abziehen kann
	Boolean nextAnimation(int rollsLeft);
	
	//wenn alle rolls aufgebraucht wurden finish animation etc.
	ItemStack finish();
	
	//wenn Spieler in der Animation rausgeht
	void simulateEnding(int rollsLeft);
	
	//return animation ID von dieser Animation f§r JSON sicherung!
	int getAnimationID();
	
	int getInventorySize();
}
