package shepherd.Bot.Behaviour.Archon;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import shepherd.Bot.Behaviour.Behaviour;

public abstract class ArchonBehaviour extends Behaviour {

	public abstract boolean shouldHireGardener() throws GameActionException;
	public abstract boolean hireGardener() throws GameActionException;

	public boolean hireGardener(Direction dir) throws GameActionException {
		RobotController archon = this.executer.getController();
		for(int i = 0; i <= 180; i += 1) {
			if(archon.canHireGardener(dir.rotateLeftDegrees(i))) { archon.hireGardener(dir.rotateLeftDegrees(i)); return true; }
			if(archon.canHireGardener(dir.rotateRightDegrees(i))) { archon.hireGardener(dir.rotateRightDegrees(i)); return true; }
		}
		return false;
	}


}
