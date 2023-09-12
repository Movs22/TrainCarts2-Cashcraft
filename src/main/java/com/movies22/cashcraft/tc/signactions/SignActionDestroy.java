package com.movies22.cashcraft.tc.signactions;

import org.bukkit.entity.Player;

import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.utils.Guides;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SignActionDestroy extends SignAction {

	private String despawn;
	
	public Boolean execute(MinecartGroup group) {
		if(this.despawn != null) {
			if(group.currentRoute.name.startsWith(this.despawn)) {
				group.destroy();
				return true;
			}
			return false;
		} else {
			group.destroy();
			return true;
		}
    }
	
	@Override
	public Boolean match(String s) {
		
    	return s.toLowerCase().equals("t:destroy");
    }
	
	@Override
	public String getAction() {
		return "SignActionDestroy";
    }
	
	@Override
	public void postParse() {
		String[] a = this.content.split(" ");
		if(a.length > 1) {
			try {
				this.despawn = a[1];
			} catch(IllegalArgumentException e) {
				this.despawn = null;
			}
		} else {
			this.despawn = null;
		}
    	return;
    }
	
	
	@Override
	public void handleBuild(Player p) {
		TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
		TextComponent clickable = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "TRAIN DESTROYER");
		TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
		TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to destroy any train that activates it.");
		clickable.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Guides.GUIDE_LINK.id + Guides.DESTROY_SIGN.id));
		p.spigot().sendMessage(m1, clickable, m2, m3);
	}
}
