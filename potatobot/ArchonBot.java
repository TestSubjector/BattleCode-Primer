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
			if ((gardeners < 3 || treesPlanted > 6) && gardeners < robotCountMax[RobotType.GARDENER.ordinal()])
			{
				tryHiringGardener(gardeners);
			}
			updateBulletCount();
			if (bullets > 500)
			{
				int donation = (int)(Math.floor((bullets / ourInitialArchons.length) / 100) * 10);
				if (donation > 0)
				{
					rc.donate(donation);
				}
			}
			wander();
			footer();
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
