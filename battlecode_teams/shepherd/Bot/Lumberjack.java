package shepherd.Bot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import shepherd.Bot.Behaviour.Behaviour;
import shepherd.Bot.Behaviour.Lumberjack.IdleLumberjackBehaviour;

public class Lumberjack extends Bot {

	public Lumberjack(RobotController rc) {
		controller = rc;
	}

	public Behaviour getBehaviour() throws GameActionException {
		if(behaviour == null) return new IdleLumberjackBehaviour();
		if(behaviour instanceof IdleLumberjackBehaviour) return behaviour;
		return new IdleLumberjackBehaviour();
	}

}
