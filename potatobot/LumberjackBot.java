package potatobot;
import battlecode.common.*;

public class LumberjackBot extends Globals
{
	public static void loop()throws GameActionException
	{
		while (true)
		{
			header();
			boolean weakEnemyNearby = false;
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
					RobotType enemyType = robot.getType();
					if (!(enemyType == RobotType.SOLDIER || enemyType == RobotType.TANK))
					{
						weakEnemyNearby = true;
						movingDirection = here.directionTo(robot.getLocation());
						break;
					}
				}
			}
			if (rc.canStrike() && alliesInRangeOfStrike * 2 < enemiesInRangeOfStrike * 3)
			{
				rc.strike();
				if (weakEnemyNearby)
				{
					tryToMove(movingDirection);
				}
				trytoChop();
				footer();
				continue;
			}
			if (weakEnemyNearby)
			{
				tryToMove(movingDirection);
			}
			if (trytoChop() || !tryToMove(movingDirection))
			{
				movingDirection = randomDirection();
			}
			footer();
		}
	}

	private static boolean trytoChop()throws GameActionException 
	{
		if (lumberjackTarget != -1 && rc.canInteractWithTree(lumberjackTarget))
		{
			if (rc.canChop(lumberjackTarget))
			{
				rc.chop(lumberjackTarget);
				return true;
			}
			tryToMoveTowards(lumberjackTargetLocation);
		}
		if (enemyTrees.length != 0)
		{
			if (enemyTrees.length != 0 && rc.canChop(enemyTrees[0].getID()))
			{
				rc.chop(enemyTrees[0].getID());
				return true;
			}
			tryToMoveTowards(enemyTrees[0].getLocation());
		}
		if (neutralTrees.length != 0)
		{
			if (neutralTrees.length != 0 && rc.canChop(neutralTrees[0].getID()))
			{
				rc.chop(neutralTrees[0].getID());
				return true;
			}
			tryToMoveTowards(neutralTrees[0].getLocation());
		}
		return false;
	}
}
