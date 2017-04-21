package shepherd.Bot.Behaviour.Scout;

import java.util.ArrayList;

import battlecode.common.BodyInfo;
import battlecode.common.BulletInfo;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TreeInfo;
import shepherd.Bot.Utilities.Geometry;
import shepherd.Bot.Utilities.Util;


/*
 * Goal: Find and kill as many hostile gardeners as possible, while trying to stay alive
 * (and gathering and broadcasting information about opponents units, maybe)
 */

public class DisruptEconBehaviour extends ScoutBehaviour {

	ArrayList<Integer> turnUpdatedArchon = new ArrayList<Integer>(10);
	ArrayList<Integer> turnUpdatedGardener = new ArrayList<Integer>(10);
	ArrayList<Integer> savedArchonIDs = new ArrayList<Integer>(10);
	ArrayList<Integer> savedGardenerIDs = new ArrayList<Integer>(10);
	ArrayList<MapLocation> savedArchonLocations = new ArrayList<MapLocation>(10);
	ArrayList<MapLocation> savedGardenerLocations = new ArrayList<MapLocation>(10);


	public void execute() throws GameActionException {
		initialize();
		Object disruptionTarget = getDistruptionTarget();
		dodgeTowards(disruptionTarget);
		attack(disruptionTarget);
		cleanOutdatedInformation();
		Clock.yield();
	}


	/*
	 * calls the correct method for attacking, depending on type of target
	 */
	private void attack(Object target) throws GameActionException {
		if(target instanceof TreeInfo) attackTree((TreeInfo)target);
		else if(target instanceof RobotInfo) attackRobot((RobotInfo)target);
		else if(target instanceof MapLocation) attackLocation((MapLocation)target);
	}


	/*
	 * TODO
	 */
	private void attackTree(TreeInfo target) throws GameActionException {
		if(scout.canFireSingleShot() && !scout.hasAttacked()) {
			scout.fireSingleShot(scout.getLocation().directionTo(target.location));
		}
	}


	/*
	 * TODO
	 */
	private void attackRobot(RobotInfo target) throws GameActionException {
		if(scout.canFireSingleShot() && !scout.hasAttacked()) {
			scout.fireSingleShot(scout.getLocation().directionTo(target.location));
		}
	}


	/*
	 * TODO
	 */
	private void attackLocation(MapLocation target) throws GameActionException {
		if(scout.canFireSingleShot() && !scout.hasAttacked()) {
			scout.fireSingleShot(scout.getLocation().directionTo(target));
		}
	}


	/*
	 * dodges bullets and keeps away from attacking hostile units,
	 * while trying to get closer to given target
	 */
	private void dodgeTowards(Object target) throws GameActionException {
		float bulletSensorRange = Util.getMaxBulletSpeed() + scout.getType().bodyRadius + scout.getType().strideRadius;
		MapLocation goal = (target instanceof MapLocation) ? (MapLocation)target : ((BodyInfo)target).getLocation();

		BulletInfo[] nearbyBullets = scout.senseNearbyBullets(bulletSensorRange);
		RobotInfo[] nearbyHostiles = getNearbyHostileUnits();
		Direction dodgeBulletsDirection = getDodgeBulletsDirection(nearbyBullets, target);
		Direction dodgeHostilesDirection = getDodgeHostilesDirection(nearbyHostiles);
		Direction dodgeDirection = Geometry.average(dodgeHostilesDirection, dodgeBulletsDirection);

		// TODO: move
		if(dodgeDirection == null) dodgeDirection = scout.getLocation().directionTo(goal);
		Direction dir = dodgeDirection;
		for(int i = 0; i <= 180; i+=2) {
			if(scout.canMove(dir.rotateLeftDegrees(i))) { scout.move(dir.rotateLeftDegrees(i)); break; }
			if(scout.canMove(dir.rotateRightDegrees(i))) { scout.move(dir.rotateRightDegrees(i)); break; }
		}
	}


	/*
	 * returns a direction in which most bullets will be dodged
	 * TODO: dodge a bit better - especially don't walk into bullets that are traveling away from you
	 */
	private Direction getDodgeBulletsDirection(BulletInfo[] bullets, Object target) throws GameActionException {
		float dx = 0, dy = 0, count = 0;
		BulletInfo bullet;
		for(int i = bullets.length; --i>=0;) {
			bullet = bullets[i];
			Direction dir = Geometry.getPerpendicularAwayFromBullet(bullet, scout.getLocation());
			dx += dir.getDeltaX(1); dy += dir.getDeltaY(1); count++;
		}
		if(count == 0) return null;
		dx /= count; dy /= count;
		return new Direction(dx, dy);
	}


	/*
	 * returns a direction away from average location of hostile attacking units
	 */
	private Direction getDodgeHostilesDirection(RobotInfo[] enemyUnits) throws GameActionException {
		float x = 0, y = 0, count = 0;
		RobotInfo enemy;
		MapLocation curr = scout.getLocation();
		for(int i = enemyUnits.length; --i>=0;) {
			enemy = enemyUnits[i];
			if(enemy.getType().canAttack() && Util.getMaxAttackRange(enemy.getType()) <= enemy.getLocation().distanceTo(curr)) {
				x += enemy.getLocation().x;
				y += enemy.getLocation().y;
				count++;
			}
		}
		if(count == 0) return null;
		else return new Direction(new MapLocation(x/count, y/count), scout.getLocation());
	}


	/*
	 * tries to find a fitting target to scout, stalk, and attack.
	 *
	 * tries to find gardeners first, by sensing nearby units,
	 * by looking for opponent's trees (gardeners will likely water those),
	 * by memorizing already sensed gardeners' locations,
	 *
	 * by looking for hostile archons (will build gardeners at some point),
	 * by memorizing already sensed archons' locations,
	 *
	 *
	 * should all of those not give something to scout, get nearest enemy spawning point
	 */
	private Object getDistruptionTarget() throws GameActionException {

		// get hostile gardener with lowest health as target
		// and memorize locations and IDs of sensed gardeners and archons
		RobotInfo[] nearbyHostileRobots = senseHostileRobots();
		saveArchonGardenerLocations(nearbyHostileRobots);
		RobotInfo lowestGardener = Util.getLowestOfTypeFromList(RobotType.GARDENER, nearbyHostileRobots);
		if(lowestGardener != null) return lowestGardener;

		// get nearest hostile bullet tree, to find gardener there
		TreeInfo[] nearbyHostileTrees = senseHostileTrees();
		TreeInfo nearestHostileTree = Geometry.getNearest(nearbyHostileTrees, scout.getLocation());
		if(nearestHostileTree != null) return nearestHostileTree;

		// get nearest location from already sensed gardener locations
		MapLocation nearestGardenerLocation = Geometry.getNearest(savedGardenerLocations, scout.getLocation());
		if(nearestGardenerLocation != null) return nearestGardenerLocation;

		// get any hostile archon that has been sensed this turn
		RobotInfo lowestArchon = Util.getLowestOfTypeFromList(RobotType.ARCHON, nearbyHostileRobots);
		if(lowestArchon != null) return lowestArchon;

		// get nearest location from already sensed archon locations
		MapLocation nearestArchonLocation = Geometry.getNearest(savedArchonLocations, scout.getLocation());
		if(nearestArchonLocation != null) return nearestArchonLocation;

		// get the nearest spawning point
		MapLocation nearestSpawnLocation = Geometry.getNearest(scout.getInitialArchonLocations(scout.getTeam().opponent()), scout.getLocation());
		return nearestSpawnLocation;
	}


	/*
	 * saves locations and IDs of archons and gardeners in the given robot info array.
	 * updates locations, if an ID has already been saved.
	 * also saves the turn number of the last update of the saved information
	 * (for later clean-up - we don't actually want too old information)
	 */
	private void saveArchonGardenerLocations(RobotInfo[] robots) {
		for(RobotInfo robot : robots) {
			// check if we already have an archon with this id
			// if not, save it to list
			// otherwise update saved location
			if(robot.getType() == RobotType.ARCHON) {
				int index = savedArchonIDs.indexOf(robot.getID());
				if(index == -1) {
					savedArchonIDs.add(robot.getID());
					savedArchonLocations.add(robot.getLocation());
					turnUpdatedArchon.add(scout.getRoundNum());
				}
				else {
					savedArchonLocations.set(index, robot.getLocation());
					turnUpdatedArchon.set(index, scout.getRoundNum());
				}
			}

			// check if we already have a gardener with this id
			// if not, save it to list
			// otherwise update saved location
			if(robot.getType() == RobotType.GARDENER) {
				int index = savedGardenerIDs.indexOf(robot.getID());
				if(index == -1) {
					savedGardenerIDs.add(robot.getID());
					savedGardenerLocations.add(robot.getLocation());
					turnUpdatedGardener.add(scout.getRoundNum());
				}
				else{
					savedGardenerLocations.set(index, robot.getLocation());
					turnUpdatedGardener.set(index, scout.getRoundNum());
				}
			}
		}
	}


	/*
	 * removes stored information about sensed archons and gardeners,
	 * once the information can be assumed to be out of date,
	 * i.e. once the last update was 20 turns or more before,
	 * or the saved robot can be assumed to be destroyed
	 * (when cannot sense robot, although given its speed it should be sensed)
	 */
	private void cleanOutdatedInformation() throws GameActionException {
		int maxSaveTurns = 20;
		ArrayList<Integer> indices = new ArrayList<Integer>();

		// clean all information about archons that is older than maxSaveTurns
		for(int index = 0; index < turnUpdatedArchon.size(); index++)
			if(turnUpdatedArchon.get(index) + maxSaveTurns < scout.getRoundNum()) indices.add(index);
		for(int index : indices) {
			turnUpdatedArchon.remove(index);
			savedArchonIDs.remove(index);
			savedArchonLocations.remove(index);
		}
		indices.clear();

		// clean all information about gardeners that is older than maxSaveTurns
		for(int index = 0; index < turnUpdatedGardener.size(); index++)
			if(turnUpdatedGardener.get(index) + maxSaveTurns < scout.getRoundNum()) indices.add(index);
		for(int index : indices) {
			turnUpdatedGardener.remove(index);
			savedGardenerIDs.remove(index);
			savedGardenerLocations.remove(index);
		}
		indices.clear();

		// if saved gardener can be assumed to be destroyed, clean stored data
		for(int index = 0; index < savedGardenerLocations.size(); index++) {
			MapLocation gardenerLocation = savedGardenerLocations.get(index);
			if(scout.canSenseLocation(gardenerLocation)) {
				int turnsSince = scout.getRoundNum() - turnUpdatedGardener.get(index);
				float maxMoveDistance = turnsSince * RobotType.GARDENER.strideRadius;
				if(scout.canSenseAllOfCircle(gardenerLocation, maxMoveDistance) && !scout.canSenseRobot(savedGardenerIDs.get(index))) indices.add(index);
			}
		}
		for(int index : indices) {
			turnUpdatedGardener.remove(index);
			savedGardenerIDs.remove(index);
			savedGardenerLocations.remove(index);
		}
		indices.clear();

		// if saved archon can be assumed to be destroyed, clean stored data
		for(int index = 0; index < savedArchonLocations.size(); index++) {
			MapLocation archonLocation = savedArchonLocations.get(index);
			if(scout.canSenseLocation(archonLocation)) {
				int turnsSince = scout.getRoundNum() - turnUpdatedArchon.get(index);
				float maxMoveDistance = turnsSince * RobotType.ARCHON.strideRadius;
				if(scout.canSenseAllOfCircle(archonLocation, maxMoveDistance) && !scout.canSenseRobot(savedArchonIDs.get(index))) indices.add(index);
			}
		}
		for(int index : indices) {
			turnUpdatedArchon.remove(index);
			savedArchonIDs.remove(index);
			savedArchonLocations.remove(index);
		}
	}




}














