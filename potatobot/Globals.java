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
	public static int patience;
	public static boolean movedBack;
	
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
	 * 56 = Index of first 0 location
	 */

	public static int[] IMPORTANT_TREES_CHANNELS;
	/* The important trees channels represent:
	 * 100 = Number of important trees seen (deprecated)
	 * {101, 102} - {119, 120} = {ID of nth detected important tree, (hashed) location of the nth detected tree}
	 * {121, 122} = Buffer Channels
	 * 123 = Index of first 0 location
	 */
	
	public static int[] FARM_LOCATIONS_CHANNELS;
	/* The farm locations channels represent:
	 * 666 = Number of farms made
	 * 667 - 690 = (hashed) location of the nth farm centre
	 */
	
	public static int[] ENEMY_GARDENERS_CHANNELS;
	/* The gardener locations channels represent:
	 * 700 = Number of enemy Gardeners seen (deprecated)
	 * {701, 702, 703} - {728, 729, 730} = {ID of nth detected enemy Gardener, (hashed) location of the nth detected enemy Gardener, Round Number it was last seen}
	 * {731, 732, 733} = Buffer Channels
	 * 734 = Index of first 0 location
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
		robotCount = new int[8];
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
		patience = 30;
		movedBack = false;
	}

	public static void robotInit(RobotType type)throws GameActionException
	{
		updateRobotCount();
		int robotsOfThisType = robotCount[type.ordinal()];
		rc.broadcast(type.ordinal(), robotsOfThisType + 1);
	}
	
	public static void initTryAngles()
	{
		tryAngles = new int[241];
		for (int i = (myType == RobotType.SCOUT) ? 90 : 0; i < 241; i++)
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
		ENEMY_GARDENERS_CHANNELS = new int[36];
		for (int i = 700; i <= 735; i++)
		{
			ENEMY_GARDENERS_CHANNELS[i - 700] = i;
		}
		ENEMY_ARCHONS_CHANNELS = new int[14];
		for (int i = 43; i <= 56; i++)
		{
			ENEMY_ARCHONS_CHANNELS[i - 43] = i;
		}
		FARM_LOCATIONS_CHANNELS = new int[35];
		for (int i = 666; i <= 690; i++)
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
		if (myType == RobotType.GARDENER && amFarmer)
		{
			robotsOfMyType = robotCount[farmerIndex];
			rc.broadcast(farmerIndex, robotsOfMyType - 1);
			int deadFarmers = rc.readBroadcast(DEAD_FARMERS_CHANNEL);
			rc.broadcast(DEAD_FARMERS_CHANNEL, deadFarmers + 1);
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
		float sightArea = (float)Math.PI * myType.sensorRadius * myType.sensorRadius;
		nonAllyTreeDensity = (nonAllyTreeArea / sightArea);
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
		else if (bullets > (100f / gameProgressPercentage))
		{
			float bulletsToSpend =  gameProgressPercentage * bullets;
			int vp = victoryPointsPurchasableWithBullets(bulletsToSpend);
			float iDonated = bulletsRequiredToBuyVictoryPoints(vp) + 0.01f;
			rc.donate(iDonated);
		}
	}
	
	public static void updateEnemies()throws GameActionException
	{
		int archonChannelLength = ENEMY_ARCHONS_CHANNELS.length;
		int gardenerChannelLength = ENEMY_GARDENERS_CHANNELS.length;
		
		int[][] archonsRead = new int[3][2];
		int numberOfArchonsRead = 0;
		boolean found = false;
		int i;
		for (i = 1; i < 9; i += 3)
		{
			int readID = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[i]);
			int hashedLocation = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[i + 1]);
			int roundLastSeen = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[i + 2]);
			if (hashedLocation != 0)
			{
				MapLocation unhashedLocation = unhashIt(hashedLocation);
				if (enemyTarget == 0 || here.distanceTo(unhashedLocation) < enemyTargetDistance)
				{
					enemyTarget = readID;
					enemyTargetLocation = unhashedLocation;
					enemyTargetDistance = here.distanceTo(enemyTargetLocation);
				}
				if (roundNum - roundLastSeen > 50)
				{
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[i + 1], 0);
					if (enemyTarget == readID)
					{
						enemyTarget = 0;
						enemyTargetDistance = 500000f;
					}
				}
				else
				{
					archonsRead[numberOfArchonsRead][0] = readID;
					archonsRead[numberOfArchonsRead++][1] = i;
				}
			}
			else if (!found)
			{
				found = true;
				rc.broadcast(ENEMY_ARCHONS_CHANNELS[archonChannelLength - 1], i);
			}
		}
		
		int[][] gardenersRead = new int[10][2];
		int numberOfGardenersRead = 0;
		found = false;
		for (i = 1; i < 30; i += 3)
		{
			int readID = rc.readBroadcast(ENEMY_GARDENERS_CHANNELS[i]);
			int hashedLocation = rc.readBroadcast(ENEMY_GARDENERS_CHANNELS[i + 1]);
			int roundLastSeen = rc.readBroadcast(ENEMY_GARDENERS_CHANNELS[i + 2]);
			if (hashedLocation != 0)
			{
				MapLocation unhashedLocation = unhashIt(hashedLocation);
				if (enemyTarget == 0 || here.distanceTo(unhashedLocation) < enemyTargetDistance)
				{
					enemyTarget = readID;
					enemyTargetLocation = unhashedLocation;
					enemyTargetDistance = here.distanceTo(enemyTargetLocation);
				}
				if (roundNum - roundLastSeen > 50)
				{
					rc.broadcast(ENEMY_GARDENERS_CHANNELS[i + 1], 0);
					if (enemyTarget == readID)
					{
						enemyTarget = 0;
						enemyTargetDistance = 500000f;
					}
				}
				else
				{
					gardenersRead[numberOfGardenersRead][0] = readID;
					gardenersRead[numberOfGardenersRead++][1] = i;
				}
			}
			else if (!found)
			{
				found = true;
				rc.broadcast(ENEMY_GARDENERS_CHANNELS[gardenerChannelLength - 1], i);
			}
		}
		
		int limit = Math.min(enemies.length, 30);
		for(i = 0; i < limit; i++)
		{
			RobotInfo enemy = enemies[i];
			MapLocation enemyLocation = enemy.getLocation();
			int enemyID = enemy.getID();
			RobotType enemyType = enemy.getType();
			int j, k;
			if (enemyType == RobotType.ARCHON)
			{
				found = false;
				for (k = 0; k < numberOfArchonsRead; k++)
				{
					if (archonsRead[k][0] == enemyID)
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					int index = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[archonChannelLength - 1]);
					if (index >= 10)
					{
						System.out.println("Lite");
					}
					else
					{
						int hashedLocation = hashIt(enemyLocation);
						rc.broadcast(ENEMY_ARCHONS_CHANNELS[index], enemyID);
						rc.broadcast(ENEMY_ARCHONS_CHANNELS[index + 1], hashedLocation);
						rc.broadcast(ENEMY_ARCHONS_CHANNELS[index + 2], roundNum);
						rc.broadcast(ENEMY_ARCHONS_CHANNELS[archonChannelLength - 1], index + 3);
					}
				}
				else
				{
					int index = archonsRead[k][1];
					int hashedLocation = hashIt(enemyLocation);
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[index + 1], hashedLocation);
					rc.broadcast(ENEMY_ARCHONS_CHANNELS[index + 2], roundNum);
				}
			}
			if (enemyType == RobotType.GARDENER)
			{
				found = false;
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
					int index = rc.readBroadcast(ENEMY_GARDENERS_CHANNELS[gardenerChannelLength - 1]);
					if (index == 31)
					{
						System.out.println("Lite");
					}
					else
					{
						int hashedLocation = hashIt(enemyLocation);
						rc.broadcast(ENEMY_GARDENERS_CHANNELS[index], enemyID);
						rc.broadcast(ENEMY_GARDENERS_CHANNELS[index + 1], hashedLocation);
						rc.broadcast(ENEMY_GARDENERS_CHANNELS[index + 2], roundNum);
						rc.broadcast(ENEMY_GARDENERS_CHANNELS[gardenerChannelLength - 1], index + 3);
					}
				}
				else
				{
					int index = gardenersRead[j][1];
					int hashedLocation = hashIt(enemyLocation);
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
					if (index == 21)
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
		int loopLength = tryAngles.length;
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
		int loopLength = sensedBullets.length;
		for(int i = 0; i<loopLength;i++)
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
		Direction[] shotDirections = {null,null,null,null,null,null,null}; //Pentad0,Triad0,Pentad1,Center,Pentad3,Triad2,Pentad4
		Target[] TargetHit = {Target.NONE,Target.NONE,Target.NONE,Target.NONE,Target.NONE,Target.NONE,Target.NONE};
		float[] targetDist = {0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f};
		int enemiesLen = enemies.length;
		int alliesLen = allies.length;
		int allyTreesLen = allyTrees.length;
		int neutTreesLen = neutralTrees.length;
		for(int i=0;i<enemiesLen;i++){
			int j;
			shotDirections[3] = here.directionTo(enemies[i].getLocation());
			shotDirections[2] = shotDirections[3].rotateLeftDegrees(GameConstants.PENTAD_SPREAD_DEGREES);
			shotDirections[1] = shotDirections[3].rotateLeftDegrees(GameConstants.TRIAD_SPREAD_DEGREES);
			shotDirections[0] = shotDirections[2].rotateLeftDegrees(GameConstants.PENTAD_SPREAD_DEGREES);
			shotDirections[4] = shotDirections[3].rotateRightDegrees(GameConstants.PENTAD_SPREAD_DEGREES);
			shotDirections[5] = shotDirections[3].rotateRightDegrees(GameConstants.TRIAD_SPREAD_DEGREES);
			shotDirections[6] = shotDirections[4].rotateRightDegrees(GameConstants.PENTAD_SPREAD_DEGREES);
			
			TargetHit[0] = Target.NONE;
			TargetHit[1] = Target.NONE;
			TargetHit[2] = Target.NONE;
			TargetHit[3] = Target.ENEMY;
			TargetHit[4] = Target.NONE;
			TargetHit[5] = Target.NONE;
			TargetHit[6] = Target.NONE;
			
			targetDist[0] = 0.0f;
			targetDist[1] = 0.0f;
			targetDist[2] = 0.0f;
			targetDist[3] = enemies[i].getLocation().distanceTo(here);
			targetDist[4] = 0.0f;
			targetDist[5] = 0.0f;
			targetDist[6] = 0.0f;
			
			for(int k=0;k<7;k++)
			{
				for(j=0;j<enemiesLen;j++)
				{
					if(willHitBody(enemies[j],shotDirections[k],here))
					{
						//if BodyHit[k]!=null, it means already assigned. Since arrays are closest to farthest, we can skip
						TargetHit[k]  = Target.ENEMY;
						BodyHit[k] = enemies[j];
						break;
					}
				}
				
				for(j=0;j<alliesLen;j++)
				{
					if(willHitBody(allies[j],shotDirections[k],here) && here.distanceTo(allies[j].getLocation()) < )
					{
						//if BodyHit[k]!=null, it means already assigned. Since arrays are closest to farthest, we can skip
						TargetHit[k]  = Target.ENEMY;
						BodyHit[k] = enemies[j];
						break;
					}
				}
			}//end of other enemies collision loop
			
		}// end of topmost enemLoop; decides centerDir
		return false;
	}
	public static boolean tryTriadShot()throws GameActionException
	{
		if(rc.canFireTriadShot()){
			Direction[] shotDirections = {null,null,null};
			Target[] TargetHit = {Target.NONE,Target.NONE,Target.NONE};
			BodyInfo[] BodyHit = {null,null,null};
			int enemiesLen = enemies.length;
			int alliesLen = allies.length;
			int allyTreesLen = allyTrees.length;
			int neutTreesLen = neutralTrees.length;
			// if enemiesLen is 0, all loops are skipped.
			for(int i=0;i<enemiesLen;i++){
				RobotInfo centerTarget = enemies[i];
				shotDirections[1] = here.directionTo(centerTarget.getLocation());
				shotDirections[0] = shotDirections[1].rotateLeftDegrees(GameConstants.TRIAD_SPREAD_DEGREES);
				shotDirections[2] = shotDirections[1].rotateRightDegrees(GameConstants.TRIAD_SPREAD_DEGREES);
				
				TargetHit[0] = Target.NONE;
				TargetHit[1] = Target.ENEMY;
				TargetHit[2] = Target.NONE;
				BodyHit[0] = null;
				BodyHit[1] = enemies[i];
				BodyHit[2] = null;
				
				for(int j=0;j<enemiesLen;j++){
					if(willHitBody(enemies[j],shotDirections[0],here) && BodyHit[0] != null){
						//left bullet
						TargetHit[0]  = Target.ENEMY;
						BodyHit[0] = enemies[j];
					}
					if(willHitBody(enemies[j],shotDirections[2],here) && BodyHit[2] != null){
						//right bullet
						TargetHit[2]  = Target.ENEMY;
						BodyHit[2] = enemies[j];
					}
					if(TargetHit[0]==Target.ENEMY && TargetHit[2]==Target.ENEMY){
						break;
					}
				}
				
				for(int j=0;j<alliesLen;j++){
					for(int k=0;k<3;k++){
						if(willHitBody(allies[j],shotDirections[k],here) && TargetHit[k]!=Target.ALLY){
							if(TargetHit[k]==Target.NONE){
								BodyHit[k] = allies[j];
								TargetHit[k] = Target.ALLY;
							}
							else if(TargetHit[k]==Target.ENEMY){
								if(allies[j].getLocation().distanceTo(here) < BodyHit[k].getLocation().distanceTo(here)){
									BodyHit[k] = allies[j];
									TargetHit[k] = Target.ALLY;
								}
							}
						}
					}
					// if no TargetHit is NONE, loop can be broken
					if(TargetHit[0]!=Target.NONE && TargetHit[1]!=Target.NONE && TargetHit[2]!=Target.NONE){
						break;
					}
				}
				
				for(int j=0;j<allyTreesLen;j++){
					for(int k=0;k<3;k++){
						if(willHitBody(allyTrees[j],shotDirections[k],here) && TargetHit[k]!=Target.TREE_A){
							if(TargetHit[k]==Target.NONE){
								BodyHit[k] = allyTrees[j];
								TargetHit[k] = Target.TREE_A;
							}
							else{
								if(allyTrees[j].getLocation().distanceTo(here) <= BodyHit[k].getLocation().distanceTo(here)){
									// The = is to account for scouts on trees not getting hit
									BodyHit[k] = null;
									TargetHit[k] = Target.TREE_A;
								}
							}
						}
					}
				} 
				
				
				for(int j=0;j<neutTreesLen;j++){
					for(int k=0;k<3;k++){
						if(willHitBody(neutralTrees[j],shotDirections[k],here) && TargetHit[k]!=Target.TREE_N){
							
							if(TargetHit[k]==Target.NONE){
								BodyHit[k] = neutralTrees[j];
								TargetHit[k] = Target.TREE_N;
							}
							else{
								if(neutralTrees[j].getLocation().distanceTo(here) <= BodyHit[k].getLocation().distanceTo(here)){
									BodyHit[k] = neutralTrees[j];
									TargetHit[k] = Target.TREE_N;
								}
							}
						}
					}
				}
				
				//All possible obstacles have been taken into account. Now verify
				System.out.println("Before checking ratio #"+i+" "+Clock.getBytecodeNum());
				if(satisfactoryRatio(TargetHit)){
					rc.setIndicatorLine(here, here.add(shotDirections[0], 5.0f), 0, 0, 255);
					rc.setIndicatorLine(here, here.add(shotDirections[1], 5.0f), 255, 0, 0);
					rc.setIndicatorLine(here, here.add(shotDirections[2], 5.0f), 0, 0, 255);
					rc.fireTriadShot(shotDirections[1]);
					return true;
				} //else loop over next detected enemy
			}
		}
		return false;
	}
	public static boolean trySingleShot()throws GameActionException
	{
		if (rc.canFireSingleShot())
		{
			// Direction shotDirection = directionToCentre;
			// rc.setIndicatorLine(here, enemy.getLocation(), 0, 255, 0);
			Direction directionToCentre;
			boolean killingFriend = false;
			boolean killingTree = false;
			int enemLen = enemies.length;
			int allyLen = allies.length;
			int allyTreeLen = allyTrees.length;
			int neutTreeLen = neutralTrees.length;
			for(int k=0;k<enemLen;k++){
				if(rc.getType() == RobotType.SCOUT){
					// Scouts should only shoot if not Archon, or after Round 500 if archon
					if( roundNum>500 || enemies[k].getType() != RobotType.ARCHON){
						//continue with current enemy
						;
					}
					else{
						continue; // skip the loop, go to next enemy
					}
				}
				directionToCentre = here.directionTo(enemies[k].getLocation());
				killingFriend=false;
				killingTree=false;
				float enemyDistance = enemies[k].getLocation().distanceTo(here);
				
				for(int i = 0; i < allyLen; i++)
				{
					if (willHitBody(allies[i], directionToCentre, here) && allies[i].getLocation().distanceTo(here) < enemyDistance)
					{
						killingFriend = true;
						break;
					}
				}
				
				for(int i = 0; i < allyTreeLen; i++)
				{
					if (willHitBody(allyTrees[i], directionToCentre, here) && allyTrees[i].getLocation().distanceTo(here) < enemyDistance)
					{
						killingTree = true;
						break;
					}
				}
				
				if(!killingTree){
					// Don't check if already hitting allied tree since less chance and won't shoot anyway
					for(int i = 0; i < neutTreeLen; i++)
					{
						if (willHitBody(neutralTrees[i], directionToCentre, here) && neutralTrees[i].getLocation().distanceTo(here) < enemyDistance)
						{
							killingTree = true;
							break;
						}
					}
				}
				
				if (!(killingFriend || killingTree))
				{
					if (rc.canFireSingleShot())
					{
						rc.fireSingleShot(directionToCentre);
					}
					return true;
				}	
			}
		}
		return false;
	}
	
	enum Target {
		ENEMY, ALLY, NONE, TREE_N, TREE_A
	}
	//Do we need to add TREE_E
	
	public static boolean satisfactoryRatio(Target[] arr)
	{
		//returns false if no change needed (i.e. shoot) else true if shoot has to be aborted
		int allyRob=0, enemRob=0, allyTree=0;
		int len = arr.length;
		for(int i=0;i < len;i++){
			switch(arr[i]){
			case ENEMY: enemRob++; break;
			case ALLY: allyRob++; break;
			case TREE_A: allyTree++; break;
			case TREE_N: break;
			case NONE: break;
			default: break;
			}
		}
		if(arr.length==3){
			//triad shot
			// only one shot at most can hit ally+allyTree 
			if(allyRob+allyTree<=1){
				//Hit at least 2 enemies
				if(enemRob>=2){
					return true;
				}
				else{
					return false;
				}
			}
			else{
				return false;
			}
		}
		else{
			//pentad shot
			//only 2 shots at most can hit ally+allyTree
			if(allyRob+allyTree<=2){
				//At least 3 enemies
				if(enemRob>=2){
					return true;
				}
				else{
					return false;
				}
			}
			else{
				return false;
			}
		}
	}
	
	public static boolean tryPentadShot()throws GameActionException
	{	
		if(rc.canFirePentadShot()){
			Direction[] shotDirections = {null,null,null,null,null};
			Target[] TargetHit = {Target.NONE,Target.NONE,Target.NONE,Target.NONE,Target.NONE};
			BodyInfo[] BodyHit = {null,null,null,null,null};
			int enemiesLen = enemies.length;
			int alliesLen = allies.length;
			int allyTreesLen = allyTrees.length;
			int neutTreesLen = neutralTrees.length;
			// if enemiesLen is 0, all loops are skipped.
			for(int i=0;i<enemiesLen;i++){
				RobotInfo centerTarget = enemies[i];
				shotDirections[2] = here.directionTo(centerTarget.getLocation());
				shotDirections[1] = shotDirections[2].rotateLeftDegrees(GameConstants.PENTAD_SPREAD_DEGREES);
				shotDirections[0] = shotDirections[1].rotateLeftDegrees(GameConstants.PENTAD_SPREAD_DEGREES);
				shotDirections[3] = shotDirections[2].rotateRightDegrees(GameConstants.PENTAD_SPREAD_DEGREES);
				shotDirections[4] = shotDirections[3].rotateRightDegrees(GameConstants.PENTAD_SPREAD_DEGREES);
				
				TargetHit[0] = Target.NONE;
				TargetHit[1] = Target.NONE;
				TargetHit[2] = Target.ENEMY;
				TargetHit[3] = Target.NONE;
				TargetHit[4] = Target.NONE;
				
				BodyHit[0] = null;
				BodyHit[1] = null;
				BodyHit[2] = enemies[i];
				BodyHit[3] = null;
				BodyHit[4] = null;
				
				for(int j=0;j<enemiesLen;j++){
					if(willHitBody(enemies[j],shotDirections[0],here) && BodyHit[0] != null){
						//leftmost bullet
						TargetHit[0]  = Target.ENEMY;
						BodyHit[0] = enemies[j];
					}
					if(willHitBody(enemies[j],shotDirections[1],here) && BodyHit[1] != null){
						//left bullet
						TargetHit[1]  = Target.ENEMY;
						BodyHit[1] = enemies[j];
					}
					if(willHitBody(enemies[j],shotDirections[3],here) && BodyHit[3] != null){
						//right bullet
						TargetHit[3]  = Target.ENEMY;
						BodyHit[3] = enemies[j];
					}
					if(willHitBody(enemies[j],shotDirections[4],here) && BodyHit[4] != null){
						//rightmost bullet
						TargetHit[4]  = Target.ENEMY;
						BodyHit[4] = enemies[j];
					}
					if(TargetHit[0]==Target.ENEMY && TargetHit[1]==Target.ENEMY && TargetHit[3]==Target.ENEMY && TargetHit[4]==Target.ENEMY){
						break;
					}
				}
				
				for(int j=0;j<alliesLen;j++){
					for(int k=0;k<5;k++){
						if(willHitBody(allies[j],shotDirections[k],here) && TargetHit[k]!=Target.ALLY){
							if(TargetHit[k]==Target.NONE){
								BodyHit[k] = allies[j];
								TargetHit[k] = Target.ALLY;
							}
							else if(TargetHit[k]==Target.ENEMY){
								if(allies[j].getLocation().distanceTo(here) < BodyHit[k].getLocation().distanceTo(here)){
									BodyHit[k] = allies[j];
									TargetHit[k] = Target.ALLY;
								}
							}
						}
					}
					// if no TargetHit is NONE, loop can be broken
					if(TargetHit[0]!=Target.NONE && TargetHit[1]!=Target.NONE && TargetHit[2]!=Target.NONE && TargetHit[3]!=Target.NONE && TargetHit[4]!=Target.NONE){
						break;
					}
				}
				
				for(int j=0;j<allyTreesLen;j++){
					for(int k=0;k<5;k++){
						if(willHitBody(allyTrees[j],shotDirections[k],here) && TargetHit[k]!=Target.TREE_A){
							if(TargetHit[k]==Target.NONE){
								BodyHit[k] = allyTrees[j];
								TargetHit[k] = Target.TREE_A;
							}
							else{
								if(allyTrees[j].getLocation().distanceTo(here) <= BodyHit[k].getLocation().distanceTo(here)){
									// The = is to account for scouts on trees not getting hit
									BodyHit[k] = null;
									TargetHit[k] = Target.TREE_A;
								}
							}
						}
					}
				} 
				
				for(int j=0;j<neutTreesLen;j++){
					for(int k=0;k<5;k++){
						if(willHitBody(neutralTrees[j],shotDirections[k],here) && TargetHit[k]!=Target.TREE_N){
							
							if(TargetHit[k]==Target.NONE){
								BodyHit[k] = neutralTrees[j];
								TargetHit[k] = Target.TREE_N;
							}
							else{
								if(neutralTrees[j].getLocation().distanceTo(here) <= BodyHit[k].getLocation().distanceTo(here)){
									BodyHit[k] = neutralTrees[j];
									TargetHit[k] = Target.TREE_N;
								}
							}
						}
					}
				}
				
				//All possible obstacles have been taken into account. Now verify
				if(satisfactoryRatio(TargetHit)){
					rc.firePentadShot(here.directionTo(centerTarget.getLocation()));
					return true;
				} //else loop over next detected enemy
			}
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
