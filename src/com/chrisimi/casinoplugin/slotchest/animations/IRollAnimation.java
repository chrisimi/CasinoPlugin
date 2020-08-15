package com.chrisimi.casinoplugin.slotchest.animations;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface IRollAnimation {

	//TODO translate to english

	/**
	 * should be executes to initialize the inventory and get the needed things
	 */
	void initialize();

	/**
	 * get the inventory from the implementation
	 * @return {@linkplain Inventory} instance of the inventory used in the implemenation
	 */
	Inventory getInventory();
	
	//wird im rollThread jede x Ticks aufgerufen
	//return wird true zur§ckgeben, wenn man einen roll abziehen kann

	/**
	 * execute to animate one frame
	 * @param rollsLeft amount of rolls left
	 * @return true if there are enough rolls left (because of skipping frames), false if there aren't enough rolls
	 */
	Boolean nextAnimation(int rollsLeft);

	/**
	 * execute when finished with the animation
	 * @return {@linkplain ItemStack} instance of the item which the player won
	 */
	ItemStack finish();

	/**
	 * use this method when the player left from the server or closed the inventory
	 * @param rollsLeft amount of rolls left which should be simulated
	 */
	void simulateEnding(int rollsLeft);

	/**
	 * get the ID of this animation
	 * @return ID of the animation
	 */
	int getAnimationID();

	/**
	 * get the size of the inventory used in the implementation
	 * @return a valid size of the inventory
	 */
	int getInventorySize();
}
