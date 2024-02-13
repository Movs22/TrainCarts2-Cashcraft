package com.movies22.cashcraft.tc.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class SpawnerRateModifier {
	/*
	 * 4:30am - 5.30am - t/2m 5:30am - 7am - 1/1m 7am - 9.30am - t/30s 9:30am -
	 * 2:30pm - t/1m 2:30pm - 8:30pm - 1/30s 8:30pm - 11pm - t/1m 11pm - 1am - t/2m
	 */
	static LocalDateTime n = LocalDateTime.now();
	static long open = 0;
	static long m1 = 0;
	static long m2 = 0;
	static long m3 = 0;
	static long m4 = 0;
	static long m5 = 0;
	static long m6 = 0;
	static long close = 0;
	static long extra = 0;
	public static void init() {
		try {
			int dc = 0;
			if(n.getHour() <= 4) {
				dc = -1;
			}
			open = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
					.parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + dc) + " 04:30:00")).getTime();
			m1 = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
					.parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + dc) + " 06:00:00")).getTime();
			m2 = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
					.parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + dc) + " 07:00:00")).getTime();
			m3 = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
					.parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + dc) + " 09:30:00")).getTime();
			m4 = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
					.parse(n.getYear() + "/" + n.getMonthValue() + "/" +(n.getDayOfMonth() + dc) + " 15:00:00")).getTime();
			m5 = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
					.parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + dc) + " 20:30:00")).getTime();
			m6 = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
					.parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + dc) + " 23:00:00")).getTime();
			close = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
					.parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + 1 + dc) + " 01:00:00"))
					.getTime();
			extra = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
					.parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + 1 + dc) + " 03:55:00"))
					.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public enum SpawnRateMod {
		EARLY_NIGHT(open, m1, 0.250000),
		EARLY_MORNING(m1, m2, 0.50000), 
		MORNING(m2, m3, 1.000000), 
		NOON(m3, m4, 0.5), 
		AFTERNOON(m4, m5, 1.000000),
		EVENING(m5, m6, 0.5), 
		LATE_NIGHT(m6, close, 1.00),
		EXTRA(close, extra, 1.00),
		DEFAULT(-1, -1, 0.000000);
		public long _start = 0;
		public long _end = 0;
		public double modifier = 0;

		private SpawnRateMod(long start, long end, double mod) {
			this._end = end - 1;
			this._start = start;
			this.modifier = mod;
		}
	}

	public static SpawnRateMod getMod(long d) {
		SpawnRateMod a = SpawnRateMod.DEFAULT;
		for (SpawnRateMod SpawnMod : SpawnRateMod.values()) { 
			if (SpawnMod._start <= d && SpawnMod._end >= d) {
				a = SpawnMod;
			}
		}
		return a;
	}
}
