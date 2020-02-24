package serializeableClass;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

//format of data.yml:
	//<UUID>;<world>;<location splited by ','>; <playAmount>;<wonAmount>;<timestamp>
	//bsp:
	//1892-8172817-38738;world;12,13,14;25;50;154272772
	
public class PlayData {
	
	public OfflinePlayer Player;
	public World World;
	public Location Location;
	public double PlayAmount;
	public double WonAmount;
	public long Timestamp;
}
