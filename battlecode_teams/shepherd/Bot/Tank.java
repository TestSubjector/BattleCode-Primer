package shepherd.Bot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import shepherd.Bot.Behaviour.Behaviour;
import shepherd.Bot.Behaviour.Tank.IdleTankBehaviour;

public class Tank extends Bot {

	public Tank(RobotController rc) {
		controller = rc;
	}

	public Behaviour getBehaviour() throws GameActionException {
		if(behaviour == null) return new IdleTankBehaviour();
		if(behaviour instanceof IdleTankBehaviour) return behaviour;
		return new IdleTankBehaviour();
	}

}
