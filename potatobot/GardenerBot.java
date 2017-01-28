package potatobot;
import battlecode.common.*;

public class GardenerBot extends Globals
{
	private static int treesIPlanted = 0;
	private static boolean amFirstGardener = false;
	private static RobotType typeToSpawnFirst = RobotType.SCOUT;
	private static Direction farmerMovingDirection = here.directionTo(theirInitialArchons[0]).rotateRightDegrees(60);
	
	public static void loop()throws GameActionException
	{
		amFarmer = shouldIBeAFarmer();
		if (amFarmer)
		{
			updateRobotCount();
			rc.broadcast(farmerIndex, farmers + 1);
			if (robotCount[farmerIndex] == 0)
			{
				amFirstGardener = true;
			}
			int farmIndex = rc.readBroadcast(FARM_LOCATIONS_CHANNELS[0]);
			rc.broadcast(FARM_LOCATIONS_CHANNELS[farmIndex + 1], hashIt(here));
			rc.broadcast(FARM_LOCATIONS_CHANNELS[0], farmIndex + 1);
		}
		int moveNumber = 0;
		while (true)
		{
			try
			{
				header();
				if (amFirstGardener)
				{
					tryToMove(farmerMovingDirection);
					moveNumber = (moveNumber + 1) % 16;
					if (moveNumber % 2 == 0)
					{
						farmerMovingDirection = farmerMovingDirection.rotateLeftDegrees(51.43f);
					}
					if (typeToSpawnFirst != null && spawn(typeToSpawnFirst))
					{
						if (typeToSpawnFirst.equals(RobotType.SCOUT))
						{
							if (neutralTrees.length > 5)
							{
								typeToSpawnFirst = RobotType.LUMBERJACK;
							}
							else
							{
								typeToSpawnFirst = RobotType.SOLDIER;
							}
						}
						else if (typeToSpawnFirst.equals(RobotType.LUMBERJACK))
						{
							typeToSpawnFirst = RobotType.SOLDIER;
						}
						else if (typeToSpawnFirst.equals(RobotType.SOLDIER))
						{
							typeToSpawnFirst = null;
						}
					}
					else if (treesIPlanted < 3)
					{
						tryToPlantUnplanned();
					}
					if (typeToSpawnFirst == null)
					{
						amFirstGardener = false;
					}
				}
				else if (amFarmer)
				{
					tryToMove(farmerMovingDirection);
					moveNumber = (moveNumber + 1) % 16;
					if (moveNumber % 2 == 0)
					{
						tryToPlantUnplanned();
						farmerMovingDirection = farmerMovingDirection.rotateLeftDegrees(51.43f);
					}
					System.out.println("After Unplanned Planting : " + Clock.getBytecodesLeft());
				}
				else
				{
					if (!tryToMove(movingDirection))
					{
						movingDirection = randomDirection();
					}
					tryToBuild();
					System.out.println("After tryToBuild : " + Clock.getBytecodesLeft());
				}
				System.out.println("Before Footer : " + Clock.getBytecodesLeft());
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

	private static boolean tryToPlantUnplanned()throws GameActionException
	{
		if (!rc.hasTreeBuildRequirements() && rc.getBuildCooldownTurns() <= 0)
		{
			return false;
		}
		int tries = 0;
		while (tries <= 10)
		{
			Direction plantDirection = farmerMovingDirection.rotateRightDegrees(85);
			System.out.println("Planting : " + Clock.getBytecodesLeft());
            if (rc.canPlantTree(plantDirection))
			{
            	rc.plantTree(plantDirection);
                treesIPlanted++;
                updateTreeCount();
                rc.broadcast(TREE_CHANNEL, treesPlanted + 1);
                return true;
			}
            plantDirection = plantDirection.rotateRightDegrees(1);
            tries++;
		}
		return false;
	}

	private static void tryToWater()throws GameActionException
	{
        if(rc.canWater()) 
        {
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
            int loopLength = nearbyTrees.length;
			for (int i=0;i<loopLength;i++)
			{	
				TreeInfo tree = nearbyTrees[i];
				float treeHealth = tree.getHealth();
				if(treeHealth < GameConstants.BULLET_TREE_MAX_HEALTH - GameConstants.WATER_HEALTH_REGEN_RATE) 
                {
                    if (rc.canWater(tree.getID())) 
                    {
                        rc.water(tree.getID());
                    }
                }
            }
        }
    }
	
	
	// Robot building functions
	
	private static boolean tryToBuild()throws GameActionException
	{		
		if (scouts < 2 || (scouts < Math.ceil((15d * roundNum) / 3000d)))
		{
			if (rc.hasRobotBuildRequirements(RobotType.SCOUT))
			{
				return spawn(RobotType.SCOUT);
			}
		}
		
		// Replace stupidCondition with some other condition
		
		boolean stupidConditionForLumberjacks = lumberjacks <= scouts * 2;
		if (stupidConditionForLumberjacks && lumberjacks < 20)
		{
			if (rc.hasRobotBuildRequirements(RobotType.LUMBERJACK))
			{
				return spawn(RobotType.LUMBERJACK);
			}
		}

		boolean stupidConditionForSoldiers = (soldiers < farmers * 2);
		if (stupidConditionForSoldiers && soldiers < 25)
		{
			if (rc.hasRobotBuildRequirements(RobotType.SOLDIER))
			{
				return spawn(RobotType.SOLDIER);
			}
		}
		
		/* Make tanks later		
		stupidCondition = tanks * 2 < soldiers;
		if (stupidCondition && tanks < 20)
		{
			if (rc.hasRobotBuildRequirements(RobotType.TANK))
			{
				return spawn(RobotType.TANK);
			}
		}
		*/
		return false;
	}
	
	private static boolean spawn(RobotType type)throws GameActionException
	{
		if (!rc.hasRobotBuildRequirements(type))
		{
			return false;
		}
		int tries = 0;
		Direction spawnDirection = here.directionTo(theirInitialArchons[0]);
		while (tries < 20)
		{
			if (rc.canBuildRobot(type, spawnDirection))
			{
				rc.buildRobot(type, spawnDirection);
				robotInit(type);
				return true;
			}
			else
			{
				spawnDirection = spawnDirection.rotateLeftDegrees(18);
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
