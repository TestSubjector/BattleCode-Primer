package mangelware.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import battlecode.common.BodyInfo;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

public class Util implements Comparator<BodyInfo>{

	public static RobotController rc;
	private static Util compare = new Util();

	public static boolean isLineFreeFromOwnUnits(RobotInfo target) {
		Direction dir = rc.getLocation().directionTo(target.getLocation());
		float distance = rc.getLocation().distanceTo(target.getLocation());
		MapLocation center1 = rc.getLocation().add(dir, distance/3);
		MapLocation center2 = rc.getLocation().add(dir, distance*2/3);
		float radius = distance/6;
		RobotInfo[] allies = rc.senseNearbyRobots(center1, radius, rc.getTeam());
		if(allies.length > 0) return false;
		allies = rc.senseNearbyRobots(center2, radius, rc.getTeam());
		return allies.length <= 0;
	}

	public static boolean isLineFreeFromNeutralTrees(RobotInfo target) {
		Direction dir = rc.getLocation().directionTo(target.getLocation());
		float distance = rc.getLocation().distanceTo(target.getLocation());
		MapLocation center1 = rc.getLocation().add(dir, distance/3);
		MapLocation center2 = rc.getLocation().add(dir, distance*2/3);
		float radius = distance/6;
		TreeInfo[] neutrals = rc.senseNearbyTrees(center1, radius, Team.NEUTRAL);
		if(neutrals.length > 0) return false;
		neutrals = rc.senseNearbyTrees(center2, radius, Team.NEUTRAL);
		return neutrals.length <= 0;
	}


    public static BodyInfo[] mergeSortedByAngle(TreeInfo[] trees, RobotInfo[] robots) {
    	ArrayList<BodyInfo> bodies = new ArrayList<BodyInfo>(trees.length + robots.length);
    	bodies.addAll(Arrays.asList(trees));
    	bodies.addAll(Arrays.asList(robots));
    	Collections.sort(bodies, compare);
    	return bodies.toArray(new BodyInfo[trees.length + robots.length]);
    }


    public static float toTau(float rads) {
    	if(rads < 0) return (float) (2*Math.PI + rads);
    	else return rads;
    }

	public int compare(BodyInfo arg0, BodyInfo arg1) {
		float a0 = toTau(rc.getLocation().directionTo(arg0.getLocation()).radians);
		float a1 = toTau(rc.getLocation().directionTo(arg1.getLocation()).radians);
		if(a0 == a1) return 0;
		if(a0 < a1) return -1;
		return 1;
	}

}
