package shepherd.Bot.Behaviour.Soldier;

import battlecode.common.Clock;
import battlecode.common.GameActionException;

public class IdleSoldierBehaviour extends SoldierBehaviour {

	public void execute() throws GameActionException {
		Clock.yield();
	}

}
