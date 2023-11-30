package com.movies22.cashcraft.tc.signactions;

import com.movies22.cashcraft.tc.PathFinding.PathRoute;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.utils.Guides;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.entity.Player;

public class SignActionRBlocker extends SignAction {
   public List<String> blocked = new ArrayList();
   private Boolean b;

   public Boolean execute(MinecartGroup group) {
      return true;
   }

   public Boolean match(String s) {
      return s.toLowerCase().startsWith("t:rblocker");
   }

   public String getAction() {
      return "SignActionRBlocker";
   }

   public void postParse() {
      this.blocked.clear();
      String[] a = this.content.split(" ");
      if (a.length > 1) {
         for(int i = 1; i < a.length; ++i) {
            this.blocked.add(a[i]);
         }
      }

   }

   public Boolean isBlocked(PathRoute r) {
      this.blocked.clear();
      String[] c = this.content.split(" ");
      if (c.length > 1) {
         for(int i = 1; i < c.length; ++i) {
            this.blocked.add(c[i]);
         }
      }

      this.b = false;
      this.blocked.forEach((a) -> {
         if (a.equals(r._line.getName()) || r.name.toUpperCase().startsWith(a.toUpperCase())) {
            this.b = true;
         }

      });
      return this.b;
   }

   public void handleBuild(Player p) {
      TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
      TextComponent clickable = new TextComponent("" + ChatColor.BLUE + ChatColor.UNDERLINE + "BLOCKER");
      TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
      TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to stop trains from entering this section from the specified direction.");
      clickable.setClickEvent(new ClickEvent(Action.OPEN_URL, Guides.GUIDE_LINK.id + Guides.BLOCKER_SIGN.id));
      p.spigot().sendMessage(new BaseComponent[]{m1, clickable, m2, m3});
   }
}
