package SloBro;

import battlecode.common.*;

public class Soldier extends RobotPlayer {
	RobotController rc;
	SoldierMemory mem;

	public Soldier(RobotController rc) {
		this.rc = rc;
		mem = new SoldierMemory(rc);
		
	}

	public void run() {
		// helper functions: isEnemyClose(), getEnemyLocation(),
		// shoot(location),
		while (true) 
		{
			try 
			{
				logic(1);
				Clock.yield();
			} 
			catch (Exception e) 
			{
				System.out.println("Soldier Exception");
				e.printStackTrace();
			}
		}
	}

	public void logic(int strat) throws GameActionException 
	{
		mem.updateMemory();
		switch (strat) {
		case 1:
			offense();
		case 2:
			//defense();
		}
	}

	public void offense() throws GameActionException
	{
		RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		TreeInfo[] enemyTrees = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
		
		// Fetch Current Area of Interest
		MapLocation aoi = TeamComms.getAreaOfMilitaryInterest();
		Direction dirToAOI = rc.getLocation().directionTo(aoi);
		Direction random = Util.randomDirection();
		
		// try and dodge if needed
		MilitaryUtil.dodge();

		if (robots.length > 0)
		{
			if(Util.pathClearTo(robots[0].location)) {
				MilitaryUtil.shootEnemy(rc, 0, robots[0].getID());
			} else if (Util.tryMove(rc, followEnemy(robots[0], 5f))) {
		
			}
			//followEnemy(robots[0], 2.5f);
			TeamComms.setAreaOfMilitaryInterest(robots[0].location);
		}
		else if (enemyTrees.length > 0 && rc.canFireSingleShot()) 
		{
			//if (Util.pathClearTo(enemyTrees[0].location)) {
				//MilitaryUtil.shootEnemy(rc, 0, enemyTrees[0].getID());
			//}
			rc.fireSingleShot(rc.getLocation().directionTo(enemyTrees[0].location));
		}
		else if (aoi != null && rc.canMove(aoi))
		{
			Util.tryMove(rc, dirToAOI);
		} 
		else if (moveTowardsEnemyArchon())
		{
			
		} 
		else if (rc.canMove(random))
		{
			Util.tryMove(rc, random);
		} 
		
		updateAreaOfInterest();
		
	}

	//public void defense() {
		/*if (SoldierMemory.getEnemyLocation() != null && rc.canSenseLocation(archonLoc)) {
			archon = rc.senseRobotAtLocation(archonLoc);
		}

		for (int i = 0; i < robots.length; i++) {
			if (robots[i].type == RobotType.ARCHON) {
				archon = robots[i];
				archonLoc = archon.location;
				TeamComms.broadcastOppArchon(rc, archonLoc);
			}
		}*/

	//}
	
	public boolean moveTowardsEnemyArchon() throws GameActionException
	{
		MapLocation enemyArchon = TeamComms.getClosestArchonLocation();
		System.out.println(enemyArchon);
		
		if (enemyArchon == null)
		{
			return false; 
		}
		
		Direction dirToEnemyArchon = rc.getLocation().directionTo(enemyArchon);
		if (rc.canMove(dirToEnemyArchon) && Util.tryMove(rc, dirToEnemyArchon))
		{
			return true; 
		} 
		else if (rc.canFireSingleShot() && MilitaryUtil.noFriendlyFire(rc, dirToEnemyArchon))
		{
			//rc.fireSingleShot(dirToEnemyArchon);
		}
		
		return false; 
	}
}
