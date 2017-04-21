package shepherd.Bot.Behaviour.Scout;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.TreeInfo;
import shepherd.Bot.Scout;
import shepherd.Bot.Behaviour.Behaviour;
import shepherd.Bot.Utilities.Geometry;

public abstract class ScoutBehaviour extends Behaviour {

	protected ArrayList<MapLocation> neutralTreeLocations;
	protected RobotController scout;

	// functions to hide ugly typecasts and stuff
	// small set-up
	protected void initialize() throws GameActionException {
		if(scout == null) {
			neutralTreeLocations = getNeutralTreeLocations();
			scout = getController();
		}
	}
	protected ArrayList<MapLocation> getNeutralTreeLocations() {
		return ((Scout)executer).neutralTreeLocations;
	}
	protected RobotController getController() {
		return ((Scout)executer).getController();
	}
	protected MapLocation getNearestFromList(List<MapLocation> locationList) throws GameActionException {
		return Geometry.getNearest(neutralTreeLocations, scout.getLocation());
	}
	protected int getLastHostileSenseTurn() {
		return ((Scout)executer).lastHostileSenseTurn;
	}
	protected void setNearbyHostileUnits(RobotInfo[] hostiles) {
		((Scout)executer).lastHostileSenseTurn = getController().getRoundNum();
		((Scout)executer).nearbyHostileUnits = hostiles;
	}
	protected RobotInfo[] getNearbyHostileUnits() {
		return ((Scout)executer).nearbyHostileUnits;
	}
	protected MapLocation getAverageEnemyStartingLocaction() {
		return ((Scout)executer).getAverageEnemyStartingLocaction();
	}
	protected RobotInfo getNearestHostileGardenerOrArchon() throws GameActionException {
		return ((Scout)executer).getNearestHostileGardenerOrArchon();
	}
	protected RobotInfo[] senseHostileRobots() throws GameActionException {
		if(((Scout)executer).lastHostileSenseTurn < getController().getRoundNum()) {
			setNearbyHostileUnits(getController().senseNearbyRobots(getController().getType().sensorRadius, getController().getTeam().opponent()));
		}
		return getNearbyHostileUnits();
	}
	protected void setNearbyHostileTrees(TreeInfo[] hostileTrees) {
		((Scout)executer).lastTreeSenseTurn = getController().getRoundNum();
		((Scout)executer).nearbyHostileTrees = hostileTrees;
	}
	protected TreeInfo[] getNearbyHostileTrees() {
		return ((Scout)executer).nearbyHostileTrees;
	}
	protected TreeInfo[] senseHostileTrees() throws GameActionException {
		if(((Scout)executer).lastTreeSenseTurn < getController().getRoundNum()) {
			setNearbyHostileTrees(getController().senseNearbyTrees(getController().getType().sensorRadius, getController().getTeam().opponent()));
		}
		return getNearbyHostileTrees();
	}


}
