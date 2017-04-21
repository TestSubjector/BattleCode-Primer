package ebotplayer.util;

import battlecode.common.*;
/**
 * Created by ebot on 1/12/17.
 */
public class Common {
    private RobotController rc;
    public Common(RobotController rc) {
        this.rc = rc;
    }
    public Team enemy() {
        return rc.getTeam().opponent();
    }
    public void shake() throws GameActionException {
        TreeInfo[] trees = rc.senseNearbyTrees(rc.getType().sensorRadius, Team.NEUTRAL);
        if (trees.length > 0) {
            for (TreeInfo t : trees) {
                if (rc.canShake(t.location) && t.containedBullets > 0) {
                    rc.shake(t.location);
                    break;
                }
            }
        }
    }
    public void vp() throws GameActionException {
        if((rc.getTeamBullets() / rc.getVictoryPointCost()) + rc.getTeamVictoryPoints()  >= 10000) {
            rc.donate(rc.getTeamBullets());
        }
    }
    public void vpEnd() throws GameActionException {
        if(rc.getTeamBullets() > 300 || rc.getTeamVictoryPoints() > 600) {
            rc.donate( rc.getVictoryPointCost() );
        }
    }
}