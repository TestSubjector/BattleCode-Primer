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
	public static int victoryPoints;
	public static Direction movingDirection;
	public static int[] robotCount;
	public static int[] robotCountMax;
	public static RobotInfo[] allies;
	public static RobotInfo[] enemies;
	public static TreeInfo[] neutralTrees;
	public static TreeInfo[] enemyTrees;
	public static BulletInfo[] sensedBullets;
	public static int treesPlanted;
	public static boolean haveTarget;
	public static int lumberjackTarget;
	public static MapLocation lumberjackTargetLocation;
	public static int[] tryAngles;
	
	// Broadcast Channels
	public static int TREE_CHANNEL = 64;
	public static int BUILD_CHANNEL = 42;
	public static int VICTORY_CHANNEL = 69;
	
	public static int numberOfArchons;
	public static int[] ENEMY_ARCHONS_CHANNELS;
	
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
	
	public static int[] FARM_LOCATIONS_CHANNELS;
	/* The farm Locations channels represent:
	 * 666 = Number of farms till now
	 * 667 - 690 = (hashed) location of the nth farm centre
	 */
	
	public static int[] IMPORTANT_TREES_CHANNELS;
	/* The important trees channels represent:
	 * 101 = Number of important trees found till now
	 * {102, 103} - {500, 501} = {ID of nth detected important tree, (hashed) location of the nth detected tree}
	 */
	// End Broadcast Channels
	
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
		victoryPoints = 0;
		movingDirection = randomDirection();
		numberOfArchons = theirInitialArchons.length;
		theirInitialArchonCentre = theirInitialArchons[0];
		for (int i = 1; i < theirInitialArchons.length; i++)
		{
			theirInitialArchonCentre = new MapLocation(
					theirInitialArchonCentre.x + theirInitialArchons[i].x,
					theirInitialArchonCentre.y + theirInitialArchons[i].y
					);
		}
		theirInitialArchonCentre = new MapLocation(
				theirInitialArchonCentre.x / theirInitialArchons.length,
				theirInitialArchonCentre.y / theirInitialArchons.length
				);
		treesPlanted = 0;
		haveTarget = false;
		lumberjackTarget = -1;
		lumberjackTargetLocation = null;
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
		robotCount = new int[6];
		initRobotCountMax();
		initChannels();
	}
	
	public static void initRobotCountMax()
	{
		robotCountMax = new int[6];
		robotCountMax[RobotType.ARCHON.ordinal()] = 3;
		robotCountMax[RobotType.GARDENER.ordinal()] = 21;
		robotCountMax[RobotType.LUMBERJACK.ordinal()] = 18;
		robotCountMax[RobotType.SCOUT.ordinal()] = 25;
		robotCountMax[RobotType.SOLDIER.ordinal()] = 12;
		robotCountMax[RobotType.TANK.ordinal()] = 7;
	}
	
	public static void initChannels()
	{
		ENEMY_ARCHONS_CHANNELS = new int[9];
		for (int i = 43; i <= 51; i++)
		{
			ENEMY_ARCHONS_CHANNELS[i - 43] = i;
		}
		FARM_LOCATIONS_CHANNELS = new int[35];
		for (int i = 666; i <= 690; i++)
		{
			FARM_LOCATIONS_CHANNELS[i - 666] = i;
		}
		IMPORTANT_TREES_CHANNELS = new int[401];
		for (int i = 101; i <= 501; i++)
		{
			IMPORTANT_TREES_CHANNELS[i - 101] = i;
		}
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
	
	public static boolean dying()throws GameActionException
	{
		float health = rc.getHealth();
		if (health < 7 && prevHealth >= 7)
		{
			return true;
		}
		return false;
	}

	public static float bulletsRequiredToBuyVictoryPoints(int vp) 
	{
		return (vp * (7.5f + ((rc.getRoundNum() * 12.5f) / 3000f)));
	}

	public static int victoryPointsPurchasableWithBullets(float bullets) 
	{
		float vp = (bullets / (7.5f + ((rc.getRoundNum() * 12.5f) / 3000f)));
		return ((int)(Math.floor(vp)));
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

	public static void updateNearbyBullets()throws GameActionException
	{
		sensedBullets = rc.senseNearbyBullets();
	}
	
	public static void updateNearbyObjects()throws GameActionException
	{
		allies = rc.senseNearbyRobots(-1, us);
		enemies = rc.senseNearbyRobots(-1, them);
		neutralTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
		enemyTrees = rc.senseNearbyTrees(-1, them);
	}
	
	public static void doVictoryPointsCalculations()throws GameActionException
	{
		victoryPoints = rc.getTeamVictoryPoints();
		boolean weHaveWon = (rc.readBroadcast(VICTORY_CHANNEL) == 1);
		float gameProgressPercentage = (rc.getRoundNum() / 3000f);
		// float enemyVictoryProgress = ((float)rc.getOpponentVictoryPoints() / 1000f);
		if (!weHaveWon && victoryPoints + victoryPointsPurchasableWithBullets(bullets) >= 1001)
		{
			rc.donate(bullets - bulletsRequiredToBuyVictoryPoints(1));
			rc.broadcast(VICTORY_CHANNEL, 1);
		}
		else if (bullets > (150f / gameProgressPercentage))
		{
			float bulletsToSpend =  gameProgressPercentage * bullets;
			int vp = victoryPointsPurchasableWithBullets(bulletsToSpend);
			rc.donate(bulletsRequiredToBuyVictoryPoints(vp) + 0.01f);
		}
	}
	
	private static void updateEnemyArchons()throws GameActionException 
	{
		int numberOfArchonsFound = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[0]);
		for (RobotInfo enemy : enemies)
		{
			if (enemy.getType() == RobotType.ARCHON)
			{
				rc.broadcast(ENEMY_ARCHONS_CHANNELS[8], rc.getRoundNum());
				boolean found = false; // initial value
				for (int i = 1; i < numberOfArchonsFound * 2; i += 2)
				{
					int ID = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[i]);
					if (enemy.getID() == ID)
					{
						// already seen this Archon
						found = true; 
						break;
					}
				}
				if (!found)
				{
					int hashedLocation = hashIt(enemy.getLocation());
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[numberOfArchonsFound * 2 + 1], enemy.getID());
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[numberOfArchonsFound * 2 + 2], hashedLocation);
					numberOfArchonsFound++;
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[0], numberOfArchonsFound);
				}
			}
		}
		float minHealth = 500000;
		int minIndex = 0;
		for (int i = 1; i < numberOfArchonsFound * 2; i += 2)
		{
			int ID = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[i]);
			if (rc.canSenseRobot(ID))
			{
				RobotInfo sensedRobot = rc.senseRobot(ID);
				int hashedLocation = hashIt(sensedRobot.getLocation());
				float healthRemaining = rc.getHealth();
				rc.broadcast(ENEMY_ARCHONS_CHANNELS[i + 1], hashedLocation);
				if (healthRemaining < minHealth)
				{
					minHealth = healthRemaining;
					minIndex = (i + 1) / 2;
				}
			}
			if (minIndex != 0)
			{
				rc.broadcast(ENEMY_ARCHONS_CHANNELS[7], minIndex);
			}
		}
		haveTarget = ((rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[7]) > 0) && ((rc.getRoundNum() - rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[8])) < 10));
	}
	
	public static void updateTrees()throws GameActionException
	{
		for (TreeInfo tree : neutralTrees)
		{
			if (tree.getContainedBullets() > 0)
			{
				if (rc.canShake(tree.getID()))
				{
					rc.shake(tree.getID());
				}
			}
			if (tree.getContainedRobot() != null)
			{
				int numberOfTreesFound = rc.readBroadcast(IMPORTANT_TREES_CHANNELS[0]);
				if (numberOfTreesFound < 200)
				{
					boolean found = false; // initial value
					for (int i = 1; i < numberOfTreesFound * 2; i += 2)
					{
						int ID = rc.readBroadcast(IMPORTANT_TREES_CHANNELS[i]);
						if (myType == RobotType.LUMBERJACK)
						{
							MapLocation unhashedLocation = unhashIt(rc.readBroadcast(IMPORTANT_TREES_CHANNELS[i + 1]));
							if (lumberjackTarget == -1 || unhashedLocation.distanceTo(here) < lumberjackTargetLocation.distanceTo(here))
							{
								// this is the first target, or a closer one
								lumberjackTarget = ID;
								lumberjackTargetLocation = unhashedLocation;
							}
							if (tree.getID() == ID)
							{
								// already seen this tree
								found = true; 
							}
						}
						else
						{
							if (tree.getID() == ID)
							{
								// already seen this tree
								found = true; 
								break;
							}
						}
					}
					if (!found)
					{
						int hashedLocation = hashIt(tree.getLocation());
						rc.broadcast(IMPORTANT_TREES_CHANNELS[numberOfTreesFound * 2 + 1], tree.getID());
						rc.broadcast(IMPORTANT_TREES_CHANNELS[numberOfTreesFound * 2 + 2], hashedLocation);
						numberOfTreesFound++;
						rc.broadcast(IMPORTANT_TREES_CHANNELS[0], numberOfTreesFound);
					}
				}
				if (myType == RobotType.LUMBERJACK)
				{
					if (rc.canChop(tree.getID()))
					{
						rc.chop(tree.getID());
					}
					else
					{
						if (rc.canChop(neutralTrees[0].getID()))
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
	
	public static float arcTanAngle(RobotInfo enemy)throws GameActionException
	{
		float enemyRadius = enemy.getRadius();
		float distanceBetweenCentres = enemy.getLocation().distanceTo(here);
		float angle = ((float)(180/Math.PI) * (float)(Math.atan((enemyRadius / distanceBetweenCentres))));
		return angle;
	}
	
	public static boolean willHitRobot(RobotInfo robot, Direction shotDirection, MapLocation shotFrom) 
	{
		float distanceToCentre = shotFrom.distanceTo(robot.getLocation());
		Direction robotDirection = shotFrom.directionTo(robot.getLocation());
		float robotRadius = robot.getRadius();
		float tan = (float)Math.tan(Math.abs(shotDirection.radiansBetween(robotDirection)) - 0.01);
		float distanceFromCentre = (float) (distanceToCentre * tan);
		if (distanceFromCentre < robotRadius)
		{
			return true;
		}
		return false;
	}
	
	public static boolean trySingleShot(RobotInfo enemy)throws GameActionException
	{
		Direction directionToCentre = here.directionTo(enemy.getLocation());
		//float arcTanAngle = arcTanAngle(enemy);
		if (rc.canFireSingleShot())
		{
			Direction shotDirection = directionToCentre;
			// rc.setIndicatorLine(here, enemy.getLocation(), 0, 255, 0);
			boolean killingFriend = false;
			for (RobotInfo ally : allies)
			{
				if (willHitRobot(ally, shotDirection, here) && ally.getLocation().distanceTo(here) < enemy.getLocation().distanceTo(here))
				{
					killingFriend = true;
					break;
				}
			}
			if (!killingFriend)
			{
				rc.fireSingleShot(shotDirection);
				return true;
			}
		}
		return false;
	}
	
	public static boolean tryTriadShot(RobotInfo enemy)throws GameActionException
	{
		Direction directionToCentre = here.directionTo(enemy.getLocation());
		if (rc.canFireTriadShot())
		{
			rc.fireTriadShot(directionToCentre);
			return true;
		}
		return false;
	}
	
	public static boolean tryPentadShot(RobotInfo enemy)throws GameActionException
	{
		Direction directionToCentre = here.directionTo(enemy.getLocation());
		if (rc.canFirePentadShot())
		{
			rc.firePentadShot(directionToCentre);
			return true;
		}
		return false;
	}
	
	// Combat functions end here
	
	
	// Header to run at the start of each round
	
	public static void header()throws GameActionException
	{
		updateBulletCount();
		doVictoryPointsCalculations();
		updateRobotCount();
		updateNearbyObjects();
		if (dying())
		{
			imDying();
		}
		updateNearbyBullets();
		tryToDodge();
		updateEnemyArchons();
		updateTrees();
	}

	// Footer to run at the end of each round
	
	public static void footer()throws GameActionException
	{
		prevHealth = rc.getHealth();
		Clock.yield();
	}
}
