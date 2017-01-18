package potatobot;
import battlecode.common.*;

public class TankBot extends Globals
{
	public static void loop()throws GameActionException
	{
		Direction random = randomDirection();
		while (true)
		{
			header();
			if (haveTarget)
			{
				int targetArchonLocationIndex = rc.readBroadcast(ENEMY_ARCHON_CHANNELS[7]) * 2;
				int hashedTargetLocation = rc.readBroadcast(ENEMY_ARCHON_CHANNELS[targetArchonLocationIndex]);
				MapLocation targetLocation = unHashIt(hashedTargetLocation);
				System.out.println(targetLocation);
				tryToMoveTowards(targetLocation);
			}
			else if (!tryToMove(random))
			{
				random = randomDirection();
			}
			for (RobotInfo enemy : enemies)
			{
				trySingleShot(enemy);
			}
			footer();
		}
	}
}
