package ebotplayer.units;

import battlecode.common.*;
import ebotplayer.units.base.Unit;
import ebotplayer.util.*;
/**
 * Created by ebot on 1/10/17.
 */
public class Gardener extends Unit {
    private Direction[] plantD;
    private int numTrees;
    public Gardener(RobotController rc) {
        super(rc);
        plantD = new Direction[6]; //Oaks Disciples uses square 8 tree formation, possibility
        for (int i = 0 ; i < 6 ; i++) {
            plantD[i] = new Direction(0 + i * ((float)Math.PI / 3) );
        }
        while(true) {
            try {
                c.vp();
                u.sendAliveSignal();
                c.shake();
                if (u.unitCount(RobotType.SOLDIER) < 2 * u.unitCount(RobotType.GARDENER) || rc.getTeamBullets() > 500) {
                    bUnit(RobotType.SOLDIER);
                }
                if (u.unitCount(RobotType.SOLDIER) > 0) {
                    if (u.unitCount(RobotType.LUMBERJACK) < u.unitCount(RobotType.GARDENER) || rc.getTeamBullets() > 600) {
                        bUnit(RobotType.LUMBERJACK);
                    }
                    water();
                    numberOfTrees();
                    if (!nearbyGardener()) {
                        plant();
                    }
                }
                if (rc.senseNearbyTrees((float)1.1, rc.getTeam()).length < 1 || nearbyGardener()) {
                    m.wander(30, 12);
                }
                c.vpEnd();
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }
    private void plant() throws GameActionException {
        if (numTrees > 0) {
            for (Direction d : plantD) {
                if (rc.canPlantTree(d)) {
                    rc.plantTree(d);
                    break;
                }
            }
        }
    }
    private void numberOfTrees() throws GameActionException {
        numTrees = 0;
        for (Direction d : plantD) {
            if (rc.canPlantTree(d)) {
                numTrees++;
            }
        }
        if (numTrees > 0) {
            numTrees--;
        }
    }
    private void water() throws GameActionException {
        TreeInfo[] fTrees = rc.senseNearbyTrees(rc.getType().sensorRadius, rc.getTeam());
        if (fTrees.length > 0) {
            float lowestH = 50;
            int tID = 0;
            for(TreeInfo t : fTrees) {
                if (t.getHealth() < lowestH && rc.canWater(t.getID())) {
                    lowestH = t.getHealth();
                    tID = t.getID();
                }
            }
            if (rc.canWater(tID)) {
                rc.water(tID);
            }
        }
    }
    private void bUnit(RobotType type) throws GameActionException {
        Direction bDirection = Tools.randomDirection();
        if (rc.canBuildRobot(type, bDirection)) {
            rc.buildRobot(type, bDirection);
        }
    }
    private boolean nearbyGardener() throws GameActionException {
        RobotInfo[] gardeners = rc.senseNearbyRobots((float)5, rc.getTeam());
        if (gardeners.length > 0) {
            for(RobotInfo r : gardeners) {
                if (r.type == RobotType.GARDENER) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }
}

