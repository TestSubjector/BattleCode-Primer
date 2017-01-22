package potatobot;
import battlecode.common.*;

public class ArchonBot extends Globals
{
	public static void loop()throws GameActionException
	{
		while (true)
		{
			header();
			int gardeners = robotCount[RobotType.GARDENER.ordinal()];
			int scouts = robotCount[RobotType.SCOUT.ordinal()];
			int lumberjacks = robotCount[RobotType.LUMBERJACK.ordinal()];
			if (gardeners < robotCountMax[RobotType.GARDENER.ordinal()] && (gardeners < 4 || gardeners < (scouts + lumberjacks) / 2))
			{
				tryHiringGardener(gardeners);
			}
			Direction awayFromOtherArchon = null;
			for (RobotInfo ally : allies)
			{
				if (ally.getType() == RobotType.ARCHON);
				{
					awayFromOtherArchon = ally.getLocation().directionTo(here);
					break;
				}
			}
			if (awayFromOtherArchon != null)
			{
				movingDirection = awayFromOtherArchon;
			}
			int tries = 0;
			while (!tryToMove(movingDirection) && tries < 100)
			{
				double choice = Math.random();
				float angle = ((float)Math.random() * 45f + 135f);
				if (choice > 0.5)
				{
					movingDirection = movingDirection.rotateLeftDegrees(angle);
				}
				else
				{
					movingDirection = movingDirection.rotateRightDegrees(angle);
				}
				tries++;
			}
			footer();
		}
	}
	
	public static void tryHiringGardener(int gardeners)throws GameActionException
	{
		int tries = 0;
		Direction hireDirection = randomDirection();
		while (tries < 45)
		{
			if (rc.canHireGardener(hireDirection))
			{
				rc.hireGardener(hireDirection);
				break;
			}
			else
			{
				hireDirection.rotateLeftDegrees(8);
			}
			tries++;
		}
	}
}
