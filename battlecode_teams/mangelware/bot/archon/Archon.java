package mangelware.bot.archon;



import java.util.ArrayList;

import battlecode.common.BodyInfo;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TreeInfo;
import mangelware.bot.Bot;
import mangelware.util.Broadcast;
import mangelware.util.GameState;
import mangelware.util.Util;

// TODO:
// - be more intelligent about when to hire
// - move away from own flowers before hiring as to not have
//	 two flowers intersect
// - donate bullets once you have more than like 2000 or so
public class Archon extends Bot {

    public static void run() throws GameActionException {
        while(true) {
            long turn = rc.getRoundNum();

            if(GameState.canWinByPoints() || rc.getRoundNum() >= 2999) rc.donate(rc.getTeamBullets());
            if(rc.getTeamBullets() >= 1000) rc.donate(100);
            Broadcast.updateUnitCount();
            if(shouldHireGardener()) hireGardener();
            moveAwayFromGardener();


            if(turn == rc.getRoundNum()) Clock.yield();
        }
    }

    // TODO: make more intelligent choices
    private static boolean shouldHireGardener() throws GameActionException {

        // don't waste bullets if we can win this turn
        if(GameState.canWinByPoints() || rc.getRoundNum() >= 2999) rc.donate(rc.getTeamBullets());

        // don't try to build, if on cooldown
        if(rc.getBuildCooldownTurns() > 0) return false;

        // at the start of a match, hire one gardener
        if(rc.getRobotCount() <= GameState.startArchons) return true;

        // build one as soon as 3 to 4 trees have been built by another
        if(rc.getTreeCount() > 3*GameState.gardenerCount && rc.getTeamBullets() >= 150) return true;

        return false;
    }



    private static void hireGardener() throws GameActionException {
        if(!rc.isBuildReady() || rc.getTeamBullets() < RobotType.GARDENER.bulletCost) return;

        // calculate best direction to hire in
        Direction dir = getHireDirection();
        if(dir != null) { if(rc.canHireGardener(dir)) rc.hireGardener(dir); }
        else {
            dir = rc.getLocation().directionTo(GameState.enemyCenter);
            for(int i = 0; i <= 180; i+=5) {
                if(rc.canHireGardener(dir.rotateLeftDegrees(i))) { rc.hireGardener(dir.rotateLeftDegrees(i)); break; }
                if(rc.canHireGardener(dir.rotateRightDegrees(i))) { rc.hireGardener(dir.rotateRightDegrees(i)); break; }
            }
        }
    }


    private static Direction getHireDirection() throws GameActionException {


        // if nothing is near us, just get the next best direction
        float radius = rc.getType().bodyRadius + RobotType.GARDENER.bodyRadius*2 + GameConstants.GENERAL_SPAWN_OFFSET*2 + RobotType.LUMBERJACK.bodyRadius*2;
        MapLocation center = rc.getLocation();
        if(!rc.isCircleOccupiedExceptByThisRobot(center, radius) && rc.onTheMap(center, radius)) {
            Direction dir = center.directionTo(GameState.enemyCenter).opposite();
            return dir;
        }


        // if only the map edge is in the way, get a direction parallel to edge, but so that archon can still move
        if(!rc.isCircleOccupiedExceptByThisRobot(center, radius)) {
            // check for edges or corners
            Direction[] dirs = new Direction[]{Direction.getEast(), Direction.getNorth(), Direction.getSouth(), Direction.getWest()};
            MapLocation edge; int index = -1;
            for(int i = 0; i < 4; i++) {
                edge = rc.getLocation().add(dirs[i], radius);
                if(!rc.onTheMap(edge)) {
                    if(index == -1) { index = i; break; }
                }
            }
            Direction dir = dirs[index].rotateLeftDegrees(90);
            edge = rc.getLocation().add(dir, radius);
            while(!rc.onTheMap(edge)) {
                dir = dir.rotateLeftDegrees(90);
                edge = rc.getLocation().add(dir, radius);
            }
            return dir;
        }

        // find the two bodies next to each other with enough distance to fit in a gardener
        TreeInfo[] trees = rc.senseNearbyTrees(center, radius, null);
        RobotInfo[] robots = rc.senseNearbyRobots(center, radius, null);
        BodyInfo[] bodies = Util.mergeSortedByAngle(trees, robots);
        ArrayList<MapLocation> centers = new ArrayList<MapLocation>();
        float maxDist = Float.MIN_VALUE;
        for(int i = 0; i < bodies.length-1; i++) {
            float dist = bodies[i].getLocation().distanceTo(bodies[i+1].getLocation());
            dist -= (bodies[i].getRadius() + bodies[i+1].getRadius());
            if(dist > maxDist && dist >= RobotType.GARDENER.bodyRadius*2) {
                maxDist = dist;
                Direction d = bodies[i].getLocation().directionTo(bodies[i+1].getLocation());
                float distance = dist + bodies[i].getRadius() + bodies[i+1].getRadius();
                MapLocation c = bodies[i].getLocation().add(d, distance/2);
                d = rc.getLocation().directionTo(c);
                for(int j = 0; j < 5; j++) {
                    if(rc.canHireGardener(d.rotateLeftDegrees(j))) {
                        c = rc.getLocation().add(d.rotateLeftDegrees(j), rc.getType().bodyRadius + GameConstants.GENERAL_SPAWN_OFFSET + RobotType.GARDENER.bodyRadius);
                        centers.add(c);
                        break;
                    }
                    if(rc.canHireGardener(d.rotateRightDegrees(j))) {
                        c = rc.getLocation().add(d.rotateRightDegrees(j), rc.getType().bodyRadius + GameConstants.GENERAL_SPAWN_OFFSET + RobotType.GARDENER.bodyRadius);
                        centers.add(c);
                        break;
                    }
                }
            }
        }
        if(centers.size() == 0) return null;
        if(centers.size() == 1) return rc.getLocation().directionTo(centers.get(0));


        // if multiple possible locations to build have been found, filter those, where the gardener can build
        ArrayList<MapLocation> filtered = new ArrayList<MapLocation>();
        float a = RobotType.GARDENER.bodyRadius + RobotType.LUMBERJACK.bodyRadius + GameConstants.GENERAL_SPAWN_OFFSET;
        float b = rc.getType().bodyRadius + RobotType.GARDENER.bodyRadius + GameConstants.GENERAL_SPAWN_OFFSET;
        float alpha = (float) Math.toDegrees(Math.abs(Math.acos( (a*a)/(2*a*b) )));
        radius = RobotType.GARDENER.bodyRadius + GameConstants.GENERAL_SPAWN_OFFSET + RobotType.LUMBERJACK.bodyRadius*2 + Float.MIN_NORMAL;
        for(int i = 0; i < centers.size(); i++) {
            center = centers.get(i);
            if(!rc.isCircleOccupiedExceptByThisRobot(center, radius) && rc.onTheMap(center, radius)) filtered.add(center);
            Direction d = center.directionTo(rc.getLocation());
            for(float beta = alpha; beta <= 360 - 2*alpha; beta += 5) {
                Direction dir = d.rotateLeftDegrees(beta);
                MapLocation check = center.add(dir, a);
                if(!rc.isCircleOccupied(check, RobotType.LUMBERJACK.bodyRadius + Float.MIN_NORMAL) && rc.onTheMap(check, RobotType.LUMBERJACK.bodyRadius + Float.MIN_NORMAL))
                { filtered.add(center); break; }
            }
        }
        if(filtered.size() == 0) return null;
        if(filtered.size() == 1) return rc.getLocation().directionTo(filtered.get(0));
        System.out.println("153");
        // if multiple are possible, get the first one towards enemy center
        MapLocation min = null; float minDist = Float.MAX_VALUE;
        for(MapLocation loc : filtered) {
            float distLoc = loc.distanceTo(GameState.enemyCenter);
            if(distLoc < minDist) {
                min = loc;
                minDist = distLoc;
            }
        }
        if(min != null) return rc.getLocation().directionTo(min);
        System.out.println("164");

        return null;
    }

    // TODO: move away from hired gardener, to not block him
    private static void moveAwayFromGardener() throws GameActionException {
        float minDist = RobotType.GARDENER.bodyRadius + rc.getType().bodyRadius + 2*GameConstants.BULLET_TREE_RADIUS + RobotType.TANK.bodyRadius*2;
        RobotInfo[] allies = rc.senseNearbyRobots(minDist, rc.getTeam());
        RobotInfo gardener = null;
        for(RobotInfo ally : allies) { if(ally.getType() == RobotType.GARDENER) { gardener = ally; break; } }
        if(gardener != null) {
            tryMove(rc.getLocation().directionTo(GameState.enemyCenter), 120, 5);
        }
    }


}
