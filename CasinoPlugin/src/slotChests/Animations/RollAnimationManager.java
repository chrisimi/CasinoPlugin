package slotChests.Animations;

import java.util.Random;

import javax.management.modelmbean.ModelMBeanAttributeInfo;

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

import com.chrisimi.casino.main.Main;
import com.chrisimi.casino.main.MessageManager;

import net.minecraft.server.v1_14_R1.SoundCategory;
import net.minecraft.server.v1_14_R1.VoxelShapeDiscrete;
import net.minecraft.server.v1_14_R1.EntityFox.i;
import scripts.CasinoManager;
import slotChests.SlotChest;

public class RollAnimationManager implements Runnable, Listener{

	public static int rollsGlobal = 0;
	
	private final OfflinePlayer owner;
	private final Player player;
	private final SlotChest slotChest;
	private final Main main;
	
	private Inventory inventory;
	private ItemStack beginInformationSign; //shows information about the bet etc.
	private ItemStack beginButton;
	private ItemStack fillMaterial;
	
	int rollThreadID = 0; //set to 0 to simulate no rolling
	private ItemStack[] currentItems = new ItemStack[5];
	private int rolls = 0;
	
	private IRollAnimation rollAnimation;
	
	public RollAnimationManager(Player player, SlotChest slotChest, Main main) {
		this.owner = slotChest.getOwner();
		this.player = player;
		this.slotChest = slotChest;
		this.main = main;
		
		rollAnimation = RollAnimationFactory.GetRollAnimation(main, slotChest, player);
		inventory = rollAnimation.getInventory();
		main.getServer().getPluginManager().registerEvents(this, main);
	}
	@Override
	public void run() {
		initialize();
		player.openInventory(inventory);
	}
	private void initialize() {
		fillMaterial = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
		
		beginInformationSign = new ItemStack(Material.OAK_SIGN);
		ItemMeta meta = beginInformationSign.getItemMeta();
		meta.setDisplayName("§6bet: " + Main.econ.format(slotChest.bet));
		beginInformationSign.setItemMeta(meta);
		inventory.setItem(4, beginInformationSign);
		
		beginButton = new ItemStack(Material.STONE_BUTTON);
		meta = beginButton.getItemMeta();
		meta.setDisplayName("§6§lROLL");
		beginButton.setItemMeta(meta);
		inventory.setItem(13, beginButton);
	}
	private void startBeginAnimation() { //TODO: make it more beautiful
		if(slotChest.running) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-is_currently_running"));
			return;
		}
		
		if(!slotChest.hasChestEnough()) {
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
		
		/*
		for(int i = 0; i < 9*3; i++)
			inventory.setItem(i, fillMaterial);
		for(int i = 11; i < 16; i++)
			inventory.setItem(i, new ItemStack(Material.AIR));
			
			*/
		
		//player.sendMessage(CasinoManager.getPrefix() + "§6You paid " + Main.econ.format(slotChest.bet));
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-player_pay_message").replace("%amount%", Main.econ.format(slotChest.bet)));
		
		Main.econ.withdrawPlayer(player, slotChest.bet);
		Main.econ.depositPlayer(owner, slotChest.bet);
		
		if(owner.isOnline()) {
			//owner.getPlayer().sendMessage(CasinoManager.getPrefix() + "§6Somebody played on your slotchest, you earned: " + Main.econ.format(slotChest.bet));
			owner.getPlayer().sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-owner_pay_message").replace("%amount%", Main.econ.format(slotChest.bet)));
		}
		
		
		rollAnimation.initialize();
		startRollingAnimation();
	}
	private void startRollingAnimation() {
		
		slotChest.running = true;
		Random random = new Random();
		rolls = random.nextInt(30) + 20;
		
		Main.econ.withdrawPlayer(player, slotChest.bet);
		Main.econ.depositPlayer(owner, slotChest.bet);
		
		main.getServer().getScheduler().runTask(main, new Runnable() {
			int rollsToSkip = 0;
			int rollSkipMaximum = 0;
			
			@Override
			public void run() {
				
				rollThreadID = main.getServer().getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
				
					
					
					@Override
					public void run() {
						
						/*
						rollsToSkip++;
						if(rollsToSkip >= rollSkipMaximum && rollSkipMaximum != 0) {
							rollsToSkip = 0;
							
							player.sendMessage("skip");
							
						} else {
							rolls--;
							player.sendMessage("normal");
							//get items
							System.out.println("a");
							for(int i = 11; i < 16; i++)
								currentItems[i-11] = inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR ? new ItemStack(Material.AIR) : inventory.getItem(i);
								System.out.println("b");
							for(int i = 0; i < 4; i++) //move items one further
								currentItems[i] = currentItems[i+1];
							System.out.println("c");
							currentItems[4] = slotChest.getRandomItem();
							System.out.println("d");
							for(int i = 0; i < 5; i++)
								inventory.setItem(i+11, currentItems[i]);
							System.out.println("e");
							player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, org.bukkit.SoundCategory.AMBIENT, 4, 3);
							
							if(rolls <= 10) {
								rollSkipMaximum = 2;
								player.sendMessage("10");
							}
							else if(rolls <= 15) {
								rollSkipMaximum = 3;
								player.sendMessage("15");
							}
								
						}
						*/
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
	private void simulateEnding() {
		/*for(int rollsLeft = rolls; rollsLeft > 0; rollsLeft--) {
			
			for(int i = 0; i < 4; i++)
				currentItems[i] = currentItems[i+1];
			currentItems[4] = slotChest.getRandomItem();
			
		}
		finish();
		*/
		rollAnimation.simulateEnding(rolls);
		slotChest.running = false;
		finish();
	}
	
	private void finish() {
		main.getServer().getScheduler().cancelTask(rollThreadID);
		rollThreadID = 0;
		/*
		player.sendMessage(CasinoManager.getPrefix() + "You won: " + currentItems[2].getAmount() + "x " + currentItems[2].getType().toString());
		*/
		ItemStack wonItem = rollAnimation.finish();
		slotChest.running = false;
		
		//player.sendMessage(CasinoManager.getPrefix() + "You won: " + wonItem.getAmount()+"x " + wonItem.getType().toString());
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-player_won").replace("%item_count%", String.valueOf(wonItem.getAmount())).replace("%item_type%", wonItem.getType().toString()));
		slotChest.RemoveItemsFromWarehouse(wonItem);
		
		int slot = player.getInventory().first(Material.AIR);
		if(slot == -1) {
			player.getLocation().getWorld().dropItem(player.getLocation(), wonItem);
		} else {
			player.getInventory().setItem(slot, wonItem);
		}
		
		
		player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation(), 5);
		rollsGlobal++;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getCurrentItem() == null) return;
		if(!event.getInventory().equals(inventory)) return;
		
		if(event.getCurrentItem().equals(beginButton)) startBeginAnimation();
		event.setCancelled(true);
	}
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(!event.getInventory().equals(inventory)) return;
		
		if(rollThreadID != 0) {
			main.getServer().getScheduler().cancelTask(rollThreadID);
			rollThreadID = 0;
			simulateEnding();
		}
		
	}
}
