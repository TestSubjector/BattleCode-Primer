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
					doPatienceThings();
					movingDirection = randomDirection();
				}
				else if (movedBack)
				{
					doPatienceThings();
				}
				else
				{
					patience = 30;
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
			if (rc.canSenseLocation(enemyTargetLocation) && !rc.canSenseRobot(enemyTarget))
			{
				enemyTarget = 0;
				movingDirection = randomDirection();
			}
			else
			{
				// rc.setIndicatorLine(here, enemyTargetLocation, 255, 0, 0);
				movingDirection = here.directionTo(enemyTargetLocation);
			}
		}
	}
	
	public static void doPatienceThings()throws GameActionException
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
				TreeInfo tree = neutralTrees[0];
				MapLocation closestTreeLocation = tree.getLocation();
				Direction shotDirection = here.directionTo(closestTreeLocation);
				float treeDistance = tree.getLocation().distanceTo(here);
				if (rc.canFireSingleShot())
				{
					boolean killingFriend = false;
					int loopLength = allies.length;
					for(int i = 0; i < loopLength; i++)
					{
						RobotInfo ally = allies[i];
						if (ally.getLocation().distanceTo(here) < treeDistance)
						{
							if (willHitBody(ally, shotDirection, here))
							{
								killingFriend = true;
								break;
							}
						}
						else
						{
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
}