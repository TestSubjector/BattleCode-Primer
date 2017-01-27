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
		//Check nearby enemies
		int loopLength = enemies.length;
		// i++ gives a Dead Code warning since the loop never executes completely even once
		// if,else if,else blocks all have a break statement
		// TODO Check if loop can be removed
		for(int i = 0; i<loopLength;i++)
		{
			RobotInfo enemy = enemies[i];
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
		loopLength = neutralTrees.length;
		for(int i = 0; i<loopLength;i++)
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
	
			