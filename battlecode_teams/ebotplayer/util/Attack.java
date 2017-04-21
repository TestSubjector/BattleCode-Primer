package ebotplayer.util;

import battlecode.common.*;
/**
 * Created by ebot on 1/12/17.
 */
public class Attack {
    private RobotController rc;
    private Common c;
    private Direction lastDir;
    public Attack(RobotController rc) {
        this.rc = rc;
        c = new Common(rc);
    }
    public void bullet() throws GameActionException { //TODO anti-friendly fire doesnt work
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().sensorRadius, c.enemy());
       // RobotInfo[] friends = rc.senseNearbyRobots(rc.getType().sensorRadius, rc.getTeam());
        if (robots.length > 0) {
            lastDir = rc.getLocation().directionTo(robots[0].location);
            for (RobotInfo r : robots) {
                //for (RobotInfo f : friends) {
                  //  if (rc.getLocation().directionTo(r.location) != rc.getLocation().directionTo(f.location)) {
                        if (rc.canFirePentadShot() && rc.getTeamBullets() > 100) {
                            rc.firePentadShot(rc.getLocation().directionTo(r.location));
                        } else if (rc.canFireTriadShot() && rc.getTeamBullets() > 50) {
                            rc.fireTriadShot(rc.getLocation().directionTo(r.location));
                        } else if (rc.canFireSingleShot()) {
                            rc.fireSingleShot(rc.getLocation().directionTo(r.location));
                        }
                  //  }
              //  }
            }
        }
    }
    public Direction getLastDir() {
        return lastDir;
    }
}