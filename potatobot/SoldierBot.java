package potatobot;
import battlecode.common.*;

public class SoldierBot extends Globals
{
	public static void loop()throws GameActionException
	{
		movingDirection = here.directionTo(theirInitialArchons[0]);
		while (true)
		{
			try
			{
				header();
	
				findMoveDirection();
				
				// movingDirection decided, now tryToMove
				if (!tryToMove(movingDirection))
				{
					movingDirection = randomDirection();
				}
				
				tryShot();
				
				footer();
			}
			catch (GameActionException e)
			{
				System.out.println("Catch kiya");
				footer();
			}
		}
	}
	
	private static void findMoveDirection()throws GameActionException
	{
		if (enemies.length != 0)
		{
			MapLocation enemyLocation = enemies[0].getLocation();
			movingDirection = here.directionTo(enemyLocation);
		}
		else if (enemyTarget != 0)
		{
			rc.setIndicatorLine(here, enemyTargetLocation, 255, 0, 0);
			movingDirection = here.directionTo(enemyTargetLocation);
		}
		else
		{
			findClosestFarm();
			findClosestEnemyArchon();
			float distanceBetweenFarmAndArchon = closestFarmLocation.distanceTo(closestArchonLocation);
			Direction directionFromFarmToArchon = closestFarmLocation.directionTo(closestArchonLocation);
			MapLocation targetLocation = closestFarmLocation.add(directionFromFarmToArchon, 0.4f * distanceBetweenFarmAndArchon);
			movingDirection = here.directionTo(targetLocation);
		}
	}
	
	private static void findClosestFarm()throws GameActionException
	{
		float closestFarmDistance = 500000f;
		closestFarmLocation = here;
		int allyFarmLocations = rc.readBroadcast(FARM_LOCATIONS_CHANNELS[0]);
		for (int i = 1; i <= allyFarmLocations; i++)
		{
			int hashedLocation = rc.readBroadcast(FARM_LOCATIONS_CHANNELS[i]);
			MapLocation unhashedLocation = unhashIt(hashedLocation);
			float farmDistance = here.distanceTo(unhashedLocation);
			if (farmDistance < closestFarmDistance)
			{
				closestFarmDistance = farmDistance;
				closestFarmLocation = unhashedLocation;
			}
		}
		allyFarmLocations = rc.readBroadcast(SPAWN_LOCATIONS_CHANNELS[0]);
		for (int i = 1; i <= allyFarmLocations; i++)
		{
			int hashedLocation = rc.readBroadcast(SPAWN_LOCATIONS_CHANNELS[i]);
			MapLocation unhashedLocation = unhashIt(hashedLocation);
			float farmDistance = here.distanceTo(unhashedLocation);
			if (farmDistance < closestFarmDistance)
			{
				closestFarmDistance = farmDistance;
				closestFarmLocation = unhashedLocation;
			}
		}
	}
	
	private static void findClosestEnemyArchon()throws GameActionException
	{
		float minDist = 500000f;
		int loopLength = 9;
		closestArchonLocation = theirInitialArchons[0];
		for (int i = 1; i < loopLength; i += 3)
		{
			int hashedLocation = rc.readBroadcast(ENEMY_ARCHONS_CHANNELS[i + 1]);
			if (hashedLocation != 0)
			{
				MapLocation unhashedLocation = unhashIt(hashedLocation);
				float distanceToArchon = here.distanceTo(unhashedLocation);
				if (distanceToArchon < minDist)
				{
					minDist = distanceToArchon;
					closestArchonLocation = unhashedLocation;
				}
			}
		}
	}
}