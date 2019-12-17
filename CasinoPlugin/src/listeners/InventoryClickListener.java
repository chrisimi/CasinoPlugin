package listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.chrisimi.casino.main.Main;

import scripts.CasinoAnimation;
import scripts.CasinoGUI;
import scripts.CasinoManager;
import scripts.UpdateManager;

public class InventoryClickListener implements Listener{

	private Main main;
	
	private Material plusBlock;
	private Material minusBlock;
	public InventoryClickListener(Main main) {
		this.main = main;
		
		plusBlock = Enum.valueOf(Material.class, (String) UpdateManager.getValue("bet.plusBlock"));
		minusBlock = Enum.valueOf(Material.class, (String) UpdateManager.getValue("bet.minusBlock"));
		
		
		
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getCurrentItem() == null) return;
		Player player = (Player)event.getWhoClicked();
		if(event.getInventory().equals(CasinoGUI.getInv())) {
	
			
		
			if(event.getCurrentItem().getType() == minusBlock) {
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 25, 2);
				String name = event.getCurrentItem().getItemMeta().getDisplayName();
				String[] zahlen = name.split("- ");
				String zahl = zahlen[zahlen.length-1];
				int zahlint = Integer.parseInt(zahl);
				//eror
				CasinoGUI.subEinsatz(zahlint, player);
				
			} else if(event.getCurrentItem().getType() == plusBlock) {
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 25, 2);
				String name = event.getCurrentItem().getItemMeta().getDisplayName();
				String[] zahlen = name.split("\\+ ");
				int zahl = Integer.parseInt(zahlen[zahlen.length-1]);
				CasinoGUI.addEinsatz(zahl, (Player)event.getWhoClicked());
			
			} else if(event.getCurrentItem().getType() == Material.STONE_BUTTON) {
				CasinoGUI.startAnimation(player);
			}
		
		} else if(event.getInventory().equals(CasinoAnimation.getInventory(player))) {
			if(event.getCurrentItem().getType() == Material.STONE_BUTTON) {
				if(event.getSlot() == 36) {
					CasinoAnimation.playerExit(player);
					new CasinoGUI(main, player);
					
					
					
					
				} else {
					CasinoAnimation a = CasinoAnimation.getAnimationClass((Player) event.getWhoClicked());
					if(!(a == null)) {
						a.startRoll();
					}
				}
			} else if(event.getCurrentItem().getType() == Material.OAK_BUTTON) {
				CasinoAnimation a = CasinoAnimation.getAnimationClass((Player) event.getWhoClicked());
				if(!(a == null)) {
					if(Main.econ.getBalance((OfflinePlayer) event.getWhoClicked()) >= a.einsatz) {
						a.startRoll();
					} else {
						event.getWhoClicked().sendMessage(CasinoManager.getPrefix()+ " You don't have enough money");
						CasinoAnimation.playerExit((Player) event.getWhoClicked());
						event.getWhoClicked().closeInventory();
					}
					
				}
			}
		} else {
			return;
		}
		
		
		event.setCancelled(true);
	}
	@EventHandler
	public void onInventoryLeave(InventoryCloseEvent event) {
		
		
		if(event.getInventory().equals(CasinoGUI.getInv())) {
			CasinoGUI.removeInventory((Player) event.getPlayer()); 
		} else if(event.getInventory().equals(CasinoAnimation.getInventory((Player) event.getPlayer()))) {
			CasinoAnimation.playerExit((Player) event.getPlayer());
		} else {
			return;
		}
	}
}
