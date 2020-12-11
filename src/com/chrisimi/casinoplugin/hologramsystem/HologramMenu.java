package com.chrisimi.casinoplugin.hologramsystem;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.serializables.Leaderboardsign.Cycle;
import com.chrisimi.casinoplugin.serializables.Leaderboardsign.Mode;
import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chrisimi github.com/chrismi
 *
 * a inventory which tends to create a hologram or update a existing one
 */
public class HologramMenu extends Inventory implements IInventoryAPI
{
	
	private enum WaitingFor {
		NONE,
		LOCATION,
		POSITION,
		RANGE,
		NAME,
		DESCRIPTION
	}
	
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
	
	private final ItemStack switchBetweenModes = ItemAPI.createItem("§6change leaderboard mode", Material.GOLD_NUGGET);
	private final ItemStack choosePosition = ItemAPI.createItem("§6set showing positions", Material.SIGN);
	private final ItemStack setLocation = com.chrisimi.casinoplugin.scripts.Skull.getSkullByTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM4Y2YzZjhlNTRhZmMzYjNmOTFkMjBhNDlmMzI0ZGNhMTQ4NjAwN2ZlNTQ1Mzk5MDU1NTI0YzE3OTQxZjRkYyJ9fX0=");
	private final ItemStack setRange = ItemAPI.createItem("§6set range", Material.TRIPWIRE_HOOK);
	private final ItemStack setHologramName = ItemAPI.createItem("§6set hologram name", Material.SIGN);
	private final ItemStack changeServerSign = new ItemStack(Material.GOLD_BLOCK);
	private final ItemStack createHologram = new ItemStack(Material.STONE_BUTTON);
	private final ItemStack switchBetweenCycles = ItemAPI.createItem("§6change cycle", Material.CLOCK);
	private final ItemStack setDescription = ItemAPI.createItem("§6set description - information, which will be shown on the top of the hologram (optional", Material.SIGN);
	private final ItemStack chooseHighlightTop3 = new ItemStack(Material.GLOWSTONE);
	private final ItemStack deleteJackpot = ItemAPI.createItem("§0DELETE JACKPOT", Material.BEDROCK);

	/**
	 * start in a new inventory to create a new hologram
	 * @param player instance of player
	 */
	public HologramMenu(Player player)
	{
		super(player, 9*3, Main.getInstance(), "Hologram create menu");
		//this.player = player;
		//this.inventory = InventoryAPI.createInventory(player, 9 * 3, Main.getInstance(), "Hologram create menu");

		addEvents(this);
		
		
		openInventory();
		initialize();
	}

	/**
	 * update a existing hologram
	 * @param player player instance of the owner
	 * @param hologram hologram instance of the hologram to edit
	 */
	public HologramMenu(Player player, LBHologram hologram)
	{
		this(player);
		/* load data from hologram to edit it
		 * 
		 */
		loadFromHologram(hologram);
		getInventory().setItem(17, deleteJackpot);
	}
	
	private void loadFromHologram(LBHologram hologram)
	{
		//load the data from the hologram object
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
		//set position of items because they won't change
		bukkitInventory.setItem(1, choosePosition);

		ItemAPI.changeName(setLocation, "§6set location (optional), your current position will be used");
		bukkitInventory.setItem(2, setLocation);
		//setLocation.setAmount(1);
		//set location x0

		bukkitInventory.setItem(3, setRange);

		bukkitInventory.setItem(4, setHologramName);

		bukkitInventory.setItem(5, setDescription);
		
		updateInventory();
		
	}
	private void updateInventory()
	{
		//set lore for switch mode
		ItemMeta meta = switchBetweenModes.getItemMeta();
		meta.setLore(getLoreForModes());
		switchBetweenModes.setItemMeta(meta);
		bukkitInventory.setItem(0, switchBetweenModes);

		//change create hologram button
		editCreateHologramButton();
		bukkitInventory.setItem(22, createHologram);

		//change other blocks
		editServerSignBlock();
		editSwitchBetweenCycles();
		editHighlightTop3();
	}

	
	
	@com.chrisimi.inventoryapi.EventMethodAnnotation
	public void onInventoryClick(ClickEvent event)
	{
		if(event.getClicked().equals(switchBetweenModes)) clickSwitchMode();
		else if(event.getClicked().equals(switchBetweenCycles)) clickSwitchCycle();
		else if(event.getClicked().equals(choosePosition)) choosePosition();
		//else if(event.getClicked().equals(setLocation)) setLocation();
		else if(event.getClicked().getType() == Material.PLAYER_HEAD) setLocation();

		else if(event.getClicked().equals(setRange)) setRange();
		else if(event.getClicked().equals(createHologram)) createHologram();
		else if(event.getClicked().equals(setHologramName)) setHologramName();
		else if(event.getClicked().equals(changeServerSign)) changeServerSign();
		else if(event.getClicked().equals(setDescription)) setDescription();
		else if(event.getClicked().equals(chooseHighlightTop3)) chooseHighlightTop3();
		else if(event.getClicked().equals(deleteJackpot)) deleteJackpot();

		updateInventory();
	}

	private void deleteJackpot()
	{
		if(HologramSystem.deleteHologram(oldLocation))
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-deletion"));
			closeInventory();
		}
		else
		{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-error-message").replace("{error}", "can't delete hologram"));
		}
	}

	private void chooseHighlightTop3()
	{
		highlightTop3 = !highlightTop3;
	}
	private void setDescription()
	{
		waitingForChatInput = WaitingFor.DESCRIPTION;
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-description"));
		closeInventory();
		waitforChatInput(player);
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
		waitforChatInput(player);
	}
	
	private void setRange()
	{
		waitingForChatInput = WaitingFor.RANGE;
		closeInventory();
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-range"));
		waitforChatInput(player);
	}
	private void setLocation()
	{
		waitingForChatInput = WaitingFor.LOCATION;
		closeInventory();
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("hologrammenu-location"));
		waitforChatInput(player);
	}

	private void choosePosition()
	{
		waitforChatInput(player);
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
			
			if(!hologramNameExists(event.getMessage()))
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
		
		waitingForChatInput = WaitingFor.NONE; //set waiting for chat input back to none
		updateInventory();
		
	}
	
	private boolean hologramNameExists(String name)
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
			a = Integer.parseInt(message.split("-")[0]);
			b = Integer.parseInt(message.split("-")[1]);
		} catch (Exception e)
		{
			return false;
		}
		
		minPosition = Math.min(a, b);
		maxPosition = Math.max(a, b);
		return true;
		
	}
	private boolean setRange(String message)
	{
		if(message.contains("all"))
		{
			useAllMode = true;
			range = 0;
		}
		else
		{
			int rangeInput;
			try
			{
				rangeInput = Integer.parseInt(message);
			} catch (Exception e)
			{
				return false;
			}
			
			if(rangeInput <= 0) return false;
			
			range = rangeInput;
			useAllMode = false;
		}
		return true;
	}
	
	private void createHologram()
	{

		//check if all values are valid

		//afterwards put all information from local variables in a new hologram instance or update in the existing one
		//send the instance to the hologram system for proceeding
		if(!validValues) return;

		LBHologram hologram;

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
		Location lrc = this.location;
		if(lrc != null)
			lore.add("§a- using position: X: " + lrc.getBlockX() + " Y: " + lrc.getBlockY() + " Z: " + lrc.getBlockZ());
		else
		{
			lore.add("§4- location not set");
			allCorrect = false;
		}
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
		if(description.equals(""))
		{
			lore.add("§a- you haven't setup a description for the hologram");
		}
		else
		{
			lore.add("§a- description: ");
			String[] lines = description.replaceAll("&", "§").split("\n");
			for (String line : lines) lore.add("  " + line);
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
		//set the value
		validValues = allCorrect;
	}
	
	private void editServerSignBlock()
	{

		ItemMeta meta = changeServerSign.getItemMeta();
		if(isServerSign)
		{
			meta.setDisplayName("§4Change it to a player sign!");
		}
		else
		{
			meta.setDisplayName("§6Change it to a serversign!");
		}
		changeServerSign.setItemMeta(meta);
		if(Main.perm.has(player, "casino.create.serverhologram"))
		{
			//check when the hologram is getting edited or  if the player is admin
			// because a player shouldn't be able to make a server sign back to a player sign
			if(loadedHologram != null && Main.perm.has(player, "casino.admin"))
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
