package mangelware.bot;

import java.util.HashMap;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import mangelware.util.Broadcast;
import mangelware.util.GameState;
import mangelware.util.Geometry;
import mangelware.util.Util;

public class Bot {

    public static RobotController rc;
    public static HashMap<Integer, MapLocation> previousLocations = new HashMap<Integer, MapLocation>();

    public static void initialize(RobotController bot) {
        rc = bot;
        Util.rc = rc;
        Geometry.rc = rc;
        Broadcast.rc = rc;
        GameState.rc = rc;
        GameState.mySpawn = rc.getLocation();
        GameState.spawnRound = rc.getRoundNum();
        GameState.ourSpawns = rc.getInitialArchonLocations(rc.getTeam());
        GameState.ourCenter = Geometry.centerOf(GameState.ourSpawns);
        GameState.enemySpawns = rc.getInitialArchonLocations(rc.getTeam().opponent());
        GameState.enemyCenter = Geometry.centerOf(GameState.enemySpawns);
        GameState.center = Geometry.centerOf(GameState.enemyCenter, GameState.ourCenter);
        GameState.startArchons = GameState.ourSpawns.length;
        GameState.targetLocation = GameState.enemyCenter;
    }


    public static void tryMove(Direction dir, float maxDegrees, float turnDegrees) throws GameActionException {
        if(dir == null || (dir.getDeltaX(1) == 0 && dir.getDeltaY(1) == 0)) return;
        for(float i = 0; i <= maxDegrees; i += turnDegrees) {
            if(rc.canMove(dir.rotateLeftDegrees(i))) { rc.move(dir.rotateLeftDegrees(i)); break; }
            if(rc.canMove(dir.rotateRightDegrees(i))) { rc.move(dir.rotateRightDegrees(i)); break; }
        }
    }

    private static MapLocation[] targetLocations;
    private static int index = 0;
    public static MapLocation getNextFlockLocation() {
        if(targetLocations == null) {
        	int i = 0;
        	targetLocations = new MapLocation[4 + GameState.enemySpawns.length];
        	targetLocations[i++] = GameState.enemyCenter;
        	for(MapLocation loc : GameState.enemySpawns) targetLocations[i++] = loc;
        	targetLocations[i++] = GameState.enemyCenter;
        	targetLocations[i++] = GameState.center;
        	targetLocations[i++] = GameState.ourCenter;
        }

    	// get next target location to flock-sweep towards
        if(rc.getLocation().distanceTo(GameState.targetLocation) <= rc.getType().sensorRadius / 2) {
            if(index == targetLocations.length) index = 0;
            GameState.targetLocation = targetLocations[index++];
        }
        return GameState.targetLocation;
    }


    public static void flockMove(float c, float s, float a, float v, MapLocation target) throws GameActionException {
        // precalculations for alignment, cohesion, and separation
        RobotInfo[] alliedRobots = rc.senseNearbyRobots(rc.getType().sensorRadius, rc.getTeam());
        Direction[] alignmentDirs = new Direction[alliedRobots.length];
        Direction[] separationDirs = new Direction[alliedRobots.length];
        float[] separationWeights = new float[alliedRobots.length];
        float[] alignmentWeigths = new float[alliedRobots.length];
        float x = 0, y = 0; int i = 0;
        for(RobotInfo robot : alliedRobots) {
            // don't align with our economy robots, they shouldn't even be near the enemy
            if(robot.getType() == RobotType.GARDENER || robot.getType() == RobotType.ARCHON) continue;

            // cohesion
            MapLocation current = robot.getLocation();
            x += current.x; y += current.y;

            // separation
            Direction seperate = current.directionTo(rc.getLocation());
            separationDirs[i] = seperate;
            float dist = current.distanceTo(rc.getLocation());
            float weight = 1/(dist - 2*rc.getType().bodyRadius + 0.01f);
            separationWeights[i] = weight;

            // alignment
            MapLocation previous = previousLocations.put(robot.getID(), current);
            if(previous != null) {
                Direction alignment = previous.directionTo(current);
                weight = previous.distanceTo(current);
                alignmentDirs[i] = alignment;
                alignmentWeigths[i] = weight;
            }

            i++;
        }
        MapLocation center = (i == 0)? null : new MapLocation(x/i, y/i);

        // calculate flocking directions
        float cx = 0, cy = 0, sx = 0, sy = 0, ax = 0, ay = 0, vx = 0, vy = 0;
        for(i = 0; i < alignmentDirs.length; i++) {
            Direction dir = alignmentDirs[i];
            if(dir == null) continue;
            ax += dir.getDeltaX(alignmentWeigths[i]);
            ay += dir.getDeltaY(alignmentWeigths[i]);
        }
        for(i = 0; i < separationDirs.length; i++) {
            Direction dir = separationDirs[i];
            if(dir == null) continue;
            sx += dir.getDeltaX(separationWeights[i]);
            sy += dir.getDeltaY(separationWeights[i]);
        }
        if(center != null) {
            Direction dir = rc.getLocation().directionTo(center);
            float dist = rc.getLocation().distanceTo(center);
            cx += dir.getDeltaX(dist);
            cy += dir.getDeltaY(dist);
        }
        if(target != null) {
            Direction dir = rc.getLocation().directionTo(target);
            float dist = rc.getLocation().distanceTo(target);
            vx += dir.getDeltaX(dist);
            vy += dir.getDeltaY(dist);
        }

        // calculate flocking force
        cx *= c; cy *= c; sx *= s; sy *= s; ax *= a; ay *= a; vx *= v; vy *= v;
        float dx = (cx + sx + ax + vx) / 4;
        float dy = (cy + sy + ay + vy) / 4;
        Direction flockingDirection = new Direction(dx, dy);

        tryMove(flockingDirection, 120, 5);
    }


}
