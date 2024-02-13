package com.movies22.cashcraft.tc.signactions;

import org.bukkit.entity.Player;

import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.utils.Guides;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SignActionSpeed extends SignAction {
	public Double speed = 0.0;
	public Boolean execute(MinecartGroup group) {
		group.getMembers().forEach(m -> {
			if(!m.virtualized || m.index == 0) {
				m.getEntity().setMaxSpeed(this.getSpeedLimit(group));
			}
		});
    	return true;
    }
	
	@Override
	public Boolean match(String s) {
    	return s.toLowerCase().equals("t:speed");
    }
	
	@Override
	public void postParse() {
		String[] a = this.content.split(" ");
		this.speed = Double.valueOf(a[1]);
	}
	
	@Override
	public String getAction() {
		return "SignActionSpeed";
    }
	
	@Override
	public Double getSpeedLimit(MinecartGroup group) {
		return this.speed;
    }
	
	@Override
	public void handleBuild(Player p) {
		TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
		TextComponent clickable = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "SPEED");
		TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
		TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to change the train's speed.");
		clickable.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Guides.GUIDE_LINK.id + Guides.SPEED_SIGN.id));
		p.spigot().sendMessage(m1, clickable, m2, m3);
	}
}
