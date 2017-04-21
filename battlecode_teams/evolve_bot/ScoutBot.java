package evolve_bot;
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
				findMoveDirection();
				
				// movingDirection decided, now tryToMove
				if (!tryToMove(movingDirection))
				{
					movingDirection = movingDirection.rotateLeftRads(2.1f);
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
		// Check nearby neutral trees for bullets
		int loopLength = neutralTrees.length;
		for(int i = 0; i < loopLength; i++)
		{	
			TreeInfo tree = neutralTrees[i];
			if (tree.getContainedBullets() > 0)
			{
				movingDirection = here.directionTo(tree.getLocation());
				return;
			}
		}
		if (enemyTarget != 0)
		{
			movingDirection = here.directionTo(enemyTargetLocation);
			return;
		}
		else if (enemies.length != 0)
		{
			RobotInfo enemy = enemies[0];
			RobotType enemyType = enemy.getType();
			MapLocation enemyLocation = enemy.getLocation();
			if (!(enemyType == RobotType.SOLDIER || enemyType == RobotType.TANK || enemyType == RobotType.ARCHON) && (here.distanceTo(enemyLocation) < 7f))
			{
				movingDirection = here.directionTo(enemyLocation);
			}
			return;
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
	
			