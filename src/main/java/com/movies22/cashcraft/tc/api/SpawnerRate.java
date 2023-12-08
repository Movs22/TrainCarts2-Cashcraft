package com.movies22.cashcraft.tc.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.movies22.cashcraft.tc.utils.Date;
import com.movies22.cashcraft.tc.utils.SpawnerRateModifier;
import com.movies22.cashcraft.tc.utils.SpawnerRateModifier.SpawnRateMod;

public class SpawnerRate {

	public static String convert(long seconds) {
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		long h = (seconds / (60 * 60)) % 24;
		return String.format("%d:%02d:%02d", h, m, s);
	}

	public List<Date> spawnTimes = new ArrayList<Date>();
	public List<Integer> trainLength = new ArrayList<Integer>();
	public long offset = -1;
	public long rate = -1;
	public String route;
	public String length;
	public SpawnerRate(long offset, long rate, String route, String length) {
		String[] lengths = length.split("/");
		this.length = length;
		// HIGH FREQ/MED FREQ/LOW FREQ
		this.offset = offset;
		this.rate = rate;
		this.route = route;
		if (rate > 0) {
			ZonedDateTime n = LocalDateTime.now().atZone(ZoneId.of("Europe/Paris"));
			long n2 = n.toInstant().toEpochMilli() - 60*60*1000;
			long open = 0;
			long closure = 0;
			int dc = 0;
			if(n.getHour() <= 4) {
				dc = -1;
			}
			try {
				open = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
						.parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + dc) + " 04:30:00"))
						.getTime();
				closure = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
						.parse(n.getYear() + "/" + n.getMonthValue() + "/" + (n.getDayOfMonth() + 1 + dc) + " 03:55:00"))
						.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			SpawnRateMod mod;
			SpawnerRateModifier.init();
			long a = open;
			while (a < closure) {
				mod = SpawnerRateModifier.getMod(a);
				if (mod.modifier != 0.0) {
					if (a > (n2 + 5000)) {
						if(mod.modifier == 1.0) {
							this.trainLength.add(Integer.parseInt(lengths[0]));
						} else if(mod.modifier == 0.5) {
							this.trainLength.add(Integer.parseInt(lengths[1]));
						} else if(mod.modifier == 0.25) {
							this.trainLength.add(Integer.parseInt(lengths[2]));
						} else {
							a = Long.MAX_VALUE;
						}
						this.spawnTimes.add(new Date(a + Math.round(offset / (mod.modifier*1000)*2000))   );
					}
					a += Math.round(rate / (mod.modifier*1000))*2000;
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
		//why is this removing 1 hour from gmt+1 when it could just be gmt+0
		long n = LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toInstant().toEpochMilli() - 60*60*1000;
		Date a = null;
		int i = 0;
		while (a == null && i < this.spawnTimes.size()) {
			a = this.spawnTimes.get(i);
			if (a.isFuture(n - 1000L)) {
				this.trainLength.remove(this.spawnTimes.indexOf(a));
				this.spawnTimes.remove(a);
				a = null;
			} else {
				break;
			}
		}
		if(this.spawnTimes.size() < (i+z+1)) {
			return null;
		}
		return this.spawnTimes.get(i+z);
	}
	
	public int getNextTrain() {
		return this.trainLength.get(0);
	}
}
