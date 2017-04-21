package SloBro;

import battlecode.common.*;

public class Lumberjack extends RobotPlayer {

	RobotController rc;
	LumberjackMemory mem;
	static float attackRange = RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS;

	public Lumberjack(RobotController rc) {
		this.rc = rc;
		mem = new LumberjackMemory(rc);
	}

	public void run() {
		while (true) {
			try {

				logic();

				Clock.yield();

			} catch (Exception e) {
				System.out.println("Lumberjack Exception");
				e.printStackTrace();
			}
		}
	}

	public void logic() throws GameActionException {
		// Gather Information Phase
		mem.updateMemory();

		//MilitaryUtil.dodge();

		// Attack Phase
		executeAttack();
		
		// Move Phase
		executeMove();
		
		// End of Turn Computations
		updateAreaOfInterest();
	}

	void executeMove() 
	{
		if (!mem.shouldMove()) 
		{
			return;
		}
		try
		{
			// Calculate where to move next
			Direction dir = calculateMoveDirection();

			// Move there 
			if (!Util.tryMove(rc, dir))
			{
				// try turning left first
				for(int i = 1; i < 6; i++)
				{
					if(Util.tryMove(rc, dir.rotateLeftDegrees(30 * i)))
						return;
				}
				
				// else try turning right
				for(int i = 1; i < 6; i++)
				{
					if(Util.tryMove(rc, dir.rotateRightDegrees(30 * i)))
						return;
				}
				
				Util.tryMove(rc, dir.opposite());
			}
		} 
		catch (GameActionException e) 
		{
			System.out.println("Lumberjack: Move Failed");
			e.printStackTrace();
		}
	}
	
	Direction calculateMoveDirection() throws GameActionException
	{
		RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		MapLocation aoe = TeamComms.getClosestArchonLocation();
		Direction dir = TeamComms.getDirectionToInitialArchonLoc();

		
		if (robots.length > 0) {
			return rc.getLocation().directionTo(robots[0].location);
		} 
		
		if(aoe != null) {
			System.out.println(aoe);
			return rc.getLocation().directionTo(aoe);
		
		}
		
		if (dir == null)
		{
			if(mem.enemiesInView.length > 0)
			{
				dir = rc.getLocation().directionTo(mem.enemiesInView[0].location);
			}
			else if (mem.trees.length > 0)
			{
				dir = rc.getLocation().directionTo(mem.trees[0].location);
			}
			else
			{
				dir = Util.randomDirection();
			}
		}
		return dir;
	}
	
	void executeAttack() {
		try
		{
			if (rc.canStrike() && mem.enemiesInRange.length > 0)
				rc.strike();
			else if (mem.shouldChop())
				chopTree();
		} 
		catch (GameActionException e) 
		{
			System.out.println("Lumberjack: executeAttack failed");
			e.printStackTrace();
		}
	}

	void chopTree() 
	{
		TreeInfo tochop = mem.trees[0];
		if (tochop == null || !rc.canChop(tochop.ID))
		{
			mem.isChopping = false;
			return;
		}
		try
		{
			if (rc.canChop(tochop.ID) && tochop.getTeam() != rc.getTeam()) {
				rc.chop(tochop.ID);
				mem.isChopping = true;
			}

		}
		catch (Exception e) 
		{
			System.out.println("Lumberjack: Chop Failed");
			e.printStackTrace();
		}
	}

}
