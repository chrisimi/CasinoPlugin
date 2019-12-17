package slotChests;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import org.apache.commons.lang.math.DoubleRange;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.annotations.Expose;

import net.minecraft.server.v1_14_R1.EnumAnimation;


public class SlotChest {
	
	@Expose
	public Boolean enabled;
	@Expose
	public String ownerUUID;
	@Expose
	/**
	 * List with items a player can win; string: name of item&amount, weight
	 */
	public HashMap<String, Double> _itemsToWin;
	@Expose
	/**
	 * Shows, how many items there are in the chest for players to win (name of item, number of items)
	 */
	public HashMap<String, Integer> _lager;
	
	@Expose
	public Integer warehouseLevel;
	
	@Expose
	public Integer winningsLevel;
	
	@Expose
	public double bet;
	
	@Expose
	public double x;
	@Expose
	public double y;
	@Expose
	public double z;
	@Expose
	public String worldname;
	@Expose
	public int animationID;
	
	/**
	 * when owner is in the OwnerInterface, this SlotChest will be in 
	 */
	public Boolean maintenance = false;
	
	public Boolean running = false;
	
	public ArrayList<ItemStack> lager = new ArrayList<>();
	public HashMap<ItemStack, Double> itemsToWin = new HashMap<>();
	
	public SlotChest() {}
	public SlotChest(Player owner, Location lrc) {
		enabled = false;
		ownerUUID = owner.getUniqueId().toString();
		_itemsToWin = new HashMap<String, Double>();
		_lager = new HashMap<String, Integer>();
		warehouseLevel = 0;
		winningsLevel = 0;
		x = lrc.getX();
		y = lrc.getY();
		z = lrc.getZ();
		worldname = lrc.getWorld().getName();
		animationID = 1;
		
		initialize();
	}
	
	public void initialize() {
		Bukkit.getLogger().info("_lager size " + _lager.size());
		if(_lager != null && _lager.size() != 0)
			lager = getLagerFromData();
		if(!(_itemsToWin == null || _itemsToWin.size() == 0))
			itemsToWin = getItemsToWinFromData();
		
		Bukkit.getLogger().info("lager size: " + lager.size());
	}
	public void save() {
		if(itemsToWin != null && itemsToWin.size() != 0) {
			Bukkit.getLogger().info("Size of itemsToWin: " + itemsToWin.size());
			_itemsToWin = new HashMap<String, Double>();
			for(Entry<ItemStack, Double> entry : itemsToWin.entrySet()) {
				String inputString = entry.getKey().getType().toString() + "&" + entry.getKey().getAmount();
				_itemsToWin.put(inputString, entry.getValue());
			}
		}
		if(lager != null && lager.size() != 0) {
			_lager = new HashMap<String, Integer>();
			for(ItemStack item : lager) {
				_lager.put(item.getType().toString(), item.getAmount());
			}
		}
	}
	public Boolean itemstoWinContains(Material material) {
		for(Entry<ItemStack, Double> entry : itemsToWin.entrySet()) {
			if(entry.getKey().getType().equals(material)) return true;
			
		}
		return false;
	}
	/**
	 * Get the Weight of all elements
	 * @return gesamtGewicht
	 */
	public Double getGesamtGewicht() {
		Double value = 0.0;
		for(Entry<ItemStack, Double> entry : itemsToWin.entrySet()) {
			value += entry.getValue();
		}
		return value;
	}
	
	public Location getLocation() {
		World world = Bukkit.getWorld(worldname);
		if(world == null) {
			try {
				throw new Exception("World is not valid!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
			
		}
		return new Location(world, x, y, z);
	}
	public OfflinePlayer getOwner() {
		return Bukkit.getOfflinePlayer(UUID.fromString(this.ownerUUID));
	}
	public HashMap<Material, Integer> getLagerWithNumbers() {
		HashMap<Material, Integer> returnValue = new HashMap<>();
		
		for(ItemStack item : lager) {
			if(item == null) continue;
			System.out.println(item.getType().toString() + " " + item.getAmount());
			
			if(returnValue.containsKey(item.getType())) {
				returnValue.compute(item.getType(), (key, val) -> val + item.getAmount() );
				
			} else {
				returnValue.put(item.getType(), item.getAmount());
			}
		}
		return returnValue;
	}
	public Boolean hasChestEnough() {
		HashMap<Material, Integer> warehouseValues = getLagerWithNumbers();
		
		for(Entry<ItemStack, Double> entry : itemsToWin.entrySet()) {
			
			if(entry.getKey() == null) continue;
			if(entry.getKey().getType() == null) {
				System.out.println("no type found");
				continue;
			}
			if(!warehouseValues.containsKey(entry.getKey().getType())) return false;
			
			if(warehouseValues.get(entry.getKey().getType()) < entry.getKey().getAmount()) {
				return false;
			}
		}
		return true;
	}
	
	private ArrayList<ItemStack> getLagerFromData() {
		Bukkit.getLogger().info("getlagerFromData");
		ArrayList<ItemStack> returnValue = new ArrayList<ItemStack>();
		for(Entry<String, Integer> entries : _lager.entrySet()) {
			ItemStack item = new ItemStack(Enum.valueOf(Material.class, entries.getKey()), entries.getValue());
			returnValue.add(item);
		}
		return returnValue;
	}
	private HashMap<ItemStack, Double> getItemsToWinFromData() {
		HashMap<ItemStack, Double> returnValue = new HashMap<ItemStack, Double>();
		
		for(Entry<String, Double> entry : _itemsToWin.entrySet()) {
			
			String[] splited = entry.getKey().split("&");
			if(splited.length != 2) Bukkit.getLogger().info("Error: WinningsItem format not correct!");
			
			Material material = Enum.valueOf(Material.class, splited[0]);
			int amount = Integer.valueOf(splited[1]);
			
			returnValue.put(new ItemStack(material, amount), entry.getValue());
		}
		return returnValue;
	}
	public ItemStack getRandomItem() {
		Random random = new Random();
		
		double gesamtGewicht = getGesamtGewicht();
		System.out.println(1);
		System.out.println(2);
		Comparator<Entry<ItemStack, Double>> comparator = new Comparator<Entry<ItemStack, Double>>() {

			@Override
			public int compare(Entry<ItemStack, Double> o1, Entry<ItemStack, Double> o2) {
				
				return o1.getValue().compareTo(o2.getValue());
			}
		};
		System.out.println(3);
		ArrayList<Entry<ItemStack, Double>> sortedList = new ArrayList<Entry<ItemStack, Double>>(itemsToWin.entrySet());
		System.out.println(4);
		Collections.sort(sortedList, comparator);
		System.out.println(5);
		LinkedHashMap<ItemStack, Double> linkedList = new LinkedHashMap<>();
		for(Entry<ItemStack, Double> entry : sortedList) {
			linkedList.put(entry.getKey(), entry.getValue());
			System.out.println(entry.getKey().toString() + " " + entry.getValue());
		}
		
		double randomValue = random.nextDouble()*gesamtGewicht;
		System.out.println("roll: " + randomValue);
		Map.Entry<ItemStack, Double> latest = null; 
		for(Entry<ItemStack, Double> entry : linkedList.entrySet()) {
			if(latest == null) {
				latest = entry;
				continue;
			}
			
			if(latest == entry) continue; 
			
			
			if(latest.getValue() < randomValue && entry.getValue() > randomValue)
				return entry.getKey();
			if(latest.getValue() > randomValue)
				return latest.getKey();
			latest = entry;
		}
		return latest.getKey();
	}

	
}
