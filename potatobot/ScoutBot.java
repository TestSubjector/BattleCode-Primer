package potatobot;
import battlecode.common.*;

public class ScoutBot extends Globals
{
	//private static boolean shotLastTurn;
	public static void loop()throws GameActionException
	{
		if (ourInitialArchons[0].distanceTo(theirInitialArchons[0]) > 35f)
		{
			movingDirection = randomDirection();
		}
		else
		{
			movingDirection = here.directionTo(theirInitialArchons[0]);
		}
		while (true)
		{
			header();

			// Check all nearby enemies
			for (RobotInfo enemy : enemies)
			{
				RobotType enemyType = enemy.getType();
				MapLocation enemyLocation = enemy.getLocation();
				if (enemyType == RobotType.LUMBERJACK && here.distanceTo(enemyLocation) - myType.bodyRadius < 3.5f)
				{
					movingDirection = enemyLocation.directionTo(here);
					break;
				}
				else if (!(enemyType == RobotType.SOLDIER || enemyType == RobotType.TANK || enemyType == RobotType.ARCHON))
				{
					movingDirection = here.directionTo(enemyLocation);
					break;
				}
				else
				{
					// think of something for case where enemyType is SOLDIER, TANK OR ARCHON.
				}
			}
			
			// Look for broadcasted gardeners
			int enemyGardeners = rc.readBroadcast(ENEMY_GARDENERS_CHANNELS[0]);
			MapLocation closestGardenerLocation = null;
			float closestGardenerDistance = 500000;
			if (enemyGardeners > 0)
			{
				for (int i = 2; i <= enemyGardeners * 2; i += 2)
				{
					int hashedLocation = rc.readBroadcast(ENEMY_GARDENERS_CHANNELS[i]);
					if (hashedLocation == -1)
					{
						continue;
					}
					else
					{
						MapLocation unhashedLocation = unhashIt(hashedLocation);
						float enemyDistance = here.distanceTo(unhashedLocation);
						if (enemyDistance < closestGardenerDistance)
						{
							closestGardenerDistance = enemyDistance;
							closestGardenerLocation = unhashedLocation;
						}
					}
				}
			}
			if (closestGardenerLocation != null)
			{
				movingDirection = here.directionTo(closestGardenerLocation);
			}
			for (TreeInfo tree : neutralTrees)
			{
				if (tree.getContainedBullets() > 0)
				{
					movingDirection = here.directionTo(tree.getLocation());
					break;
				}
			}
			
			// movingDirection decided, now tryToMove
			if (!tryToMove(movingDirection))
			{
				float angle = ((float)Math.random() * 90f);
				double choice = Math.random();
				if (choice > 0.5)
				{
					movingDirection = movingDirection.opposite().rotateLeftDegrees(angle);
				}
				else
				{
					movingDirection = movingDirection.opposite().rotateRightDegrees(angle);
				}
			}
			shootClosestEnemy();
			
			footer();
		}
	}

	private static void shootClosestEnemy()throws GameActionException 
	{

		// look for the closest enemy and shoot
		//shotLastTurn = false;
		for (RobotInfo enemy : enemies)
		{
			if ((roundNum > 500 || enemy.getType() != RobotType.ARCHON)  && trySingleShot(enemy))
			{
				//shotLastTurn = true;
				break;
			}
		}
	}
	
	/* Deprecated function, would have needed this if the scouts travelled faster than their bullets like they used to
	public static boolean tryToMove(Direction movingDirection)throws GameActionException
	{
		if (shotLastTurn)
		{
			return tryToMoveThisMuch(movingDirection, (myType.strideRadius * 2 / 3));
		}
		return Globals.tryToMove(movingDirection);
	} */
}
