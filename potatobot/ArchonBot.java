package potatobot;
import battlecode.common.*;

public class ArchonBot extends Globals
{
	public static void loop()throws GameActionException
	{
		while (true)
		{
			updateRobotCount();
			if (dying())
			{
				imDying();
			}
			int gardeners = robotCount[RobotType.GARDENER.ordinal()];
			if (gardeners < robotCountMax[RobotType.GARDENER.ordinal()])
			{
				tryHiringGardener(gardeners);
			}
			wander();
			prevHealth = rc.getHealth();
			Clock.yield();
		}
	}
	
	public static void tryHiringGardener(int gardeners)throws GameActionException
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
}
