package com.chrisimi.casinoplugin.animations.signanimation;

import org.bukkit.block.Sign;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
/**
 * represents one blackjack sign animation cycle
 * @author chris
 *
 */
public class Blackjack implements Runnable
{
	private final Sign sign;
	private final PlayerSignsConfiguration thisSign;
	
	public Blackjack(Sign sign, PlayerSignsConfiguration thisSign) 
	{
		this.sign = sign;
		this.thisSign = thisSign;
	}
	@Override
	public void run() {
		sign.setLine(0, "§fBlackjack");
		sign.setLine(1, thisSign.getOwnerName());
		
		if(thisSign.isSignDisabled()) {
			sign.setLine(2, "§4DISABLED!");
			sign.setLine(3, "§4DISABLED!");
		} else {
			if(thisSign.hasOwnerEnoughMoney(thisSign.blackjackGetMaxBet()*thisSign.blackjackMultiplicator())) {
				if(thisSign.currentSignAnimation == 0) {
					sign.setLine(2, "§l§6min: " + Main.econ.format(thisSign.blackjackGetMinBet()));
					
					if(thisSign.plusinformations.contains("to"))
						sign.setLine(3, "§a" + thisSign.plusinformations.split(";")[1]);
					else
						sign.setLine(3, "§awin: " + Main.econ.format(thisSign.blackjackMultiplicator()*thisSign.bet));
					
				} else {
					//print infinite symbol when max bet is infinite
					if(thisSign.isServerOwner() && thisSign.unlimitedBet())
						sign.setLine(2, "§l§6max: §f§linfinite");
					else
						sign.setLine(2, "§l§6max: " + Main.econ.format(thisSign.blackjackGetMaxBet()));
					
					if(thisSign.plusinformations.contains("to"))
						sign.setLine(3, "§a" + thisSign.plusinformations.split(";")[1]);
					else
						sign.setLine(3, "§awin: " + Main.econ.format(thisSign.blackjackMultiplicator()*thisSign.blackjackGetMaxBet()));
				}
			} else {
				if(thisSign.currentSignAnimation == 1) {
					sign.setLine(2, "§4ERROR!");
					sign.setLine(3, "§4ERROR!");
				} else {
					sign.setLine(2, "§4doesn't have");
					sign.setLine(3, "§4enough money!");
				}
			}
				
		}
		sign.update(true);
		
		thisSign.currentSignAnimation = (thisSign.currentSignAnimation == 0) ? 1 : 0;
	}
}
