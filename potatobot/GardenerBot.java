package potatobot;
import battlecode.common.*;

public class GardenerBot extends Globals
{
	private static boolean isFarmer;
	private static Direction plantDirection;
	public static void loop()throws GameActionException
	{
		isFarmer = shouldIBeAFarmer();
		plantDirection = here.directionTo(theirInitialArchonCentre);
		while (true)
		{
			header();
			if (isFarmer)
			{
				tryToPlant();
			}
			else
			{
				int scouts = robotCount[RobotType.SCOUT.ordinal()];
				if (treesPlanted > 6 && scouts < robotCountMax[RobotType.SCOUT.ordinal()])
				{
					spawn(RobotType.SCOUT);
				}
			}
			tryToWater();
			wander();
			footer();
		}
	}
	
	private static boolean shouldIBeAFarmer()throws GameActionException
	{
		updateRobotCount();
		int gardeners = robotCount[myType.ordinal()];
		if (gardeners <= 4)
		{
			return true;
		}
		else
		{
			if (gardeners % 3 == 1)
			{
				return true;
			}
		}
		return false;
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
			int tries = 0;
			while (tries < 10)
			{
				plantDirection = plantDirection.rotateLeftDegrees(5);
				if (rc.canPlantTree(plantDirection)) 
				{
	                rc.plantTree(plantDirection);
	                return;
	            }
				tries++;
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
}
