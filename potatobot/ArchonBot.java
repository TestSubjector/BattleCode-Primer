package potatobot;
import battlecode.common.*;

public class ArchonBot extends Globals
{
	public static void loop()throws GameActionException
	{
		while (true)
		{
			header();
			int gardeners = robotCount[RobotType.GARDENER.ordinal()];
			int scouts = robotCount[RobotType.SCOUT.ordinal()];
			int lumberjacks = robotCount[RobotType.LUMBERJACK.ordinal()];
			if (gardeners < robotCountMax[RobotType.GARDENER.ordinal()] && (gardeners < 4 || gardeners < (scouts + lumberjacks) / 2))
			{
				tryHiringGardener(gardeners);
			}
			TreeInfo[] allyTrees = rc.senseNearbyTrees(-1, us);
			BodyInfo[][] array = {enemies, allies, neutralTrees, enemyTrees, allyTrees};
			Direction awayFromNearestObstacle = findDirectionAwayFromNearestObstacle(array);		
			if (awayFromNearestObstacle != null)
			{
				movingDirection = awayFromNearestObstacle;
			}
			if (!tryToMove(movingDirection))
			{
				float angle = ((float)Math.random() * 45f + 80f);
				double choice = Math.random();
				if (choice > 0.5)
				{
					movingDirection = movingDirection.rotateLeftDegrees(angle);
				}
				else
				{
					movingDirection = movingDirection.rotateRightDegrees(angle);
				}
			}
			footer();
		}
	}
	
	public static Direction findDirectionAwayFromNearestObstacle(BodyInfo array[][])throws GameActionException 
	{
		float minDist = 100000f;
		Direction awayFromNearestObstacle = null;
		for (int i = 0; i < array.length; i++)
		{
			int arrayLength = array[i].length;
			if (arrayLength != 0)
			{
				if (array[i][0].isRobot())
				{
					array[i][0] = (RobotInfo)array[i][0];
				}
				else
				{
					array[i][0] = (TreeInfo)array[i][0];
				}
				MapLocation bodyLocation = array[i][0].getLocation();
				float bodyDistance = bodyLocation.distanceTo(here) - array[i][0].getRadius();
				if (bodyDistance < minDist)
				{
					awayFromNearestObstacle = bodyLocation.directionTo(here);
					minDist = bodyDistance;
				}
			}
		}
		minDist = Math.min(minDist, myType.sensorRadius - 0.1f);
		float angle = 0;
		Direction initialDirection = Direction.getEast();
		while (angle < 360)
		{
			Direction sensorDirection = initialDirection.rotateLeftDegrees(angle);
			while (!rc.onTheMap(here.add(sensorDirection, minDist)))
			{
				minDist = Math.max(minDist - 0.5f, 2.0f);
				if (minDist <= 2.01f)
				{
					return sensorDirection.opposite();
				}
				awayFromNearestObstacle = sensorDirection.opposite();
			}
			angle += 4;
		}
		return awayFromNearestObstacle;
	}

	public static void tryHiringGardener(int gardeners)throws GameActionException
	{
		int tries = 0;
		Direction hireDirection = randomDirection();
		while (tries < 45)
		{
			if (rc.canHireGardener(hireDirection))
			{
				rc.hireGardener(hireDirection);
				break;
			}
			else
			{
				hireDirection.rotateLeftDegrees(8);
			}
			tries++;
		}
	}
}
