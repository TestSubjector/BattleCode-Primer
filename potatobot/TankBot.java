package potatobot;
import battlecode.common.*;

public class TankBot extends Globals
{
	public static int patience = 30;
	public static void loop()throws GameActionException
	{
		if (ourInitialArchons[0].distanceTo(theirInitialArchons[0]) > 35f)
		{
			movingDirection = here.directionTo(theirInitialArchons[0]);
		}
		while (true)
		{
			header();

			findMoveDirection();
			
			// movingDirection decided, now tryToMove
			if (!tryToMove(movingDirection))
			{
				patience--;
				if (patience <= -30)
				{
					movingDirection = movingDirection.opposite();
					patience = 10;
				}
				if (patience <= 0)
				{
					if (neutralTrees.length != 0)
					{
						MapLocation closestTreeLocation = neutralTrees[0].getLocation();
						Direction shotDirection = here.directionTo(closestTreeLocation);
						if (rc.canFireSingleShot())
						{
							boolean killingFriend = false;
							int loopLength = allies.length;
							for(int i = 0; i<loopLength;i++)
							{
								RobotInfo ally = allies[i];
								if (willHitRobot(ally, shotDirection, here) && ally.getLocation().distanceTo(here) < closestTreeLocation.distanceTo(here))
								{
									killingFriend = true;
									break;
								}
							}
							if (!killingFriend)
							{
								if (rc.canFireSingleShot())
								{
									rc.fireSingleShot(shotDirection);
								}
							}
						}
					}
				}
			}
			else
			{
				patience = 30;
			}
			shootClosestEnemy();
			
			footer();
		}
	}
	
	private static void findMoveDirection()throws GameActionException
	{
		// Defend your closest farmer
		if (soldiers <= farmers)
		{
			int numberOfAllyFarmLocations = rc.readBroadcast(FARM_LOCATIONS_CHANNELS[0]);
			float closestFarmDistance = 500000;
			MapLocation closestFarmLocation = null;
			if (numberOfAllyFarmLocations > 0)
			{
				for (int i = 1; i <= numberOfAllyFarmLocations; i++)
				{
					int hashedLocation = rc.readBroadcast(FARM_LOCATIONS_CHANNELS[i]);
					if (hashedLocation == -1)
					{
						continue;
					}
					else
					{
						MapLocation unhashedLocation = unhashIt(hashedLocation);
						float farmDistance = here.distanceTo(unhashedLocation);
						if (farmDistance < closestFarmDistance)
						{
							closestFarmDistance = farmDistance;
							closestFarmLocation = unhashedLocation;
						}
					}
				}
			}
			if (closestFarmLocation != null)
			{
				Direction toClosestFarm = here.directionTo(closestFarmLocation);
				if (closestFarmDistance > 5f)
				{
					movingDirection = toClosestFarm;
				}
				else
				{
					movingDirection = toClosestFarm.opposite();
				}
			}
		}
		else if (enemies.length != 0)
		{
			MapLocation enemyLocation = enemies[0].getLocation();
			movingDirection = here.directionTo(enemyLocation);
		}
		else if (enemyTarget != -1)
		{
			if (rc.canSenseLocation(enemyTargetLocation) && !rc.canSenseRobot(enemyTarget))
			{
				enemyTarget = -1;
				movingDirection = randomDirection();
			}
			else
			{
				movingDirection = here.directionTo(enemyTargetLocation);
			}
		}
		else
		{
			movingDirection = randomDirection();
		}
	}
	
	public static boolean shootClosestEnemy()throws GameActionException
	{
		if (enemies.length == 0)
		{
			return false;
		}
		if (tryPentadShot(enemies[0]))
		{
			return true;
		}
		if (tryTriadShot(enemies[0]))
		{
			return true;
		}
		int loopLength = enemies.length;
		for(int i = 0; i<loopLength;i++)
		{
			RobotInfo enemy = enemies[i];
			if (trySingleShot(enemy))
			{
				return true;
			}
		}
		return false;
	}
}