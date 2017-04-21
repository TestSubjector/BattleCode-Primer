package SloBro;
import battlecode.common.*;

public abstract class RobotMemory {
	
	RobotController rc;
	public Team enemy;
	public RobotInfo[] enemiesInView;
	public RobotInfo[] enemiesInRange;
	public RobotInfo[] alliesInView;
	public RobotInfo[] alliesInRange;
	public TreeInfo[] trees;
	MapLocation archonLocation;
	MapLocation[] areasOfInterest;
	public  boolean canSeeOppArchon;
	
	public RobotMemory(RobotController rc)
	{
		this.rc = rc;
		enemy = rc.getTeam().opponent();
		updateMemory();
	}
	
	void updateMemory()
	{
		float attackRange = getAttackRange();
	
		// Update Entities that can be seen
		enemiesInView = rc.senseNearbyRobots(-1, enemy);
		enemiesInRange = rc.senseNearbyRobots(attackRange, enemy);
		alliesInView = rc.senseNearbyRobots(-1, rc.getTeam());
		alliesInRange = rc.senseNearbyRobots(attackRange, enemy);
		trees = rc.senseNearbyTrees();
		
		if(alliesInView.length > 0)
		{
			analyzeAllies();
		}
		
		if(enemiesInView.length > 0)
		{
			analyzeEnemies();
		}
	}
	
	void analyzeAllies()
	{
		for(int i = 0; i < alliesInView.length; i++)
		{
			RobotInfo ally = alliesInView[i];
			if(ally.getType().equals(RobotType.ARCHON))
			{
				archonLocation = ally.location;
			}
		}
	}
	
	void analyzeEnemies()
	{
		boolean canSeeArchon = false;
		try
		{
			for(int i = 0; i < enemiesInView.length; i++)
			{
				RobotInfo enemy = enemiesInView[i];
				if(enemy.getType().equals(RobotType.ARCHON))
				{
					TeamComms.broadcastOppArchon(enemy.location, enemy.ID);
					canSeeArchon = true;
				}
			}
			this.canSeeOppArchon = canSeeArchon;
		} 
		catch (GameActionException e)
		{
			System.out.println("Exception while analyzing enemies");
			e.printStackTrace();
		}
	}
	
	float getAttackRange()
	{
		RobotType type = rc.getType();
		switch(type){
		case ARCHON:
			return 0;
		case GARDENER:
			return 0;
		case SCOUT:
			return type.sensorRadius;
		case TANK:
			return type.sensorRadius;
		case SOLDIER:
			return type.sensorRadius;
		case LUMBERJACK:
			return GameConstants.LUMBERJACK_STRIKE_RADIUS;
		default:
			return 0;
			
		}
	}
}
