package mangelware.bot.scout;


import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TreeInfo;
import mangelware.bot.Bot;
import mangelware.util.Broadcast;
import mangelware.util.GameState;
import mangelware.util.Util;


public class Scout extends Bot {


	private static float c = 1, s = 1.5f, a = 2, v = 1;


    public static void run() throws GameActionException {
        while(true) {
            long turn = rc.getRoundNum();

            if(GameState.canWinByPoints() || rc.getRoundNum() >= 2999) rc.donate(rc.getTeamBullets());
            if(rc.getTeamBullets() >= 1000) rc.donate(100);
            if(rc.getRoundNum() > 2000) rc.disintegrate();
            Broadcast.updateUnitCount();
            gatherBullets();
            harassEnemy();
            sendInformation();

            if(turn == rc.getRoundNum()) Clock.yield();
        }
    }




    private static void harassEnemy() throws GameActionException {
		// find enemy gardeners and archons
    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadius, rc.getTeam().opponent());
    	RobotInfo gardener = null; int armyCount = 0;
    	for(RobotInfo enemy : enemies) {
    		if(enemy.getType() == RobotType.GARDENER && gardener == null) { gardener = enemy; }
    		else if(enemy.getType().canAttack()) armyCount++;
    	}

    	// get next enemy spawn location we cannot sense
    	MapLocation flockTarget = Bot.getNextFlockLocation();

    	// if no units have been sensed, flock-move to next spawn
    	if(enemies.length == 0 && flockTarget != null) {
    		if(!rc.hasMoved()) Bot.flockMove(c, s, a, v, flockTarget);
    		return;
    	}

    	// if only gardeners and archons have been sensed, charge it
    	if(armyCount == 0 && (gardener != null)) {
    		RobotInfo target = gardener;
    		Direction dir = rc.getLocation().directionTo(target.getLocation());
    		if(!rc.hasMoved() && dir != null) { tryMove(dir, 120, 5); }

    		// if can hit gardener this turn, shoot
    		float targetDist = (target.getType() == RobotType.GARDENER) ? rc.getLocation().distanceTo(target.getLocation()) : Float.MAX_VALUE;
    		float range = rc.getType().bulletSpeed + rc.getType().bodyRadius + GameConstants.BULLET_SPAWN_OFFSET + RobotType.GARDENER.bodyRadius - Float.MIN_NORMAL;
    		if(range >= targetDist && !rc.hasAttacked() && rc.canFireSingleShot()) rc.fireSingleShot(rc.getLocation().directionTo(target.getLocation()));
    	}

    	// if army units have been sensed but no gardener
    	if(armyCount > 0 && gardener == null) {
    		// move away from all army units
    		float x = 0, y = 0;
    		for(RobotInfo enemy : enemies) {
    			if(enemy.getType().canAttack()) {
    				x += enemy.getLocation().x;
    				y += enemy.getLocation().y;
    			}
    		}
    		x /= armyCount; y /= armyCount;
    		MapLocation center = new MapLocation(x,y);
    		if(!rc.hasMoved()) tryMove(center.directionTo(rc.getLocation()), 85, 5);
    	}

    	// the interesting part: we want to kill the gardener while avoiding the army units. how?
    	if(armyCount > 0 && gardener != null) {

    		// get closest army unit by attack range
    		RobotInfo closest = null; float closestAttackRange = Float.MAX_VALUE;
    		for(RobotInfo enemy : enemies) {
    			RobotType type = enemy.getType();
    			if(!type.canAttack()) continue;
    			float attackRange = type.bodyRadius + rc.getType().bodyRadius + type.strideRadius;
    			attackRange += (type == RobotType.LUMBERJACK) ? GameConstants.LUMBERJACK_STRIKE_RADIUS : type.bulletSpeed + GameConstants.BULLET_SPAWN_OFFSET;
    			attackRange += Float.MIN_NORMAL;
    			float distance = rc.getLocation().distanceTo(enemy.getLocation());
    			if(distance - attackRange < closestAttackRange) {
    				closestAttackRange = distance - attackRange;
    				closest = enemy;
    			}
    		}

    		// circle around closest by attack range towards gardener
    		RobotType type = closest.getType();
    		float r = type.bodyRadius + rc.getType().bodyRadius + type.strideRadius;
			r += (type == RobotType.LUMBERJACK) ? GameConstants.LUMBERJACK_STRIKE_RADIUS : type.bulletSpeed + GameConstants.BULLET_SPAWN_OFFSET;
    		Direction direction = rc.getLocation().directionTo(gardener.getLocation());
    		float d = rc.getLocation().distanceTo(closest.getLocation());
    		float alpha = direction.radiansBetween(rc.getLocation().directionTo(closest.getLocation()));
    		float l = (float) Math.abs(d*Math.sin(alpha));
    		float t = (float) Math.sqrt(d*d + l*l);
    		float x = r - l;
    		float y = (float) Math.sqrt(t*t + x*x);
    		float beta = (float) Math.abs(Math.asin(x/y));
    		Direction dir = (alpha > 0) ? direction.rotateRightRads(beta) : direction.rotateLeftRads(beta);
    		if(!rc.hasMoved()) tryMove(dir, 45, 1);

    		// if can hit gardener this turn, shoot
    		if(Util.isLineFreeFromOwnUnits(gardener)) {
    			float targetDist = rc.getLocation().distanceTo(gardener.getLocation());
    			float range = rc.getType().bulletSpeed + rc.getType().bodyRadius + GameConstants.BULLET_SPAWN_OFFSET + RobotType.GARDENER.bodyRadius - Float.MIN_NORMAL;
    			if(range >= targetDist && !rc.hasAttacked() && rc.canFireSingleShot()) rc.fireSingleShot(rc.getLocation().directionTo(gardener.getLocation()));
    		}
    	}

	}



	private static void sendInformation() throws GameActionException {

    }


    private static void gatherBullets() throws GameActionException {
        // find nearest tree with bullets
        TreeInfo[] trees = rc.senseNearbyTrees(rc.getType().sensorRadius, Team.NEUTRAL);
        TreeInfo tree = null;
        for(int i = 0; i < trees.length; i++) {
            if(trees[i].containedBullets > 0) { tree = trees[i]; break; }
        }
        if(tree == null) return;

        // move to tree and shake it, if possible
        Direction dir = rc.getLocation().directionTo(tree.getLocation());
        tryMove(dir, 120, 5);
        if(rc.canShake(tree.getID())) rc.shake(tree.getID());
    }

}
