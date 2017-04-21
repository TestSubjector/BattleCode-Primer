package shepherd.Bot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import shepherd.Bot.Behaviour.Behaviour;
import shepherd.Bot.Behaviour.Gardener.InitialGardenerBehaviour;

public class Gardener extends Bot {

	public Gardener(RobotController rc) {
		controller = rc;
	}

	public Behaviour getBehaviour() throws GameActionException {
		if(behaviour == null) return new InitialGardenerBehaviour();
		if(behaviour instanceof InitialGardenerBehaviour) return behaviour;
		return new InitialGardenerBehaviour();
	}

}
