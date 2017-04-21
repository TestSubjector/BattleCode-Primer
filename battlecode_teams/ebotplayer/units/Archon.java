package ebotplayer.units;

import battlecode.common.*;
import ebotplayer.units.base.Unit;
import ebotplayer.util.*;
/**
 * Created by ebot on 1/10/17.
 */
public class Archon extends Unit {
    private int gards;
    public Archon(RobotController rc) {
        super(rc);
        while(true) {
            try {

                c.vp();
                c.shake();
                hGardener();
                m.wander(30, 12);
                c.vpEnd();
                u.clearAll();
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }
    private void hGardener() throws GameActionException {
        gardCount();
        if (rc.hasRobotBuildRequirements(RobotType.GARDENER) && u.unitCount(RobotType.GARDENER) < gards) {
            Direction bDirection = Tools.randomDirection();
            int i = 0;
            while (!rc.canHireGardener(bDirection) && i < 3) {
                bDirection = Tools.randomDirection();
                i++;
            } if (rc.canHireGardener(bDirection)) {
                rc.hireGardener(bDirection);
            }
        }
    }
    private void gardCount() throws GameActionException { //help determining good gardeners counts at bullet levels, my #'s are pretty random
        if (rc.getTeamBullets() < 100) {
            gards = 2;
        } else if (rc.getTeamBullets() < 200) {
            gards = 4;
        } else if (rc.getTeamBullets() < 400){
            gards = 10;
        } else {
            gards = 50;
        }
    }
}
