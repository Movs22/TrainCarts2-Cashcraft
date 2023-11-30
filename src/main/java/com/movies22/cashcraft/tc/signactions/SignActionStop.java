package com.movies22.cashcraft.tc.signactions;

import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.utils.Guides;
import java.util.Timer;
import java.util.TimerTask;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.entity.Player;

public class SignActionStop extends SignAction {
   public Boolean execute(final MinecartGroup group) {
      long i = Long.parseLong(this.sign.getLine(0).split("t:stop ")[1]) * 1000L;
      (new Timer()).schedule(new TimerTask() {
         public void run() {
            group.head()._targetSpeed = 0.4D;
         }
      }, i);
      return true;
   }

   public Boolean match(String s) {
      return s.toLowerCase().equals("t:stop");
   }

   public String getAction() {
      return "SignActionStop";
   }

   public Double getSpeedLimit(MinecartGroup group) {
      return 0.0D;
   }

   public void handleBuild(Player p) {
      TextComponent m1 = new TextComponent(ChatColor.YELLOW + "You've built a ");
      TextComponent clickable = new TextComponent("" + ChatColor.BLUE + ChatColor.UNDERLINE + "STOP");
      TextComponent m2 = new TextComponent(ChatColor.YELLOW + " sign.");
      TextComponent m3 = new TextComponent(ChatColor.GREEN + "\nUse this sign to make a train stop for a specified time.");
      clickable.setClickEvent(new ClickEvent(Action.OPEN_URL, Guides.GUIDE_LINK.id + Guides.STOP_SIGN.id));
      p.spigot().sendMessage(new BaseComponent[]{m1, clickable, m2, m3});
   }
}
