package serializeableClass;

import java.util.List;
import java.util.Random;

public class Card {
	private Integer value;
	public Card() {
		Random rnd = new Random();
		value = rnd.nextInt(14+1-2) + 2;
	}
	public static Card newCard() {
		return new Card();
	}
	public static Integer getValue(List<Card> cards) {
		int sum = 0;
		for(Card card : cards) {
			sum += card.getValue();
		}
		return sum;
	}
	public Integer getValue() {
		if(value <= 10)
			return value;
		else if(value <= 13)
			return 10;
		else {
			return 11;
		}
	}
	
	@Override
	public String toString() {
		if(value <= 10)
			return String.valueOf(value);
		else if(value == 11)
			return "J";
		else if(value == 12)
			return "Q";
		else if(value == 13)
			return "K";
		else
			return "A";
	}
	
}
