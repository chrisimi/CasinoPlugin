package animations.signanimation;

import org.bukkit.block.Sign;

import com.chrisimi.casino.main.Main;

import serializeableClass.PlayerSignsConfiguration;

public class Slots implements Runnable
{

	private final Sign sign;
	private final PlayerSignsConfiguration thisSign;
	
	public Slots(Sign sign, PlayerSignsConfiguration thisSign)
	{
		this.sign = sign;
		this.thisSign = thisSign;
	}
	
	@Override
	public void run()
	{
		
		
		sign.setLine(0, "§fSlots");
		sign.setLine(1, thisSign.getOwnerName());
		
		if(thisSign.isSignDisabled())
		{
			sign.setLine(2, "§4DISABLED!");
			sign.setLine(3, "§4DISABLED!");
		}
		else
		{
			if(thisSign.hasOwnerEnoughMoney(thisSign.getSlotsHighestPayout()))
			{
				//owner has enough money
				sign.setLine(2, "§6" + Main.econ.format(thisSign.bet));
				sign.setLine(3, "3x " + thisSign.getColorMultiplicators()[thisSign.currentSignAnimation] + thisSign.getSlotsSymbols()[thisSign.currentSignAnimation] + " : " + Main.econ.format(thisSign.bet * thisSign.getSlotsMultiplicators()[thisSign.currentSignAnimation]));
			} 
			else
			{
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
		
		thisSign.currentSignAnimation = (thisSign.currentSignAnimation == 2) ? 0 : ++thisSign.currentSignAnimation; // add 1 and set it to 0 if it's 2.     range: 0-2
		System.out.println(thisSign.currentSignAnimation);
	}

}
