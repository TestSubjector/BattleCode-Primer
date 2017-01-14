package potatobot;

import battlecode.common.*;

public class Globals 
{
	public static RobotController rc;
	public static int roundNum;
	public static MapLocation here;
	public static int myID;
	public static RobotType myType;
	public static float bullets;
	public static float prevHealth;
	public static Team us;
	public static Team them;
	public static MapLocation[] ourInitialArchons;
	public static MapLocation[] theirInitialArchons;
	public static MapLocation theirInitialArchonCentre;
	public static int[] robotCount;
	public static int[] robotCountMax;
	public static int treesPlanted;
	public static final int TREE_CHANNEL = 64;
	public static final int tryAngles[] = {0, 10, -10, 20, -20, 30, -30, 40, -40, 45, -45};
	
	public static void init(RobotController rcinit)throws GameActionException
	{
		rc = rcinit;
		roundNum = 0;
		myID = rc.getID();
		myType = rc.getType();
		bullets = rc.getTeamBullets();
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
		initRobotCountMax();
		treesPlanted = 0;
	}
	
	public static void initRobotCountMax()
	{
		robotCountMax = new int[6];
		robotCountMax[RobotType.ARCHON.ordinal()] = 3;
		robotCountMax[RobotType.GARDENER.ordinal()] = 21;
		robotCountMax[RobotType.LUMBERJACK.ordinal()] = 10;
		robotCountMax[RobotType.SCOUT.ordinal()] = 10;
		robotCountMax[RobotType.SOLDIER.ordinal()] = 10;
		robotCountMax[RobotType.TANK.ordinal()] = 7;
	}
	
	public static void updateLocation()
	{
		here = rc.getLocation();
	}
	
	public static void updateBulletCount()
	{
		bullets = rc.getTeamBullets();
	}
	
	public static void updateTreeCount()throws GameActionException
	{
		treesPlanted = rc.readBroadcast(TREE_CHANNEL);
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
	
	public static boolean wander()throws GameActionException
	{
		if (rc.hasMoved())
		{
			return false;
		}
		int tries = 0;
		while (tries < 10)
		{
			Direction randomDir = randomDirection();
			if (rc.canMove(randomDir))
			{
				rc.move(randomDir);
				updateLocation();
				return true;
			}
			tries++;
		}
		return false;
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
	
	public static void trySingleShot(RobotInfo enemy)throws GameActionException
	{
		if (Clock.getBytecodesLeft() > (2 * myType.bytecodeLimit / 3) && rc.canFireSingleShot())
		{
			Direction shotDirection = here.directionTo(enemy.getLocation());
			boolean killingFriend = false;
			RobotInfo[] allies = rc.senseNearbyRobots(-1, us);
			for (RobotInfo ally : allies)
			{
				if (willHitRobot(ally, shotDirection, here))
				{
					killingFriend = true;
				}
			}
			if (!killingFriend)
			{
				rc.fireSingleShot(shotDirection);
			}
		}
	}
	
	public static boolean willHitRobot(RobotInfo robot, Direction shotDirection, MapLocation shotFrom) 
	{
		float distanceToCentre = shotFrom.distanceTo(robot.getLocation());
		Direction robotDirection = shotFrom.directionTo(robot.getLocation());
		float robotRadius = robot.getRadius();
		if ((distanceToCentre * Math.abs(Math.sin(shotDirection.radiansBetween(robotDirection)))) <= robotRadius)
		{
			return true;
		}
		return false;
	}

	public static void tryToDodge()throws GameActionException
	{
		BulletInfo[] sensedBullets = rc.senseNearbyBullets();
		RobotInfo me = new RobotInfo(myID, us, myType, here, rc.getHealth(), rc.getAttackCount(), rc.getMoveCount());
		for (BulletInfo sensedBullet : sensedBullets)
		{
			Direction bulletDirection = sensedBullet.getDir();
			MapLocation bulletLocation = sensedBullet.getLocation();
			if (willHitRobot(me, bulletDirection, bulletLocation))
			{
				sideStep(bulletDirection);
			}
		}
	}
	
	public static boolean sideStep(Direction bulletDirection)throws GameActionException
	{
		if (tryToMove(bulletDirection.rotateLeftDegrees(90)))
		{
			return true;
		}
		return tryToMove(bulletDirection.rotateRightDegrees(90));
	}

	private static boolean tryToMove(Direction movingDirection)throws GameActionException
	{
		if (rc.hasMoved())
		{
			return false;
		}
		for (int angle: tryAngles)
		{
			Direction candidateDirection = movingDirection.rotateLeftDegrees(angle);
			if (rc.canMove(candidateDirection))
			{
				rc.move(candidateDirection);
				updateLocation();
				return true;
			}
		}
		return false;
	}

	public static void header()throws GameActionException
	{
		updateRobotCount();
		updateTreeCount();
		updateBulletCount();
		if (dying())
		{
			imDying();
		}
		tryToDodge();
	}
	
	public static void footer()throws GameActionException
	{
		prevHealth = rc.getHealth();
		Clock.yield();
	}
}
