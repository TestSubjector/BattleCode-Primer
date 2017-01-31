package potatobot;
import battlecode.common.*;

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
	public static float archonDistance;
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
	public static int spawners;
	public static float nonAllyTreeArea;
	public static float nonAllyTreeDensity;
	public static RobotInfo[] allies;
	public static RobotInfo[] enemies;
	public static TreeInfo[] neutralTrees;
	public static TreeInfo[] enemyTrees;
	public static TreeInfo[] allyTrees;
	public static BulletInfo[] sensedBullets;
	public static int treesPlanted;
	public static int enemyTarget;
	public static MapLocation enemyTargetLocation;
	public static float enemyTargetDistance;
	public static int importantTreeTarget;
	public static MapLocation importantTreeTargetLocation;
	public static float importantTreeTargetDistance;
	public static boolean amFarmer;
	public static int[] tryAngles;
	public static boolean movedBack;
	public static boolean lastMoveWasPositive;
	
	// Broadcast Channels
	public static int TREE_CHANNEL = 64;
	
	public static int DEAD_FARMERS_CHANNEL = 66;
	
	public static int GARDENER_NUMBER_CHANNEL = 67;
	
	public static int VICTORY_CHANNEL = 69;
	
	public static int[] ENEMY_ARCHONS_CHANNELS;
	
	/* The enemy archon channels represent:
	 * 43 = Number of enemy Archons seen (deprecated)
	 * {44, 45, 46} - {50, 51, 52} = {ID of nth detected Archon, (hashed) location of the nth detected Archon, Round Number it was last seen}
	 * {53, 54, 55} = Buffer Channels
	 */

	public static int[] IMPORTANT_TREES_CHANNELS;
	/* The important trees channels represent:
	 * 100 = Number of important trees seen (deprecated)
	 * {101, 102} - {119, 120} = {ID of nth detected important tree, (hashed) location of the nth detected tree}
	 * {121, 122} = Buffer Channels
	 * 123 = Index of first 0 location
	 */
	
	public static int[] SPAWN_LOCATIONS_CHANNELS;
	/* The spawn locations channels represent:
	 * 333 = Number of spawn locations made
	 * 334 - 364 = (hashed) location of the nth farm centre
	 */
	
	public static int[] FARM_LOCATIONS_CHANNELS;
	/* The farm locations channels represent:
	 * 666 = Number of farms made
	 * 667 - 697 = (hashed) location of the nth farm centre
	 */
	
	public static int[] ENEMY_GARDENERS_CHANNELS;
	/* The gardener locations channels represent:
	 * 700 = Number of enemy Gardeners seen (deprecated)
	 * {701, 702, 703} - {728, 729, 730} = {ID of nth detected enemy Gardener, (hashed) location of the nth detected enemy Gardener, Round Number it was last seen}
	 * {731, 732, 733} = Buffer Channels
	 */
	
	// End Broadcast Channels
	
	public static final int hasher = 100000;
	public static final int farmerIndex = 7;
	public static final int spawnerIndex = 8;
	public static final double maxHitAngle = Math.atan(2.0f / 3.0f);
	// Initialization functions start here
	
	public static void init(RobotController rcinit)throws GameActionException
	{
		rc = rcinit;
		roundNum = 0;
		gameProgressPercentage = 0;
		updateLocation();
		myID = rc.getID();
		myType = rc.getType();
		bullets = rc.getTeamBullets();
		prevHealth = rc.getHealth();
		us = rc.getTeam();
		them = us.opponent();
		ourInitialArchons = rc.getInitialArchonLocations(us);
		theirInitialArchons = rc.getInitialArchonLocations(them);
		archonDistance = ourInitialArchons[0].distanceTo(theirInitialArchons[0]);
		numberOfArchons = theirInitialArchons.length;
		victoryPoints = 0;
		movingDirection = randomDirection();
		robotCount = new int[9];
		updateRobotCount();
		updateNonAllyTreeDensity();
		treesPlanted = 0;
		enemyTarget = 0;
		enemyTargetLocation = null;
		enemyTargetDistance = 5000000f;
		importantTreeTarget = 0;
		importantTreeTargetLocation = null;
		importantTreeTargetDistance = 5000000f;
		initTryAngles();
		initChannels();
		movedBack = false;
		lastMoveWasPositive = true;
	}

	public static void robotInit(RobotType type)throws GameActionException
	{
		updateRobotCount();
		int robotsOfThisType = robotCount[type.ordinal()];
		rc.broadcast(type.ordinal(), robotsOfThisType + 1);
	}
	
	public static void initTryAngles()
	{
		int numberOfAngles = 221; 
		tryAngles = new int[numberOfAngles];
		for (int i = 0; i < numberOfAngles; i++)
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
		ENEMY_GARDENERS_CHANNELS = new int[34];
		for (int i = 700; i <= 733; i++)
		{
			ENEMY_GARDENERS_CHANNELS[i - 700] = i;
		}
		ENEMY_ARCHONS_CHANNELS = new int[13];
		for (int i = 43; i <= 55; i++)
		{
			ENEMY_ARCHONS_CHANNELS[i - 43] = i;
		}
		SPAWN_LOCATIONS_CHANNELS = new int[32];
		for (int i = 333; i <= 364; i++)
		{
			SPAWN_LOCATIONS_CHANNELS[i - 333] = i;
		}
		FARM_LOCATIONS_CHANNELS = new int[32];
		for (int i = 666; i <= 697; i++)
		{
			FARM_LOCATIONS_CHANNELS[i - 666] = i;
		}
		IMPORTANT_TREES_CHANNELS = new int[24];
		for (int i = 100; i <= 123; i++)
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
	
	public static Direction findDirectionAwayFromNearestObstacle(BodyInfo array[][])throws GameActionException 
	{
		float minDist = 100000f;
		Direction awayFromNearestObstacle = null;
		for (int i = 0; i < array.length; i++)
		{
			int arrayLength = array[i].length;
			if (arrayLength != 0)
			{
				if (array[i][0].isRobot())
				{
					array[i][0] = (RobotInfo)array[i][0];
				}
				else
				{
					array[i][0] = (TreeInfo)array[i][0];
				}
				MapLocation bodyLocation = array[i][0].getLocation();
				float bodyDistance = bodyLocation.distanceTo(here) - array[i][0].getRadius();
				if (bodyDistance <= minDist)
				{
					awayFromNearestObstacle = bodyLocation.directionTo(here);
					minDist = bodyDistance;
				}
			}
		}
		minDist = Math.min(minDist, myType.sensorRadius - 0.1f);
		float angle = 0;
		Direction initialDirection = Direction.getEast();
		while (angle < 360)
		{
			Direction sensorDirection = initialDirection.rotateLeftDegrees(angle);
			while (rc.canSenseLocation(here.add(sensorDirection, minDist)) && !rc.onTheMap(here.add(sensorDirection, minDist)))
			{
				minDist = Math.max(minDist - 0.5f, 2.0f);
				if (minDist <= 2.01f)
				{
					return sensorDirection.opposite();
				}
				awayFromNearestObstacle = sensorDirection.opposite();
			}
			angle += 4;
		}
		if (awayFromNearestObstacle == null)
		{
			return randomDirection();
		}
		return awayFromNearestObstacle;
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
		if (myType == RobotType.GARDENER)
		{
			if (amFarmer)
			{
				robotsOfMyType = robotCount[farmerIndex];
				rc.broadcast(farmerIndex, robotsOfMyType - 1);
			}
			else
			{
				robotsOfMyType = robotCount[spawnerIndex];
				rc.broadcast(spawnerIndex, robotsOfMyType - 1);
			}
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

	public static void updateNonAllyTreeDensity()
	{
		float sightArea = (float)Math.PI * myType.sensorRadius * myType.sensorRadius;
		nonAllyTreeDensity = (nonAllyTreeArea / sightArea);
	}
	
	public static void updateRobotCount()throws GameActionException
	{
		for (int i = 1; i <= 8; i++)
		{
			robotCount[i] = rc.readBroadcast(i);
		}
		scouts = robotCount[RobotType.SCOUT.ordinal()];
		lumberjacks = robotCount[RobotType.LUMBERJACK.ordinal()];
		soldiers = robotCount[RobotType.SOLDIER.ordinal()];
		tanks = robotCount[RobotType.TANK.ordinal()];
		gardeners = robotCount[RobotType.GARDENER.ordinal()];
		farmers = robotCount[farmerIndex];
		spawners = robotCount[spawnerIndex];
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
		allyTrees = rc.senseNearbyTrees(-1, us);
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
		else if (bullets > (75f / gameProgressPercentage))
		{
			float bulletsToSpend =  gameProgressPercentage * bullets;
			int vp = victoryPointsPurchasableWithBullets(bulletsToSpend);
			float iDonated = bulletsRequiredToBuyVictoryPoints(vp) + 0.01f;
			rc.donate(iDonated);
		}
	}
	
	public static void updateEnemies()throws GameActionException
	{
		enemyTarget = 0;
		enemyTargetLocation = null;
		enemyTargetDistance = 5000000f;
		
		int[][] archonsRead = new int[3][2];
		int numberOfArchonsRead = 0;
		boolean[] archonZeros = new boolean[3];
		for (int i = 1; i < 9; i += 3)
		{
			int readID = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[i]);
			int hashedLocation = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[i + 1]);
			int roundNumLastSeen = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[i + 2]);
			if (hashedLocation != 0)
			{
				if (roundNum - roundNumLastSeen > 30)
				{
					archonZeros[(i - 1) / 3] = true;
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[i], 0);
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[i + 1], 0);
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[i + 2], 0);
				}
				else 
				{
					MapLocation unhashedLocation = unhashIt(hashedLocation);
					archonsRead[numberOfArchonsRead][0] = readID;
					archonsRead[numberOfArchonsRead++][1] = i;
					if (!(myType == RobotType.SCOUT && roundNum < 1000) && here.distanceTo(unhashedLocation) < enemyTargetDistance)
					{
						enemyTarget = readID;
						enemyTargetLocation = unhashedLocation;
						enemyTargetDistance = here.distanceTo(enemyTargetLocation);
					}
				}
			}
			else
			{
				archonZeros[(i - 1) / 3] = true;
			}
		}
		
		int[][] gardenersRead = new int[10][2];
		int numberOfGardenersRead = 0;
		boolean[] gardenerZeros = new boolean[10];
		for (int i = 1; i < 30; i += 3)
		{
			int readID = rc.readBroadcast(ENEMY_GARDENERS_CHANNELS[i]);
			int hashedLocation = rc.readBroadcast(ENEMY_GARDENERS_CHANNELS[i + 1]);
			int roundNumLastSeen = rc.readBroadcast(ENEMY_GARDENERS_CHANNELS[i + 2]);
			if (hashedLocation != 0)
			{
				if (roundNum - roundNumLastSeen > 30)
				{
					gardenerZeros[(i - 1) / 3] = true;
					rc.broadcast(ENEMY_GARDENERS_CHANNELS[i], 0);
					rc.broadcast(ENEMY_GARDENERS_CHANNELS[i + 1], 0);
					rc.broadcast(ENEMY_GARDENERS_CHANNELS[i + 2], 0);
				}
				else
				{
					MapLocation unhashedLocation = unhashIt(hashedLocation);
					gardenersRead[numberOfGardenersRead][0] = readID;
					gardenersRead[numberOfGardenersRead++][1] = i;
					if (here.distanceTo(unhashedLocation) < enemyTargetDistance)
					{
						enemyTarget = readID;
						enemyTargetLocation = unhashedLocation;
						enemyTargetDistance = here.distanceTo(enemyTargetLocation);
					}
				}
			}
			else
			{
				gardenerZeros[(i - 1) / 3] = true;
			}
		}
		
		int az = 0;
		int gz = 0;
		
		int limit = Math.min(enemies.length, 30);
		for(int i = 0; i < limit; i++)
		{
			RobotInfo enemy = enemies[i];
			MapLocation enemyLocation = enemy.getLocation();
			int enemyID = enemy.getID();
			RobotType enemyType = enemy.getType();
			if (enemyType == RobotType.ARCHON)
			{
				boolean found = false;
				int j;
				for (j = 0; j < numberOfArchonsRead; j++)
				{
					if (archonsRead[j][0] == enemyID)
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					while (az < 3 && !archonZeros[az])
					{
						az++;
					}
					if (az == 3)
					{
						System.out.println("Lite");
					}
					else
					{
						int index = (az * 3) + 1;
						int hashedLocation = hashIt(enemyLocation);
						rc.broadcast(ENEMY_ARCHONS_CHANNELS[index], enemyID);
						rc.broadcast(ENEMY_ARCHONS_CHANNELS[index + 1], hashedLocation);
						rc.broadcast(ENEMY_ARCHONS_CHANNELS[index + 2], roundNum);
					}
				}
				else
				{
					int index = archonsRead[j][1];
					int hashedLocation = hashIt(enemyLocation);
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[index], enemyID);
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[index + 1], hashedLocation);
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[index + 2], roundNum);
				}
			}
			else if (enemyType == RobotType.GARDENER)
			{
				boolean found = false;
				int j;
				for (j = 0; j < numberOfGardenersRead; j++)
				{
					if (gardenersRead[j][0] == enemyID)
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					while (gz < 10 && !gardenerZeros[gz])
					{
						gz++;
					}
					if (gz == 10)
					{
						System.out.println("Lite");
					}
					else
					{
						int index = (gz * 3) + 1;
						int hashedLocation = hashIt(enemyLocation);
						rc.broadcast(ENEMY_GARDENERS_CHANNELS[index], enemyID);
						rc.broadcast(ENEMY_GARDENERS_CHANNELS[index + 1], hashedLocation);
						rc.broadcast(ENEMY_GARDENERS_CHANNELS[index + 2], roundNum);
					}
				}
				else
				{
					int index = gardenersRead[j][1];
					int hashedLocation = hashIt(enemyLocation);
					rc.broadcast(ENEMY_GARDENERS_CHANNELS[index], enemyID);
					rc.broadcast(ENEMY_GARDENERS_CHANNELS[index + 1], hashedLocation);
					rc.broadcast(ENEMY_GARDENERS_CHANNELS[index + 2], roundNum);
				}
			}
		}
	}
	
	public static void updateTrees()throws GameActionException
	{
		int impChannelLength = IMPORTANT_TREES_CHANNELS.length;
		int[] treesRead = new int[10];
		int numberOfTreesRead = 0;
		boolean found = false;
		for (int i = 1; i < 20; i += 2)
		{
			int readID = rc.readBroadcast(IMPORTANT_TREES_CHANNELS[i]);
			int hashedLocation = rc.readBroadcast(IMPORTANT_TREES_CHANNELS[i + 1]);
			if (hashedLocation != 0)
			{
				MapLocation unhashedLocation = unhashIt(hashedLocation);
				if (myType == RobotType.LUMBERJACK && (importantTreeTarget == 0 || here.distanceTo(unhashedLocation) < importantTreeTargetDistance))
				{
					importantTreeTarget = readID;
					importantTreeTargetLocation = unhashedLocation;
					importantTreeTargetDistance = here.distanceTo(importantTreeTargetLocation);
				}
				if (rc.canSenseLocation(unhashedLocation) && !rc.canSenseTree(readID))
				{
					rc.broadcast(IMPORTANT_TREES_CHANNELS[i + 1], 0);
					if (importantTreeTarget == readID)
					{
						importantTreeTarget = 0;
						importantTreeTargetDistance = 500000f;
					}
				}
				else
				{
					treesRead[numberOfTreesRead++] = readID;
				}
			}
			else if (!found)
			{
				found = true;
				rc.broadcast(IMPORTANT_TREES_CHANNELS[impChannelLength - 1], i);
			}
		}
		
		int limit = Math.min(neutralTrees.length, 30);
		for(int i = 0; i < limit; i++)
		{
			TreeInfo tree = neutralTrees[i];
			float r = tree.getRadius();
			float area = (float)Math.PI * r * r;
			nonAllyTreeArea += area;
			MapLocation treeLocation = tree.getLocation();
			int treeID = tree.getID();
			if (tree.getContainedBullets() > 0)
			{
				if (rc.canShake(treeID))
				{
					rc.shake(treeID);
				}
			}
			if (tree.getContainedRobot() != null)
			{
				found = false;
				int j;
				for (j = 0; j < numberOfTreesRead; j++)
				{
					if (treesRead[j] == treeID)
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					int index = rc.readBroadcast(IMPORTANT_TREES_CHANNELS[impChannelLength - 1]);
					if (index >= 21)
					{
						System.out.println("Lite");
					}
					else
					{
						int hashedLocation = hashIt(treeLocation);
						rc.broadcast(IMPORTANT_TREES_CHANNELS[index], treeID);
						rc.broadcast(IMPORTANT_TREES_CHANNELS[index + 1], hashedLocation);
						rc.broadcast(IMPORTANT_TREES_CHANNELS[impChannelLength - 1], index + 2);
					}
				}
			}
		}
		int loopLength = enemyTrees.length;
		for(int i = 0; i < loopLength; i++)
		{
			TreeInfo tree = enemyTrees[i];
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
		
		int loopLength = (myType == RobotType.SCOUT) ? 179 : tryAngles.length;
		for(int i = 0; i < loopLength; i++)
		{
			int angle = tryAngles[i];
			if ((lastMoveWasPositive && angle >= 0) || (!lastMoveWasPositive && angle < 0))
			{
				Direction candidateDirection = movingDirection.rotateLeftDegrees(angle);
				if (rc.canMove(candidateDirection))
				{
					if (i <= 210)
					{
						movedBack = false;
					}
					else
					{
						movedBack = true;
					}
					if (angle < 0)
					{
						lastMoveWasPositive = false;
					}
					else
					{
						lastMoveWasPositive = true;
					}
					rc.move(candidateDirection);
					updateLocation();
					return true;
				}
			}
		}
		for(int i = 0; i < loopLength; i++)
		{
			int angle = tryAngles[i];
			Direction candidateDirection = movingDirection.rotateLeftDegrees(angle);
			if (rc.canMove(candidateDirection))
			{
				if (i <= 210)
				{
					movedBack = false;
				}
				else
				{
					movedBack = true;
				}
				if (angle < 0)
				{
					lastMoveWasPositive = false;
				}
				else
				{
					lastMoveWasPositive = true;
				}
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
		int loopLength = tryAngles.length;
		for(int i = 0; i < loopLength; i++)
		{
			int angle = tryAngles[i];
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
	
	public static boolean backStep(Direction bulletDirection)throws GameActionException
	{
		Direction back = bulletDirection;
		if (!rc.hasMoved() && rc.canMove(back))
		{
			rc.move(back);
			updateLocation();
			return true;
		}
		return tryToMove(back);
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
			int loopLength = enemies.length;
			for(int i = 0; i < loopLength; i++)
			{
				RobotInfo enemy = enemies[i];
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
		int loopLength = Math.min(3, sensedBullets.length);
		Direction centralBulletDirection = null;
		MapLocation centralBulletLocation = null;
		if (loopLength != 0)
		{
			centralBulletDirection = sensedBullets[0].getDir(); 
			centralBulletLocation = sensedBullets[0].getLocation(); 
		}
		for(int i = 1; i < loopLength; i++)
		{
			double deviationFromCentre = Math.abs(sensedBullets[i].getDir().degreesBetween(centralBulletDirection));
			if ((deviationFromCentre - GameConstants.TRIAD_SPREAD_DEGREES <= 0.01) || (deviationFromCentre - GameConstants.PENTAD_SPREAD_DEGREES <= 0.01))
			{
				if (willHitBody(me, centralBulletDirection, centralBulletLocation))
				{
					if (backStep(centralBulletDirection))
					{
						return;
					}
				}
			}
		}
		loopLength = sensedBullets.length;
		for(int i = 0; i < loopLength; i++)
		{
			BulletInfo sensedBullet = sensedBullets[i];
			Direction bulletDirection = sensedBullet.getDir();
			MapLocation bulletLocation = sensedBullet.getLocation();
			// rc.setIndicatorLine(bulletLocation, bulletLocation.add(bulletDirection, 2.5f), 0, 0, 255);
			if (willHitBody(me, bulletDirection, bulletLocation))
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
	
	public static boolean willHitBody(BodyInfo body, Direction shotDirection, MapLocation shotFrom)throws GameActionException
	{
		float distanceToCentre = shotFrom.distanceTo(body.getLocation());
		Direction bodyDirection = shotFrom.directionTo(body.getLocation());
		float radiansBetween = shotDirection.radiansBetween(bodyDirection);
		if (Math.abs(radiansBetween) > maxHitAngle)
		{
			return false;
		}
		float bodyRadius = body.getRadius();
		float tan = (float)Math.tan(Math.abs(radiansBetween));
		float distanceFromCentre = (float) (distanceToCentre * tan);
		if (distanceFromCentre < bodyRadius)
		{
			return true;
		}
		return false;
	}
	
	public static boolean tryShot()throws GameActionException
	{
		// Center, TriadLeft, TriadRight, PentadLeftest, PentadLeft, PentadRight, PentadRightest
		Direction[] shotDirections = {null,null,null,null,null,null,null}; 
		Target[] TargetHit = {Target.NONE,Target.NONE,Target.NONE,Target.NONE,Target.NONE,Target.NONE,Target.NONE};
		float[] targetDist = {0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f};
		int enemiesLen = enemies.length;
		int alliesLen = allies.length;
		int allyTreesLen = allyTrees.length;
		int neutralTreesLen = neutralTrees.length;
		for(int i = 0; i < enemiesLen; i++)
		{
			if (myType == RobotType.SCOUT && enemies[i].getType() == RobotType.ARCHON && roundNum < 400)
			{
				continue;
			}
			int j;
			shotDirections[0] = here.directionTo(enemies[i].getLocation());
			shotDirections[1] = shotDirections[0].rotateLeftDegrees(GameConstants.TRIAD_SPREAD_DEGREES);
			shotDirections[2] = shotDirections[0].rotateRightDegrees(GameConstants.TRIAD_SPREAD_DEGREES);
			shotDirections[3] = shotDirections[0].rotateLeftDegrees(2 * GameConstants.PENTAD_SPREAD_DEGREES);
			shotDirections[4] = shotDirections[0].rotateLeftDegrees(GameConstants.PENTAD_SPREAD_DEGREES);
			shotDirections[5] = shotDirections[0].rotateRightDegrees(GameConstants.TRIAD_SPREAD_DEGREES);
			shotDirections[6] = shotDirections[0].rotateRightDegrees(2 * GameConstants.PENTAD_SPREAD_DEGREES);
			
			TargetHit[0] = Target.ENEMY;
			TargetHit[1] = Target.NONE;
			TargetHit[2] = Target.NONE;
			TargetHit[3] = Target.NONE;
			TargetHit[4] = Target.NONE;
			TargetHit[5] = Target.NONE;
			TargetHit[6] = Target.NONE;
			
			targetDist[0] = enemies[i].getLocation().distanceTo(here);
			targetDist[1] = 0.0f;
			targetDist[2] = 0.0f;
			targetDist[3] = 0.0f;
			targetDist[4] = 0.0f;
			targetDist[5] = 0.0f;
			targetDist[6] = 0.0f;
			int directionLimit = 1;
			if (rc.canFirePentadShot())
			{
				directionLimit = 7;
			}
			else if (rc.canFireTriadShot())
			{
				directionLimit = 3;
			}
			for(int k = 0; k < directionLimit; k++)
			{
				for(j = 0; j < enemiesLen ; j++)
				{
					if(willHitBody(enemies[j],shotDirections[k],here))
					{
						TargetHit[k]  = Target.ENEMY;
						targetDist[k] = here.distanceTo(enemies[j].getLocation());
						break;
					}
				}
				
				for(j = 0;j < alliesLen; j++)
				{
					if(willHitBody(allies[j],shotDirections[k],here) && here.distanceTo(allies[j].getLocation()) < targetDist[k])
					{
						TargetHit[k]  = Target.ALLY;
						targetDist[k] = here.distanceTo(allies[j].getLocation());
						break;
					}
				}
				
				for(j = 0; j < allyTreesLen; j++)
				{
					if(willHitBody(allyTrees[j],shotDirections[k],here) && here.distanceTo(allyTrees[j].getLocation()) < targetDist[k])
					{
						TargetHit[k]  = Target.ALLY_TREE;
						targetDist[k] = here.distanceTo(allyTrees[j].getLocation());
						break;
					}
				}
				
				for(j = 0; j < neutralTreesLen; j++)
				{
					if(willHitBody(neutralTrees[j],shotDirections[k],here) && here.distanceTo(neutralTrees[j].getLocation()) < targetDist[k])
					{
						TargetHit[k]  = Target.NEUTRAL_TREE;
						break;
					}
				}
			}//end of directions loop
			switch (whichShotToFire(TargetHit, directionLimit))
			{
				case 0:
					break;
				case 1:
					if (rc.canFireSingleShot())
					{
						rc.fireSingleShot(shotDirections[0]);
					}
					return true;
				case 3:
					if (rc.canFireTriadShot())
					{
						rc.fireTriadShot(shotDirections[0]);
					}
					return true;
				case 5:
					if (rc.canFirePentadShot())
					{
						rc.firePentadShot(shotDirections[0]);
					}
					return true;
			}
		}// end of topmost enemLoop; decides centerDir
		return false;
	}
	
	enum Target 
	{
		ENEMY, ALLY, NONE, NEUTRAL_TREE, ALLY_TREE
	}
	
	public static int whichShotToFire(Target[] arr, int directionLimit)
	{
		
		boolean[][] hit = new boolean[7][2];
		for(int i = 0; i < directionLimit; i++)
		{
			switch(arr[i])
			{
				case ENEMY: hit[i][0] = true; break;
				case ALLY: hit[i][1] = true; break;
				case ALLY_TREE: break;
				case NEUTRAL_TREE: break;
				case NONE: break;
				default: break;
			}
		}
		
		int enemySum = 0, allySum = 0, wasteSum = 0, ans = 0;
		float maxWeight = 0.0f;
		final float SINGLE_ENEMY_WEIGHT = 1.0f, SINGLE_ALLY_WEIGHT = -0.4f, SINGLE_WASTE_WEIGHT = -0.125f;
		final float TRIAD_ENEMY_WEIGHT = 10f, TRIAD_ALLY_WEIGHT = -5f, TRIAD_WASTE_WEIGHT = -1.0f;
		final float PENTAD_ENEMY_WEIGHT = 14.0f, PENTAD_ALLY_WEIGHT = -8f, PENTAD_WASTE_WEIGHT = -1.2f;
		
		if(directionLimit == 7)
		{
			//pentad check
			float weight = 0.0f;
			for(int i = 0; i < 7; i++)
			{
				if(i == 1 || i == 2) continue;
				if (hit[i][0])
				{
					enemySum++;
				}
				else if (hit[i][1])
				{
					allySum++;
				}
				else
				{
					wasteSum++;
				}
			}
			weight = (PENTAD_ENEMY_WEIGHT * enemySum + PENTAD_ALLY_WEIGHT * allySum + PENTAD_WASTE_WEIGHT * wasteSum) / 6.0f;
			if (weight > maxWeight)
			{
				maxWeight = weight; 
				ans = 5;
			}
		}
		if(directionLimit >= 3)
		{
			//triad check
			float weight = 0.0f;
			enemySum = 0;
			allySum = 0;
			wasteSum = 0;
			for(int i = 0; i < 3; i++)
			{
				if (hit[i][0])
				{
					enemySum++;
				}
				else if (hit[i][1])
				{
					allySum++;
				}
				else
				{
					wasteSum++;
				}
			}
			weight = (TRIAD_ENEMY_WEIGHT * enemySum + TRIAD_ALLY_WEIGHT * allySum + TRIAD_WASTE_WEIGHT * wasteSum) / 4;
			if (weight > maxWeight)
			{
				maxWeight = weight;
				ans = 3;
			}
		}
		if(directionLimit >= 1)
		{
			float weight = (SINGLE_ENEMY_WEIGHT * (hit[0][0] ? 1 : 0) + SINGLE_ALLY_WEIGHT * (hit[0][1] ? 1 : 0));
			if (weight < 0.1f)
			{
				weight = SINGLE_WASTE_WEIGHT;
			}
			if (weight > maxWeight)
			{
				maxWeight = weight;
				ans = 1;
			}
		}
		return ans;
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
		updateNearbyObjects();
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
