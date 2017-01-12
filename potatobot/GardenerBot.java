package potatobot;
import battlecode.common.*;

public class GardenerBot extends Globals
{
	public static void loop()throws GameActionException
	{
		while (true)
		{
			header();
			int scouts = robotCount[RobotType.SCOUT.ordinal()];
			if (bullets > 1.25 * RobotType.SCOUT.bulletCost && scouts < robotCountMax[RobotType.SCOUT.ordinal()])
			{
				spawn(RobotType.SCOUT);
			}
			else
			{
				tryToPlant();
				tryToWater();
			}
			wander();
			footer();
		}
	}
	
	public static void spawn(RobotType type)throws GameActionException
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
	
	public static void tryToPlant()throws GameActionException
	{
		if (bullets > GameConstants.BULLET_TREE_COST)
		{
			int tries = 0;
			while (tries < 50)
			{
				Direction randomDir = randomDirection();
				if (rc.canPlantTree(randomDir)) 
				{
	                rc.plantTree(randomDir);
	                return;
	            }
				tries++;
			}
		}
	}
	
	public static void tryToWater()throws GameActionException
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
