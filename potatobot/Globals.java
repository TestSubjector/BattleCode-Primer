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
	public static Direction movingDirection;
	public static int[] robotCount;
	public static int[] robotCountMax;
	public static RobotInfo[] allies;
	public static RobotInfo[] enemies;
	public static TreeInfo[] neutralTrees;
	public static TreeInfo[] enemyTrees;
	public static int treesPlanted;
	public static boolean haveTarget;
	public static ArrayList<Integer> beenHere;
	public static int[] tryAngles;
	public static final int TREE_CHANNEL = 64;
	public static final int BUILD_CHANNEL = 42;
	public static int numberOfArchons;
	public static final int[] ENEMY_ARCHON_CHANNELS = {43,
			44,
			45,
			46,
			47,
			48,
			49,
			50,
			51
			};
	/* The Enemy Archon channels represent:
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
	public static final int[] FARM_LOCATIONS_CHANNELS = {666,
			667, 
			668, 
			669, 
			670, 
			671, 
			672, 
			673, 
			674, 
			675, 
			676, 
			677, 
			678, 
			679, 
			680, 
			681, 
			682, 
			683, 
			684, 
			685, 
			686, 
			687, 
			688, 
			689, 
			690
			};
	/* The Farm Locations channels represent:
	 * 666 = Number of farms till now
	 * 667 - 690 = (hashed) location of the nth farm centre
	 */
	public static final int hasher = 100000;
	
	// Initialization functions start here
	
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
		movingDirection = randomDirection();
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
		tryAngles = new int[91];
		for (int i = 0; i < 91; i++)
		{
			if (i % 2 == 0)
			{
				tryAngles[i] = i / 2;
			}
			else
			{
				tryAngles[i] = -((i + 1) / 2);
			}
		}
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
	
	// Initialization functions end here
	
	
	// Utility functions start here
	
	public static int hashIt(MapLocation location)
	{
		int x = (int)Math.round(location.x * 10);
		int y = (int)Math.round(location.y * 10);
		int hashValue = ((hasher) * x + y);
		return hashValue;
	}
	 
	public static MapLocation unhashIt(int h)
	{
		float x10 = h / hasher;
		float y10 = h % hasher;
		float x = x10 / 10.0f;
		float y = y10 / 10.0f;
		MapLocation location = new MapLocation(x, y);
		return location;
	}

	public static Direction randomDirection()
	{
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

	public static boolean isClear(MapLocation location, float radius)throws GameActionException
	{
		if (!rc.isCircleOccupiedExceptByThisRobot(location, radius) && rc.onTheMap(location, radius))
		{
			return true;
		}
		return false;
	}
	
	public static boolean isInAFarmingArea(MapLocation location)throws GameActionException
	{
		/* Function useless as of now
		
		float x = location.x;		 
		float y = location.y;
		int numberOfFarms = rc.readBroadcast(FARM_LOCATIONS_CHANNELS[0]);
		for (int i = 1; i <= numberOfFarms; i++)
		{
			int farmHashedLocation = rc.readBroadcast(FARM_LOCATIONS_CHANNELS[i]);
			MapLocation farmUnhashedLocation = unhashIt(farmHashedLocation);
			float farmCentreX = farmUnhashedLocation.x;
			float farmCentreY = farmUnhashedLocation.y;
			if ((Math.abs(farmCentreX - x) < 3.1) || (Math.abs(farmCentreY - y) < 3.1))
			{
				return true;
			}
		}
		*/
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
	
	// Utility functions end here
	
	
	// Updation functions start here
	
	public static void updateLocation()
	{
		here = rc.getLocation();
	}
	
	public static void imDying()throws GameActionException
	{
		int robots = robotCount[myType.ordinal()];
		rc.broadcast(myType.ordinal(), robots - 1);
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
	
	// Updation functions end here
	
	
	// Movement functions start here
	
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
				MapLocation candidateLocation = here.add(candidateDirection, rc.getType().strideRadius + 0.05f);
				if (!isInAFarmingArea(candidateLocation))
				{
					rc.move(candidateDirection);
					updateLocation();
					return true;
				}
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
	
	public static boolean sideStep(Direction bulletDirection)throws GameActionException
	{
		if (tryToMove(bulletDirection.rotateLeftDegrees(90)))
		{
			return true;
		}
		return tryToMove(bulletDirection.rotateRightDegrees(90));
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
			Direction randomDirection = randomDirection();
			tryToMove(randomDirection);
			tries++;
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
	
	// Movement functions end here
	
	
	// Combat functions start here
	
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
	
	public static void trySingleShot(RobotInfo enemy)throws GameActionException
	{
		if (rc.canFireSingleShot())
		{
			Direction shotDirection = here.directionTo(enemy.getLocation());
			boolean killingFriend = false;
			for (RobotInfo ally : allies)
			{
				if (willHitRobot(ally, shotDirection, here) && ally.getLocation().distanceTo(here) < enemy.getLocation().distanceTo(here))
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
	
	// Combat functions end here
	
	
	// Header to run at the start of each round
	
	public static void header()throws GameActionException
	{
		updateRobotCount();
		updateTreeCount();
		updateBulletCount();
		if (bullets >= 10000)
		{
			rc.donate(10000);
		}
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
			if (tree.getContainedRobot() != null)
			{
				if (rc.getType() == RobotType.LUMBERJACK)
				{
					if (rc.canChop(tree.getID()))
					{
						rc.chop(tree.getID());
					}
					else
					{
						if (tryToMoveTowards(tree.getLocation()))
						{
							footer();
							header();
						}
						else if (rc.canChop(neutralTrees[0].getID()))
						{
							rc.chop(neutralTrees[0].getID());
						}
						else if (enemyTrees.length != 0)
						{
							if (rc.canChop(enemyTrees[0].getID()))
							{
								rc.chop(enemyTrees[0].getID());
							}
						}
					}
				}
			}
		}
	}
	
	
	// Footer to run at the end of each round
	
	public static void footer()throws GameActionException
	{
		prevHealth = rc.getHealth();
		Clock.yield();
	}
}
