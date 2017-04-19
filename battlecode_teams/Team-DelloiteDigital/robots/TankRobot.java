package p_patrick.robots;

import battlecode.common.*;
import ddframework.robots.SmartBaseRobot;
import ddframework.util.RandomUtil;

public class TankRobot extends SmartBaseRobot {
    static private Direction exploreDirection;
    public TankRobot(RobotController controller) {
        super(controller);
    }

    @Override
    protected void onGameRound(RobotController rc) throws Exception {
        super.onGameRound(rc);

        MapLocation myLocation = rc.getLocation();

        // move directly away from the gardener that spawned us to get out of the way
        final RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getType() == RobotType.GARDENER) {
                exploreDirection = new Direction(robot.getLocation(), myLocation);
                break;
            }
        }

        // See if there are any nearby enemy robots
        RobotInfo[] visibleHostiles = getCachedVisibleHostiles();
        if (visibleHostiles.length > 0) {
            RobotInfo closest = findClosestRobot(visibleHostiles);
            fireMostPowerfulShotWeCanAfford(closest, 3);
        }

//        RobotInfo[] visibleFriendlies = getCachedVisibleFriendlies();
        boolean foundHighPriorityTarget = false;
        // Stay near gardeners to protect them
//        for (RobotInfo robot : visibleFriendlies) {
//            if (robot.type == RobotType.GARDENER) {
//                patrol(robot);
//                foundHighPriorityTarget = true;
//                System.out.println("FOUND GARDENER TO PROTECT");
//            }
//        }

        if (!foundHighPriorityTarget) {
            // Move randomly
            explore();
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
