package com.movies22.cashcraft.tc.flags;

public enum Flags {
	CASH("CASH", new CashFlag());
	public String name;
	public BaseFlag flag;
	private Flags(String n, BaseFlag f) {
		this.name = n;
		this.flag = f;
	}
}
