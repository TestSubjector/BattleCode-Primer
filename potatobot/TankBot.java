package potatobot;
import battlecode.common.*;

public class TankBot extends Globals
{
	public static void loop()throws GameActionException
	{
		movingDirection = here.directionTo(theirInitialArchons[0]);
		while (true)
		{
			header();
			if (haveTarget)
			{
				int targetArchonLocationIndex = rc.readBroadcast(ENEMY_ARCHON_CHANNELS[7]) * 2;
				int hashedTargetLocation = rc.readBroadcast(ENEMY_ARCHON_CHANNELS[targetArchonLocationIndex]);
				MapLocation targetLocation = unhashIt(hashedTargetLocation);
				tryToMoveTowards(targetLocation);
			}
			else if (!tryToMove(movingDirection))
			{
				movingDirection = randomDirection();
			}
			for (RobotInfo enemy : enemies)
			{
				if (here.distanceTo(enemy.getLocation()) <= 4 || enemies.length > 4)
				{
					if (tryPentadShot(enemy))
					{
						break;
					}
				}
				else if (here.distanceTo(enemy.getLocation()) <= 6)
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
			footer();
		}
	}
}
