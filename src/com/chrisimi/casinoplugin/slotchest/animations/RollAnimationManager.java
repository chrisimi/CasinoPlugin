package com.chrisimi.casinoplugin.slotchest.animations;

import java.util.Random;

import javax.management.modelmbean.ModelMBeanAttributeInfo;

import com.chrisimi.casinoplugin.utils.ItemAPI;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.experimental.theories.Theories;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.slotchest.SlotChest;

public class RollAnimationManager implements Runnable, Listener
{

	public static int rollsGlobal = 0;
	
	private final OfflinePlayer owner;
	private final Player player;
	private final SlotChest slotChest;
	
	private Inventory inventory;
	private ItemStack beginInformationSign; //shows information about the bet etc.
	private ItemStack beginButton;
	private ItemStack fillMaterial;
	
	int rollThreadID = 0; //set to 0 to simulate no rolling
	private int rolls = 0;

	/**
	 * instance of the roll animation which will be played
	 */
	private IRollAnimation rollAnimation;
	
	public RollAnimationManager(Player player, SlotChest slotChest, Main main)
	{
		this.owner = slotChest.getOwner();
		this.player = player;
		this.slotChest = slotChest;
		
		rollAnimation = RollAnimationFactory.GetRollAnimation(Main.getInstance(), slotChest, player);
		inventory = rollAnimation.getInventory();
		Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
	}
	@Override
	public void run()
	{
		initialize();
		player.openInventory(inventory);
	}
	private void initialize()
	{
		fillMaterial = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
		
		beginInformationSign = ItemAPI.createItem("§6bet: " + Main.econ.format(slotChest.bet), Material.SIGN);
		inventory.setItem(4, beginInformationSign);
		
		beginButton = ItemAPI.createItem("§6§lROLL", Material.STONE_BUTTON);
		inventory.setItem(13, beginButton);
	}

	private void startBeginAnimation()
	{
		//test for various cases
		if(slotChest.running) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-is_currently_running"));
			return;
		}

		if(!slotChest.hasChestEnough() && !slotChest.isServerOwner()) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-not_enough_items"));
			return;
		}

		if(slotChest.itemsToWin.size() < 2) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-not_enough_winnings"));
			return;
		}

		if(!(Main.econ.has(player, slotChest.bet))) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-player_not_enough_money"));
			return;
		}

		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-player_pay_message").replace("%amount%", Main.econ.format(slotChest.bet)));
		
		Main.econ.withdrawPlayer(player, slotChest.bet);
		slotChest.giveOwnerMoney(slotChest.bet);

		if(!slotChest.isServerOwner() && owner.isOnline())
			owner.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-owner_pay_message").replace("%amount%", Main.econ.format(slotChest.bet)));

		rollAnimation.initialize();
		startRollingAnimation();
	}

	/**
	 * start the roll animation
	 */
	private void startRollingAnimation()
	{
		slotChest.running = true;
		Random random = new Random();
		rolls = random.nextInt(30) + 20;

		Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(), new Runnable()
		{
			int rollsToSkip = 0;
			int rollSkipMaximum = 0;
			
			@Override
			public void run()
			{
				rollThreadID = Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable()
				{
					@Override
					public void run()
					{
						Boolean decreseRolls = rollAnimation.nextAnimation(rolls);
						if(decreseRolls) rolls--;
						if(rolls <= 0)
							finish();
					}
				}, 5L, 4L);
			}
		});
		rolls++;
	}

	private void simulateEnding()
	{
		rollAnimation.simulateEnding(rolls);
		slotChest.running = false;
		finish();
	}
	
	private void finish()
	{
		Main.getInstance().getServer().getScheduler().cancelTask(rollThreadID);
		rollThreadID = 0;

		ItemStack wonItem = rollAnimation.finish();
		slotChest.running = false;

		//only remove items from the warehouse when it's a player managed slot chest
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-player_won").replace("%item_count%", String.valueOf(wonItem.getAmount())).replace("%item_type%", wonItem.getType().toString()));
		if(!slotChest.isServerOwner())
			slotChest.RemoveItemsFromWarehouse(wonItem);
		
		ItemAPI.putItemInInventory(wonItem, player);

		player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation(), 5);
		rollsGlobal++;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if(event.getCurrentItem() == null) return;
		if(!event.getInventory().equals(inventory)) return;
		
		if(event.getCurrentItem().equals(beginButton)) startBeginAnimation();
		event.setCancelled(true);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
	{
		if(!event.getInventory().equals(inventory)) return;
		
		if(rollThreadID != 0)
		{
			Main.getInstance().getServer().getScheduler().cancelTask(rollThreadID);
			rollThreadID = 0;
			simulateEnding();
		}
	}
}
