package com.movies22.cashcraft.tc.commands;

import com.movies22.cashcraft.tc.TrainCarts;

public class CommandLoader {
	public static void _init() {
		new DepotCommand(TrainCarts.plugin);
		new StationCommand(TrainCarts.plugin);
		new TC2Command(TrainCarts.plugin);
		new RouteCommand(TrainCarts.plugin);
		new LineCommand(TrainCarts.plugin);
		new TrainCommand(TrainCarts.plugin);
	}
}
