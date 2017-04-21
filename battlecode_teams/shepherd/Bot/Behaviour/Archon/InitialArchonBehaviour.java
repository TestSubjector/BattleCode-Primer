package shepherd.Bot.Behaviour.Archon;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;

/**
 * initially, archons should always start by building a gardener,
 * shake nearby trees, and wait for further instructions.
 *
 */

public class InitialArchonBehaviour extends ArchonBehaviour {

	RobotController archon;

	/*
	 *	returns true if a gardener should be hired.
	 *	initially should hire exactly one gardener.
	 *	should hire another gardener if another one is needed -> TODO
	 */
	public boolean shouldHireGardener() throws GameActionException {
		// return true if no gardener has been hired yet
		if(archon.getRoundNum() <= 1 && archon.getTeamBullets() >= GameConstants.BULLETS_INITIAL_AMOUNT) return true;

		// return true if amount of gardeners is less than a certain threshhold value
		int amountOfGardeners = 1;// TODO : implement get amount of <type> function
		int threshHoldGardeners = 1; // TODO : calculate threshhold for min. amount of gardeners
		return amountOfGardeners < threshHoldGardeners;
	}

	// hires a gardener roughly in the direction of the enemy
	public boolean hireGardener() throws GameActionException {
		Direction preferredDirection = Direction.getEast();// TODO: get preferred building direction
		return hireGardener(preferredDirection);
	}

	public void execute() throws GameActionException {
		if(archon == null) archon = executer.getController();
		if(shouldHireGardener()) hireGardener();
		executer.headlessChicken();
		Clock.yield();
	}

}
