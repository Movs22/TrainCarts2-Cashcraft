package com.movies22.cashcraft.tc.signactions;

import org.bukkit.entity.Player;

import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.utils.Despawn;
import com.movies22.cashcraft.tc.utils.Guides;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SignActionBuffer extends SignAction {

	public Boolean execute(MinecartGroup group) {
		group.destroy(Despawn.UNKNOWN_PARENT);
    	return true;
    }
	
	@Override
	public Boolean match(String s) {
		//TrainCarts.plugin.getLogger().log(Level.INFO, "[SignAcionBuffer:match] Trying to match " + s + " to t:buffer.");
    	return s.toLowerCase().equals("t:buffer");
    }
	
	@Override
	public String getAction() {
		return "SignActionBuffer";
    }
	
	@Override
	public Double getSpeedLimit(MinecartGroup group) {
    	return 0.0;
    }
	
	@Override
	public void handleBuild(Player p) {
		TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
		TextComponent clickable = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "BUFFER");
		TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
		TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to stop trains from going past this point.");
		clickable.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Guides.GUIDE_LINK.id + Guides.BUFFER_SIGN.id));
		p.spigot().sendMessage(m1, clickable, m2, m3);
	}
}
