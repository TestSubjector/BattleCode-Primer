package SloBro;
import battlecode.common.*;

public class Util {	
    /**
     * Returns a random Direction
     * @return a random Direction
     */
    public static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

    // Needs to be implemented 
    public static float findDistance(MapLocation location1, MapLocation location2) {
    	return 0;
    }
    
    // Needs to be implemented 
    public static Direction getDirectionToLocation(RobotController rc, MapLocation location) {
    	return rc.getLocation().directionTo(location);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    public static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
        return tryMove(rc, dir,20,3);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    public static boolean tryMove(RobotController rc, Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
    	if(rc.hasMoved())
    		return false;
    	
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        //boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }
    
    public static boolean tryMove(RobotController rc, MapLocation location, float stride) throws GameActionException {
    	if (location.distanceTo(rc.getLocation()) < stride) {
    		rc.move(location);
    	} else {
    		return tryMove(rc, rc.getLocation().directionTo(location));
    	}
    	return true; 
    }

    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    public static boolean willCollideWithMe(RobotController rc, BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }
    
    public static boolean isWithinDistanceToSide(RobotController rc, int distance) throws GameActionException{
    	return !(rc.canMove(Direction.getWest(), distance) && rc.canMove(Direction.getSouth(), distance) && rc.canMove(Direction.getNorth(), distance) && rc.canMove(Direction.getEast(), distance));    	
    }
    
    public static boolean pathClearTo(MapLocation loc)
    {
    	RobotInfo[] allies = RobotPlayer.rc.senseNearbyRobots(-1, RobotPlayer.rc.getTeam());
    	TreeInfo[] trees = RobotPlayer.rc.senseNearbyTrees();
    	MapLocation myLoc = RobotPlayer.rc.getLocation();
    	
    	// Check for allies in the way
    	for(int i = 0; i < allies.length; i++)
    	{
    		if(bulletWouldHit(allies[i], myLoc, loc))
    			return false;
    	}
    	
    	// Check for trees in the way
    	for(int i = 0; i < trees.length; i++)
    	{
    		if(bulletWouldHit(trees[i], myLoc, loc))
    			return false;
    	}
    	return true;
    }
    
    
    static boolean bulletWouldHit(RobotInfo r, MapLocation from, MapLocation to)
    {
    	Direction dirToRobot = from.directionTo(r.location);
    	Direction dirToTarget = from.directionTo(to);
    	float distToRobot = from.distanceTo(r.location);
    	float theta = dirToTarget.radiansBetween(dirToRobot);
    	
    	
    	// Check if bullet is moving towards robot
    	if(Math.abs(theta) >= Math.PI / 2)
    		return false;
    	
    	float perpendicularDistance = (float) Math.abs(distToRobot * Math.sin(theta));
    	
    	return (perpendicularDistance <= r.getType().bodyRadius);
    }
    
    static boolean bulletWouldHit(TreeInfo t, MapLocation from, MapLocation to)
    {
    	Direction dirToTree = from.directionTo(t.location);
    	Direction dirToTarget = from.directionTo(to);
    	float distToTree = from.distanceTo(t.location);
    	float theta = dirToTarget.radiansBetween(dirToTree);
    	
    	
    	// Check if bullet is moving towards robot
    	if(Math.abs(theta) >= Math.PI / 2)
    		return false;
    	
    	float perpendicularDistance = (float) Math.abs(distToTree * Math.sin(theta));
    	
    	return (perpendicularDistance <= t.radius);
    }
    
    public static boolean nearLocation(MapLocation l)
    {
    	return RobotPlayer.rc.getLocation().distanceTo(l) < RobotPlayer.rc.getType().sensorRadius - .5;
    }
    
    public static void printMapList(MapLocation[] locs) throws GameActionException {    	
    	System.out.println("Printing Map Locations....");
    	for (int i = 0; i < locs.length; i++) {
    		System.out.println(locs[i].x + " " + locs[i].y);
    	}
    }
    
}
