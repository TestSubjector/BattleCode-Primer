package potatobot;
import java.util.Arrays;

import battlecode.common.*;

public class Globals 
{
	public static RobotController rc;
	public static int roundNum;
	public static MapLocation here;
	public static int myID;
	public static RobotType myType;
	public static float prevHealth;
	public static Team us;
	public static Team them;
	public static MapLocation[] ourInitialArchons;
	public static MapLocation[] theirInitialArchons;
	public static MapLocation theirInitialArchonCentre;
	public static int[] robotCount;
	public static int[] robotCountMax;
	
	public static void init(RobotController rcinit)throws GameActionException
	{
		rc = rcinit;
		roundNum = 0;
		myID = rc.getID();
		myType = rc.getType();
		prevHealth = rc.getHealth();
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
		robotCount = new int[6];
		robotCountMax = new int[6];
		Arrays.fill(robotCountMax, 5);
	}
	
	public static void updateLocation()
	{
		here = rc.getLocation();
	}
	
	public static void updateRobotCount()throws GameActionException
	{
		for (int i = 1; i <= 5; i++)
		{
			robotCount[i] = rc.readBroadcast(i);
		}
	}
	
	public static Direction randomDirection() 
	{
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }
	
	public static void wander()throws GameActionException
	{
		int tries = 0;
		while (tries < 50)
		{
			Direction randomDir = randomDirection();
			if (rc.canMove(randomDir))
			{
				rc.move(randomDir);
				return;
			}
			tries++;
		}
	}
	
	public static boolean dying()throws GameActionException
	{
		float health = rc.getHealth();
		if (health < 5 && prevHealth >= 5)
		{
			return true;
		}
		return false;
	}
	
	public static void imDying()throws GameActionException
	{
		int robots = robotCount[myType.ordinal()];
		rc.broadcast(myType.ordinal(), robots - 1);
	}
}
