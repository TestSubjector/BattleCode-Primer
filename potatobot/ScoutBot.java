package potatobot;
import battlecode.common.*;

public class ScoutBot extends Globals
{
	//private static boolean shotLastTurn;
	public static void loop()throws GameActionException
	{
		movingDirection = here.directionTo(theirInitialArchons[0]);
		while (true)
		{
			try
			{
				header();
				System.out.println("After header : " + Clock.getBytecodesLeft());
				findMoveDirection();
				System.out.println("After moveDirection : " + Clock.getBytecodesLeft());
				
				// movingDirection decided, now tryToMove
				if (!tryToMove(movingDirection))
				{
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
				System.out.println("After tryToMove : " + Clock.getBytecodesLeft());
				shootClosestEnemy();
				System.out.println("After shootClosestEnemy : " + Clock.getBytecodesLeft());
				footer();
			}
			catch (GameActionException e)
			{
				System.out.println("Catch kiya");
				footer();
			}
		}
	}

	private static void shootClosestEnemy()throws GameActionException 
	{

		// look for the closest enemy and shoot
		int loopLength = enemies.length;
		for(int i=0;i<loopLength;i++)
		{
			RobotInfo enemy = enemies[i];
			if ((roundNum > 500 || enemy.getType() != RobotType.ARCHON)  && trySingleShot(enemy))
			{
				break;
			}
		}
	}
	

	private static void findMoveDirection()throws GameActionException
	{
		if (enemies.length != 0)
		{
			RobotInfo enemy = enemies[0];
			RobotType enemyType = enemy.getType();
			MapLocation enemyLocation = enemy.getLocation();
			movingDirection = here.directionTo(enemyLocation);
			if ((enemyType == RobotType.SOLDIER || enemyType == RobotType.TANK || enemyType == RobotType.ARCHON))
			{
				float angle = 45;
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
		// Check nearby neutral trees for bullets
		int loopLength = neutralTrees.length;
		for(int i = 0; i< loopLength;i++)
		{	
			TreeInfo tree = neutralTrees[i];
			if (tree.getContainedBullets() > 0)
			{
				movingDirection = here.directionTo(tree.getLocation());
				return;
			}
		}
	}
	
	/* Deprecated function, would have needed this if the scouts travelled faster than their bullets like they used to
	public static boolean tryToMove(Direction movingDirection)throws GameActionException
	{
		if (shotLastTurn)
		{
			return tryToMoveThisMuch(movingDirection, (myType.strideRadius * 2 / 3));
		}
		return Globals.tryToMove(movingDirection);
	} */
	
}
	
			