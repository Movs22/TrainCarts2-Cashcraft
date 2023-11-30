package com.movies22.cashcraft.tc.webserver;

import com.movies22.cashcraft.tc.TrainCarts;
import com.movies22.cashcraft.tc.PathFinding.PathRoute;
import com.movies22.cashcraft.tc.api.MetroLines;
import com.movies22.cashcraft.tc.api.MinecartGroup;
import com.movies22.cashcraft.tc.api.MinecartMember;
import com.movies22.cashcraft.tc.api.Station;
import com.movies22.cashcraft.tc.signactions.SignActionPlatform;
import express.Express;
import express.utils.Status;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainServer {
   private Express app;
   private TrainCarts tc;
   private MetroLines lines;

   public MainServer() {
      this.tc = TrainCarts.plugin;
      this.lines = TrainCarts.plugin.lines;
   }

   public String getOsi(String s) {
      String z = "[";

      for(int i = 0; i < s.length(); ++i) {
         char z2 = s.charAt(i);
         MetroLines.MetroLine l = this.lines.getFromChar("" + z2);
         if (i != 0) {
            z = z + ", ";
         }

         z = z + "\"" + l.getName() + "\"";
      }

      z = z + "]";
      return z;
   }

   public List<String> getOsi(String s, Boolean b) {
      List<String> z = new ArrayList();

      for(int i = 0; i < s.length(); ++i) {
         char z2 = s.charAt(i);
         MetroLines.MetroLine l = this.lines.getFromChar("" + z2);
         z.add(l.getName());
      }

      return z;
   }

   public String getStationInfo(Station s) {
      String plat = "[";
      int x = Integer.MAX_VALUE;
      char x2 = '?';
      int i = 0;

      for(Iterator var7 = s.platforms.keySet().iterator(); var7.hasNext(); ++i) {
         String p = (String)var7.next();
         if (i != 0) {
            plat = plat + ", ";
         }

         plat = plat + "\"" + p + "\"";
      }

      plat = plat + "]";
      return "{\"name\":\"" + s.name + "\",\"code\":\"" + s.code + "\",\"lines\":" + this.getOsi(s.osi) + ", \"platforms\":" + plat + ", \"nextTrain\":" + x + ", \"nextTrainChar\":\"" + x2 + "\"}";
   }

   public String getTrainInfo(MinecartGroup s) {
      String stops = "[";
      int i = 0;

      for(Iterator var5 = s.currentRoute.stops.iterator(); var5.hasNext(); ++i) {
         SignActionPlatform p = (SignActionPlatform)var5.next();
         if (i != 0) {
            stops = stops + ", ";
         }

         stops = stops + "\"" + p.station.name + " (Platform " + p.platform + ")\"";
      }

      stops = stops + "]";
      return "{\"headcode\":\"" + s.getHeadcode() + "\",\"length\":\"" + s._getLength() + "\",\"route\":\"" + s.currentRoute._line.getName() + ":" + s.currentRoute.name + "\", \"nextService\":" + s.nextTrain + ", \"stops\":" + stops + "\"virtualized\":" + s.virtualized + "}";
   }

   public String getLineInfo(MetroLines.MetroLine s) {
      String stops = "[";
      int i = 0;

      for(Iterator var5 = s.getRoutes().keySet().iterator(); var5.hasNext(); ++i) {
         String p = (String)var5.next();
         if (i != 0) {
            stops = stops + ", ";
         }

         stops = stops + "\"" + p + "\"";
      }

      stops = stops + "]";
      return "{\"name\":\"" + s.getName() + "\",\"character\":\"" + s.getChar() + "\",\"colour\":\"" + s.getColour() + "\", \"routes\":" + stops + "}";
   }

   public String getRouteInfo(PathRoute s) {
      String stops = "[";
      stops = stops + "\"" + ((SignActionPlatform)s._start.getAction()).station.name + "~" + ((SignActionPlatform)s._start.getAction()).platform + "\"";

      SignActionPlatform p;
      for(Iterator var4 = s.stops.iterator(); var4.hasNext(); stops = stops + "\"" + p.station.name + "~" + p.platform + "\"") {
         p = (SignActionPlatform)var4.next();
         stops = stops + ", ";
      }

      stops = stops + "]";
      return "{\"name\":\"" + s.name + "\",\"line\":\"" + s._line.getName() + "\",\"reverse\":\"" + s.reverse + "\", \"stops\":" + stops + "}";
   }

   public void enable() {
      this.app = new Express();
      this.app.get("/", (req, res) -> {
         res.send("{\"version\":\"" + this.tc.version + "\", \"mcVersion\":\"" + this.tc.mcVersion + "\"}");
      });
      this.app.get("/stations", (req, res) -> {
         String s = "";
         int i = 0;

         for(Iterator var6 = this.tc.StationStore.Stations.values().iterator(); var6.hasNext(); ++i) {
            Station station = (Station)var6.next();
            if (i != 0) {
               s = s + ", ";
            }

            s = s + this.getStationInfo(station);
         }

         res.send("{\"stations\":[" + s + "]}");
      });
      this.app.get("/trains", (req, res) -> {
         String s = "";
         int i = 0;

         for(Iterator var6 = this.tc.MemberController.getHeads().iterator(); var6.hasNext(); ++i) {
            MinecartMember train = (MinecartMember)var6.next();
            MinecartGroup g = train.getGroup();
            if (i != 0) {
               s = s + ", ";
            }

            s = s + this.getTrainInfo(g);
         }

         res.send("{\"trains\":[" + s + "]}");
      });
      this.app.get("/lines", (req, res) -> {
         String s = "";
         int i = 0;

         for(Iterator var6 = this.tc.lines.getLines().values().iterator(); var6.hasNext(); ++i) {
            MetroLines.MetroLine l = (MetroLines.MetroLine)var6.next();
            if (i != 0) {
               s = s + ", ";
            }

            s = s + this.getLineInfo(l);
         }

         res.send("{\"lines\":[" + s + "]}");
      });
      this.app.get("/station/:station", (req, res) -> {
         Station s = this.tc.StationStore.getFromName(req.getParam("station").replaceAll("-", " ").replaceAll("%2D", " "));
         if (s == null) {
            s = this.tc.StationStore.getFromCode(req.getParam("station"));
         }

         if (s == null) {
            res.sendStatus(Status._404);
         }

         res.send(this.getStationInfo(s));
      });
      this.app.get("/lines/:line", (req, res) -> {
         MetroLines.MetroLine l = this.tc.lines.getLine(req.getParam("line"));
         if (l == null) {
            res.sendStatus(Status._404);
         }

         res.send(this.getLineInfo(l));
      });
      this.app.get("/routes/:line/:route", (req, res) -> {
         MetroLines.MetroLine l = this.tc.lines.getLine(req.getParam("line"));
         if (l == null) {
            res.sendStatus(Status._404);
         }

         PathRoute r = l.getRoute(req.getParam("route"));
         res.send(this.getRouteInfo(r));
      });
      this.app.get("/train/:train", (req, res) -> {
         MinecartGroup g = null;
         Iterator var5 = this.tc.MemberController.getHeads().iterator();

         while(var5.hasNext()) {
            MinecartMember mh = (MinecartMember)var5.next();
            if (mh.getGroup().getHeadcode().equals(req.getParam("train"))) {
               g = mh.getGroup();
            }
         }

         if (g == null) {
            res.sendStatus(Status._404);
         }

         res.send(this.getTrainInfo(g));
      });
      this.app.listen(25566);
   }

   public void disable() {
      this.app.stop();
   }
}
