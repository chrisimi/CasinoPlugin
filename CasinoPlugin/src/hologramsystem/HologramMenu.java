package hologramsystem;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.chrisimi.casino.main.Main;
import com.chrisimi.casino.main.MessageManager;
import com.chrisimi.inventoryapi.ChatEvent;
import com.chrisimi.inventoryapi.ClickEvent;
import com.chrisimi.inventoryapi.EventMethodAnnotation;
import com.chrisimi.inventoryapi.IInventoryAPI;
import com.chrisimi.inventoryapi.Inventory;
import com.chrisimi.inventoryapi.InventoryAPI;

import scripts.CasinoManager;
import serializeableClass.Leaderboardsign.Mode;


public class HologramMenu implements IInventoryAPI
{
	/*
	 * todo
	 *  function to set hologram name
	 *  when player has serversigns perms a block should be in the inventory to set if it's a server sign
	 * 
	 */
	
	private enum WaitingFor {
		NONE,
		LOCATION,
		POSITION,
		RANGE
	}
	
	
	private final Player player;
	private final Inventory inventory;
	private final org.bukkit.inventory.Inventory bukkitInventory;
	
	private Mode currentMode = Mode.HIGHESTAMOUNT;
	private int minPosition = 0;
	private int maxPosition = 0;
	private String nameOfHologram;
	private boolean useAllMode;
	private int range = 0;
	
	private boolean validValues = false;
	
	private WaitingFor waitingForChatInput = WaitingFor.NONE;
	
	private ItemStack switchBetweenModes = new ItemStack(Material.GOLD_NUGGET);
	private ItemStack choosePosition = new ItemStack(Material.SIGN);
	private ItemStack setLocation = scripts.Skull.getSkullByTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM4Y2YzZjhlNTRhZmMzYjNmOTFkMjBhNDlmMzI0ZGNhMTQ4NjAwN2ZlNTQ1Mzk5MDU1NTI0YzE3OTQxZjRkYyJ9fX0=");
	private ItemStack setRange = new ItemStack(Material.TRIPWIRE_HOOK);
	private ItemStack createHologram = new ItemStack(Material.STONE_BUTTON);
	
	public HologramMenu(Player player)
	{
		this.player = player;
		this.inventory = InventoryAPI.createInventory(player, 9 * 3, Main.getInstance(), "Hologram create menu");
		this.bukkitInventory = inventory.getInventory();
		inventory.addEvents(this);
		
		
		openInventory();
		initialize();
	}
	
	
	
	
	private void initialize()
	{
		ItemMeta meta = switchBetweenModes.getItemMeta();
		meta.setDisplayName("§6change leaderboard mode");
		switchBetweenModes.setItemMeta(meta);
		
		meta = choosePosition.getItemMeta();
		meta.setDisplayName("§6set showing positions");
		choosePosition.setItemMeta(meta);
		bukkitInventory.setItem(5, choosePosition);
		
		meta = setLocation.getItemMeta();
		meta.setDisplayName("§6set location (optional) your current position will be used");
		setLocation.setItemMeta(meta);
		bukkitInventory.setItem(6, setLocation);
		
		meta = setRange.getItemMeta();
		meta.setDisplayName("§6set range");
		setLocation.setItemMeta(meta);
		bukkitInventory.setItem(7, setRange);
		
		updateInventory();
		
	}
	private void updateInventory()
	{
		ItemMeta meta = switchBetweenModes.getItemMeta();
		meta.setLore(getLoreForModes());
		switchBetweenModes.setItemMeta(meta);
		bukkitInventory.setItem(4, switchBetweenModes);
		
		editCreateHologramButton();
		bukkitInventory.setItem(22, createHologram);
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
	
	
	@com.chrisimi.inventoryapi.EventMethodAnnotation
	public void onInventoryClick(ClickEvent event)
	{
		if(event.getClicked().equals(switchBetweenModes)) clickSwitchMode();
		else if(event.getClicked().equals(choosePosition)) choosePosition();
		else if(event.getClicked().equals(setLocation)) setLocation();
		else if(event.getClicked().equals(setRange)) setRange();
		else if(event.getClicked().equals(createHologram)) createHologram();
		updateInventory();
	}
	
	private void setRange()
	{
		waitingForChatInput = WaitingFor.RANGE;
		closeInventory();
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-range"));
		inventory.waitforChatInput(player);
	}
	private void setLocation()
	{
		waitingForChatInput = WaitingFor.LOCATION;
		closeInventory();
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-location"));
		inventory.waitforChatInput(player);
	}

	private void choosePosition()
	{
		inventory.waitforChatInput(player);
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-chooseposition"));
		waitingForChatInput = WaitingFor.POSITION;
		closeInventory();
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
//	
//	Chat event area
//	
	
	@EventMethodAnnotation
	public void onChat(ChatEvent event)
	{
		switch (waitingForChatInput)
		{
		case POSITION:
			if(!(choosePosition(event.getMessage())))
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-invalid_format_position"));
			else
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-position_successful"));
			
			openInventory();
			break;
		case LOCATION:
			openInventory();
			break;
		case RANGE:
			if(!(setRange(event.getMessage())))
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-invalid_format_range"));
			else
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-range_successful"));
			
		default:
			break;
		}
		
		waitingForChatInput = (waitingForChatInput != WaitingFor.NONE) ? WaitingFor.NONE : waitingForChatInput; //set waitingforchatinput back to none 
		
		
	}
	
	private boolean choosePosition(String message)
	{
		if(!(message.contains("-"))) return false;
		int a, b;
		try
		{
			a = Integer.valueOf(message.split("-")[0]);
			b = Integer.valueOf(message.split("-")[1]);
		} catch (Exception e)
		{
			return false;
		}
		
		minPosition = (a < b) ? a : b;
		maxPosition = (b > a) ? b : a;
		return true;
		
	}
	private boolean setRange(String message)
	{
		if(message.contains("all"))
		{
			useAllMode = true;
			range = 0;
			return true;
		}
		else
		{
			int rangeInput = 0;
			try
			{
				rangeInput = Integer.valueOf(message);
			} catch (Exception e)
			{
				return false;
			}
			
			if(rangeInput <= 0) return false;
			
			range = rangeInput;
			useAllMode = false;
			return true;
		}
	}
	
	private void createHologram()
	{
//	TODO	
		if(!validValues) return;
		
		
		LBHologram hologram = new LBHologram();
		hologram.mode = currentMode;
		hologram.minPosition = minPosition;
		hologram.maxPosition = maxPosition;
		hologram.useAllMode = useAllMode;
		hologram.range = range;
		hologram.hologramName = nameOfHologram;
		
	}
	
	private void editCreateHologramButton()
	{
		boolean allCorrect = true;
		List<String> lore = new ArrayList<>();
		
		lore.add("§a- using mode: " + currentMode.toString());
		
		if(minPosition != 0 && maxPosition != 0)
		{
			lore.add("§a- using positions: from §e" + minPosition + " §6to §e" + maxPosition);
		}
		else
		{
			lore.add("§4- positions not set!");
			allCorrect = false;
		}
		Location lrc = player.getLocation();
		lore.add("§a- using your current position: X: " + lrc.getBlockX() + " Y: " + lrc.getBlockY() + " Z: " + lrc.getBlockZ());
		
		if(range == 0 && !useAllMode)
		{
			lore.add("§4- range not set");
			allCorrect = false;
		}
		else
		{
			if(useAllMode)
			{
				lore.add("§a- using all your casino signs");
			}
			else
			{
				lore.add("§a- using all your casino signs in range of §e" + range + " §6blocks");
			}
		}
		
		if(allCorrect)
		{
			ItemMeta meta = createHologram.getItemMeta();
			meta.setDisplayName("§6Create hologram");
			createHologram.setItemMeta(meta);
		}
		else
		{
			ItemMeta meta = createHologram.getItemMeta();
			meta.setDisplayName("§4You can't create a hologram!");
			createHologram.setItemMeta(meta);
		}
		validValues = allCorrect;
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
