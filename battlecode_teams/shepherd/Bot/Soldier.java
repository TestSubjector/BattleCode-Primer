package shepherd.Bot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import shepherd.Bot.Behaviour.Behaviour;
import shepherd.Bot.Behaviour.Soldier.IdleSoldierBehaviour;

public class Soldier extends Bot {

	public Soldier(RobotController rc) {
		controller = rc;
	}

	public Behaviour getBehaviour() throws GameActionException {
		if(behaviour == null) return new IdleSoldierBehaviour();
		if(behaviour instanceof IdleSoldierBehaviour) return behaviour;
		return new IdleSoldierBehaviour();
	}
}
