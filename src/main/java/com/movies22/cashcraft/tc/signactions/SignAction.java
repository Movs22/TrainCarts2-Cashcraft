package com.movies22.cashcraft.tc.signactions;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.PathFinding.PathNode;
import com.movies22.cashcraft.tc.PathFinding.PathRoute;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class SignAction implements Cloneable {
   public static List<SignAction> actions = Collections.emptyList();
   public List<MinecartGroup> executed = new ArrayList();
   public List<MinecartGroup> ExitExecuted = new ArrayList();
   public PathNode node;
   public Sign sign;
   public String content;
   public HashMap<PathNode, PathRoute> rcache = new HashMap();
   private String s;

   public static void init() {
      actions = new ArrayList();
      register(new SignActionPlatform());
      register(new SignActionBuffer());
      register(new SignActionSwitcher());
      register(new SignActionSpawner());
      register(new SignActionBlocker());
      register(new SignActionStop());
      register(new SignActionSpeed());
      register(new SignActionRBlocker());
      register(new SignActionDestroy());
   }

   public PathRoute getRoute(PathNode e) {
      PathRoute r = (PathRoute)this.rcache.get(e);
      if (r == null) {
         List<PathNode> a = new ArrayList();
         a.add(this.node);
         a.add(e);
         this.s = "[";
         if (e.getAction() instanceof SignActionPlatform) {
            this.s = "[S";
         }

         r = new PathRoute(this.s + "CACHED ROUTE/" + e.getLocationStr() + "]", a, TrainCarts.plugin.global);
         r.calculate();
         return r.clone();
      } else {
         return r.clone();
      }
   }

   public static <T extends SignAction> T register(T action) {
      actions.add(action);
      return action;
   }

   public Boolean match(String s) {
      return false;
   }

   public Boolean execute(MinecartGroup group) {
      return false;
   }

   public Boolean exit(MinecartGroup group) {
      return false;
   }

   public String getAction() {
      return null;
   }

   public Double getSpeedLimit(MinecartGroup group) {
      return null;
   }

   public BlockFace getBlocked(Sign s) {
      return null;
   }

   public void postParse() {
   }

   public static SignAction parse(Sign s) {
      String a = "";

      for(int i = 0; i < 4; ++i) {
         a = a + s.getLine(i);
      }

      if (!a.startsWith("t:")) {
         return null;
      } else {
         String[] b = a.split(" ");
         Iterator var4 = actions.iterator();

         while(var4.hasNext()) {
            SignAction c = (SignAction)var4.next();
            if (c.match(b[0])) {
               SignAction d = null;

               try {
                  d = (SignAction)c.clone();
                  d.executed = new ArrayList();
                  d.ExitExecuted = new ArrayList();
               } catch (CloneNotSupportedException var7) {
                  var7.printStackTrace();
               }

               d.sign = s;
               d.content = a;
               return d;
            }
         }

         return null;
      }
   }

   public void handleBuild(Player p) {
      p.sendMessage(this.getClass().toString());
   }
}
