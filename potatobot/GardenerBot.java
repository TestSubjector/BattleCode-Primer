package potatobot;
import battlecode.common.*;

public class GardenerBot extends Globals
{
	private static boolean amFirstGardener = false;
	private static RobotType typeToSpawnFirst;
	private static Direction plantDirection;
	public static void loop()throws GameActionException
	{
		int spawnRoundNum = rc.getRoundNum();
		int maxICanPlant = 0;
		amFarmer = shouldIBeAFarmer();
		if (amFarmer)
		{
			rc.broadcast(farmerIndex, farmers + 1);
		}
		else
		{
			rc.broadcast(spawnerIndex, spawners + 1);
		}
		plantDirection = here.directionTo(theirInitialArchons[0]).rotateLeftDegrees(75);
		updateRobotCount();
		updateNearbyObjects();
		if (gardeners == 1)
		{
			amFirstGardener = true;
			if (neutralTrees.length > 10)
			{
				typeToSpawnFirst = RobotType.LUMBERJACK;
			}
			else
			{
				typeToSpawnFirst = RobotType.SOLDIER;
			}
		}
		while (true)
		{
			try
			{
				header();
				BodyInfo[][] array = {enemies, allies, neutralTrees, enemyTrees, allyTrees};
				Direction awayFromNearestObstacle = findDirectionAwayFromNearestObstacle(array);		
				// rc.setIndicatorLine(here, here.add(awayFromNearestObstacle), 255, 255, 255);
				if (amFirstGardener)
				{
					tryToMove(awayFromNearestObstacle);
					if (typeToSpawnFirst != null)
					{
						if (tryToSpawn(typeToSpawnFirst))
						{
							if (typeToSpawnFirst.equals(RobotType.LUMBERJACK))
							{
								typeToSpawnFirst = RobotType.SOLDIER;
							}
							else
							{
								typeToSpawnFirst = null;
							}
						}
						else
						{
							footer();
							continue;
						}
					}
					else
					{
						amFirstGardener = false;
					}
				}
				else if (amFarmer)
				{
					if (roundNum - spawnRoundNum > 4)
					{
						if (rc.hasTreeBuildRequirements() && rc.getBuildCooldownTurns() <= 0)
						{
							tryToPlant();
						}
					}
					else if (roundNum - spawnRoundNum == 4)
					{
						int farmIndex = rc.readBroadcast(FARM_LOCATIONS_CHANNELS[0]);
						rc.broadcast(FARM_LOCATIONS_CHANNELS[farmIndex + 1], hashIt(here));
						rc.broadcast(FARM_LOCATIONS_CHANNELS[0], farmIndex + 1);
					}
					else
					{
						tryToMove(awayFromNearestObstacle);
					}
				}
				else
				{
					maxICanPlant = findMaxICanPlant();
					if (roundNum - spawnRoundNum < 30)
					{
						tryToMove(awayFromNearestObstacle);
						tryToBuild();
					}
					else if (maxICanPlant > 2)
					{
						if (roundNum - spawnRoundNum == 30)
						{
							int spawnLocationIndex = rc.readBroadcast(SPAWN_LOCATIONS_CHANNELS[0]);
							rc.broadcast(SPAWN_LOCATIONS_CHANNELS[spawnLocationIndex + 1], hashIt(here));
							rc.broadcast(SPAWN_LOCATIONS_CHANNELS[0], spawnLocationIndex + 1);
						}
						tryToPlant();
					}
					else if (roundNum - spawnRoundNum > 80)
					{
						tryToBuild();
					}
				}
				footer();
			}
			catch (GameActionException e)
			{
				System.out.println("Catch kiya");
				footer();
			}
		}
	}
	


	// Farming functions 
	
	private static boolean shouldIBeAFarmer()throws GameActionException
	{
		updateRobotCount();
		float farmerPercentage = ((float)farmers) / gardeners;
		int iAmGardenerNumber = rc.readBroadcast(GARDENER_NUMBER_CHANNEL) + 1;
		// int deadFarmers = rc.readBroadcast(DEAD_FARMERS_CHANNEL);
		boolean answer = false;
		if (farmerPercentage * farmerPercentage < gameProgressPercentage)
		{
			answer = true;
		}
		else if (iAmGardenerNumber % 2 == 0)
		{
			answer = true;
		}
		else
		{
			answer = false;
		}
		rc.broadcast(GARDENER_NUMBER_CHANNEL, iAmGardenerNumber);
		return answer;
	}
	
	private static int findMaxICanPlant()throws GameActionException
	{
		Direction checkDirection = plantDirection;
		int a = 0;
		for (int i = 0; i < 6; i++)
		{
			if (!rc.isCircleOccupiedExceptByThisRobot(here.add(checkDirection, 2.2f), 1f))
			{
				a++;
			}
			checkDirection = checkDirection.rotateLeftDegrees(60);
		}
		return a;
	}

	private static boolean tryToPlant()throws GameActionException
	{
		int tries = 1;
		while (tries <= 6)
		{
            if (rc.canPlantTree(plantDirection))
			{
            	rc.plantTree(plantDirection);
                updateTreeCount();
                rc.broadcast(TREE_CHANNEL, treesPlanted + 1);
                return true;
			}
            plantDirection = plantDirection.rotateLeftDegrees(60);
            tries++;
		}
		return false;
	}

	private static boolean tryToWater()throws GameActionException
	{
        if(rc.canWater()) 
        {
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
            int loopLength = nearbyTrees.length;
			for (int i = 0; i < loopLength; i++)
			{
				TreeInfo tree = nearbyTrees[i];
				float treeHealth = tree.getHealth();
				if(treeHealth < GameConstants.BULLET_TREE_MAX_HEALTH - GameConstants.WATER_HEALTH_REGEN_RATE) 
                {
                    if (rc.canWater(tree.getID()))
                    {
                        rc.water(tree.getID());
                        return true;
                    }
                }
            }
        }
        return false;
    }
	
	
	// Robot building functions
	
	private static boolean tryToBuild()throws GameActionException
	{		
		// Try to incorporate TREES_CHANNEL
		boolean conditionForScouts = (scouts < 2 && roundNum < 75) || ((scouts < Math.ceil(8d * gameProgressPercentage)) && soldiers >= 2);
		if (conditionForScouts || scouts < 1)
		{
			if (rc.hasRobotBuildRequirements(RobotType.SCOUT))
			{
				return tryToSpawn(RobotType.SCOUT);
			}
		}
		
		boolean conditionForLumberjacks = (neutralTrees.length > 12 || lumberjacks < 2) || (lumberjacks < 4 && soldiers >= 5);
		if (conditionForLumberjacks && lumberjacks < 20)
		{
			if (rc.hasRobotBuildRequirements(RobotType.LUMBERJACK))
			{
				return tryToSpawn(RobotType.LUMBERJACK);
			}
		}

		boolean conditionForSoldiers =  (soldiers <= 7) || (tanks * 11 >= soldiers * 7);
		if (conditionForSoldiers && soldiers < 30)
		{
			if (rc.hasRobotBuildRequirements(RobotType.SOLDIER))
			{
				return tryToSpawn(RobotType.SOLDIER);
			}
		}
		
		if (tanks < 8 && amIATankSpawningSpawner())
		{
			if (rc.hasRobotBuildRequirements(RobotType.TANK))
			{
				return tryToSpawn(RobotType.TANK);
			}
		}
		return false;
	}
	
	private static boolean tryToSpawn(RobotType type)throws GameActionException
	{
		if (!rc.hasRobotBuildRequirements(type))
		{
			return false;
		}
		int tries = 0;
		Direction spawnDirection = Direction.getNorth();
		if (type == RobotType.LUMBERJACK && neutralTrees.length != 0)
		{
			spawnDirection = here.directionTo(neutralTrees[0].getLocation());
		}
		while (tries < 90)
		{
			if (rc.canBuildRobot(type, spawnDirection))
			{
				rc.buildRobot(type, spawnDirection);
				robotInit(type);
				return true;
			}
			else
			{
				spawnDirection = spawnDirection.rotateLeftDegrees(4);
			}
			tries++;
		}
		return false;
	}


	private static boolean amIATankSpawningSpawner()throws GameActionException
	{
		float minDist = 500000f;
		int loopLength = 9;
		closestArchonLocation = theirInitialArchons[0];
		for (int i = 1; i < loopLength; i += 3)
		{
			int hashedLocation = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[i + 1]);
			if (hashedLocation != 0)
			{
				MapLocation unhashedLocation = unhashIt(hashedLocation);
				float distanceToArchon = here.distanceTo(unhashedLocation);
				if (distanceToArchon < minDist)
				{
					minDist = distanceToArchon;
					closestArchonLocation = unhashedLocation;
				}
			}
		}
		minDist = 500000f;
		loopLength = rc.readBroadcast(SPAWN_LOCATIONS_CHANNELS[0]);
		MapLocation spawnerLocation = here;
		for (int i = 1; i <= loopLength; i++)
		{
			int hashedLocation = rc.readBroadcast(SPAWN_LOCATIONS_CHANNELS[i]);
			MapLocation unhashedLocation = unhashIt(hashedLocation);
			float distanceToArchon = closestArchonLocation.distanceTo(unhashedLocation);
			if (distanceToArchon < minDist)
			{
				minDist = distanceToArchon;
				spawnerLocation = unhashedLocation;
			}
		}
		return (spawnerLocation.distanceTo(here) < 1f);
	}
	
	
	// Overridden footer for gardener, always try to water and then call global footer
	
	public static void footer()throws GameActionException
	{
		tryToWater();
		Globals.footer();
	}
}
