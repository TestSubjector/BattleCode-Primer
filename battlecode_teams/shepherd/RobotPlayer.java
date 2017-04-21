package shepherd;


import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import shepherd.Bot.*;


public class RobotPlayer {

	public static void run(RobotController rc) throws GameActionException {
		switch(rc.getType()) {
			case ARCHON:		(new Archon(rc)).run();  	break;
			case GARDENER:		(new Gardener(rc)).run();  	break;
			case SCOUT:			(new Scout(rc)).run();  	break;
			case SOLDIER:		(new Soldier(rc)).run();  	break;
			case TANK:			(new Tank(rc)).run();  		break;
			case LUMBERJACK:	(new Lumberjack(rc)).run(); break;
		}
	}

}
