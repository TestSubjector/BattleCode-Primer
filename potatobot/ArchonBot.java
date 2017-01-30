package potatobot;
import battlecode.common.*;

public class ArchonBot extends Globals
{
	public static Direction awayFromNearestObstacle; 
	public static void loop()throws GameActionException
	{
		while (true)
		{
			try
			{
				header();
				/*
				for (int i = 0; i <= 7; i++)
				{
					System.out.print(robotCount[i] + " ");
				}
				System.out.println();
				*/
				BodyInfo[][] array = {enemies, allies, neutralTrees, enemyTrees, allyTrees};
				awayFromNearestObstacle = findDirectionAwayFromNearestObstacle(array);		
				// rc.setIndicatorLine(here, here.add(awayFromNearestObstacle), 255, 255, 255);
				// Use TreeDensity after Akhil is done with the maths
				if (rc.getBuildCooldownTurns() <= 0)
				{
					System.out.println("1");
					if ((gardeners < 1  || neutralTrees.length < 15) && gardeners <= gameProgressPercentage * 30 && tryHiringGardener())
					{
						System.out.println("2");
						robotInit(RobotType.GARDENER);
					}
				}
				if (awayFromNearestObstacle != null)
				{
					movingDirection = awayFromNearestObstacle;
				}
				if (!tryToMove(movingDirection))
				{
					movingDirection = randomDirection();
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
	
	public static boolean tryHiringGardener()throws GameActionException
	{
		int tries = 0;
		Direction hireDirection = awayFromNearestObstacle;
		while (tries < 90)
		{
			if (rc.canHireGardener(hireDirection))
			{
				rc.hireGardener(hireDirection);
				return true;
			}
			else
			{
				hireDirection = hireDirection.rotateLeftDegrees(4f);
			}
			tries++;
		}
		return false;
	}
}
