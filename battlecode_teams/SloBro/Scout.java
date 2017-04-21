package SloBro;
import battlecode.common.*;

public class Scout extends RobotPlayer {

	RobotController rc;
	ScoutMemory mem;
	
	public Scout(RobotController rc)
	{
		this.rc = rc;
		mem = new ScoutMemory(rc);
	}
	
	public void run()
	{
		initScout();
		
		
		while (true)
    	{
    		try
    		{
    			// Update Robots Memory with Information it can sense
    			mem.updateMemory();
    			logic();
    		}
    		catch (Exception e)
    		{
    			System.out.println("Scout Exception");
    			e.printStackTrace();
    		}
    		Clock.yield();
    	}
	}
	
	void logic() {
		try {
			mem.updateMemory();
			executeMove(); 
			executeAction(); 
			updateAreaOfInterest();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	void executeMove() throws GameActionException {
		avoidFriendlyScout();
		if (!Util.tryMove(rc, mem.getMyDirection())) {
			mem.setDirection(Util.randomDirection()); 
			Util.tryMove(rc, mem.getMyDirection());
		}
		
	}
	
	void executeAction() throws GameActionException {
		tryShake();
		harassFromCover();
	}
	
	int initScout()
	{
		try
		{
			int num = TeamComms.getScouts();
			return num;
		}
		catch (Exception e)
		{
			System.out.println("Error In Scout Init");
			return -1;
		}
		
	}
	
	boolean enemiesNearby()
	{
		return mem.enemiesInView.length > 0;
	}
	
	void harassFromCover()
	{
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		MapLocation tree_loc = null;
		// If enemies nearby & on a tree, move toward enemy
		try {
			if (rc.isLocationOccupiedByTree(rc.getLocation()) && enemies.length > 0)
			{
				if (enemies[0].getType() == RobotType.GARDENER || enemies[0].getType() == RobotType.ARCHON)
				{
					tree_loc = rc.getLocation();
					mem.setDirection(Util.getDirectionToLocation(rc, enemies[0].location).rotateRightDegrees(45));
					while (enemies.length > 0)
					{
					   Util.tryMove(rc, mem.getMyDirection());
					   MilitaryUtil.shootEnemy(rc, 0, enemies[0].ID);
					   returnToCover(tree_loc);
					   Clock.yield();
					}
				}
			}
		} catch (GameActionException e) {
			System.out.println("Error in Scout harassFromCover!");
			e.printStackTrace();
		}
	}
	
	void returnToCover(MapLocation tree_loc)
	{
		BulletInfo[] bullets = RobotPlayer.rc.senseNearbyBullets();
		
		if (bullets.length > 0)
		{
			mem.setDirection(Util.getDirectionToLocation(rc, tree_loc));
		}
	}
	
	void avoidFriendlyScout()
	{
	   RobotInfo[] robots = rc.senseNearbyRobots(3, rc.getTeam()); 	
	   
	   for (int i=0; i<robots.length; i++)
	   {
		   // If scout comes in contact with a friendly scout, change path 45 degrees to left
		   if (robots[i].type == RobotType.SCOUT)
		   {
			   mem.setDirection(Util.randomDirection());
		   }
	   }
	}
	
	// Attempts to fire at the first enemy seen this turn, 
	void fireAtFirstEnemy()
	{
		try
		{
			if (rc.canFireSingleShot()) {
	            // ...Then fire a bullet in the direction of the enemy.
	            rc.fireSingleShot(rc.getLocation().directionTo(mem.enemiesInView[0].location));
	        }
			if (!rc.hasAttacked())
			{
				// Try to follow the enemy if attacking couldn't happen
				Util.tryMove(rc, followEnemy(mem.enemiesInView[0], 8.5f));
			}
		} 
		catch (Exception e)
		{
			System.out.println("Scout Raised Exception while Firing");
			e.printStackTrace();
		}
	}
	
	void tryShake()
	{
		// if on a tree, attempt to shake
		if (rc.canInteractWithTree(rc.getLocation()) && rc.canShake(rc.getLocation()))
		{
			try {
				rc.shake(rc.getLocation());
			} catch (GameActionException e) {
				System.out.println("Scout raised an exception while attmepting to shake tree!");;
				e.printStackTrace();
			}
		}
	}
}
