package potatobot;
import battlecode.common.*;

public class LumberjackBot extends Globals
{
	public static void loop()throws GameActionException
	{
		movingDirection = randomDirection();
		while (true)
		{
			try
			{
				header();
	
				findMoveDirection();
				
				if (!(tryToStrike() || tryToChop()))
				{
					if (!tryToMove(movingDirection))
					{
						// Replace with pathing later
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
	
	private static void findMoveDirection()throws GameActionException
	{
		if (neutralTrees.length != 0)
		{
			TreeInfo closestNeutralTree = neutralTrees[0];
			movingDirection = here.directionTo(closestNeutralTree.getLocation());
		}
		if (enemyTrees.length != 0)
		{
			TreeInfo closestEnemyTree = enemyTrees[0];
			movingDirection = here.directionTo(closestEnemyTree.getLocation());
		}
		if (enemies.length != 0)
		{
			RobotType enemyType = enemies[0].getType();
			MapLocation enemyLocation = enemies[0].getLocation();
			movingDirection = here.directionTo(enemyLocation);
			if (enemyType == RobotType.SOLDIER || enemyType == RobotType.TANK)
			{
				movingDirection = movingDirection.opposite();
			}
		}
		if (importantTreeTarget != -1)
		{
			movingDirection = here.directionTo(importantTreeTargetLocation);
		}
	}
	
	private static boolean tryToStrike()throws GameActionException 
	{
		RobotInfo[] allRobots = rc.senseNearbyRobots(2.1f);
		int alliesInRangeOfStrike = 0;
		int enemiesInRangeOfStrike = 0;
		
		int loopLength = allRobots.length;
		for (int i = 0; i < loopLength; i++)
		{
			RobotInfo robot = allRobots[i];
			if (robot.getTeam() == us && robot.getType() != RobotType.SCOUT)
			{
				// We don't care about our scouts
				// Devs y u nerf
				alliesInRangeOfStrike++;
			}
			else
			{
				enemiesInRangeOfStrike++;
			}
		}
		if (rc.canStrike() && alliesInRangeOfStrike * 2 < enemiesInRangeOfStrike * 3)
		{
			rc.strike();
			return true;
		}
		return false;
		
	}

	private static boolean tryToChop()throws GameActionException 
	{
		if (importantTreeTarget != -1)
		{
			
			if (rc.canInteractWithTree(importantTreeTarget))
			{
				RobotType botInside = rc.senseTree(importantTreeTarget).getContainedRobot();
				rc.chop(importantTreeTarget);
				if (!rc.canInteractWithTree(importantTreeTarget))
				{
					int botsAlready = robotCount[botInside.ordinal()];
					rc.broadcast(botInside.ordinal(), botsAlready + 1);
				}
				return true;
			}
			else if (rc.canSenseLocation(importantTreeTargetLocation))
			{
				importantTreeTarget = -1;
			}
		}
		if (enemyTrees.length != 0)
		{
			if (enemyTrees.length != 0 && rc.canChop(enemyTrees[0].getID()))
			{
				rc.chop(enemyTrees[0].getID());
				return true;
			}
		}
		if (neutralTrees.length != 0)
		{
			if (neutralTrees.length != 0 && rc.canChop(neutralTrees[0].getID()))
			{
				rc.chop(neutralTrees[0].getID());
				return true;
			}
		}
		return false;
	}
}
