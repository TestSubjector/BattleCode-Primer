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
			
			// Look for broadcasted archons
			int enemyArchons = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[0]);
			MapLocation closestArchonLocation = null;
			float closestArchonDistance = 500000;
			if (enemyArchons > 0)
			{
				for (int i = 2; i <= enemyArchons * 2; i += 2)
				{
					int hashedLocation = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[i]);
					if (hashedLocation == -1)
					{
						continue;
					}
					else
					{
						MapLocation unhashedLocation = unhashIt(hashedLocation);
						float enemyDistance = here.distanceTo(unhashedLocation);
						if (enemyDistance < closestArchonDistance)
						{
							closestArchonDistance = enemyDistance;
							closestArchonLocation = unhashedLocation;
						}
					}
				}
			}
			if (closestArchonLocation != null)
			{
				movingDirection = here.directionTo(closestArchonLocation);
			}
			
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
				else
				{
					movingDirection = here.directionTo(enemyLocation);
					break;
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
			
			// movingDirection decided, now tryToMove
			if (!tryToMove(movingDirection))
			{
				movingDirection = randomDirection();
			}
			shootClosestEnemy();
			
			footer();
		}
	}
	
	private static void shootClosestEnemy()throws GameActionException 
	{
		for (RobotInfo enemy : enemies)
		{
			if (here.distanceTo(enemy.getLocation()) <= 5 || (enemies.length > 4 && enemies.length * 3 > allies.length * 2))
			{
				if (tryPentadShot(enemy))
				{
					break;
				}
			}
			else if (here.distanceTo(enemy.getLocation()) <= 7 || (enemies.length > 3 && enemies.length * 3 > allies.length * 2))
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
	}
}
