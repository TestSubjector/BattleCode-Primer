package SloBro;
import battlecode.common.*;

public class Tank extends RobotPlayer {

	RobotController rc;
	TankMemory mem;
	
	public Tank(RobotController rc)
	{
		this.rc = rc;
		mem = new TankMemory(rc);
	}
	
	public void run()
	{
		System.out.println("I am a Tank!");
		Team enemy = rc.getTeam().opponent();
		
		while (true)
    	{
    		try
    		{
    			// Look around and Record Important Information
    			RobotInfo[] enemies = rc.senseNearbyRobots(-1, enemy);
    			
    			// Attack If Need Be
    			if(enemies.length > 0)
    			{
    				if (rc.canFireSingleShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireSingleShot(rc.getLocation().directionTo(enemies[0].location));
                    }
    				if (!rc.hasAttacked())
    				{
    					Util.tryMove(rc, rc.getLocation().directionTo(enemies[0].location));
    				}
    			}
    			else
    			{

        			// Move Phase
        			Util.tryMove(rc, Util.randomDirection());
    			}
    			
    		}
    		catch (Exception e)
    		{
    			System.out.println("Tank Exception");
    			e.printStackTrace();
    		}
    		Clock.yield();
    	}
	}
}
