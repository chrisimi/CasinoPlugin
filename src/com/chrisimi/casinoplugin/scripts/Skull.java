package com.chrisimi.casinoplugin.scripts;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class Skull {

	/**
	 * CODE by deanveloper/SkullCreator
	 * MIT Licence
	 * 
	 * Create a Head with base64 code
	 * @param base64
	 * @return head with textures
	 */
	@SuppressWarnings("deprecation")
	public static ItemStack getSkullByTexture(String base64) {
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		UUID uuid = new UUID(base64.hashCode(), base64.hashCode());
		return Bukkit.getUnsafe().modifyItemStack(item, 
				"{SkullOwner:{Id:\"" + uuid + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}");
	}
}
