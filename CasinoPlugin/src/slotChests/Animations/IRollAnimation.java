package slotChests.Animations;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface IRollAnimation {

	//wird beim Initialisieren vom RollAnimationManager ausgeführt!
	void initialize();
	
	Inventory getInventory();
	
	//wird im rollThread jede x Ticks aufgerufen
	//return wird true zurückgeben, wenn man einen roll abziehen kann
	Boolean nextAnimation(int rollsLeft);
	
	//wenn alle rolls aufgebraucht wurden finish animation etc.
	ItemStack finish();
	
	//wenn Spieler in der Animation rausgeht
	void simulateEnding(int rollsLeft);
	
	//return animation ID von dieser Animation für JSON sicherung!
	int getAnimationID();
	
	int getInventorySize();
}
