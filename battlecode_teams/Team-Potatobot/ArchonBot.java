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
				
				BodyInfo[][] array = {enemies, allies, neutralTrees, enemyTrees, allyTrees};
				awayFromNearestObstacle = findDirectionAwayFromNearestObstacle(array);
				if (rc.getBuildCooldownTurns() <= 0)
				{
					if (gardeners < gameProgressPercentage * 30 && tryHiringGardener())
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
