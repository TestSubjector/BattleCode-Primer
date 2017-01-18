package potatobot;
import java.util.ArrayList;
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
	public static RobotInfo[] allies;
	public static RobotInfo[] enemies;
	public static TreeInfo[] neutralTrees;
	public static TreeInfo[] enemyTrees;
	public static int treesPlanted;
	public static boolean haveTarget;
	public static ArrayList<Integer> beenHere;
	public static final int TREE_CHANNEL = 64;
	public static final int BUILD_CHANNEL = 42;
	public static int numberOfArchons;
	public static final int ENEMY_ARCHON_CHANNELS[] = {43, 44, 45, 46, 47, 48, 49, 50, 51};
	/* The channels represent:
	 * 43 = Number of Enemy Archons detected till now
	 * 44 = ID of 1st detected Enemy Archon
	 * 45 = Last known (hashed) location of the 1st detected Enemy Archon
	 * 46 = ID of the 2nd detected Enemy Archon
	 * 47 = Last known (hashed) location of the 2nd detected Enemy Archon
	 * 48 = ID of the 3rd detected Enemy Archon
	 * 49 = Last known (hashed) location of the 3rd detected Enemy Archon
	 * 50 = Index (from 1 to 3) of the currently targeted Enemy Archon
	 * 51 = Round Number of the most recent encounter with an Enemy Archon
	 */
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
		numberOfArchons = theirInitialArchons.length;
		theirInitialArchonCentre = theirInitialArchons[0];
		beenHere = new ArrayList<Integer>(10);
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
		haveTarget = false;
	}
	
	public static void initRobotCountMax()
	{
		robotCountMax = new int[6];
		robotCountMax[RobotType.ARCHON.ordinal()] = 3;
		robotCountMax[RobotType.GARDENER.ordinal()] = 21;
		robotCountMax[RobotType.LUMBERJACK.ordinal()] = 10;
		robotCountMax[RobotType.SCOUT.ordinal()] = 25;
		robotCountMax[RobotType.SOLDIER.ordinal()] = 10;
		robotCountMax[RobotType.TANK.ordinal()] = 7;
	}
	
	public static int hashIt(MapLocation location)
	{
		int x = (int)Math.round(location.x);
		int y = (int)Math.round(location.y);
		int hashValue = ((10000) * x + y);
		return hashValue;
		
	}
	 
	public static MapLocation unHashIt(int x)
	{
		MapLocation location = new MapLocation((x / (10000)), (x % (10000)));
		return location;
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

	private static void updateEnemyArchons()throws GameActionException 
	{
		int numberOfArchonsFound = rc.readBroadcast(ENEMY_ARCHON_CHANNELS[0]);
		for (RobotInfo enemy : enemies)
		{
			if (enemy.getType() == RobotType.ARCHON)
			{
				rc.broadcast(ENEMY_ARCHON_CHANNELS[8], rc.getRoundNum());
				boolean found = false; // initial value
				for (int i = 1; i < numberOfArchonsFound * 2; i += 2)
				{
					int ID = rc.readBroadcast(ENEMY_ARCHON_CHANNELS[i]);
					if (enemy.getID() == ID)
					{
						found = true; // already seen this Archon
						break;
					}
				}
				if (!found)
				{
					int hashedLocation = hashIt(enemy.getLocation());
					rc.broadcast(ENEMY_ARCHON_CHANNELS[numberOfArchonsFound * 2 + 1], enemy.getID());
					rc.broadcast(ENEMY_ARCHON_CHANNELS[numberOfArchonsFound * 2 + 2], hashedLocation);
					numberOfArchonsFound++;
					rc.broadcast(ENEMY_ARCHON_CHANNELS[0], numberOfArchonsFound);
				}
			}
		}
		float minHealth = 500000;
		int minIndex = 0;
		for (int i = 1; i < numberOfArchonsFound * 2; i += 2)
		{
			int ID = rc.readBroadcast(ENEMY_ARCHON_CHANNELS[i]);
			if (rc.canSenseRobot(ID))
			{
				RobotInfo sensedRobot = rc.senseRobot(ID);
				int hashedLocation = hashIt(sensedRobot.getLocation());
				float healthRemaining = rc.getHealth();
				rc.broadcast(ENEMY_ARCHON_CHANNELS[i + 1], hashedLocation);
				if (healthRemaining < minHealth)
				{
					minHealth = healthRemaining;
					minIndex = (i + 1) / 2;
				}
			}
			if (minIndex != 0)
			{
				rc.broadcast(ENEMY_ARCHON_CHANNELS[7], minIndex);
			}
		}
		haveTarget = ((rc.readBroadcast(ENEMY_ARCHON_CHANNELS[7]) > 0) && ((rc.getRoundNum() - rc.readBroadcast(ENEMY_ARCHON_CHANNELS[8])) < 20));
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
				if (sideStep(bulletDirection))
				{
					return;
				}
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

	public static boolean tryToMove(Direction movingDirection)throws GameActionException
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
	
	public static boolean tryToMoveThisMuch(Direction movingDirection, float distance)throws GameActionException
	{
		if (rc.hasMoved())
		{
			return false;
		}
		for (int angle: tryAngles)
		{
			Direction candidateDirection = movingDirection.rotateLeftDegrees(angle);
			if (rc.canMove(candidateDirection, distance))
			{
				rc.move(candidateDirection, distance);
				updateLocation();
				return true;
			}
		}
		return false;
	}

	public static boolean tryToMoveTowards(MapLocation location)throws GameActionException
	{
		return tryToMove(here.directionTo(location));
	}
	
	public static void header()throws GameActionException
	{
		updateRobotCount();
		updateTreeCount();
		updateBulletCount();
		allies = rc.senseNearbyRobots(-1, us);
		enemies = rc.senseNearbyRobots(-1, them);
		neutralTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
		enemyTrees = rc.senseNearbyTrees(-1, them);
		if (dying())
		{
			imDying();
		}
		tryToDodge();
		updateEnemyArchons();
		for (TreeInfo tree : neutralTrees)
		{
			if (tree.getContainedBullets() > 0)
			{
				if (rc.canShake(tree.getID()))
				{
					rc.shake(tree.getID());
				}
				if (rc.getType() == RobotType.SCOUT)
				{
					tryToMoveTowards(tree.getLocation());
					footer();
					header();
				}
			}
		}
	}

	public static void footer()throws GameActionException
	{
		prevHealth = rc.getHealth();
		Clock.yield();
	}
}
