package potatobot;
import battlecode.common.*;

public class Globals 
{
	public static RobotController rc;
	public static MapLocation here;
	public static int myID;
	public static RobotType myType;
	public static Team us;
	public static Team them;
	public static MapLocation[] ourInitialArchons;
	public static MapLocation[] theirInitialArchons;
	public static MapLocation theirInitialArchonCentre;
	
	public static void init(RobotController rcinit)throws GameActionException
	{
		rc = rcinit;
		myID = rc.getID();
		myType = rc.getType();
		us = rc.getTeam();
		them = us.opponent();
		ourInitialArchons = rc.getInitialArchonLocations(us);
		theirInitialArchons = rc.getInitialArchonLocations(them);
		theirInitialArchonCentre = theirInitialArchons[0];
		int n = theirInitialArchons.length;
		for (int i = 1; i < n; i++)
		{
			theirInitialArchonCentre = new MapLocation(
					theirInitialArchonCentre.x + theirInitialArchons[i].x,
					theirInitialArchonCentre.y + theirInitialArchons[i].y
					);
		}
		theirInitialArchonCentre = new MapLocation(
				theirInitialArchonCentre.x / n,
				theirInitialArchonCentre.y / n
				);
	}
	
	static void updateLocation()
	{
		here = rc.getLocation();
	}
	
	static Direction randomDirection() 
	{
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }
}
