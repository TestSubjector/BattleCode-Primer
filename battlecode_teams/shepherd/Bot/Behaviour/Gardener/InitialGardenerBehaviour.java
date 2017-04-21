package shepherd.Bot.Behaviour.Gardener;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.TreeInfo;

public class InitialGardenerBehaviour extends GardenerBehaviour{

	RobotController gardener;

	public void execute() throws GameActionException {
		if(gardener == null) gardener = executer.getController(); // easier access to this robots controller

		buildFirstScout();	// start with building a scout as very first action in the game

		buildInitialFarm(); // start making a first farm of some trees

		waterBulletTrees(); // finds and waters tree that needs it

		donate(); // randomly donate some bullets to win at least some matches

		Clock.yield();
	}

	public void buildFirstScout() throws GameActionException {
		if(gardener.getRoundNum() > 2) return;
		Direction preferredDirection = getPreferredDirection();
		int rotateDegrees = 5;
		build(RobotType.SCOUT, preferredDirection, rotateDegrees);
	}

	public void buildInitialFarm() throws GameActionException {
		// TODO
		// 1.: Find location where
		//		a) some trees can be built close to one another
		//		b) a gap can be left for building new robots
		// until that is implemented, just build trees directly next to your spawning point...
		Direction preferredDirection = getPreferredDirection();
		int rotateDegrees = 5;
		plantTree(preferredDirection, rotateDegrees);
	}

	public void waterBulletTrees() throws GameActionException {
		// TODO
		TreeInfo[] nearbyBulletTrees = gardener.senseNearbyTrees(gardener.getType().strideRadius + 0.1f, gardener.getTeam());
		TreeInfo lowestHealthTree = null;
		float lowestHP = Float.MAX_VALUE;
		for(TreeInfo tree : nearbyBulletTrees) {
			if(tree.health < lowestHP) {
				lowestHealthTree = tree;
				lowestHP = tree.health;
			}
		}
		if(lowestHealthTree != null) {
			if(gardener.canWater(lowestHealthTree.location)) gardener.water(lowestHealthTree.location);
		}
	}

	public Direction getPreferredDirection() throws GameActionException {
		// TODO
		return Direction.getEast();
	}


	public void donate() throws GameActionException {
		if(gardener.getTreeCount() >= 3 && gardener.getTeamBullets() >= 10 + GameConstants.BULLET_TREE_COST)
			gardener.donate((int)((gardener.getTeamBullets() - GameConstants.BULLET_TREE_COST) / 10) * 10);
	}

}
