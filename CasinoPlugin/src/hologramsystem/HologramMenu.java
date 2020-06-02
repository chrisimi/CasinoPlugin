package hologramsystem;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chrisimi.casino.main.Main;
import com.chrisimi.inventoryapi.ClickEvent;
import com.chrisimi.inventoryapi.IInventoryAPI;
import com.chrisimi.inventoryapi.Inventory;
import com.chrisimi.inventoryapi.InventoryAPI;

import serializeableClass.Leaderboardsign.Mode;


public class HologramMenu implements IInventoryAPI
{
	private enum WaitingFor {
		NONE,
		LOCATION
	}
	
	
	private final Player player;
	private final Inventory inventory;
	private final org.bukkit.inventory.Inventory bukkitInventory;
	
	private Mode currentMode = Mode.HIGHESTAMOUNT;
	private WaitingFor waitingForChatInput = WaitingFor.NONE;
	
	private ItemStack switchBetweenModes = new ItemStack(Material.GOLD_NUGGET);
	private ItemStack choosePosition = new ItemStack(Material.SIGN);
	
	
	public HologramMenu(Player player)
	{
		this.player = player;
		this.inventory = InventoryAPI.createInventory(player, 9 * 3, Main.getInstance(), "Hologram create menu");
		this.bukkitInventory = inventory.getInventory();
		inventory.addEvents(this);
		
		
		openInventory();
		initialize();
	}
	
	@com.chrisimi.inventoryapi.EventMethodAnnotation
	public void onInventoryClick(ClickEvent event)
	{
		if(event.getClicked().equals(switchBetweenModes)) clickSwitchMode();
		
		updateInventory();
	}
	
	private void clickSwitchMode()
	{
		System.out.println("higher");
		Mode[] modes = Mode.values();
		
		for(int i = 0; i < modes.length; i++)
		{
			if(currentMode.equals(modes[i]))
			{
				if(i != modes.length - 1)
					currentMode = modes[i + 1];
				else
					currentMode = modes[0];
				
				break;
			}
		}
	}
	
	
	private void initialize()
	{
		ItemMeta meta = switchBetweenModes.getItemMeta();
		meta.setDisplayName("§6change leaderboard mode");
		switchBetweenModes.setItemMeta(meta);
		
		updateInventory();
		
	}
	private void updateInventory()
	{
		System.out.println("currentmode: " + currentMode.toString());
		ItemMeta meta = switchBetweenModes.getItemMeta();
		meta.setLore(getLoreForModes());
		switchBetweenModes.setItemMeta(meta);
		bukkitInventory.setItem(4, switchBetweenModes);
	}
	

	@Override
	public void openInventory()
	{
		inventory.openInventory();
	}

	@Override
	public void closeInventory()
	{
		inventory.closeInventory();
	}
	private List<String> getLoreForModes()
	{
		List<String> result = new ArrayList<>();
		for(Mode mode : Mode.values())
		{
			if(mode.equals(currentMode)) 
				result.add("§6§l" + mode.toString());
			else
				result.add("§6" + mode.toString());
		}
		return result;
	}
}
