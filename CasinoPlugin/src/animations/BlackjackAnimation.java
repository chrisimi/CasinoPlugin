package animations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.chrisimi.casino.main.Main;

import scripts.CasinoManager;
import scripts.PlayerSignsManager;
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
	
	private int currentWaitingTask = 0;
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
		minBet = thisSign.bet;
		maxBet = thisSign.blackjackGetMaxBet();
		waitingForInputs.put(player, this);
		player.sendMessage(String.format("\n\n"+CasinoManager.getPrefix() + "Welcome to Blackjack! \nTo begin please typ in your bet, between %s and %s", Main.econ.format(minBet), Main.econ.format(maxBet)));
		this.waitingForBet = true;
		//check after 1 minute if player put nothing in reset sign!
		currentWaitingTask = main.getServer().getScheduler().runTaskLater(main, new Runnable() {
			@Override
			public void run() {
				if(waitingForInputs.containsKey(player)) {
					waitingForInputs.remove(player);
					resetSign();
					main.getLogger().info("1 Minute over and no player back message!");
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
		main.getLogger().info("spieler veliert: " + playerBet);
		contactOwner(String.format("%s is playing on a blackjack sign with %s", player.getPlayerListName(), Main.econ.format(playerBet)));
		if(owner.isOnline()) {
			Main.econ.depositPlayer(owner, playerBet);
			main.getLogger().info("owner bekommt: " + playerBet);
			
		} else {
			this.manager.addOfflinePlayerWinOrLose(playerBet, owner);
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
		}, 60L);
		finished = true;
		main.getServer().getScheduler().cancelTask(resetTask);
	}
	private void resetSign() { //animation is "finished" and bring back to normal
		if(waitingForInputs.containsKey(player)) 
			waitingForInputs.remove(player);
		this.manager.animationFinished(this.thisSign);
	}
	
	private void nextRound() { //show the player which card he has and what he can do next
		if(checkIfSomeoneWon()) {
			//player lost
			
			
		} else {
			
			changeSign();
			int gesamtValue = Card.getValue(cards);
			String cardsString = "";
			for(int i = 0; i < cards.size(); i++) {
				if(i == cards.size()-1) {
					cardsString += cards.get(i).toString() + " = " + gesamtValue;
				} else 
					cardsString += cards.get(i).toString() + " + ";
			}
			
			String sendString = String.format(CasinoManager.getPrefix() + "Your next possibilities: \n Skip: skip \n Leave: leave \n Card: card");
			player.sendMessage(sendString);
			player.sendMessage(CasinoManager.getPrefix() + "Your cards: " + cardsString);
			waitingForInputs.put(player, this);
			this.waitingForGameDecision = true;
		}
		
		
	}
	private void simulateDealer() { //on the next Round the dealer should made this action! (simulated) return if dealer wons/lose
		int valueOfDealer = Card.getValue(dealer);
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
			if(valueOfPlayer == 21 && valueOfPlayer == valueOfDealer) {
				if(cards.size() < dealer.size()) {
					dealerLost();
					return true;
				}
			}
			if(valueOfDealer > 21) {
				dealerLost();
				return true;
			}
			if(valueOfPlayer > valueOfDealer) {
				dealerLost();
				return true;
			} else {
				playerLost();
				return true;
			}
		}
		return false;
		
	}
	private void playerLost() {
		player.sendMessage(CasinoManager.getPrefix() + "§4You lost!");
		contactOwner(String.format("%s lost at your blackjack sign!", player.getPlayerListName()));
		
		finish();
	}
	private void dealerLost() {
		double winamount = (Card.getValue(cards) == 21) ? this.playerBet * this.thisSign.blackjackMultiplicator() : this.playerBet;
		if(Card.getValue(cards) == 21) {
			player.sendMessage(CasinoManager.getPrefix() + "§lYou got a Blackjack!");
		}
		player.sendMessage(CasinoManager.getPrefix() + "§aYou won " + Main.econ.format(winamount));
		contactOwner(String.format("§4%s won at your blackjack sign, you lost: %s", player.getPlayerListName(), Main.econ.format(winamount)));
		
		Main.econ.depositPlayer(player, winamount);
		main.getLogger().info("spieler bekommt: " + winamount);
		if(owner.isOnline()) {
			Main.econ.withdrawPlayer(owner, winamount);
			main.getLogger().info("owner verliert: " + winamount);
		}
		else
			this.manager.addOfflinePlayerWinOrLose(winamount * -1, owner);
		
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
				player.sendMessage(CasinoManager.getPrefix() + "§4That's incorrect!");
				return;
			}
			if(eingabe < thisAnimation.minBet) {
				player.sendMessage(CasinoManager.getPrefix() + "§Your number is smaller than " + Main.econ.format(thisAnimation.minBet));
				return;
			}
			if(eingabe > thisAnimation.maxBet) {
				player.sendMessage(CasinoManager.getPrefix() + "§4Your number is higher than " + Main.econ.format(thisAnimation.maxBet));
				return;
			}
			if(!(Main.econ.has(player, eingabe))) {
				player.sendMessage(CasinoManager.getPrefix() + "§4You don't have enough money for that!");
				return;
			}
			thisAnimation.main.getServer().getScheduler().cancelTask(thisAnimation.currentWaitingTask);
			waitingForInputs.remove(player);
			thisAnimation.userBetInput(eingabe);
			thisAnimation.waitingForBet = false;
		} else if(thisAnimation.waitingForGameDecision) {
			//test the possibilities for the playr
			//stop for leaving sign
			if(message.equalsIgnoreCase("leave")) {
				thisAnimation.resetSign();
	
			} else if(message.equalsIgnoreCase("skip")) {
				thisAnimation.playerWantToSkip = true;
				thisAnimation.nextRound();
				thisAnimation.finish();
			} else if(message.equalsIgnoreCase("card")) {
				waitingForInputs.remove(player);
				thisAnimation.nextCard();
				return; //causes error if not return here
			}
			thisAnimation.main.getServer().getScheduler().cancelTask(thisAnimation.currentWaitingTask);
			thisAnimation.waitingForGameDecision = false;
			waitingForInputs.remove(player);
		}
		
		
		
	}
	private void contactOwner(String message) {
		if(this.owner.isOnline()) {
			owner.getPlayer().sendMessage(CasinoManager.getPrefix() + message);
		}
	}
}
