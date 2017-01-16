package potatobot;
import battlecode.common.*;

public class GardenerBot extends Globals
{
	private static boolean amFarmer;
	private static Direction plantMoveDirection = Direction.getEast();
	private static Direction plantDirection = Direction.getNorth();
	private static int treesIPlanted = 0;
	public static void loop()throws GameActionException
	{
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
				tryToPlant();
			}
			else
			{
				int scouts = robotCount[RobotType.SCOUT.ordinal()];
				if (scouts < robotCountMax[RobotType.SCOUT.ordinal()])
				{
					spawn(RobotType.SCOUT);
				}
				wander();
			}
			footer();
		}
	}
	
	private static void getClear()throws GameActionException
	{
		if (!isClear(here))
		{
			MapLocation target = null;
			for (float distance = 1; distance <= 3; distance++)
			{
				for (float degrees = 0; degrees < 360; degrees += 20)
				{
					MapLocation there = here.add(Direction.getEast().rotateLeftDegrees(degrees), distance);
					if (isClear(there))
					{
						target = there;
						rc.setIndicatorLine(here, there, 5, 5, 5);
						break;
					}
				}
				if (target != null)
				{
					break;
				}
			}
			int attempts = 0;
			while (!isClear(here))
			{
				header();
				if (target != null && !target.equals(here))
				{
					tryToMoveTowards(target);
				}
				else
				{
					wander();
				}
				footer();
				attempts++;
				if (attempts > 25)
				{
					amFarmer = false;
					return;
				}
			}
		}
	}

	private static boolean isClear(MapLocation location)throws GameActionException
	{
		if (!rc.isCircleOccupiedExceptByThisRobot(location, (float)3) && rc.onTheMap(location, (float)3))
		{
			return true;
		}
		return false;
	}

	private static boolean shouldIBeAFarmer()throws GameActionException
	{
		updateRobotCount();
		return true;
		/*int gardeners = robotCount[myType.ordinal()];
		if (gardeners % 2 == 0)
		{
			return true;
		}
		return false;*/
	}
	
	private static void spawn(RobotType type)throws GameActionException
	{
		int tries = 0;
		while (tries < 5)
		{
			Direction randomDir = randomDirection();
			if (rc.canBuildRobot(type, randomDir))
			{
				rc.buildRobot(type, randomDir);
				rc.broadcast(type.ordinal(), robotCount[type.ordinal()] + 1);
				return;
			}
			tries++;
		}
	}
	
	private static void tryToPlant()throws GameActionException
	{
		if (bullets > GameConstants.BULLET_TREE_COST)
		{
			if (treesIPlanted < 4)
			{
	            tryToMove(plantMoveDirection);
	            footer();
	            header();
	            tryToMove(plantMoveDirection);
	            footer();
	            header();
	            tryToMoveThisMuch(plantMoveDirection, 0.1f);
	            footer();
	            header();
	            rc.setIndicatorLine(here, here.add(plantDirection, 4), 24, 1, 22);
	            while (!rc.canPlantTree(plantDirection))
	            {
	            	footer();
	            	header();
	            }
                rc.plantTree(plantDirection);
                treesIPlanted++;
                updateTreeCount();
                rc.broadcast(TREE_CHANNEL, treesPlanted + 1);
                plantDirection = plantDirection.opposite();
				tryToMove(plantMoveDirection.opposite());
                footer();
                header();
                tryToMove(plantMoveDirection.opposite());
	            footer();
	            header();
	            tryToMoveThisMuch(plantMoveDirection.opposite(), 0.1f);
                if (treesIPlanted == 2)
                {
                	plantMoveDirection = plantMoveDirection.opposite();
                }
			}
			else
			{
				if (rc.canPlantTree(plantDirection))
				{
	                rc.plantTree(plantDirection);
	                plantDirection = plantDirection.rotateLeftDegrees(90);
	                treesIPlanted++;
	                updateTreeCount();
	                rc.broadcast(TREE_CHANNEL, treesPlanted + 1);
	                return;
				}
			}
		}
	}
	
	private static void tryToWater()throws GameActionException
	{
        if(rc.canWater()) 
        {
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
            for (TreeInfo tree : nearbyTrees)
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
	
	public static void footer()throws GameActionException
	{
		tryToWater();
		Globals.footer();
	}
}
