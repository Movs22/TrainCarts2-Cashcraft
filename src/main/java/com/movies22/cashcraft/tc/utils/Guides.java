package com.movies22.cashcraft.tc.utils;

public enum Guides {
	GUIDE_LINK("https://docs.google.com/presentation/d/1YuTBuEPojSdSAFVdNvl7G4YqXqwxv4RH3vHVNdo9bpc/present?slide="),
	BLOCKER_SIGN("id.g247af2f7e7f_0_202"),
	BUFFER_SIGN("id.g247af2f7e7f_0_238"),
	DESTROY_SIGN("id.g252b072ab7e_1_0"),
	PLATFORM_SIGN("id.g247af2f7e7f_0_196"),
	SPEED_SIGN("id.g247cb217c79_0_0"),
	STOP_SIGN("id.g247af2f7e7f_0_246"),
	SWITCHER_SIGN("id.g247af2f7e7f_0_232");
	public final String id;
	private Guides(String id) {
		this.id = id;
	}
}
