package com.movies22.cashcraft.tc.signactions;

import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.utils.Despawn;
import com.movies22.cashcraft.tc.utils.Guides;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SignActionBlocker extends SignAction {

	public Boolean execute(MinecartGroup group) {
    	return true;
    }
	
	@Override
	public Boolean match(String s) {
    	return s.toLowerCase().equals("t:blocker");
    }
	
	@Override
	public String getAction() {
		return "SignActionBuffer";
    }
	
	@Override
	public BlockFace getBlocked(Sign s) {
		return BlockFace.valueOf(s.getLine(0).split(" ")[1]);
	}
	
	@Override
	public void handleBuild(Player p) {
		TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
		TextComponent clickable = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "BLOCKER");
		TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
		TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to stop trains from entering this section from the specified direction.");
		clickable.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Guides.GUIDE_LINK.id + Guides.BLOCKER_SIGN.id));
		p.spigot().sendMessage(m1, clickable, m2, m3);
	}
}
