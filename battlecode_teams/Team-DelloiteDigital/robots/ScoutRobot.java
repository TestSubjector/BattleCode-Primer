package p_patrick.robots;

import battlecode.common.*;
import ddframework.robots.SmartBaseRobot;
import ddframework.util.RandomUtil;

public class ScoutRobot extends SmartBaseRobot {

    static private Direction exploreDirection = RandomUtil.randomDirection();

    public ScoutRobot(RobotController controller) {
        super(controller);
    }

    @Override
    protected void onGameRound(RobotController rc) throws Exception {
        super.onGameRound(rc);

        treeShakeHop();

        boolean foundHighPriorityTarget = false;

        RobotInfo[] visibleHostiles = getCachedVisibleHostiles();
        if (visibleHostiles.length > 0) {
            for (RobotInfo robot : visibleHostiles) {

                // Move away from lumberjacks, soldiers, and tanks
                if (robot.getType() == RobotType.LUMBERJACK || robot.getType() == RobotType.SOLDIER || robot.getType() == RobotType.TANK) {
                    Direction away = new Direction(robot.getLocation(), getCachedLocation());
                    tryMove(away);
                }

                // search for all gardeners and prioritize attacking them first.
                if (robot.type == RobotType.GARDENER) {
                    attackAndFollow(robot);
                    foundHighPriorityTarget = true;
                }

            }

            // Attack all other enemies except for archons, because shooting archons is a waste of bullets
            if (!foundHighPriorityTarget) {
                for (RobotInfo robot : visibleHostiles) {
                    if (robot.type != RobotType.ARCHON) {
                        attackAndFollow(robot);
                        break;
                    }
                }
            }

        } else {
            // TODO: Check for global target using buffer instead of going random
            explore();
        }
    }

    private void treeShakeHop() throws GameActionException {
        RobotController rc = getRc();
        // If we've already moved this turn, don't continue with tree shaking to save byte count
        if (rc.hasMoved()) {
            return;
        }

        for (TreeInfo tree : getCachedVisibleTrees()) {
            if (tree.containedBullets > 0) {
                if (rc.canShake(tree.location) && tree.containedBullets > 0 && rc.canInteractWithTree(tree.getID())) {
                    System.out.print("SHAKIN' DAT TREE.  Pre-Shake Bullet Count: " + rc.getTeamBullets());
                    rc.shake(tree.location);
                    System.out.print("SHOOK DAT TREE.  Post-Shake Bullet Count: " + rc.getTeamBullets());
                    rc.setIndicatorDot(tree.location,0,155,155);
                } else {
                    Direction dir = getCachedLocation().directionTo(tree.location);
                    tryMove(dir);
                }
                return;
            }
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
