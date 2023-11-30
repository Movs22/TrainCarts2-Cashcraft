package com.movies22.cashcraft.tc.signactions;

import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.utils.Despawn;
import com.movies22.cashcraft.tc.utils.Guides;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.entity.Player;

public class SignActionBuffer extends SignAction {
   public Boolean execute(MinecartGroup group) {
      group.destroy(Despawn.UNKNOWN_PARENT);
      return true;
   }

   public Boolean match(String s) {
      return s.toLowerCase().equals("t:buffer");
   }

   public String getAction() {
      return "SignActionBuffer";
   }

   public Double getSpeedLimit(MinecartGroup group) {
      return 0.0D;
   }

   public void handleBuild(Player p) {
      TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
      TextComponent clickable = new TextComponent("" + ChatColor.BLUE + ChatColor.UNDERLINE + "BUFFER");
      TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
      TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to stop trains from going past this point.");
      clickable.setClickEvent(new ClickEvent(Action.OPEN_URL, Guides.GUIDE_LINK.id + Guides.BUFFER_SIGN.id));
      p.spigot().sendMessage(new BaseComponent[]{m1, clickable, m2, m3});
   }
}
