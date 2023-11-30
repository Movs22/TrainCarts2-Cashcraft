package com.movies22.cashcraft.tc.api;

import com.movies22.cashcraft.tc.utils.Date;
import com.movies22.cashcraft.tc.utils.SpawnerRateModifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class SpawnerRate {
   public List<Date> spawnTimes = new ArrayList();
   public List<Integer> trainLength = new ArrayList();
   public long offset = -1L;
   public long rate = -1L;
   public String route;
   public String length;

   public static String convert(long seconds) {
      long s = seconds % 60L;
      long m = seconds / 60L % 60L;
      long h = seconds / 3600L % 24L;
      return String.format("%d:%02d:%02d", h, m, s);
   }

   public SpawnerRate(long offset, long rate, String route, String length) {
      String[] lengths = length.split("/");
      this.length = length;
      this.offset = offset;
      this.rate = rate;
      this.route = route;
      if (rate > 0L) {
         ZonedDateTime n = LocalDateTime.now().atZone(ZoneId.of("Europe/Paris"));
         long n2 = n.toInstant().toEpochMilli() - 3600000L;
         long open = 0L;
         long closure = 0L;
         int dc = 0;
         if (n.getHour() <= 4) {
            dc = -1;
         }

         try {
            open = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + dc) + " 04:30:00").getTime();
            closure = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + 1 + dc) + " 03:55:00").getTime();
         } catch (ParseException var19) {
            var19.printStackTrace();
         }

         SpawnerRateModifier.init();
         long a = open;

         while(a < closure) {
            SpawnerRateModifier.SpawnRateMod mod = SpawnerRateModifier.getMod(a);
            if (mod.modifier != 0.0D) {
               if (a > n2 + 5000L) {
                  if (mod.modifier == 1.0D) {
                     this.trainLength.add(Integer.parseInt(lengths[0]));
                  } else if (mod.modifier == 0.5D) {
                     this.trainLength.add(Integer.parseInt(lengths[1]));
                  } else if (mod.modifier == 0.25D) {
                     this.trainLength.add(Integer.parseInt(lengths[2]));
                  } else {
                     a = Long.MAX_VALUE;
                  }

                  this.spawnTimes.add(new Date(a + Math.round((double)offset / (mod.modifier * 1000.0D) * 1000.0D)));
               }

               a += Math.round((double)rate / (mod.modifier * 1000.0D)) * 2000L;
            } else {
               a = Long.MAX_VALUE;
            }
         }
      }

   }

   public Date getNextSpawnTime() {
      return this.getNextSpawnTime(0);
   }

   public Date getNextSpawnTime(int z) {
      long n = LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toInstant().toEpochMilli() - 3600000L;
      Date a = null;

      byte i;
      for(i = 0; a == null && i < this.spawnTimes.size(); a = null) {
         a = (Date)this.spawnTimes.get(i);
         if (!a.isFuture(n - 1000L)) {
            break;
         }

         this.trainLength.remove(this.spawnTimes.indexOf(a));
         this.spawnTimes.remove(a);
      }

      return this.spawnTimes.size() < i + z + 1 ? null : (Date)this.spawnTimes.get(i + z);
   }

   public int getNextTrain() {
      return (Integer)this.trainLength.get(0);
   }
}
