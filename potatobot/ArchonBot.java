package potatobot;
import battlecode.common.*;

public class ArchonBot extends Globals
{
	private static int gardenerTime = 20;
	public static void loop()throws GameActionException
	{
		Direction movingDirection = randomDirection();
		while (true)
		{
			header();
			updateBulletCount();
			if (bullets >= 10000)
			{
				rc.donate(10000);
			}
			int gardeners = robotCount[RobotType.GARDENER.ordinal()];
			if (gardeners < robotCountMax[RobotType.GARDENER.ordinal()] && gardenerTime >= 15)
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
			gardenerTime++;
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
				gardenerTime = 0;
				break;
			}
			tries++;
		}
	}
}
