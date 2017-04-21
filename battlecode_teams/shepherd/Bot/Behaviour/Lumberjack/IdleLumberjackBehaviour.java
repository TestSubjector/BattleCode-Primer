package shepherd.Bot.Behaviour.Lumberjack;

import battlecode.common.Clock;
import battlecode.common.GameActionException;

public class IdleLumberjackBehaviour extends LumberjackBehaviour{

	public void execute() throws GameActionException {
		Clock.yield();
	}

}
