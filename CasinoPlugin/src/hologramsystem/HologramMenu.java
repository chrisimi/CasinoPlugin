package hologramsystem;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.chrisimi.inventoryapi.IInventoryAPI;


public class HologramMenu implements IInventoryAPI
{
	private final Player player;
	//private final Inventory inventory;
	
	private ItemStack switchBetweenModes = new ItemStack(Material.GOLD_NUGGET);
	
	public HologramMenu(Player player)
	{
		this.player = player;
		//this.inventory = InventoryAPI.createInventory(player, 9 * 3, Main.getInstance(), "Hologram create menu");
		
		//inventory.addEvents(this);
		
	}
	
	

	@Override
	public void openInventory()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeInventory()
	{
		// TODO Auto-generated method stub
		
	}

}
