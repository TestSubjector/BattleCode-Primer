package potatobot;
import battlecode.common.*;

public class ScoutBot extends Globals
{
	//private static boolean shotLastTurn;
	public static void loop()throws GameActionException
	{
		if (ourInitialArchons[0].distanceTo(theirInitialArchons[0]) > 35f)
		{
			movingDirection = randomDirection();
		}
		else
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
			shootClosestEnemy();
			
			footer();
		}
	}

	private static void shootClosestEnemy()throws GameActionException 
	{

		// look for the closest enemy and shoot
		for (RobotInfo enemy : enemies)
		{
			if ((roundNum > 500 || enemy.getType() != RobotType.ARCHON)  && trySingleShot(enemy))
			{
				break;
			}
		}
	}
	
	private static void findMoveDirection()throws GameActionException
	{
		//Check nearby enemies
		for (RobotInfo enemy : enemies)
		{
			RobotType enemyType = enemy.getType();
			MapLocation enemyLocation = enemy.getLocation();
			if (enemyType == RobotType.LUMBERJACK && here.distanceTo(enemyLocation) - myType.bodyRadius < 3.5f)
			{
				movingDirection = enemyLocation.directionTo(here);
				break;
			}
			else if (!(enemyType == RobotType.SOLDIER || enemyType == RobotType.TANK))
			{
				movingDirection = here.directionTo(enemyLocation);
				break;
			}
			else
			{
				movingDirection = enemyLocation.directionTo(here);
				break;
			}
		}
		
		// Check nearby neutral trees for bullets
		for (TreeInfo tree : neutralTrees)
		{
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
	
			