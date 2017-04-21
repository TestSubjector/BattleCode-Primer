package mangelware.bot.gardener;


import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TreeInfo;
import mangelware.bot.Bot;
import mangelware.util.Broadcast;
import mangelware.util.GameState;


public class Gardener extends Bot {

    public enum Buildable {
        TREE, LUMBERJACK, SCOUT, SOLDIER, TANK
    }


    public static void run() throws GameActionException {
        initialBuild();
        while(true) {
            long turn = rc.getRoundNum();

            if(GameState.canWinByPoints() || rc.getRoundNum() >= 2999) rc.donate(rc.getTeamBullets());
            if(rc.getTeamBullets() >= 1000) rc.donate(100);
            Broadcast.updateUnitCount();
            moveAwayFromEdges();
            water();
            buildFarm();
            buildUnits();


            if(turn == rc.getRoundNum()) Clock.yield();
        }
    }

    private static void initialBuild() throws GameActionException {
        if(rc.getRoundNum() > 100) return;
    	if(mapIsSmall() && forestTowardsEnemyIsSparse()) {
            buildUnit(RobotType.SOLDIER); Clock.yield();
            while(!rc.hasRobotBuildRequirements(RobotType.SOLDIER) || !rc.isBuildReady()) Clock.yield();
            buildUnit(RobotType.SOLDIER); Clock.yield();
        }

        else if(mapIsLarge() &&  mapHasBullets()) {
            buildUnit(RobotType.SCOUT); Clock.yield();
            while(!rc.hasRobotBuildRequirements(RobotType.SCOUT) || !rc.isBuildReady()) Clock.yield();
            buildUnit(RobotType.SCOUT); Clock.yield();
            while(!rc.hasRobotBuildRequirements(RobotType.SCOUT) || !rc.isBuildReady()) Clock.yield();
            buildUnit(RobotType.SCOUT); Clock.yield();
        }

        else {
            buildUnit(RobotType.LUMBERJACK);
            while(!rc.hasRobotBuildRequirements(RobotType.LUMBERJACK) || !rc.isBuildReady()) Clock.yield();
            buildUnit(RobotType.LUMBERJACK);
            while(!rc.hasRobotBuildRequirements(RobotType.LUMBERJACK) || !rc.isBuildReady()) Clock.yield();
            buildUnit(RobotType.LUMBERJACK);
        }

        while(rc.getTeamBullets() < GameConstants.BULLET_TREE_COST || !rc.isBuildReady()) { moveAwayFromEdges(); Clock.yield(); }
        buildFarm();
    }

    private static void water() throws GameActionException {
        TreeInfo[] trees = rc.senseNearbyTrees(rc.getType().bodyRadius + GameConstants.BULLET_TREE_RADIUS, rc.getTeam());
        for(TreeInfo tree: trees) {
            if(tree.getHealth() <= tree.getMaxHealth() - 3 && rc.canWater(tree.getID()))
                rc.water(tree.getID());
        }
    }

    private static void buildFarm() throws GameActionException {
        TreeInfo[] trees = rc.senseNearbyTrees(rc.getType().bodyRadius + GameConstants.BULLET_TREE_RADIUS, rc.getTeam());
        if(trees.length == 0) {
            Direction dir = rc.getLocation().directionTo(GameState.center).rotateLeftDegrees(60);
            for(int i = 0; i < 30; i++) {
                if(rc.canPlantTree(dir.rotateLeftDegrees(i))) { rc.plantTree(dir.rotateLeftDegrees(i)); break; }
            }
        }
        else if(trees.length < 4) {
            Direction dir = rc.getLocation().directionTo(GameState.center).rotateLeftDegrees(60);
            for(int i = 30; i < 240; i+=5) {
                if(rc.canPlantTree(dir.rotateLeftDegrees(i))) { rc.plantTree(dir.rotateLeftDegrees(i)); break; }
            }
        }
    }


    private static void moveAwayFromEdges() throws GameActionException {
        // check for nearby edges
        float radius = rc.getType().bodyRadius + GameConstants.GENERAL_SPAWN_OFFSET + 2*GameConstants.BULLET_TREE_RADIUS + Float.MIN_NORMAL;
        if(rc.onTheMap(rc.getLocation(), radius)) return;
        if(rc.hasMoved()) return;

        // find edges
        Direction[] dirs = new Direction[]{Direction.getEast(), Direction.getNorth(), Direction.getSouth(), Direction.getWest()};
        MapLocation edge = null;
        for(int i = 0; i < dirs.length; i++) {
            edge = rc.getLocation().add(dirs[i], radius);
            if(!rc.onTheMap(edge)) break;
        }

        // move away from edge
        if(edge != null) {
            Direction dir = edge.directionTo(rc.getLocation());
            tryMove(dir, 120, 5);
        }
    }

    private static void buildUnits() throws GameActionException {
        Direction dir = rc.getLocation().directionTo(GameState.enemyCenter);

        if(rc.getRobotCount() > 60) return;

        RobotType type = RobotType.LUMBERJACK;
        if((GameState.lumberCount > 12) || (rc.getRoundNum() > 400 && rc.getTreeCount() > 6 && rc.getTeamBullets() > RobotType.TANK.bulletCost + 50))
        	type = RobotType.TANK;
        else if(GameState.lumberCount < 20 && rc.getTreeCount() > 3)
        	type = RobotType.LUMBERJACK;
        else if(rc.getRoundNum() >= 2999)
        	{rc.donate(rc.getTeamBullets()); return; }
        else if(rc.getTeamBullets() > 600)
        	{ rc.donate(100); return; }

        if(GameState.lumberCount > 30) type = RobotType.TANK;

        if(rc.getRoundNum() < 100 && (rc.senseNearbyRobots(rc.getType().sensorRadius, rc.getTeam().opponent()).length > 0)) type = RobotType.SOLDIER;

        if(!rc.isBuildReady()) return;
        if(rc.getTeamBullets() < type.bulletCost) return;
        for(int i = 0; i < 180; i+=5) {
            if(rc.canBuildRobot(type, dir.rotateLeftDegrees(i))) { rc.buildRobot(type, dir.rotateLeftDegrees(i)); break; }
            if(rc.canBuildRobot(type, dir.rotateRightDegrees(i))) { rc.buildRobot(type, dir.rotateRightDegrees(i)); break; }
        }
    }


    private static void buildUnit(RobotType type) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(GameState.enemyCenter);

        if(!rc.isBuildReady()) return;
        if(rc.getTeamBullets() < type.bulletCost) return;
        for(int i = 0; i < 180; i+=5) {
            if(rc.canBuildRobot(type, dir.rotateLeftDegrees(i))) { rc.buildRobot(type, dir.rotateLeftDegrees(i)); break; }
            if(rc.canBuildRobot(type, dir.rotateRightDegrees(i))) { rc.buildRobot(type, dir.rotateRightDegrees(i)); break; }
        }
    }


    private static boolean mapIsSmall() {
        float minDist = Float.MAX_VALUE;
        for(int i = 0; i < GameState.ourSpawns.length; i++) {
            for(int j = 0; j < GameState.enemySpawns.length; j++) {
                float dist = GameState.ourSpawns[i].distanceTo(GameState.enemySpawns[j]);
                if(dist < minDist) minDist = dist;
            }
        }
        return minDist <= 30;
    }


    private static boolean forestTowardsEnemyIsSparse() {
        MapLocation center = rc.getLocation().add(rc.getLocation().directionTo(GameState.enemyCenter), rc.getType().sensorRadius);
        TreeInfo[] trees = rc.senseNearbyTrees(center, rc.getType().sensorRadius, Team.NEUTRAL);
        return trees.length <= 3;
    }


    private static boolean mapIsLarge() {
        float minDist = Float.MAX_VALUE;
        for(int i = 0; i < GameState.ourSpawns.length; i++) {
            for(int j = 0; j < GameState.enemySpawns.length; j++) {
                float dist = GameState.ourSpawns[i].distanceTo(GameState.enemySpawns[j]);
                if(dist < minDist) minDist = dist;
            }
        }
        return minDist <= 65;
    }

    private static boolean mapHasBullets() {
        TreeInfo[] trees = rc.senseNearbyTrees(rc.getType().sensorRadius, Team.NEUTRAL);
        for(TreeInfo tree : trees) if(tree.containedBullets > 0) return true;
        return false;
    }


}
