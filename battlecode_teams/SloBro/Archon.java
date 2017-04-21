package SloBro;
import battlecode.common.*;

public class Archon extends RobotPlayer {

	RobotController rc;
	ArchonMemory mem;
	
	public Archon(RobotController rc)
	{
		this.rc = rc;
		mem = new ArchonMemory(rc);
	}
	
	public void run() throws GameActionException {				
		initArchon(rc);

		while (true) {
            try {
            	
            	logic(); 
            	Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
	}
	
	
	
	public void logic() throws GameActionException {
		// Generate a random direction
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        Direction dir = TeamComms.getDirectionToInitialArchonLoc();
        Direction optionDir = Util.randomDirection();
        
        checkDonation();
        
        //updateAreaOfInterest();

        // Attempt to deploy with a max number of gardeners
        if (rc.canBuildRobot(RobotType.GARDENER, dir))
        {
        	// This function builds a gardener if possible, and increments the unit count
        	deployGardener(dir);
        } else if (rc.canBuildRobot(RobotType.GARDENER, optionDir)) {
        	deployGardener(optionDir);
        }
        
        // Look for enemies and move away from them
        if (enemies.length > 0)
        {
        	Util.tryMove(rc, (rc.getLocation().directionTo(enemies[0].location)).opposite());
        }
        	
        // Broadcast archon's location for other robots on the team to know
        TeamComms.broadcastArchonLoc();
	}
	
	void initArchon(RobotController rc) throws GameActionException
	{
		System.out.println("I'm an archonnn!");
		TeamComms.broadcastArchonLoc();
		TeamComms.updateGardeners(0);
		TeamComms.updateSoldiers(0);
		TeamComms.updateLumberjacks(0);
		TeamComms.updateScouts(0);
		TeamComms.updateTanks(0);
		broadcastEnemyArchon(rc.getTeam().opponent());
		TeamComms.broadcastNumArchons();
		
		// Generate Initial Area of Interest to Explore
		MapLocation[] areasOfInterest = rc.getInitialArchonLocations(rc.getTeam().opponent());
		if(areasOfInterest.length > 0)
		{
			TeamComms.setAreaOfInterest(areasOfInterest[0]);
		}
	}
	
	void broadcastEnemyArchon(Team enemy) throws GameActionException {
		MapLocation[] enemyArchon = rc.getInitialArchonLocations(enemy);
		
		Util.printMapList(enemyArchon);
		
		for (int i = 0; i < enemyArchon.length; i++) {
			TeamComms.broadcastOppArchon(enemyArchon[i], -1 - i);
		}
				
	}
	
	void deployGardener(Direction dir) throws GameActionException
	{
		// Attempt to hire a gardener
		if (rc.canHireGardener(dir))
		{
			rc.hireGardener(dir);
			incrementCount(RobotType.GARDENER);
		}
	}
	
	void updateTeamAreaOfInterest() throws GameActionException
	{
		if(rc.getRoundNum() - TeamComms.getLastArchonSighting() < 5)
		{
			MapLocation l = TeamComms.getOppArchonLoc();
			TeamComms.setAreaOfInterest(l);
		}
	}
	
	void checkDonation() throws GameActionException {
		double cost = 7.5 + (rc.getRoundNum())*12.5 / 3000;
		if (rc.getTeamBullets() / cost > 1000) {
			rc.donate(rc.getTeamBullets());
			
		}
	}
	
}
