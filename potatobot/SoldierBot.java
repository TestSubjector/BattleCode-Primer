package potatobot;
import battlecode.common.*;

public class SoldierBot extends Globals
{
	public static void loop()throws GameActionException
	{
		if (ourInitialArchons[0].distanceTo(theirInitialArchons[0]) > 35f)
		{
			movingDirection = here.directionTo(theirInitialArchons[0]);
		}
		while (true)
		{
			try
			{
				header();
	
				findMoveDirection();
				
				// movingDirection decided, now tryToMove
				if (!tryToMove(movingDirection) || movedBack)
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
				else
				{
					patience = 30;
				}
				shootClosestEnemy();
				
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