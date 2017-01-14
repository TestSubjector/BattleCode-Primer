package potatobot;
import battlecode.common.*;

public class ScoutBot extends Globals
{
	public static void loop()throws GameActionException
	{
		while (true)
		{
			header();
			RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
			for (RobotInfo enemy : enemies)
			{
				trySingleShot(enemy);
			}
			wander();
			footer();
		}
	}
}
