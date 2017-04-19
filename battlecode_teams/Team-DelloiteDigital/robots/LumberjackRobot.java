package p_patrick.robots;

import battlecode.common.*;
import ddframework.robots.SmartBaseRobot;
import ddframework.util.RandomUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LumberjackRobot extends SmartBaseRobot {

    static private Direction exploreDirection = RandomUtil.randomDirection();
    static private boolean berzerker = false;

    public LumberjackRobot(RobotController controller) {
        super(controller);
    }

    @Override
    protected void onGameRound(RobotController rc) throws Exception {
        super.onGameRound(rc);

        // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
        RobotInfo[] visibleHostiles = getCachedVisibleHostiles();
        RobotInfo[] visibleFriendlies = getCachedVisibleFriendlies();

        List<RobotInfo> nearbyLumberJacksArray = new ArrayList<>();
        if (visibleFriendlies.length > 0) {

            for (RobotInfo robot : visibleFriendlies) {
                if (robot.type == RobotType.LUMBERJACK) {
                    nearbyLumberJacksArray.add(robot);
                }
            }
            // If there are lots of lumberjacks nearby, lets form a squadron and go explore.
            // TODO: be smarter about targeting.
//            RobotInfo[] nearbyLumberJacks = nearbyLumberJacksArray.toArray(new RobotInfo[nearbyLumberJacksArray.size()]);
//
//            // If there are lots of lumberjacks nearby, go into berzerker mode
//            if (nearbyLumberJacks.length > 4 || berzerker) {
//                System.out.println("I AM BERZERK!");
//                berzerker = true;
//                explore();
//                return;
//            }
        }

        if (visibleHostiles.length > 0) {
            RobotInfo closestRobot = findClosestRobot(visibleHostiles);
            attackAndStrike(closestRobot, rc);
        } else {

            // If there is a robot, move towards it
            if (visibleHostiles.length > 0) {
                MapLocation myLocation = rc.getLocation();
                MapLocation enemyLocation = visibleHostiles[0].getLocation();
                Direction toEnemy = myLocation.directionTo(enemyLocation);
                // Move and chop toward enemy
                tryMoveChopDir(toEnemy, rc);
            } else {
                // Move and Chop Randomly
                tryMoveChopDir(RandomUtil.randomDirection(), rc);
            }
        }
    }

    private void tryMoveChopDir(Direction dir, RobotController rc) throws GameActionException {
	    final MapLocation myLocation = getCachedLocation();

	    // move toward and target trees.
        TreeInfo[] trees = rc.senseNearbyTrees(rc.getType().sensorRadius);
        if (trees.length > 0) {

            TreeInfo[] enemyNeutralTrees = Arrays.stream(trees).filter(x -> x.getTeam() != rc.getTeam()).toArray(TreeInfo[]::new);

            // Filter the trees down to only ones that are already damaged and don't belong to me.
            TreeInfo[] damagedTrees = Arrays.stream(enemyNeutralTrees).filter(x -> x.getHealth() < x.maxHealth).toArray(TreeInfo[]::new);

            if (damagedTrees.length > 0) {
                TreeInfo closestTree = findClosestTree(damagedTrees);
                if (closestTree != null) {
                    System.out.print("Found a damaged enemy/neutral tree");
	                dir = myLocation.directionTo(closestTree.location);
                    rc.setIndicatorLine(myLocation, closestTree.location, 255, 20, 0);
                }
            } else if (enemyNeutralTrees.length > 0) {
                TreeInfo closestTree = findClosestTree(enemyNeutralTrees);
                if (closestTree != null) {
                    System.out.print("Found an enemy/neutral tree");
                    dir = myLocation.directionTo(closestTree.location);
                    rc.setIndicatorLine(myLocation, closestTree.location, 155, 0, 0);
                }
            }
        }

        // get the location of where we're moving within strike radius
        MapLocation dirLoc = myLocation.add(dir,GameConstants.LUMBERJACK_STRIKE_RADIUS);

        // See if there's a tree within strike radius
        TreeInfo treeAhead = rc.senseTreeAtLocation(dirLoc);
        // First, try intended direction
        if (!rc.hasMoved() && rc.canMove(dir, 0.75f)) {
            System.out.print("DOING MOVE");
            rc.move(dir);
        }
        // If moving isn't possible, check for tree.
        else if (treeAhead != null && (treeAhead.team == Team.NEUTRAL || treeAhead.team == getEnemyTeam())) {
            // If we can shake the tree and it has bullets, lets get those out!
            if (rc.canShake(dirLoc) && treeAhead.containedBullets > 0) {
                System.out.print("SHAKIN' DAT TREE.  Previous Bullets: " + rc.getTeamBullets());
                rc.shake(dirLoc);
                System.out.print("SHOOK DAT TREE.  After Bullets: " + rc.getTeamBullets());
                rc.setIndicatorDot(dirLoc,0,155,155);
            }
            // else lets chop that sucka down.
            else if (rc.canChop(dirLoc)) {
                rc.chop(dirLoc);
                System.out.print("CHOPPED DAT TREE!");
                rc.setIndicatorDot(dirLoc,155,0,155);
            } else {
                // if we can't chop it, something went wrong... scramble
                explore();
            }
        } else {
            // Else trying moving randomly
            System.out.print("NO TREE! MOVING ON");
            explore();
        }
    }

    private void attackAndStrike(RobotInfo robot, RobotController rc) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        MapLocation enemyLocation = robot.getLocation();
        Direction toEnemy = myLocation.directionTo(enemyLocation);
        if (!rc.hasMoved()) {
            tryMove(toEnemy);
        }

        if (!rc.hasAttacked() && rc.canStrike()) {
            rc.strike();
        }

    }

    private void explore() throws GameActionException {
        RobotController rc = getRc();

        if (rc.hasMoved()) {
            return;
        }

        System.out.println("Exploring in a random direction: " + exploreDirection);
        if (!tryMove(exploreDirection)) {
            exploreDirection = exploreDirection.rotateLeftDegrees(90);
            tryMove(exploreDirection);
        }

    }
}
