package slotChests.Animations;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.chrisimi.casino.main.Main;

import slotChests.SlotChest;

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
