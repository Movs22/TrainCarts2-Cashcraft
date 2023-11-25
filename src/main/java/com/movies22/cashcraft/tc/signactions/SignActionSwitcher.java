package com.movies22.cashcraft.tc.signactions;

import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Player;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.PathFinding.PathOperation;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.utils.Despawn;
import com.movies22.cashcraft.tc.utils.Guides;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SignActionSwitcher extends SignAction {

	private PathOperation t;
	public Boolean execute(MinecartGroup group) {
		if(!this.node.rail.getMaterial().equals(Material.RAIL)) {
			TrainCarts.plugin.getLogger().log(Level.WARNING, this.node.getLocationStr() + " is an invalid SignActionSwitcher.");
			return false;
		}
		t = null;
		BlockFace s = group.head().facing;
		this.node.connections.forEach(con -> {
			if(con.getEndNode() != null && con.getEndNode().equals(group.head().getNextNode(1, true))) {
				t = con;
			}
		});
		Rail.Shape a;
		if(t == null) {
			group.destroy(Despawn.INVALID_HEADING);
		}
		if(s.getOppositeFace().equals(t.facing)) {
			try {
				a = Rail.Shape.valueOf(s + "_" + t.facing);
			} catch(IllegalArgumentException e) {
				try {
					a = Rail.Shape.valueOf(t.facing + "_" + s);
				} catch(IllegalArgumentException e2) {
					group.destroy(Despawn.INVALID_ROUTE);
					throw(e2);
				}
			} 
		} else {
			try {
				a = Rail.Shape.valueOf(s.getOppositeFace() + "_" + t.facing);
			} catch(IllegalArgumentException e) {
				try {
					a = Rail.Shape.valueOf(t.facing + "_" + s.getOppositeFace());
				} catch(IllegalArgumentException e2) {
					group.destroy(Despawn.INVALID_ROUTE);
					throw(e2);
				}
			}
		}
		group.head().facing = t.facing;
		if(a instanceof Rail.Shape) {
			this.node.rail.setShape(a);
			this.node.loc.getBlock().setBlockData(this.node.rail);
			this.node.loc.getBlock().getState().update();
		}
    	return true;
    }
	
	public Boolean exit(MinecartGroup g) {
		this.executed.remove(g);
		return true;
	}
	
	@Override
	public Boolean match(String s) {
    	return s.toLowerCase().equals("t:switcher");
    }
	
	@Override
	public String getAction() {
		return "SignActionSwitcher";
    }
	
	@Override
	public void handleBuild(Player p) {
		TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
		TextComponent clickable = new TextComponent(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "SWITCHER");
		TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
		TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to change a train's direction.");
		clickable.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Guides.GUIDE_LINK.id + Guides.SWITCHER_SIGN.id));
		p.spigot().sendMessage(m1, clickable, m2, m3);
	}
}
