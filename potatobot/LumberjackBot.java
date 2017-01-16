package potatobot;
import battlecode.common.*;

public class LumberjackBot extends Globals
{
	public static void loop()throws GameActionException
	{
		while (true)
		{
			header();
			for (RobotInfo enemy : enemies)
			{
				if (enemy.getLocation().distanceTo(here) - enemy.getRadius() < 2)
				{
					if (rc.canStrike())
					{
						rc.strike();
						break;
					}
				}
			}
			
			if (trees.length != 0)
			{
				tryToMoveTowards(trees[0].getLocation());
				if (rc.canChop(trees[0].getID()))
				{
					rc.chop(trees[0].getID());
				}
			}
			else
			{
				wander();
			}
			footer();
		}
	}
}
