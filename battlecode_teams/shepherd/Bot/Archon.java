package shepherd.Bot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import shepherd.Bot.Behaviour.Behaviour;
import shepherd.Bot.Behaviour.Archon.InitialArchonBehaviour;

public class Archon extends Bot {

	public Archon(RobotController rc) {
		controller = rc;
	}

	public Behaviour getBehaviour() throws GameActionException {
		if(behaviour == null) return new InitialArchonBehaviour();
		if(behaviour instanceof InitialArchonBehaviour) return behaviour;
		return new InitialArchonBehaviour();
	}


}
