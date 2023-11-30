package com.movies22.cashcraft.tc.signactions;

import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.utils.Guides;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.entity.Player;

public class SignActionSpeed extends SignAction {
   public Double speed = 0.0D;

   public Boolean execute(MinecartGroup group) {
      return true;
   }

   public Boolean match(String s) {
      return s.toLowerCase().equals("t:speed");
   }

   public void postParse() {
      String[] a = this.content.split(" ");
      this.speed = Double.valueOf(a[1]);
   }

   public String getAction() {
      return "SignActionSpeed";
   }

   public Double getSpeedLimit(MinecartGroup group) {
      return this.speed;
   }

   public void handleBuild(Player p) {
      TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
      TextComponent clickable = new TextComponent("" + ChatColor.BLUE + ChatColor.UNDERLINE + "SPEED");
      TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
      TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to change the train's speed.");
      clickable.setClickEvent(new ClickEvent(Action.OPEN_URL, Guides.GUIDE_LINK.id + Guides.SPEED_SIGN.id));
      p.spigot().sendMessage(new BaseComponent[]{m1, clickable, m2, m3});
   }
}
