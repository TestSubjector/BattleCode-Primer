package potatobot;
import battlecode.common.*;

public class ScoutBot extends Globals
{
	public static void loop()throws GameActionException
	{
		movingDirection = here.directionTo(theirInitialArchons[0]);
		while (true)
		{
			header();
			boolean gonnaGetBullets = false;
			for (TreeInfo tree : neutralTrees)
			{
				if (tree.getContainedBullets() > 0)
				{
					movingDirection = here.directionTo(tree.getLocation());
					gonnaGetBullets = true;
					break;
				}
			}
			for (RobotInfo enemy : enemies)
			{
				if (trySingleShot(enemy))
				{
					break;
				}
			}
			if (gonnaGetBullets)
			{
				tryToMove(movingDirection);
			}
			else if (haveTarget)
			{
				int targetArchonLocationIndex = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[7]) * 2;
				int hashedTargetLocation = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[targetArchonLocationIndex]);
				MapLocation targetLocation = unhashIt(hashedTargetLocation);
				tryToMoveTowards(targetLocation);
			}
			else if (!tryToMove(movingDirection))
			{
				movingDirection = randomDirection();
			}
			else 
			{
				wander();
			}
			footer();
		}
	}
}
