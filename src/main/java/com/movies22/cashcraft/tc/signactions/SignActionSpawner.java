package com.movies22.cashcraft.tc.signactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.Depot;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.PathFinding.PathNode;
import com.movies22.cashcraft.tc.PathFinding.PathRoute;
import com.movies22.cashcraft.tc.utils.Guides;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SignActionSpawner extends SignAction {
	public String lane = null;
	public Depot depot = null;
	public int length = 0;
	
	public Boolean execute(MinecartGroup group) {
    	return true;
    }
	
	@Override
	public Boolean match(String s) {
    	return s.toLowerCase().equals("t:spawn");
    }
	
	@Override
	public void postParse() {
		try {
		String[] a = this.content.split(" ");
		this.lane = a[1];
		Depot b = TrainCarts.plugin.DepotStore.getFromCode(a[2]);
		if(b == null) {
			TrainCarts.plugin.getLogger().log(Level.WARNING, this.content + " is an invalid SignActionPlatform sign.");
			this.lane = null;
			this.node.line.deleteNode(this.node);
			if(this.node.line != TrainCarts.plugin.global) {
				TrainCarts.plugin.global.deleteNode(this.node);
			}
			return;
		}
		this.depot = b;
		b.addLane(a[1], this);
	} catch(IndexOutOfBoundsException e) {
		TrainCarts.plugin.getLogger().log(Level.WARNING, this.content + " is an invalid SignActionPlatform sign.");
		this.lane = null;
		this.node.line.deleteNode(this.node);
		if(this.node.line != TrainCarts.plugin.global) {
			TrainCarts.plugin.global.deleteNode(this.node);
		}
		return;
	}
    	return;
    }
	
	
	@Override
	public String getAction() {
		return "SignActionSpawner";
    }
	
	@Override
	public void handleBuild(Player p) {
		TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
		TextComponent clickable = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "SPAWNER");
		TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
		TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to spawn a train.");
		clickable.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Guides.GUIDE_LINK.id + "Guides.SPAWNER_SIGN"));
		p.spigot().sendMessage(m1, clickable, m2, m3);
	}
}
