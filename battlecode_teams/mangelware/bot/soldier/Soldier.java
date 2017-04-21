package mangelware.bot.soldier;




import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import mangelware.bot.Bot;
import mangelware.util.Broadcast;
import mangelware.util.GameState;
import mangelware.util.Util;

public class Soldier extends Bot {

    private static float c = 10,  s = 15,  a = 10,  v = 25;


    public static void run() throws GameActionException {
        while(true) {
            long turn = rc.getRoundNum();

            if(GameState.canWinByPoints() || rc.getRoundNum() >= 2999) rc.donate(rc.getTeamBullets());
            if(rc.getTeamBullets() >= 1000) rc.donate(100);
            Broadcast.updateUnitCount();
            shootEnemy();
            clearBuildLocation();
            moveTowardsTarget();

            if(turn == rc.getRoundNum()) Clock.yield();
        }
    }



	// stay just in range to instantly attack the closest enemy unit
    private static void shootEnemy() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadius, rc.getTeam().opponent());
        float range = rc.getType().bodyRadius + GameConstants.BULLET_SPAWN_OFFSET + rc.getType().bulletSpeed;
        if(enemies.length == 0) return;
        RobotInfo target = enemies[0];
        range -= target.getRadius();
        float dist = rc.getLocation().distanceTo(target.getLocation());
        float length = dist - range;
        if(length > 0 && !rc.hasMoved()) tryMove(rc.getLocation().directionTo(target.getLocation()), 120, 5);
        else if(length < 0 && !rc.hasMoved()) tryMove(target.getLocation().directionTo(rc.getLocation()), 120, 5);

        if(Util.isLineFreeFromNeutralTrees(target) && Util.isLineFreeFromOwnUnits(target) && rc.canFirePentadShot()) rc.firePentadShot(rc.getLocation().directionTo(target.getLocation()));
        else if(Util.isLineFreeFromNeutralTrees(target) && Util.isLineFreeFromOwnUnits(target) && rc.canFireTriadShot()) rc.fireTriadShot(rc.getLocation().directionTo(target.getLocation()));
        else if(Util.isLineFreeFromNeutralTrees(target) && Util.isLineFreeFromOwnUnits(target) && rc.canFireSingleShot()) rc.fireSingleShot(rc.getLocation().directionTo(target.getLocation()));
    }

    private static void moveTowardsTarget() throws GameActionException {
        // TODO: get enemy locations via scout and such
        GameState.targetLocation = getNextFlockLocation();
        if(!rc.hasMoved()) flockMove(c, s, a, v, GameState.targetLocation);
    }



	/*
     * moves away from nearest gardener until a distance of at least twice your radius,
     * to not block him from building more units
     */
    private static void clearBuildLocation() throws GameActionException {
        // find nearest gardener
        RobotInfo gardener = null;
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getType().sensorRadius, rc.getTeam());
        for(RobotInfo ally : nearbyAllies) {
            if(ally.getType() == RobotType.GARDENER && gardener == null) {
                gardener = ally;
            }
        }
        if(gardener == null) return;

        // move away from nearest gardener
        float dist = rc.getLocation().distanceTo(gardener.getLocation()) - rc.getType().bodyRadius - gardener.getType().bodyRadius - GameConstants.GENERAL_SPAWN_OFFSET;
        if(dist <= rc.getType().bodyRadius*2 + GameConstants.GENERAL_SPAWN_OFFSET) {
            if(!rc.hasMoved()) tryMove(gardener.getLocation().directionTo(rc.getLocation()), 120, 5);
        }

    }





}
