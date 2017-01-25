package potatobot;
import battlecode.common.*;
import java.util.HashMap;

public class Globals 
{
	public static RobotController rc;
	public static int roundNum;
	public static float gameProgressPercentage;
	public static MapLocation here;
	public static int myID;
	public static RobotType myType;
	public static float bullets;
	public static float prevHealth;
	public static Team us;
	public static Team them;
	public static MapLocation[] ourInitialArchons;
	public static MapLocation[] theirInitialArchons;
	public static int numberOfArchons;
	public static int victoryPoints;
	public static Direction movingDirection;
	public static int[] robotCount;
	public static int scouts;
	public static int lumberjacks;
	public static int soldiers;
	public static int tanks;
	public static int gardeners;
	public static int farmers;
	public static float nonAllyTreeArea;
	public static float nonAllyTreeDensity;
	public static RobotInfo[] allies;
	public static RobotInfo[] enemies;
	public static TreeInfo[] neutralTrees;
	public static TreeInfo[] enemyTrees;
	public static BulletInfo[] sensedBullets;
	public static int treesPlanted;
	public static int lumberjackTarget;
	public static MapLocation lumberjackTargetLocation;
	public static boolean amFarmer;
	public static int[] tryAngles;
	public static HashMap<Integer, Integer> seenEnemyGardeners;
	public static HashMap<Integer, Integer> seenEnemyArchons;
	
	// Broadcast Channels
	public static int TREE_CHANNEL = 64;
	
	public static int GARDENER_NUMBER_CHANNEL = 67;
	
	public static int VICTORY_CHANNEL = 69;
	
	public static int[] ENEMY_ARCHONS_CHANNELS;
	
	/* The enemy archon channels represent:
	 * 43 = Number of Enemy Archons seen
	 * 44 = ID of 1st detected Enemy Archon
	 * 45 = Last known (hashed) location of the 1st detected Enemy Archon
	 * 46 = ID of the 2nd detected Enemy Archon
	 * 47 = Last known (hashed) location of the 2nd detected Enemy Archon
	 * 48 = ID of the 3rd detected Enemy Archon
	 * 49 = Last known (hashed) location of the 3rd detected Enemy Archon
	 * 50 = Index (from 1 to 3) of the currently targeted Enemy Archon
	 * 51 = Round Number of the most recent encounter with an Enemy Archon
	 */

	public static int[] IMPORTANT_TREES_CHANNELS;
	/* The important trees channels represent:
	 * 100 = Number of important trees seen
	 * {101, 102} - {499, 500} = {ID of nth detected important tree, (hashed) location of the nth detected tree}
	 */
	
	public static int[] FARM_LOCATIONS_CHANNELS;
	/* The farm locations channels represent:
	 * 666 = Number of farms made
	 * 667 - 690 = (hashed) location of the nth farm centre
	 */
	
	public static int[] ENEMY_GARDENERS_CHANNELS;
	/* The gardener locations channels represent:
	 * 700 = Number of Enemy Gardeners seen
	 * {701, 702} - {899, 900} = {ID of nth detected Enemy Gardener, (hashed) location of the nth detected Enemy Gardener}
	 * 901 = Index (from 1 to 100) of the currently targeted Enemy Gardener
	 * 902 = Round Number of the most recent encounter with an Enemy Gardener
	 */
	
	// End Broadcast Channels
	
	public static final int hasher = 100000;
	public static final int farmerIndex = 7;
	public static final double maxHitAngle = Math.atan(2.0f / 3.0f);
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
		robotCount = new int[8];
		updateRobotCount();
		updateNonAllyTreeDensity();
		numberOfArchons = theirInitialArchons.length;
		treesPlanted = 0;
		lumberjackTarget = -1;
		lumberjackTargetLocation = null;
		initTryAngles();
		seenEnemyGardeners = new HashMap<Integer, Integer>();
		seenEnemyArchons = new HashMap<Integer, Integer>();
		initChannels();
	}

	public static void robotInit(RobotType type)throws GameActionException
	{
		updateRobotCount();
		int robotsOfThisType = robotCount[type.ordinal()];
		rc.broadcast(type.ordinal(), robotsOfThisType + 1);
	}
	
	public static void initTryAngles()
	{
		tryAngles = new int[181];
		for (int i = 0; i < 181; i++)
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
	
	public static void initChannels()
	{
		ENEMY_GARDENERS_CHANNELS = new int[203];
		for (int i = 700; i <= 902; i++)
		{
			ENEMY_GARDENERS_CHANNELS[i - 700] = i;
		}
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
		for (int i = 100; i <= 500; i++)
		{
			IMPORTANT_TREES_CHANNELS[i - 100] = i;
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
		return (vp * (7.5f + ((roundNum * 12.5f) / 3000f)));
	}

	public static int victoryPointsPurchasableWithBullets(float bullets) 
	{
		float vp = (bullets / (7.5f + ((roundNum * 12.5f) / 3000f)));
		return ((int)(Math.floor(vp)));
	}
	
	// Utility functions end here
	
	
	// Updation functions start here
	
	public static void updateRoundNum()
	{
		roundNum = rc.getRoundNum();
	}
	
	public static void updateLocation()
	{
		here = rc.getLocation();
	}
	
	public static void imDying()throws GameActionException
	{
		int robotsOfMyType = robotCount[myType.ordinal()];
		rc.broadcast(myType.ordinal(), robotsOfMyType - 1);
		if (myType == RobotType.GARDENER && amFarmer)
		{
			robotsOfMyType = robotCount[farmerIndex];
			rc.broadcast(farmerIndex, robotsOfMyType - 1);
		}
	}
	
	public static void updateBulletCount()
	{
		bullets = rc.getTeamBullets();
	}
	
	public static void updateTreeCount()throws GameActionException
	{
		treesPlanted = rc.readBroadcast(TREE_CHANNEL);
	}

	private static void updateNonAllyTreeDensity()
	{
		nonAllyTreeDensity = (nonAllyTreeArea / myType.sensorRadius);
	}
	
	public static void updateRobotCount()throws GameActionException
	{
		for (int i = 1; i <= 7; i++)
		{
			robotCount[i] = rc.readBroadcast(i);
		}
		scouts = robotCount[RobotType.SCOUT.ordinal()];
		lumberjacks = robotCount[RobotType.LUMBERJACK.ordinal()];
		soldiers = robotCount[RobotType.SOLDIER.ordinal()];
		tanks = robotCount[RobotType.TANK.ordinal()];
		gardeners = robotCount[RobotType.GARDENER.ordinal()];
		farmers = robotCount[farmerIndex];
	}

	public static void updateNearbyBullets()throws GameActionException
	{
		sensedBullets = rc.senseNearbyBullets();
	}
	
	public static void updateNearbyObjectLocations()throws GameActionException
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
		gameProgressPercentage = (roundNum / 3000f);
		// float enemyVictoryProgress = ((float)rc.getOpponentVictoryPoints() / 1000f);
		if (!weHaveWon && victoryPoints + victoryPointsPurchasableWithBullets(bullets) >= 1001)
		{
			rc.donate(bullets - bulletsRequiredToBuyVictoryPoints(1));
			rc.broadcast(VICTORY_CHANNEL, 1);
		}
		else if (bullets > (100f / gameProgressPercentage))
		{
			float bulletsToSpend =  gameProgressPercentage * bullets;
			int vp = victoryPointsPurchasableWithBullets(bulletsToSpend);
			float iDonated = bulletsRequiredToBuyVictoryPoints(vp) + 0.01f;
			rc.donate(iDonated);
		}
	}
	
	private static void updateEnemies()throws GameActionException 
	{
		int enemyGardeners = rc.readBroadcast(ENEMY_GARDENERS_CHANNELS[0]);
		int enemyArchons = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[0]);
		for (int i = 1; i < enemyGardeners * 2; i += 2)
		{
			int ID = rc.readBroadcast(ENEMY_GARDENERS_CHANNELS[i]);
			int hashedLocation = rc.readBroadcast(ENEMY_GARDENERS_CHANNELS[i + 1]);
			if (hashedLocation == -1)
			{
				continue;
			}
			else
			{
				MapLocation unhashedLocation = unhashIt(hashedLocation);
				if (rc.canSenseLocation(unhashedLocation) && !rc.canSenseRobot(ID))
				{
					rc.broadcast(ENEMY_GARDENERS_CHANNELS[i + 1], -1);
				}
				if (!seenEnemyGardeners.containsKey(ID))
				{
					seenEnemyGardeners.put(ID, i);
				}
			}
		}
		for (int i = 1; i < enemyArchons * 2; i += 2)
		{
			int ID = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[i]);
			int hashedLocation = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[i + 1]);
			if (hashedLocation == -1)
			{
				continue;
			}
			else
			{
				MapLocation unhashedLocation = unhashIt(hashedLocation);
				if (rc.canSenseLocation(unhashedLocation) && !rc.canSenseRobot(ID))
				{
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[i + 1], -1);
				}
				if (!seenEnemyArchons.containsKey(ID))
				{
					seenEnemyArchons.put(ID, i);
				}
			}
		}
		for (RobotInfo enemy : enemies)
		{
			int ID = enemy.getID();
			if (enemy.getType() == RobotType.GARDENER)
			{
				rc.broadcast(ENEMY_GARDENERS_CHANNELS[ENEMY_GARDENERS_CHANNELS.length - 2], roundNum);
				int hashedLocation = hashIt(enemy.getLocation());
				if (!seenEnemyGardeners.containsKey(ID))
				{
					rc.broadcast(ENEMY_GARDENERS_CHANNELS[enemyGardeners * 2 + 1], ID);
					rc.broadcast(ENEMY_GARDENERS_CHANNELS[enemyGardeners * 2 + 2], hashedLocation);
					enemyGardeners++;
					rc.broadcast(ENEMY_GARDENERS_CHANNELS[0], enemyGardeners);
				}
				else
				{
					rc.broadcast(ENEMY_GARDENERS_CHANNELS[seenEnemyGardeners.get(ID) + 1], hashedLocation);
				}
			}
			else if (enemy.getType() == RobotType.ARCHON)
			{
				rc.broadcast(ENEMY_ARCHONS_CHANNELS[ENEMY_ARCHONS_CHANNELS.length - 1], roundNum);
				int hashedLocation = hashIt(enemy.getLocation());
				if (!seenEnemyArchons.containsKey(ID))
				{
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[enemyArchons * 2 + 1], ID);
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[enemyArchons * 2 + 2], hashedLocation);
					enemyArchons++;
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[0], enemyArchons);
				}
				else
				{
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[seenEnemyArchons.get(ID) + 1], hashedLocation);
				}
			}
		}
		for (int i = 2; i <= enemyGardeners * 2; i += 2)
		{
			int hashedLocation = rc.readBroadcast(ENEMY_GARDENERS_CHANNELS[i]);
			if (hashedLocation == -1)
			{
				continue;
			}
		}
		for (int i = 2; i <= enemyArchons * 2; i += 2)
		{
			int hashedLocation = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[i]);
			if (hashedLocation == -1)
			{
				continue;
			}
		}
		/*
		haveTarget = ((rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[7]) > 0) && ((roundNum - rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[8])) < 10));
		*/
	}
	
	public static void updateTrees()throws GameActionException
	{
		for (TreeInfo tree : neutralTrees)
		{
			float r = tree.getRadius();
			float area = (float)Math.PI * r * r;
			nonAllyTreeArea += area;
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
						}
						if (tree.getID() == ID)
						{
							// already seen this tree
							found = true; 
							break;
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
			}
		}
		for (TreeInfo tree : enemyTrees)
		{
			float r = tree.getRadius();
			float area = (float)Math.PI * r * r;
			nonAllyTreeArea += area;
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
				try
				{
					rc.move(candidateDirection);
					updateLocation();
					return true;
				}
				catch (GameActionException e)
				{
					System.out.println("Catch kiya");
					return false;
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
		Direction left = bulletDirection.rotateLeftDegrees(90);
		Direction right = bulletDirection.rotateRightDegrees(90);
		if (!rc.hasMoved() && rc.canMove(left))
		{
			rc.move(left);
			updateLocation();
			return true;
		}
		if (!rc.hasMoved() && rc.canMove(right))
		{
			rc.move(right);
			updateLocation();
			return true;
		}
		if (tryToMove(left))
		{
			return true;
		}
		return tryToMove(right);
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
		if (!(myType == RobotType.LUMBERJACK))
		{
			for (RobotInfo enemy : enemies)
			{
				RobotType enemyType = enemy.getType();
				MapLocation enemyLocation = enemy.getLocation();
				if (enemyType == RobotType.LUMBERJACK && here.distanceTo(enemyLocation) - myType.bodyRadius < 3.5f)
				{
					tryToMove(enemyLocation.directionTo(here));
					return;
				}
			}
		}
		RobotInfo me = new RobotInfo(myID, us, myType, here, rc.getHealth(), rc.getAttackCount(), rc.getMoveCount());
		for (BulletInfo sensedBullet : sensedBullets)
		{
			Direction bulletDirection = sensedBullet.getDir();
			MapLocation bulletLocation = sensedBullet.getLocation();
			rc.setIndicatorLine(bulletLocation, bulletLocation.add(bulletDirection, 2.5f), 0, 0, 255);
			if (willHitRobot(me, bulletDirection, bulletLocation) && (sensedBullet.getSpeed()) >= (bulletLocation.distanceTo(here) - myType.bodyRadius))
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
	
	public static boolean willHitRobot(RobotInfo robot, Direction shotDirection, MapLocation shotFrom)throws GameActionException
	{
		float distanceToCentre = shotFrom.distanceTo(robot.getLocation());
		Direction robotDirection = shotFrom.directionTo(robot.getLocation());
		float radiansBetween = shotDirection.radiansBetween(robotDirection);
		if (Math.abs(radiansBetween) > maxHitAngle)
		{
			return false;
		}
		float robotRadius = robot.getRadius();
		float tan = (float)Math.tan(Math.abs(radiansBetween));
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
		if (rc.canFireSingleShot())
		{
			Direction shotDirection = directionToCentre;
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
				if (rc.canFireSingleShot())
				{
					rc.fireSingleShot(shotDirection);
				}
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
		updateRoundNum();
		updateBulletCount();
		doVictoryPointsCalculations();
		updateNearbyBullets();
		updateRobotCount();
		updateNearbyObjectLocations();
		tryToDodge();
		if (dying())
		{
			imDying();
		}
		updateEnemies();
		updateTrees();
		updateNonAllyTreeDensity();
	}

	// Footer to run at the end of each round
	
	public static void footer()throws GameActionException
	{
		prevHealth = rc.getHealth();
		Clock.yield();
	}
}
