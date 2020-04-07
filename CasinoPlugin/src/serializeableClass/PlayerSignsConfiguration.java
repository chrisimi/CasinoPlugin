package serializeableClass;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.chrisimi.casino.main.Main;
import com.google.gson.annotations.Expose;

import scripts.CasinoManager;

public class PlayerSignsConfiguration {
	
	@Expose
	public String gamemode;
	
	@Expose
	public String ownerUUID;
	
	@Expose
	public Double bet;
	
	@Expose
	public String plusinformations;
	
	@Expose
	public Boolean disabled;
	
	@Expose
	public String worldname;
	@Expose
	public double x;
	@Expose
	public double y;
	@Expose
	public double z;
	
	public PlayerSignsConfiguration() {}
	
	public PlayerSignsConfiguration(Location lrc, String gamemode, Player player, Double bet, String plusInformations) {
		this.gamemode = gamemode;
		this.ownerUUID = player.getUniqueId().toString();
		this.bet = bet;
		this.plusinformations = plusInformations;
		this.worldname = lrc.getWorld().getName();
		this.x = lrc.getX();
		this.y = lrc.getY();
		this.z = lrc.getZ();
		this.disabled = false;
	}
	
	
	public Location getLocation() {
		return new Location(Bukkit.getWorld(worldname), x, y, z);
	}
	
	public double[] getWinChancesDice() {
		double[] values = new double[2];
		String[] informations = plusinformations.split(";");
		String[] numbers = informations[0].split("-");
		values[0] = Double.parseDouble(numbers[0]);
		values[1] = Double.parseDouble(numbers[1]);
		return values;
	}
	public double winMultiplicatorDice() {
		String[] informations = plusinformations.split(";");
		Double value = Double.parseDouble(informations[1].trim());
		return value;
	}
	
	public OfflinePlayer getOwner() {
		return Bukkit.getOfflinePlayer(UUID.fromString(this.ownerUUID));
	}
	public Boolean hasOwnerEnoughMoney() {
		return Main.econ.has(getOwner(), this.winMultiplicatorDice() * this.bet);
	}
	public Boolean hasOwnerEnoughMoney(double amount) {
		return Main.econ.has(getOwner(), amount);
	}
	public Sign getSign() {
		return (Sign) Bukkit.getWorld(worldname).getBlockAt(this.getLocation()).getState();
	}
	
	public Boolean isSignDisabled() {
		return disabled;
	}
	public Boolean isSignEnabled() {
		return !(disabled);
	}
	public void enableSign() {
		this.disabled = false;
	}
	public void disableSign() {
		this.disabled = true;
	}
	
	
	public Double blackjackGetMaxBet() {
		String[] values = this.plusinformations.split(";");
		return Double.valueOf(values[0]);
	}
	public Double blackjackMultiplicator() {
		if(this.plusinformations.contains("to"))
		{
			String[] values = this.plusinformations.split(";");
			String[] blackjackValues = values[1].split("to");
			Double left = Double.parseDouble(blackjackValues[0]);
			Double right = Double.parseDouble(blackjackValues[1]);
			return left/right;
		}
		String[] values = this.plusinformations.split(";");
		return Double.valueOf(values[1]);
	}
	public Double blackjackGetWin(Double bet) {
		return blackjackMultiplicator()*bet;
	}
}
