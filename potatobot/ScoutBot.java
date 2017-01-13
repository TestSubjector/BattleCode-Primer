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
				if (Clock.getBytecodesLeft() > (2 * myType.bytecodeLimit / 3) && rc.canFireSingleShot())
				{
					rc.fireSingleShot(here.directionTo(enemy.getLocation()));
				}
			}
			wander();
			footer();
		}
	}
}
