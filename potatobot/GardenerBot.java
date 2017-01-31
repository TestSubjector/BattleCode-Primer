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
		plantDirection = here.directionTo(theirInitialArchons[0]).rotateLeftDegrees(60);
		if (amFarmer)
		{
			updateRobotCount();
			updateNearbyObjects();
			rc.broadcast(farmerIndex, farmers + 1);
			if (robotCount[farmerIndex] == 0)
			{
				amFirstGardener = true;
			}
			if (neutralTrees.length > 10)
			{
				typeToSpawnFirst = RobotType.LUMBERJACK;
			}
			else if (archonDistance < 30f)
			{
				typeToSpawnFirst = RobotType.SOLDIER;
			}
			else
			{
				typeToSpawnFirst = RobotType.SCOUT;
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
					if (typeToSpawnFirst != null && spawn(typeToSpawnFirst))
					{
						typeToSpawnFirst = RobotType.SOLDIER;
						if (roundNum - spawnRoundNum < 2)
						{
							footer();
							continue;
						}
						if (typeToSpawnFirst.equals(RobotType.SOLDIER))
						{
							typeToSpawnFirst = null;
						}
					}
					if (typeToSpawnFirst == null)
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
							int farmIndex = rc.readBroadcast(FARM_LOCATIONS_CHANNELS[0]);
							rc.broadcast(FARM_LOCATIONS_CHANNELS[farmIndex + 1], hashIt(here));
							rc.broadcast(FARM_LOCATIONS_CHANNELS[0], farmIndex + 1);
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
	
	private static boolean shouldIBeAFarmer()throws GameActionException
	{
		updateRobotCount();
		float farmerPercentage = ((float)farmers) / gardeners;
		int iAmGardenerNumber = rc.readBroadcast(GARDENER_NUMBER_CHANNEL);
		// int deadFarmers = rc.readBroadcast(DEAD_FARMERS_CHANNEL);
		boolean answer = false;
		if (farmerPercentage * farmerPercentage <= gameProgressPercentage)
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
		rc.broadcast(GARDENER_NUMBER_CHANNEL, iAmGardenerNumber + 1);
		return answer;
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
		if (scouts < 2 || (scouts < Math.ceil((8d * roundNum) / 3000d)))
		{
			if (rc.hasRobotBuildRequirements(RobotType.SCOUT))
			{
				return spawn(RobotType.SCOUT);
			}
		}
		
		// Replace stupidCondition with some other condition
		
		boolean stupidConditionForLumberjacks = neutralTrees.length > 15 || lumberjacks < 2;
		if (stupidConditionForLumberjacks && lumberjacks < 20)
		{
			if (rc.hasRobotBuildRequirements(RobotType.LUMBERJACK))
			{
				return spawn(RobotType.LUMBERJACK);
			}
		}

		boolean stupidConditionForSoldiers =  (soldiers <= 5) || (tanks * 3 >= soldiers * 2);
		if (stupidConditionForSoldiers && soldiers < 30)
		{
			if (rc.hasRobotBuildRequirements(RobotType.SOLDIER))
			{
				return spawn(RobotType.SOLDIER);
			}
		}
		
		boolean stupidConditionForTanks = allyTrees.length == 0;
		if (stupidConditionForTanks && tanks < 10)
		{
			if (rc.hasRobotBuildRequirements(RobotType.TANK))
			{
				return spawn(RobotType.TANK);
			}
		}
		return false;
	}
	
	private static boolean spawn(RobotType type)throws GameActionException
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

	
	// Overridden footer for gardener, always try to water and then call global footer
	
	public static void footer()throws GameActionException
	{
		tryToWater();
		Globals.footer();
	}
}
