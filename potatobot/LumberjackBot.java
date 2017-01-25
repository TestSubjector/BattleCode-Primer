package potatobot;
import battlecode.common.*;

public class LumberjackBot extends Globals
{
	public static void loop()throws GameActionException
	{
		allies = rc.senseNearbyRobots(-1, us);
		movingDirection = allies[0].getLocation().directionTo(here);
		while (true)
		{
			header();

			findMoveDirection();
			
			if (!(tryToStrike() || tryToChop()))
			{
				if (!tryToMove(movingDirection))
				{
					float angle = ((float)Math.random() * 90f);
					double choice = Math.random();
					if (choice > 0.5)
					{
						movingDirection = movingDirection.opposite().rotateLeftDegrees(angle);
					}
					else
					{
						movingDirection = movingDirection.opposite().rotateRightDegrees(angle);
					}
				}
			}
			
			footer();
		}
	}
	
	private static void findMoveDirection()throws GameActionException
	{
		if (neutralTrees.length != 0)
		{
			TreeInfo closestNeutralTree = neutralTrees[0];
			movingDirection = here.directionTo(closestNeutralTree.getLocation());
		}
		if (enemyTrees.length != 0)
		{
			TreeInfo closestEnemyTree = enemyTrees[0];
			movingDirection = here.directionTo(closestEnemyTree.getLocation());
		}
		for (RobotInfo enemy : enemies)
		{
			RobotType enemyType = enemy.getType();
			MapLocation enemyLocation = enemy.getLocation();
			if (!(enemyType == RobotType.SOLDIER || enemyType == RobotType.TANK))
			{
				movingDirection = here.directionTo(enemyLocation);
				break;
			}
		}
		if (lumberjackTarget != -1)
		{
			movingDirection = here.directionTo(lumberjackTargetLocation);
		}
	}
	
	private static boolean tryToStrike()throws GameActionException 
	{
		RobotInfo[] allRobots = rc.senseNearbyRobots(2.1f);
		int alliesInRangeOfStrike = 0;
		int enemiesInRangeOfStrike = 0;
		for (RobotInfo robot : allRobots)
		{
			if (robot.getTeam() == us)
			{
				alliesInRangeOfStrike++;
			}
			else
			{
				enemiesInRangeOfStrike++;
			}
		}
		if (rc.canStrike() && alliesInRangeOfStrike * 2 < enemiesInRangeOfStrike * 3)
		{
			rc.strike();
			return true;
		}
		return false;
		
	}

	private static boolean tryToChop()throws GameActionException 
	{
		boolean returnValue = false;
		if (lumberjackTarget != -1)
		{
			if (rc.canInteractWithTree(lumberjackTarget))
			{
				rc.chop(lumberjackTarget);
				if (!rc.canInteractWithTree(lumberjackTarget))
				{
					lumberjackTarget = -1;
				}
				returnValue = true;
			}
			else if (rc.canSenseLocation(lumberjackTargetLocation))
			{
				lumberjackTarget = -1;
			}
		}
		if (!returnValue && enemyTrees.length != 0)
		{
			if (enemyTrees.length != 0 && rc.canChop(enemyTrees[0].getID()))
			{
				rc.chop(enemyTrees[0].getID());
				returnValue = true;
			}
		}
		if (!returnValue && neutralTrees.length != 0)
		{
			if (neutralTrees.length != 0 && rc.canChop(neutralTrees[0].getID()))
			{
				rc.chop(neutralTrees[0].getID());
				returnValue = true;
			}
		}
		return returnValue;
	}
}
