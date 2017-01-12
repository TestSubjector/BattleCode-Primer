package potatobot;
import battlecode.common.*;

public class ArchonBot extends Globals
{
	public static void loop()throws GameActionException
	{
		while (true)
		{
			roundNum = rc.getRoundNum();
			updateRobotCount();
			int gardeners = robotCount[RobotType.GARDENER.ordinal()];
			if (gardeners * 70 < roundNum)
			{
				int tries = 0;
				while (tries < 5)
				{
					Direction randomDir = randomDirection();
					if (rc.canHireGardener(randomDir))
					{
						rc.hireGardener(randomDir);
						rc.broadcast(RobotType.GARDENER.ordinal(), gardeners + 1);
						break;
					}
					tries++;
				}
			}
			else
			{
				wander();
			}
			Clock.yield();
		}
	}
}
