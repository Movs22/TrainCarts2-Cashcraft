package com.movies22.cashcraft.tc.signactions;

import org.bukkit.entity.Player;

import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.utils.Guides;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SignActionStop extends SignAction {
	public Boolean execute(MinecartGroup group) {
		long i = Long.parseLong(this.sign.getLine(0).split("t:stop ")[1])*1000;
		new java.util.Timer().schedule( 
		        new java.util.TimerTask() {
		            @Override
		            public void run() {
		                group.head()._targetSpeed = 0.4d;
		            }
		        }, 
		        i 
		);
    	return true;
    }
	
	@Override
	public Boolean match(String s) {
    	return s.toLowerCase().equals("t:stop");
    }
	
	@Override
	public String getAction() {
		return "SignActionStop";
    }
	
	@Override
	public Double getSpeedLimit(MinecartGroup group) {
		return 0.0d;
    }
	
	@Override
	public void handleBuild(Player p) {
		TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
		TextComponent clickable = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "STOP");
		TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
		TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to make a train stop for a specified time.");
		clickable.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Guides.GUIDE_LINK.id + Guides.STOP_SIGN.id));
		p.spigot().sendMessage(m1, clickable, m2, m3);
	}
}
