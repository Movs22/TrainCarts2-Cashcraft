package com.movies22.cashcraft.tc.signactions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.pathFinding.PathRoute;
import com.movies22.cashcraft.tc.utils.Despawn;
import com.movies22.cashcraft.tc.utils.Guides;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SignActionRBlocker extends SignAction {

	public List<String> blocked = new ArrayList<String>();
	
	public Boolean execute(MinecartGroup group) {
    	return true;
    }
	
	@Override
	public Boolean match(String s) {
    	return s.toLowerCase().startsWith("t:rblocker");
    }
	
	@Override
	public String getAction() {
		return "SignActionRBlocker";
    }
	
	@Override
	public void postParse() {
		this.blocked.clear();
		String[] a = this.content.split(" ");
		if(a.length > 1) {
			for(int i = 1; i < a.length; i++) {
				blocked.add(a[i]);
			}
		}
	}
	
	private Boolean b;

	public Boolean isBlocked(String s) {
		this.blocked.clear();
		String[] c = this.content.split(" ");
		if(c.length > 1) {
			for(int i = 1; i < c.length; i++) {
				blocked.add(c[i]);
			}
		}
		b = false;
		this.blocked.forEach(a -> {
			if(a.equals(s) || s.toUpperCase().startsWith(a.toUpperCase())) {
				b = true;
			}
		});
		return b;
	}

	public Boolean isBlocked(PathRoute r) {
		this.blocked.clear();
		String[] c = this.content.split(" ");
		if(c.length > 1) {
			for(int i = 1; i < c.length; i++) {
				blocked.add(c[i]);
			}
		}
		b = false;
		this.blocked.forEach(a -> {
			if(a.equals(r._line.getName()) || r.name.toUpperCase().startsWith(a.toUpperCase())) {
				b = true;
			}
		});
		return b;
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
