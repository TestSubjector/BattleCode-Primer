package p_patrick.robots;

import battlecode.common.*;
import ddframework.robots.SmartBaseRobot;
import ddframework.util.RandomUtil;
import java.util.Random;

public class FarmerGardenerRobot extends SmartBaseRobot {

	private enum GardenerStates {
		FINDING_OPTIMAL_FARM_LOCATION,
		FINDING_OPTIMAL_FARM_LOCATION_RAND,
		FINDING_OPTIMAL_FARM_LOCATION_2,
		REVERSING_FROM_IMPACT,
		FARMING
	}
	private static final int RAND_TREE_EXPLORE = 3;
	private static final int MAX_ROUNDS_IN_SEARCH = 30;
	private static final int NUM_REVERSE_FROM_IMPACT_MOVES = 5;
	private static final int MAX_TREES_IN_RING = 5;
	private static final float TREE_RING_SEARCH_INCREMENT_RAD = (float) ((2d * Math.PI) / 12);

	private final Team mMyTeam;
	private static MapLocation mMyLocation;
	private Direction mExploreDir;
	private Direction keepClearDir;
	private int mRandExploresCount;
	private int mReversesCount;
	private int mRoundsInSearch;
	private GardenerStates mCurrentState = GardenerStates.FINDING_OPTIMAL_FARM_LOCATION;

	private static boolean haveSpawnedInitialScout;
	private static boolean haveSpawnedInitialLumberjack;
	private static boolean haveSpawnedInitialSoldier;
	private static int treesBuilt = 0;

	private static RobotType[] earlyBuildOrder = new RobotType[] {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER,RobotType.LUMBERJACK};
	private static RobotType[] midBuildOrder = new RobotType[] { RobotType.SOLDIER,RobotType.SOLDIER,RobotType.SCOUT, RobotType.LUMBERJACK, RobotType.SOLDIER};
	//private static RobotType[] lateBuildOrder = new RobotType[] { RobotType.SOLDIER,RobotType.LUMBERJACK,RobotType.TANK}; // TODO: WHEN IS LATE GAME?
	private static int buildOrderIndex = 0;


	public FarmerGardenerRobot(RobotController controller) {
		super(controller);
		mMyTeam = controller.getTeam();
		mMyLocation = controller.getLocation();

		// move directly away from the Archon that spawned us (or whichever one is first in the array...)
		final RobotInfo[] nearbyRobots = controller.senseNearbyRobots();
		for (RobotInfo robot : nearbyRobots) {
			if (robot.getType() == RobotType.ARCHON) {
				mExploreDir = new Direction(robot.getLocation(), mMyLocation);
				break;
			}
		}

		// no Archon nearby, (very improbable) so pick a random direction
		if (mExploreDir == null) {
			Random rand = RandomUtil.getRandom();
			mExploreDir = new Direction(rand.nextInt(3) - 1, rand.nextInt(3) - 1);
		}

	}

	@Override
	protected void onGameRound(RobotController rc) throws Exception {
		mMyLocation = rc.getLocation();
		GardenerStates nextState;

		switch (mCurrentState) {
			case FINDING_OPTIMAL_FARM_LOCATION:
				if (initialUnitsHaveBeenSpawned()) {
					// move in the explore direction until we hit something

					if (!rc.hasMoved() && rc.canMove(mExploreDir)) {
						rc.move(mExploreDir);
						nextState = GardenerStates.FINDING_OPTIMAL_FARM_LOCATION;
					} else {
						nextState = GardenerStates.FINDING_OPTIMAL_FARM_LOCATION_RAND;
						mRandExploresCount = RAND_TREE_EXPLORE;
					}
                    mRoundsInSearch = MAX_ROUNDS_IN_SEARCH;
					break;
				}
			case FINDING_OPTIMAL_FARM_LOCATION_RAND:
				// move randomly in case the thing we hit was also moving
				Direction randDir = RandomUtil.randomDirection();
				if (!rc.hasMoved() && rc.canMove(randDir)) {
					rc.move(randDir);
				}
				mRandExploresCount--;
				if (mRandExploresCount <= 0) {
					nextState = GardenerStates.FINDING_OPTIMAL_FARM_LOCATION_2;
				} else {
					nextState = GardenerStates.FINDING_OPTIMAL_FARM_LOCATION_RAND;
				}
				break;
			case FINDING_OPTIMAL_FARM_LOCATION_2:
				// continue exploring in the explore direction until we hit something again

				if (!rc.hasMoved() && rc.canMove(mExploreDir)) {
					rc.move(mExploreDir);
					nextState = GardenerStates.FINDING_OPTIMAL_FARM_LOCATION_2;
				} else {
					mReversesCount = NUM_REVERSE_FROM_IMPACT_MOVES;
					nextState = GardenerStates.REVERSING_FROM_IMPACT;
				}
				break;
			case REVERSING_FROM_IMPACT:
				// back up a bit to make room for our tree ring and the thing we hit

				Direction opposite = mExploreDir.opposite();
				if (!rc.hasMoved() && rc.canMove(opposite)) {
					rc.move(opposite);
				}
				mReversesCount--;
				if (mReversesCount <= 0) {
					nextState = GardenerStates.FARMING;
                    keepClearDir = opposite;
				} else {
					nextState = GardenerStates.REVERSING_FROM_IMPACT;
				}
				break;
			case FARMING:
                mRoundsInSearch = MAX_ROUNDS_IN_SEARCH;
				tryWaterTrees();
				tryBuildTreeRing();
				nextState = GardenerStates.FARMING;
				break;
			default:
				nextState = GardenerStates.FINDING_OPTIMAL_FARM_LOCATION;
				break;
		}

		mCurrentState = nextState;

		// Always attempt to spawn unites every round.
		spawnUnits();

	}

	private void spawnUnits() throws GameActionException {
		RobotController rc = getRc();

		Integer nearbyDefenseUnits = 0;
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(RobotType.GARDENER.sensorRadius, getTeam());
		for (RobotInfo robot : nearbyRobots) {
			if (robot.getType() == RobotType.SOLDIER || robot.getType() == RobotType.LUMBERJACK || robot.getType() == RobotType.TANK) {
				nearbyDefenseUnits++;
			}
		}
		if (nearbyDefenseUnits < 5) {
			System.out.println("THERE ARE ONLY " + nearbyDefenseUnits + " FRIENDLY DEFENSE UNITS NEARBY. Spawning more");
			if (initialUnitsHaveBeenSpawned() && rc.getTeamBullets() >= 150) {
				System.out.println("INITIAL UNITS HAVE BEEN SPAWNED");
				RobotType[] buildOrder = rc.getTreeCount() < 8 && rc.getRoundNum() < 200 ? earlyBuildOrder : midBuildOrder;
				RobotType nextUnitToSpawn = buildOrder[buildOrderIndex % buildOrder.length];
				System.out.println("SPAWNING " + nextUnitToSpawn.name());
				boolean spawned = attemptSpawnRobotType(nextUnitToSpawn, RandomUtil.randomDirection());
				if (spawned) {
					buildOrderIndex++;
					if (buildOrderIndex >= buildOrder.length) {
						buildOrderIndex = 0;
					}
				}
			} else {
				System.out.println("INITIAL UNITS HAVE NOT SPAWNED YET");
			}
		} else {
			if (rc.getTeamBullets() > 250 && rc.getRoundLimit() - rc.getRoundNum() < 500) {
				System.out.println("DONATING 100 bullets Because we have lots of defense and extra bullets");
				rc.donate(100);
			}
		}

	}

	private boolean initialUnitsHaveBeenSpawned() {
		if (!haveSpawnedInitialScout) {
			haveSpawnedInitialScout = attemptSpawnRobotType(RobotType.SCOUT, RandomUtil.randomDirection());
            System.out.println("SPAWNING INITIAL SCOUT:" + haveSpawnedInitialScout);
			return false;
		}

		TreeInfo[] nearbyNeutralTrees = getRc().senseNearbyTrees(RobotType.GARDENER.sensorRadius, Team.NEUTRAL);
        System.out.println("THERE ARE " + (nearbyNeutralTrees != null ? nearbyNeutralTrees.length : 0) + " TREES NEARBY");
		if (!haveSpawnedInitialLumberjack && nearbyNeutralTrees != null && nearbyNeutralTrees.length > 3) {
			haveSpawnedInitialLumberjack = attemptSpawnRobotType(RobotType.LUMBERJACK, RandomUtil.randomDirection());
            System.out.println("SPAWNING INITIAL LUMBERJACK:" + haveSpawnedInitialLumberjack);
			return false;
		} else {
            haveSpawnedInitialLumberjack = true;
        }

		if (!haveSpawnedInitialSoldier) {

			haveSpawnedInitialSoldier = attemptSpawnRobotType(RobotType.SOLDIER, RandomUtil.randomDirection());
            System.out.println("SPAWNING INITIAL SOLDIER:" + haveSpawnedInitialSoldier);
			return false;
		}

		return true;
	}

	private boolean attemptSpawnRobotType(RobotType type, Direction dir) {
		RobotController rc = getRc();
		try {
			if (!rc.hasRobotBuildRequirements(type)) {
				return false;
			}
			// attempt to build the robot by rotating in X degree increments until able to build.
			for (int i = 0; i < 12; ++i) {
				// can I build at this degree?
				if (rc.canBuildRobot(type, dir)) {
					rc.buildRobot(type, dir);
					return true;
				}
				// do the rotate if can't build here
				dir = dir.rotateRightDegrees(30);
			}
			return false;
		} catch (GameActionException e) {
			System.out.println("Exception in FarmerGardnerRobot.attemptSpawnRobotType: " + e);
			return false;
		}
	}

	private void tryWaterTrees() {
		RobotController rc = getRc();
		try {
			TreeInfo[] treeInfos = rc.senseNearbyTrees();
			RandomUtil.shuffle(treeInfos);
			for (TreeInfo tree : treeInfos) {
				if (tree.team.equals(mMyTeam)) {
					float health = tree.getHealth();
					float maxHealth = tree.getMaxHealth();
					if (health / maxHealth < 0.7f) {
						int treeId = tree.getID();
						if (rc.canWater(treeId)) {
							rc.water(treeId);
							break;
						}
					}
				}
			}
		} catch (GameActionException e) {
			System.out.println("Exception in FarmerGardenerRobot.waterTrees: " + e);
		}
	}

	private void tryBuildTreeRing() {
		RobotController rc = getRc();
		boolean gardenerTooClose = false;
		int gardenerNearCount = 0;
		Direction away = null;

		try {
			RobotInfo[] visibleFriendlies = rc.senseNearbyRobots(RobotType.GARDENER.sensorRadius, rc.getTeam());
			// Check to see if other gardeners are nearby and move away if they are.
			if (treesBuilt == 0 && visibleFriendlies != null && visibleFriendlies.length > 0 && !rc.hasMoved()) {
                System.out.println("There are " + visibleFriendlies.length + " friendlies nearby.");
				for (RobotInfo robot : visibleFriendlies) {
					// Look for other nearby gardeners/archons
					if (robot.getType() == RobotType.GARDENER || robot.getType() == RobotType.ARCHON) {
						// If the gardener/archon is close, mark it and set away direction
						if (robot.location.distanceTo(mMyLocation) < RobotType.GARDENER.bodyRadius * 7) {
							away = new Direction(robot.location, mMyLocation).rotateLeftDegrees(20);
							rc.setIndicatorLine(mMyLocation, robot.location, 255, 0, 0);
							gardenerTooClose = true;
						}
						gardenerNearCount++;
					}
				}

				// Try to leave group of gardeners if many are present
				if (gardenerTooClose) {
					if (gardenerNearCount < 2) {
						System.out.println("There are only " + gardenerNearCount + " nearby. I will stay here.");
						tryMove(away);
					} else {
						System.out.println("There are too many gardeners! " + gardenerNearCount + " nearby. I will leave.");
                        mExploreDir = RandomUtil.randomDirection();
                        tryMove(mExploreDir);
					}
				}
			}

			if (treesBuilt <= MAX_TREES_IN_RING && !gardenerTooClose) {
				// try to fill in the ring
				rc.setIndicatorLine(mMyLocation, mMyLocation.add(keepClearDir, 2f), 0, 255, 0);
				final float startDir = keepClearDir.radians + 1f; //keep room for units
				rc.setIndicatorLine(mMyLocation, mMyLocation.add(startDir, 2f), 50, 50, 0);
				final float endDir = (float) (startDir + (2 * Math.PI)) - 2.1f; //keep room for units
				rc.setIndicatorLine(mMyLocation, mMyLocation.add(endDir, 2f), 0, 50, 50);
				for (float f = startDir; f < endDir; f += TREE_RING_SEARCH_INCREMENT_RAD) {
					Direction treeDir = new Direction(f);
					if (rc.canPlantTree(treeDir)) {
						rc.plantTree(treeDir);
						treesBuilt++;
						break;
					}
				}
			}

		} catch (GameActionException e) {
			System.out.println("Exception in FarmerGardenerRobot.waterTrees: " + e);
		}
	}

}
