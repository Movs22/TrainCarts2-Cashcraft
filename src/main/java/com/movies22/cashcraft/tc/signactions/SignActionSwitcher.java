package com.movies22.cashcraft.tc.signactions;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Player;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.pathFinding.PathOperation;
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
		group.route.removeIf(a -> a.getEndNode().equals(this.node));
		BlockFace s = group.head().facing;
		Rail.Shape a;
		if(group.head().getNextNode(1, true).equals(this.node)) {
			try {
				a = Rail.Shape.valueOf(s + "_" + s.getOppositeFace());
			} catch(IllegalArgumentException e) {
				try {
					a = Rail.Shape.valueOf(s.getOppositeFace() + "_" + s);
				} catch(IllegalArgumentException e2) {
					TrainCarts.plugin.getLogger().log(Level.WARNING, "SignActionSwitcher: FAILED to find connection between " + t.facing.toString() + " and " + s.getOppositeFace().toString());
					group.destroy(Despawn.INVALID_ROUTE);
					throw(e2);
				}
			} 
		} else {
			this.node.connections.forEach(con -> {
				if(con.getEndNode() != null && (con.getEndNode().equals(group.head().getNextNode(0, true)) || con.getEndNode().equals(group.head().getNextNode(1, true)))) {
				t = con;
				}
			});
			if(t == null) {
				TrainCarts.plugin.getLogger().log(Level.WARNING, "SignActionSwitcher: FAILED to find connection between " + group.head().getNextNode(1).getLocationStr() + " and " + s.getOppositeFace().toString());
				group.destroy(Despawn.INVALID_HEADING);
				return false;
			}
			if(s.getOppositeFace().equals(t.facing)) {
				try {
					a = Rail.Shape.valueOf(s + "_" + t.facing);
				} catch(IllegalArgumentException e) {
					try {
						a = Rail.Shape.valueOf(t.facing + "_" + s);
					} catch(IllegalArgumentException e2) {
						TrainCarts.plugin.getLogger().log(Level.WARNING, "SignActionSwitcher: FAILED to find connection between " + t.facing.toString() + " and " + s.getOppositeFace().toString());
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
						TrainCarts.plugin.getLogger().log(Level.WARNING, "SignActionSwitcher: FAILED to find connection between " + t.facing.toString() + " and " + s.getOppositeFace().toString());
						group.destroy(Despawn.INVALID_ROUTE);
						throw(e2);
					}
				}
			}
		}
		if(a instanceof Rail.Shape) {
			group.head().getEntity().syncPos(this.node.loc);
			group.head().facing = s.getOppositeFace();
			this.node.rail.setShape(a);
			Location l = this.node.loc;
			TrainCarts.plugin.offlineWorlds.get(l.getWorld()).getBlock(l.getBlockX(), l.getBlockY(), l.getBlockZ()).setBlockData((BlockData) this.node.rail);
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
