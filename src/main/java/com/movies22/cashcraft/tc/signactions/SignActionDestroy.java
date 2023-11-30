package com.movies22.cashcraft.tc.signactions;

import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.utils.Guides;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.entity.Player;

public class SignActionDestroy extends SignAction {
   private String despawn;

   public Boolean execute(MinecartGroup group) {
      if (this.despawn != null) {
         if (group.currentRoute.name.startsWith(this.despawn)) {
            group.destroy();
            return true;
         } else {
            return false;
         }
      } else {
         group.destroy();
         return true;
      }
   }

   public Boolean match(String s) {
      return s.toLowerCase().equals("t:destroy");
   }

   public String getAction() {
      return "SignActionDestroy";
   }

   public void postParse() {
      String[] a = this.content.split(" ");
      if (a.length > 1) {
         try {
            this.despawn = a[1];
         } catch (IllegalArgumentException var3) {
            this.despawn = null;
         }
      } else {
         this.despawn = null;
      }

   }

   public void handleBuild(Player p) {
      TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
      TextComponent clickable = new TextComponent("" + ChatColor.BLUE + ChatColor.UNDERLINE + "TRAIN DESTROYER");
      TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
      TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to destroy any train that activates it.");
      clickable.setClickEvent(new ClickEvent(Action.OPEN_URL, Guides.GUIDE_LINK.id + Guides.DESTROY_SIGN.id));
      p.spigot().sendMessage(new BaseComponent[]{m1, clickable, m2, m3});
   }
}
