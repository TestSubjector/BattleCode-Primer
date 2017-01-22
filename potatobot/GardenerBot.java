package potatobot;
import battlecode.common.*;

public class GardenerBot extends Globals
{
	private static boolean amFarmer;
	private static boolean planningRequired = true;
	private static Direction plantMoveDirection = Direction.getEast();
	private static Direction plantDirection = Direction.getNorth();
	private static int treesIPlanted = 0;
	private static int buildThis = 0;
	
	public static void loop()throws GameActionException
	{
		updateRobotCount();
		int gardeners = robotCount[RobotType.GARDENER.ordinal()];
		rc.broadcast(RobotType.GARDENER.ordinal(), gardeners + 1);
		amFarmer = shouldIBeAFarmer();
		if (amFarmer)
		{
			getClear();
		}
		while (true)
		{
			header();
			if (amFarmer)
			{
				if (planningRequired)
				{
					tryToPlantPlanned();
				}
				else
				{
					tryToPlantUnplanned();
				}
				if (buildThis == 3)
				{
					int scouts = robotCount[RobotType.SCOUT.ordinal()];
					if (scouts < robotCountMax[RobotType.SCOUT.ordinal()])
					{
						if (spawn(RobotType.SCOUT))
						{
							rc.broadcast(BUILD_CHANNEL, 2);
						}
					}
					else
					{
						rc.broadcast(BUILD_CHANNEL, 2);
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
		int farmIndex = rc.readBroadcast(FARM_LOCATIONS_CHANNELS[0]);
		farmIndex++;
		rc.broadcast(FARM_LOCATIONS_CHANNELS[farmIndex], hashIt(here));
		rc.broadcast(FARM_LOCATIONS_CHANNELS[0], farmIndex);
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
	
	private static boolean shouldIBeAFarmer()throws GameActionException
	{
		updateRobotCount();
		int gardeners = robotCount[myType.ordinal()];
		if (gardeners % 2 == 1)
		{
			return true;
		}
		return false;
	}
	

	private static void tryToBuild()throws GameActionException
	{		

		buildThis = rc.readBroadcast(BUILD_CHANNEL);
		switch (buildThis)
		{
			case 0:
				rc.broadcast(BUILD_CHANNEL, 3);
				break;
	
			case 2:
				int lumberjacks = robotCount[RobotType.LUMBERJACK.ordinal()];
				if (lumberjacks < robotCountMax[RobotType.LUMBERJACK.ordinal()])
				{
					if (spawn(RobotType.LUMBERJACK))
					{
						rc.broadcast(BUILD_CHANNEL, 4);
					}
				}
				else
				{
					rc.broadcast(BUILD_CHANNEL, 4);
				}
				break;
			
			case 3:
				int scouts = robotCount[RobotType.SCOUT.ordinal()];
				if (scouts < robotCountMax[RobotType.SCOUT.ordinal()])
				{
					if (spawn(RobotType.SCOUT))
					{
						rc.broadcast(BUILD_CHANNEL, 2);
					}
				}
				else
				{
					rc.broadcast(BUILD_CHANNEL, 2);
				}
				break;
			
			case 4:
				int soldiers = robotCount[RobotType.SOLDIER.ordinal()];
				if (soldiers < robotCountMax[RobotType.SOLDIER.ordinal()])
				{
					if (spawn(RobotType.SOLDIER))
					{
						rc.broadcast(BUILD_CHANNEL, 3);
					}
				}
				else
				{
					rc.broadcast(BUILD_CHANNEL, 3);
				}
				break;	
				
			default:
				System.out.println("Bro wtf");
		}
	}
	
	private static boolean spawn(RobotType type)throws GameActionException
	{
		int tries = 0;
		while (tries < 20)
		{
			if (type == RobotType.LUMBERJACK)
			{
				for (TreeInfo tree : neutralTrees)
				{
					Direction buildDirection = here.directionTo(tree.getLocation());
					if (rc.canBuildRobot(type, buildDirection))
					{
						rc.buildRobot(type, buildDirection);
						rc.broadcast(type.ordinal(), robotCount[type.ordinal()] + 1);
						return true;
					}
				}
			}
			Direction randomDir = randomDirection();
			if (rc.canBuildRobot(type, randomDir))
			{
				rc.buildRobot(type, randomDir);
				rc.broadcast(type.ordinal(), robotCount[type.ordinal()] + 1);
				return true;
			}
			tries++;
		}
		return false;
	}
	
	private static void tryToPlantPlanned()throws GameActionException
	{
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
	            	return;
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
			}
			else
			{
				planningRequired = false;
				tryToPlantUnplanned();
			}
		}
	}

	private static boolean tryToPlantUnplanned()throws GameActionException
	{
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
            plantDirection = plantDirection.rotateLeftDegrees(2);
            angle += 2;
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
