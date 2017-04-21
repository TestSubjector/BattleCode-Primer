package mangelware;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import mangelware.bot.Bot;
import mangelware.bot.archon.Archon;
import mangelware.bot.gardener.Gardener;
import mangelware.bot.lumberjack.Lumberjack;
import mangelware.bot.scout.Scout;
import mangelware.bot.soldier.Soldier;
import mangelware.bot.tank.Tank;


public class RobotPlayer {

	public static void run(RobotController rc) throws GameActionException {
		Bot.initialize(rc);
		switch(rc.getType()) {
		case ARCHON: Archon.run(); break;

		case GARDENER: Gardener.run(); break;

		case LUMBERJACK: Lumberjack.run(); break;

		case SCOUT: Scout.run(); break;

		case SOLDIER: Soldier.run(); break;

		case TANK: Tank.run(); break;
		}
	}

}
