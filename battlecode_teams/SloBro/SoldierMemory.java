package SloBro;
import battlecode.common.*;

public class SoldierMemory extends RobotMemory
{
	RobotController rc;
    RobotInfo enemyArchon[]; 
    MapLocation EnemyArchonLoc;
	
	public SoldierMemory(RobotController rc)
	{
		super(rc);
	}
	
	public void updateMemory()
	{
		super.updateMemory();
		
	}
	
	public MapLocation getImportantLoc()
	{
		return null;
	}
	
	public MapLocation getEnemyLocation() {
		if (enemyArchon != null) {
			//if ()
			
		} 
		return null; 
	}
}
