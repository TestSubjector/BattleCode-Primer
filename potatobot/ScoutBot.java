package potatobot;
import battlecode.common.*;

public class ScoutBot extends Globals
{
	public static void loop()throws GameActionException
	{
		while (true)
		{
			header();
			wander();
			footer();
		}
	}
}
