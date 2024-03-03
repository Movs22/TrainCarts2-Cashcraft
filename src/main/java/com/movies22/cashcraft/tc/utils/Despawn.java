package com.movies22.cashcraft.tc.utils;

public enum Despawn {
	INVALID_SECTION("despawn.invalid_section","Invalid section allocation"),
	INVALID_ROUTE("despawn.invalid_route","Invalid route detected. Route calculation timed out after {X} connections."),
	UNKNOWN_PARENT("despawn.unknown_parent",null),
	INVALID_NODE("despawn.invalid_node","Node {POS} is not a valid spawn location"),
	INVALID_HEADING("despawn.invalid_heading","DESPAWN.INVALID_HEADING: {X} | {Z}"),
	MISSING_CARTS("despawn.missing_carts","A missing cart was detected at position #{POS}"),
	EXCEEDED_REROUTING_ATTEMPTS("despawn.exceeded_rerouting_attempts", "Exceeded the maximum amount of rerouting attempts"),
	ILLEGAL_SPEED("despawn.illegal_speed", "Exceeded the maximum speed. Cart #{POS} had a speed modifier of x{MOD}"),
	BUFFERS_EATEN("despawn.buffers_eaten", "Buffers don't like to be eaten"),
	INVALID_TRAIN("despawn.invalid_train","This train is missing carts."),
	UNKNOWN("despawn.unknown","An unknown error has occured");
	public final String name;
	public final String label;
	private Despawn(String n, String l) {
		if(l == null) {
			l = n.toUpperCase();
		}
		this.name = n;
		this.label = l;
	}
}



