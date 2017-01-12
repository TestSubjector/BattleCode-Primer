package potatobot;
import battlecode.common.*;

public class GardenerBot extends Globals
{
	public static void loop()throws GameActionException
	{
		while (true)
		{
			roundNum = rc.getRoundNum();
			updateRobotCount();
			int scouts = robotCount[RobotType.SCOUT.ordinal()];
			if (scouts * 50 < roundNum)
			{
				spawn(RobotType.SCOUT);
			}
			else
			{
				wander();
			}
			Clock.yield();
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
				rc.broadcast(RobotType.SCOUT.ordinal(), robotCount[RobotType.SCOUT.ordinal()] + 1);
				return;
			}
			tries++;
		}
	}
}
