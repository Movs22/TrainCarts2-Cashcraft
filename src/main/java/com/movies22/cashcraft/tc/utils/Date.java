package com.movies22.cashcraft.tc.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Date {
   public long _timestamp = -1L;

   public Date(long i) {
      this._timestamp = i;
   }

   public boolean isPast(long i) {
      return i < this._timestamp;
   }

   public boolean isFuture(long i) {
      return i > this._timestamp;
   }

   public Date fromString(String s) {
      LocalDateTime n = LocalDateTime.now();
      long t = LocalDateTime.parse(n.getYear() + "-" + n.getMonthValue() + "-" + n.getDayOfMonth() + "T" + s).atZone(ZoneId.of("Europe/London")).toInstant().toEpochMilli();
      return new Date(t);
   }
}
