package potatobot;
import battlecode.common.*;

public class GardenerBot extends Globals
{
	private static boolean planningRequired = true;
	private static Direction plantMoveDirection = Direction.getEast();
	private static Direction plantDirection = Direction.getNorth();
	private static int treesIPlanted = 0;
	private static boolean haveGuard = false;
	
	public static void loop()throws GameActionException
	{
		amFarmer = shouldIBeAFarmer();
		if (amFarmer)
		{
			updateRobotCount();
			rc.broadcast(farmerIndex, farmers + 1);
			if (robotCount[farmerIndex] > 0)
			{
				getClear();
			}
			else
			{
				planningRequired = false;
			}
			int farmIndex = rc.readBroadcast(FARM_LOCATIONS_CHANNELS[0]);
			rc.broadcast(FARM_LOCATIONS_CHANNELS[farmIndex + 1], hashIt(here));
			rc.broadcast(FARM_LOCATIONS_CHANNELS[0], farmIndex + 1);
		}
		while (true)
		{
			header();
			if (amFarmer)
			{
				if (scouts >= 1)
				{
					if (planningRequired)
					{
						tryToPlantPlanned();
					}
					else
					{
						tryToPlantUnplanned();
					}
				}
				else
				{
					if (!tryToBuild())
					{
						TreeInfo[] allyTrees = rc.senseNearbyTrees(-1, us);
						if (allyTrees.length < 4)
						{
							tryToPlantUnplanned();
						}
					}
				}
			}
			else
			{
				tryToBuild();
				wander();
			}
			footer();
		}
	}
	
	private static boolean shouldIBeAFarmer()throws GameActionException
	{
		updateRobotCount();
		float farmerPercentage = ((float)farmers) / gardeners;
		int iAmGardenerNumber = rc.readBroadcast(GARDENER_NUMBER_CHANNEL);
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

	private static void getClear()throws GameActionException
	{
		if (!isClear(here, 3.5f))
		{
			MapLocation target = null;
			for (float distance = 1; distance <= 3; distance++)
			{
				for (float degrees = 0; degrees < 360; degrees += 20)
				{
					MapLocation there = here.add(Direction.getEast().rotateLeftDegrees(degrees), distance);
					if (isClear(there, 3.0f))
					{
						target = there;
						break;
					}
				}
				if (target != null)
				{
					break;
				}
			}
			int tries = 0;
			while (!isClear(here, 3.5f))
			{
				header();
				tryToBuild();
				if (target != null && !target.equals(here))
				{
					tryToMoveTowards(target);
				}
				else
				{
					wander();
				}
				tries++;
				if (tries == 50)
				{
					planningRequired = false;
					tryToPlantUnplanned();
					return;
				}
				footer();
			}
		}
	}
	
	private static void marchOn()throws GameActionException
	{
		tryToMove(plantMoveDirection);
        footer();
        header();
        tryToMove(plantMoveDirection);
        footer();
        header();
        tryToMove(plantMoveDirection);
        footer();
        header();
        tryToMove(plantMoveDirection);
        footer();
        header();
        tryToMoveThisMuch(plantMoveDirection, 0.1f);
        footer();
        header();
	}
	
	private static void moveBack()throws GameActionException
	{
		tryToMove(plantMoveDirection.opposite());
        footer();
        header();
        tryToMove(plantMoveDirection.opposite());
        footer();
        header();
        tryToMove(plantMoveDirection.opposite());
        footer();
        header();
        tryToMove(plantMoveDirection.opposite());
        footer();
        header();
        tryToMoveThisMuch(plantMoveDirection.opposite(), 0.1f);
	}	

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
		
		boolean stupidConditionForLumberjacks = lumberjacks <= scouts;
		if (stupidConditionForLumberjacks && lumberjacks < 20)
		{
			if (rc.hasRobotBuildRequirements(RobotType.LUMBERJACK))
			{
				return spawn(RobotType.LUMBERJACK);
			}
		}

		boolean stupidConditionForSoldiers = (soldiers < farmers) || (nonAllyTreeDensity < 0.125f);
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
		while (tries < 100)
		{
			if (type == RobotType.LUMBERJACK)
			{
				for (TreeInfo tree : neutralTrees)
				{
					Direction buildDirection = here.directionTo(tree.getLocation());
					if (rc.canBuildRobot(type, buildDirection))
					{
						rc.buildRobot(type, buildDirection);
						robotInit(type);
						return true;
					}
				}
			}
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
	
	private static boolean tryToPlantPlanned()throws GameActionException
	{
		if (!haveGuard)
		{
			if (spawn(RobotType.SOLDIER))
			{
				haveGuard = true;
			}
		}
		if (bullets > GameConstants.BULLET_TREE_COST)
		{
			if (treesIPlanted < 4)
			{
	            marchOn();
	            int tries = 0;
	            boolean iHaveFailedYou = false;
	            while (!rc.canPlantTree(plantDirection))
	            {
	            	footer();
	            	header();
	            	tries++;
	            	if (tries == 20)
	            	{
	            		iHaveFailedYou = true;
	            	}
	            }
	            if (iHaveFailedYou)
	            {
	            	planningRequired = false;
	            	moveBack();
	            	return false;
	            }
                rc.plantTree(plantDirection);
                treesIPlanted++;
                updateTreeCount();
                rc.broadcast(TREE_CHANNEL, treesPlanted + 1);
                plantDirection = plantDirection.opposite();
                moveBack();
                if (treesIPlanted == 2)
                {
                	plantMoveDirection = plantMoveDirection.opposite();
                }
                return true;
			}
			else
			{
				planningRequired = false;
				return tryToPlantUnplanned();
			}
		}
		return false;
	}

	private static boolean tryToPlantUnplanned()throws GameActionException
	{
		if (!haveGuard)
		{
			if (spawn(RobotType.SOLDIER))
			{
				haveGuard = true;
			}
		}
		int angle = 0;
		while (angle <= 360)
		{
            if (rc.canPlantTree(plantDirection))
			{
            	rc.plantTree(plantDirection);
                treesIPlanted++;
                updateTreeCount();
                rc.broadcast(TREE_CHANNEL, treesPlanted + 1);
                return true;
			}
            plantDirection = plantDirection.rotateLeftDegrees(1);
            angle += 1;
		}
		return false;
	}

	private static void tryToWater()throws GameActionException
	{
        if(rc.canWater()) 
        {
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
            for (TreeInfo tree : nearbyTrees)
            {
                if(tree.getHealth() < GameConstants.BULLET_TREE_MAX_HEALTH - GameConstants.WATER_HEALTH_REGEN_RATE) 
                {
                    if (rc.canWater(tree.getID())) 
                    {
                        rc.water(tree.getID());
                        break;
                    }
                }
            }
        }
    }
	
	
	// Overridden footer for gardener, always try to water and then call global footer
	
	public static void footer()throws GameActionException
	{
		tryToWater();
		Globals.footer();
	}
}
