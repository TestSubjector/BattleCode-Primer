package revisedstrat;

import battlecode.common.*;

public class BulletCollector extends RobotLogic {

	Direction moveDir;
	CombatUnitLogic combatMode;

	public BulletCollector(RobotController rc) {
		super(rc);
		combatMode = new CombatUnitLogic(rc);
		moveDir = Utils.randomDirection();
	}

	@Override
	public void run() {
		while (true) {
			try {
				TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
				MapLocation goodTreeLocation = this.getLocationOfClosestTreeWithBullets(nearbyTrees);
				if (goodTreeLocation != null) {
					Direction toMove = rc.getLocation().directionTo(goodTreeLocation);
					toMove = getDirectionTowards(toMove);
					move(toMove);

					dodgeBullets();
					RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, enemyTeam);
					if (enemyRobots.length > 0) {
						BroadcastManager.saveLocation(rc, enemyRobots[0].location,
								BroadcastManager.LocationInfoType.ENEMY);
					}
				} else if(rc.getRoundNum() > 500) {
					System.out.println("MOving with a random bounce");
					CombatUnitLogic logic = new CombatUnitLogic(rc);
					logic.run();
				} else {
					dodgeBullets();
					RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, enemyTeam);
					if (enemyRobots.length > 0) {
						BroadcastManager.saveLocation(rc, enemyRobots[0].location,
								BroadcastManager.LocationInfoType.ENEMY);
					}
					moveDir = this.moveWithRandomBounce(moveDir);
				}

				senseAllyEnemyPairs();

				endTurn();

				// move();

				// endTurn();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public Team getFirstHitTeamAprox(MapLocation location, Direction direction, boolean hitTrees)
			throws GameActionException {
		MapLocation testLocation = location;
		while (rc.canSenseLocation(testLocation)) {
			if (rc.isLocationOccupied(testLocation)) {
				TreeInfo targetTree = rc.senseTreeAtLocation(testLocation);
				if (targetTree != null) {
					if (hitTrees) {
						return targetTree.getTeam();
					} else {
						return Team.NEUTRAL;
					}
				}
				RobotInfo targetRobot = rc.senseRobotAtLocation(testLocation);
				if (targetRobot != null) {
					return targetRobot.team;
				} else {
					// System.out.println("This should never happen");
					return Team.NEUTRAL;
				}
			} else {
				float DELTA_BULLET_DISTANCE = .5f;
				testLocation = testLocation.add(direction, DELTA_BULLET_DISTANCE);
			}
		}
		return Team.NEUTRAL;
	}

	private void senseAllyEnemyPairs() throws Exception {
		RobotInfo[] allies = rc.senseNearbyRobots(-1, this.allyTeam);
		RobotInfo[] foes = rc.senseNearbyRobots(-1, this.enemyTeam);

		for (RobotInfo ally : allies) {
			if (ally.type == RobotType.SOLDIER || ally.type == RobotType.TANK) {
				for (RobotInfo foe : foes) {
					MapLocation bulletSpawnPoint = ally.location.add(ally.location.directionTo(foe.location),
							ally.getType().bodyRadius + GameConstants.BULLET_SPAWN_OFFSET);
					if (this.getFirstHitTeamAprox(bulletSpawnPoint, ally.getLocation().directionTo(foe.location),
							false) == enemyTeam) {
						BroadcastManager.hashLocationToFire(rc, ally.ID, foe.location);
						// rc.setIndicatorLine(ally.location, foe.location, 0,
						// 255, 0);
					} else {
						// rc.setIndicatorLine(ally.location, foe.location, 255,
						// 0, 0);
					}
				}
			}
		}
	}

	public Direction getDirectionTowards(Direction toMove) throws GameActionException {
		if (smartCanMove(toMove)) {
			return toMove;
		} else {
			BulletInfo[] bullets = rc.senseNearbyBullets();
			for (int deltaAngle = 0; deltaAngle < 180; deltaAngle += 5) {
				Direction leftDir = toMove.rotateLeftDegrees(deltaAngle);
				if (smartCanMove(leftDir)
						&& !willGetHitByABullet(rc.getLocation().add(leftDir, type.strideRadius), bullets)) {
					return leftDir;
				}
				Direction rightDir = toMove.rotateRightDegrees(deltaAngle);
				if (smartCanMove(rightDir)
						&& !willGetHitByABullet(rc.getLocation().add(rightDir, type.strideRadius), bullets)) {
					return rightDir;
				}

			}
		}
		return null;
	}

	private void move() throws GameActionException {
		boolean hasMoved = dodgeBullets();
		if (!hasMoved) {
			float[] currentForce = new float[2];
			TreeInfo[] trees = rc.senseNearbyTrees();
			MapLocation treeWithBullets = getLocationOfClosestTreeWithBullets(trees);
			float distance = rc.getLocation().distanceSquaredTo(treeWithBullets);
			currentForce[0] = (treeWithBullets.x - rc.getLocation().x) / distance;
			currentForce[1] = (treeWithBullets.y - rc.getLocation().y) / distance;

			currentForce[0] *= 5;
			currentForce[1] *= 5;

			RobotInfo[] foes = rc.senseNearbyRobots(-1, enemyTeam);
			Direction fromFoes = this.getDirectionAway(foes);
			currentForce[0] += Math.cos(fromFoes.radians);
			currentForce[1] += Math.sin(fromFoes.radians);

			RobotInfo[] allys = rc.senseNearbyRobots(-1, allyTeam);
			Direction toAllys = getDirectionAway(allys).opposite();
			currentForce[0] += Math.cos(toAllys.radians);
			currentForce[1] += Math.sin(toAllys.radians);

			Direction toMove = new Direction((float) Math.atan2(currentForce[0], currentForce[1]));

			toMove = getDirectionTowards(toMove);

			move(toMove);
		}
	}

	private MapLocation getLocationOfClosestTreeWithBullets(TreeInfo[] nearbyTrees) throws GameActionException {
		double value = 0;
		TreeInfo toReturn = null;
		for (TreeInfo t : nearbyTrees) {
			double currentValue = t.containedBullets / rc.getLocation().distanceTo(t.location);
			if (currentValue > value) {
				value = currentValue;
				toReturn = t;
			}
		}
		if (toReturn == null) {
			System.out.println("No target found");
			return null;
		} else {
			rc.setIndicatorDot(toReturn.location, 37, 14, 200);
		}
		return toReturn.location;
	}

}
