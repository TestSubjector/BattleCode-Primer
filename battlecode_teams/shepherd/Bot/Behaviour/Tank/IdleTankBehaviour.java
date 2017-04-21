package shepherd.Bot.Behaviour.Tank;

import battlecode.common.Clock;
import battlecode.common.GameActionException;

public class IdleTankBehaviour extends TankBehaviour {

	public void execute() throws GameActionException {
		Clock.yield();
	}

}
