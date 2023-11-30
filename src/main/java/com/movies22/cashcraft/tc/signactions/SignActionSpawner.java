package com.movies22.cashcraft.tc.signactions;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.api.Depot;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.utils.Guides;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.entity.Player;

public class SignActionSpawner extends SignAction {
   public String lane = null;
   public Depot depot = null;
   public int length = 0;

   public Boolean execute(MinecartGroup group) {
      return true;
   }

   public Boolean match(String s) {
      return s.toLowerCase().equals("t:spawn");
   }

   public void postParse() {
      try {
         String[] a = this.content.split(" ");
         this.lane = a[1];
         Depot b = TrainCarts.plugin.DepotStore.getFromCode(a[2]);
         if (b == null) {
            TrainCarts.plugin.getLogger().log(Level.WARNING, this.content + " is an invalid SignActionPlatform sign.");
            this.lane = null;
            this.node.line.deleteNode(this.node);
            if (this.node.line != TrainCarts.plugin.global) {
               TrainCarts.plugin.global.deleteNode(this.node);
            }

         } else {
            this.depot = b;
            b.addLane(a[1], this);
         }
      } catch (IndexOutOfBoundsException var3) {
         TrainCarts.plugin.getLogger().log(Level.WARNING, this.content + " is an invalid SignActionPlatform sign.");
         this.lane = null;
         this.node.line.deleteNode(this.node);
         if (this.node.line != TrainCarts.plugin.global) {
            TrainCarts.plugin.global.deleteNode(this.node);
         }

      }
   }

   public String getAction() {
      return "SignActionSpawner";
   }

   public void handleBuild(Player p) {
      TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
      TextComponent clickable = new TextComponent("" + ChatColor.BLUE + ChatColor.UNDERLINE + "SPAWNER");
      TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
      TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to spawn a train.");
      clickable.setClickEvent(new ClickEvent(Action.OPEN_URL, Guides.GUIDE_LINK.id + "Guides.SPAWNER_SIGN"));
      p.spigot().sendMessage(new BaseComponent[]{m1, clickable, m2, m3});
   }
}
