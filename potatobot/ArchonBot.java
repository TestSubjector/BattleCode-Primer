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
			while (!tryToMove(movingDirection) && Clock.getBytecodesLeft() > 1000)
			{
				Direction newDirection = randomDirection();
				while (Math.abs(newDirection.degreesBetween(movingDirection)) > 100)
				{
					newDirection = randomDirection();
				}
				movingDirection = newDirection;
			}
			footer();
		}
	}
	
	public static void tryHiringGardener(int gardeners)throws GameActionException
	{
		int tries = 0;
		while (tries < 15)
		{
			Direction randomDir = randomDirection();
			if (rc.canHireGardener(randomDir))
			{
				rc.hireGardener(randomDir);
				break;
			}
			tries++;
		}
	}
}
