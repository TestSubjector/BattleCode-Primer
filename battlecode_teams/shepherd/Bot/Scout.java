package shepherd.Bot;

import java.util.ArrayList;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TreeInfo;
import shepherd.Bot.Behaviour.Behaviour;
import shepherd.Bot.Behaviour.Scout.DisruptEconBehaviour;
import shepherd.Bot.Behaviour.Scout.InitialScoutBehaviour;

public class Scout extends Bot {

	public ArrayList<MapLocation> neutralTreeLocations = new ArrayList<MapLocation>();
	public MapLocation averageEnemystartingLocation;
	public TreeInfo[] nearbyHostileTrees;
	public RobotInfo[] nearbyHostileUnits;
	public int lastTreeSenseTurn = -1;
	public int lastHostileSenseTurn = -1;

	RobotController scout;

	public Scout(RobotController rc) {
		controller = rc;
		scout = controller;
	}

	public Behaviour getBehaviour() throws GameActionException {

		// start by using the initial behaviour first
		if(behaviour == null) return new InitialScoutBehaviour();

		// check if we can keep staying in the initial behaviour, or if we can disrupt the enemy's econ
		nearbyHostileUnits = scout.senseNearbyRobots(controller.getType().sensorRadius, controller.getTeam().opponent());
		lastHostileSenseTurn = scout.getRoundNum();
		if(neutralTreeLocations.size() == 0 && getNearestHostileGardenerOrArchon() != null) {
			if(behaviour instanceof DisruptEconBehaviour) return behaviour;
			else return new DisruptEconBehaviour();
		}
		else if(behaviour instanceof InitialScoutBehaviour) return behaviour;

		// if all else fails, just do the initial behaviour thingy
		return new InitialScoutBehaviour();
	}



	// returns the nearest hostile gardener, or archon if none found, or null if neither found
	public RobotInfo getNearestHostileGardenerOrArchon() throws GameActionException {
		RobotInfo nearestHostileGardener = null, nearestHostileArchon = null;
		float gardenerDist = Float.MAX_VALUE, archonDist = Float.MAX_VALUE;
		for(RobotInfo hostile : nearbyHostileUnits) {
			if(hostile.getType() == RobotType.GARDENER) {
				float dist = scout.getLocation().distanceTo(hostile.location);
				if(dist < gardenerDist) {
					nearestHostileGardener = hostile;
					gardenerDist = dist;
				}
			}
			else if(hostile.getType() == RobotType.ARCHON) {
				float dist = scout.getLocation().distanceTo(hostile.location);
				if(dist < archonDist) {
					nearestHostileArchon = hostile;
					archonDist = dist;
				}
			}
		}
		if(nearestHostileGardener == null) return nearestHostileArchon;
		return nearestHostileGardener;
	}


	public MapLocation getAverageEnemyStartingLocaction() {
		if(averageEnemystartingLocation != null) return averageEnemystartingLocation;

		MapLocation[] startingLocations = scout.getInitialArchonLocations(scout.getTeam().opponent());
		float x = 0, y = 0;
		for(MapLocation loc : startingLocations) {
			x += loc.x; y += loc.y;
		}
		averageEnemystartingLocation = new MapLocation(x / startingLocations.length, y / startingLocations.length);
		return averageEnemystartingLocation;
	}

}
