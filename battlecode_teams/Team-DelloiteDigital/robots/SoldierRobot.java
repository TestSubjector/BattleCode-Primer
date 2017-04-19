package p_patrick.robots;

import battlecode.common.*;
import ddframework.robots.SmartBaseRobot;
import ddframework.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SoldierRobot extends SmartBaseRobot {
    static private Direction exploreDirection;
    static private boolean berzerker = false;

    public SoldierRobot(RobotController controller) {
        super(controller);

        MapLocation myLocation = controller.getLocation();

        // move directly away from the gardener that spawned us to get out of the way
        final RobotInfo[] nearbyRobots = controller.senseNearbyRobots();
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getType() == RobotType.GARDENER) {
                exploreDirection = new Direction(robot.getLocation(), myLocation);
                break;
            }
        }

        // no Gardener nearby, (very improbable) so pick a random direction
        if (exploreDirection == null) {
            exploreDirection = RandomUtil.randomDirection();
        }

    }

    @Override
    protected void onGameRound(RobotController rc) throws Exception {
	    super.onGameRound(rc);

	    RobotInfo[] visibleHostiles = getCachedVisibleHostiles();
	    RobotInfo[] visibleFriendlies = getCachedVisibleFriendlies();

        boolean foundHighPriorityTarget = false;
        List<RobotInfo> nearbySoldiersArray = new ArrayList<>();
        if (visibleFriendlies.length > 0) {

            for (RobotInfo robot: visibleFriendlies) {
                if (robot.type == RobotType.SOLDIER) {
                    nearbySoldiersArray.add(robot);
                }
            }
            // If there are lots of soldiers nearby, lets form a squadron and go explore.
            // TODO: be smarter about targeting.
            RobotInfo[] nearbySoldiers = nearbySoldiersArray.toArray(new RobotInfo[nearbySoldiersArray.size()]);

            // If there are lots of soldiers nearby, go into berzerker mode
            if (nearbySoldiers.length > 7 || berzerker) {
                System.out.println("I AM BERZERK!");
                berzerker = true;
                explore();
            }

            if (!berzerker) {
                // Stay near gardeners to protect them
                for (RobotInfo robot : visibleFriendlies) {
                    if (robot.type == RobotType.GARDENER) {
                        patrol(robot);
                        foundHighPriorityTarget = true;
                        System.out.println("FOUND GARDENER TO PROTECT");
                    }
                }

                if (!foundHighPriorityTarget) {
                    System.out.println("DID NOT FIND HIGH PRIORITY TARGET TO PROTECT");
                    explore();
                }
            }


        } else {
            System.out.println("DID NOT FIND HIGH PRIORITY TARGET TO PROTECT");
            explore();
        }

        // If there are any nearby enemy robots
        if (visibleHostiles.length > 0) {
            RobotInfo closest = findClosestRobot(visibleHostiles);
            Direction away = null;
            for (RobotInfo robot : visibleHostiles) {
                if (robot.getType() == RobotType.LUMBERJACK || robot.getType() == RobotType.SOLDIER || robot.getType() == RobotType.TANK) {
                    away = new Direction(robot.getLocation(), getCachedLocation());

                    break;
                }
            }

            fireMostPowerfulShotWeCanAfford(closest, 3);

            if (away != null) {
                tryMove(away);
            } else {
                explore();
            }

        } else {
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
