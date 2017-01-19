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
			if (enemyTrees.length != 0)
			{
				tryToMoveTowards(enemyTrees[0].getLocation());
				if (rc.canChop(enemyTrees[0].getID()))
				{
					rc.chop(enemyTrees[0].getID());
				}
				else if (neutralTrees.length != 0)
				{
					if (rc.canChop(neutralTrees[0].getID()))
					{
						rc.chop(neutralTrees[0].getID());
					}
				}
			}
			else if (neutralTrees.length != 0)
			{
				tryToMoveTowards(neutralTrees[0].getLocation());
				if (rc.canChop(neutralTrees[0].getID()))
				{
					rc.chop(neutralTrees[0].getID());
				}
			}
			else if (!tryToMove(movingDirection))
			{
				movingDirection = randomDirection();
			}
			footer();
		}
	}
}
