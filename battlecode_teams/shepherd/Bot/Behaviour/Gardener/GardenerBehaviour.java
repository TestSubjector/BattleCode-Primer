package shepherd.Bot.Behaviour.Gardener;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import shepherd.Bot.Behaviour.Behaviour;

public abstract class GardenerBehaviour extends Behaviour {

	public boolean build(RobotType type, Direction dir, int rotateDegrees) throws GameActionException {
		RobotController gardener = this.executer.getController();
		for(int i = 0; i <= 180; i += rotateDegrees) {
			if(gardener.canBuildRobot(type, dir.rotateLeftDegrees(i))) { gardener.buildRobot(type, dir.rotateLeftDegrees(i)); return true; }
			if(gardener.canBuildRobot(type, dir.rotateRightDegrees(i))) { gardener.buildRobot(type, dir.rotateRightDegrees(i)); return true; }
		}
		return false;
	}

	public boolean plantTree(Direction dir, int rotateDegrees) throws GameActionException {
		RobotController gardener = this.executer.getController();
		for(int i = 0; i <= 180; i += rotateDegrees) {
			if(gardener.canPlantTree(dir.rotateLeftDegrees(i))) { gardener.plantTree(dir.rotateLeftDegrees(i)); return true; }
			if(gardener.canPlantTree(dir.rotateRightDegrees(i))) { gardener.plantTree(dir.rotateRightDegrees(i)); return true; }
		}
		return false;
	}

}
