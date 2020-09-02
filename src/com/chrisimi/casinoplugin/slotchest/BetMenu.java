package com.chrisimi.casinoplugin.slotchest;

import java.util.HashMap;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;

import javax.swing.text.GapContent;

import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.ChatEvent;
import com.chrisimi.inventoryapi.ClickEvent;
import com.chrisimi.inventoryapi.EventMethodAnnotation;
import com.chrisimi.inventoryapi.IInventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;

public class BetMenu extends com.chrisimi.inventoryapi.Inventory implements IInventoryAPI, Listener
{

	private ItemStack inputsign = ItemAPI.createItem("Write bet in chat", Material.SIGN);
	private ItemStack backButton = ItemAPI.createItem("§bBack", Material.STONE_BUTTON);
	private ItemStack currentBetsign = ItemAPI.createItem("§6Current bet: ", Material.SIGN);
	
	private ItemStack[] plusBet = new ItemStack[4];
	private ItemStack[] minusBet = new ItemStack[4];
	
	private double[] minusBetValues = new double[4];
	private double[] plusBetValues = new double[4];
	private final double[] multiplicators =  {1.0, 2.0, 5.0, 10.0};

	private static Material minusBlockMaterial = Material.RED_WOOL;
	private static Material plusBlockMaterial = Material.GREEN_WOOL;

	private final Player player;
	private final SlotChest slotChest;
	private final OwnerInterfaceInventory ownerInterfaceInventory;
	public BetMenu(Main main, Player owner, SlotChest slotchest, OwnerInterfaceInventory oii)
	{
		super(owner, 9*3, Main.getInstance(), "BetMenu");
		this.main = main;
		this.player = owner;
		this.slotChest = slotchest;
		this.ownerInterfaceInventory = oii;

		addEvents(this);
		openInventory();

		initialize();
	}
	private void initialize()
	{
		for(int i = 0; i < plusBet.length; i++)
		{
			plusBet[i] = new ItemStack(plusBlockMaterial);
			minusBet[i] = new ItemStack(minusBlockMaterial);
		}

		bukkitInventory.setItem(22, inputsign);
		bukkitInventory.setItem(18, backButton);

		managePlusMinusBlocks();
	}
	public void updateInventory() {
		ItemAPI.changeName(currentBetsign, "§6Current bet: " + Main.econ.format(slotChest.bet));
		bukkitInventory.setItem(4, currentBetsign);

		managePlusMinusBlocks();
		
		CasinoManager.slotChestManager.save();
	}
	private void managePlusMinusBlocks() {
		double playerbalance = Main.econ.getBalance(player) - slotChest.bet;
		
		/* Slot: 1	2	3	4	5	6	7	8	9
		 * 		 m4	m3	m2	m1	sign p1	p2	p3	p4 
		 *  m4>m1 p1<p4
		 */
		for(int i = 0; i < 9; i++) {
			if(i == 4) continue;
			bukkitInventory.setItem(i, null);
		}

		//calculate the bet for every minusBet block
		if(slotChest.bet > 0)
		{
			for(int i = 0; i < 4; i++)
			{
				double bet = Math.round(slotChest.bet / multiplicators[i] * 100.0) / 100.0;
				createNewBlock(minusBet[i], bet * -1);
				bukkitInventory.setItem(i, minusBet[i]);
				minusBetValues[i] = bet;
			}
		}

		//calculate the bet for every plusBet block
		if(playerbalance >= slotChest.bet)
		{
			for(int i = 0; i < 4; i++)
			{
				double bet = Math.round(playerbalance / multiplicators[i] * 100.0) / 100.0;
				createNewBlock(plusBet[i], bet);
				bukkitInventory.setItem(i + 5, plusBet[i]);
				plusBetValues[i] = bet;
			}
		}
	}
	private void createNewBlock(ItemStack item, double bet)
	{
		ItemAPI.changeName(item, bet>=1 ? String.format("§2Increase your bet by §6%s", Main.econ.format(bet)) : String.format("§4Decrease your bet by §6%s", Main.econ.format(bet)));
	}

	@EventMethodAnnotation
	public void clickEvent(ClickEvent event)
	{
		if(event.getClicked().equals(backButton)) goBack();
		else if(event.getClicked().equals(inputsign))
		{
			waitforChatInput(player);
			closeInventory();
			player.sendMessage("\n\n"+CasinoManager.getPrefix() + MessageManager.get("slotchest-bet_message"));
		}
		else if(event.getClicked().getType() == minusBlockMaterial) clickedOnMinusBlock(event.getPos());
		else if(event.getClicked().getType() == plusBlockMaterial) clickedOnPlusBlock(event.getPos());

		updateInventory();
	}

	@EventMethodAnnotation
	public void onChat(ChatEvent event)
	{
		try
		{
			slotChest.bet = Double.parseDouble(event.getMessage());
		} catch (Exception e) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-bet_input_invalid"));
			return;
		}
		if(slotChest.bet <= 0) {
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-bet_input_lower_than_0"));
			return;
		}

		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-bet_new_bet_message").replace("%amount%", Main.econ.format(slotChest.bet)));

		slotChest.save();
		updateInventory();
		openInventory();
	}

	private void goBack()
	{
		ownerInterfaceInventory.openInventory();
	}
	
	private void clickedOnMinusBlock(int pos)
	{
		if(pos >= 4) return;

		slotChest.bet -= minusBetValues[pos];
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-bet_decrease").replace("%amount%", Main.econ.format(minusBetValues[pos])));
	}
	private void clickedOnPlusBlock(int pos)
	{
		if(pos <= 4 || pos >= 9) return;

		slotChest.bet += plusBetValues[pos - 5];
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("slotchest-bet_increase").replace("%amount%", Main.econ.format(plusBetValues[pos - 5])));
	}

}
