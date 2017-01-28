package potatobot;
import battlecode.common.*;

public class ArchonBot extends Globals
{
	public static void loop()throws GameActionException
	{
		while (true)
		{
			try
			{
				header();
				/* Count of each type of robot, for debugging purposes
				for (int i = 0; i <= 7; i++)
				{
					System.out.print(robotCount[i] + " ");
				}
				System.out.println();
				*/

				BodyInfo[][] array = {enemies, allies, neutralTrees, enemyTrees, allyTrees};
				Direction awayFromNearestObstacle = findDirectionAwayFromNearestObstacle(array);		
				
				// Use TreeDensity after Akhil is done with the maths
				if (rc.getBuildCooldownTurns() <= 0 && bullets > 151 && (soldiers >= 1 || gardeners < 1))
				{
					if (tryHiringGardener())
					{
						robotInit(RobotType.GARDENER);
					}
				}
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
			catch (GameActionException e)
			{
				System.out.println("Catch kiya");
				footer();
			}
		}
	}
	
	public static Direction findDirectionAwayFromNearestObstacle(BodyInfo array[][])throws GameActionException 
	{
		float minDist = 100000f;
		float minDist2 = 100000f;
		Direction awayFromNearestObstacle = null;
		Direction awayFromNearestObstacle2 = null;
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
				if (bodyDistance <= minDist)
				{
					awayFromNearestObstacle = bodyLocation.directionTo(here).opposite();
					minDist = bodyDistance;
				}
			}
			if (arrayLength > 1)
			{
				if (array[i][1].isRobot())
				{
					array[i][1] = (RobotInfo)array[i][0];
				}
				else
				{
					array[i][1] = (TreeInfo)array[i][0];
				}
				MapLocation bodyLocation2 = array[i][1].getLocation();
				float bodyDistance2 = bodyLocation2.distanceTo(here) - array[i][1].getRadius();
				if (bodyDistance2 <= minDist2)
				{
					awayFromNearestObstacle2 = bodyLocation2.directionTo(here).opposite();
					minDist2 = bodyDistance2;
				}
			}
		}
		minDist = Math.min(minDist, myType.sensorRadius - 0.1f);
		minDist2 = Math.min(minDist, myType.sensorRadius - 0.1f);
		float angle = 0;
		Direction initialDirection = Direction.getEast();
		while (angle < 360)
		{
			Direction sensorDirection = initialDirection.rotateLeftDegrees(angle);
			while (rc.canSenseLocation(here.add(sensorDirection, minDist)) && !rc.onTheMap(here.add(sensorDirection, minDist)))
			{
				minDist = Math.max(minDist - 0.5f, 2.0f);
				if (minDist <= 2.01f)
				{
					return sensorDirection.opposite();
				}
				awayFromNearestObstacle = sensorDirection.opposite();
			}
			while (rc.canSenseLocation(here.add(sensorDirection, minDist2)) && !rc.onTheMap(here.add(sensorDirection, minDist2)))
			{
				minDist2 = Math.max(minDist2 - 0.5f, 2.0f);
				if (minDist2 <= 2.01f)
				{
					return sensorDirection.opposite();
				}
				awayFromNearestObstacle2 = sensorDirection.opposite();
			}
			angle += 4;
		}
		if (awayFromNearestObstacle == null)
		{
			return randomDirection();
		}
		if (awayFromNearestObstacle2 == null)
		{
			return awayFromNearestObstacle;
		}
		Direction away = new Direction(((float)Math.PI / 180f) * (awayFromNearestObstacle.getAngleDegrees() + awayFromNearestObstacle2.getAngleDegrees()) / 2).opposite();
		return away;
	}

	public static boolean tryHiringGardener()throws GameActionException
	{
		int tries = 0;
		Direction hireDirection = randomDirection();
		while (tries < 45)
		{
			if (rc.canHireGardener(hireDirection))
			{
				rc.hireGardener(hireDirection);
				return true;
			}
			else
			{
				hireDirection.rotateLeftDegrees(8);
			}
			tries++;
		}
		return false;
	}
}
