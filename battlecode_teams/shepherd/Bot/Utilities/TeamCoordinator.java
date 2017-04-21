package shepherd.Bot.Utilities;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class TeamCoordinator {

	public static int getRobotTypeCount(RobotType type, RobotController rc) throws GameActionException {
		// TODO
		return rc.getInitialArchonLocations(rc.getTeam()).length;
	}

}
