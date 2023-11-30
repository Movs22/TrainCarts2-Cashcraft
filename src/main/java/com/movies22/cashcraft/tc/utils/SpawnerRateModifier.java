package com.movies22.cashcraft.tc.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class SpawnerRateModifier {
   static LocalDateTime n = LocalDateTime.now();
   static long open = 0L;
   static long m1 = 0L;
   static long m2 = 0L;
   static long m3 = 0L;
   static long m4 = 0L;
   static long m5 = 0L;
   static long m6 = 0L;
   static long close = 0L;
   static long extra = 0L;

   public static void init() {
      try {
         int dc = 0;
         if (n.getHour() <= 4) {
            dc = -1;
         }

         open = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + dc) + " 04:30:00").getTime();
         m1 = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + dc) + " 06:00:00").getTime();
         m2 = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + dc) + " 07:00:00").getTime();
         m3 = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + dc) + " 09:30:00").getTime();
         m4 = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + dc) + " 15:00:00").getTime();
         m5 = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + dc) + " 20:30:00").getTime();
         m6 = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + dc) + " 23:00:00").getTime();
         close = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + 1 + dc) + " 01:00:00").getTime();
      } catch (ParseException var1) {
         var1.printStackTrace();
      }

   }

   public static SpawnerRateModifier.SpawnRateMod getMod(long d) {
      SpawnerRateModifier.SpawnRateMod a = SpawnerRateModifier.SpawnRateMod.DEFAULT;
      SpawnerRateModifier.SpawnRateMod[] var3 = SpawnerRateModifier.SpawnRateMod.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         SpawnerRateModifier.SpawnRateMod SpawnMod = var3[var5];
         if (SpawnMod._start <= d && SpawnMod._end >= d) {
            a = SpawnMod;
         }
      }

      return a;
   }

   public static enum SpawnRateMod {
      EARLY_NIGHT(SpawnerRateModifier.open, SpawnerRateModifier.m1, 0.25D),
      EARLY_MORNING(SpawnerRateModifier.m1, SpawnerRateModifier.m2, 0.5D),
      MORNING(SpawnerRateModifier.m2, SpawnerRateModifier.m3, 1.0D),
      NOON(SpawnerRateModifier.m3, SpawnerRateModifier.m4, 0.5D),
      AFTERNOON(SpawnerRateModifier.m4, SpawnerRateModifier.m5, 1.0D),
      EVENING(SpawnerRateModifier.m5, SpawnerRateModifier.m6, 0.5D),
      LATE_NIGHT(SpawnerRateModifier.m6, SpawnerRateModifier.close, 0.25D),
      DEFAULT(-1L, -1L, 0.0D);

      public long _start = 0L;
      public long _end = 0L;
      public double modifier = 0.0D;

      private SpawnRateMod(long start, long end, double mod) {
         this._end = end - 1L;
         this._start = start;
         this.modifier = mod;
      }

      // $FF: synthetic method
      private static SpawnerRateModifier.SpawnRateMod[] $values() {
         return new SpawnerRateModifier.SpawnRateMod[]{EARLY_NIGHT, EARLY_MORNING, MORNING, NOON, AFTERNOON, EVENING, LATE_NIGHT, DEFAULT};
      }
   }
}
