package potatobot;
import battlecode.common.*;

public class SoldierBot extends Globals
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
				trySingleShot(enemy);
			}
			footer();
		}
	}
}
