package com.movies22.cashcraft.tc.signactions;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.PathFinding.PathOperation;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.utils.Despawn;
import com.movies22.cashcraft.tc.utils.Guides;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rail.Shape;
import org.bukkit.entity.Player;

public class SignActionSwitcher extends SignAction {
   private PathOperation t;

   public Boolean execute(MinecartGroup group) {
      if (!this.node.rail.getMaterial().equals(Material.RAIL)) {
         TrainCarts.plugin.getLogger().log(Level.WARNING, this.node.getLocationStr() + " is an invalid SignActionSwitcher.");
         return false;
      } else {
         this.t = null;
         BlockFace s = group.head().facing;
         this.node.connections.forEach((con) -> {
            if (con.getEndNode() != null && (con.getEndNode().equals(group.head().getNextNode(0, true)) || con.getEndNode().equals(group.head().getNextNode(1, true)))) {
               this.t = con;
            }

         });
         if (this.t == null) {
            group.destroy(Despawn.INVALID_HEADING);
            return false;
         } else {
            Shape a;
            if (s.getOppositeFace().equals(this.t.facing)) {
               try {
                  a = Shape.valueOf(s + "_" + this.t.facing);
               } catch (IllegalArgumentException var9) {
                  try {
                     a = Shape.valueOf(this.t.facing + "_" + s);
                  } catch (IllegalArgumentException var8) {
                     group.destroy(Despawn.INVALID_ROUTE);
                     throw var8;
                  }
               }
            } else {
               try {
                  a = Shape.valueOf(s.getOppositeFace() + "_" + this.t.facing);
               } catch (IllegalArgumentException var7) {
                  try {
                     a = Shape.valueOf(this.t.facing + "_" + s.getOppositeFace());
                  } catch (IllegalArgumentException var6) {
                     TrainCarts.plugin.getLogger().log(Level.WARNING, "SignActionSwitcher: FAILED to find connection between " + this.t.facing.toString() + " and " + s.getOppositeFace().toString());
                     group.destroy(Despawn.INVALID_ROUTE);
                     throw var6;
                  }
               }
            }

            group.head().facing = this.t.facing;
            if (a instanceof Shape) {
               this.node.rail.setShape(a);
               this.node.loc.getBlock().setBlockData(this.node.rail);
               this.node.loc.getBlock().getState().update();
            }

            return true;
         }
      }
   }

   public Boolean exit(MinecartGroup g) {
      this.executed.remove(g);
      return true;
   }

   public Boolean match(String s) {
      return s.toLowerCase().equals("t:switcher");
   }

   public String getAction() {
      return "SignActionSwitcher";
   }

   public void handleBuild(Player p) {
      TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
      TextComponent clickable = new TextComponent("" + ChatColor.BLUE + ChatColor.UNDERLINE + "SWITCHER");
      TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
      TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to change a train's direction.");
      clickable.setClickEvent(new ClickEvent(Action.OPEN_URL, Guides.GUIDE_LINK.id + Guides.SWITCHER_SIGN.id));
      p.spigot().sendMessage(new BaseComponent[]{m1, clickable, m2, m3});
   }
}
