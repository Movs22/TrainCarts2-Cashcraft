package com.movies22.cashcraft.tc.utils;

import com.movies22.cashcraft.tc.api.MetroLines.MetroLine;

public class StationAnnouncements {
	
	
	public static String convertColor(String name) {
		name = name.split("/")[0];
		if (name.contains("$CHR"))
			return "#C55A11";
		if (name.contains("$GAR"))
			return "#1BF400";
		if (name.contains("$HS1"))
			return "#00946F";
		if (name.contains("$HS2"))
			return "#00DFFF";
		if (name.contains("$SVR"))
			return "#B955FF";
		if(name.contains("$Purple"))
			return "#54009f";
		if (name.contains("$Blue"))
			return "#387eff";
		if (name.contains("$Green"))
			return "#22D74C";
		if (name.contains("$Orange"))
			return "#FF7F27";
		if (name.contains("$Pink"))
			return "#FFAEC9";
		if (name.contains("$Red"))
			return "#ED1C24";
		if (name.contains("$Yellow"))
			return "#FCE600";
		if (name.contains("$NAT"))
			return "#0075B4";
		if (name.contains("$FTrams"))
			return "#BFAF81";
		if(name.contains("$Cyan"))
			return "#86c4bf";
		if(name.contains("$Beige"))
			return "#fce6a7";
		if(name.contains("$Grey"))
			return "#AAAAAA";
		if(name.contains("$Airport"))
			return "#D17059";
		if(name.startsWith("$")) 
			return name.substring(1);
		return name;
	}
	
	public static String parseMetro(String string, MetroLine l) {
		return parseMetro(string, l, false);
	}
	
	public static String parseMetro(String string, MetroLine l, Boolean z) {
		Boolean shuttle = false;
		if(string.contains("S")) {
			shuttle = true;
			string.replace("S", "");
		}
		String color = l.getColour();
		String result = "";
		if(z) {
			result = "[{\"text\":\"Change for the \",\"color\":\"" + convertColor(color) + "\"}";
		} else {
			result = "[{\"text\":\"Change here for the \",\"color\":\"" + convertColor(color) + "\"}";
		}
		String a = ", ";
		int b = 0;
		if(string.equalsIgnoreCase("-")) return null;
		if (string.equalsIgnoreCase("")) return null;
		if (string == "") return null;
		string = string.replaceAll(l.getChar(), "");
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == 'B' && !l.getName().equals("Blue")) {
				result = result + (", {\"text\":\"Blue\",\"color\":\"" + convertColor("$Blue") + "\"}");
			} else if (string.charAt(i) == 'G' && !l.getName().equals("Green")) {
				result = result + (", {\"text\":\"Green\",\"color\":\"" + convertColor("$Green") + "\"}");
			} else if (string.charAt(i) == 'O' && !l.getName().equals("Orange")) {
				result = result + (", {\"text\":\"Orange\",\"color\":\"" + convertColor("$Orange") + "\"}");
			} else if (string.charAt(i) == 'P' && !l.getName().equals("Pink")) {
				result = result + (", {\"text\":\"Pink\",\"color\":\"" + convertColor("$Pink") + "\"}");
			} else if (string.charAt(i) == 'U' && !l.getName().equals("Purple")) {
				result = result + (", {\"text\":\"Purple\",\"color\":\"" + convertColor("$Purple") + "\"}");
			} else if (string.charAt(i) == 'C' && !l.getName().equals("Cyan")) {
				result = result + (", {\"text\":\"Cyan\",\"color\":\"" + convertColor("$Cyan") + "\"}");
			} else if (string.charAt(i) == 'R' && !l.getName().equals("Red")) {
				result = result + (", {\"text\":\"Red\",\"color\":\"" + convertColor("$Red") + "\"}");
			} else if (string.charAt(i) == 'Y' && !l.getName().equals("Yellow")) {
				result = result + (", {\"text\":\"Yellow\",\"color\":\"" + convertColor("$Yellow") + "\"}");
			} else if (string.charAt(i) == 'E' && !l.getName().equals("Grey")) {
				result = result + (", {\"text\":\"Grey\",\"color\":\"" + convertColor("$Grey") + "\"}");
			} else {
				if (b == string.length()) {
						if(!z) {
							a = " Line" + (string.length() > 2 ? "s" : "") + ".";
							result = result + (", {\"text\":\"" + a + "\",\"color\":\"" + convertColor(color) + "\"}");
						} else {
							a = " Line" + (string.length() > 2 ? "s" : "") + " services at {STATION}.";
							result = result + (", {\"text\":\"" + a + "\",\"color\":\"" + convertColor(color) + "\"}");
						}
				}
				b += 1;
				continue;
			}
			if (b == string.length() - 1)
				a = " Line" + (string.length() > 1 ? "s" : "") + ".";
			if (b == string.length() - 2)
				a = " and ";
			if (b < string.length() - 2)
				a = ", ";
			result = result + (", {\"text\":\"" + a + "\",\"color\":\"" + convertColor(color) + "\"}");
			if (b == string.length() - 1 && !z) {
				if(shuttle) {
					result = result + (", {\"text\":\" and \",\"color\":\"" + convertColor(color) + "\"}");
					result = result + (", {\"text\":\"Airport Shuttle\",\"color\":\"" + convertColor("$Airport") + "\"}");
					result = result + (", {\"text\":\" services.\",\"color\":\"" + convertColor(color) + "\"}");
				}
				result = result + "]";
			}
			b += 1;
		}
		return result;
	}

	public static String parseRail(String string, MetroLine l, Boolean m) {
		return parseRail(string, l, m, false);
	}
	
	public static String parseRail(String string, MetroLine l, Boolean m, Boolean z) {
		String color = l.getColour();
		String result;
		if(m && !z) {
			result = "[{\"text\":\"Also change for \",\"color\":\"" + convertColor(color) + "\"}";
		} else if(!z) {
			result = "[{\"text\":\"Change here for \",\"color\":\"" + convertColor(color) + "\"}";
		} else if(m) {
			result = "{\"text\":\"and \",\"color\":\"" + convertColor(color) + "\"}";
		} else {
			result = "[{\"text\":\"Also change for \",\"color\":\"" + convertColor(color) + "\"}";
		}
		String a = ", ";
		int b = 0;
		if(string.equalsIgnoreCase("-")) return null;
		if (string.equalsIgnoreCase("")) return null;
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == '1') {
				result = result + ", {\"text\":\"HS1\",\"color\":\"" + convertColor("$HS1") + "\"}";
			} else if (string.charAt(i) == '2') {
				result = result + (", {\"text\":\"HS2\",\"color\":\"" + convertColor("$HS2") + "\"}");
			} else if (string.charAt(i) == 'S') {
				result = result + (", {\"text\":\"South Valley Railway\",\"color\":\"" + convertColor("$SVR") + "\"}");
			} else if (string.charAt(i) == 'G') {
				result = result + (", {\"text\":\"Greater Arbridge Railway\",\"color\":\"" + convertColor("$GAR") + "\"}");
			} else if (string.charAt(i) == 'A') {
				result = result
						+ (", {\"text\":\"Ailsbury Connect\",\"color\":\"" + convertColor("$CHR") + "\"}");
			}
			if (b == string.length() - 1) {
				if(!z) {
					a = " services.";
					result = result + (", {\"text\":\"" + a + "\",\"color\":\"" + convertColor(color) + "\"}");
					result = result + "]";
				} else {
					a = " services at {STATION}.";
					result = result + (", {\"text\":\"" + a + "\",\"color\":\"" + convertColor(color) + "\"}");
					result = result + "]";
				}
				break;
		}
			if(b == string.length() - 2) {
				a = " and ";
			}
			if (b < string.length() - 2)
				a = ", ";
			result = result + (", {\"text\":\"" + a + "\",\"color\":\"" + convertColor(color) + "\"}");
			b += 1;
		}
		return result;
	}
}

