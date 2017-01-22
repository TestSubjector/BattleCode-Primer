package potatobot;
import battlecode.common.*;

public class SoldierBot extends Globals
{
	public static void loop()throws GameActionException
	{
		while (true)
		{
			header();
			for (RobotInfo enemy : enemies)
			{
				if (here.distanceTo(enemy.getLocation()) <= 3 || enemies.length > 4)
				{
					if (tryPentadShot(enemy))
					{
						break;
					}
				}
				else if (here.distanceTo(enemy.getLocation()) <= 5)
				{
					if (tryTriadShot(enemy))
					{
						break;
					}
				}
				else
				{
					if (trySingleShot(enemy))
					{
						break;
					}
				}
			}
			wander();
			footer();
		}
	}
}