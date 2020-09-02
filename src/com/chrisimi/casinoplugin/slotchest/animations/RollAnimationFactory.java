package com.chrisimi.casinoplugin.slotchest.animations;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.slotchest.SlotChest;

public class RollAnimationFactory {

	//TODO fix switch issue and add documentation

	/**
	 * get the RollAnimation for the slotchest
	 * @param slotChest instance of the slot chest
	 * @param player player
	 * @return a IRollAnimation implementation
	 */
	public static IRollAnimation GetRollAnimation(SlotChest slotChest, Player player)
	{
		switch(slotChest.animationID) {
		case 0:
		case 2:
			return new NormalLeftToRightAnimation(slotChest, player);
		case 1:
			return new NormalRightToLeftAnimation(slotChest, player);
		}
		return null;
		
	}

	/**
	 * get the name of all animations to use for example for lores
	 * @return {@linkplain String} array containg the names of the animations
	 */
	public static String[] getNameOfAllAnimations() {
		return new String[] {"RightToLeft", "LeftToRight"};
	}
}
