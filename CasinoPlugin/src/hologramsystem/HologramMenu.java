package hologramsystem;

import java.util.ArrayList;
import java.util.Iterator;
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
import serializeableClass.Leaderboardsign.Cycle;
import serializeableClass.Leaderboardsign.Mode;


public class HologramMenu implements IInventoryAPI
{
	/*
	 * todo
	 *  done function to set hologram name
	 *  done when player has serversigns perms a block should be in the inventory to set if it's a server sign
	 * 
	 * add a button to switch between cycles
	 * 
	 */
	
	private enum WaitingFor {
		NONE,
		LOCATION,
		POSITION,
		RANGE,
		NAME,
		DESCRIPTION
	}
	
	
	private final Player player;
	private final Inventory inventory;
	private final org.bukkit.inventory.Inventory bukkitInventory;
	
	private Mode currentMode = Mode.HIGHESTAMOUNT;
	private Cycle currentCycle = Cycle.NaN;
	private int minPosition = 0;
	private int maxPosition = 0;
	private String nameOfHologram = "";
	private boolean useAllMode = false;
	private int range = 0;
	private boolean isServerSign = false;
	private boolean highlightTop3 = false;
	private Location location = null;
	private String description = "";
	
	private LBHologram loadedHologram = null;
	private Location oldLocation = null;
	
	private boolean validValues = false;
	
	private WaitingFor waitingForChatInput = WaitingFor.NONE;
	
	private ItemStack switchBetweenModes = new ItemStack(Material.GOLD_NUGGET);
	private ItemStack choosePosition = new ItemStack(Material.SIGN);
	private ItemStack setLocation = scripts.Skull.getSkullByTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM4Y2YzZjhlNTRhZmMzYjNmOTFkMjBhNDlmMzI0ZGNhMTQ4NjAwN2ZlNTQ1Mzk5MDU1NTI0YzE3OTQxZjRkYyJ9fX0=");
	private ItemStack setRange = new ItemStack(Material.TRIPWIRE_HOOK);
	private ItemStack setHologramName = new ItemStack(Material.SIGN);
	private ItemStack changeServerSign = new ItemStack(Material.GOLD_BLOCK);
	private ItemStack createHologram = new ItemStack(Material.STONE_BUTTON);
	private ItemStack switchBetweenCycles = new ItemStack(Material.CLOCK);
	private ItemStack setDescription = new ItemStack(Material.SIGN);
	private ItemStack chooseHighlightTop3 = new ItemStack(Material.GLOWSTONE);
	
	public HologramMenu(Player player)
	{
		this.player = player;
		this.inventory = InventoryAPI.createInventory(player, 9 * 3, Main.getInstance(), "Hologram create menu");
		this.bukkitInventory = inventory.getInventory();
		inventory.addEvents(this);
		
		
		openInventory();
		initialize();
	}
	public HologramMenu(Player player, LBHologram hologram)
	{
		this(player);
		/* load data from holorgam to edit it
		 * 
		 */
		loadFromHologram(hologram);
	}
	
	private void loadFromHologram(LBHologram hologram)
	{
		loadedHologram = hologram;
		oldLocation = hologram.getLocation().clone();
		this.currentMode = hologram.mode;
		this.currentCycle = hologram.cycleMode;
		this.minPosition = hologram.minPosition;
		this.maxPosition = hologram.maxPosition;
		this.nameOfHologram = hologram.hologramName;
		this.useAllMode = hologram.useAllMode;
		this.range = hologram.range;
		this.isServerSign = hologram.isServerHologram();
		this.highlightTop3 = hologram.highlightTop3;
		this.description = hologram.description;
		this.location = hologram.getLocation();
		updateInventory();
	}
	
	
	private void initialize()
	{
		ItemMeta meta = switchBetweenModes.getItemMeta();
		meta.setDisplayName("§6change leaderboard mode");
		switchBetweenModes.setItemMeta(meta);
		
		meta = switchBetweenCycles.getItemMeta();
		meta.setDisplayName("§6change cycle");
		switchBetweenCycles.setItemMeta(meta);
		
		meta = choosePosition.getItemMeta();
		meta.setDisplayName("§6set showing positions");
		choosePosition.setItemMeta(meta);
		bukkitInventory.setItem(1, choosePosition);
		
		meta = setLocation.getItemMeta();
		meta.setDisplayName("§6set location (optional) your current position will be used");
		setLocation.setItemMeta(meta);
		bukkitInventory.setItem(2, setLocation);
		
		meta = setRange.getItemMeta();
		meta.setDisplayName("§6set range");
		setRange.setItemMeta(meta);
		bukkitInventory.setItem(3, setRange);
		
		meta = setHologramName.getItemMeta();
		meta.setDisplayName("§6set hologram name");
		setHologramName.setItemMeta(meta);
		bukkitInventory.setItem(4, setHologramName);
		
		meta = setDescription.getItemMeta();
		meta.setDisplayName("§6set description - information, which will be shown on the top of the hologram (optional)");
		setDescription.setItemMeta(meta);
		bukkitInventory.setItem(5, setDescription);
		
		updateInventory();
		
	}
	private void updateInventory()
	{
		ItemMeta meta = switchBetweenModes.getItemMeta();
		meta.setLore(getLoreForModes());
		switchBetweenModes.setItemMeta(meta);
		bukkitInventory.setItem(0, switchBetweenModes);
		
		editCreateHologramButton();
		bukkitInventory.setItem(22, createHologram);
		
		editServerSignBlock();
		
		editSwitchBetweenCycles();
		
		editHighlightTop3();
	}
	

	
	
	@Override
	public void openInventory()
	{
		inventory.openInventory();
		player.openInventory(bukkitInventory);
	}

	@Override
	public void closeInventory()
	{
		inventory.closeInventory();
		player.closeInventory();
	}
	
	
	@com.chrisimi.inventoryapi.EventMethodAnnotation
	public void onInventoryClick(ClickEvent event)
	{
		if(event.getClicked().equals(switchBetweenModes)) clickSwitchMode();
		else if(event.getClicked().equals(switchBetweenCycles)) clickSwitchCycle();
		else if(event.getClicked().equals(choosePosition)) choosePosition();
		else if(event.getClicked().equals(setLocation)) setLocation();
		else if(event.getClicked().equals(setRange)) setRange();
		else if(event.getClicked().equals(createHologram)) createHologram();
		else if(event.getClicked().equals(setHologramName)) setHologramName();
		else if(event.getClicked().equals(changeServerSign)) changeServerSign();
		else if(event.getClicked().equals(setDescription)) setDescription();
		else if(event.getClicked().equals(chooseHighlightTop3)) chooseHighlightTop3();
		
		updateInventory();
	}
	
	private void chooseHighlightTop3()
	{
		highlightTop3 = !highlightTop3;
	}
	private void setDescription()
	{
		//TODO
		waitingForChatInput = WaitingFor.DESCRIPTION;
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-description"));
		closeInventory();
		inventory.waitforChatInput(player);
	}
	private void changeServerSign()
	{
		isServerSign = !isServerSign;
	}

	private void setHologramName()
	{
		waitingForChatInput = WaitingFor.NAME;
		closeInventory();
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-name"));
		inventory.waitforChatInput(player);
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
	private void clickSwitchCycle()
	{
		currentCycle = (currentCycle.ordinal() < Cycle.values().length - 1) ? Cycle.values()[currentCycle.ordinal() + 1] : Cycle.values()[0];
		updateInventory();
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
			location = event.getPlayer().getLocation();
			openInventory();
			break;
		case RANGE:
			if(!(setRange(event.getMessage())))
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-invalid_format_range"));
			else
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-range_successful"));
			openInventory();
			break;
		case NAME:
			
			if(!hologramnameExists(event.getMessage()))
			{
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammneu-name_successful"));
				this.nameOfHologram = event.getMessage();
			}
			else
				event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-name_exists"));
			openInventory();
			break;
		case DESCRIPTION:
			event.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-description_successful"));
			this.description = event.getMessage();
			openInventory();
			
			
		default:
			break;
		}
		
		waitingForChatInput = (waitingForChatInput != WaitingFor.NONE) ? WaitingFor.NONE : waitingForChatInput; //set waitingforchatinput back to none 
		updateInventory();
		
	}
	
	private boolean hologramnameExists(String name)
	{
		for(LBHologram holo : HologramSystem.getHolograms())
		{
			if(holo.hologramName.equals(name)) return true;
		}
		
		return false;
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
		
		LBHologram hologram = null;
		
		if(loadedHologram == null)
			hologram = new LBHologram();
		else
			hologram = loadedHologram;
		
		hologram.mode = currentMode;
		hologram.cycleMode = currentCycle;
		hologram.minPosition = minPosition;
		hologram.maxPosition = maxPosition;
		hologram.useAllMode = useAllMode;
		hologram.range = range;
		hologram.hologramName = nameOfHologram;
		hologram.setLocation(this.location);
		hologram.highlightTop3 = highlightTop3;
		hologram.description = description;
		hologram.ownerUUID = (isServerSign) ? "server" : player.getUniqueId().toString();
		
		if(loadedHologram == null)
		{			
			HologramSystem.addHologram(hologram);
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-creation"));	
		}
		else
		{
			
			HologramSystem.updateHologram(hologram, oldLocation);
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-update"));
		}
		closeInventory();
		
		
	}
	
	private void editCreateHologramButton()
	{
		boolean allCorrect = true;
		List<String> lore = new ArrayList<>();
		
		if(nameOfHologram.equals(""))
		{
			lore.add("§4- no name set");
			allCorrect = false;
		}
		else
		{
			lore.add("§a- name: " + nameOfHologram);
		}
		
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
		
		if(isServerSign)
		{
			lore.add("§a- is a ServerSign");
		}
		else
		{
			lore.add("§a- is a player sign (yours)");
		}
		
		if(highlightTop3)
		{
			lore.add("§a- top 3 will be highlighted with a diamond, gold and iron block.");
		}
		else
		{
			lore.add("§a- top 3 won't be highlighed with a diamond, gold and iron block.");
		}
		if(description == "")
		{
			lore.add("§a- you haven't setup a description for the hologram");
		}
		else
		{
			lore.add("§a- description: ");
			String[] lines = description.replaceAll("&", "§").split("\n");
			for(int i = 0; i < lines.length; i++)
				lore.add("  " + lines[i]);
		}
		
		
		
		if(allCorrect)
		{
			ItemMeta meta = createHologram.getItemMeta();
			meta.setDisplayName("§6Create hologram");
			meta.setLore(lore);
			createHologram.setItemMeta(meta);
		}
		else
		{
			ItemMeta meta = createHologram.getItemMeta();
			meta.setDisplayName("§4You can't create a hologram!");
			meta.setLore(lore);
			createHologram.setItemMeta(meta);
		}
		validValues = allCorrect;
	}
	
	private void editServerSignBlock()
	{

		if(isServerSign)
		{
			ItemMeta meta = changeServerSign.getItemMeta();
			meta.setDisplayName("§4Change it to a player sign!");
			changeServerSign.setItemMeta(meta);
		}
		else
		{
			ItemMeta meta = changeServerSign.getItemMeta();
			meta.setDisplayName("§6Change it to a serversign!");
			changeServerSign.setItemMeta(meta);
		}
		if(Main.perm.has(player, "casino.admin") || Main.perm.has(player, "casino.serversigns"))
		{
			bukkitInventory.setItem(9, changeServerSign);
		}
		else
			bukkitInventory.setItem(9, null);
	}
	
	private void editSwitchBetweenCycles()
	{
		Cycle[] cycles = Cycle.values();
		
		List<String> lores = new ArrayList<>();
		for(Cycle cycle : cycles)
		{
			String lore = "";
			if(cycle == currentCycle)
				lore += "§6§l";
			else
				lore += "§6";
			
			if(cycle == Cycle.NaN)
				lore += "no cycle";
			else
				lore += cycle.toString();
			lores.add(lore);
		}
		
		ItemMeta meta = switchBetweenCycles.getItemMeta();
		meta.setLore(lores);
		switchBetweenCycles.setItemMeta(meta);
		bukkitInventory.setItem(7, switchBetweenCycles);
	}
	
	private void editHighlightTop3()
	{
		ItemMeta meta = chooseHighlightTop3.getItemMeta();
		meta.setDisplayName((highlightTop3) ? "§4disable highlighting top 3" : "§aenable highlighting top3");
		chooseHighlightTop3.setItemMeta(meta);
		bukkitInventory.setItem(6, chooseHighlightTop3);
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
