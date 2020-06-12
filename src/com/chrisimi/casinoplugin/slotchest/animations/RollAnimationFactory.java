package com.chrisimi.casinoplugin.slotchest.animations;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.slotchest.SlotChest;

public class RollAnimationFactory {

	public static IRollAnimation GetRollAnimation(Main main, SlotChest slotChest, Player player) {
		switch(slotChest.animationID) {
		case 0:
			return new NormalLeftToRightAnimation(main, slotChest, player);
		case 1:
			return new NormalRightToLeftAnimation(main, slotChest, player);
			
		case 2:
			return new NormalLeftToRightAnimation(main, slotChest, player);
		}
		return null;
		
	}
	
	public static String[] getNameOfAllAnimations() {
		return new String[] {"RightToLeft", "LeftToRight"};
	}
}
