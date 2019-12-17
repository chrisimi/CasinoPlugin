package serializeableClass;

import java.util.List;
import java.util.Random;

public class Card {
	private Integer value;
	public Card() {
		Random rnd = new Random();
		value = rnd.nextInt(11+1-2) + 2;
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
		return value;
	}
	
	@Override
	public String toString() {
		if(value <= 10)
			return String.valueOf(value);
		else
			return "A";
	}
	
}
