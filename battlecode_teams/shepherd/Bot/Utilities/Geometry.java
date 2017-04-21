package shepherd.Bot.Utilities;

import java.util.List;

import battlecode.common.BodyInfo;
import battlecode.common.BulletInfo;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TreeInfo;

public class Geometry {

    /* given a line, by two points L1 = (L1x, L1y) and L2 = (L2x, L2y) and a circle by center C = (Cx, Cy) and radius r,
     * calculates if the line intersects with the circle */
    public static boolean lineIntersectsCircle(float L1x, float L1y, float L2x, float L2y, float Cx, float Cy, float r) {
        if(L1x != L2x && L1y != L2y) {
            float m = (L2y - L1y) / (L2x - L1x);
            float b = -1*m * L2x + L2y;
            float x = (m*m + 1);
            float y = 2*(m*b - m*Cy - Cx);
            float z = Cy*Cy - r*r + Cx*Cx - 2*b*Cy + b*b;
            return y*y - 4*x*z >= 0;
        }
        else if(L1x == L2x && L1y != L2y) {
            float dx = (Cx - L1x < 0) ? (L1x - Cx) : (Cx - L1x);
            return dx < r;
        }
        else if(L1x != L2x && L1y == L2y) {
            float dy = (Cy - L1y < 0) ? (L1y - Cy) : (Cy - L1y);
            return dy < r;
        }
        else return (L1x - Cx)*(L1x - Cx) + (L1y - Cy)*(L1y - Cy) <= r*r;
    }


    /*
     * returns the distance between the borders of two given circles,
     * defined by their radii and centers,
     * or by body info
     */
    public static float distanceBetween(MapLocation centerA, float radiusA, MapLocation centerB, float radiusB) {
        if(centerA == null || centerB == null) return -1;
        return centerA.distanceTo(centerB) - radiusA - radiusB;
    }
    public static float distanceBetween(MapLocation center, float radius, BodyInfo body) {
        if(body == null) return -1;
        return distanceBetween(center, radius, body.getLocation(), body.getRadius());
    }
    public static float distanceBetween(BodyInfo body, MapLocation center, float radius) {
        return distanceBetween(center, radius, body);
    }
    public static float distanceBetween(BodyInfo bodyA, BodyInfo bodyB) {
        if(bodyA == null) return -1;
        return distanceBetween(bodyA.getLocation(), bodyA.getRadius(), bodyB);
    }


    /*
     * returns the center of two given locations
     */
    public static MapLocation centerOf(MapLocation... locations) {
    	float x = 0, y = 0;
    	for(MapLocation loc : locations) {
    		x += loc.x; y += loc.y;
    	}
    	return new MapLocation(x/locations.length, y/locations.length);
    }

    /*
     * returns aaverage direction from given directions
     */
    public static Direction average(Direction... dirs) {
    	float dx = 0, dy = 0, count = 0;
    	Direction dir;
    	for(int i = dirs.length; --i>=0;) {
    		dir = dirs[i];
    		if(dir != null) {
    			dx += dir.getDeltaX(1);
    			dy += dir.getDeltaY(1);
    			count++;
    		}
    	}
    	if(count == 0) return null;
    	return new Direction(dx/count, dy/count);
    }


    /*
     * returns the nearest thing from given list or array to the relative location
     */
    public static MapLocation getNearest(List<MapLocation> locations, MapLocation relativeLocation) throws GameActionException {
        float minDist = Float.MAX_VALUE;
        MapLocation nearest = null;
        for(int index = 0; index < locations.size(); index++) {
            MapLocation loc = locations.get(index);
            float dist = relativeLocation.distanceTo(loc);
            if(dist < minDist) {
                nearest = loc;
                minDist = dist;
            }
        }
        return nearest;
    }
    public static TreeInfo getNearest(TreeInfo[] trees, MapLocation relativeLocation) throws GameActionException {
        float minDist = Float.MAX_VALUE;
        TreeInfo nearest = null;
        for(TreeInfo tree : trees) {
            float dist = relativeLocation.distanceTo(tree.location);
            if(dist < minDist) {
                nearest = tree;
                minDist = dist;
            }
        }
        return nearest;
    }
    public static MapLocation getNearest(MapLocation[] locations, MapLocation relativeLocation) throws GameActionException {
        float minDist = Float.MAX_VALUE;
        MapLocation nearest = null;
        for(MapLocation loc : locations) {
            float dist = relativeLocation.distanceTo(loc);
            if(dist < minDist) {
                nearest = loc;
                minDist = dist;
            }
        }
        return nearest;
    }



    /*
     * calculates the intersection points of bullet's path and given circle.
     *
     * @param:
     * BulletInfo bullet - the bullet to check
     * MapLocation C - center of circle to check against
     * float r - radius of circle to check against
     *
     * or
     *
     * BulletInfo bullet - the bullet to check
     * BodyInfo body - the body to check against
     *
     * or
     *
     * BulletInfo bullet - the bullet to check
     * RobotController rc - robot controller's body to check against
     *
     * @Bytecode Costs: 100
     */
    public static MapLocation[] intersectionPoints(BulletInfo bullet, MapLocation C, float r) {
    	float[] lambda = intersectionLambda(bullet, C, r);
    	if(lambda == null) return new MapLocation[0];

    	float t1 = lambda[0], t2 = lambda[1];
    	int numSolutions = 0;
    	if(t1 >= 0) numSolutions++;
    	if(t2 >= 0) numSolutions++;
    	if(numSolutions == 0) return new MapLocation[0];

    	MapLocation[] intersections = new MapLocation[numSolutions];
    	if(t1 >= 0 && t2 < 0) intersections[0] = bullet.getLocation().add(bullet.getDir(), t1);
    	else if(t1 < 0 && t2 >= 0) intersections[0] = bullet.getLocation().add(bullet.getDir(), t2);
    	else {
    		intersections[0] = bullet.getLocation().add(bullet.getDir(), t1);
    		intersections[1] = bullet.getLocation().add(bullet.getDir(), t2);
    	}

    	return intersections;
    }
    public static MapLocation[] intersectionPoints(BulletInfo bullet, BodyInfo body) {
    	return intersectionPoints(bullet, body.getLocation(), body.getRadius());
    }
    public static MapLocation[] intersectionPoints(BulletInfo bullet, RobotController rc) {
    	return intersectionPoints(bullet, rc.getLocation(), rc.getType().bodyRadius);
    }
    private static float[] intersectionLambda(BulletInfo bullet, MapLocation C, float r) {
    	float dx = bullet.getDir().getDeltaX(1), dy = bullet.getDir().getDeltaY(1);
    	float x = bullet.getLocation().x - C.x, y = bullet.getLocation().y - C.y;
    	float a = (dx*dx + dy*dy), b = (2*x*dx + 2*y*dy), c = (x*x + y*y - r*r);
    	float p = (b/a), q = (c/a);
    	float z = (p/2);
    	float u = z*z - q;
    	if(u < 0) return null;
    	float s = (float) Math.sqrt(u);

    	float t1 = (s - z), t2 = (-1)*(s + z);
    	return new float[]{t1, t2};
    }



    public static Direction getPerpendicularDirection(Direction dir) {
    	return dir.rotateLeftDegrees(90);
    }
    public static Direction getPerpendicularAwayFromBullet(BulletInfo bullet, MapLocation loc) {
    	Direction dir = bullet.getDir().rotateLeftDegrees(90);
    	float currDist = loc.distanceTo(bullet.getLocation());
    	float nextDist = loc.add(dir).distanceTo(bullet.getLocation());
    	return (currDist < nextDist) ? dir : dir.opposite();
    }


}
