package animations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.chrisimi.casino.main.Main;
import com.chrisimi.casino.main.MessageManager;
import com.mojang.datafixers.functions.PointFreeRule.CompAssocLeft;

import scripts.CasinoManager;
import scripts.LeaderboardsignsManager;
import scripts.OfflineEarnManager;
import scripts.PlayerSignsManager;
import scripts.UpdateManager;
import serializeableClass.Card;
import serializeableClass.PlayerSignsConfiguration;


public class BlackjackAnimation implements Runnable {

	//min bet is value to come in game
	//max bet is value where you can higher the bet
	enum Decision {
		SKIP,
		DRAW,
		HALF,
		HIGHER
		
	}
	
	private static HashMap<Player, BlackjackAnimation> waitingForInputs = new HashMap<Player, BlackjackAnimation>();
	
	private final Main main;
	private final PlayerSignsConfiguration thisSign;
	private final Player player;
	private final OfflinePlayer owner;
	private final PlayerSignsManager manager;
	private double minBet = 0.0;
	private double maxBet = 0.0;
	private double playerBet = 0.0;
	private Sign sign;
	private List<Card> cards;
	private List<Card> dealer;
	
	private int currentWaitingTask = 0; //Id of the task which is waiting for chat input
	private int resetTask = 0;
	
	public Boolean waitingForBet = true;
	public Boolean waitingForGameDecision = false; //if he wants to draw a card or to skip!

	private Boolean finished = false;
	private Boolean playerWantToSkip = false;
	
	
	public BlackjackAnimation(Main main, PlayerSignsConfiguration thisSign, Player player, PlayerSignsManager manager) {
		this.main = main;
		this.thisSign = thisSign;
		this.player = player;
		this.owner = thisSign.getOwner();
		this.manager = manager;
		this.cards = new ArrayList<Card>();
		this.dealer = new ArrayList<Card>();
		this.sign = thisSign.getSign();
		
	}
	
	@Override
	public void run() {
		prepareForGettingBetFromPlayer();
		
	}

	private void prepareForGettingBetFromPlayer() {
		
//		if(thisSign.unlimitedBet() && !thisSign.isServerOwner())
//			maxBet = Main.econ.getBalance(owner) / thisSign.blackjackMultiplicator();
//		else if(thisSign.unlimitedBet() && thisSign.isServerOwner())
//			maxBet = (Double.valueOf(UpdateManager.getValue("blackjack-max-bet").toString()) == -1) ? Double.MAX_VALUE : Double.valueOf(UpdateManager.getValue("blackjack-max-bet").toString());
//		
		
		minBet = thisSign.blackjackGetMinBet();
		maxBet = thisSign.blackjackGetMaxBet();
			
		
		waitingForInputs.put(player, this);
		
		player.sendMessage("\n\n"+CasinoManager.getPrefix() + MessageManager.get("blackjack-welcome_message").replace("%min_bet%", Main.econ.format(minBet)).replace("%max_bet%", Main.econ.format(maxBet)));
		
		
		this.waitingForBet = true;
		//check after 1 minute if player put nothing in reset sign!
		currentWaitingTask = main.getServer().getScheduler().runTaskLater(main, new Runnable() {
			@Override
			public void run() {
				if(waitingForInputs.containsKey(player)) {
					waitingForInputs.remove(player);
					resetSign();
					CasinoManager.Debug(this.getClass(), "no message from player after 1 Minute, all actions canceled!");
				}
			}
		}, 60*20).getTaskId();
		
		resetTask = main.getServer().getScheduler().runTaskLater(main, new Runnable() {
			@Override
			public void run() {
				resetSign();
			}
		}, 20*60*10).getTaskId();
	}
	public void userBetInput(Double input) { //amount what the player want to bet at start
		cards.add(Card.newCard());
		cards.add(Card.newCard());
		
		dealer.add(Card.newCard());
		this.playerBet = input;
		
		Main.econ.withdrawPlayer(player, playerBet);
		CasinoManager.Debug(this.getClass(), player.getName() + " - " + Main.econ.format(playerBet) + " because of the bet!");
		
		contactOwner(MessageManager.get("blackjack-owner-player_playing").replace("%playername%", player.getPlayerListName()).replace("%money%", Main.econ.format(playerBet)));
		//Main.econ.depositPlayer(owner, playerBet);
		thisSign.depositOwner(playerBet);
		
		if(!thisSign.isServerOwner() && owner.isOnline())
		CasinoManager.Debug(this.getClass(), owner.getName() + " +" + Main.econ.format(playerBet) + " because " + player.getName() + " clicked on his sign");
		
		if(!thisSign.isServerOwner())
		{
			OfflineEarnManager.getInstance().addEarning(owner, this.playerBet);
			CasinoManager.Debug(this.getClass(), "[OFFLINE] " + owner.getName() + " +" + Main.econ.format(playerBet) + " because " + player.getName() + " clicked on his sign");
		}
		
		
		if(Card.getValue(cards) == 21)
			dealerLost();
		nextRound();
	}
	
	public void userSplitCards() {
		
	}
	
	public void nextCard() {
		cards.add(Card.newCard());
		nextRound();
	}
	
	
	private void finish() {
		if(finished) return;
		simulateDealer();
		changeSign();
			
		main.getServer().getScheduler().runTaskLater(main, new Runnable() {
			@Override
			public void run() {
				resetSign();
			}
		}, 60L); //60L default
		finished = true;
		main.getServer().getScheduler().cancelTask(resetTask);
	}
	private void resetSign() { //animation is "finished" and bring back to normal
		if(waitingForInputs.containsKey(player)) 
			waitingForInputs.remove(player);
		thisSign.isRunning = false;
	}
	
	private void nextRound() { //show the player which card he has and what he can do next
		if(checkIfSomeoneWon()) {
			//player lost
			
			
		} else if(!finished) {
			
			changeSign();
			int gesamtValue = Card.getValue(cards);
			String cardsString = "";
			for(int i = 0; i < cards.size(); i++) {
				if(i == cards.size()-1) {
					cardsString += cards.get(i).toString() + " = " + gesamtValue;
				} else 
					cardsString += cards.get(i).toString() + " + ";
			}
			
			if(Card.getValue(cards) == 21)
				dealerLost();
			else
			{
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("blackjack-next-possibilities"));
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("blackjack-next-possibilities-cards").replace("%cards%", cardsString));
			CasinoManager.Debug(this.getClass(), player.getName() + " cards: " + cardsString);
			waitingForInputs.put(player, this);
			this.waitingForGameDecision = true;
			}
		}
		
		
	}
	private void simulateDealer() { //on the next Round the dealer should made this action! (simulated) return if dealer wons/lose
		int valueOfDealer = Card.getValue(dealer);
		
		if(!(Card.getValue(cards) > 21)) //only calc dealer values when the player could possible win
			while(valueOfDealer < 17) {
				dealer.add(Card.newCard());
				valueOfDealer = Card.getValue(dealer);
			}
	}
	private Boolean checkIfSomeoneWon() { //before the next round started check if the dealer or the player could win the game!
		int valueOfDealer = Card.getValue(dealer);
		int valueOfPlayer = Card.getValue(cards);
		if(valueOfPlayer > 21) {
			playerLost();
			return true;
		}
		if(playerWantToSkip) {
			if(valueOfDealer < 17)
			{
				simulateDealer();
				valueOfDealer = Card.getValue(dealer);
			}
			
			if(valueOfDealer == 21 && valueOfPlayer != 21) //dealer got a blackjack and player not
			{
				playerLost();
				return true;
			}
			if(valueOfPlayer == 21 && valueOfDealer != 21) //player got a blackjack and dealer not
			{
				dealerLost();
				return true;
			}
			if(valueOfDealer == 21 && valueOfPlayer == 21)
			{
				if(dealer.size() > cards.size())
					dealerLost(); //dealer lose because player has less cards
				else
					playerLost(); //player lose because dealer has less cards
				return true;
			}
			
			if(valueOfDealer > 21) //dealer loses because his cards does have a value more than 21
			{
				dealerLost();
				return true;
			}
			if(valueOfPlayer > 21) //player loses because his cards does have a value more than 21
			{
				playerLost();
				return true;
			}
			
			if(valueOfDealer > valueOfPlayer)
			{
				playerLost(); //dealer is more near to 21 than the player
				return true;
			} 
			else  if(valueOfDealer < valueOfPlayer)
			{
				dealerLost(); //player is more near to 21 than the dealer
				return true;
			}
			if(valueOfDealer == valueOfPlayer)
			{
				draw();
				return true;
			}
		}
		return false;
		
	}
	private void draw()
	{
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("blackjack-draw"));
		contactOwner(MessageManager.get("blackjack-owner-draw").replace("%playername%", player.getDisplayName()));
		LeaderboardsignsManager.addData(player, thisSign, this.playerBet, this.playerBet);
		
		CasinoManager.Debug(this.getClass(), "Draw!");
		
		Main.econ.depositPlayer(player, this.playerBet);
		
		//pay bet back
		if(owner.isOnline())
			thisSign.withdrawOwner(this.playerBet);
		else
			OfflineEarnManager.getInstance().addLoss(owner, this.playerBet);
		
		player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT, 4f, 2.5f);
		
		finish();
		
	}
	
	private void playerLost() {
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("blackjack-player_lost"));
		
		contactOwner(MessageManager.get("blackjack-owner-player_lost").replace("%playername%", player.getPlayerListName()));
	
		LeaderboardsignsManager.addData(player, thisSign, this.playerBet, 0);
		CasinoManager.Debug(this.getClass(), player.getName() + " lost!");
		player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 4f, 2.5f);
		finish();
	}
	private void dealerLost() {
		if(Card.getValue(dealer) == 21 && Card.getValue(cards) != 21)
		{
			playerLost(); //when dealer has 21 and the player not
			return;
		}
		
		double winamount = (Card.getValue(cards) == 21) ? this.playerBet * this.thisSign.blackjackMultiplicator() + this.playerBet : this.playerBet * 2;
		if(Card.getValue(cards) == 21) {
			//player.sendMessage(CasinoManager.getPrefix() + "§lYou got a Blackjack!");
			player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("blackjack-player_blackjack"));
		}
		//player.sendMessage(CasinoManager.getPrefix() + "§aYou won " + Main.econ.format(winamount));
		player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("blackjack-player_won").replace("%amount%", Main.econ.format(winamount)));
		
		CasinoManager.Debug(this.getClass(), "won!");
		//contactOwner(String.format("§4%s won at your blackjack sign, you lost: %s", player.getPlayerListName(), Main.econ.format(winamount)));
		contactOwner(MessageManager.get("blackjack-owner-player_won").replace("%playername%", player.getPlayerListName()).replace("%amount%", Main.econ.format(winamount)));
		
		LeaderboardsignsManager.addData(player, thisSign, this.playerBet, winamount);
		Main.econ.depositPlayer(player, winamount);
		CasinoManager.Debug(this.getClass(), player.getName() + " +" + Main.econ.format(winamount) + " because of win!");
		
		//Main.econ.withdrawPlayer(owner, winamount);
		thisSign.withdrawOwner(winamount);
	
		if(!thisSign.isServerOwner())
		{
			CasinoManager.Debug(this.getClass(), owner.getName() + " -" + Main.econ.format(winamount) + " because of lose!");
			OfflineEarnManager.getInstance().addLoss(owner, winamount);
			
		}
		
		player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 4f, 2.5f);

		finish();
	}
	private void changeSign() { //change the sign to the new values
		String a = "";
		for(Card card : cards)
			a += card.toString() + ", ";
		
		this.sign.setLine(0, "§4§ldealer: " + Card.getValue(dealer));
		this.sign.setLine(1, "§6§lbet: " + Main.econ.format(this.playerBet));
		this.sign.setLine(2, a);
		this.sign.setLine(3, String.valueOf(Card.getValue(cards)));
		
		this.sign.update(true);
	}
	
	public static Boolean IsBlackJackAnimationWaitingForUserInput(Player player) {
		return waitingForInputs.containsKey(player);
	}
	public static void userInput(String message, Player player) {
		BlackjackAnimation thisAnimation = waitingForInputs.get(player);
		if(thisAnimation == null) {
			return;
		}
		
		if(thisAnimation.waitingForBet) {
			Double eingabe = null;
			try {
				eingabe = Double.parseDouble(message);
			} catch(NumberFormatException e) {
				player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("blackjack-input-incorrect"));
				return;
			}

			
			if(eingabe < thisAnimation.minBet) {
				//player.sendMessage(CasinoManager.getPrefix() + "§4Your number is smaller than " + Main.econ.format(thisAnimation.minBet));
				player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("blackjack-input-bet_is_too_low").replace("%min_bet%", Main.econ.format(thisAnimation.minBet)));
				return;
			}
			if(eingabe > thisAnimation.maxBet) {
				//player.sendMessage(CasinoManager.getPrefix() + "§4Your number is higher than " + Main.econ.format(thisAnimation.maxBet));
				player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("blackjack-input-bet_is_too_high").replace("%max_bet%", Main.econ.format(thisAnimation.maxBet)));
				return;
			}
			if(!(Main.econ.has(player, eingabe))) {
				player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("blackjack-input-not_enough_money"));
				return;
			}
			thisAnimation.main.getServer().getScheduler().cancelTask(thisAnimation.currentWaitingTask);
			waitingForInputs.remove(player);
			thisAnimation.userBetInput(eingabe);
			thisAnimation.waitingForBet = false;
		} else if(thisAnimation.waitingForGameDecision) {
			//test the possibilities for the playr
			//stop for leaving sign
			if(message.equalsIgnoreCase("cancel")) {
				thisAnimation.resetSign();
				player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("blackjack-player_left"));
				CasinoManager.Debug(thisAnimation.getClass(), player.getName() + " left!");
				
			} else if(message.equalsIgnoreCase("stand")) {
				thisAnimation.playerWantToSkip = true;
				thisAnimation.nextRound();
				thisAnimation.finish();
			} else if(message.equalsIgnoreCase("hit")) {
				waitingForInputs.remove(player);
				thisAnimation.nextCard();
				return; //causes error if not return here
			} else 
			{
				player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("blackjack-input-incorrect"));
				return;
			}
			thisAnimation.main.getServer().getScheduler().cancelTask(thisAnimation.currentWaitingTask);
			thisAnimation.waitingForGameDecision = false;
			waitingForInputs.remove(player);
		}
		
		
		
	}
	private void contactOwner(String message) {
		if(!thisSign.isServerOwner() && this.owner.isOnline()) {
			owner.getPlayer().sendMessage(CasinoManager.getPrefix() + message);
		}
	}
}
