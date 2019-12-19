package slotChests;


import static org.junit.Assert.assertFalse;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chrisimi.casino.main.Main;

import net.minecraft.server.v1_14_R1.VoxelShapeDiscrete;
import scripts.CasinoManager;

public class OwnerInterfaceInventory implements Listener{
	/*
	 * Interface for the Owner, to configure his chest!
	 */
	private Player owner;
	private Main main;
	private SlotChest slotChest;
	
	private Inventory ownerInterface;
	private ItemStack warehouseItem;
	private ItemStack winningsItem;
	private ItemStack betItem;
	private ItemStack settingsItem;
	
	private ItemStack disableItem;
	private ItemStack enableItem;
	public OwnerInterfaceInventory(Player owner, Main main, SlotChest chest) {
		this.owner = owner;
		this.main = main;
		this.slotChest = chest;
		main.getServer().getPluginManager().registerEvents(this, main);
		initialize();
	}
	private void initialize() {
		slotChest.maintenance = true;
		createInventory();
		owner.openInventory(ownerInterface);
	}
	public void openInventory() {
		owner.closeInventory();
		owner.openInventory(ownerInterface);
	}
	
	
	
	//--------------------------------------------------------------
	//initialize methods
	private void createInventory() {
		ownerInterface = Bukkit.createInventory(owner, 9*1, "Owner Interface");
		
		warehouseItem = new ItemStack(Material.CHEST, 1);
		ItemMeta meta = warehouseItem.getItemMeta();
		meta.addEnchant(Enchantment.LUCK, 2, true);
		meta.setDisplayName("§bwarehouse");
		warehouseItem.setItemMeta(meta);
		ownerInterface.setItem(0, warehouseItem);
		
		winningsItem = new ItemStack(Material.DIAMOND, 1);
		meta = winningsItem.getItemMeta();
		meta.addEnchant(Enchantment.LUCK, 2, true);
		meta.setDisplayName("§awinnings");
		winningsItem.setItemMeta(meta);
		ownerInterface.setItem(1, winningsItem);
		
		disableItem = new ItemStack(Material.RED_WOOL, 1);
		meta = disableItem.getItemMeta();
		meta.addEnchant(Enchantment.LUCK, 2, true);
		meta.setDisplayName("§4disable");
		disableItem.setItemMeta(meta);
		
		settingsItem = new ItemStack(Material.IRON_NUGGET);
		meta = settingsItem.getItemMeta();
		meta.setDisplayName("§fsettings");
		settingsItem.setItemMeta(meta);
		ownerInterface.setItem(6, settingsItem);
		
		enableItem = new ItemStack(Material.GREEN_WOOL);
		meta = enableItem.getItemMeta();
		meta.addEnchant(Enchantment.LUCK, 2, true);
		meta.setDisplayName("§2enable");
		enableItem.setItemMeta(meta);
		ownerInterface.setItem(8, enableItem);
		
		betItem = new ItemStack(Material.GOLD_INGOT, 1);
		meta = betItem.getItemMeta();
		meta.addEnchant(Enchantment.LUCK, 2, true);
		meta.setDisplayName("§6bet");
		betItem.setItemMeta(meta);
		ownerInterface.setItem(4, betItem);
		
		
	}

	//--------------------------------------------------------------
	//EventHandlers
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getInventory() != ownerInterface) return;
		if(event.getCurrentItem() == null) return;
	
		if(event.getCurrentItem().equals(warehouseItem)) openWarehouseMenu();
		else if(event.getCurrentItem().equals(winningsItem)) openWinningsMenu();
		else if(event.getCurrentItem().equals(disableItem)) disableChest();
		else if(event.getCurrentItem().equals(enableItem)) enableChest();
		else if(event.getCurrentItem().equals(betItem)) openBetMenu();
		else if(event.getCurrentItem().equals(settingsItem)) openSettingsMenu();
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onInventoryLeave(InventoryCloseEvent event) {
		if(!(event.getInventory().equals(ownerInterface))) return;
		
		slotChest.maintenance = false;
	}
	
	//-------------------------------------------------------------
	//Opening other Inventories
	private void openWarehouseMenu() {
		owner.closeInventory();
		new WarehouseMenu(main, slotChest, owner);
		Bukkit.getLogger().info("warehouse");
	}
	private void openWinningsMenu() {
		owner.closeInventory();
		new WinningsMenu(main, owner, slotChest);
		Bukkit.getLogger().info("winningsmenu");
	}
	private void openBetMenu() {
		owner.closeInventory();
		new BetMenu(main, owner, slotChest, this);
		
	}
	private void openSettingsMenu() {
		owner.closeInventory();
		new SettingsMenu(main, slotChest, owner);
	}
	
	private void disableChest() {
		this.slotChest.enabled = false;
		ownerInterface.setItem(8, enableItem);
	}
	private void enableChest() {
		Boolean breakOp = false;
		for(Entry<ItemStack, Double> entry : slotChest.itemsToWin.entrySet()) {
			if(slotChest.itemIsOnForbiddenList(entry.getKey().getType())) {
				owner.sendMessage(CasinoManager.getPrefix() + "§4Can't activate SlotChest! " + entry.getKey().getType().toString() + " is forbidden on this server!");
				breakOp = true;
			}
		}
		
		if(breakOp) return;
		
		this.slotChest.enabled = true;
		ownerInterface.setItem(8, disableItem);
	}
	

}
